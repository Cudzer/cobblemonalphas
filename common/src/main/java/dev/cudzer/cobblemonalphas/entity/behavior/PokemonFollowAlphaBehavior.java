package dev.cudzer.cobblemonalphas.entity.behavior;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.cudzer.cobblemonalphas.util.HerdUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.Map;

public class PokemonFollowAlphaBehavior extends Behavior<PokemonEntity> {
    private PokemonEntity alphaEntity;

    private int recalcTimer = 0;

    public PokemonFollowAlphaBehavior() {
        super(Map.of());
    }

    @Override
    protected void start(ServerLevel level, PokemonEntity entity, long gameTime) {
        recalcTimer = 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PokemonEntity entity, long gameTime) {
        return alphaEntity != null && !alphaEntity.isBattling();
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

        double dist = entity.distanceToSqr(alphaEntity);
        return dist > 9 && dist < 256;
    }

    @Override
    public void tick(ServerLevel level, PokemonEntity entity, long gameTime) {
        if (--recalcTimer <= 0) {
            recalcTimer = 10;
            entity.getNavigation().moveTo(alphaEntity, 1.1);
        }
    }
}
