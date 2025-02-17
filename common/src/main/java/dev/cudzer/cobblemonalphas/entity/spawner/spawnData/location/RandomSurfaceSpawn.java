package dev.cudzer.cobblemonalphas.entity.spawner.spawnData.location;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RandomSurfaceSpawn implements ISpawnLocation{

    private final int minimumSpawnDistance;
    private final int maximumSpawnDistance;

    public RandomSurfaceSpawn(int minimumSpawnDistance, int maximumSpawnDistance){
        this.minimumSpawnDistance = minimumSpawnDistance;
        this.maximumSpawnDistance = maximumSpawnDistance;
    }

    @Override
    public @Nullable Vec3i getSpawnLocation(Level level, Vec3 center) {
        double dist = minimumSpawnDistance + ((maximumSpawnDistance - minimumSpawnDistance) * Math.random());
        double theta = 2 * Math.PI * Math.random();
        double x = center.x() + (dist * Math.cos(theta));
        double z = center.z() + (dist * Math.sin(theta));
        BlockPos blockPos = new BlockPos((int)x, level.getMaxBuildHeight() - 1, (int)z);
        while(level.getBlockState(blockPos).isAir() && level.isInWorldBounds(blockPos)){
            blockPos = blockPos.below();
        }
        while(!level.getBlockState(blockPos).isAir() && level.isInWorldBounds(blockPos)){
            blockPos = blockPos.above();
        }
        return level.isInWorldBounds(blockPos) ? new Vec3i((int)x, blockPos.getY(), (int)z) : null;
    }
}
