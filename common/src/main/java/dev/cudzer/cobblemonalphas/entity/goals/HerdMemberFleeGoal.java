package dev.cudzer.cobblemonalphas.entity.goals;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class HerdMemberFleeGoal extends Goal {

    private final PokemonEntity herdEntity;

    @Nullable
    private PokemonEntity alphaEntity;

    protected final double speedModifier;
    protected double posX;
    protected double posY;
    protected double posZ;

    protected boolean isRunning;

    public HerdMemberFleeGoal(PokemonEntity herdEntity, double speedModifier){
        this.herdEntity = herdEntity;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse() {
        if (!this.shouldFlee()) {
            return false;
        } else {
            return this.findRandomPosition();
        }

    }

    private boolean shouldFlee(){
        if(!this.herdEntity.getPokemon().isWild()) return false;
        if(!herdEntity.getPokemon().getPersistentData().contains("ALPHA_ID")) return false;
        if(herdEntity.getPokemon().getPersistentData().getUUID("ALPHA_ID").toString().isEmpty()) return false;

        alphaEntity = findAlphaEntity();
        if(alphaEntity == null) return false;

        return alphaEntity.isBattling();
    }

    public boolean isRunning() {
        return this.isRunning;
    }


    public void start() {
        this.herdEntity.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
        this.isRunning = true;
    }

    public void stop() {
        this.isRunning = false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.herdEntity.getNavigation().isDone();
    }

    protected boolean findRandomPosition() {
        Vec3 vec3 = DefaultRandomPos.getPos(this.herdEntity, 10, 5);
        if (vec3 == null) {
            return false;
        } else {
            this.posX = vec3.x;
            this.posY = vec3.y;
            this.posZ = vec3.z;
            return true;
        }
    }

    @Override
    public void tick(){

    }

    private PokemonEntity findAlphaEntity(){
        List<? extends PokemonEntity> list = this.herdEntity.level()
                .getEntitiesOfClass(this.herdEntity.getClass(), this.herdEntity.getBoundingBox().inflate(8.0, 4.0, 8.0));
        Iterator<? extends PokemonEntity> iterator = list.iterator();

        while(iterator.hasNext()){
            PokemonEntity entityToCheck = (PokemonEntity) iterator.next();
            if(herdEntity.getPokemon().getPersistentData().getUUID("ALPHA_ID").equals(entityToCheck.getUUID())){
                return (PokemonEntity) entityToCheck;
            }
        }
        return null;
    }
}
