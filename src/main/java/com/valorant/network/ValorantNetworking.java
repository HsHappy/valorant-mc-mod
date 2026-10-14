package com.valorant.network;

import com.valorant.ValorantMod;
import com.valorant.client.recoil.WeaponRecoilState;
import com.valorant.combat.HitscanEngine;
import com.valorant.combat.TracerHelper;
import com.valorant.sound.ValorantSounds;
import com.valorant.weapon.GunAmmoComponent;
import com.valorant.weapon.WeaponData;
import com.valorant.weapon.WeaponItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Hardened Networking Engine for Valorant Ballistics.
 *
 * Implements:
 * 1. Zero Trust Client Architecture: The client only informs the server of firing and its camera angles.
 * 2. Server-side authoritative recoil, bloom, movement error, and consecutive shot simulation.
 * 3. Strict fire-rate limiting and packet spam defense against macros / hacked clients.
 * 4. Automatic tracker memory cleanup on player disconnect.
 */
public class ValorantNetworking {

    public static void registerCommon() {
        PayloadTypeRegistry.playC2S().register(FirePayload.ID, FirePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReloadPayload.ID, ReloadPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CancelReloadPayload.ID, CancelReloadPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FirePayload.ID, (payload, context) -> {
            context.server().execute(() -> handleFire(context.player(), payload));
        });

        ServerPlayNetworking.registerGlobalReceiver(ReloadPayload.ID, (payload, context) -> {
            context.server().execute(() -> handleReload(context.player()));
        });

        ServerPlayNetworking.registerGlobalReceiver(CancelReloadPayload.ID, (payload, context) -> {
            context.server().execute(() -> handleCancelReload(context.player()));
        });

        // Clean up per-player recoil trackers when a player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerRecoilTracker.removePlayer(handler.player.getUUID());
        });
    }

    private static void handleFire(ServerPlayer player, FirePayload payload) {
        if (player == null || player.isSpectator() || !player.isAlive()) {
            return;
        }

        // 1. Rotation Sanity Validation (Anti-Cheat / Malformed Input Check)
        float camPitch = payload.cameraPitch();
        float camYaw = payload.cameraYaw();
        if (Float.isNaN(camPitch) || Float.isInfinite(camPitch) ||
            Float.isNaN(camYaw) || Float.isInfinite(camYaw)) {
            return;
        }
        camPitch = Math.clamp(camPitch, -90.0f, 90.0f);
        camYaw = Mth.wrapDegrees(camYaw);

        ServerLevel level = player.serverLevel();
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof WeaponItem weaponItem)) {
            return;
        }

        WeaponData data = weaponItem.getWeaponData();
        ServerPlayerRecoilTracker.PlayerRecoilContext tracker = ServerPlayerRecoilTracker.getContext(player.getUUID());
        tracker.checkWeaponSwitch(data.id());

        long now = System.currentTimeMillis();

        // 2. Strict Fire Rate Limit Validation (Anti-Packet-Spam / Rapid-Fire Macro Check)
        double fireRateRps = Math.max(0.1, data.fireRateRps());
        double minIntervalMs = 1000.0 / fireRateRps;
        long timeSinceLastShot = now - tracker.getLastShotTime();

        // 15% + 15ms buffer to account for normal network latency fluctuations
        double minAllowedIntervalMs = Math.max(10.0, minIntervalMs * 0.85 - 15.0);
        if (tracker.getLastShotTime() > 0 && timeSinceLastShot < minAllowedIntervalMs) {
            // Rapid-fire packet flood detected — reject packet silently
            return;
        }

        GunAmmoComponent ammo = stack.getOrDefault(ValorantMod.GUN_AMMO, 
            new GunAmmoComponent(data.magazineSize(), data.magazineSize() * 3, false, 0));

        // 3. Ammo & Reload State Verification
        if (ammo.isReloading()) {
            if (now >= ammo.reloadFinishTime()) {
                ammo = ammo.finishReload(data.magazineSize());
                stack.set(ValorantMod.GUN_AMMO, ammo);
            } else {
                return; // Reload currently in progress
            }
        }

        if (ammo.currentAmmo() <= 0) {
            level.playSound(null, player, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.6f);
            return;
        }

        // 4. Update Server Recoil State & Authoritatively Calculate Trajectory
        WeaponRecoilState recoilState = tracker.getRecoilState();
        WeaponRecoilState.ShotTrajectory trajectory = recoilState.calculateShot(player, data);
        tracker.setLastShotTime(now);

        // Deduct 1 ammo authoritatively on server
        stack.set(ValorantMod.GUN_AMMO, ammo.withCurrent(ammo.currentAmmo() - 1));

        // 5. Authoritative Hitscan Raycast with Server Recoil & Lag Compensation
        HitscanEngine.ShotResult result = HitscanEngine.fireRaycast(
            player, data, 100.0,
            camPitch, camYaw,
            trajectory.pitchOffset(), trajectory.yawOffset(), trajectory.spread()
        );

        // 6. Gunshot Sound
        SoundEvent fireSound = ValorantSounds.getFireSound(data.id());
        level.playSound(null, player, fireSound, SoundSource.PLAYERS, 1.2f, 1.0f);

        // 7. Tracers, Impacts & Bullet Holes
        TracerHelper.spawnTracer(
            level, player.getEyePosition(), result.hitLocation(),
            result.zone(), result.hitFace()
        );

        // 8. Hit Damage & Feedback Sounds
        if (result.target() != null) {
            float dmg = (float) result.damageDealt();
            Vec3 oldVel = result.target().getDeltaMovement();
            result.target().invulnerableTime = 0;

            // Apply direct damage
            result.target().hurt(player.damageSources().playerAttack(player), dmg);

            // Valorant Tagging: Cancel vanilla flying knockback and apply movement slow
            result.target().setDeltaMovement(oldVel.x * 0.25, Math.min(oldVel.y, 0.0), oldVel.z * 0.25);
            result.target().hurtMarked = true;
            result.target().invulnerableTime = 0; // Reset invulnerability so rapid-fire spray bullets all deal damage

            boolean isKilled = result.target().isDeadOrDying() || result.target().getHealth() <= 0.0f;

            if (isKilled) {
                // Kill confirmation sound
                player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.2f, 0.8f);
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.9f, 1.6f);
            } else {
                if (result.zone() == HitscanEngine.HitZone.HEAD) {
                    // Crisp Headshot DINK
                    player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.1f, 1.8f);
                    player.playNotifySound(SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.3f);
                } else if (result.zone() == HitscanEngine.HitZone.BODY) {
                    // Bodyshot thud
                    player.playNotifySound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 0.8f, 1.1f);
                } else {
                    // Legshot / foot thud
                    player.playNotifySound(SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 0.7f, 0.9f);
                }
            }

            // Real-time Tactical Combat Feedback for Mob & Player Testing
            String targetName = result.target().getName().getString();
            float remainingHp = Math.max(0.0f, result.target().getHealth());
            String zoneTag = switch (result.zone()) {
                case HEAD -> "§c✦ [KAFA / HEAD]";
                case BODY -> "§6■ [GÖVDE / BODY]";
                case LEG -> "§e▼ [BACAK-AYAK / LEGS]";
                default -> "§7[HIT]";
            };

            String status = isKilled ? "§a§l☠ ELENDİ!" : String.format("§7(Kalan: §f%.1f HP§7)", remainingHp);
            player.displayClientMessage(
                Component.literal(String.format("%s §f-%.1f %s §8(%s)", zoneTag, dmg, status, targetName)),
                true
            );
        }
    }

    private static void handleReload(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof WeaponItem weaponItem)) {
            return;
        }

        WeaponData data = weaponItem.getWeaponData();
        GunAmmoComponent ammo = stack.getOrDefault(ValorantMod.GUN_AMMO, 
            new GunAmmoComponent(data.magazineSize(), data.magazineSize() * 3, false, 0));

        if (ammo.isReloading() || ammo.currentAmmo() >= data.magazineSize() || ammo.reserveAmmo() <= 0) {
            return;
        }

        long finishTime = System.currentTimeMillis() + (long) (data.reloadTimeSeconds() * 1000);
        stack.set(ValorantMod.GUN_AMMO, ammo.startReload(finishTime));

        SoundEvent reloadSound = ValorantSounds.getReloadSound(data.id());
        // Pass 'player' as except so other players hear 3D spatial sound, while local player plays centered 1P sound
        player.serverLevel().playSound(player, player, reloadSound, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void handleCancelReload(ServerPlayer player) {
        if (player == null || !player.isAlive()) {
            return;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof WeaponItem) {
                GunAmmoComponent ammo = stack.get(ValorantMod.GUN_AMMO);
                if (ammo != null && ammo.isReloading()) {
                    stack.set(ValorantMod.GUN_AMMO, ammo.cancelReload());
                }
            }
        }

        if (ValorantSounds.VANDAL_RELOAD != null) {
            ClientboundStopSoundPacket stopVandal = new ClientboundStopSoundPacket(ValorantSounds.VANDAL_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopVandal);
        }
        if (ValorantSounds.PHANTOM_RELOAD != null) {
            ClientboundStopSoundPacket stopPhantom = new ClientboundStopSoundPacket(ValorantSounds.PHANTOM_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopPhantom);
        }
        if (ValorantSounds.FRENZY_RELOAD != null) {
            ClientboundStopSoundPacket stopFrenzy = new ClientboundStopSoundPacket(ValorantSounds.FRENZY_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopFrenzy);
        }
        if (ValorantSounds.GHOST_RELOAD != null) {
            ClientboundStopSoundPacket stopGhost = new ClientboundStopSoundPacket(ValorantSounds.GHOST_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopGhost);
        }
        if (ValorantSounds.SHERIFF_RELOAD != null) {
            ClientboundStopSoundPacket stopSheriff = new ClientboundStopSoundPacket(ValorantSounds.SHERIFF_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopSheriff);
        }
        if (ValorantSounds.SHORTY_RELOAD != null) {
            ClientboundStopSoundPacket stopShorty = new ClientboundStopSoundPacket(ValorantSounds.SHORTY_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopShorty);
        }
        if (ValorantSounds.BUCKY_RELOAD != null) {
            ClientboundStopSoundPacket stopBucky = new ClientboundStopSoundPacket(ValorantSounds.BUCKY_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopBucky);
        }
        if (ValorantSounds.JUDGE_RELOAD != null) {
            ClientboundStopSoundPacket stopJudge = new ClientboundStopSoundPacket(ValorantSounds.JUDGE_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopJudge);
        }
        if (ValorantSounds.STINGER_RELOAD != null) {
            ClientboundStopSoundPacket stopStinger = new ClientboundStopSoundPacket(ValorantSounds.STINGER_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopStinger);
        }
        if (ValorantSounds.MARSHAL_RELOAD != null) {
            ClientboundStopSoundPacket stopMarshal = new ClientboundStopSoundPacket(ValorantSounds.MARSHAL_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopMarshal);
        }
        if (ValorantSounds.SPECTRE_RELOAD != null) {
            ClientboundStopSoundPacket stopSpectre = new ClientboundStopSoundPacket(ValorantSounds.SPECTRE_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopSpectre);
        }
        if (ValorantSounds.BULLDOG_RELOAD != null) {
            ClientboundStopSoundPacket stopBulldog = new ClientboundStopSoundPacket(ValorantSounds.BULLDOG_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopBulldog);
        }
        if (ValorantSounds.GUARDIAN_RELOAD != null) {
            ClientboundStopSoundPacket stopGuardian = new ClientboundStopSoundPacket(ValorantSounds.GUARDIAN_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopGuardian);
        }
        if (ValorantSounds.OUTLAW_RELOAD != null) {
            ClientboundStopSoundPacket stopOutlaw = new ClientboundStopSoundPacket(ValorantSounds.OUTLAW_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopOutlaw);
        }
        if (ValorantSounds.OPERATOR_RELOAD != null) {
            ClientboundStopSoundPacket stopOperator = new ClientboundStopSoundPacket(ValorantSounds.OPERATOR_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopOperator);
        }
        if (ValorantSounds.WEAPON_RELOAD != null) {
            ClientboundStopSoundPacket stopGeneric = new ClientboundStopSoundPacket(ValorantSounds.WEAPON_RELOAD.getLocation(), SoundSource.PLAYERS);
            player.serverLevel().getChunkSource().broadcastAndSend(player, stopGeneric);
        }
    }
}
