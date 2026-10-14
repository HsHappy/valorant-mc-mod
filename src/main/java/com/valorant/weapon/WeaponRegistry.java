package com.valorant.weapon;

import com.valorant.ValorantMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.*;

public class WeaponRegistry {
    public static final Map<String, WeaponItem> REGISTERED_WEAPONS = new LinkedHashMap<>();

    // --- Sidearms ---
    public static WeaponItem CLASSIC;
    public static WeaponItem SHORTY;
    public static WeaponItem FRENZY;
    public static WeaponItem GHOST;
    public static WeaponItem SHERIFF;

    // --- SMGs ---
    public static WeaponItem STINGER;
    public static WeaponItem SPECTRE;

    // --- Shotguns ---
    public static WeaponItem BUCKY;
    public static WeaponItem JUDGE;

    // --- Rifles ---
    public static WeaponItem BULLDOG;
    public static WeaponItem GUARDIAN;
    public static WeaponItem PHANTOM;
    public static WeaponItem VANDAL;

    // --- Snipers ---
    public static WeaponItem MARSHAL;
    public static WeaponItem OUTLAW;
    public static WeaponItem OPERATOR;

    // --- Heavy Weapons ---
    public static WeaponItem ARES;
    public static WeaponItem ODIN;

    public static void registerWeapons() {
        // =========================================================================
        // SIDEARMS
        // =========================================================================

        // Classic (Free Sidearm) — Semi-auto (Valo: 78/26/22 at 0-30m, 66/22/19 at 30-50m)
        CLASSIC = register("classic", new WeaponData(
            "classic", "Classic", "SIDEARM", 0, 12, 6.75, 1.75, 0.75, 0.40,
            List.of(
                new WeaponData.DamageRange(0, 30, 15.6, 5.2, 4.4),
                new WeaponData.DamageRange(30, 50, 13.2, 4.4, 3.7)
            )
        ));

        // Shorty (Sidearm Shotgun) — Semi-auto, 2 devastating close-range blasts
        SHORTY = register("shorty", new WeaponData(
            "shorty", "Shorty", "SIDEARM", 300, 2, 3.3, 1.75, 0.75, 4.0,
            List.of(
                new WeaponData.DamageRange(0, 7, 28.0, 14.0, 11.0),
                new WeaponData.DamageRange(7, 15, 16.0, 8.0, 6.5),
                new WeaponData.DamageRange(15, 50, 8.0, 4.0, 3.0)
            )
        ));

        // Frenzy (Automatic Pistol) — Full-auto (Valo: 78/26/22 at 0-20m, 63/21/17 at 20-50m)
        FRENZY = register("frenzy", new WeaponData(
            "frenzy", "Frenzy", "SIDEARM", 450, 13, 10.0, 1.5, 1.0, 0.65,
            List.of(
                new WeaponData.DamageRange(0, 20, 15.6, 5.2, 4.4),
                new WeaponData.DamageRange(20, 50, 12.6, 4.2, 3.4)
            )
        ));

        // Ghost (Silenced Pistol) — Semi-auto (Valo: 105/30/25.5 at 0-30m -> 1-tap unarmored!, 87.5/25/21.2 at 30-50m)
        GHOST = register("ghost", new WeaponData(
            "ghost", "Ghost", "SIDEARM", 500, 15, 6.75, 1.5, 0.75, 0.30,
            List.of(
                new WeaponData.DamageRange(0, 30, 21.0, 6.0, 5.1),
                new WeaponData.DamageRange(30, 50, 17.5, 5.0, 4.2)
            )
        ));

        // Sheriff (Heavy Revolver) — Semi-auto (Valo: 159.5/55/46.8 at 0-30m -> 1-tap kill!, 145/50/42.5 at 30-50m)
        SHERIFF = register("sheriff", new WeaponData(
            "sheriff", "Sheriff", "SIDEARM", 800, 6, 4.0, 2.25, 1.0, 0.25,
            List.of(
                new WeaponData.DamageRange(0, 30, 31.9, 11.0, 9.4),
                new WeaponData.DamageRange(30, 50, 29.0, 10.0, 8.5)
            )
        ));

        // =========================================================================
        // SMGS
        // =========================================================================

        // Stinger (Rapid-Fire SMG) — Full-auto, 16 RPS blazing cadence (Valo: 67/27/23 at 0-15m, 57/23/19 at 15-50m)
        STINGER = register("stinger", new WeaponData(
            "stinger", "Stinger", "SMG", 1100, 20, 16.0, 2.25, 0.75, 0.65,
            List.of(
                new WeaponData.DamageRange(0, 15, 13.4, 5.4, 4.6),
                new WeaponData.DamageRange(15, 50, 11.4, 4.6, 3.8)
            )
        ));

        // Spectre (Silenced SMG) — Full-auto, stealth (Valo: 78/26/22 at 0-15m, 66/22/18.7 at 15-30m, 60/20/17 at 30-50m)
        SPECTRE = register("spectre", new WeaponData(
            "spectre", "Spectre", "SMG", 1600, 30, 13.33, 2.25, 0.75, 0.30,
            List.of(
                new WeaponData.DamageRange(0, 15, 15.6, 5.2, 4.4),
                new WeaponData.DamageRange(15, 30, 13.2, 4.4, 3.7),
                new WeaponData.DamageRange(30, 50, 12.0, 4.0, 3.4)
            )
        ));

        // =========================================================================
        // SHOTGUNS
        // =========================================================================

        // Bucky (Pump-Action Shotgun) — Semi-auto, devastating close blast
        BUCKY = register("bucky", new WeaponData(
            "bucky", "Bucky", "SHOTGUN", 850, 5, 1.1, 2.5, 1.0, 3.0,
            List.of(
                new WeaponData.DamageRange(0, 8, 36.0, 18.0, 15.0),
                new WeaponData.DamageRange(8, 12, 22.0, 11.0, 9.0),
                new WeaponData.DamageRange(12, 50, 12.0, 6.0, 5.0)
            )
        ));

        // Judge (Automatic Drum Shotgun) — Full-auto, rapid close-quarters fire
        JUDGE = register("judge", new WeaponData(
            "judge", "Judge", "SHOTGUN", 1850, 7, 3.5, 2.2, 1.0, 2.5,
            List.of(
                new WeaponData.DamageRange(0, 10, 26.0, 13.0, 10.5),
                new WeaponData.DamageRange(10, 15, 16.0, 8.0, 6.5),
                new WeaponData.DamageRange(15, 50, 10.0, 5.0, 4.0)
            )
        ));

        // =========================================================================
        // RIFLES
        // =========================================================================

        // Bulldog (Bullpup Assault Rifle) — Full-auto (Valo: 115/35/30 -> 1-tap headshot!)
        BULLDOG = register("bulldog", new WeaponData(
            "bulldog", "Bulldog", "RIFLE", 2050, 24, 10.0, 2.5, 1.0, 0.30,
            List.of(
                new WeaponData.DamageRange(0, 50, 23.0, 7.0, 6.0)
            )
        ));

        // Guardian (Designated Marksman Rifle) — Semi-auto, 1-tap kill at all ranges! (Valo: 195/65/48.8)
        GUARDIAN = register("guardian", new WeaponData(
            "guardian", "Guardian", "RIFLE", 2250, 12, 5.25, 2.5, 1.0, 0.10,
            List.of(
                new WeaponData.DamageRange(0, 50, 39.0, 13.0, 9.8)
            )
        ));

        // Phantom (Silenced Assault Rifle) — Full-auto (Valo: 156/39/33 at 0-15m -> 1-tap!, 140/35/29 at 15-30m, 124/31/26 at 30-50m)
        PHANTOM = register("phantom", new WeaponData(
            "phantom", "Phantom", "RIFLE", 2900, 30, 11.0, 2.5, 1.0, 0.20,
            List.of(
                new WeaponData.DamageRange(0, 15, 31.2, 7.8, 6.6),
                new WeaponData.DamageRange(15, 30, 28.0, 7.0, 5.8),
                new WeaponData.DamageRange(30, 50, 24.8, 6.2, 5.2)
            )
        ));

        // Vandal (AK Assault Rifle) — Full-auto, 1-tap kill at ANY range! (Valo: 160/40/34)
        VANDAL = register("vandal", new WeaponData(
            "vandal", "Vandal", "RIFLE", 2900, 25, 9.75, 2.5, 1.0, 0.25,
            List.of(
                new WeaponData.DamageRange(0, 50, 32.0, 8.0, 6.8)
            )
        ));

        // =========================================================================
        // SNIPERS
        // =========================================================================

        // Marshal (Lever-Action Sniper) — Semi-auto (Valo: 202/101/85.9 -> 1-tap head & 1-tap body on unarmored!)
        MARSHAL = register("marshal", new WeaponData(
            "marshal", "Marshal", "SNIPER", 950, 5, 1.5, 2.5, 1.25, 0.10,
            List.of(
                new WeaponData.DamageRange(0, 50, 40.4, 20.2, 17.2)
            )
        ));

        // Outlaw (Double-Barrel High-Impact Sniper) — Semi-auto, 2 rapid shots (Valo: 238/140/119 -> 1-tap body!)
        OUTLAW = register("outlaw", new WeaponData(
            "outlaw", "Outlaw", "SNIPER", 2400, 2, 2.75, 3.8, 1.25, 0.15,
            List.of(
                new WeaponData.DamageRange(0, 50, 47.6, 28.0, 23.8)
            )
        ));

        // Operator (Heavy Bolt-Action Sniper) — Semi-auto, 1-tap headshot & 1-tap body shot! (Valo: 255/150/120)
        OPERATOR = register("operator", new WeaponData(
            "operator", "Operator", "SNIPER", 4700, 5, 0.6, 3.7, 1.5, 0.0,
            List.of(
                new WeaponData.DamageRange(0, 50, 51.0, 30.0, 24.0)
            )
        ));

        // =========================================================================
        // HEAVY WEAPONS
        // =========================================================================

        // Ares (Light Machine Gun) — Full-auto (Valo: 72/30/25 at 0-30m, 67/28/23 at 30-50m)
        ARES = register("ares", new WeaponData(
            "ares", "Ares", "HEAVY", 1600, 50, 13.0, 3.25, 1.25, 1.0,
            List.of(
                new WeaponData.DamageRange(0, 30, 14.4, 6.0, 5.0),
                new WeaponData.DamageRange(30, 50, 13.4, 5.6, 4.6)
            )
        ));

        // Odin (Heavy Machine Gun) — Full-auto, 100 rounds sustained suppressive fire (Valo: 95/38/32 at 0-30m, 77/31/26 at 30-50m)
        ODIN = register("odin", new WeaponData(
            "odin", "Odin", "HEAVY", 3200, 100, 12.0, 5.0, 1.25, 0.80,
            List.of(
                new WeaponData.DamageRange(0, 30, 19.0, 7.6, 6.4),
                new WeaponData.DamageRange(30, 50, 15.4, 6.2, 5.2)
            )
        ));
    }

    private static WeaponItem register(String id, WeaponData data) {
        WeaponItem item = new WeaponItem(new Item.Properties(), data);
        Registry.register(BuiltInRegistries.ITEM, ValorantMod.id(id), item);
        REGISTERED_WEAPONS.put(id, item);
        return item;
    }
}
