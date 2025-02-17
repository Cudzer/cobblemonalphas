package dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SkyVisible implements ISpawnCondition{

    private final boolean skyVisible;

    public SkyVisible(boolean skyVisible){
        this.skyVisible = skyVisible;
    }

    @Override
    public boolean isSafe(Level level, BlockPos blockPos) {
        return level.canSeeSky(blockPos) == skyVisible;
    }
}
