package com.valorant.combat;

import com.valorant.ValorantMod;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class TracerHelper {
    public static void spawnTracer(ServerLevel level, Vec3 from, Vec3 to, boolean hitWall, Direction face) {
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

        // Impact spark at hit location
        level.sendParticles(
            ParticleTypes.SMOKE,
            to.x, to.y, to.z,
            3, 0.05, 0.05, 0.05, 0.02
        );

        // Wall bullet hole (stays on wall for 20 seconds)
        if (hitWall && face != null) {
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
