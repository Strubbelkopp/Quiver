package dev.strubbelkopp.quiver.mixin;

import dev.strubbelkopp.quiver.item.QuiverItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;
    @Unique private ItemStack selectedArrow;

    @Shadow private void renderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed) { throw new IllegalCallerException(); }

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getOffHandStack()Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getSelectedArrow(float tickDelta, DrawContext context, CallbackInfo ci, PlayerEntity player) {
        QuiverItem.getQuiverItem(player).flatMap(QuiverItem::getSelectedArrow).ifPresentOrElse(
                selectedArrow -> this.selectedArrow = selectedArrow,
                () -> this.selectedArrow = null);
    }

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderArrowSlot(float tickDelta, DrawContext context, CallbackInfo ci, PlayerEntity player) {
        if (selectedArrow != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            Arm arm = player.getMainArm();

            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, -90.0F);
            if (arm == Arm.LEFT) {
                context.drawTexture(WIDGETS_TEXTURE, this.scaledWidth / 2 - 91 - 29, this.scaledHeight - 23, 24, 22, 29, 24);
            } else {
                context.drawTexture(WIDGETS_TEXTURE, this.scaledWidth / 2 + 91, this.scaledHeight - 23, 53, 22, 29, 24);
            }
            context.getMatrices().pop();
        }
    }

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getAttackIndicator()Lnet/minecraft/client/option/SimpleOption;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderSelectedArrow(float tickDelta, DrawContext context, CallbackInfo ci, PlayerEntity player, ItemStack itemStack, Arm arm, int i) {
        if (this.selectedArrow != null) {
            int seed = (player.getOffHandStack().isEmpty()) ? 9 : 10;
            if (arm != Arm.LEFT) {
                this.renderHotbarItem(context, this.scaledWidth / 2 - 91 - 26, this.scaledHeight - 19, tickDelta, player, this.selectedArrow, seed);
            } else {
                this.renderHotbarItem(context, this.scaledWidth / 2 + 91 + 10, this.scaledHeight - 19, tickDelta, player, this.selectedArrow, seed);
            }
        }
    }
}
