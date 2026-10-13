package com.valorant.network;

import com.valorant.ValorantMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server fire payload.
 *
 * Anti-Cheat Hardened:
 * The client ONLY sends its view rotation (pitch & yaw) at the exact moment of firing.
 * All spray offsets (pitchOffset, yawOffset), bloom, movement penalty, and consecutive shot
 * counts are authoritatively simulated and validated on the server.
 */
public record FirePayload(float cameraPitch, float cameraYaw) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FirePayload> ID = new CustomPacketPayload.Type<>(ValorantMod.id("fire"));
    public static final StreamCodec<ByteBuf, FirePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, FirePayload::cameraPitch,
        ByteBufCodecs.FLOAT, FirePayload::cameraYaw,
        FirePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
