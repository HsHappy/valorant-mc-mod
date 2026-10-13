package com.valorant.weapon;

import com.valorant.ValorantMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ValorantItemGroups {
    public static final ResourceKey<CreativeModeTab> ARSENAL_KEY = ResourceKey.create(
        BuiltInRegistries.CREATIVE_MODE_TAB.key(),
        ValorantMod.id("arsenal")
    );

    public static CreativeModeTab ARSENAL;

    public static void register() {
        ARSENAL = FabricItemGroup.builder()
            .icon(() -> new ItemStack(WeaponRegistry.VANDAL))
            .title(Component.translatable("itemGroup.valorant.weapons"))
            .displayItems((params, output) -> {
                for (WeaponItem item : WeaponRegistry.REGISTERED_WEAPONS.values()) {
                    output.accept(item);
                }
            })
            .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ARSENAL_KEY, ARSENAL);
    }
}
