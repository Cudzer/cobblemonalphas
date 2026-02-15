package dev.cudzer.cobblemonalphas.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.cudzer.cobblemonalphas.render.AlphaEyesRender;
import net.minecraft.client.renderer.MultiBufferSource;

@Mixin(value = PokemonRenderer.class)
public class PokemonRendererMixin {
    @Inject(method = "render*", at = @At(value = "TAIL"))
    public void render(
            PokemonEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci) {
        // Fetch the delegate and the pokemons entity instance
        PokemonClientDelegate clientDelegate = (PokemonClientDelegate) entity.getDelegate();
        Pokemon pokemon = entity.getPokemon();

        // Return early on non-alphas because we have nothing to do with them
        if (!pokemon.getAspects().contains("alpha"))
            return;

        AlphaEyesRender.INSTANCE.render(entity, entityYaw, partialTicks, poseStack, clientDelegate, buffer);
    }
}
