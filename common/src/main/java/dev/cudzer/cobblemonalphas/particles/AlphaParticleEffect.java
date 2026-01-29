package dev.cudzer.cobblemonalphas.particles;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket;

import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.*;

public class AlphaParticleEffect {
    private static final ResourceLocation ALPHA_PARTICLE = ResourceLocation.fromNamespaceAndPath("cobblemon",
            "alpha_burst");

    private static final ResourceLocation ALPHA_NEARBY_SOUND = ResourceLocation
            .fromNamespaceAndPath(CobblemonAlphasMod.MOD_ID, "alpha_spawn");

    private static final Map<UUID, Long> alphaAmbientTimer = new HashMap<>();
    private static final double particleDistance = 26.0;
    private static final int particleInterval = 20000;

    public static void tick(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        server.getAllLevels().forEach(level -> {
            level.getAllEntities().forEach(entity -> {
                // Cast to pokemon entity and check if it's an Alpha
                if (!(entity instanceof PokemonEntity pokemonEntity))
                    return;
                if (!pokemonEntity.getPokemon().getPersistentData().getBoolean("IS_ALPHA"))
                    return;

                // If there is a player close by that is looking at a wild alpha play the
                // particle effect
                boolean inRange = players.stream().anyMatch(player -> {
                    return player.distanceToSqr(pokemonEntity) <= particleDistance * particleDistance;
                });

                if (!inRange)
                    return;

                // Fetch the first player that has LOS to the alpha
                ServerPlayer lookingAtAlpha = players.stream()
                        .filter(player -> pokemonEntity.getPokemon().isWild() && player.hasLineOfSight(pokemonEntity))
                        .findFirst().orElse(null);

                // If a wild pokemon is within LOS play the particle effect
                if (pokemonEntity.getPokemon().isWild() &&
                        !pokemonEntity.isBattling() &&
                        !pokemonEntity.isBusy() &&
                        lookingAtAlpha != null) {
                    Long startTime = alphaAmbientTimer.get(pokemonEntity.getUUID());
                    if (startTime == null || System.currentTimeMillis() - startTime >= particleInterval) {
                        spawnParticles(pokemonEntity);
                        alphaAmbientTimer.put(pokemonEntity.getUUID(), System.currentTimeMillis());
                    }
                }
            });
        });

    }

    public static void spawnParticles(PokemonEntity entity) {
        List<String> locator = List.of("root");
        SpawnSnowstormEntityParticlePacket packet = new SpawnSnowstormEntityParticlePacket(ALPHA_PARTICLE,
                entity.getId(), locator, entity.getId(), List.of());
        packet.sendToPlayersAround(entity.getX(), entity.getY(), entity.getZ(), 64, Level.OVERWORLD, (player) -> false);

        SoundEvent sound = SoundEvent.createFixedRangeEvent(ALPHA_NEARBY_SOUND, 64.0f);
        entity.level().playSound(entity, entity.blockPosition(), sound, SoundSource.NEUTRAL, 2.0f, 1.0f);
    }
}
