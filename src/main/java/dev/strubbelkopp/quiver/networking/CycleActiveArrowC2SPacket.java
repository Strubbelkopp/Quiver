package dev.strubbelkopp.quiver.networking;

import dev.strubbelkopp.quiver.Quiver;
import dev.strubbelkopp.quiver.item.QuiverItem;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CycleActiveArrowC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        QuiverItem.getQuiverItem(player).ifPresent(quiver -> {
            if (QuiverItem.cycleActiveArrow(quiver, buf.getBoolean(0))) {
                QuiverItem.getSelectedArrow(quiver).ifPresent(selectedArrow -> player
                        .sendMessage(Text.translatable(Quiver.QUIVER.getTranslationKey() + ".selected_arrow")
                                .append(Text.translatable(selectedArrow.getTranslationKey())), true));
            }
        });
    }
}
