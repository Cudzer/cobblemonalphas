package dev.cudzer.cobblemonalphas.entity.behavior;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.cudzer.cobblemonalphas.util.HerdUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.Map;

public class HerdMemberRegroupBehavior extends Behavior<PokemonEntity> {
    private PokemonEntity alphaEntity;

    private int pathCooldown = 0;

    public HerdMemberRegroupBehavior() {
        super(Map.of());
    }

    @Override
    protected void start(ServerLevel level, PokemonEntity entity, long gameTime) {
        pathCooldown = 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PokemonEntity entity, long gameTime) {
        if (alphaEntity == null) return false;
        if (!alphaEntity.isAlive()) return false;

        if (alphaEntity.isBattling()) return false;

        double distSq = entity.distanceToSqr(alphaEntity);
        return distSq > 16;
    }

    @Override
    protected void stop(ServerLevel level, PokemonEntity entity, long gameTime) {
        alphaEntity = null;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PokemonEntity entity) {
        alphaEntity = HerdUtils.findAlpha(entity);
        if (alphaEntity == null) return false;

        if (alphaEntity.isBattling()) return false;

        double distSq = entity.distanceToSqr(alphaEntity);
        if (distSq < 20) return false;

        return true;
    }

    @Override
    public void tick(ServerLevel level, PokemonEntity entity, long gameTime) {
        if (--pathCooldown <= 0) {
            pathCooldown = 10;

            entity.getNavigation().moveTo(alphaEntity, 1.2);
        }
    }
}
