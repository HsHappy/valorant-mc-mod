package com.valorant.client.recoil;

import com.valorant.weapon.WeaponData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Encapsulated Object-Oriented Recoil & Ballistics State.
 *
 * Replaces global static state in RecoilEngine to allow per-player (client & server)
 * and per-weapon instances with clean state transitions, zero memory leakage,
 * high refresh rate render interpolation, and authentic Valorant yaw drift.
 */
public class WeaponRecoilState {

    // --- Active Weapon Tracking ---
    private String currentWeaponId = "";

    // --- State Variables ---
    private int consecutiveShots = 0;
    private long lastShotTime = 0;
    private long currentWeaponIntervalMs = 100;

    // --- Horizontal Yaw Sway & Valorant-Style Drift Algorithm ---
    // Valorant does not use a predictable sine curve; after reaching plateau,
    // the sway direction changes dynamically with hold duration and smooth drift.
    private float swayPhase = 0.0f;
    private float swayDirection = 1.0f; // +1.0f (Right) or -1.0f (Left)
    private int swaySideHoldShots = 0;
    private int targetHoldDuration = 3;

    // --- True Camera Recoil (View Recoil: Climb & Plateau) ---
    private float currentCameraPitch = 0.0f; // Current applied camera pitch (vertical climb)
    private float targetCameraPitch = 0.0f;  // Target plateau pitch
    private float currentCameraYaw = 0.0f;   // Current applied camera yaw (horizontal sway)
    private float targetCameraYaw = 0.0f;    // Target horizontal sway angle

    // --- Tick-Render Interpolation Values ---
    private float prevCameraPitch = 0.0f;
    private float prevCameraYaw = 0.0f;
    private float prevPunchPitch = 0.0f;
    private float prevPunchYaw = 0.0f;
    private float prevBulletPitchOffset = 0.0f;
    private float prevBulletYawOffset = 0.0f;

    // --- Mechanical Camera Punch (Spring-Damper Physics) ---
    // wn = 22 rad/s, zeta = 0.82 (Heavy, visceral punch settling in ~200ms)
    // PRESERVED CONSTANTS PER STRICT SPECIFICATION
    public static final float PUNCH_STIFFNESS = 484.0f;
    public static final float PUNCH_DAMPING = 36.1f;
    private float punchPitch = 0.0f;
    private float punchPitchVel = 0.0f;
    private float punchYaw = 0.0f;
    private float punchYawVel = 0.0f;

    // --- Mouse Input Tracking (Anti-Floor-Slam) ---
    private float lastPlayerXRot = 0.0f;
    private float lastPlayerYRot = 0.0f;
    private boolean trackingInitialized = false;

    // --- True Bullet Offsets (Offset on top of crosshair) ---
    private float bulletPitchOffset = 0.0f;
    private float bulletYawOffset = 0.0f;
    private float currentBloom = 0.0f;
    private float currentTotalSpread = 0.0f;

    public record ShotTrajectory(float pitchOffset, float yawOffset, float spread) {}

    public WeaponRecoilState() {
        reset();
    }

    /**
     * Checks if the player swapped weapons. If so, immediately triggers a clean reset.
     */
    public void checkWeaponSwitch(String newWeaponId) {
        if (newWeaponId == null) newWeaponId = "";
        if (!newWeaponId.equals(this.currentWeaponId)) {
            reset();
            this.currentWeaponId = newWeaponId;
        }
    }

    /**
     * Resets all recoil, sway, punch, and ballistics state to pristine initial condition.
     */
    public void reset() {
        consecutiveShots = 0;
        lastShotTime = 0;
        swayPhase = 0.0f;
        swayDirection = 1.0f;
        swaySideHoldShots = 0;
        targetHoldDuration = 3;

        currentCameraPitch = 0.0f;
        targetCameraPitch = 0.0f;
        currentCameraYaw = 0.0f;
        targetCameraYaw = 0.0f;

        prevCameraPitch = 0.0f;
        prevCameraYaw = 0.0f;
        prevPunchPitch = 0.0f;
        prevPunchYaw = 0.0f;

        bulletPitchOffset = 0.0f;
        bulletYawOffset = 0.0f;
        prevBulletPitchOffset = 0.0f;
        prevBulletYawOffset = 0.0f;

        currentBloom = 0.0f;
        currentTotalSpread = 0.0f;

        punchPitch = 0.0f;
        punchPitchVel = 0.0f;
        punchYaw = 0.0f;
        punchYawVel = 0.0f;

        trackingInitialized = false;
        currentWeaponId = "";
    }

    /**
     * Computes the ballistic trajectory, camera impulse, bloom, and spread for an outgoing bullet.
     * Can be invoked authoritatively on server or predictively on client.
     */
    public ShotTrajectory calculateShot(Player player, WeaponData weapon) {
        long now = System.currentTimeMillis();
        long timeSinceLastShot = now - lastShotTime;
        RecoilEngine.WeaponRecoilProfile profile = RecoilEngine.getProfile(weapon.id());
        currentWeaponIntervalMs = (long) (1000.0 / Math.max(0.1, weapon.fireRateRps()));
        checkWeaponSwitch(weapon.id());

        // 1. Dynamic tap recovery / cooldown
        if (timeSinceLastShot > profile.recoveryTimeMs()) {
            consecutiveShots = 0;
            swayPhase = 0.0f;
            swayDirection = (Math.random() < 0.5) ? 1.0f : -1.0f;
            swaySideHoldShots = 0;
            targetHoldDuration = 2 + (int) (Math.random() * 3);
            targetCameraPitch = 0.0f;
            targetCameraYaw = 0.0f;
            bulletPitchOffset = 0.0f;
            bulletYawOffset = 0.0f;
            currentBloom = 0.0f;
        } else if (timeSinceLastShot > currentWeaponIntervalMs * 1.30) {
            float pauseTime = (float) (timeSinceLastShot - currentWeaponIntervalMs);
            float maxRecoveryPause = (float) (profile.recoveryTimeMs() - currentWeaponIntervalMs);
            float decayRatio = Math.min(1.0f, Math.max(0.0f, pauseTime / maxRecoveryPause));
            consecutiveShots = Math.max(0, (int) (consecutiveShots * (1.0f - decayRatio * 0.8f)));
            targetCameraPitch = Math.max(0.0f, targetCameraPitch * (1.0f - decayRatio));
            targetCameraYaw *= (1.0f - decayRatio);
            bulletPitchOffset = Math.max(0.0f, bulletPitchOffset * (1.0f - decayRatio));
            bulletYawOffset *= (1.0f - decayRatio);
            currentBloom = Math.max(0.0f, currentBloom * (1.0f - decayRatio));
        }

        consecutiveShots++;
        lastShotTime = now;

        // 2. Heavy Mechanical Camera Punch (Spring Impulse)
        punchPitchVel += profile.punchVelocity();
        punchYawVel += (float) ((Math.random() - 0.5) * profile.punchVelocity() * 0.25f);

        // 3. Persistent Camera Climb & Valorant Horizontal Sway Drift
        if (consecutiveShots == 1) {
            targetCameraPitch = 0.0f;
            targetCameraYaw = 0.0f;
            bulletPitchOffset = 0.0f;
            bulletYawOffset = 0.0f;
            currentBloom = 0.0f;
            swayPhase = 0.0f;
            swayDirection = (Math.random() < 0.5) ? 1.0f : -1.0f;
            swaySideHoldShots = 0;
            targetHoldDuration = 2 + (int) (Math.random() * 3);
        } else {
            // Vertical climb: plateaus smoothly as it reaches ceiling
            float climbProgress = targetCameraPitch / profile.cameraMaxPitchPlateau();
            float stepFactor = Math.max(0.35f, 1.25f - (climbProgress * 0.9f));
            float verticalBreathing = (consecutiveShots >= profile.horizontalSwayStartShot())
                ? (float) ((Math.random() - 0.5) * 0.35f) : 0.0f;
            targetCameraPitch = Math.min(profile.cameraMaxPitchPlateau() + 0.3f,
                Math.min(profile.cameraMaxPitchPlateau(), targetCameraPitch + profile.cameraPitchStep() * stepFactor) + verticalBreathing);

            // Extra bullet pitch offset above crosshair
            float extraPitchJitter = (consecutiveShots >= profile.horizontalSwayStartShot())
                ? (float) ((Math.random() - 0.5) * 0.20f) : 0.0f;
            bulletPitchOffset = Math.min(profile.bulletExtraPitchOffset(), bulletPitchOffset + profile.cameraPitchStep() * 0.35f) + extraPitchJitter;

            // Authentic Valorant Yaw Drift Algorithm (Non-monotonic direction changes)
            if (consecutiveShots >= profile.horizontalSwayStartShot()) {
                float swayStep = profile.cameraYawSwayStep();
                swayPhase += swayStep * swayDirection;

                float halfPi = (float) (Math.PI / 2.0);
                if (swayDirection > 0.0f && swayPhase >= halfPi) {
                    swayPhase = halfPi;
                    swaySideHoldShots++;
                    if (swaySideHoldShots >= targetHoldDuration) {
                        swayDirection = -1.0f; // Flip direction to left
                        swaySideHoldShots = 0;
                        targetHoldDuration = 2 + (int) (Math.random() * 3);
                    }
                } else if (swayDirection < 0.0f && swayPhase <= -halfPi) {
                    swayPhase = -halfPi;
                    swaySideHoldShots++;
                    if (swaySideHoldShots >= targetHoldDuration) {
                        swayDirection = 1.0f; // Flip direction to right
                        swaySideHoldShots = 0;
                        targetHoldDuration = 2 + (int) (Math.random() * 3);
                    }
                }

                float baseSway = (float) Math.sin(swayPhase) * profile.cameraMaxYawSway();
                float microJitter = (float) ((Math.random() - 0.5) * 0.15f);
                targetCameraYaw = baseSway + microJitter;
                bulletYawOffset = (float) Math.sin(swayPhase) * (profile.cameraMaxYawSway() * 0.20f) + (microJitter * 0.3f);
            } else {
                float microDrift = (float) ((Math.random() - 0.5) * 0.15f);
                targetCameraYaw += microDrift;
                bulletYawOffset = microDrift;
            }

            // Bloom accumulation
            currentBloom = Math.min(profile.maxFiringErrorBloom(), currentBloom + profile.firingErrorBloomPerShot());
        }

        // 4. Movement Error (Movement Spread)
        float movementSpread = 0.0f;
        if (!player.onGround()) {
            movementSpread = 12.0f;
        } else if (player.isSprinting()) {
            movementSpread = 6.5f;
        } else if (player.getDeltaMovement().horizontalDistanceSqr() > 0.003) {
            movementSpread = player.isCrouching() ? 0.35f : 3.5f;
        }

        float baseAccuracy = (float) weapon.firstBulletAccuracy();
        if (baseAccuracy <= 0.0f) baseAccuracy = profile.firstBulletAccuracy();
        if (player.isCrouching()) baseAccuracy *= 0.8f;

        currentTotalSpread = baseAccuracy + currentBloom + movementSpread;

        return new ShotTrajectory(bulletPitchOffset, bulletYawOffset, currentTotalSpread);
    }

    /**
     * High-frequency render frame update (60 - 500 FPS).
     * Drives the physical camera climb, horizontal sway, and spring recovery.
     * Synchronizes xRotO/yRotO to prevent 20 TPS tick vs 144Hz+ frame render fighting.
     */
    public void update(Player player, float dt) {
        if (player == null || dt <= 0.0f) return;
        dt = Math.min(dt, 0.05f); // Prevent spring jumps on lag spikes

        float playerXRot = player.getXRot();
        float playerYRot = player.getYRot();

        if (!trackingInitialized) {
            lastPlayerXRot = playerXRot;
            lastPlayerYRot = playerYRot;
            trackingInitialized = true;
            return;
        }

        long now = System.currentTimeMillis();
        long timeSinceLastShot = now - lastShotTime;

        // 1. Natural recoil decay when fire stops
        if (timeSinceLastShot > currentWeaponIntervalMs + 35) {
            float decayFactor = Math.min(1.0f, 8.0f * dt);
            targetCameraPitch = Math.max(0.0f, targetCameraPitch - targetCameraPitch * decayFactor);
            targetCameraYaw = targetCameraYaw - targetCameraYaw * decayFactor;
            bulletPitchOffset = Math.max(0.0f, bulletPitchOffset - bulletPitchOffset * decayFactor);
            bulletYawOffset = bulletYawOffset - bulletYawOffset * decayFactor;
            currentBloom = Math.max(0.0f, currentBloom - currentBloom * decayFactor);
            currentTotalSpread = Math.max(0.0f, currentTotalSpread - currentTotalSpread * decayFactor);

            if (timeSinceLastShot > 380) {
                consecutiveShots = 0;
                swayPhase = 0.0f;
                swaySideHoldShots = 0;
                targetCameraPitch = 0.0f;
                targetCameraYaw = 0.0f;
                bulletPitchOffset = 0.0f;
                bulletYawOffset = 0.0f;
                currentBloom = 0.0f;
                currentTotalSpread = 0.0f;
            }
        }

        // 2. Smooth Camera Angle Progression (Camera View Climb & Sway)
        float smoothSpeed = (timeSinceLastShot <= currentWeaponIntervalMs + 35) ? 24.0f : 14.0f;
        float nextCameraPitch = currentCameraPitch + (targetCameraPitch - currentCameraPitch) * Math.min(1.0f, smoothSpeed * dt);
        float nextCameraYaw = currentCameraYaw + (targetCameraYaw - currentCameraYaw) * Math.min(1.0f, smoothSpeed * dt);

        float pitchStep = nextCameraPitch - currentCameraPitch;
        float yawStep = nextCameraYaw - currentCameraYaw;

        currentCameraPitch = nextCameraPitch;
        currentCameraYaw = nextCameraYaw;

        // 3. Mechanical Camera Punch (Spring Physics — PRESERVED EQUATIONS)
        float accelPitch = -PUNCH_STIFFNESS * punchPitch - PUNCH_DAMPING * punchPitchVel;
        punchPitchVel += accelPitch * dt;
        float nextPunchPitch = punchPitch + punchPitchVel * dt;

        float accelYaw = -PUNCH_STIFFNESS * punchYaw - PUNCH_DAMPING * punchYawVel;
        punchYawVel += accelYaw * dt;
        float nextPunchYaw = punchYaw + punchYawVel * dt;

        float punchDeltaPitch = nextPunchPitch - punchPitch;
        float punchDeltaYaw = nextPunchYaw - punchYaw;

        punchPitch = nextPunchPitch;
        punchYaw = nextPunchYaw;

        // Total rotation delta to apply to camera
        float totalDeltaPitch = pitchStep + punchDeltaPitch;
        float totalDeltaYaw = yawStep + punchDeltaYaw;

        if (Math.abs(totalDeltaPitch) > 1e-6f || Math.abs(totalDeltaYaw) > 1e-6f) {
            float newXRot = player.getXRot() - totalDeltaPitch;
            float newYRot = player.getYRot() + totalDeltaYaw;
            player.setXRot(newXRot);
            player.setYRot(newYRot);

            // CRITICAL JITTER FIX:
            // Synchronize previous tick rotations so Minecraft's Camera.setup lerp
            // does not interpolate against stale tick values at high refresh rates (144Hz - 240Hz).
            player.xRotO = newXRot;
            player.yRotO = newYRot;
        }

        // 4. Dynamic Mouse Input Tracking (Anti-Floor-Slam)
        float manualMousePitch = playerXRot - lastPlayerXRot;
        if (manualMousePitch > 0.005f) {
            float absorbed = Math.min(currentCameraPitch, manualMousePitch);
            currentCameraPitch -= absorbed;
            targetCameraPitch = Math.max(0.0f, targetCameraPitch - absorbed);
        }

        lastPlayerXRot = player.getXRot();
        lastPlayerYRot = player.getYRot();
    }

    /**
     * 20 TPS Client tick cycle hook.
     * Records previous tick states for sub-tick frame lerp interpolation.
     */
    public void clientTick(Player player) {
        this.prevCameraPitch = this.currentCameraPitch;
        this.prevCameraYaw = this.currentCameraYaw;
        this.prevPunchPitch = this.punchPitch;
        this.prevPunchYaw = this.punchYaw;
        this.prevBulletPitchOffset = this.bulletPitchOffset;
        this.prevBulletYawOffset = this.bulletYawOffset;
    }

    // --- Interpolated & Raw Getters ---
    public float getVisualPitchOffset() { return bulletPitchOffset; }
    public float getVisualYawOffset() { return bulletYawOffset; }
    public float getInterpolatedVisualPitchOffset(float partialTicks) {
        return Mth.lerp(partialTicks, prevBulletPitchOffset, bulletPitchOffset);
    }
    public float getInterpolatedVisualYawOffset(float partialTicks) {
        return Mth.lerp(partialTicks, prevBulletYawOffset, bulletYawOffset);
    }

    public float getCurrentBloom() { return currentBloom; }
    public float getCurrentTotalSpread() { return currentTotalSpread; }
    public int getConsecutiveShots() { return consecutiveShots; }
    public float getCurrentCameraPitch() { return currentCameraPitch; }
    public float getCurrentCameraYaw() { return currentCameraYaw; }
    public float getTargetCameraPitch() { return targetCameraPitch; }
    public float getTargetCameraYaw() { return targetCameraYaw; }
    public float getPunchPitch() { return punchPitch; }
    public float getPunchYaw() { return punchYaw; }
    public long getLastShotTime() { return lastShotTime; }
    public String getCurrentWeaponId() { return currentWeaponId; }
}
