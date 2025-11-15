package me.jungdab.zsm.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class BossZombiePart extends Entity {
    public BossZombieEntity owner;
    private EntityDimensions partDimensions;

    public BossZombiePart(EntityType<?> t, World world) {
        super(t, world);
        world.spawnEntity(this);
    }

    public void setOwner(BossZombieEntity owner) {
        this.owner = owner;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isPartOf(Entity entity) {
        return this == entity || this.owner == entity;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return this.owner != null && this.owner.damage(world, source, amount);
    }


    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return this.partDimensions;
    }
    @Override
    public boolean shouldSave() {
        return false;
    }
}
