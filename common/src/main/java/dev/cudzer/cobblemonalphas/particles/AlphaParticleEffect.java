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
    private static final ResourceLocation ALPHA_AURA = ResourceLocation.fromNamespaceAndPath("cobblemon", "alpha_aura");
    private static final ResourceLocation WILD_MIGHT = ResourceLocation.fromNamespaceAndPath("cobblemon", "wild_might");

    private static final ResourceLocation ALPHA_NEARBY_SOUND = ResourceLocation
            .fromNamespaceAndPath(CobblemonAlphasMod.MOD_ID, "alpha_spawn");
    private static final ResourceLocation ALPHA_MIGHT_SOUND = ResourceLocation
            .fromNamespaceAndPath(CobblemonAlphasMod.MOD_ID, "wild_might");

    private static final Map<UUID, Long> alphaAmbientTimer = new HashMap<>();
    private static final double particleDistance = 26.0;
    private static final int particleInterval = 20000;

    public static void tick(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        server.getAllLevels().forEach(level -> {
            level.getAllEntities().forEach(entity -> {
                if (!(entity instanceof PokemonEntity pokemonEntity)
                        || !pokemonEntity.getPokemon().getPersistentData().getBoolean("IS_ALPHA")) {
                    return;
                }

                // * Loop over all Alpha pokemon in the world from here

                // ? Apply aura effects to batteling alphas (tbh I don't know why this exists)
                if (pokemonEntity.isBattling()) {
                    if (pokemonEntity.getPokemon().getPersistentData().getBoolean("SUPER_ALPHA")) {
                        AlphaParticleEffect.superAura(pokemonEntity);
                    } else {
                        AlphaParticleEffect.aura(pokemonEntity);
                    }
                }

                // If ther is a player closeby that is looking at a wild alpha play the particle
                // effect
                boolean inRange = players.stream().anyMatch(player -> {
                    return player.distanceToSqr(pokemonEntity) <= particleDistance * particleDistance;
                });
                ServerPlayer lookingAtAlpha = players.stream()
                        .filter(player -> pokemonEntity.getPokemon().isWild() && player.hasLineOfSight(pokemonEntity))
                        .findFirst().orElse(null);
                if (!inRange) {
                    return;
                } else {
                    if (pokemonEntity.getPokemon().isWild() && !pokemonEntity.isBattling() && !pokemonEntity.isBusy()
                            && lookingAtAlpha != null) {
                        Long startTime = alphaAmbientTimer.get(pokemonEntity.getUUID());
                        if (startTime == null || System.currentTimeMillis() - startTime >= particleInterval) {
                            spawnParticles(pokemonEntity);
                            alphaAmbientTimer.put(pokemonEntity.getUUID(), System.currentTimeMillis());
                        }
                    }
                }
            });
        });
    }

    public static void wildMight(PokemonEntity entity) {
        List<String> locator = List.of("root");
        SpawnSnowstormEntityParticlePacket packet = new SpawnSnowstormEntityParticlePacket(WILD_MIGHT, entity.getId(),
                locator, entity.getId(), List.of());
        packet.sendToPlayersAround(entity.getX(), entity.getY(), entity.getZ(), 64, Level.OVERWORLD, (player) -> false);

        SoundEvent sound = SoundEvent.createFixedRangeEvent(ALPHA_MIGHT_SOUND, 64.0f);
        entity.level().playSound(entity, entity.blockPosition(), sound, SoundSource.NEUTRAL, 2.0f, 1.0f);
    }

    public static void aura(PokemonEntity entity) {
        List<String> locator = List.of("root");
        SpawnSnowstormEntityParticlePacket packet = new SpawnSnowstormEntityParticlePacket(ALPHA_AURA, entity.getId(),
                locator, entity.getId(), List.of());
        packet.sendToPlayersAround(entity.getX(), entity.getY(), entity.getZ(), 64, Level.OVERWORLD, (player) -> false);
    }

    public static void superAura(PokemonEntity entity) {
        List<String> locator = List.of("root");
        SpawnSnowstormEntityParticlePacket packet = new SpawnSnowstormEntityParticlePacket(ALPHA_AURA, entity.getId(),
                locator, entity.getId(), List.of());
        packet.sendToPlayersAround(entity.getX(), entity.getY(), entity.getZ(), 64, Level.OVERWORLD, (player) -> false);
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
