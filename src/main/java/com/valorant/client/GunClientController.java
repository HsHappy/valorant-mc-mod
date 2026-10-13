package com.valorant.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.valorant.ValorantMod;
import com.valorant.client.recoil.RecoilEngine;
import com.valorant.network.CancelReloadPayload;
import com.valorant.network.FirePayload;
import com.valorant.network.ReloadPayload;
import com.valorant.sound.ValorantSounds;
import com.valorant.weapon.GunAmmoComponent;
import com.valorant.weapon.WeaponData;
import com.valorant.weapon.WeaponItem;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class GunClientController {
    public static KeyMapping RELOAD_KEY;
    public static KeyMapping RECOIL_FOLLOW_KEY;
    private static long lastShotTime = 0;
    private static long lastFrameNano = 0;
    private static String lastHeldWeaponId = "";
    private static int lastSelectedSlot = -1;

    public static void playLocalReloadSound(String weaponId) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getSoundManager() == null) return;

        SoundEvent reloadSound = ValorantSounds.getReloadSound(weaponId);
        if (reloadSound == null) return;

        stopReloadSound();

        // Play 1P First-Person centered non-attenuated audio directly into player's ears
        client.getSoundManager().play(
            new SimpleSoundInstance(
                reloadSound.getLocation(),
                SoundSource.PLAYERS,
                1.0f,
                1.0f,
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0,
                0.0,
                0.0,
                true
            )
        );
    }

    public static void stopReloadSound() {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.getSoundManager() != null) {
            if (ValorantSounds.VANDAL_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.VANDAL_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.PHANTOM_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.PHANTOM_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.FRENZY_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.FRENZY_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.GHOST_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.GHOST_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.SHERIFF_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.SHERIFF_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.SHORTY_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.SHORTY_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.BUCKY_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.BUCKY_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.JUDGE_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.JUDGE_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.STINGER_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.STINGER_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.MARSHAL_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.MARSHAL_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.SPECTRE_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.SPECTRE_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.BULLDOG_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.BULLDOG_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.GUARDIAN_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.GUARDIAN_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.OUTLAW_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.OUTLAW_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.OPERATOR_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.OPERATOR_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.ARES_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.ARES_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.ODIN_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.ODIN_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
            if (ValorantSounds.WEAPON_RELOAD != null) {
                client.getSoundManager().stop(ValorantSounds.WEAPON_RELOAD.getLocation(), SoundSource.PLAYERS);
            }
        }
    }

    public static boolean cancelReloadForInventory(Player player, ItemStack currentHeld) {
        if (player == null) return false;
        boolean cancelled = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof WeaponItem) {
                GunAmmoComponent ammo = stack.get(ValorantMod.GUN_AMMO);
                if (ammo != null && ammo.isReloading()) {
                    if (stack != currentHeld) {
                        stack.set(ValorantMod.GUN_AMMO, ammo.cancelReload());
                        cancelled = true;
                    }
                }
            }
        }
        return cancelled;
    }

    // Semi-automatic trigger tracking
    private static boolean attackKeyPressedLastFrame = false;
    private static long lastTriggerPressTime = 0;

    /**
     * Determines whether a weapon requires single clicks per shot (semi-automatic)
     * vs continuous fire while holding Mouse 1 (full-automatic).
     */
    public static boolean isSemiAutomaticWeapon(String weaponId) {
        return switch (weaponId) {
            case "classic", "sheriff", "operator", "marshal", "ghost", "guardian", "outlaw", "shorty", "bucky" -> true;
            default -> false; // vandal, phantom, spectre, stinger, odin, ares, frenzy, bulldog are full-auto
        };
    }

    public static void initialize() {
        // Register 'R' Key for Reload
        RELOAD_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.valorant.reload",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.valorant"
        ));

        // Register 'H' Key for Recoil Follow Toggle
        RECOIL_FOLLOW_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.valorant.recoil_follow",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.valorant"
        ));

        // 1. High-Frequency Frame Loop (Render Thread): Smooth 144Hz+ spring camera physics & sub-tick firing
        WorldRenderEvents.START.register(context -> {
            Minecraft client = Minecraft.getInstance();
            Player player = client.player;
            if (player == null || !player.isAlive()) return;

            long currentNano = System.nanoTime();
            if (lastFrameNano == 0) lastFrameNano = currentNano;
            float dt = (currentNano - lastFrameNano) / 1_000_000_000.0f;
            lastFrameNano = currentNano;
            dt = Math.min(dt, 0.05f); // Prevent spring jumps on lag spikes

            // Update Spring-Damper Camera Recoil & Ballistic Decay every frame
            RecoilEngine.update(player, dt);

            // Cannot fire if inside a screen/menu
            if (client.screen != null) {
                attackKeyPressedLastFrame = false;
                lastTriggerPressTime = 0;
                return;
            }

            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof WeaponItem weaponItem)) return;

            WeaponData data = weaponItem.getWeaponData();
            long now = System.currentTimeMillis();

            boolean attackDown = client.options.keyAttack.isDown();
            boolean semiAuto = isSemiAutomaticWeapon(data.id());

            // Track fresh click (rising edge)
            if (attackDown && !attackKeyPressedLastFrame) {
                lastTriggerPressTime = now;
            }
            attackKeyPressedLastFrame = attackDown;

            // Semi-auto weapons only fire once per click (with input buffer), full-auto fires while held
            boolean shouldFire = semiAuto
                ? (attackDown && (now - lastTriggerPressTime <= 150))
                : attackDown;

            if (shouldFire) {
                GunAmmoComponent ammo = mainHand.getOrDefault(ValorantMod.GUN_AMMO,
                    new GunAmmoComponent(data.magazineSize(), data.magazineSize() * 3, false, 0));

                double intervalMs = 1000.0 / Math.max(0.1, data.fireRateRps());

                if (now - lastShotTime >= intervalMs) {
                    if (ammo.currentAmmo() > 0 && !ammo.isReloading()) {
                        lastShotTime = now;
                        lastTriggerPressTime = 0; // Consume click for semi-auto (prevents auto-repeat while held)

                        // Deduct ammo predictively on client
                        int newAmmo = ammo.currentAmmo() - 1;
                        ammo = ammo.withCurrent(newAmmo);
                        mainHand.set(ValorantMod.GUN_AMMO, ammo);

                        // Calculate predictive client recoil for local visual feedback & screen punch
                        RecoilEngine.calculateShot(player, data);

                        // Send fire packet with player's instantaneous camera rotation
                        ClientPlayNetworking.send(new FirePayload(
                            player.getXRot(),
                            player.getYRot()
                        ));

                        // Local swing animation
                        player.swing(InteractionHand.MAIN_HAND);

                        // Update HUD action bar
                        displayAmmoActionBar(player, data.displayName(), newAmmo, ammo.reserveAmmo(), false);
                    } else if (ammo.currentAmmo() <= 0 && !ammo.isReloading() && ammo.reserveAmmo() > 0) {
                        lastTriggerPressTime = 0;
                        // Auto reload when attempting to shoot empty magazine
                        long finishTime = now + (long) (data.reloadTimeSeconds() * 1000);
                        ammo = ammo.startReload(finishTime);
                        mainHand.set(ValorantMod.GUN_AMMO, ammo);
                        ClientPlayNetworking.send(new ReloadPayload());
                        playLocalReloadSound(data.id());
                        displayAmmoActionBar(player, data.displayName(), 0, ammo.reserveAmmo(), true);
                    }
                }
            }
        });

        // 2. Client Tick Loop (20 TPS): Handles Reload Countdown, Key Inputs & Weapon Switching
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Player player = client.player;
            if (player == null || client.screen != null) return;
            if (!player.isAlive()) {
                stopReloadSound();
                return;
            }

            // Tick recoil state to capture tick boundaries for smooth frame interpolation
            RecoilEngine.clientTick(player);

            ItemStack mainHand = player.getMainHandItem();
            int currentSlot = player.getInventory().selected;
            boolean slotChanged = (lastSelectedSlot != -1 && currentSlot != lastSelectedSlot);
            lastSelectedSlot = currentSlot;

            if (cancelReloadForInventory(player, mainHand)) {
                stopReloadSound();
                ClientPlayNetworking.send(new CancelReloadPayload());
            }

            if (!(mainHand.getItem() instanceof WeaponItem weaponItem)) {
                if (!lastHeldWeaponId.isEmpty()) {
                    RecoilEngine.reset();
                    lastHeldWeaponId = "";
                    stopReloadSound();
                    ClientPlayNetworking.send(new CancelReloadPayload());
                }
                return;
            }

            WeaponData data = weaponItem.getWeaponData();
            if (!data.id().equals(lastHeldWeaponId)) {
                RecoilEngine.reset();
                lastHeldWeaponId = data.id();
                stopReloadSound();
                ClientPlayNetworking.send(new CancelReloadPayload());
            }

            GunAmmoComponent ammo = mainHand.getOrDefault(ValorantMod.GUN_AMMO,
                new GunAmmoComponent(data.magazineSize(), data.magazineSize() * 3, false, 0));

            long now = System.currentTimeMillis();

            // Toggle Recoil Follow crosshair tracking ('H')
            while (RECOIL_FOLLOW_KEY.consumeClick()) {
                RecoilEngine.recoilFollowEnabled = !RecoilEngine.recoilFollowEnabled;
                player.displayClientMessage(
                    Component.literal("§b[Valorant] §fCrosshair Recoil Follow: " + (RecoilEngine.recoilFollowEnabled ? "§aON" : "§cOFF (Static Crosshair)")),
                    true
                );
            }

            // Process Active Reload Timer
            if (ammo.isReloading()) {
                if (now >= ammo.reloadFinishTime()) {
                    // Reload Finished
                    ammo = ammo.finishReload(data.magazineSize());
                    mainHand.set(ValorantMod.GUN_AMMO, ammo);
                    displayAmmoActionBar(player, data.displayName(), ammo.currentAmmo(), ammo.reserveAmmo(), false);
                } else {
                    // Show remaining countdown
                    double remaining = (ammo.reloadFinishTime() - now) / 1000.0;
                    player.displayClientMessage(
                        Component.literal(String.format("§e⟳ Reloading %s... (%.1fs)", data.displayName(), Math.max(0.1, remaining))),
                        true
                    );
                    return;
                }
            }

            // Check Reload Key 'R'
            while (RELOAD_KEY.consumeClick()) {
                if (!ammo.isReloading() && ammo.currentAmmo() < data.magazineSize() && ammo.reserveAmmo() > 0) {
                    long finishTime = now + (long) (data.reloadTimeSeconds() * 1000);
                    ammo = ammo.startReload(finishTime);
                    mainHand.set(ValorantMod.GUN_AMMO, ammo);
                    ClientPlayNetworking.send(new ReloadPayload());
                    playLocalReloadSound(data.id());
                    displayAmmoActionBar(player, data.displayName(), ammo.currentAmmo(), ammo.reserveAmmo(), true);
                    return;
                }
            }
        });
    }

    private static void displayAmmoActionBar(Player player, String weaponName, int current, int reserve, boolean reloading) {
        if (reloading) {
            player.displayClientMessage(Component.literal("§e⟳ Reloading " + weaponName + "..."), true);
        } else {
            String ammoColor = current <= 5 ? "§c" : "§f";
            player.displayClientMessage(
                Component.literal("§6" + weaponName + "  " + ammoColor + current + " §7/ §f" + reserve),
                true
            );
        }
    }
}
