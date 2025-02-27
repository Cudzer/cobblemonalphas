package dev.cudzer.cobblemonalphas.neoforge;

import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.IPlatform;
import dev.cudzer.cobblemonalphas.entity.spawner.AlphaSpawner;
import dev.cudzer.cobblemonalphas.particles.AlphaParticleEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.nio.file.Path;

@Mod(CobblemonAlphasMod.MOD_ID)
public final class CobblemonAlphasModNeoForge implements IPlatform {

    public CobblemonAlphasModNeoForge() {
        // Run our common setup.
        NeoForge.EVENT_BUS.register(this);
        CobblemonAlphasMod.init(this);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @SubscribeEvent
    public void onCommandRegistration(final RegisterCommandsEvent event){
        CobblemonAlphasMod.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Post event){
        try{
            AlphaSpawner.getInstance().tick();
            AlphaParticleEffect.tick(event.getServer());

        }
        catch (Throwable t){
            CobblemonAlphasMod.LOGGER.warn("An exception occurred in the tick method for Cobblemon Alphas: {}", t.getMessage());
        }
    }

    @SubscribeEvent
    public void onServerStart(final ServerStartedEvent event){
        try{
            AlphaSpawner.getInstance().setServer(event.getServer());
            AlphaSpawner.getInstance().init();
        }catch (Throwable t){
            CobblemonAlphasMod.LOGGER.warn("An exception occurred on the server start for Cobblemon Alphas: {}", t.getMessage());
        }
    }
}
