package dev.cudzer.cobblemonalphas.render.layerEntities;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class LayerEntity {
    protected double animSeconds = 0.0;
    protected long lastTimeNs = -1L;
    public float ticks;
    public final PosableState state;

    public LayerEntity(PosableState state) {
        this.state = state;
    }

    public void render(RenderContext context, PokemonClientDelegate clientDelegate, PokemonEntity entity,
            Pokemon pokemon, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        updateAnimTime();
    }

    public void render(String aspect, RenderContext context, PokemonClientDelegate clientDelegate, PokemonEntity entity,
            Pokemon pokemon, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight) {
        updateAnimTime();
    }

    protected void updateAnimTime() {
        if (!Minecraft.getInstance().isPaused()) {
            long now = System.nanoTime();

            if (lastTimeNs != -1L) {
                double deltaSeconds = (now - lastTimeNs) / 1_000_000_000.0;
                animSeconds += deltaSeconds;
            }

            lastTimeNs = now;
        } else {
            lastTimeNs = System.nanoTime();
        }

        float ticks = (float) (animSeconds * 20f);

        int age = (int) ticks;
        float pt = ticks - age;

        state.updateAge(age);
        state.updatePartialTicks(pt);
    }
}