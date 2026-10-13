package com.valorant.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class BulletHoleParticle extends TextureSheetParticle {

    protected BulletHoleParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.gravity = 0.0f;
        this.lifetime = 20 * 20; // Stays on wall for 20 seconds!
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.quadSize = 0.072f; // Realistic bullet puncture diameter
        this.hasPhysics = false;
        this.alpha = 1.0f;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Smoothly fade out in the last 2 seconds (40 ticks)
        if (this.age > this.lifetime - 40) {
            this.alpha = Math.max(0.0f, (float)(this.lifetime - this.age) / 40.0f);
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new BulletHoleParticle(level, x, y, z, this.sprites);
        }
    }
}
