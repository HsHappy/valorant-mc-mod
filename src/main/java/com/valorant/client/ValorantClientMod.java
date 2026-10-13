package com.valorant.client;

import com.valorant.ValorantMod;
import com.valorant.client.particle.BulletHoleParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class ValorantClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ValorantMod.LOGGER.info("[Valorant MC] Initializing Client Engine (HUD, Recoil, Crosshairs)...");
        
        // Initialize Gun Firing, Reload & Keybindings
        GunClientController.initialize();

        // Register Custom Particles
        ParticleFactoryRegistry.getInstance().register(ValorantMod.BULLET_HOLE, BulletHoleParticle.Provider::new);
    }
}
