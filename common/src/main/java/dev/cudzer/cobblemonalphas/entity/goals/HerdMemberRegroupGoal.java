package dev.cudzer.cobblemonalphas.entity.goals;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.cudzer.cobblemonalphas.util.HerdUtils;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class HerdMemberRegroupGoal extends Goal {
    private final PokemonEntity herdEntity;
    private PokemonEntity alphaEntity;

    private int pathCooldown = 0;

    public HerdMemberRegroupGoal(PokemonEntity herdMember) {
        this.herdEntity = herdMember;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        alphaEntity = HerdUtils.findAlpha(herdEntity);
        if (alphaEntity == null) return false;

        if (alphaEntity.isBattling()) return false;

        double distSq = herdEntity.distanceToSqr(alphaEntity);
        if (distSq < 20) return false;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (alphaEntity == null) return false;
        if (!alphaEntity.isAlive()) return false;

        if (alphaEntity.isBattling()) return false;

        double distSq = herdEntity.distanceToSqr(alphaEntity);
        return distSq > 16;
    }

    @Override
    public void start() {
        pathCooldown = 0;
    }

    @Override
    public void tick() {
        if (--pathCooldown <= 0) {
            pathCooldown = 10;

            herdEntity.getNavigation().moveTo(alphaEntity, 1.2);
        }
    }

    @Override
    public void stop() {
        alphaEntity = null;
    }

}
