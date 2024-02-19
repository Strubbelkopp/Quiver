package dev.strubbelkopp.simple_quiver;

import dev.strubbelkopp.simple_quiver.item.QuiverItem;
import dev.strubbelkopp.simple_quiver.networking.CycleActiveArrowC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Quiver implements ModInitializer {

    public static final String MOD_ID = "simple_quiver";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    public static final Item QUIVER = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "quiver"),
            new QuiverItem(new FabricItemSettings().maxCount(1)));

    public static final TagKey<Item> ARROWS = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "arrows"));

    public static final Identifier CYCLE_ACTIVE_ARROW_PACKET_ID = new Identifier(MOD_ID, "cycle_active_arrow_packet");

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(itemGroup -> itemGroup.addAfter(Items.CROSSBOW, QUIVER));
        ServerPlayNetworking.registerGlobalReceiver(CYCLE_ACTIVE_ARROW_PACKET_ID, CycleActiveArrowC2SPacket::receive);
    }
}
