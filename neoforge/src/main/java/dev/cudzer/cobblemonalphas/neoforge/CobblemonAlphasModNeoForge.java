package dev.cudzer.cobblemonalphas.neoforge;

import com.cobblemon.mod.common.NetworkManager;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.IPlatform;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.nio.file.Path;

@Mod(CobblemonAlphasMod.MOD_ID)
public final class CobblemonAlphasModNeoForge implements IPlatform {
    public CobblemonAlphasModNeoForge() {
        // Run our common setup.
        CobblemonAlphasMod.init(this);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @SubscribeEvent
    public void onCommandRegistration(final RegisterCommandsEvent event){
        CobblemonAlphasMod.registerCommands(event.getDispatcher());
    }
}
