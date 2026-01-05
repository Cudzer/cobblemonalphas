package dev.cudzer.cobblemonalphas.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import dev.cudzer.cobblemonalphas.entity.behavior.HerdMemberFleeBehavior;
import dev.cudzer.cobblemonalphas.entity.behavior.HerdMemberRegroupBehavior;
import dev.cudzer.cobblemonalphas.entity.behavior.PokemonFollowAlphaBehavior;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin {

    @Inject(
            method = "makeBrain",
            at = @At("RETURN")
    )
    private void onBrainCreated(Dynamic<?> dynamic, CallbackInfoReturnable<Brain<?>> cir) {
        Brain<PokemonEntity> brain = (Brain<PokemonEntity>) cir.getReturnValue();

        brain.addActivity(
                Activity.IDLE,
                0,
                ImmutableList.of(
                        new PokemonFollowAlphaBehavior(),
                        new HerdMemberRegroupBehavior(),
                        new HerdMemberFleeBehavior(2.0)
                )
        );
    }
}
