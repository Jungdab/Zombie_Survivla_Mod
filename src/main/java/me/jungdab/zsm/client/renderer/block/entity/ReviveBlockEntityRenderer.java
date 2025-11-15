package me.jungdab.zsm.client.renderer.block.entity;

import com.mojang.authlib.GameProfile;
import me.jungdab.zsm.block.entity.ReviveBlockEntity;
import me.jungdab.zsm.client.network.ZSMClientPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ReviveBlockEntityRenderer implements BlockEntityRenderer<ReviveBlockEntity> {

    public ReviveBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(ReviveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        UUID uuid = entity.getUuid();

        matrices.push();

        matrices.translate(1.5f, 0.125f, 0.5f);

        ZSMClientPlayerEntity playerEntity = new ZSMClientPlayerEntity((ClientWorld) entity.getWorld(), new GameProfile(uuid, ""));

        playerEntity.setPose(EntityPose.SLEEPING);
        playerEntity.prevHeadYaw = 25;
        playerEntity.setHeadYaw(25);

        EntityRenderer entityRenderer = client.getEntityRenderDispatcher().getRenderer(playerEntity);
        entityRenderer.render(client.getEntityRenderDispatcher().getRenderer(playerEntity).getAndUpdateRenderState(playerEntity, tickDelta),
                        matrices,
                        vertexConsumers,
                        light
        );

        matrices.pop();
    }
}
