package dev.strubbelkopp.simple_quiver.mixin;

import dev.strubbelkopp.simple_quiver.item.QuiverItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(BowItem.class)
public class BowItemMixin {
    @Inject(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    public void removeArrowFromQuiver(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        Predicate<ItemStack> predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
        ItemStack offhandArrowItemStack = RangedWeaponItem.getHeldProjectile(user, predicate);
        if (offhandArrowItemStack.isEmpty()) {
            QuiverItem.getQuiverItem(user).ifPresent(QuiverItem::removeOneSelectedArrow);
        }
    }
}
