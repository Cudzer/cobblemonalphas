package dev.cudzer.cobblemonalphas.entity.behavior;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.util.HerdUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class HerdMemberFleeBehavior extends Behavior<PokemonEntity> {

    private final double speed;

    private double x, y, z;
    private int pathCooldown = 0;
    @Nullable private PokemonEntity alpha;

    public HerdMemberFleeBehavior(double speedModifier){
        super(Map.of());
        this.speed = speedModifier;
    }

    @Override
    protected void start(ServerLevel level, PokemonEntity entity, long gameTime) {
        pathCooldown = 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PokemonEntity entity, long gameTime) {
        return alpha != null && alpha.isBattling() && !entity.getNavigation().isDone();
    }

    @Override
    protected void stop(ServerLevel level, PokemonEntity entity, long gameTime) {
        alpha = null;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PokemonEntity entity) {
        alpha = HerdUtils.findAlpha(entity);
        if (alpha == null) return false;

        // only flee while alpha is in a battle
        if (!alpha.isBattling()) return false;

        return findRandomPosition(entity);
    }

    private boolean findRandomPosition(PokemonEntity entity) {
        Vec3 pos = DefaultRandomPos.getPos(entity, 12, 6);
        if (pos == null) return false;

        x = pos.x;
        y = pos.y;
        z = pos.z;
        return true;
    }

    @Override
    public void tick(ServerLevel level, PokemonEntity entity, long gameTime) {
        if (--pathCooldown <= 0) {
            pathCooldown = 20;
            entity.getNavigation().moveTo(x, y, z, speed);
        }
    }
}
