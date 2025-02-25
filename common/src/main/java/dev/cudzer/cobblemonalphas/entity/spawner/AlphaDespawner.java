package dev.cudzer.cobblemonalphas.entity.spawner;

import com.cobblemon.mod.common.api.entity.Despawner;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import org.jetbrains.annotations.NotNull;

public class AlphaDespawner implements Despawner<PokemonEntity> {

    private static final AlphaDespawner instance = new AlphaDespawner();
    public static AlphaDespawner getInstance() {return instance;}

    private AlphaDespawner(){}

    private int minimumDespawnDistance;
    private int spawnIntervalTicks;

    public void setMinimumDespawnDistance(int minimumDespawnDistance) {
        this.minimumDespawnDistance = minimumDespawnDistance;
    }

    public void setSpawnIntervalTicks(int spawnIntervalTicks) {
        this.spawnIntervalTicks = spawnIntervalTicks;
    }

    @Override
    public void beginTracking(@NotNull PokemonEntity pokemonEntity) {

    }

    @Override
    public boolean shouldDespawn(@NotNull PokemonEntity pokemonEntity) {
        if(pokemonEntity.getTicksLived() < spawnIntervalTicks) return false;
        if(pokemonEntity.isBusy()) return false;
        return !pokemonEntity.level().hasNearbyAlivePlayer(pokemonEntity.getX(), pokemonEntity.getY(), pokemonEntity.getZ(), minimumDespawnDistance);
    }
}
