package com.valorant.weapon;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WeaponItem extends Item {
    private final WeaponData weaponData;

    public WeaponItem(Properties properties, WeaponData weaponData) {
        super(properties.stacksTo(1));
        this.weaponData = weaponData;
    }

    public WeaponData getWeaponData() {
        return weaponData;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Right click handles ADS / Secondary Zoom or Alt-fire
        if (level.isClientSide()) {
            // Client-side zoom toggle / ADS logic
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
