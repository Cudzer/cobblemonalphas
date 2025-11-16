package dev.cudzer.cobblemonalphas.util;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import java.util.UUID;

public class HerdUtils {
    public static PokemonEntity findAlpha(PokemonEntity herd) {
        if (!herd.getPokemon().getPersistentData().contains("ALPHA_ID"))
            return null;

        UUID alphaId = herd.getPokemon().getPersistentData().getUUID("ALPHA_ID");

        return herd.level().getEntitiesOfClass(
                        PokemonEntity.class,
                        herd.getBoundingBox().inflate(24, 8, 24)
                ).stream()
                .filter(e -> e.getUUID().equals(alphaId))
                .findFirst()
                .orElse(null);
    }
}
