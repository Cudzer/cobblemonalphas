package dev.cudzer.cobblemonalphas.render.layerEntities.states;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cobblemon.mod.common.api.scheduling.SchedulingTracker;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;

import net.minecraft.world.entity.Entity;

public class AlphaEyesState extends PosableState {
    private final SchedulingTracker schedulingTracker;

    public AlphaEyesState() {
        setPose("idle");
        this.schedulingTracker = new SchedulingTracker();
    }

    @Override
    public @Nullable Entity getEntity() {
        return null;
    }

    @Override
    public void updatePartialTicks(float partialTicks) {
        this.setCurrentPartialTicks(partialTicks);
    }

    @Override
    public @NotNull SchedulingTracker getSchedulingTracker() {
        return schedulingTracker;
    }
}
