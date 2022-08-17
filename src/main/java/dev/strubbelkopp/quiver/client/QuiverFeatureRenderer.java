package dev.strubbelkopp.quiver.client;

import dev.strubbelkopp.quiver.Quiver;
import dev.strubbelkopp.quiver.item.QuiverItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class QuiverFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public static final EntityModelLayer QUIVER = new EntityModelLayer(new Identifier(Quiver.MOD_ID, "quiver"), "main");
    private final QuiverEntityModel<T> quiverEntityModel;

    public QuiverFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader) {
        super(context);
        this.quiverEntityModel = new QuiverEntityModel<>(loader.getModelPart(QUIVER));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        QuiverItem.getQuiverItem(entity).ifPresent(quiver -> {

            int colour = ((QuiverItem) quiver.getItem()).getColor(quiver);
            float red = (float) (colour >> 16 & 0xFF) / 255.0F;
            float green = (float) (colour >> 8 & 0xFF) / 255.0F;
            float blue = (float) (colour & 0xFF) / 255.0F;

            matrices.push();

            this.getContextModel().copyStateTo(this.quiverEntityModel);
            this.quiverEntityModel.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);

            if (entity.isInSneakingPose() && !this.getContextModel().riding && !entity.isSwimming()) {
                matrices.translate(0.0F, 0.2F, 0.0F);
                if (this.getContextModel() instanceof BipedEntityModel<?> bipedEntityModel) {
                    matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(bipedEntityModel.body.pitch));
                }
            }

            this.renderQuiverModel(matrices, vertexConsumers, light, red, green, blue, false);
            this.renderQuiverModel(matrices, vertexConsumers, light, 1.0F, 1.0F, 1.0F, true);

            matrices.pop();
        });
    }

    private void renderQuiverModel(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float red, float green, float blue, boolean overlay) {
        VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(getQuiverTexture(overlay)), false, false);
        this.quiverEntityModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, red, green, blue, 1.0F);
    }

    private Identifier getQuiverTexture(boolean overlay) {
        return new Identifier(Quiver.MOD_ID, "textures/models/quiver/quiver" + (overlay ? "_overlay" : "") + ".png");
    }
}
