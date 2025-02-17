package dev.cudzer.cobblemonalphas.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class HerdMember {

    public static Codec<HerdMember> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    Codec.STRING.fieldOf("species").forGetter(t -> t.species),
                    Codec.INT.fieldOf("level").forGetter(w -> w.level)
            ).apply(inst, HerdMember::new));


    protected String species;
    protected int level;

    public HerdMember(String species, int level){
        this.species = species;
        this.level = level;
    }

    public String getSpecies() {
        return species;
    }

    public int getLevel() {
        return level;
    }
}
