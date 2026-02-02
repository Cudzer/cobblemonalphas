package dev.cudzer.cobblemonalphas.entity.spawner;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.config.ModConfig;
import dev.cudzer.cobblemonalphas.data.AlphaJsonDataManager;
import dev.cudzer.cobblemonalphas.entity.Alpha;
import dev.cudzer.cobblemonalphas.entity.AlphaGenerator;
import dev.cudzer.cobblemonalphas.entity.HerdMember;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.location.ISpawnLocation;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.location.RandomSpawnAroundPlayer;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety.BlockBlacklist;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety.HeightBounds;
import dev.cudzer.cobblemonalphas.entity.spawner.spawnData.safety.ISpawnCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

public class AlphaSpawner {
    private static final AlphaSpawner instance = new AlphaSpawner();

    public static AlphaSpawner getInstance() {
        return instance;
    }

    private static final Random RNG = new Random();
    private int spawnCountdown;
    private ISpawnLocation spawnLocationSelector;
    private List<ISpawnCondition> spawnConditions;

    private static final int HERD_SIZE = 4;

    private MinecraftServer server;

    public void init() {
        spawnCountdown = ModConfig.ticksBetweenSpawns;
        spawnLocationSelector = new RandomSpawnAroundPlayer(
                ModConfig.minimumSpawnDistance,
                ModConfig.maximumSpawnDistance);
        spawnConditions = List.of(
                new BlockBlacklist(List.of(Blocks.LAVA, Blocks.FIRE, Blocks.CACTUS)),
                new HeightBounds(-50, 200));
        AlphaDespawner.getInstance().setMinimumDespawnDistance(ModConfig.minimumSpawnDistance);
        AlphaDespawner.getInstance().setSpawnIntervalTicks(ModConfig.ticksBetweenSpawns);
        AlphaJsonDataManager.populateBiomeData(server.getLevel(Level.OVERWORLD));
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public void tick() {
        if (spawnCountdown > 0)
            spawnCountdown--;
        else {
            attemptSpawn();
            spawnCountdown = ModConfig.ticksBetweenSpawns;
        }
    }

    private void attemptSpawn() {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty() || players.size() < ModConfig.requiredPlayerAmount)
            return;

        // Do spawn attempts
        for (int attempt = 1; attempt <= ModConfig.spawnAttempts; attempt++) {

            if (ModConfig.spawnBehavior.equals("player")) {
                for (ServerPlayer player : players) {
                    boolean spawnSucceeded = doSpawnAttempt(player);
                    if (spawnSucceeded)
                        return;
                }
            } else {
                // Pick a random player
                ServerPlayer chosenPlayer = players.get(RNG.nextInt(players.size()));

                boolean spawnSucceeded = doSpawnAttempt(chosenPlayer);
                if (spawnSucceeded)
                    return;
            }
        }

        CobblemonAlphasMod.LOGGER.info("Maximum spawn attempts reached. Skipping this alpha spawn");
    }

    public boolean doSpawnAttempt(ServerPlayer player) {
        double totalChance = ModConfig.alphaSpawnChance
                + ModConfig.perPlayerSpawnChanceBoost
                        * (server.getPlayerCount() - ModConfig.requiredPlayerAmount);

        // * Fail due to an RNG miss
        if (RNG.nextDouble() > totalChance)
            return false;

        final Level level = player.level();
        final Vec3i location = spawnLocationSelector.getSpawnLocation(player.level(), player.position());

        // * Fail attempt if the position isn't valid
        if (location == null)
            return false;

        // Get the exact block the spawn will happen on
        BlockPos finalSpawnPos = new BlockPos(location.getX(), location.getY(), location.getZ());

        // * Fail attempt if the block isn't safe
        if (spawnConditions.stream().anyMatch(condition -> !condition.isSafe(level, finalSpawnPos)))
            return false;

        // Get the biome key the player is in
        var biomeKey = level.getBiome(finalSpawnPos).unwrapKey();

        // * Fail if the biomekey is invalid
        if (biomeKey.isEmpty())
            return false;

        // Check if the spawn is happening underground
        boolean underground = !level.canSeeSky(finalSpawnPos);

        // Fetch all alphas
        Map<ResourceLocation, Alpha> alphaMap = AlphaJsonDataManager.getRandomAlphaForBiome(
                level,
                biomeKey.get(),
                underground);

        // * Fail if we didn't load any alphas
        if (alphaMap == null || alphaMap.isEmpty())
            return false;

        // * All checks have passed - proceeding with spawn

        // Pick an alpha and spawn it in the world
        Alpha chosenAlpha = alphaMap.values().iterator().next();
        spawnAlphaEntity(chosenAlpha, level, location, ModConfig.doHerdSpawning);

        // Announce the spawn if enabled
        if (ModConfig.doSpawnAnnouncementMessage)
            announceSpawn(location);

        return true;
    }

    public void announceSpawn(Vec3i location) {
        String announcement = ModConfig.spawnAnnouncementMessage;
        if (ModConfig.showCoordinatesInAnnouncement) {
            announcement += String.format(
                    " (%d, %d, %d)",
                    location.getX(),
                    location.getY(),
                    location.getZ());
        }

        server.getPlayerList().broadcastSystemMessage(Component.literal(announcement), false);

        return;
    }

    public void spawnAlphaEntity(Alpha alpha, Level level, Vec3i spawnPosition, boolean doHerdSpawning) {
        if (!level.isClientSide()) {
            PokemonEntity alphaEntity = AlphaGenerator.generate(alpha, level, spawnPosition);

            if (alphaEntity != null) {
                level.getChunkAt(new BlockPos(spawnPosition));
                level.addFreshEntity(alphaEntity);

                if (doHerdSpawning) {
                    spawnHerdPokemon(alphaEntity, alpha.getHerdMembers(), level, spawnPosition);
                }
            } else {
                CobblemonAlphasMod.LOGGER.error("An alpha spawn was attempted but the entity creation failed...");
            }
        }
    }

    private void spawnHerdPokemon(PokemonEntity alphaEntity, List<HerdMember> herdMembers, Level spawnLevel,
            Vec3i spawnPos) {
        String alphaString = alphaEntity.getPokemon().getSpecies().getName();
        if (herdMembers.size() <= 0) {
            CobblemonAlphasMod.LOGGER.warn(
                    String.format("Herd members for %s is empty. Skipping herd spawning for this alpha", alphaString));
            return;
        }

        for (int i = 1; i <= HERD_SIZE; i++) {
            HerdMember herdMember = herdMembers.get(RNG.nextInt(herdMembers.size()));
            Pokemon herdMemberPokemon = new Pokemon();
            Species herdMemberSpecies = PokemonSpecies.getByName(herdMember.getSpecies());
            if (herdMemberSpecies == null) {
                CobblemonAlphasMod.LOGGER.warn(String.format(
                        "Incorrect species defined for herd member of %s. %s is not a valid pokemon species",
                        alphaString, herdMember.getSpecies()));
                return;
            }

            herdMemberPokemon.setSpecies(herdMemberSpecies);
            herdMemberPokemon.setLevel(herdMember.getLevelFromRange());
            herdMemberPokemon.initializeMoveset(true);

            herdMemberPokemon.getPersistentData().putUUID("ALPHA_ID", alphaEntity.getUUID());

            if (RNG.nextDouble() < (1d / ModConfig.shinyOdds))
                herdMemberPokemon.setShiny(true);

            PokemonEntity herdEntity = new PokemonEntity(spawnLevel, herdMemberPokemon, CobblemonEntities.POKEMON);
            herdEntity.setDespawner(AlphaDespawner.getInstance());

            herdEntity.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
            spawnLevel.getChunkAt(new BlockPos(spawnPos));
            spawnLevel.addFreshEntity(herdEntity);
        }
    }
}
