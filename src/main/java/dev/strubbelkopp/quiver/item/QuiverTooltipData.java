package dev.strubbelkopp.quiver.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class QuiverTooltipData implements TooltipData {

    private final DefaultedList<ItemStack> inventory;
    private final int quiverOccupancy;
    private final int activeArrowIndex;

    public QuiverTooltipData(DefaultedList<ItemStack> inventory, int quiverOccupancy, int activeArrowIndex) {
        this.inventory = inventory;
        this.quiverOccupancy = quiverOccupancy;
        this.activeArrowIndex = activeArrowIndex;
    }

    public DefaultedList<ItemStack> getInventory() {
        return this.inventory;
    }

    public int getQuiverOccupancy() {
        return this.quiverOccupancy;
    }

    public int getActiveArrowIndex() {
        return this.activeArrowIndex;
    }
}
