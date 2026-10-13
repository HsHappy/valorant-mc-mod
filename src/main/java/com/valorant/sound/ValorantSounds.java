package com.valorant.sound;

import com.valorant.ValorantMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class ValorantSounds {
    public static SoundEvent VANDAL_FIRE;
    public static SoundEvent PHANTOM_FIRE;
    public static SoundEvent CLASSIC_FIRE;
    public static SoundEvent FRENZY_FIRE;
    public static SoundEvent GHOST_FIRE;
    public static SoundEvent SHERIFF_FIRE;
    public static SoundEvent SHORTY_FIRE;
    public static SoundEvent BUCKY_FIRE;
    public static SoundEvent JUDGE_FIRE;
    public static SoundEvent STINGER_FIRE;
    public static SoundEvent SPECTRE_FIRE;
    public static SoundEvent OPERATOR_FIRE;
    public static SoundEvent MARSHAL_FIRE;
    public static SoundEvent BULLDOG_FIRE;
    public static SoundEvent GUARDIAN_FIRE;
    public static SoundEvent OUTLAW_FIRE;
    public static SoundEvent ARES_FIRE;
    public static SoundEvent ODIN_FIRE;
    public static SoundEvent WEAPON_RELOAD;
    public static SoundEvent VANDAL_RELOAD;
    public static SoundEvent PHANTOM_RELOAD;
    public static SoundEvent FRENZY_RELOAD;
    public static SoundEvent GHOST_RELOAD;
    public static SoundEvent SHERIFF_RELOAD;
    public static SoundEvent SHORTY_RELOAD;
    public static SoundEvent BUCKY_RELOAD;
    public static SoundEvent JUDGE_RELOAD;
    public static SoundEvent STINGER_RELOAD;
    public static SoundEvent MARSHAL_RELOAD;
    public static SoundEvent SPECTRE_RELOAD;
    public static SoundEvent BULLDOG_RELOAD;
    public static SoundEvent GUARDIAN_RELOAD;
    public static SoundEvent OUTLAW_RELOAD;
    public static SoundEvent OPERATOR_RELOAD;
    public static SoundEvent ARES_RELOAD;
    public static SoundEvent ODIN_RELOAD;
    public static SoundEvent HEADSHOT_KILL;
    public static SoundEvent BODYSHOT_HIT;

    public static void registerSounds() {
        VANDAL_FIRE = register("weapon.vandal.fire");
        VANDAL_RELOAD = register("weapon.vandal.reload");
        PHANTOM_FIRE = register("weapon.phantom.fire");
        PHANTOM_RELOAD = register("weapon.phantom.reload");
        CLASSIC_FIRE = register("weapon.classic.fire");
        FRENZY_FIRE = register("weapon.frenzy.fire");
        FRENZY_RELOAD = register("weapon.frenzy.reload");
        GHOST_FIRE = register("weapon.ghost.fire");
        GHOST_RELOAD = register("weapon.ghost.reload");
        SHERIFF_FIRE = register("weapon.sheriff.fire");
        SHERIFF_RELOAD = register("weapon.sheriff.reload");
        SHORTY_FIRE = register("weapon.shorty.fire");
        SHORTY_RELOAD = register("weapon.shorty.reload");
        BUCKY_FIRE = register("weapon.bucky.fire");
        BUCKY_RELOAD = register("weapon.bucky.reload");
        JUDGE_FIRE = register("weapon.judge.fire");
        JUDGE_RELOAD = register("weapon.judge.reload");
        STINGER_FIRE = register("weapon.stinger.fire");
        STINGER_RELOAD = register("weapon.stinger.reload");
        SPECTRE_FIRE = register("weapon.spectre.fire");
        SPECTRE_RELOAD = register("weapon.spectre.reload");
        OPERATOR_FIRE = register("weapon.operator.fire");
        MARSHAL_FIRE = register("weapon.marshal.fire");
        MARSHAL_RELOAD = register("weapon.marshal.reload");
        BULLDOG_FIRE = register("weapon.bulldog.fire");
        BULLDOG_RELOAD = register("weapon.bulldog.reload");
        GUARDIAN_FIRE = register("weapon.guardian.fire");
        GUARDIAN_RELOAD = register("weapon.guardian.reload");
        OUTLAW_FIRE = register("weapon.outlaw.fire");
        OUTLAW_RELOAD = register("weapon.outlaw.reload");
        ARES_FIRE = register("weapon.ares.fire");
        ARES_RELOAD = register("weapon.ares.reload");
        ODIN_FIRE = register("weapon.odin.fire");
        ODIN_RELOAD = register("weapon.odin.reload");
        OPERATOR_RELOAD = register("weapon.operator.reload");
        WEAPON_RELOAD = register("weapon.reload");
        HEADSHOT_KILL = register("combat.headshot_kill");
        BODYSHOT_HIT = register("combat.bodyshot_hit");
    }

    private static SoundEvent register(String id) {
        SoundEvent event = SoundEvent.createVariableRangeEvent(ValorantMod.id(id));
        return Registry.register(BuiltInRegistries.SOUND_EVENT, ValorantMod.id(id), event);
    }

    public static SoundEvent getReloadSound(String weaponId) {
        return switch (weaponId) {
            case "vandal" -> VANDAL_RELOAD;
            case "phantom" -> PHANTOM_RELOAD;
            case "frenzy" -> FRENZY_RELOAD;
            case "ghost" -> GHOST_RELOAD;
            case "sheriff" -> SHERIFF_RELOAD;
            case "shorty" -> SHORTY_RELOAD;
            case "bucky" -> BUCKY_RELOAD;
            case "judge" -> JUDGE_RELOAD;
            case "stinger" -> STINGER_RELOAD;
            case "marshal" -> MARSHAL_RELOAD;
            case "spectre" -> SPECTRE_RELOAD;
            case "bulldog" -> BULLDOG_RELOAD;
            case "guardian" -> GUARDIAN_RELOAD;
            case "outlaw" -> OUTLAW_RELOAD;
            case "operator" -> OPERATOR_RELOAD;
            case "ares" -> ARES_RELOAD;
            case "odin" -> ODIN_RELOAD;
            default -> WEAPON_RELOAD;
        };
    }

    public static SoundEvent getFireSound(String weaponId) {
        return switch (weaponId) {
            case "bulldog" -> BULLDOG_FIRE;
            case "guardian" -> GUARDIAN_FIRE;
            case "ares" -> ARES_FIRE;
            case "odin" -> ODIN_FIRE;
            case "vandal" -> VANDAL_FIRE;
            case "phantom" -> PHANTOM_FIRE;
            case "spectre" -> SPECTRE_FIRE;
            case "ghost" -> GHOST_FIRE;
            case "frenzy" -> FRENZY_FIRE;
            case "sheriff" -> SHERIFF_FIRE;
            case "shorty" -> SHORTY_FIRE;
            case "bucky" -> BUCKY_FIRE;
            case "judge" -> JUDGE_FIRE;
            case "stinger" -> STINGER_FIRE;
            case "marshal" -> MARSHAL_FIRE;
            case "classic" -> CLASSIC_FIRE;
            case "operator" -> OPERATOR_FIRE;
            case "outlaw" -> OUTLAW_FIRE;
            default -> VANDAL_FIRE;
        };
    }
}
