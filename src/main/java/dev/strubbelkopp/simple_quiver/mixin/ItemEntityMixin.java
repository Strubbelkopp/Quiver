package dev.strubbelkopp.simple_quiver.mixin;

import dev.strubbelkopp.simple_quiver.Quiver;
import dev.strubbelkopp.simple_quiver.item.QuiverItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow private int pickupDelay;
    @Shadow private UUID owner;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract ItemStack getStack();

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;getStack()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    public void collectArrowToQuiver(PlayerEntity player, CallbackInfo ci) {
        if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUuid()))) {
            ItemStack stack = this.getStack();
            if (stack.isIn(Quiver.ARROWS)) {
                QuiverItem.getQuiverItem(player).ifPresent(quiver -> {
                    int addedItemCount = QuiverItem.addToQuiver(quiver, stack);
                    if (addedItemCount > 0) {
                        player.sendPickup((ItemEntity)(Object)this, 0);
                        player.increaseStat(Stats.PICKED_UP.getOrCreateStat(stack.getItem()), addedItemCount);
                        player.triggerItemPickedUpByEntityCriteria((ItemEntity)(Object)this);
                        stack.decrement(addedItemCount);
                        if (stack.isEmpty()) {
                            discard();
                            ci.cancel();
                        }
                    }
                });
            }
        }
    }
}
