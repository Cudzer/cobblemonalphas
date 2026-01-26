package dev.cudzer.cobblemonalphas.render.layerEntities;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.client.render.ModelLayer;
import com.cobblemon.mod.common.client.render.ModelTextureSupplier;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import dev.cudzer.cobblemonalphas.render.layerEntities.states.AlphaEyesState;
import kotlin.Unit;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class AlphaEyesLayer extends LayerEntity {
    private final Set<String> aspects = new HashSet<>();

    private static RenderType renderType(ResourceLocation texture) {
        return RenderType.create(
                "alpha_eye_glow",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_EYES_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false));
    }

    public AlphaEyesLayer() {
        super(new AlphaEyesState());
    }

    @Override
    public void render(RenderContext context, PokemonClientDelegate clientDelegate, PokemonEntity entity,
            Pokemon pokemon, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight) {

        super.render(context, clientDelegate, entity, pokemon, entityYaw, partialTicks, poseStack, buffer, packedLight);
        state.setCurrentAspects(aspects);

        // Fetch the poserId
        ResourceLocation poserId = pokemon.getSpecies().getResourceIdentifier();

        // Fetch the location of the root
        Map<String, MatrixWrapper> locatorStates = clientDelegate.getLocatorStates();
        MatrixWrapper rootLocator = locatorStates.get("root");
        if (rootLocator == null)
            return;

        // Get model and texture of the mon
        PosableModel model = VaryingModelRepository.INSTANCE.getPoser(poserId, clientDelegate);

        // Fetch the alpha eyes layer
        Iterable<ModelLayer> layers = VaryingModelRepository.INSTANCE.getLayers(poserId, clientDelegate);
        List<ModelTextureSupplier> alphaEyesLayerResult = StreamSupport
                .stream(layers.spliterator(), false)
                .filter(tl -> "alpha_eyes".equals(tl.getName()))
                .map(tl -> tl.getTexture())
                .toList();

        if (alphaEyesLayerResult.isEmpty())
            return;

        ResourceLocation eyesLayer = alphaEyesLayerResult.getFirst().invoke(state);

        model.context = context;
        model.setBufferProvider(buffer);
        state.setCurrentModel(model);

        // Setup context
        context.put(RenderContext.Companion.getASPECTS(), aspects);
        context.put(RenderContext.Companion.getTEXTURE(), eyesLayer);
        context.put(RenderContext.Companion.getSPECIES(), poserId);
        context.put(RenderContext.Companion.getPOSABLE_STATE(), state);

        float scale = pokemon.getSpecies().getBaseScale() * pokemon.getScaleModifier();

        // Render multiple bloom layers for stronger effect
        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();

            poseStack.mulPose(rootLocator.getMatrix());
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));

            // 1.0, 1.02, 1.04
            float bloomScale = scale + (i * 0.02f);
            poseStack.scale(bloomScale, scale, bloomScale);

            // Apply animations
            model.applyAnimations(entity, state, 0F, 0F, ticks, 0F, 0F);

            // Use custom glow render type with additive blending
            VertexConsumer vertexConsumer = buffer.getBuffer(renderType(eyesLayer));

            // Red with decreasing alpha for each layer
            int alpha = Math.max(20, 80 - (i * 25)); // 80, 55, 30
            int glowColor = (alpha << 24) | 0x80FF0000;

            model.render(context, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, glowColor);

            model.withLayerContext(buffer, state, layers, () -> {
                model.render(context, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, glowColor);
                return Unit.INSTANCE;
            });

            model.setDefault();
            poseStack.popPose();
        }
    }
}
