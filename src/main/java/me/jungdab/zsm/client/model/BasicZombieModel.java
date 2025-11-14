package me.jungdab.zsm.client.model;

import me.jungdab.zsm.ZSM;
import me.jungdab.zsm.entity.BasicZombieEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoRenderer;

public class BasicZombieModel extends GeoModel<BasicZombieEntity> {
    @Override
    public Identifier getModelResource(BasicZombieEntity basicZombieEntity, @Nullable GeoRenderer<BasicZombieEntity> geoRenderer) {
        return Identifier.of(ZSM.MOD_ID, "geo/zombie.geo.json");
    }

    @Override
    public Identifier getTextureResource(BasicZombieEntity basicZombieEntity, @Nullable GeoRenderer<BasicZombieEntity> geoRenderer) {
        return Identifier.of(ZSM.MOD_ID, "textures/entity/zombie.png");
    }

    @Override
    public Identifier getAnimationResource(BasicZombieEntity basicZombieEntity) {
        return Identifier.of(ZSM.MOD_ID, "animations/zombie.animation.json");
    }

    @Override
    public void setCustomAnimations(BasicZombieEntity animatable, long instanceId, AnimationState<BasicZombieEntity> animationState) {
        GeoBone head = this.getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * 0.017453292F);
            head.setRotY(entityData.netHeadYaw() * 0.017453292F);
        }
    }
}
