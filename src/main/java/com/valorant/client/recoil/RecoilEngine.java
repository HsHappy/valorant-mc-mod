package com.valorant.client.recoil;

import com.valorant.weapon.WeaponData;
import net.minecraft.world.entity.player.Player;

/**
 * Authentic Valorant View Recoil & Ballistics Engine for Minecraft.
 *
 * Calibrated against official Valorant recorded gameplay:
 * 1. Physical Camera Climb (Kafanın / Cross'un Yukarı Tırmanması):
 *    Shooting rotates player view up into a stable plateau (~7.20° for Vandal).
 * 2. Smooth Harmonic Horizontal Sway (Sağa ve Sola Sakin Salınım):
 *    At the plateau, sway cycles with randomized direction drift and hold transitions.
 * 3. Heavy Physical Camera Punch (Mekanik Ekran Sarsıntısı):
 *    Sheriff and Operator feature colossal, visceral camera kicks matching Valorant's hand cannons.
 * 4. Semi-Automatic vs Automatic Differentiation:
 *    Pistols and snipers fire single shots per click, with smooth paced recovery.
 * 5. Dynamic Mouse Input Tracking (Anti-Floor-Slam):
 *    Detects user mouse pull-down without accidentally absorbing weapon recoil impulses.
 *
 * Refactored to decouple global static state into WeaponRecoilState instances,
 * enabling per-player server validation and high refresh rate client interpolation.
 */
public class RecoilEngine {

    // --- Mechanical Camera Punch Physical Constants (Strictly Preserved) ---
    public static final float PUNCH_STIFFNESS = WeaponRecoilState.PUNCH_STIFFNESS;
    public static final float PUNCH_DAMPING = WeaponRecoilState.PUNCH_DAMPING;

    // --- Valorant Recoil Follow Option ('H' Key Toggle) ---
    public static boolean recoilFollowEnabled = false;

    // --- Client-Side Active Recoil State Instance ---
    private static final WeaponRecoilState CLIENT_STATE = new WeaponRecoilState();

    public static WeaponRecoilState getClientState() {
        return CLIENT_STATE;
    }

    public record WeaponRecoilProfile(
        float firstBulletAccuracy,
        float cameraPitchStep,        // Her mermide kameranın (cross'un) dikey tırmanma adımı
        float cameraMaxPitchPlateau,  // Kameranın dikey olarak duracağı tavan (plateau)
        int horizontalSwayStartShot,  // Sağa sola salınımın başlayacağı mermi sayısı
        float cameraYawSwayStep,      // Yatay salınım faz adımı (düşük değer = yavaş ve sakin salınım)
        float cameraMaxYawSway,       // Sağa ve sola maksimum kayma açısı
        float bulletExtraPitchOffset, // Merminin crosshair üzerine ek dikey sapması
        float firingErrorBloomPerShot,// Mermi başına bloom
        float maxFiringErrorBloom,    // Maksimum bloom
        long recoveryTimeMs,          // Geri toparlanma süresi (ms)
        float punchVelocity           // Ekran sarsıntısı şiddeti (punch)
    ) {}

    // Type alias for backward compatibility
    public record ShotTrajectory(float pitchOffset, float yawOffset, float spread) {}

    public static WeaponRecoilProfile getProfile(String weaponId) {
        return switch (weaponId) {
            // --- Vandal (Assault Rifle) — Full Auto ---
            case "vandal" -> new WeaponRecoilProfile(
                0.25f,              // firstBulletAccuracy
                1.30f,              // cameraPitchStep
                7.20f,              // cameraMaxPitchPlateau
                7,                  // horizontalSwayStartShot — ilk 6 mermi dikey
                0.32f,              // cameraYawSwayStep — sakin, okunabilir salınım
                3.20f,              // cameraMaxYawSway
                2.20f,              // bulletExtraPitchOffset
                0.14f,              // firingErrorBloomPerShot
                1.50f,              // maxFiringErrorBloom
                375,                // recoveryTimeMs
                75.0f               // punchVelocity
            );

            // --- Phantom (Assault Rifle) — Full Auto ---
            case "phantom" -> new WeaponRecoilProfile(
                0.20f, 1.00f, 5.80f, 9, 0.28f, 2.60f, 1.80f, 0.10f, 1.20f, 320, 55.0f
            );

            // --- Classic (Starter Pistol) — Semi Auto ---
            case "classic" -> new WeaponRecoilProfile(
                0.40f, 1.20f, 4.00f, 4, 0.40f, 1.80f, 1.20f, 0.25f, 1.80f, 240, 85.0f
            );

            // --- Sheriff (Heavy Hand Cannon) — Semi Auto ---
            case "sheriff" -> new WeaponRecoilProfile(
                0.25f,              // firstBulletAccuracy — pinpoint 1-tap
                3.40f,              // cameraPitchStep — her atışta sert dikey kalkış
                9.00f,              // cameraMaxPitchPlateau
                2,                  // horizontalSwayStartShot
                0.45f,              // cameraYawSwayStep
                2.80f,              // cameraMaxYawSway
                2.50f,              // bulletExtraPitchOffset
                0.90f,              // firingErrorBloomPerShot
                3.50f,              // maxFiringErrorBloom
                420,                // recoveryTimeMs
                190.0f              // punchVelocity — AĞIR REVOLVER DARBESİ!
            );

            // --- Spectre (Silenced SMG) — Full Auto ---
            case "spectre" -> new WeaponRecoilProfile(
                0.40f,              // firstBulletAccuracy
                0.75f,              // cameraPitchStep
                5.00f,              // cameraMaxPitchPlateau
                6,                  // horizontalSwayStartShot
                0.22f,              // cameraYawSwayStep — SAKİN ve KONTROLLÜ salınım!
                2.20f,              // cameraMaxYawSway — dengeli yayılma
                1.60f,              // bulletExtraPitchOffset
                0.12f,              // firingErrorBloomPerShot
                1.60f,              // maxFiringErrorBloom
                280,                // recoveryTimeMs
                45.0f               // punchVelocity
            );

            // --- Stinger (Rapid-Fire SMG) — Full Auto ---
            case "stinger" -> new WeaponRecoilProfile(
                0.65f,              // firstBulletAccuracy
                0.85f,              // cameraPitchStep — hızlı tırmanış
                6.50f,              // cameraMaxPitchPlateau
                5,                  // horizontalSwayStartShot
                0.35f,              // cameraYawSwayStep
                2.80f,              // cameraMaxYawSway
                1.80f,              // bulletExtraPitchOffset
                0.18f,              // firingErrorBloomPerShot
                2.20f,              // maxFiringErrorBloom
                260,                // recoveryTimeMs
                50.0f               // punchVelocity
            );

            // --- Operator (Sniper) — Semi Auto ---
            case "operator" -> new WeaponRecoilProfile(
                0.00f,              // firstBulletAccuracy — scoped pinpoint
                8.50f,              // cameraPitchStep — devasa kamera kalkışı
                8.50f,              // cameraMaxPitchPlateau
                1,                  // horizontalSwayStartShot
                0.00f,              // cameraYawSwayStep
                0.00f,              // cameraMaxYawSway
                0.00f,              // bulletExtraPitchOffset
                0.00f,              // firingErrorBloomPerShot
                0.00f,              // maxFiringErrorBloom
                1250,               // recoveryTimeMs — 1.25s bolt rechamber
                360.0f              // punchVelocity — DEVASA KESKİN NİŞANCI PATLAMASI!
            );

            // --- Guardian (DMR) — Semi Auto ---
            case "guardian" -> new WeaponRecoilProfile(
                0.10f, 2.40f, 7.80f, 3, 0.40f, 2.20f, 1.80f, 0.40f, 1.80f, 350, 140.0f
            );

            // --- Marshal (Sniper) — Semi Auto ---
            case "marshal" -> new WeaponRecoilProfile(
                0.10f, 5.50f, 5.50f, 1, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 600, 180.0f
            );

            // --- Ghost (Silenced Pistol) — Semi Auto ---
            case "ghost" -> new WeaponRecoilProfile(
                0.30f, 1.00f, 3.60f, 5, 0.35f, 1.50f, 1.00f, 0.18f, 1.50f, 230, 75.0f
            );

            // --- Bulldog (Rifle) — Full Auto ---
            case "bulldog" -> new WeaponRecoilProfile(
                0.30f, 1.10f, 6.20f, 7, 0.32f, 2.50f, 1.80f, 0.12f, 1.40f, 350, 65.0f
            );

            // --- Odin (Heavy LMG) — Full Auto ---
            case "odin" -> new WeaponRecoilProfile(
                0.80f, 0.60f, 8.20f, 12, 0.25f, 3.50f, 2.00f, 0.08f, 1.80f, 450, 60.0f
            );

            // --- Ares (LMG) — Full Auto ---
            case "ares" -> new WeaponRecoilProfile(
                1.00f, 0.70f, 7.80f, 10, 0.28f, 3.20f, 2.00f, 0.10f, 2.00f, 400, 65.0f
            );

            // --- Frenzy (Auto Pistol) — Full Auto ---
            case "frenzy" -> new WeaponRecoilProfile(
                0.45f, 1.05f, 6.50f, 4, 0.38f, 3.20f, 1.80f, 0.20f, 2.20f, 275, 60.0f
            );

            // --- Shorty (Sidearm Shotgun) — Semi Auto ---
            case "shorty" -> new WeaponRecoilProfile(
                4.00f, 3.80f, 6.50f, 2, 0.50f, 2.00f, 2.20f, 0.40f, 2.50f, 350, 130.0f
            );

            // --- Bucky (Pump Shotgun) — Semi Auto ---
            case "bucky" -> new WeaponRecoilProfile(
                3.00f, 4.20f, 7.00f, 2, 0.40f, 2.00f, 2.50f, 0.35f, 2.80f, 450, 160.0f
            );

            // --- Judge (Auto Shotgun) — Full Auto ---
            case "judge" -> new WeaponRecoilProfile(
                2.50f, 2.50f, 6.80f, 3, 0.35f, 2.40f, 2.00f, 0.25f, 2.50f, 350, 110.0f
            );

            // --- Outlaw (Sniper) — Semi Auto ---
            case "outlaw" -> new WeaponRecoilProfile(
                0.15f, 6.50f, 8.00f, 1, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 800, 260.0f
            );

            // Default fallback
            default -> new WeaponRecoilProfile(
                0.30f, 1.00f, 6.00f, 6, 0.30f, 2.80f, 1.80f, 0.14f, 1.50f, 350, 65.0f
            );
        };
    }

    /**
     * High-frequency render frame update on client.
     */
    public static void update(Player player, float dt) {
        CLIENT_STATE.update(player, dt);
    }

    /**
     * 20 TPS client tick hook.
     */
    public static void clientTick(Player player) {
        CLIENT_STATE.clientTick(player);
    }

    /**
     * Computes recoil trajectory using client state.
     */
    public static ShotTrajectory calculateShot(Player player, WeaponData weapon) {
        WeaponRecoilState.ShotTrajectory st = CLIENT_STATE.calculateShot(player, weapon);
        return new ShotTrajectory(st.pitchOffset(), st.yawOffset(), st.spread());
    }

    public static void reset() {
        CLIENT_STATE.reset();
    }

    // --- Backward-Compatible Static Getters (Delegates to Client State) ---
    public static float getVisualPitchOffset() { return CLIENT_STATE.getVisualPitchOffset(); }
    public static float getVisualYawOffset() { return CLIENT_STATE.getVisualYawOffset(); }
    public static float getInterpolatedVisualPitchOffset(float partialTicks) {
        return CLIENT_STATE.getInterpolatedVisualPitchOffset(partialTicks);
    }
    public static float getInterpolatedVisualYawOffset(float partialTicks) {
        return CLIENT_STATE.getInterpolatedVisualYawOffset(partialTicks);
    }
    public static float getCurrentBloom() { return CLIENT_STATE.getCurrentBloom(); }
    public static float getCurrentTotalSpread() { return CLIENT_STATE.getCurrentTotalSpread(); }
    public static int getConsecutiveShots() { return CLIENT_STATE.getConsecutiveShots(); }
    public static float getCurrentCameraPitch() { return CLIENT_STATE.getCurrentCameraPitch(); }
    public static float getCurrentCameraYaw() { return CLIENT_STATE.getCurrentCameraYaw(); }
}
