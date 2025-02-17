package dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class HeightBounds implements ISpawnCondition{

    private final int minYLevel;
    private final int maxYLevel;

    public HeightBounds(int minYLevel, int maxYLevel){
        this.minYLevel = minYLevel;
        this.maxYLevel = maxYLevel;
    }

    @Override
    public boolean isSafe(Level level, BlockPos blockPos) {
        return blockPos.getY() > minYLevel && blockPos.getY() < maxYLevel;
    }
}
