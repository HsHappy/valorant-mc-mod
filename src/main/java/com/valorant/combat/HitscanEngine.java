package com.valorant.combat;

import com.valorant.weapon.WeaponData;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Tactical Valorant Hitscan & Ballistics Engine.
 *
 * Implements:
 * 1. Orthonormal basis vector uniform disc spread (Bloom cone).
 * 2. Anatomical hitbox resolution (Head: 77%+, Body: 38%+, Leg: 0-38%, Head radius: 0.28 blocks).
 * 3. Ping-aware lag compensation & historical entity interpolation.
 */
public class HitscanEngine {

    public enum HitZone {
        HEAD,
        BODY,
        LEG,
        WALL,
        MISS
    }

    public record ShotResult(
        HitZone zone,
        LivingEntity target,
        double distanceMeters,
        double damageDealt,
        Vec3 hitLocation,
        Direction hitFace
    ) {}

    /**
     * Executes authoritative raycast using explicit camera orientation angles.
     */
    public static ShotResult fireRaycast(
        Player shooter,
        WeaponData weapon,
        double maxDistance,
        float cameraPitch,
        float cameraYaw,
        float pitchOffset,
        float yawOffset,
        float spreadDegrees
    ) {
        Level level = shooter.level();
        Vec3 eyePos = shooter.getEyePosition();

        // 1. Calculate trajectory with decoupled spray offsets
        // In Minecraft, looking UP is negative pitch
        float effectivePitch = cameraPitch - pitchOffset;
        float effectiveYaw = cameraYaw + yawOffset;
        Vec3 trajectoryVec = calculateViewVector(effectivePitch, effectiveYaw);

        // 2. Apply random spread bloom (Direction-invariant 3D cone)
        if (spreadDegrees > 0.0f) {
            trajectoryVec = applySpread(trajectoryVec, spreadDegrees);
        }

        Vec3 endPos = eyePos.add(trajectoryVec.scale(maxDistance));

        // 3. Block collision (walls)
        BlockHitResult blockHit = level.clip(new ClipContext(
            eyePos, endPos,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            shooter
        ));

        double blockDistance = blockHit.getType() != HitResult.Type.MISS
            ? eyePos.distanceTo(blockHit.getLocation())
            : maxDistance;

        // 4. Entity collision with Lag Compensation & Historical Interpolation
        int pingMs = 0;
        if (shooter instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            pingMs = Math.clamp(serverPlayer.connection.latency(), 0, 300);
        }

        AABB searchBox = shooter.getBoundingBox().expandTowards(trajectoryVec.scale(blockDistance)).inflate(2.0);
        LivingEntity closestEntity = null;
        Vec3 closestHitPoint = null;
        HitZone bestZone = HitZone.MISS;
        double minEntityDistance = blockDistance;

        for (Entity entity : level.getEntities(shooter, searchBox, e -> e instanceof LivingEntity && !e.isSpectator() && e.isAlive())) {
            LivingEntity living = (LivingEntity) entity;

            AABB currentBox = living.getBoundingBox();

            // Historical movement delta
            Vec3 prevPos = new Vec3(living.xo, living.yo, living.zo);
            Vec3 currPos = living.position();
            Vec3 moveDelta = currPos.subtract(prevPos);

            // Ping-aware historical compensation window
            double pingSeconds = pingMs / 1000.0;
            Vec3 lagOffset = moveDelta.scale(Math.clamp(pingSeconds * 20.0, 0.0, 2.5)); // 0 to 2.5 ticks back
            AABB historicalBox = currentBox.move(-lagOffset.x, -lagOffset.y, -lagOffset.z);

            // Spanning box with latency-aware tolerance buffer
            double tolerance = 0.04 + Math.min(0.12, pingMs * 0.0004);
            AABB compensatedBox = currentBox.minmax(historicalBox).inflate(tolerance);

            // Test direct hit first
            Optional<Vec3> directHit = currentBox.inflate(0.02).clip(eyePos, endPos);
            Optional<Vec3> compHit = compensatedBox.clip(eyePos, endPos);

            Optional<Vec3> hitOpt = directHit.isPresent() ? directHit : compHit;

            if (hitOpt.isPresent()) {
                Vec3 hit = hitOpt.get();
                double dist = eyePos.distanceTo(hit);
                if (dist < minEntityDistance) {
                    minEntityDistance = dist;
                    closestEntity = living;
                    closestHitPoint = hit;

                    if (directHit.isPresent()) {
                        bestZone = determineHitZone(living, hit, living.getY(), living.getX(), living.getZ());
                    } else {
                        // Compensate anatomical zone calculation by shifting center reference
                        double refY = living.getY() - lagOffset.y * 0.5;
                        double refX = living.getX() - lagOffset.x * 0.5;
                        double refZ = living.getZ() - lagOffset.z * 0.5;
                        bestZone = determineHitZone(living, hit, refY, refX, refZ);
                    }
                }
            }
        }

        // Entity hit
        if (closestEntity != null && closestHitPoint != null) {
            WeaponData.DamageRange range = weapon.getRangeForDistance(minEntityDistance);

            double damage = switch (bestZone) {
                case HEAD -> range.headDamage();
                case BODY -> range.bodyDamage();
                case LEG -> range.legDamage();
                default -> 0.0;
            };

            return new ShotResult(bestZone, closestEntity, minEntityDistance, damage, closestHitPoint, null);
        }

        // Wall hit
        if (blockHit.getType() != HitResult.Type.MISS) {
            return new ShotResult(HitZone.WALL, null, blockDistance, 0.0, blockHit.getLocation(), blockHit.getDirection());
        }

        // Complete miss
        return new ShotResult(HitZone.MISS, null, maxDistance, 0.0, endPos, null);
    }

    /**
     * Backward-compatible overload for local client raycasts or tests.
     */
    public static ShotResult fireRaycast(
        Player shooter,
        WeaponData weapon,
        double maxDistance,
        float pitchOffset,
        float yawOffset,
        float spreadDegrees
    ) {
        return fireRaycast(
            shooter, weapon, maxDistance,
            shooter.getXRot(), shooter.getYRot(),
            pitchOffset, yawOffset, spreadDegrees
        );
    }

    /**
     * Anatomical Hitbox Resolution for Valorant Combat (Mobs & Players).
     *
     * In Valorant, anatomy is divided into 3 distinct zones:
     * 1. KAFA (Head):
     *    - Distance to entity eye position <= 0.40m, OR
     *    - Hit Y is in upper 28% of height (>= 72%) within head radius <= max(0.34, width * 0.55).
     * 2. GÖVDE / VÜCUT (Body / Torso / Arms):
     *    - From hips (>= 38% of height) up to chin (< 72% of height).
     * 3. BACAK VE AYAK (Legs & Feet):
     *    - From ground level (0%) up to hips (< 38% of height).
     *    - In Valorant, legDamage covers hips, knees, shins, ankles, and feet.
     */
    private static HitZone determineHitZone(LivingEntity entity, Vec3 hitPoint, double refY, double refX, double refZ) {
        double hitY = hitPoint.y;
        double entityHeight = entity.getBbHeight();
        double entityWidth = entity.getBbWidth();

        // 1. Head Zone Check:
        // Use eye position as anatomical head center
        Vec3 eyePos = entity.getEyePosition();
        double eyeOffsetCompX = eyePos.x + (refX - entity.getX());
        double eyeOffsetCompY = eyePos.y + (refY - entity.getY());
        double eyeOffsetCompZ = eyePos.z + (refZ - entity.getZ());
        Vec3 compEyePos = new Vec3(eyeOffsetCompX, eyeOffsetCompY, eyeOffsetCompZ);

        double distToEye = hitPoint.distanceTo(compEyePos);

        double dx = hitPoint.x - refX;
        double dz = hitPoint.z - refZ;
        double distFromCenter = Math.sqrt(dx * dx + dz * dz);
        double maxHeadRadius = Math.max(0.34, entityWidth * 0.55);

        boolean isHeadHeight = hitY >= (refY + entityHeight * 0.72);
        if (distToEye <= 0.40 || (isHeadHeight && distFromCenter <= maxHeadRadius)) {
            return HitZone.HEAD;
        }

        // 2. Torso / Body Zone (Chest, abdomen, arms):
        if (hitY >= (refY + entityHeight * 0.38)) {
            return HitZone.BODY;
        }

        // 3. Leg & Feet Zone (Bacak ve Ayak - Hips, thighs, knees, shins, feet):
        return HitZone.LEG;
    }

    public static HitZone determineHitZone(LivingEntity entity, Vec3 hitPoint) {
        return determineHitZone(entity, hitPoint, entity.getY(), entity.getX(), entity.getZ());
    }

    public static Vec3 calculateViewVector(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180F);
        float g = -yaw * ((float)Math.PI / 180F);
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3((double)(i * j), (double)(-k), (double)(h * j));
    }

    /**
     * Orthonormal Camera Basis Vector Bloom Spread (STRICTLY PRESERVED).
     */
    private static Vec3 applySpread(Vec3 forward, double spreadDegrees) {
        if (spreadDegrees <= 0.0) return forward;

        // 1. Calculate true orthonormal camera basis vectors (invariant to player facing direction)
        Vec3 worldUp = Math.abs(forward.y) > 0.95 ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);
        Vec3 right = forward.cross(worldUp).normalize();
        Vec3 up = right.cross(forward).normalize();

        // 2. Uniform circular disc distribution in the spread cone (Valorant bloom disc)
        double r = Math.toRadians(spreadDegrees) * Math.sqrt(Math.random());
        double theta = Math.random() * 2.0 * Math.PI;

        double offsetX = Math.tan(r) * Math.cos(theta);
        double offsetY = Math.tan(r) * Math.sin(theta);

        return forward.add(right.scale(offsetX)).add(up.scale(offsetY)).normalize();
    }
}
