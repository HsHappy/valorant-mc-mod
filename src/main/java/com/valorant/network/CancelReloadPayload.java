package com.valorant.network;

import com.valorant.ValorantMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CancelReloadPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CancelReloadPayload> ID = new CustomPacketPayload.Type<>(ValorantMod.id("cancel_reload"));
    public static final StreamCodec<ByteBuf, CancelReloadPayload> CODEC = StreamCodec.unit(new CancelReloadPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
