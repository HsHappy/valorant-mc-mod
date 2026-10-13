package com.valorant.network;

import com.valorant.client.recoil.WeaponRecoilState;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-Side Player Recoil & Fire-Rate Tracker.
 *
 * Enforces authoritative ballistics on the server:
 * - Prevents client-side no-recoil, no-spread, and rapid-fire exploits.
 * - Tracks consecutive shots, spray decay, movement spread, and fire timestamps.
 * - Manages clean lifecycle and cleanup upon player disconnect.
 */
public class ServerPlayerRecoilTracker {

    public static class PlayerRecoilContext {
        private final WeaponRecoilState recoilState = new WeaponRecoilState();
        private long lastShotTime = 0;
        private String lastWeaponId = "";

        public WeaponRecoilState getRecoilState() {
            return recoilState;
        }

        public long getLastShotTime() {
            return lastShotTime;
        }

        public void setLastShotTime(long lastShotTime) {
            this.lastShotTime = lastShotTime;
        }

        public String getLastWeaponId() {
            return lastWeaponId;
        }

        public void checkWeaponSwitch(String newWeaponId) {
            if (newWeaponId == null) newWeaponId = "";
            if (!newWeaponId.equals(this.lastWeaponId)) {
                this.recoilState.reset();
                this.lastWeaponId = newWeaponId;
            }
        }
    }

    private static final Map<UUID, PlayerRecoilContext> TRACKERS = new ConcurrentHashMap<>();

    public static PlayerRecoilContext getContext(UUID playerUuid) {
        return TRACKERS.computeIfAbsent(playerUuid, id -> new PlayerRecoilContext());
    }

    public static void removePlayer(UUID playerUuid) {
        TRACKERS.remove(playerUuid);
    }

    public static void clear() {
        TRACKERS.clear();
    }
}
