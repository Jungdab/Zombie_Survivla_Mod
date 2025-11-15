package me.jungdab.zsm.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

public class GlobalTargetGoal<T extends LivingEntity> extends Goal {
    protected final MobEntity mob;
    private LivingEntity targetEntity;
    protected final Class<T> targetClass;

    private final int reciprocalChance;

    public GlobalTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocalChance) {
        this.mob = mob;
        this.targetClass = targetClass;
        this.reciprocalChance = reciprocalChance;
    }

    public boolean shouldContinue() {
        this.findClosestTarget();

        if(this.targetEntity == null) return false;

        this.mob.setTarget(this.targetEntity);
        LivingEntity livingEntity = this.mob.getTarget();

        if (!this.mob.canTarget(livingEntity)) {
            return false;
        } else {
            this.mob.setTarget(livingEntity);
            return true;
        }
    }

    public boolean canStart() {
        if (this.mob.getRandom().nextInt(this.reciprocalChance) != 0) return false;

        this.findClosestTarget();
        return this.targetEntity != null;
    }

    protected void findClosestTarget() {
        if(!(this.mob.getWorld() instanceof ServerWorld world)) return;
        LivingEntity entity = world.getClosestEntity(this.targetClass, TargetPredicate.createAttackable(), this.mob, this.mob.getX(), this.mob.getX(), this.mob.getX(), this.getSearchBox(200));
        LivingEntity entity1 = this.mob.getWorld().getClosestPlayer(this.mob.getX(), this.mob.getY(), this.mob.getZ(), 200, true);

        if(entity == null && entity1 == null) this.targetEntity = null;
        else if (entity == null) this.targetEntity = entity1;
        else if (entity1 == null) this.targetEntity = entity;
        else this.targetEntity = (this.mob.squaredDistanceTo(entity) < this.mob.squaredDistanceTo(entity1)) ? entity : entity1;
    }

    protected Box getSearchBox(double distance) {
        return this.mob.getBoundingBox().expand(distance, 4.0, distance);
    }

    public void start() {
        this.mob.setTarget(this.targetEntity);
    }

    public void stop() {
        this.mob.setTarget(null);
        this.targetEntity = null;
    }
}
