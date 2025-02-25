package dev.cudzer.cobblemonalphas.fabric;

import com.cobblemon.mod.common.NetworkManager;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.IPlatform;
import dev.cudzer.cobblemonalphas.entity.spawner.AlphaSpawner;
import dev.cudzer.cobblemonalphas.particles.AlphaParticleEffect;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class CobblemonAlphasModFabric implements ModInitializer, IPlatform {

    @Override
    public void onInitialize() {
        // Run our common setup.
        CobblemonAlphasMod.init(this);

        CommandRegistrationCallback.EVENT.register((((commandDispatcher, commandBuildContext, commandSelection) -> {
            CobblemonAlphasMod.registerCommands(commandDispatcher);
        })));

        ServerLifecycleEvents.SERVER_STARTING.register(event -> {
            try{
                AlphaSpawner.getInstance().setServer(event.getConnection().getServer());
                AlphaSpawner.getInstance().init();
            }catch (Throwable t){
                CobblemonAlphasMod.LOGGER.warn("An exception occurred on the server start for CobblemonAlphas");
                t.printStackTrace();
            }

        });

        ServerTickEvents.END_SERVER_TICK.register(event -> {
            try{
                AlphaSpawner.getInstance().tick();
                AlphaParticleEffect.tick(event.getConnection().getServer());

            }
            catch (Throwable t){
                CobblemonAlphasMod.LOGGER.warn("An exception occurred in the tick method for CobblemonAlphas");
                t.printStackTrace();
            }
        });
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
