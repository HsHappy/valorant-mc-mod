package com.valorant.client.gui;

import com.valorant.client.recoil.RecoilEngine;
import com.valorant.weapon.WeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ValorantCrosshairOverlay {

    // Cyan/Green Valorant color (ARGB) and black outline
    private static final int CROSSHAIR_COLOR = 0xFF00FFAA;
    private static final int OUTLINE_COLOR = 0xFF000000;

    public static void render(GuiGraphics graphics, Minecraft client) {
        if (client.player == null || !(client.player.getMainHandItem().getItem() instanceof WeaponItem)) {
            return;
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Recoil Follow Mode: offsets crosshair on screen to trace bullet trajectory
        if (RecoilEngine.recoilFollowEnabled) {
            double fov = client.options.fov().get();
            // Tam Perspektif Projeksiyon (ValorantResearcher Raporu — Patch 11.08):
            //   ΔY = (screenHeight / 2) × tan(pitchOffset) / tan(vFOV / 2)
            //   ΔX = (screenWidth  / 2) × tan(yawOffset)   / tan(hFOV / 2)
            // Eski tek-eksenli fovScale (fov * 0.9 veya tan tabanlı) yatay eksen için
            // aspect ratio'yu yok sayıyordu — geniş/dar ekranlarda hata üretiyordu.
            double halfVFovRad = Math.toRadians(fov / 2.0);
            double tanHalfVFov = Math.tan(halfVFovRad);

            // Aspect ratio'dan yatay FOV'u türet (hFOV = 2 * atan(aspect × tan(vFOV/2)))
            double aspect      = (double) screenWidth / screenHeight;
            double tanHalfHFov = tanHalfVFov * aspect;

            // Pitch offset → dikey piksel kayması
            float pitchOffset = RecoilEngine.getVisualPitchOffset();
            int   offsetY     = (int) ((screenHeight / 2.0) * Math.tan(Math.toRadians(pitchOffset)) / tanHalfVFov);

            // Yaw offset → yatay piksel kayması (aspect-ratio düzeltmeli)
            float yawOffset = RecoilEngine.getVisualYawOffset();
            int   offsetX   = (int) ((screenWidth  / 2.0) * Math.tan(Math.toRadians(yawOffset))   / tanHalfHFov);

            centerX += offsetX;
            centerY -= offsetY;
        }

        // Compact Valorant Pro Crosshair Dimensions (e.g., 1-3-1-2 style)
        int length = 3;
        int thickness = 1;
        int gap = 2;

        // Dynamic bloom: expands crosshair lines when moving, jumping, or spraying
        float totalSpread = RecoilEngine.getCurrentTotalSpread();
        if (totalSpread > 0.30f) {
            gap += Math.min(8, Math.round((totalSpread - 0.30f) * 0.75f));
        }

        int halfThick = 0;

        // Center dot removed per user request for unobstructed vision and spray tracking

        // Four Crosshair Lines (Top, Bottom, Left, Right) with 1px black outline
        // Top
        drawBox(graphics, centerX - halfThick, centerY - gap - length, thickness, length, CROSSHAIR_COLOR, OUTLINE_COLOR);
        // Bottom
        drawBox(graphics, centerX - halfThick, centerY + gap, thickness, length, CROSSHAIR_COLOR, OUTLINE_COLOR);
        // Left
        drawBox(graphics, centerX - gap - length, centerY - halfThick, length, thickness, CROSSHAIR_COLOR, OUTLINE_COLOR);
        // Right
        drawBox(graphics, centerX + gap, centerY - halfThick, length, thickness, CROSSHAIR_COLOR, OUTLINE_COLOR);
    }

    private static void drawBox(GuiGraphics graphics, int x, int y, int width, int height, int fillColor, int outlineColor) {
        // Black Outline (1px border)
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, outlineColor);
        // Main Fill
        graphics.fill(x, y, x + width, y + height, fillColor);
    }
}
