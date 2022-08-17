package dev.strubbelkopp.quiver.mixin;

import dev.strubbelkopp.quiver.item.QuiverItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @Inject(method = "loadProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;split(I)Lnet/minecraft/item/ItemStack;"))
    private static void removeArrowFromQuiver(LivingEntity user, ItemStack crossbow, ItemStack projectile, boolean simulated, boolean creative, CallbackInfoReturnable<Boolean> cir) {
        Predicate<ItemStack> predicate = ((RangedWeaponItem)crossbow.getItem()).getHeldProjectiles();
        ItemStack offhandArrowItemStack = RangedWeaponItem.getHeldProjectile(user, predicate);
        if (offhandArrowItemStack.isEmpty()) {
            QuiverItem.getQuiverItem(user).ifPresent(QuiverItem::removeOneSelectedArrow);
        }
    }
}