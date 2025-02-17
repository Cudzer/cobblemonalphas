package dev.cudzer.cobblemonalphas.entity.spawner;

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
import com.cobblemon.mod.common.pokemon.Species;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.config.ModConfig;
import dev.cudzer.cobblemonalphas.data.AlphaJsonDataManager;
import dev.cudzer.cobblemonalphas.entity.Alpha;
import dev.cudzer.cobblemonalphas.entity.HerdMember;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.location.ISpawnLocation;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.location.RandomSurfaceSpawn;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety.BlockBlacklist;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety.HeightBounds;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety.ISpawnCondition;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety.SkyVisible;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

public class AlphaSpawner {
    private static final AlphaSpawner instance = new AlphaSpawner();
    public static AlphaSpawner getInstance() {return instance;}

    private int spawnCountdown;
    private ISpawnLocation spawnLocationSelector;
    private List<ISpawnCondition> spawnConditions;

    private static final int HERD_SIZE = 4;

    private MinecraftServer server;

    public void init(){
        spawnCountdown = ModConfig.ticksBetweenSpawns;

        spawnLocationSelector = new RandomSurfaceSpawn(ModConfig.minimumSpawnDistance, ModConfig.maximumSpawnDistance);
        spawnConditions = List.of(
                new BlockBlacklist(
                        List.of(
                                Blocks.LAVA,
                                Blocks.FIRE,
                                Blocks.CACTUS
                        )
                ),
                new HeightBounds(60, 200),
                new SkyVisible(true)
        );
        AlphaDespawner.getInstance().setMinimumDespawnDistance(ModConfig.minimumSpawnDistance);
        AlphaDespawner.getInstance().setSpawnIntervalTicks(ModConfig.ticksBetweenSpawns);

        AlphaJsonDataManager.populateBiomeData(server.getLevel(Level.OVERWORLD));
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public void tick(){
        if(spawnCountdown > 0) spawnCountdown--;
        else {
            attemptSpawn();
                spawnCountdown = ModConfig.ticksBetweenSpawns;
        }
    }

    private void attemptSpawn(){
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if(players.size() < ModConfig.requiredPlayerAmount) return;

        if(Math.random() > (ModConfig.alphaSpawnChance + (0.02f * (server.getPlayerCount() - ModConfig.requiredPlayerAmount)))) return;

        Alpha chosenAlpha = AlphaJsonDataManager.getRandomAlphaObj(server.overworld());

        int attemptedSpawns = 0;
        Level spawnLevel;
        Vec3i spawnPos;
        ServerPlayer chosenPlayer;

        while(true){
            if(++attemptedSpawns > ModConfig.spawnAttempts){
                CobblemonAlphasMod.LOGGER.info("Maximum spawn attempts reached. Skipping this alpha spawn");
                return;
            }

            Optional<ServerPlayer> chosenPlayerOpt = players.stream()
                    .filter(player -> player.level().dimension() == Level.OVERWORLD)
                    .skip((int) (players.size() * Math.random()))
                    .findFirst();
            if(chosenPlayerOpt.isPresent()){
                chosenPlayer = chosenPlayerOpt.get();

                final Level chosenPlayerSpawnLevel = chosenPlayer.level();
                final Vec3i spawnLocation = spawnLocationSelector.getSpawnLocation(chosenPlayer.level(), chosenPlayer.position());

                if(spawnLocation == null) continue;
                BlockPos finalSpawnPos = new BlockPos(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());

                if(spawnConditions.stream().anyMatch(condition -> !condition.isSafe(chosenPlayerSpawnLevel, finalSpawnPos))) continue;

                var biomeKey = chosenPlayerSpawnLevel.getBiome(finalSpawnPos).unwrapKey();
                if(biomeKey.isPresent()){
                    chosenAlpha = AlphaJsonDataManager.getRandomAlphaForBiome(chosenPlayer.level(), biomeKey.get()).values().stream().toList().getFirst();
                }

                spawnPos = spawnLocation;
                spawnLevel = chosenPlayerSpawnLevel;
                break;
            }
        }

        PokemonEntity alphaEntity = generateAlpha(chosenAlpha, spawnLevel, spawnPos);

        spawnLevel.addFreshEntity(alphaEntity);

        if(ModConfig.doHerdSpawning){
            spawnHerdPokemon(alphaEntity, chosenAlpha.getHerdMembers(), spawnLevel, spawnPos);
        }

        server.getPlayerList().broadcastSystemMessage(
                Component.literal(ModConfig.spawnAnnouncementMessage), false
        );
    }

    private PokemonEntity generateAlpha(Alpha chosenAlpha, Level spawnLevel, Vec3i spawnPos){
        Pokemon pokemon = new Pokemon();
        pokemon.setSpecies(Objects.requireNonNull(PokemonSpecies.INSTANCE.getByName(chosenAlpha.getSpecies())));
        pokemon.setLevel(chosenAlpha.getLevel());
        pokemon.initializeMoveset(true);
        pokemon.setScaleModifier((float)ModConfig.alphaSizeMultiplier);

        pokemon.getPersistentData().putBoolean("IS_ALPHA", true);

        IVs ivs = pokemon.getIvs();
        pokemon.setIvs$common(maximizeRandomIVs(ivs));

        pokemon.setAbility$common(doHiddenAbilityCheck(pokemon));

        if(Math.random() < (1d / ModConfig.shinyOdds)) pokemon.setShiny(true);

        PokemonEntity alphaEntity = new PokemonEntity(spawnLevel, pokemon, CobblemonEntities.POKEMON);
        alphaEntity.setDespawner(AlphaDespawner.getInstance());

        alphaEntity.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        spawnLevel.getChunkAt(new BlockPos(spawnPos));
        return alphaEntity;

    }

    private void spawnHerdPokemon(PokemonEntity alphaEntity, List<HerdMember> herdMembers, Level spawnLevel, Vec3i spawnPos){
        String alphaString = alphaEntity.getPokemon().getSpecies().getName();
        if(herdMembers.size() <= 0){
            CobblemonAlphasMod.LOGGER.warn(String.format("Herd members for %s is empty. Skipping herd spawning for this alpha", alphaString));
            return;
        }

        for (int i = 1; i <= HERD_SIZE; i++) {
            int random = new Random().nextInt(herdMembers.size());
            HerdMember herdMember = herdMembers.get(random);
            Pokemon herdMemberPokemon = new Pokemon();
            Species herdMemberSpecies = PokemonSpecies.INSTANCE.getByName(herdMember.getSpecies());
            if(herdMemberSpecies == null){
                CobblemonAlphasMod.LOGGER.warn(String.format("Incorrect species defined for herd member of %s. %s is not a valid pokemon species", alphaString, herdMember.getSpecies()));
                return;
            }

            herdMemberPokemon.setSpecies(herdMemberSpecies);
            herdMemberPokemon.setLevel(herdMember.getLevel());
            herdMemberPokemon.initializeMoveset(true);

            herdMemberPokemon.getPersistentData().putUUID("ALPHA_ID", alphaEntity.getUUID());

            if(Math.random() < (1d / ModConfig.shinyOdds)) herdMemberPokemon.setShiny(true);

            PokemonEntity herdEntity = new PokemonEntity(spawnLevel, herdMemberPokemon, CobblemonEntities.POKEMON);
            herdEntity.setDespawner(AlphaDespawner.getInstance());

            herdEntity.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
            spawnLevel.getChunkAt(new BlockPos(spawnPos));
            spawnLevel.addFreshEntity(herdEntity);
        }
    }

    private Ability doHiddenAbilityCheck(Pokemon pokemon){
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

    public static IVs maximizeRandomIVs(IVs ivs){
        List<String> chosenStats = new ArrayList<>(List.of(statNames));
        for (int i = 1; i <= ModConfig.maximumBestIVs; i++){
            int ivIndex = new Random().nextInt(chosenStats.size());
            String statName = chosenStats.get(ivIndex);
            Stat stat = Stats.valueOf(statName);

            ivs.set(stat, 31);
            chosenStats.remove(statName);
        }
        return ivs;
    }
}
