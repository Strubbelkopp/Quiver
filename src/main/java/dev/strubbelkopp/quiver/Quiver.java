package dev.strubbelkopp.quiver;

import dev.strubbelkopp.quiver.item.QuiverItem;
import dev.strubbelkopp.quiver.networking.CycleActiveArrowC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Quiver implements ModInitializer {

    public static final String MOD_ID = "quiver";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item QUIVER = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "quiver"),
            new QuiverItem(new FabricItemSettings().group(ItemGroup.COMBAT).maxCount(1)));

    public static final TagKey<Item> ARROWS = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "arrows"));

    public static final Identifier CYCLE_ACTIVE_ARROW_PACKET_ID = new Identifier(MOD_ID, "cycle_active_arrow_packet");

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(CYCLE_ACTIVE_ARROW_PACKET_ID, CycleActiveArrowC2SPacket::receive);
    }
}
