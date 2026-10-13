package com.valorant.network;

import com.valorant.ValorantMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ReloadPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReloadPayload> ID = new CustomPacketPayload.Type<>(ValorantMod.id("reload"));
    public static final StreamCodec<ByteBuf, ReloadPayload> CODEC = StreamCodec.unit(new ReloadPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
