package com.valorant.combat;

import com.valorant.ValorantMod;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class TracerHelper {
    public static void spawnTracer(ServerLevel level, Vec3 from, Vec3 to, HitscanEngine.HitZone zone, Direction face) {
        Vec3 direction = to.subtract(from);
        double distance = direction.length();
        Vec3 step = direction.normalize().scale(1.5); // Particle every 1.5 blocks

        double currentDist = 1.0; // Start slightly in front of player
        Vec3 currentPos = from.add(direction.normalize().scale(currentDist));

        while (currentDist < distance) {
            level.sendParticles(
                ParticleTypes.CRIT,
                currentPos.x, currentPos.y, currentPos.z,
                1, 0.0, 0.0, 0.0, 0.0
            );
            currentPos = currentPos.add(step);
            currentDist += 1.5;
        }

        // Zone-specific impact feedback particles
        if (zone == HitscanEngine.HitZone.HEAD) {
            // Bright golden critical spark burst on headshot
            level.sendParticles(
                ParticleTypes.CRIT,
                to.x, to.y, to.z,
                12, 0.15, 0.15, 0.15, 0.20
            );
            level.sendParticles(
                ParticleTypes.ENCHANTED_HIT,
                to.x, to.y, to.z,
                8, 0.10, 0.10, 0.10, 0.15
            );
        } else if (zone == HitscanEngine.HitZone.BODY) {
            // Flesh impact and blood/damage indicator particles
            level.sendParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                to.x, to.y, to.z,
                2, 0.10, 0.10, 0.10, 0.05
            );
            level.sendParticles(
                ParticleTypes.CRIT,
                to.x, to.y, to.z,
                4, 0.08, 0.08, 0.08, 0.10
            );
        } else if (zone == HitscanEngine.HitZone.LEG) {
            // Dust and thud particles on legs/feet
            level.sendParticles(
                ParticleTypes.SMOKE,
                to.x, to.y, to.z,
                5, 0.08, 0.08, 0.08, 0.03
            );
        } else if (zone == HitscanEngine.HitZone.WALL) {
            // Impact spark at hit location
            level.sendParticles(
                ParticleTypes.SMOKE,
                to.x, to.y, to.z,
                3, 0.05, 0.05, 0.05, 0.02
            );

            // Wall bullet hole (stays on wall for 20 seconds)
            if (face != null) {
                // Nudge 0.015 blocks outward along hit face normal so it sits right on the wall surface
                double holeX = to.x + face.getStepX() * 0.015;
                double holeY = to.y + face.getStepY() * 0.015;
                double holeZ = to.z + face.getStepZ() * 0.015;

                level.sendParticles(
                    ValorantMod.BULLET_HOLE,
                    holeX, holeY, holeZ,
                    1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }
    }

    public static void spawnTracer(ServerLevel level, Vec3 from, Vec3 to, boolean hitWall, Direction face) {
        spawnTracer(level, from, to, hitWall ? HitscanEngine.HitZone.WALL : HitscanEngine.HitZone.MISS, face);
    }
}
