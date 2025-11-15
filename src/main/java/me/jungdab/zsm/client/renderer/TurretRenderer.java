package me.jungdab.zsm.client.renderer;

import me.jungdab.zsm.ZSM;
import me.jungdab.zsm.client.model.TurretModel;
import me.jungdab.zsm.entity.TurretEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class TurretRenderer extends GeoEntityRenderer<TurretEntity> {

    public TurretRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager,new TurretModel(Identifier.of(ZSM.MOD_ID, "turret")));
    }

    @Override
    public void actuallyRender(MatrixStack poseStack, TurretEntity animatable, BakedGeoModel model, @Nullable RenderLayer renderType,
                               VertexConsumerProvider bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int colour) {
        poseStack.push();

        LivingEntity livingEntity = animatable instanceof LivingEntity entity ? entity : null;
        boolean shouldSit = animatable.hasVehicle() && (animatable.getVehicle() != null);
        float lerpHeadRot = livingEntity == null ? 0 : MathHelper.lerpAngleDegrees(partialTick, livingEntity.prevHeadYaw, livingEntity.headYaw);


        if (animatable.getPose() == EntityPose.SLEEPING && livingEntity != null) {
            Direction bedDirection = livingEntity.getSleepingDirection();

            if (bedDirection != null) {
                float eyePosOffset = livingEntity.getEyeHeight(EntityPose.STANDING) - 0.1F;

                poseStack.translate(-bedDirection.getOffsetX() * eyePosOffset, 0, -bedDirection.getOffsetZ() * eyePosOffset);
            }
        }

        float nativeScale = livingEntity != null ? livingEntity.getScale() : 1;
        float ageInTicks = animatable.age + partialTick;
        float limbSwingAmount = 0;
        float limbSwing = 0;

        poseStack.scale(nativeScale, nativeScale, nativeScale);
        applyRotations(animatable, poseStack, ageInTicks, lerpHeadRot, partialTick, nativeScale);

        if (!shouldSit && animatable.isAlive() && livingEntity != null) {
            limbSwingAmount = livingEntity.limbAnimator.getSpeed(partialTick);
            limbSwing = livingEntity.limbAnimator.getPos(partialTick);

            if (livingEntity.isBaby())
                limbSwing *= 3f;

            if (limbSwingAmount > 1f)
                limbSwingAmount = 1f;
        }

        if (!isReRender) {
            float headPitch = MathHelper.lerp(partialTick, animatable.prevPitch, animatable.getPitch());
            float motionThreshold = getMotionAnimThreshold(animatable);
            Vec3d velocity = animatable.getVelocity();
            float avgVelocity = (float)(Math.abs(velocity.x) + Math.abs(velocity.z) / 2f);
            AnimationState<TurretEntity> animationState = new AnimationState<>(animatable, limbSwing, limbSwingAmount, partialTick, avgVelocity >= motionThreshold && limbSwingAmount != 0);
            long instanceId = getInstanceId(animatable);
            GeoModel<TurretEntity> currentModel = getGeoModel();

            animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
            animationState.setData(DataTickets.ENTITY, animatable);
            animationState.setData(DataTickets.ENTITY_MODEL_DATA, new EntityModelData(shouldSit, livingEntity != null && livingEntity.isBaby(), -lerpHeadRot, -headPitch));
            currentModel.addAdditionalStateData(animatable, instanceId, animationState::setData);
            currentModel.handleAnimations(animatable, instanceId, animationState, partialTick);
        }

        poseStack.translate(0, 0.01f, 0);

        this.modelRenderTranslations = new Matrix4f(poseStack.peek().getPositionMatrix());

        if (buffer != null) {
            for (GeoBone group : model.topLevelBones()) {
                renderRecursively(poseStack, animatable, group, renderType, bufferSource, buffer, isReRender, partialTick, packedLight,
                        packedOverlay, colour);
            }
        }

        poseStack.pop();
    }

    @Override
    public int getPackedOverlay(TurretEntity animatable, float u, float partialTick) {
        if(animatable.hurtTime > 0 || animatable.deathTime > 0) return OverlayTexture.getUv(0.3f, false);
        else return OverlayTexture.DEFAULT_UV;
    }
}
