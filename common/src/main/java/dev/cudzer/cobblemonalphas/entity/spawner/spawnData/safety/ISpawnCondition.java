package dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ISpawnCondition {
    boolean isSafe(Level level, BlockPos blockPos);
}
