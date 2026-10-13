package com.valorant.weapon;

import java.util.List;

public record WeaponData(
    String id,
    String displayName,
    String category,
    int cost,
    int magazineSize,
    double fireRateRps,
    double reloadTimeSeconds,
    double equipTimeSeconds,
    double firstBulletAccuracy,
    List<DamageRange> damageRanges
) {
    public record DamageRange(
        double minDistance,
        double maxDistance,
        double headDamage,
        double bodyDamage,
        double legDamage
    ) {}

    public DamageRange getRangeForDistance(double distanceMeters) {
        if (damageRanges == null || damageRanges.isEmpty()) {
            return new DamageRange(0, 100, 40, 25, 20);
        }
        for (DamageRange range : damageRanges) {
            if (distanceMeters >= range.minDistance() && distanceMeters <= range.maxDistance()) {
                return range;
            }
        }
        return damageRanges.get(damageRanges.size() - 1);
    }
}
