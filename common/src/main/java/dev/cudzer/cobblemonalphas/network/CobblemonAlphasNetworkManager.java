package dev.cudzer.cobblemonalphas.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.cudzer.cobblemonalphas.network.client.AlphaRoarHandler;
import net.fabricmc.api.EnvType;
import net.minecraft.server.level.ServerPlayer;

public class CobblemonAlphasNetworkManager {
    public static void register() {
        if (Platform.getEnv() == EnvType.SERVER) {
            registerServer();
        } else {
            registerClient();
        }
    }

    private static void registerServer() {
        NetworkManager.registerS2CPayloadType(AlphaRoarPacket.TYPE, AlphaRoarPacket.STREAM_CODEC);
    }

    private static void registerClient() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                AlphaRoarPacket.TYPE,
                AlphaRoarPacket.STREAM_CODEC,
                AlphaRoarHandler::handle);
    }

    public static void sendRoarEffect(ServerPlayer player, int entityId, float duration) {
        NetworkManager.sendToPlayer(player, new AlphaRoarPacket(entityId, duration));
    }
}
