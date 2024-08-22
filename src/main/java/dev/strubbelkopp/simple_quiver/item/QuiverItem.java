package dev.strubbelkopp.simple_quiver.item;

import dev.strubbelkopp.simple_quiver.Quiver;
import dev.strubbelkopp.simple_quiver.integration.trinkets.Trinkets;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class QuiverItem extends Item implements DyeableItem, Equipment {

    private static final String ITEMS_KEY = "Arrows";
    private static final String ACTIVE_SLOT_KEY = "Active Slot";
    public static final int MAX_STORAGE = 256;
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);
    private static final int DEFAULT_COLOR = 0xcc7b46;

    public QuiverItem(Item.Settings settings) {
        super(settings);
    }

    public static Optional<ItemStack> getQuiverItem(LivingEntity user) {
        if (user != null) {
            Optional<ItemStack> quiverTrinket = Trinkets.getQuiverTrinket(user);
            if (quiverTrinket.isPresent()) {
                return quiverTrinket;
            }
            for (ItemStack armorItem : user.getArmorItems()) {
                if (armorItem.getItem() instanceof QuiverItem) {
                    return Optional.of(armorItem);
                }
            }
        }
        return Optional.empty();
    }

    public static boolean cycleActiveArrow(ItemStack quiver, boolean forward) {
        NbtCompound quiverNbtCompound = quiver.getNbt();
        if (quiverNbtCompound != null && quiverNbtCompound.contains(ITEMS_KEY)) {
            int occupiedSlots = quiverNbtCompound.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE).size();
            if (occupiedSlots > 1) {
                int activeSlot = quiverNbtCompound.getInt(ACTIVE_SLOT_KEY);
                int newSlot = (forward) ? (activeSlot + 1) % occupiedSlots : Math.floorMod(activeSlot - 1, occupiedSlots);
                quiverNbtCompound.putInt(ACTIVE_SLOT_KEY, newSlot);
                return true;
            }
        }
        return false;
    }

    public static Optional<ItemStack> getSelectedArrow(ItemStack quiver) {
        NbtCompound quiverNbtCompound = quiver.getNbt();
        if (quiverNbtCompound != null && quiverNbtCompound.contains(ITEMS_KEY)) {
            NbtList itemList = quiverNbtCompound.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
            int activeSlot = quiverNbtCompound.getInt(ACTIVE_SLOT_KEY);
            return Optional.of(ItemStack.fromNbt(itemList.getCompound(activeSlot)));
        }
        return Optional.empty();
    }

    public static void removeOneSelectedArrow(ItemStack quiver) {
        NbtCompound quiverNbtCompound = quiver.getNbt();
        if (quiverNbtCompound != null && quiverNbtCompound.contains(ITEMS_KEY)) {
            int activeSlot = quiverNbtCompound.getInt(ACTIVE_SLOT_KEY);
            NbtList itemList = quiverNbtCompound.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
            NbtCompound activeArrowNbtCompound = itemList.getCompound(activeSlot);
            ItemStack activeArrowStack = ItemStack.fromNbt(activeArrowNbtCompound);
            activeArrowStack.decrement(1);
            activeArrowStack.writeNbt(activeArrowNbtCompound);
            itemList.remove(activeArrowNbtCompound);
            if (activeArrowStack.getCount() > 0) {
                itemList.add(activeSlot, activeArrowNbtCompound);
            } else if (activeSlot > 0) {
                quiverNbtCompound.putInt(ACTIVE_SLOT_KEY, activeSlot - 1);
            }
            if (itemList.isEmpty()) {
                quiver.removeSubNbt(ACTIVE_SLOT_KEY);
                quiver.removeSubNbt(ITEMS_KEY);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack quiver = player.getStackInHand(hand);
        if (dropAllBundledItems(quiver, player)) {
            this.playDropContentsSound(player);
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(quiver, world.isClient());
        }
        return TypedActionResult.fail(quiver);
    }

    @Override
    public boolean onStackClicked(ItemStack quiver, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                removeFirstStack(quiver).ifPresent(removedStack -> {
                    this.playRemoveOneSound(player);
                    addToQuiver(quiver, slot.insertStack(removedStack));
                });
            } else if (stack.isIn(Quiver.ARROWS) && stack.getItem().canBeNested()) {
                int maxAddedItemCount = (MAX_STORAGE - getQuiverOccupancy(quiver)) / getItemOccupancy(stack);
                int addedItemCount = addToQuiver(quiver, slot.takeStackRange(stack.getCount(), maxAddedItemCount, player));
                if (addedItemCount > 0) {
                    this.playInsertSound(player);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack quiver, ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
            if (stack.isEmpty()) {
                removeFirstStack(quiver).ifPresent(removedStack -> {
                    this.playRemoveOneSound(player);
                    cursorStackReference.set(removedStack);
                });
            } else if (stack.isIn(Quiver.ARROWS) && stack.getItem().canBeNested()) {
                int addedItemCount = addToQuiver(quiver, stack);
                if (addedItemCount > 0) {
                    this.playInsertSound(player);
                    stack.decrement(addedItemCount);
                }
            }
            return true;
        }
        return false;
    }

    public static int addToQuiver(ItemStack quiver, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem().canBeNested()) {
            NbtCompound quiverNbtCompound = quiver.getOrCreateNbt();
            int addedItemCount = Math.min(stack.getCount(), (MAX_STORAGE - getQuiverOccupancy(quiver)) / getItemOccupancy(stack));
            if (addedItemCount > 0) {
                if (!quiverNbtCompound.contains(ITEMS_KEY)) {
                    quiverNbtCompound.put(ITEMS_KEY, new NbtList());
                }
                NbtList itemList = quiverNbtCompound.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
                canMergeStack(stack, itemList).ifPresentOrElse(pair -> {
                    int i = pair.getLeft();
                    NbtCompound itemNbtCompound = pair.getRight();
                    ItemStack matchingStack = ItemStack.fromNbt(itemNbtCompound);
                    matchingStack.increment(addedItemCount);
                    matchingStack.writeNbt(itemNbtCompound);
                    itemList.remove(itemNbtCompound);
                    itemList.add(i, itemNbtCompound);
                }, () -> {
                    if (!itemList.isEmpty()) {
                        int activeSlot = quiverNbtCompound.getInt(ACTIVE_SLOT_KEY);
                        quiverNbtCompound.putInt(ACTIVE_SLOT_KEY, activeSlot + 1);
                    } else {
                        quiverNbtCompound.putInt(ACTIVE_SLOT_KEY, 0);
                    }
                    ItemStack itemStack = stack.copy();
                    itemStack.setCount(addedItemCount);
                    NbtCompound itemNbtCompound = new NbtCompound();
                    itemStack.writeNbt(itemNbtCompound);
                    itemList.add(0, itemNbtCompound);
                });
                return addedItemCount;
            }
        }
        return 0;
    }

    private static Optional<Pair<Integer, NbtCompound>> canMergeStack(ItemStack stack, NbtList itemList) {
        return stack.isOf(Quiver.QUIVER)
            ? Optional.empty()
            : itemList.stream()
                .filter(NbtCompound.class::isInstance)
                .map(NbtCompound.class::cast)
                .filter(nestedItemStack -> ItemStack.canCombine(ItemStack.fromNbt(nestedItemStack), stack))
                .map(nbtElement -> new Pair<>(itemList.indexOf(nbtElement), nbtElement))
                .findFirst();
    }

    private static int getItemOccupancy(ItemStack stack) {
        return Item.DEFAULT_MAX_COUNT / stack.getMaxCount();
    }

    private static int getQuiverOccupancy(ItemStack quiver) {
        return getBundledStacks(quiver).mapToInt(itemStack -> getItemOccupancy(itemStack) * itemStack.getCount()).sum();
    }

    private Optional<ItemStack> removeFirstStack(ItemStack quiver) {
        NbtCompound quiverNbtCompound = quiver.getOrCreateNbt();
        if (quiverNbtCompound.contains(ITEMS_KEY)) {
            NbtList itemList = quiverNbtCompound.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
            if (!itemList.isEmpty()) {
                NbtCompound itemNbtCompound = itemList.getCompound(0);
                ItemStack itemStack = ItemStack.fromNbt(itemNbtCompound);
                int count = itemStack.getCount();
                if (count > Item.DEFAULT_MAX_COUNT) {
                    itemStack.decrement(Item.DEFAULT_MAX_COUNT);
                    itemStack.writeNbt(itemNbtCompound);
                    itemList.remove(itemNbtCompound);
                    itemList.add(0, itemNbtCompound);
                } else {
                    itemList.remove(0);
                }
                itemStack.setCount(Math.min(count, Item.DEFAULT_MAX_COUNT));
                int activeSlot = quiverNbtCompound.getInt(ACTIVE_SLOT_KEY);
                if (itemList.isEmpty()) {
                    quiver.removeSubNbt(ACTIVE_SLOT_KEY);
                    quiver.removeSubNbt(ITEMS_KEY);
                } else if (activeSlot > 0) {
                    quiverNbtCompound.putInt(ACTIVE_SLOT_KEY, activeSlot - 1);
                }
                return Optional.of(itemStack);
            }
        }
        return Optional.empty();
    }

    private static boolean dropAllBundledItems(ItemStack quiver, PlayerEntity player) {
        NbtCompound quiverNbtCompound = quiver.getOrCreateNbt();
        if (quiverNbtCompound.contains(ITEMS_KEY)) {
            if (player instanceof ServerPlayerEntity) {
                NbtList itemList = quiverNbtCompound.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
                for(int i = 0; i < itemList.size(); ++i) {
                    NbtCompound itemNbtCompound = itemList.getCompound(i);
                    ItemStack itemStack = ItemStack.fromNbt(itemNbtCompound);
                    player.dropItem(itemStack, true);
                }
                quiver.removeSubNbt(ACTIVE_SLOT_KEY);
                quiver.removeSubNbt(ITEMS_KEY);
                return true;
            }
        }
        return false;
    }

    private static Stream<ItemStack> getBundledStacks(ItemStack quiver) {
        NbtCompound quiverNbtCompound = quiver.getNbt();
        if (quiverNbtCompound != null && quiverNbtCompound.contains(ITEMS_KEY)) {
            NbtList itemList = quiverNbtCompound.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
            return itemList.stream().map(NbtCompound.class::cast).map(ItemStack::fromNbt);
        }
        return Stream.empty();
    }

    public static float getAmountFilled(ItemStack stack) {
        return (float)getQuiverOccupancy(stack) / MAX_STORAGE;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack quiver) {
        DefaultedList<ItemStack> itemList = DefaultedList.of();
        getBundledStacks(quiver).forEach(itemList::add);
        int activeSlot = quiver.getOrCreateNbt().getInt(ACTIVE_SLOT_KEY);
        return Optional.of(new QuiverTooltipData(itemList, getQuiverOccupancy(quiver), activeSlot));
    }

    @Override
    public void appendTooltip(ItemStack quiver, World world, List<Text> tooltip, TooltipContext context) {
        getSelectedArrow(quiver).ifPresent(selectedArrow -> tooltip
               .add(Text.translatable(this.getTranslationKey() + ".selected_arrow")
               .append(Text.translatable(selectedArrow.getTranslationKey()))
               .formatted(Formatting.GRAY)));
        tooltip.add(Text.translatable(this.getTranslationKey() + ".fullness", getQuiverOccupancy(quiver), MAX_STORAGE)
               .formatted(Formatting.GRAY));
    }

    @Override
    public boolean isItemBarVisible(ItemStack quiver) {
        return getQuiverOccupancy(quiver) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack quiver) {
        return Math.min(1 + 12 * getQuiverOccupancy(quiver) / MAX_STORAGE, 13);
    }

    @Override
    public int getItemBarColor(ItemStack quiver) {
        return ITEM_BAR_COLOR;
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ItemUsage.spawnItemContents(entity, getBundledStacks(entity.getStack()));
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    @Override
    public int getColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt(DISPLAY_KEY);
        if (nbtCompound != null && nbtCompound.contains(COLOR_KEY, NbtElement.NUMBER_TYPE)) {
            return nbtCompound.getInt(COLOR_KEY);
        }
        return DEFAULT_COLOR;
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.CHEST;
    }
}
