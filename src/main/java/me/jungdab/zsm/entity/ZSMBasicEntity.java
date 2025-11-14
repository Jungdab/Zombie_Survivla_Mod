package me.jungdab.zsm.entity;

import me.jungdab.zsm.item.ZombieBoneArmorItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class ZSMBasicEntity extends HostileEntity {
    private int deathTimer;

    protected ZSMBasicEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return 0;
    }

    protected void updatePostDeath() {
        this.deathTimer++;
        if (this.deathTimer >= 30 && !this.getWorld().isClient() && !this.isRemoved()) {
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public boolean canSee(Entity entity) {
        if (entity.getWorld() != this.getWorld()) {
            return false;
        } else {
            Vec3d vec3d = new Vec3d(this.getX(), this.getEyeY(), this.getZ());
            Vec3d vec3d2 = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
            return vec3d2.distanceTo(vec3d) > 128.0
                    ? false
                    : this.getWorld().raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, this)).getType()
                    == HitResult.Type.MISS;
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if(source.getAttacker() instanceof PlayerEntity player && ZombieBoneArmorItem.hasFullSuitOfArmorOn(player)) {
            if(amount > 0) amount *= 2f;
        }
        return super.damage(world, source, amount);
    }
}
