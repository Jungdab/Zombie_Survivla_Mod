package me.jungdab.zsm.item;

import me.jungdab.zsm.registry.ModAdvancements;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class NightVisionDeviceItem extends Item {

    public NightVisionDeviceItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ((ServerPlayerEntity) user).getAdvancementTracker().grantCriterion(ModAdvancements.EQUIP_NIGHT_VISION_DEVICE, "equip_night_vision_device");
        return super.use(world,user,hand);
    }
}
