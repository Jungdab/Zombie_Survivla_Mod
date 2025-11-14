package me.jungdab.zsm.item;

import me.jungdab.zsm.entity.TurretEntity;
import me.jungdab.zsm.registry.ModEntities;
import me.jungdab.zsm.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.function.Consumer;

public class TurretItem extends Item {
    public TurretItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Direction direction = context.getSide();
        if (direction != Direction.UP) {
            return ActionResult.FAIL;
        } else {
            World world = context.getWorld();
            ItemPlacementContext itemPlacementContext = new ItemPlacementContext(context);
            BlockPos blockPos = itemPlacementContext.getBlockPos();
            ItemStack itemStack = context.getStack();
            Vec3d vec3d = Vec3d.ofBottomCenter(blockPos);
            Box box = ModEntities.TURRET.getDimensions().getBoxAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());
            if (world.isSpaceEmpty(null, box) && world.getOtherEntities(null, box).isEmpty()) {
                if (world instanceof ServerWorld serverWorld) {
                    Consumer<TurretEntity> consumer = EntityType.copier(serverWorld, itemStack, context.getPlayer());
                    TurretEntity turretEntity = ModEntities.TURRET.create(serverWorld, consumer, blockPos, SpawnReason.SPAWN_ITEM_USE, true, true);
                    if (turretEntity == null) {
                        return ActionResult.FAIL;
                    }

                    turretEntity.headYaw = (MathHelper.wrapDegrees(context.getPlayerYaw() - 180.0F) + 22.5F) / 45.0F * 45.0F;

                    serverWorld.spawnEntityAndPassengers(turretEntity);
                    world.playSound(
                            null, turretEntity.getX(), turretEntity.getY(), turretEntity.getZ(), ModSounds.ENTITY_TURRET_SPAWN, SoundCategory.BLOCKS, 1F, 1F
                    );
                    turretEntity.emitGameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
                }

                itemStack.decrement(1);
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.FAIL;
            }
        }
    }
}
