package dev.cudzer.cobblemonalphas.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Alpha {
    public static Codec<Alpha> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    Codec.STRING.fieldOf("species").forGetter(t -> t.species),
                    Codec.STRING.fieldOf("level").forGetter(w -> w.levelRange),
                    Codec.BOOL.fieldOf("spawnUnderground").forGetter(s -> s.spawnUnderground),
                    HerdMember.CODEC.listOf().fieldOf("herdMembers").forGetter(h -> h.herdMembers),
                    ExtraCodecs.TAG_OR_ELEMENT_ID.listOf().optionalFieldOf("biome", Collections.emptyList())
                            .forGetter(t -> t.biomeTags))
            .apply(inst, Alpha::new));

    protected final String species;
    protected int level = 1;
    protected final String levelRange;
    protected final boolean spawnUnderground;
    protected final List<HerdMember> herdMembers;
    protected final List<ResourceLocation> spawnBiome;
    protected final List<ResourceLocation> spawnBiomeTags;
    protected final List<ExtraCodecs.TagOrElementLocation> biomeTags;

    private ResourceLocation jsonLocation;

    private int minLevel = 1;
    private int maxLevel = 1;

    public Alpha(String species, String levelRange, boolean spawnUnderground, List<HerdMember> herdMembers,
            List<ExtraCodecs.TagOrElementLocation> spawnBiome) {
        this.species = species;
        this.levelRange = levelRange;
        this.spawnUnderground = spawnUnderground;

        String[] levels = levelRange.split("-");
        if (levels.length == 1) {
            this.level = Integer.parseInt(levels[0]);
        } else if (levels.length == 2) {
            this.minLevel = Integer.parseInt(levels[0]);
            this.maxLevel = Integer.parseInt(levels[1]);
        } else {
            CobblemonAlphasMod.LOGGER.warn(String
                    .format("Incorrect values defined for levels in alpha %s file. Assigning default values", species));
            this.minLevel = 40;
            this.maxLevel = 50;
        }

        this.herdMembers = herdMembers;
        List<ResourceLocation> spawnBiomeTags = new ArrayList<>();
        List<ResourceLocation> spawnBiomes = new ArrayList<>();
        for (ExtraCodecs.TagOrElementLocation tagOrElementLocation : spawnBiome) {
            if (tagOrElementLocation.tag()) {
                spawnBiomeTags.add(tagOrElementLocation.id());
            } else {
                spawnBiomes.add(tagOrElementLocation.id());
            }
        }
        this.spawnBiome = spawnBiomes;
        this.spawnBiomeTags = spawnBiomeTags;

        this.biomeTags = spawnBiome;
    }

    public void setJsonLocation(ResourceLocation jsonLocation) {
        this.jsonLocation = jsonLocation;
    }

    public ResourceLocation getJsonLocation() {
        try {
            return jsonLocation;
        } catch (Exception e) {
            CobblemonAlphasMod.LOGGER.error(String.format("Could not find json location due to %s", e));
        }
        return ResourceLocation.parse("");
    }

    public String getSpecies() {
        return this.species;
    }

    public boolean canSpawnUnderground() {
        return spawnUnderground;
    }

    public List<HerdMember> getHerdMembers() {
        return herdMembers;
    }

    public List<ResourceLocation> getSpawnBiome() {
        return spawnBiome;
    }

    public List<ResourceLocation> getSpawnBiomeTags() {
        return spawnBiomeTags;
    }

    public int getLevelFromRange() {
        if (level != 1) {
            return level;
        }
        return new Random().nextInt(this.minLevel, this.maxLevel);
    }
}
