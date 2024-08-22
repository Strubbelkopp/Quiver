package dev.strubbelkopp.simple_quiver.integration.trinkets;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import dev.strubbelkopp.simple_quiver.Quiver;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Optional;

public class Trinkets {
    public static Optional<ItemStack> getQuiverTrinket(LivingEntity user) {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(user).orElse(null);
            if (trinketComponent != null) {
                List<Pair<SlotReference, ItemStack>> quiverTrinkets = trinketComponent.getEquipped(Quiver.QUIVER);
                if (!quiverTrinkets.isEmpty()) {
                    return Optional.of(quiverTrinkets.get(0).getRight());
                }
            }
        }
        return Optional.empty();
    }
}
