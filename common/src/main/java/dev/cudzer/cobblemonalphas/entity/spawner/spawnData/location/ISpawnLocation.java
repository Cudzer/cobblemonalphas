package dev.cudzer.cobblemonalphas.entity.spawner.spawnData.location;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface ISpawnLocation {
    @Nullable
    Vec3i getSpawnLocation(Level level, Vec3 center);
}
