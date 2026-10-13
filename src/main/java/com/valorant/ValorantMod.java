package com.valorant;

import com.valorant.network.ValorantNetworking;
import com.valorant.sound.ValorantSounds;
import com.valorant.weapon.GunAmmoComponent;
import com.valorant.weapon.ValorantItemGroups;
import com.valorant.weapon.WeaponRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValorantMod implements ModInitializer {
    public static final String MOD_ID = "valorant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static DataComponentType<GunAmmoComponent> GUN_AMMO;
    public static net.minecraft.core.particles.SimpleParticleType BULLET_HOLE;

    @Override
    public void onInitialize() {
        LOGGER.info("[Valorant MC] Initializing tactical FPS engine...");

        // Register Data Component for Ammo
        GUN_AMMO = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            id("gun_ammo"),
            DataComponentType.<GunAmmoComponent>builder()
                .persistent(GunAmmoComponent.CODEC)
                .networkSynchronized(GunAmmoComponent.STREAM_CODEC)
                .build()
        );

        // Register Particles
        BULLET_HOLE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            id("bullet_hole"),
            net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple(true)
        );

        // Register Sounds
        ValorantSounds.registerSounds();

        // Register Weapons & Items
        WeaponRegistry.registerWeapons();
        ValorantItemGroups.register();

        // Register Networking Payloads & Handlers
        ValorantNetworking.registerCommon();

        // Server tick listener to finalize reloading on server
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.currentTimeMillis();
            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof com.valorant.weapon.WeaponItem weaponItem) {
                    GunAmmoComponent ammo = stack.get(GUN_AMMO);
                    if (ammo != null && ammo.isReloading() && now >= ammo.reloadFinishTime()) {
                        stack.set(GUN_AMMO, ammo.finishReload(weaponItem.getWeaponData().magazineSize()));
                    }
                }
            }
        });

        LOGGER.info("[Valorant MC] Firing, Ammo & Networking loaded successfully.");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
