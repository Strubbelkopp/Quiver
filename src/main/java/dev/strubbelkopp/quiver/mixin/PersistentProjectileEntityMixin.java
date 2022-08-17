package dev.strubbelkopp.quiver.mixin;

import dev.strubbelkopp.quiver.item.QuiverItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends Entity {

    @Shadow public PersistentProjectileEntity.PickupPermission pickupType;

    public PersistentProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow protected abstract ItemStack asItemStack();

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;tryPickup(Lnet/minecraft/entity/player/PlayerEntity;)Z"), cancellable = true)
    public void collectArrowToQuiver(PlayerEntity player, CallbackInfo ci) {
        if (this.pickupType.equals(PersistentProjectileEntity.PickupPermission.ALLOWED)) {
            QuiverItem.getQuiverItem(player).ifPresent(quiver -> {
                int addedItemCount = QuiverItem.addToQuiver(quiver, this.asItemStack());
                if (addedItemCount > 0) {
                    player.sendPickup(this, 0);
                    discard();
                    ci.cancel();
                }
            });
        }
    }
}
