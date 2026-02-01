package dev.cudzer.cobblemonalphas;

import java.util.stream.StreamSupport;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockActiveAnimation;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;

import dev.cudzer.cobblemonalphas.network.CobblemonAlphasNetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class ModEvents {

    public static void registerEvents() {
        CobblemonEvents.BATTLE_STARTED_POST.subscribe(Priority.HIGH, ModEvents::onBattleStarted);
    }

    private static void onBattleStarted(BattleStartedEvent.Post event) {
        // Check if a WILD alpha pokemon is present in the battle
        PokemonBattle battle = event.getBattle();
        BattlePokemon wildAlpha = StreamSupport.stream(battle.getActors().spliterator(), false)
                .filter(a -> a.getType() == ActorType.WILD && !a.getPokemonList().isEmpty())
                .map(a -> a.getPokemonList().getFirst())
                .filter(w -> {
                    Pokemon p = w.getOriginalPokemon();
                    return p != null && p.getPersistentData().getBoolean("IS_ALPHA");
                })
                .findFirst()
                .orElse(null);

        if (wildAlpha == null)
            return;

        // Apply the wild might stat boosts
        wildAlpha.getStatChanges().put(Stats.HP, 2);
        wildAlpha.getStatChanges().put(Stats.ATTACK, 2);
        wildAlpha.getStatChanges().put(Stats.DEFENCE, 2);
        wildAlpha.getStatChanges().put(Stats.SPECIAL_ATTACK, 2);
        wildAlpha.getStatChanges().put(Stats.SPECIAL_DEFENCE, 2);
        wildAlpha.getStatChanges().put(Stats.SPEED, 2);

        // Fetch the associated pokemon of this alpha
        Pokemon alphaPokemon = wildAlpha.getOriginalPokemon();
        String alphaName = alphaPokemon.getSpecies().getName();

        // Play the cry / roar animation
        if (!alphaPokemon.getEntity().isSilent()) {
            float duration = new BedrockActiveAnimation(
                    BedrockAnimationRepository.INSTANCE.getAnimation(alphaName.toLowerCase(),
                            "animation." + alphaName.toLowerCase() + ".cry"))
                    .getDuration() * 1000;
            alphaPokemon.getEntity().cry();
            CobblemonAlphasNetworkManager.sendRoarEffect(
                    battle.getPlayers().getFirst(),
                    alphaPokemon.getEntity().getId(),
                    duration);
        }

        // Construct wild might message
        MutableComponent wildMightMessage = LocalizationUtilsKt
                .battleLang("ability.wildMight", alphaName)
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

        // Display wild might message
        battle.broadcastChatMessage(wildMightMessage);
        for (ServerPlayer player : battle.getPlayers()) {
            player.displayClientMessage(wildMightMessage, true);
        }
    }
}
