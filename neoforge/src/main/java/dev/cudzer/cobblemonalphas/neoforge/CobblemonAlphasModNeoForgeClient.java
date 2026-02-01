package dev.cudzer.cobblemonalphas.neoforge;

import dev.cudzer.cobblemonalphas.CobblemonAlphasClient;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CobblemonAlphasMod.MOD_ID, value = Dist.CLIENT)
public class CobblemonAlphasModNeoForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CobblemonAlphasClient.init();
    }
}
