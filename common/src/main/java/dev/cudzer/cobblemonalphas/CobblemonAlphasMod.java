package dev.cudzer.cobblemonalphas;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.cudzer.cobblemonalphas.command.SpawnAlphaCommand;
import dev.cudzer.cobblemonalphas.config.ModConfig;
import dev.cudzer.cobblemonalphas.data.AlphaJsonDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CobblemonAlphasMod {
    public static final String MOD_ID = "cobblemonalphas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static IPlatform platform;

    public static ResourceLocation cobblemonAlphasResource(String path){
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init(IPlatform modPlatform) {
        platform = modPlatform;
        ModConfig.init(platform.getConfigDirectory());
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new AlphaJsonDataManager(), cobblemonAlphasResource("alphas"));

        ModEvents.registerEvents();
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher){
        SpawnAlphaCommand.registerCommand(dispatcher);
    }
}
