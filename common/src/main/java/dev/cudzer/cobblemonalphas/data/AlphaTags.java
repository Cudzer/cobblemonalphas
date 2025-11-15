package dev.cudzer.cobblemonalphas.data;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.nbt.CompoundTag;

public class AlphaTags {
    public static final String IS_ALPHA = "IS_ALPHA";
    public static final String SUPER_ALPHA = "SUPER_ALPHA";

    public static boolean isAlpha(Pokemon pokemon) {
        CompoundTag tag = pokemon.getPersistentData();
        return tag.getBoolean(IS_ALPHA);
    }

    public static boolean isSuperAlpha(Pokemon pokemon) {
        CompoundTag tag = pokemon.getPersistentData();
        return tag.getBoolean(SUPER_ALPHA);
    }

    public static void markAlpha(Pokemon pokemon) {
        pokemon.getPersistentData().putBoolean(IS_ALPHA, true);
    }

    public static void markSuperAlpha(Pokemon pokemon) {
        markAlpha(pokemon);
        pokemon.getPersistentData().putBoolean(SUPER_ALPHA, true);
    }
}
