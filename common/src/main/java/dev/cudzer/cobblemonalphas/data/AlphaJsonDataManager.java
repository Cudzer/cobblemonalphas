package dev.cudzer.cobblemonalphas.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.entity.Alpha;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.*;

import static dev.cudzer.cobblemonalphas.CobblemonAlphasMod.cobblemonAlphasResource;

public class AlphaJsonDataManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    protected static Map<ResourceLocation, Alpha> data = new HashMap<>();
    protected static List<ResourceLocation> resourceLocationList = new ArrayList<>();
    protected static Map<ResourceKey<Biome>, Map<ResourceLocation, Alpha>> biomeData = new HashMap<>();
    protected static Map<ResourceKey<Biome>, List<ResourceLocation>> resourceLocationMap = new HashMap<>();

    protected static List<ResourceLocation> undergroundSpawnResourceLocationList = new ArrayList<>();
    protected static Map<ResourceLocation, Alpha> undergroundSpawnsMap = new HashMap<>();
    protected static Map<ResourceKey<Biome>, List<ResourceLocation>> undergroundResourceLocationMap = new HashMap<>();
    protected static Map<ResourceKey<Biome>, Map<ResourceLocation, Alpha>> undergroundBiomeData = new HashMap<>();

    public AlphaJsonDataManager(){
        super(GSON, cobblemonAlphasResource("alphas").getPath());
    }

    public static Map<ResourceLocation, Alpha> getRandomAlphaForBiome(Level level, ResourceKey<Biome> biome, boolean undergroundSpawn){
        if(!undergroundSpawn){
            Map<ResourceLocation, Alpha> map = new HashMap<>();
            ResourceLocation rl = getRandomResourceLocationForBiome(level, biome);
            Map<ResourceLocation, Alpha> alphaMap = biomeData.getOrDefault(biome, new HashMap<>());
            Alpha alpha = alphaMap.getOrDefault(rl, null);
            if(alpha == null){
                alphaMap = getRandomAlpha(level);
                rl = alphaMap.keySet().stream().toList().getFirst();
                alpha = alphaMap.values().stream().toList().getFirst();
            }
            map.put(rl, alpha);
            return map;
        }
        else {
            Map<ResourceLocation, Alpha> map = new HashMap<>();
            ResourceLocation rl = getRandomUndergroundResourceLocationForBiome(level, biome);
            Map<ResourceLocation, Alpha> alphaMap = undergroundBiomeData.getOrDefault(biome, new HashMap<>());
            Alpha alpha = alphaMap.getOrDefault(rl, null);
            if(alpha == null){
                alphaMap = getRandomUndergroundAlpha(level);
                rl = alphaMap.keySet().stream().toList().getFirst();
                alpha = alphaMap.values().stream().toList().getFirst();
            }
            map.put(rl, alpha);
            return map;
        }

    }

    private static ResourceLocation getRandomResourceLocationForBiome(Level level, ResourceKey<Biome> biome){
        List<ResourceLocation> resourceLocations = resourceLocationMap.getOrDefault(biome, Collections.emptyList());
        if(resourceLocations.isEmpty()){
            return getRandomResourceLocation(level);
        }
        return resourceLocations.get(level.random.nextInt(resourceLocations.size()));
    }

    private static ResourceLocation getRandomUndergroundResourceLocationForBiome(Level level, ResourceKey<Biome> biome){
        List<ResourceLocation> resourceLocations = undergroundResourceLocationMap.getOrDefault(biome, Collections.emptyList());
        if(resourceLocations.isEmpty()){
            return getRandomUndergroundResourceLocation(level);
        }
        return resourceLocations.get(level.random.nextInt(resourceLocations.size()));
    }

    public static Map<ResourceLocation, Alpha> getRandomAlpha(Level level){
        Map<ResourceLocation, Alpha> map = new HashMap<>();
        ResourceLocation rl = getRandomResourceLocation(level);
        Alpha alpha = data.get(rl);
        map.put(rl, alpha);
        return map;
    }

    public static Map<ResourceLocation, Alpha> getRandomUndergroundAlpha(Level level){
        Map<ResourceLocation, Alpha> map = new HashMap<>();
        ResourceLocation rl = getRandomUndergroundResourceLocation(level);
        Alpha alpha = undergroundSpawnsMap.get(rl);
        map.put(rl, alpha);
        return map;
    }

    public static Alpha getRandomAlphaObj(Level level){
        ResourceLocation rl = getRandomResourceLocation(level);
        if(rl == null){
            rl = getRandomUndergroundResourceLocation(level);
            return undergroundSpawnsMap.get(rl);
        }
        return data.get(rl);
    }

    public static Alpha getAlphaObj(Level level, String alphaName){
        for(Alpha alpha : data.values()){
            if(alpha.getSpecies().equalsIgnoreCase(alphaName)){
                return alpha;
            }
        }
        for(Alpha alpha : undergroundSpawnsMap.values()){
            if(alpha.getSpecies().equalsIgnoreCase(alphaName)){
                return alpha;
            }
        }
        return null;
    }

    private static ResourceLocation getRandomResourceLocation(Level level) {
        return resourceLocationList.get(level.random.nextInt(resourceLocationList.size()));
    }

    private static ResourceLocation getRandomUndergroundResourceLocation(Level level){
        return undergroundSpawnResourceLocationList.get(level.random.nextInt(undergroundSpawnResourceLocationList.size()));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        CobblemonAlphasMod.LOGGER.info("Loading alpha definitions...");

        Map<ResourceLocation, Alpha> newMap = new HashMap<>();
        List<ResourceLocation> newResourceLocationList = new ArrayList<>();
        Map<ResourceKey<Biome>, List<ResourceLocation>> resourceLocationBiomeMap = new HashMap<>();

        Map<ResourceLocation, Alpha> newUndergroundMap = new HashMap<>();
        List<ResourceLocation> newUndergroundResourceLocationList = new ArrayList<>();
        Map<ResourceKey<Biome>, List<ResourceLocation>> undergroundLocationBiomeMap = new HashMap<>();

        data.clear();
        biomeData.clear();
        resourceLocationList.clear();

        undergroundSpawnsMap.clear();
        undergroundSpawnResourceLocationList.clear();

        for(Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()){
            ResourceLocation key = entry.getKey();
            JsonElement element = entry.getValue();

            Alpha.CODEC.decode(JsonOps.INSTANCE, element)
                    .ifSuccess( result -> {
                        Alpha alpha = result.getFirst();
                        alpha.setJsonLocation(key);
                        if(alpha.canSpawnUnderground()){
                            newUndergroundMap.put(key, alpha);
                            newUndergroundResourceLocationList.add(key);
                        }
                        else {
                            newMap.put(key, alpha);
                            newResourceLocationList.add(key);
                        }

                        List<ResourceLocation> spawnBiome = alpha.getSpawnBiome();

                        spawnBiome.forEach(biome -> {
                            ResourceKey<Biome> biomeResourceKey = null;
                            try{
                                biomeResourceKey = ResourceKey.create(Registries.BIOME, biome);
                            }catch (Exception e) {
                                CobblemonAlphasMod.LOGGER.error(String.format("Could not find biome %s in %s due to %s", biome, key, e));
                            }
                            if(biomeResourceKey == null){
                                CobblemonAlphasMod.LOGGER.error(String.format("Could not find biome %s in %s", biome, key));
                            }
                            List<ResourceLocation> resourceLocations = resourceLocationBiomeMap.getOrDefault(biomeResourceKey, new ArrayList<>());
                            resourceLocations.add(key);
                            resourceLocationBiomeMap.put(biomeResourceKey, resourceLocations);
                        });
                    })
                    .ifError(partial -> {
                        CobblemonAlphasMod.LOGGER.error(String.format("Failed to parse data json for %s due to %s", key, partial.message()));
                    });
        }
        resourceLocationList = newResourceLocationList;
        data = newMap;

        undergroundSpawnsMap = newUndergroundMap;
        undergroundSpawnResourceLocationList = newUndergroundResourceLocationList;

        CobblemonAlphasMod.LOGGER.info(String.format("Loaded %s alpha definitions.", (data.size() + undergroundSpawnsMap.size())));
    }

    public static void populateBiomeData(ServerLevel level){
        applySurfaceBiomeAlphas(level);
        applyUndergroundBiomeAlphas(level);
    }

    public static Map<ResourceLocation, Alpha> getAlphaData(){
        var newData = data;
        newData.putAll(undergroundSpawnsMap);
        return newData;
    }

    private static void applySurfaceBiomeAlphas(Level level){
        Map<ResourceKey<Biome>, Map<ResourceLocation, Alpha>> newBiomeData = new HashMap<>();
        Map<ResourceKey<Biome>, List<ResourceLocation>> resourceLocationBiomeMap = new HashMap<>();

        for(Alpha alpha : data.values()) {
            if (!alpha.canSpawnUnderground()) {
                List<ResourceLocation> tagsResourceLocation = alpha.getSpawnBiomeTags();
                List<ResourceLocation> biomesResourceLocation = alpha.getSpawnBiome();
                for (ResourceLocation tag : tagsResourceLocation) {
                    TagKey<Biome> biomeTagKey = TagKey.create(Registries.BIOME, tag);
                    level.registryAccess().registry(Registries.BIOME).ifPresent(reg -> {
                        Iterable<Holder<Biome>> biomeHolder = reg.getTagOrEmpty(biomeTagKey);
                        for (Holder<Biome> biome : biomeHolder) {
                            ResourceKey<Biome> biomeResourceKey = biome.unwrapKey().get();
                            Map<ResourceLocation, Alpha> mapToPut = newBiomeData.computeIfAbsent(biomeResourceKey, k -> new HashMap<>());
                            mapToPut.put(alpha.getJsonLocation(), alpha);
                            newBiomeData.put(biomeResourceKey, mapToPut);

                            List<ResourceLocation> resourceLocations = resourceLocationBiomeMap.getOrDefault(biomeResourceKey, new ArrayList<>());
                            resourceLocations.add(alpha.getJsonLocation());
                            resourceLocationBiomeMap.put(biomeResourceKey, resourceLocations);
                        }
                        if (!biomeHolder.iterator().hasNext()) {
                            CobblemonAlphasMod.LOGGER.error(String.format("Tag for %s does not have any biomes!", biomeTagKey));
                            CobblemonAlphasMod.LOGGER.error(String.format("Alpha for %s might not have any biomes assigned!", alpha.getJsonLocation()));
                        }
                    });
                }

                for (ResourceLocation biome : biomesResourceLocation) {
                    ResourceKey<Biome> biomeResourceKey = null;
                    try {
                        biomeResourceKey = ResourceKey.create(Registries.BIOME, biome);
                    } catch (Exception e) {
                        CobblemonAlphasMod.LOGGER.error(String.format("Could not find biome %s in alpha for %s due to %s", biome, alpha.getJsonLocation(), e));
                    }
                    if (biomeResourceKey == null) {
                        CobblemonAlphasMod.LOGGER.error(String.format("Could not find biome %s in alpha for %s, skipping...", biome, alpha.getJsonLocation()));
                        continue;
                    }

                    List<ResourceLocation> resourceLocations = resourceLocationBiomeMap.getOrDefault(biomeResourceKey, new ArrayList<>());
                    resourceLocations.add(alpha.getJsonLocation());
                    resourceLocationBiomeMap.put(biomeResourceKey, resourceLocations);

                    Map<ResourceLocation, Alpha> mapToPut = newBiomeData.computeIfAbsent(biomeResourceKey, k -> new HashMap<>());
                    mapToPut.put(alpha.getJsonLocation(), alpha);
                    newBiomeData.put(biomeResourceKey, mapToPut);
                }
            }
        }
        biomeData.putAll(newBiomeData);
        resourceLocationMap.putAll(resourceLocationBiomeMap);

        newBiomeData.clear();
    }

    private static void applyUndergroundBiomeAlphas(Level level){
        Map<ResourceKey<Biome>, Map<ResourceLocation, Alpha>> newUndergroundBiomeData = new HashMap<>();
        Map<ResourceKey<Biome>, List<ResourceLocation>> undergroundLocationBiomeMap = new HashMap<>();

        for(Alpha alpha : undergroundSpawnsMap.values()) {
            if(alpha.canSpawnUnderground()){
                List<ResourceLocation> tagsResourceLocation = alpha.getSpawnBiomeTags();
                List<ResourceLocation> biomesResourceLocation = alpha.getSpawnBiome();
                for(ResourceLocation tag : tagsResourceLocation){
                    TagKey<Biome> biomeTagKey = TagKey.create(Registries.BIOME, tag);
                    level.registryAccess().registry(Registries.BIOME).ifPresent(reg -> {
                        Iterable<Holder<Biome>> biomeHolder = reg.getTagOrEmpty(biomeTagKey);
                        for(Holder<Biome> biome : biomeHolder){
                            ResourceKey<Biome> biomeResourceKey = biome.unwrapKey().get();
                            Map<ResourceLocation, Alpha> mapToPut = newUndergroundBiomeData.computeIfAbsent(biomeResourceKey, k -> new HashMap<>());
                            mapToPut.put(alpha.getJsonLocation(), alpha);
                            newUndergroundBiomeData.put(biomeResourceKey, mapToPut);

                            List<ResourceLocation> resourceLocations = undergroundLocationBiomeMap.getOrDefault(biomeResourceKey, new ArrayList<>());
                            resourceLocations.add(alpha.getJsonLocation());
                            undergroundLocationBiomeMap.put(biomeResourceKey, resourceLocations);
                        }
                        if(!biomeHolder.iterator().hasNext()){
                            CobblemonAlphasMod.LOGGER.error(String.format("Tag for %s does not have any biomes!", biomeTagKey));
                            CobblemonAlphasMod.LOGGER.error(String.format("Alpha for %s might not have any biomes assigned!", alpha.getJsonLocation()));
                        }
                    });
                }

                for(ResourceLocation biome : biomesResourceLocation){
                    ResourceKey<Biome> biomeResourceKey = null;
                    try{
                        biomeResourceKey = ResourceKey.create(Registries.BIOME, biome);
                    } catch (Exception e){
                        CobblemonAlphasMod.LOGGER.error(String.format("Could not find biome %s in alpha for %s due to %s", biome, alpha.getJsonLocation(), e));
                    }
                    if(biomeResourceKey == null){
                        CobblemonAlphasMod.LOGGER.error(String.format("Could not find biome %s in alpha for %s, skipping...", biome, alpha.getJsonLocation()));
                        continue;
                    }

                    List<ResourceLocation> resourceLocations = undergroundLocationBiomeMap.getOrDefault(biomeResourceKey, new ArrayList<>());
                    resourceLocations.add(alpha.getJsonLocation());
                    undergroundLocationBiomeMap.put(biomeResourceKey, resourceLocations);

                    Map<ResourceLocation, Alpha> mapToPut  = newUndergroundBiomeData.computeIfAbsent(biomeResourceKey, k -> new HashMap<>());
                    mapToPut.put(alpha.getJsonLocation(), alpha);
                    newUndergroundBiomeData.put(biomeResourceKey, mapToPut);
                }
            }

        }
        undergroundBiomeData.putAll(newUndergroundBiomeData);
        undergroundResourceLocationMap.putAll(undergroundLocationBiomeMap);

        newUndergroundBiomeData.clear();
    }
}
