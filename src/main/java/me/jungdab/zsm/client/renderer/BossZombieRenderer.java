package me.jungdab.zsm.client.renderer;

import me.jungdab.zsm.ZSM;
import me.jungdab.zsm.client.model.BossZombieModel;
import me.jungdab.zsm.entity.BossZombieEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class BossZombieRenderer extends GeoEntityRenderer<BossZombieEntity> {
    public BossZombieRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new BossZombieModel(Identifier.of(ZSM.MOD_ID, "boss_zombie")));
    }

    protected boolean canBeCulled(BossZombieEntity bossZombieEntity) {
        return false;
    }
}
