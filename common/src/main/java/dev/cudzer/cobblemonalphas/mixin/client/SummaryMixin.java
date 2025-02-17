package dev.cudzer.cobblemonalphas.mixin.client;

import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants;
import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.cudzer.cobblemonalphas.util.PokemonUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Summary.class)
public class SummaryMixin {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    )
    private void displaySizeIcon(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci){
        Summary summary = (Summary) (Object) this;
        final float SCALE = PokedexGUIConstants.SCALE;
        int x = (summary.width - Summary.BASE_WIDTH) / 2;
        int y = (summary.height - Summary.BASE_HEIGHT) / 2;

        Pokemon pokemon = summary.getSelectedPokemon$common();
        if(pokemon.getPersistentData().getBoolean("IS_ALPHA")){
            //draw icon
            PokemonUtils.buildAlphaIcon(context.pose(), (x + 25f) / SCALE, (y + 101f) / SCALE, 16, 16, SCALE);
        }
    }
}
