package dev.cudzer.cobblemonalphas.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext;
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.cudzer.cobblemonalphas.render.AlphaEyesRender;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Mixin(value = PokemonRenderer.class)
public class PokemonRendererMixin {
    @Unique
    private final RenderContext alpha_project$context = new RenderContext();

    @Unique
    private final AlphaEyesRender alpha_project$render = new AlphaEyesRender();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void init(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.alpha_project$context.put(RenderContext.Companion.getRENDER_STATE(), RenderContext.RenderState.WORLD);
        this.alpha_project$context.put(RenderContext.Companion.getDO_QUIRKS(), true);
    }

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
        if (!pokemon.getPersistentData().getBoolean("IS_ALPHA"))
            return;

        alpha_project$render.render(entity, poseStack, clientDelegate, buffer);
    }
}
