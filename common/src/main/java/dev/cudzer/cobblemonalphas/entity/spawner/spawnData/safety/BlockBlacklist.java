package dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlockBlacklist implements ISpawnCondition{

    private final List<Block> blocks;

    public BlockBlacklist(List<Block> blocks){
        this.blocks = blocks;
    }

    @Override
    public boolean isSafe(Level level, BlockPos blockPos) {
        while(level.getBlockState(blockPos).isAir() && level.isInWorldBounds(blockPos)){
            blockPos = blockPos.below();
        }
        return !blocks.contains(level.getBlockState(blockPos).getBlock());
    }
}
