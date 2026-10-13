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

        // Classic (Free Sidearm) — Semi-auto
        CLASSIC = register("classic", new WeaponData(
            "classic", "Classic", "SIDEARM", 0, 12, 6.75, 1.75, 0.75, 0.40,
            List.of(
                new WeaponData.DamageRange(0, 30, 10.5, 3.5, 3.0),
                new WeaponData.DamageRange(30, 50, 9.0, 3.0, 2.5)
            )
        ));

        // Shorty (Sidearm Shotgun) — Semi-auto, 2 high-damage close-range blasts
        SHORTY = register("shorty", new WeaponData(
            "shorty", "Shorty", "SIDEARM", 300, 2, 3.3, 1.75, 0.75, 4.0,
            List.of(
                new WeaponData.DamageRange(0, 7, 16.0, 8.0, 6.5),
                new WeaponData.DamageRange(7, 15, 9.0, 4.5, 3.5),
                new WeaponData.DamageRange(15, 50, 4.5, 2.2, 1.8)
            )
        ));

        // Frenzy (Automatic Pistol) — Full-auto
        FRENZY = register("frenzy", new WeaponData(
            "frenzy", "Frenzy", "SIDEARM", 450, 13, 10.0, 1.5, 1.0, 0.65,
            List.of(
                new WeaponData.DamageRange(0, 20, 9.0, 3.2, 2.7),
                new WeaponData.DamageRange(20, 50, 7.5, 2.6, 2.2)
            )
        ));

        // Ghost (Silenced Pistol) — Semi-auto, 1-tap on low armor, accurate
        GHOST = register("ghost", new WeaponData(
            "ghost", "Ghost", "SIDEARM", 500, 15, 6.75, 1.5, 0.75, 0.30,
            List.of(
                new WeaponData.DamageRange(0, 30, 14.0, 4.0, 3.4),
                new WeaponData.DamageRange(30, 50, 11.5, 3.3, 2.8)
            )
        ));

        // Sheriff (Heavy Revolver) — Semi-auto, 0-30m Head = 25.0 (Instakill 1-tap!), 30m+ = 19.0 (survives 1 HP)
        SHERIFF = register("sheriff", new WeaponData(
            "sheriff", "Sheriff", "SIDEARM", 800, 6, 4.0, 2.25, 1.0, 0.25,
            List.of(
                new WeaponData.DamageRange(0, 30, 25.0, 7.5, 6.5),
                new WeaponData.DamageRange(30, 50, 19.0, 6.8, 5.8)
            )
        ));

        // =========================================================================
        // SMGS
        // =========================================================================

        // Stinger (Rapid-Fire SMG) — Full-auto, 16 RPS blazing cadence
        STINGER = register("stinger", new WeaponData(
            "stinger", "Stinger", "SMG", 1100, 20, 16.0, 2.25, 0.75, 0.65,
            List.of(
                new WeaponData.DamageRange(0, 15, 8.5, 3.2, 2.7),
                new WeaponData.DamageRange(15, 50, 7.2, 2.7, 2.3)
            )
        ));

        // Spectre (Silenced SMG) — Full-auto, stealth, smooth spray
        SPECTRE = register("spectre", new WeaponData(
            "spectre", "Spectre", "SMG", 1600, 30, 13.33, 2.25, 0.75, 0.30,
            List.of(
                new WeaponData.DamageRange(0, 15, 10.5, 3.6, 3.0),
                new WeaponData.DamageRange(15, 30, 9.0, 3.0, 2.5),
                new WeaponData.DamageRange(30, 50, 8.0, 2.6, 2.2)
            )
        ));

        // =========================================================================
        // SHOTGUNS
        // =========================================================================

        // Bucky (Pump-Action Shotgun) — Semi-auto, devastating close blast
        BUCKY = register("bucky", new WeaponData(
            "bucky", "Bucky", "SHOTGUN", 850, 5, 1.1, 2.5, 1.0, 3.0,
            List.of(
                new WeaponData.DamageRange(0, 8, 22.0, 12.0, 10.0),
                new WeaponData.DamageRange(8, 12, 14.0, 7.5, 6.5),
                new WeaponData.DamageRange(12, 50, 8.0, 4.5, 3.8)
            )
        ));

        // Judge (Automatic Drum Shotgun) — Full-auto, rapid close-quarters fire
        JUDGE = register("judge", new WeaponData(
            "judge", "Judge", "SHOTGUN", 1850, 7, 3.5, 2.2, 1.0, 2.5,
            List.of(
                new WeaponData.DamageRange(0, 10, 16.0, 8.0, 7.0),
                new WeaponData.DamageRange(10, 15, 10.0, 5.0, 4.2),
                new WeaponData.DamageRange(15, 50, 6.0, 3.2, 2.7)
            )
        ));

        // =========================================================================
        // RIFLES
        // =========================================================================

        // Bulldog (Bullpup Assault Rifle) — Full-auto
        BULLDOG = register("bulldog", new WeaponData(
            "bulldog", "Bulldog", "RIFLE", 2050, 24, 10.0, 2.5, 1.0, 0.30,
            List.of(
                new WeaponData.DamageRange(0, 50, 15.0, 4.8, 4.0)
            )
        ));

        // Guardian (Designated Marksman Rifle) — Semi-auto, 1-tap kill at all ranges!
        GUARDIAN = register("guardian", new WeaponData(
            "guardian", "Guardian", "RIFLE", 2250, 12, 5.25, 2.5, 1.0, 0.10,
            List.of(
                new WeaponData.DamageRange(0, 50, 25.0, 8.5, 6.5)
            )
        ));

        // Phantom (Silenced Assault Rifle) — Full-auto, 0-15m 1-tap, 15m+ 2-shot headshot
        PHANTOM = register("phantom", new WeaponData(
            "phantom", "Phantom", "RIFLE", 2900, 30, 11.0, 2.5, 1.0, 0.20,
            List.of(
                new WeaponData.DamageRange(0, 15, 22.0, 5.2, 4.4),
                new WeaponData.DamageRange(15, 30, 18.5, 4.6, 4.0),
                new WeaponData.DamageRange(30, 50, 16.5, 4.2, 3.6)
            )
        ));

        // Vandal (AK Assault Rifle) — Full-auto, 1-tap kill at ANY range!
        VANDAL = register("vandal", new WeaponData(
            "vandal", "Vandal", "RIFLE", 2900, 25, 9.75, 2.5, 1.0, 0.25,
            List.of(
                new WeaponData.DamageRange(0, 50, 25.0, 5.5, 4.5)
            )
        ));

        // =========================================================================
        // SNIPERS
        // =========================================================================

        // Marshal (Lever-Action Sniper) — Semi-auto, fast rechamber, 1-tap headshot
        MARSHAL = register("marshal", new WeaponData(
            "marshal", "Marshal", "SNIPER", 950, 5, 1.5, 2.5, 1.25, 0.10,
            List.of(
                new WeaponData.DamageRange(0, 50, 25.0, 14.0, 11.5)
            )
        ));

        // Outlaw (Double-Barrel High-Impact Sniper) — Semi-auto, 2 rapid shots, 140 body dmg in Valo (18.5 HP)
        OUTLAW = register("outlaw", new WeaponData(
            "outlaw", "Outlaw", "SNIPER", 2400, 2, 2.75, 3.8, 1.25, 0.15,
            List.of(
                new WeaponData.DamageRange(0, 50, 30.0, 18.5, 15.5)
            )
        ));

        // Operator (Heavy Bolt-Action Sniper) — Semi-auto, 1-tap headshot & 1-tap body shot!
        OPERATOR = register("operator", new WeaponData(
            "operator", "Operator", "SNIPER", 4700, 5, 0.6, 3.7, 1.5, 0.0,
            List.of(
                new WeaponData.DamageRange(0, 50, 50.0, 22.0, 16.0)
            )
        ));

        // =========================================================================
        // HEAVY WEAPONS
        // =========================================================================

        // Ares (Light Machine Gun) — Full-auto, 50 rounds
        ARES = register("ares", new WeaponData(
            "ares", "Ares", "HEAVY", 1600, 50, 13.0, 3.25, 1.25, 1.0,
            List.of(
                new WeaponData.DamageRange(0, 30, 10.0, 4.0, 3.4),
                new WeaponData.DamageRange(30, 50, 9.0, 3.6, 3.0)
            )
        ));

        // Odin (Heavy Machine Gun) — Full-auto, 100 rounds sustained suppressive fire
        ODIN = register("odin", new WeaponData(
            "odin", "Odin", "HEAVY", 3200, 100, 12.0, 5.0, 1.25, 0.80,
            List.of(
                new WeaponData.DamageRange(0, 30, 12.5, 5.0, 4.2),
                new WeaponData.DamageRange(30, 50, 10.5, 4.2, 3.5)
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
