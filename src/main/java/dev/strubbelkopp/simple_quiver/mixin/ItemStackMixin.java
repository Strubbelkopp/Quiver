package dev.strubbelkopp.simple_quiver.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow private int count;

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;contains(Ljava/lang/String;I)Z"))
    private void getCountAsShort(NbtCompound nbt, CallbackInfo ci) {
        this.count = nbt.getShort("Count");
    }

    @Inject(method = "writeNbt", at = @At(value = "TAIL"))
    public void writeCountAsShort(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        nbt.putShort("Count", (short) this.count);
    }
}
