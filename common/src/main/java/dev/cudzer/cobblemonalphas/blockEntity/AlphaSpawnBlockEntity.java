package dev.cudzer.cobblemonalphas.blockEntity;

import dev.cudzer.cobblemonalphas.config.ModConfig;
import dev.cudzer.cobblemonalphas.data.AlphaJsonDataManager;
import dev.cudzer.cobblemonalphas.entity.Alpha;
import dev.cudzer.cobblemonalphas.entity.spawner.AlphaSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

public class AlphaSpawnBlockEntity extends BlockEntity {

    protected static int ticks = 0;

    public AlphaSpawnBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntityType.ALPHA_SPAWNER, blockPos, blockState);
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, AlphaSpawnBlockEntity spawnerBlockEntity) {
        //check range
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, AlphaSpawnBlockEntity spawnerBlockEntity) {
        if(ticks++ % 40 == 0){
            Optional<Player> opt = level.getEntities(EntityType.PLAYER, new AABB(blockPos).inflate(8, 8, 8), EntitySelector.NO_CREATIVE_OR_SPECTATOR).stream().findFirst();
            opt.ifPresent(player -> {
                level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());

                Alpha alpha = AlphaJsonDataManager.getRandomAlphaForBiome(level, level.getBiome(blockPos).unwrapKey().get(), false).values().stream().toList().getFirst();

                AlphaSpawner.getInstance().spawnAlphaEntity(alpha, level, blockPos, ModConfig.doHerdSpawning);
            });
        }
    }
}
