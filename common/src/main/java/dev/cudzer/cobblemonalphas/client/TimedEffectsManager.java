package dev.cudzer.cobblemonalphas.client;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.cudzer.cobblemonalphas.render.AlphaRoarRender;
import dev.cudzer.cobblemonalphas.render.TimedRoarEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TimedEffectsManager {
    private static final AlphaRoarRender alphaRoarRender = new AlphaRoarRender();

    private static final List<TimedRoarEffect> ACTIVE_ROARS = new ArrayList<>();

    public static void addRoar(TimedRoarEffect effect) {
        ACTIVE_ROARS.add(effect);
    }

    public static void tickAndRender(PoseStack poseStack, MultiBufferSource buffer) {
        Iterator<TimedRoarEffect> it = ACTIVE_ROARS.iterator();
        while (it.hasNext()) {
            TimedRoarEffect effect = it.next();
            if (effect.isExpired() || !effect.entity.isAlive()) {
                it.remove();
                continue;
            }

            alphaRoarRender.render(
                    effect.entity,
                    poseStack,
                    (PokemonClientDelegate) effect.entity.getDelegate(),
                    buffer);
        }
    }
}
