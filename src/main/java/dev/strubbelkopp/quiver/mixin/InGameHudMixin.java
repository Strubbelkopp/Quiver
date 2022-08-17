package dev.strubbelkopp.quiver.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.strubbelkopp.quiver.item.QuiverItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
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
public abstract class InGameHudMixin extends DrawableHelper {

    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;
    @Unique private ItemStack selectedArrow;

    @Shadow private void renderHotbarItem(int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed) { throw new IllegalCallerException(); }

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getOffHandStack()Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getSelectedArrow(float tickDelta, MatrixStack matrices, CallbackInfo ci, PlayerEntity player) {
        QuiverItem.getQuiverItem(player).flatMap(QuiverItem::getSelectedArrow).ifPresentOrElse(
                selectedArrow -> this.selectedArrow = selectedArrow,
                () -> this.selectedArrow = null);
    }

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setZOffset(I)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderArrowSlot(float tickDelta, MatrixStack matrices, CallbackInfo ci, PlayerEntity player) {
        if (selectedArrow != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            Arm arm = player.getMainArm();
            if (arm == Arm.LEFT) {
                this.drawTexture(matrices, this.scaledWidth / 2 - 91 - 29, this.scaledHeight - 23, 24, 22, 29, 24);
            } else {
                this.drawTexture(matrices, this.scaledWidth / 2 + 91, this.scaledHeight - 23, 53, 22, 29, 24);
            }
        }
    }

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getAttackIndicator()Lnet/minecraft/client/option/SimpleOption;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderSelectedArrow(float tickDelta, MatrixStack matrices, CallbackInfo ci, PlayerEntity player) {
        if (this.selectedArrow != null) {
            Arm arm = player.getMainArm();
            int seed = (player.getOffHandStack().isEmpty()) ? 9 : 10;
            if (arm == Arm.LEFT) {
                this.renderHotbarItem(this.scaledWidth / 2 - 91 - 26, this.scaledHeight - 19, tickDelta, player, this.selectedArrow, seed);
            } else {
                this.renderHotbarItem(this.scaledWidth / 2 + 91 + 10, this.scaledHeight - 19, tickDelta, player, this.selectedArrow, seed);
            }
        }
    }
}
