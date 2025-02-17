package dev.cudzer.cobblemonalphas.mixin.client;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.cudzer.cobblemonalphas.util.PokemonUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PCGUI.class)
public class PCGuiMixin {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    )
    private void displayAlphaIcon(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci){
        PCGUI pcgui = ((PCGUI) (Object) this);
        int x = (pcgui.width - PCGUI.BASE_WIDTH) / 2;
        int y = (pcgui.height - PCGUI.BASE_HEIGHT) / 2;

        Pokemon pokemon = pcgui.getPreviewPokemon$common();
        if(pcgui.getPreviewPokemon$common() != null){
            if(pokemon.getPersistentData().getBoolean("IS_ALPHA")){
                //draw icon
                PokemonUtils.buildAlphaIcon(context.pose(), (x + 25f) / PCGUI.SCALE, (y + 95.5f) / PCGUI.SCALE, 16, 16, PCGUI.SCALE);
            }
        }
    }
}
