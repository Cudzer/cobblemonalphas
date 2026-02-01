package dev.cudzer.cobblemonalphas.network;

import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AlphaRoarPacket(int pokemonId, float duration) implements CustomPacketPayload {
    public static final ResourceLocation PACKET_ID = ResourceLocation.fromNamespaceAndPath(
            CobblemonAlphasMod.MOD_ID,
            "alpha_roar");
    public static final Type<AlphaRoarPacket> TYPE = new Type<>(PACKET_ID);

    public static final StreamCodec<ByteBuf, AlphaRoarPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            AlphaRoarPacket::pokemonId,

            ByteBufCodecs.FLOAT,
            AlphaRoarPacket::duration,

            AlphaRoarPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
