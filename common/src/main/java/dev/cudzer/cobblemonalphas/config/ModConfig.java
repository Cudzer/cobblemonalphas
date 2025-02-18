package dev.cudzer.cobblemonalphas.config;

import com.google.gson.*;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModConfig {

    private static Path fullConfigPath;

    //config fields
    public static boolean doAlphaSpawning;
    public static float alphaSpawnChance;
    public static double alphaSizeMultiplier;
    public static int maximumBestIVs;
    public static boolean doHerdSpawning;
    public static int ticksBetweenSpawns;
    public static int spawnAttempts;
    public static int requiredPlayerAmount;
    public static int shinyOdds;
    public static int minimumSpawnDistance;
    public static int maximumSpawnDistance;

    public static String spawnAnnouncementMessage;

    public static void init(Path baseConfigPath){
        fullConfigPath = baseConfigPath.resolve(ConfigKey.configPath);

        final JsonObject defaultConfiguration = new JsonObject();

        addDefaultFields(defaultConfiguration);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject configuration;
        try{
            configuration = JsonParser.parseReader(new FileReader(fullConfigPath.toString()))
                    .getAsJsonObject();
        } catch (FileNotFoundException e) {
            CobblemonAlphasMod.LOGGER.warn("Could not find configuration file");
            configuration = new JsonObject();
        }

        final JsonObject finalConfiguration = configuration;
        if(defaultConfiguration.keySet().stream().anyMatch(k -> !finalConfiguration.has(k))){
            rewriteConfig(gson, defaultConfiguration, finalConfiguration);
        }

        loadConfig(finalConfiguration);
    }

    private static void addDefaultFields(JsonObject defaultConfig){
        defaultConfig.addProperty(ConfigKey.DO_ALPHA_SPAWNING, true);
        defaultConfig.addProperty(ConfigKey.ALPHA_SPAWN_CHANCE, 0.01);
        defaultConfig.addProperty(ConfigKey.ALPHA_SIZE_MULTIPLIER, 2.0);
        defaultConfig.addProperty(ConfigKey.MAXIMUM_BEST_IVS, 3);
        defaultConfig.addProperty(ConfigKey.DO_HERD_SPAWNING, true);
        defaultConfig.addProperty(ConfigKey.SECONDS_BETWEEN_SPAWNS, 300);
        defaultConfig.addProperty(ConfigKey.SPAWN_ATTEMPTS, 10);
        defaultConfig.addProperty(ConfigKey.REQUIRED_PLAYER_AMOUNT, 1);
        defaultConfig.addProperty(ConfigKey.SHINY_ODDS, 4096);
        defaultConfig.addProperty(ConfigKey.MINIMUM_SPAWN_DISTANCE, 30);
        defaultConfig.addProperty(ConfigKey.MAXIMUM_SPAWN_DISTANCE, 60);
        defaultConfig.addProperty(ConfigKey.SPAWN_ANNOUNCEMENT_MESSAGE, "An Alpha Pokemon has spawned near somebody!");
    }

    private static void rewriteConfig(Gson gson, JsonObject defaultConfig, JsonObject finalConfig){
        defaultConfig.keySet().stream()
                .filter(k -> !finalConfig.has(k))
                .forEach( k -> {
                    CobblemonAlphasMod.LOGGER.info(String.format("Adding new field '%s' to the config", k));
                    finalConfig.add(k, defaultConfig.get(k));
                });
        try{
            Files.createDirectories(Paths.get(fullConfigPath.toString()).getParent());
            FileWriter writer = new FileWriter(fullConfigPath.toString());
            gson.toJson(finalConfig, writer);
            writer.close();
        } catch (IOException ioException){
            CobblemonAlphasMod.LOGGER.warn("Could not create new config");
        }
    }

    private static void loadConfig(JsonObject finalConfiguration){
        doAlphaSpawning = finalConfiguration.get(ConfigKey.DO_ALPHA_SPAWNING).getAsBoolean();
        alphaSpawnChance = finalConfiguration.get(ConfigKey.ALPHA_SPAWN_CHANCE).getAsFloat();
        alphaSizeMultiplier = finalConfiguration.get(ConfigKey.ALPHA_SIZE_MULTIPLIER).getAsFloat();
        maximumBestIVs = finalConfiguration.get(ConfigKey.MAXIMUM_BEST_IVS).getAsInt();
        doHerdSpawning = finalConfiguration.get(ConfigKey.DO_HERD_SPAWNING).getAsBoolean();
        ticksBetweenSpawns = finalConfiguration.get(ConfigKey.SECONDS_BETWEEN_SPAWNS).getAsInt() * 20;
        spawnAttempts = finalConfiguration.get(ConfigKey.SPAWN_ATTEMPTS).getAsInt();
        requiredPlayerAmount = finalConfiguration.get(ConfigKey.REQUIRED_PLAYER_AMOUNT).getAsInt();
        shinyOdds = finalConfiguration.get(ConfigKey.SHINY_ODDS).getAsInt();
        minimumSpawnDistance = finalConfiguration.get(ConfigKey.MINIMUM_SPAWN_DISTANCE).getAsInt();
        maximumSpawnDistance = finalConfiguration.get(ConfigKey.MAXIMUM_SPAWN_DISTANCE).getAsInt();
        spawnAnnouncementMessage = finalConfiguration.get(ConfigKey.SPAWN_ANNOUNCEMENT_MESSAGE).getAsString();
    }
}
