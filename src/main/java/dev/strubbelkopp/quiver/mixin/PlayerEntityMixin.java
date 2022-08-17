package dev.strubbelkopp.quiver.mixin;

import dev.strubbelkopp.quiver.item.QuiverItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "getArrowType", at = @At("HEAD"), cancellable = true)
    public void getArrowType(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!(stack.getItem() instanceof RangedWeaponItem)) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else {
            Predicate<ItemStack> predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
            ItemStack offhandArrowItemStack = RangedWeaponItem.getHeldProjectile((PlayerEntity)(Object)this, predicate);
            if (offhandArrowItemStack.isEmpty()) {
                QuiverItem.getQuiverItem((PlayerEntity)(Object)this).flatMap(QuiverItem::getSelectedArrow).ifPresent(cir::setReturnValue);
            }
        }
    }
}