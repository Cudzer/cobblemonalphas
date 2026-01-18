package dev.cudzer.cobblemonalphas.config;

import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;

import java.nio.file.Path;

public class ConfigKey {
    public static final Path configPath = Path.of(CobblemonAlphasMod.MOD_ID + "/config.json");

    // Spawning behavior
    public static String DO_ALPHA_SPAWNING = "doAlphaSpawning";
    public static String ALPHA_SPAWN_CHANCE = "alphaSpawnChance";
    public static String SECONDS_BETWEEN_SPAWNS = "secondsBetweenSpawns";
    public static String SPAWN_ATTEMPTS = "spawnAttempts";
    public static String REQUIRED_PLAYER_AMOUNT = "requiredPlayerAmount";
    public static String DO_HERD_SPAWNING = "doHerdSpawning";
    public static String MINIMUM_SPAWN_DISTANCE = "minimumSpawnDistance";
    public static String MAXIMUM_SPAWN_DISTANCE = "maximumSpawnDistance";

    // Alpha properties
    public static String MAXIMUM_BEST_IVS = "maximumBestIvs";
    public static String ALPHA_SIZE_MULTIPLIER = "alphaSizeMultiplier";
    public static String SHINY_ODDS = "shinyOdds";
    
    // Messages
    public static String DO_SPAWN_ANNOUNCEMENT_MESSAGE = "doSpawnAnnouncementMessage";
    public static String SPAWN_ANNOUNCEMENT_MESSAGE = "spawnAnnouncementMessage";
    public static String SHOW_COORDS_IN_ANNOUNCEMENT = "showCoordinatesInAnnouncement";
}
