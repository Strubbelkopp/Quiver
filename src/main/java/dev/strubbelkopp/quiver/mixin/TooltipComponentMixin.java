package dev.strubbelkopp.quiver.mixin;

import dev.strubbelkopp.quiver.client.QuiverTooltipComponent;
import dev.strubbelkopp.quiver.item.QuiverTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin {
    @Inject(method = "of(Lnet/minecraft/client/item/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void of(TooltipData data, CallbackInfoReturnable<TooltipComponent> cir) {
        if (data instanceof QuiverTooltipData) {
            cir.setReturnValue(new QuiverTooltipComponent((QuiverTooltipData) data));
        }
    }
}
