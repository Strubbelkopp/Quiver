package dev.strubbelkopp.simple_quiver.client;

import com.google.common.collect.ImmutableList;
import dev.strubbelkopp.simple_quiver.item.QuiverItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class QuiverEntityModel<T extends LivingEntity> extends AnimalModel<T> {

    private final ModelPart quiver;
    private final ModelPart arrow_1;
    private final ModelPart arrow_2;
    private final ModelPart arrow_3;

    public QuiverEntityModel(ModelPart root) {
        this.quiver = root.getChild("quiver");
        this.quiver.getChild("quiver_base");
        this.arrow_1 = this.quiver.getChild("arrow_1");
        this.arrow_2 = this.quiver.getChild("arrow_2");
        this.arrow_3 = this.quiver.getChild("arrow_3");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        
        ModelPartData quiver = modelPartData.addChild("quiver", ModelPartBuilder.create(),
                ModelTransform.pivot(-1.0F, 14.0F, 0.0F));

        quiver.addChild("quiver_base", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-2.0F, -6.0F, 2.0F, 4.0F, 12.0F, 3.0F), 
                ModelTransform.of(1.0F, -6.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

        quiver.addChild("arrow_1", ModelPartBuilder.create()
                .uv(6, 15).cuboid(-1.5F, -3.25F, 0.75F, 5.0F, 5.0F, 0.0F)
                .uv(6, 10).cuboid(1.0F, -3.25F, -1.75F, 0.0F, 5.0F, 5.0F),
                ModelTransform.of(-2.817F, -11.9378F, 3.75F, 0.0F, -2.9671F, -0.5236F));

        quiver.addChild("arrow_2", ModelPartBuilder.create()
                .uv(6, 15).cuboid(-3.5F, -4.5F, 0.25F, 5.0F, 5.0F, 0.0F)
                .uv(6, 10).cuboid(-1.0F, -4.5F, -2.25F, 0.0F, 5.0F, 5.0F),
                ModelTransform.of(-1.317F, -11.9378F, 3.75F, 0.0F, 0.5672F, -0.5236F));

        quiver.addChild("arrow_3", ModelPartBuilder.create()
                .uv(6, 15).cuboid(-3.0F, -3.0F, 0.0F, 5.0F, 5.0F, 0.0F)
                .uv(6, 10).cuboid(-0.5F, -3.0F, -2.5F, 0.0F, 5.0F, 5.0F),
                ModelTransform.of(-1.067F, -13.4378F, 2.75F, 0.0F, -0.3491F, -0.5236F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        QuiverItem.getQuiverItem(entity).ifPresent(quiver -> {
            float percentageFilled = QuiverItem.getAmountFilled(quiver);
            this.arrow_1.visible = percentageFilled >= 0.001;
            this.arrow_2.visible = percentageFilled >= 0.33;
            this.arrow_3.visible = percentageFilled >= 0.66;
        });
    }
    
    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.quiver);
    }
    
    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of();
    }
}
