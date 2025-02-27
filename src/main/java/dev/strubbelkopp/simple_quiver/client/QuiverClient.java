package dev.strubbelkopp.simple_quiver.client;

import dev.strubbelkopp.simple_quiver.Quiver;
import dev.strubbelkopp.simple_quiver.item.QuiverItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.DyeableItem;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class QuiverClient implements ClientModInitializer {

    private static KeyBinding ARROW_CYCLE_KEYBINDING;

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((DyeableItem)stack.getItem()).getColor(stack), Quiver.QUIVER);

        ModelPredicateProviderRegistry.register(Quiver.QUIVER, new Identifier("filled"), ((stack, world, entity, seed) -> QuiverItem.getAmountFilled(stack)));

        EntityModelLayerRegistry.registerModelLayer(QuiverFeatureRenderer.QUIVER, QuiverEntityModel::getTexturedModelData);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(((entityType, entityRenderer, registrationHelper, context) ->
                registrationHelper.register(new QuiverFeatureRenderer<>(entityRenderer, context.getModelLoader()))));

        ARROW_CYCLE_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + Quiver.MOD_ID + ".arrow_cycle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category." + Quiver.MOD_ID
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> ((IMouse) client.mouse).quiver$setArrowCycleState(ARROW_CYCLE_KEYBINDING.isPressed()));
    }
}
