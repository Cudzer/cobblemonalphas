package dev.cudzer.cobblemonalphas.config;

import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;

import java.nio.file.Path;

public class ConfigKey {
    public static final Path configPath = Path.of(CobblemonAlphasMod.MOD_ID + "/config.json");

    // Spawning properties
    public static String DO_ALPHA_SPAWNING = "doAlphaSpawning";
    public static String DO_HERD_SPAWNING = "doHerdSpawning";
    
    // Spawn rate
    // Can be either 'world' or 'player'
    public static String SPAWN_BEHAVIOR = "spawnBehavior";
    public static String ALPHA_SPAWN_CHANCE = "alphaSpawnChance";
    public static String SPAWN_ATTEMPTS = "spawnAttempts";
    public static String SECONDS_BETWEEN_SPAWNS = "secondsBetweenSpawns";
    public static String PER_PLAYER_SPAWN_CHANCE_BOOST = "perPlayerSpawnChanceBoost";
    
    // Spawning conditions
    public static String REQUIRED_PLAYER_AMOUNT = "requiredPlayerAmount";
    public static String MINIMUM_SPAWN_DISTANCE = "minimumSpawnDistance";
    public static String MAXIMUM_SPAWN_DISTANCE = "maximumSpawnDistance";
    
    // Alpha properties
    public static String RETAIN_ALPHA_STATUS = "retainAlphaStatus";
    public static String MAXIMUM_BEST_IVS = "maximumBestIvs";
    public static String ALPHA_SIZE_MULTIPLIER = "alphaSizeMultiplier";
    public static String SHINY_ODDS = "shinyOdds";
    
    // Announcements
    public static String DO_SPAWN_ANNOUNCEMENT_MESSAGE = "doSpawnAnnouncementMessage";
    public static String SPAWN_ANNOUNCEMENT_MESSAGE = "spawnAnnouncementMessage";
    public static String SHOW_COORDS_IN_ANNOUNCEMENT = "showCoordinatesInAnnouncement";
}
