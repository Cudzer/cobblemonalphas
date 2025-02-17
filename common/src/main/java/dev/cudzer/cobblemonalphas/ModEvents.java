package dev.cudzer.cobblemonalphas;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleStartedPostEvent;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ModEvents {

    public static void registerEvents(){
        CobblemonEvents.BATTLE_STARTED_POST.subscribe(Priority.HIGH, ModEvents::onBattleStarted);
    }

    private static Unit onBattleStarted(BattleStartedPostEvent event){
        event.getBattle().getActors().forEach(battleActor -> {
            if(battleActor.getType() == ActorType.WILD){
                BattlePokemon battlePokemon = battleActor.getPokemonList().getFirst();
                if(battlePokemon.getOriginalPokemon().getPersistentData().getBoolean("IS_ALPHA")){

                    battlePokemon.getStatChanges().put(Stats.ATTACK, 2);
                    battlePokemon.getStatChanges().put(Stats.DEFENCE, 2);
                    battlePokemon.getStatChanges().put(Stats.HP, 2);
                    battlePokemon.getStatChanges().put(Stats.SPECIAL_ATTACK, 2);
                    battlePokemon.getStatChanges().put(Stats.SPECIAL_DEFENCE, 2);
                    battlePokemon.getStatChanges().put(Stats.SPEED, 2);
                }
            }
            else if(battleActor.getType() == ActorType.PLAYER){
                //This doesn't seem to work right now. Work on an alternative
                MutableComponent component = Component.literal("The alpha is filled with Wild Might");
                battleActor.sendMessage(component);
            }
        });
        return Unit.INSTANCE;
    }
}
