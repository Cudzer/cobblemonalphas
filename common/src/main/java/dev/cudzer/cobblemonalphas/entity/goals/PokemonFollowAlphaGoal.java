package dev.cudzer.cobblemonalphas.entity.goals;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class PokemonFollowAlphaGoal extends Goal {
    private final PokemonEntity herdEntity;
    @Nullable private PokemonEntity alphaEntity;

    private int timeToRecalcPath;

    public PokemonFollowAlphaGoal(PokemonEntity herdEntity){
        this.herdEntity = herdEntity;
    }

    @Override
    public boolean canUse(){
        if(!this.herdEntity.getPokemon().isWild()) return false;
        if(!herdEntity.getPokemon().getPersistentData().contains("ALPHA_ID")) return false;
        if(herdEntity.getPokemon().getPersistentData().getUUID("ALPHA_ID").toString().isEmpty()) return false;

        List<? extends PokemonEntity> list = this.herdEntity.level()
                .getEntitiesOfClass(this.herdEntity.getClass(), this.herdEntity.getBoundingBox().inflate(8.0, 4.0, 8.0));
        PokemonEntity alpha = null;
        double currentMinDist = Double.MAX_VALUE;
        Iterator<? extends PokemonEntity> iterator = list.iterator();

        while(iterator.hasNext()){
            PokemonEntity entityToCheck = (PokemonEntity) iterator.next();
            if(herdEntity.getPokemon().getPersistentData().getUUID("ALPHA_ID").equals(entityToCheck.getUUID())){
                double distanceToAlpha = this.herdEntity.distanceToSqr(entityToCheck);
                if(!(distanceToAlpha > currentMinDist)){
                    currentMinDist = distanceToAlpha;
                    alpha = entityToCheck;
                }
            }
        }
        if(alpha == null){
            return false;
        } else if (currentMinDist < 9.0) {
            return false;
        }else {
            this.alphaEntity = alpha;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse(){
        if(this.alphaEntity == null) return false;
        if(this.alphaEntity.isBattling()) return false;
        else if( !this.alphaEntity.isAlive()){
            return false;
        }
        else {
            double distanceToAlpha = this.herdEntity.distanceToSqr(this.alphaEntity);
            return !(distanceToAlpha < 9.0) && !(distanceToAlpha > 256.0);
        }
    }

    @Override
    public void start(){
        this.timeToRecalcPath = 0;
    }

    public void stop() {
        this.alphaEntity = null;
    }

    @Override
    public void tick(){
        if(--this.timeToRecalcPath <= 0){
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            herdEntity.getNavigation().moveTo(this.alphaEntity, 1.0d);
        }
    }
}
