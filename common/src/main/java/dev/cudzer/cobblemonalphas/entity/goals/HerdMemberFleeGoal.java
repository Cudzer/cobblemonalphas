package dev.cudzer.cobblemonalphas.entity.goals;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.cudzer.cobblemonalphas.util.HerdUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class HerdMemberFleeGoal extends Goal {

    private final PokemonEntity herdEntity;
    private final double speed;
    @Nullable private PokemonEntity alpha;

    private double x, y, z;

    public HerdMemberFleeGoal(PokemonEntity herdEntity, double speedModifier){
        this.herdEntity = herdEntity;
        this.speed = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        alpha = HerdUtils.findAlpha(herdEntity);
        if (alpha == null) return false;

        // only flee while alpha is in a battle
        if (!alpha.isBattling()) return false;

        return findRandomPosition();
    }

    @Override
    public boolean canContinueToUse() {
        return alpha != null && alpha.isBattling() && !herdEntity.getNavigation().isDone();
    }

    @Override
    public void start() {
        herdEntity.getNavigation().moveTo(x, y, z, speed);
    }

    @Override
    public void stop() {
        alpha = null;
    }

    private boolean findRandomPosition() {
        Vec3 pos = DefaultRandomPos.getPos(herdEntity, 12, 6);
        if (pos == null) return false;

        x = pos.x;
        y = pos.y;
        z = pos.z;
        return true;
    }
}
