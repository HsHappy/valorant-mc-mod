package com.valorant.mixin.client;

import com.valorant.client.gui.ValorantCrosshairOverlay;
import com.valorant.weapon.WeaponItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.player.getMainHandItem().getItem() instanceof WeaponItem) {
            // Cancel vanilla crosshair
            ci.cancel();
            // Draw clean Valorant Crosshair
            ValorantCrosshairOverlay.render(guiGraphics, client);
        }
    }
}
