package dev.cudzer.cobblemonalphas.fabric.client;

import dev.cudzer.cobblemonalphas.CobblemonAlphasClient;
import net.fabricmc.api.ClientModInitializer;

public final class CobblemonAlphasModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CobblemonAlphasClient.init();
    }
}
