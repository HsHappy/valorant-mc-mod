package com.valorant.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GunAmmoComponent(int currentAmmo, int reserveAmmo, boolean isReloading, long reloadFinishTime) {
    public static final Codec<GunAmmoComponent> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("current").forGetter(GunAmmoComponent::currentAmmo),
            Codec.INT.fieldOf("reserve").forGetter(GunAmmoComponent::reserveAmmo),
            Codec.BOOL.fieldOf("reloading").forGetter(GunAmmoComponent::isReloading),
            Codec.LONG.fieldOf("finish_time").forGetter(GunAmmoComponent::reloadFinishTime)
        ).apply(instance, GunAmmoComponent::new)
    );

    public static final StreamCodec<ByteBuf, GunAmmoComponent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, GunAmmoComponent::currentAmmo,
        ByteBufCodecs.VAR_INT, GunAmmoComponent::reserveAmmo,
        ByteBufCodecs.BOOL, GunAmmoComponent::isReloading,
        ByteBufCodecs.VAR_LONG, GunAmmoComponent::reloadFinishTime,
        GunAmmoComponent::new
    );

    public GunAmmoComponent withCurrent(int newCurrent) {
        return new GunAmmoComponent(newCurrent, this.reserveAmmo, this.isReloading, this.reloadFinishTime);
    }

    public GunAmmoComponent startReload(long finishTime) {
        return new GunAmmoComponent(this.currentAmmo, this.reserveAmmo, true, finishTime);
    }

    public GunAmmoComponent finishReload(int magSize) {
        int needed = magSize - this.currentAmmo;
        int taken = Math.min(needed, this.reserveAmmo);
        return new GunAmmoComponent(this.currentAmmo + taken, this.reserveAmmo - taken, false, 0);
    }

    public GunAmmoComponent cancelReload() {
        return new GunAmmoComponent(this.currentAmmo, this.reserveAmmo, false, 0);
    }
}
