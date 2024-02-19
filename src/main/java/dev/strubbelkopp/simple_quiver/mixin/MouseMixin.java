package dev.strubbelkopp.simple_quiver.mixin;

import dev.strubbelkopp.simple_quiver.Quiver;
import dev.strubbelkopp.simple_quiver.item.QuiverItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow private double eventDeltaWheel;
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void cycleSelectedArrow(long window, double horizontal, double vertical, CallbackInfo ci, double d) {
        ClientPlayerEntity player = this.client.player;
        if (player != null && player.isSneaking()) {
            if (QuiverItem.getQuiverItem(player).isPresent()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(getScrollDirection(d));
                ClientPlayNetworking.send(Quiver.CYCLE_ACTIVE_ARROW_PACKET_ID, buf);
                ci.cancel();
            }
        }
    }

    // No idea what this does, but it works
    @Unique
    private boolean getScrollDirection(double d) {
        if (this.eventDeltaWheel != 0.0 && Math.signum(d) != Math.signum(this.eventDeltaWheel)) {
            this.eventDeltaWheel = 0.0;
        }
        this.eventDeltaWheel += d;
        int scrollAmount = (int)this.eventDeltaWheel;
        return Math.signum(scrollAmount) > 0;
    }
}
