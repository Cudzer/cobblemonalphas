package dev.cudzer.cobblemonalphas.render;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public class TimedRoarEffect {
    public final PokemonEntity entity;
    public final long startTimeMs;
    public final float durationMs;

    public TimedRoarEffect(PokemonEntity entity, float durationMs) {
        this.entity = entity;
        this.startTimeMs = System.currentTimeMillis();
        this.durationMs = durationMs;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTimeMs >= durationMs;
    }
}
