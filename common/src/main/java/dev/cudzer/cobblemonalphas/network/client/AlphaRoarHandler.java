package dev.cudzer.cobblemonalphas.network.client;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import dev.architectury.networking.NetworkManager;
import dev.cudzer.cobblemonalphas.client.TimedEffectsManager;
import dev.cudzer.cobblemonalphas.network.AlphaRoarPacket;
import dev.cudzer.cobblemonalphas.render.TimedRoarEffect;
import net.minecraft.client.Minecraft;

public class AlphaRoarHandler {
    public static void handle(AlphaRoarPacket packet, NetworkManager.PacketContext context) {
        Minecraft.getInstance().execute(() -> {
            PokemonEntity e = (PokemonEntity) Minecraft.getInstance().level.getEntity(packet.pokemonId());
            if (e != null) {
                TimedEffectsManager.addRoar(new TimedRoarEffect(e, packet.duration()));
            }
        });
    }
}
