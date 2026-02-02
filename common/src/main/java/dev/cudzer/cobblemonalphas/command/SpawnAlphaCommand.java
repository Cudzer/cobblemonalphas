package dev.cudzer.cobblemonalphas.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.cudzer.cobblemonalphas.data.AlphaJsonDataManager;
import dev.cudzer.cobblemonalphas.entity.Alpha;
import dev.cudzer.cobblemonalphas.entity.spawner.AlphaSpawner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class SpawnAlphaCommand {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("alphaspawn").requires(src -> src.hasPermission(2))
                .then(Commands.argument("pokemon", StringArgumentType.string())
                        .suggests((ctx, sb) -> SharedSuggestionProvider.suggest(getAlphaNames(), sb))
                        .then(Commands.argument("spawnHerd", BoolArgumentType.bool())
                                .executes(SpawnAlphaCommand::spawnAlpha))));

        dispatcher.register(Commands.literal("alphaspawn").requires(src -> src.hasPermission(2))
                .then(Commands.literal("random")
                        .then(Commands.argument("spawnHerd", BoolArgumentType.bool())
                                .executes(SpawnAlphaCommand::spawnAlphaRandom))));
    }

    private static int spawnAlpha(CommandContext<CommandSourceStack> context) {
        return spawn(context, false);
    }

    private static int spawnAlphaRandom(CommandContext<CommandSourceStack> context) {
        return spawn(context, true);
    }

    private static int spawn(CommandContext<CommandSourceStack> context, boolean isRandom) {
        if (context.getSource().isPlayer()) {
            Alpha alpha = isRandom ? AlphaJsonDataManager.getRandomAlphaObj(context.getSource().getLevel())
                    : AlphaJsonDataManager.getAlphaObj(context.getSource().getLevel(),
                            StringArgumentType.getString(context, "pokemon"));
                            
            boolean spawnHerd = BoolArgumentType.getBool(context, "spawnHerd");

            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure((Component.literal("Source player not found!")));
                return -1;
            }
            Vec3i spawnPos = new Vec3i((int) player.getX(), (int) player.getY(), (int) player.getZ());

            if (alpha == null) {
                context.getSource().sendFailure((Component.literal("Alpha not found!")));
                return -1;
            }

            AlphaSpawner.getInstance().spawnAlphaEntity(alpha, player.level(), spawnPos, spawnHerd);
        }
        
        return 1;
    }

    private static Set<String> getAlphaNames() {
        var alphaData = AlphaJsonDataManager.getAlphaData();
        Set<String> names = new HashSet<>();
        for (Alpha alpha : alphaData.values()) {
            names.add(alpha.getSpecies());
        }
        return names;
    }
}
