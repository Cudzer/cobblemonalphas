package dev.cudzer.cobblemonalphas.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.cudzer.cobblemonalphas.entity.goals.PokemonFollowAlphaGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PokemonEntity.class)
public abstract class PokemonEntityMixin extends ShoulderRidingEntity {

    protected PokemonEntityMixin(EntityType<? extends ShoulderRidingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at= @At("TAIL"))
    public void registerGoalsCA(CallbackInfo callbackInfo){
        this.goalSelector.addGoal(3, new PokemonFollowAlphaGoal((PokemonEntity)(Object)this));
    }
}
