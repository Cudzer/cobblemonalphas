package dev.cudzer.cobblemonalphas.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;

import java.util.Random;

public class HerdMember {

    public static Codec<HerdMember> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    Codec.STRING.fieldOf("species").forGetter(t -> t.species),
                    Codec.STRING.fieldOf("level").forGetter(w -> w.levelRange)
            ).apply(inst, HerdMember::new));


    protected String species;
    protected int level;
    protected String levelRange;

    private final int minLevel;
    private final int maxLevel;

    public HerdMember(String species, String levelRange){
        this.species = species;
        this.levelRange = levelRange;

        String[] levels = levelRange.split("-");
        if(levels.length != 2){
            CobblemonAlphasMod.LOGGER.warn(String.format("Incorrect values defined for level for herd member %s. Assigning default values", species));
            this.minLevel = 10;
            this.maxLevel = 25;
        }
        else {
            this.minLevel = Integer.parseInt(levels[0]);
            this.maxLevel = Integer.parseInt(levels[1]);
        }
    }

    public String getSpecies() {
        return species;
    }

    public int getLevelFromRange(){
        return new Random().nextInt(this.minLevel, this.maxLevel);
    }
}
