package dev.cudzer.cobblemonalphas.entity;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityPool;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.config.ModConfig;
import dev.cudzer.cobblemonalphas.entity.spawner.AlphaDespawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AlphaGenerator {
    private static final Random RNG = new Random();

    public static PokemonEntity generate(Alpha chosenAlpha, Level spawnLevel, Vec3i spawnPos){
        Pokemon pokemon = new Pokemon();
        pokemon.setSpecies(Objects.requireNonNull(PokemonSpecies.INSTANCE.getByName(chosenAlpha.getSpecies())));
        pokemon.setLevel(chosenAlpha.getLevelFromRange());
        pokemon.initializeMoveset(true);

        float baseSize = (float) ModConfig.alphaSizeMultiplier;
        pokemon = pokemon.clone(true, RegistryAccess.EMPTY);

        pokemon.setScaleModifier((float)ModConfig.alphaSizeMultiplier);
        pokemon.getAspects().add("alpha");

        pokemon.getPersistentData().putBoolean("IS_ALPHA", true);
        pokemon.setScaleModifier(baseSize);
        IVs ivs = pokemon.getIvs();
        pokemon.setIvs$common(maximizeRandomIVs(ivs, ModConfig.maximumBestIVs));
        pokemon.setAbility$common(doHiddenAbilityCheck(pokemon));

        if (RNG.nextDouble() < (1d / ModConfig.shinyOdds)) {
            pokemon.setShiny(true);
        }

        PokemonEntity alphaEntity = CobblemonEntities.POKEMON.create(spawnLevel);
        if(alphaEntity != null){
            alphaEntity.setPokemon(pokemon);
            alphaEntity.setDespawner(AlphaDespawner.getInstance());

            alphaEntity.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
            spawnLevel.getChunkAt(new BlockPos(spawnPos));

            return alphaEntity;
        }
        return null;
    }

    private static Ability doHiddenAbilityCheck(Pokemon pokemon){
        AbilityPool abilities = pokemon.getSpecies().getAbilities();

        List<AbilityTemplate> hiddenAbilities = new ArrayList<>();
        for(PotentialAbility potentialAbility : abilities) {
            if(potentialAbility.getPriority() == Priority.LOW) {
                hiddenAbilities.add(potentialAbility.getTemplate());
            }
        }
        if(!hiddenAbilities.isEmpty()){
            if(Math.random() >= 0.6){
                int selection = new Random().nextInt(hiddenAbilities.size());
                return new Ability(hiddenAbilities.get(selection), false, Priority.NORMAL);
            }
        }
        return pokemon.getAbility();
    }

    private static final String[] statNames = new String[]{
            "HP","ATTACK","DEFENCE","SPECIAL_ATTACK","SPECIAL_DEFENCE","SPEED"
    };

    public static IVs maximizeRandomIVs(IVs ivs, int numIvs){
        List<String> chosenStats = new ArrayList<>(List.of(statNames));
        for (int i = 1; i <= numIvs; i++){
            int ivIndex = new Random().nextInt(chosenStats.size());
            String statName = chosenStats.get(ivIndex);
            Stat stat = Stats.valueOf(statName);

            ivs.set(stat, 31);
            chosenStats.remove(statName);
        }
        return ivs;
    }
}
