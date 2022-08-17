package dev.strubbelkopp.quiver.client;

import dev.strubbelkopp.quiver.Quiver;
import dev.strubbelkopp.quiver.item.QuiverItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.DyeableItem;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class QuiverClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((DyeableItem)stack.getItem()).getColor(stack), Quiver.QUIVER);

        ModelPredicateProviderRegistry.register(Quiver.QUIVER, new Identifier("filled"), ((stack, world, entity, seed) -> QuiverItem.getAmountFilled(stack)));

        EntityModelLayerRegistry.registerModelLayer(QuiverFeatureRenderer.QUIVER, QuiverEntityModel::getTexturedModelData);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(((entityType, entityRenderer, registrationHelper, context) ->
                registrationHelper.register(new QuiverFeatureRenderer<>(entityRenderer, context.getModelLoader()))));
    }
}
