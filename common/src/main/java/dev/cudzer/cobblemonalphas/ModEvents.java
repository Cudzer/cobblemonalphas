package dev.cudzer.cobblemonalphas;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.cudzer.cobblemonalphas.particles.AlphaParticleEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ModEvents {

    public static void registerEvents(){
        CobblemonEvents.BATTLE_STARTED_POST.subscribe(Priority.HIGH, ModEvents::onBattleStarted);
    }

    private static void onBattleStarted(BattleStartedEvent.Post event){
        var battle = event.getBattle();
        boolean wildIsAlpha = false;
        BattlePokemon alphaWild = null;

        for (var actor : battle.getActors()) {

            if (actor.getType() != ActorType.WILD) continue;
            if (actor.getPokemonList().isEmpty()) continue;

            BattlePokemon wild = actor.getPokemonList().getFirst();
            Pokemon original = wild.getOriginalPokemon();

            if (original != null && original.getPersistentData().getBoolean("IS_ALPHA")) {
                wildIsAlpha = true;
                alphaWild = wild;
                break;
            }
        }

        if (!wildIsAlpha || alphaWild == null) return;
        Pokemon alphaMon = alphaWild.getOriginalPokemon();

        alphaWild.getStatChanges().put(Stats.ATTACK, 2);
        alphaWild.getStatChanges().put(Stats.DEFENCE, 2);
        alphaWild.getStatChanges().put(Stats.HP, 2);
        alphaWild.getStatChanges().put(Stats.SPECIAL_ATTACK, 2);
        alphaWild.getStatChanges().put(Stats.SPECIAL_DEFENCE, 2);
        alphaWild.getStatChanges().put(Stats.SPEED, 2);
        CobblemonAlphasMod.LOGGER.info("Alpha {} detected — applying Wild Might boosts.", alphaMon.getSpecies().getName());

        var entity = alphaMon.getEntity();
        if (entity != null) {
            //AlphaParticleEffect.simpleBurst(entity);
            AlphaParticleEffect.wildMight(entity);
        }

        Component msg = Component.literal("The Alpha is filled with Wild Might!")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

        for (ServerPlayer player : battle.getPlayers()) {
            player.displayClientMessage(msg, true);
        }
    }
}
