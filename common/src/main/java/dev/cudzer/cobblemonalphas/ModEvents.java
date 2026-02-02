package dev.cudzer.cobblemonalphas;

import java.util.stream.StreamSupport;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

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
        alphaPokemon.getEntity().cry();

        // Construct wild might message
        MutableComponent wildMightMessage = LocalizationUtilsKt
                .battleLang("ability.wildMight", alphaName)
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

        // Recursive retry until actors exist
        @SuppressWarnings("unchecked")
        Function0<Unit>[] retry = new Function0[1];

        retry[0] = () -> {
            if (!battle.getActors().iterator().hasNext()) {
                battle.dispatchWaitingToFront(0.1f, retry[0]);
                return Unit.INSTANCE;
            }

            for (BattleActor actor : battle.getActors()) {
                actor.sendMessage(wildMightMessage);
            }
            return Unit.INSTANCE;
        };

        // schedule the first attempt
        battle.dispatchWaitingToFront(0.1f, retry[0]);
    }
}
