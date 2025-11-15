package me.jungdab.zsm.entity;

import me.jungdab.zsm.entity.ai.goal.GlobalTargetGoal;
import me.jungdab.zsm.registry.ModEffects;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import static software.bernie.geckolib.constant.DefaultAnimations.IDLE;
import static software.bernie.geckolib.constant.DefaultAnimations.WALK;

public class HammerZombieEntity extends ZSMBasicEntity implements GeoEntity {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final TrackedData<Integer> ANIMATION_ID = DataTracker.registerData(HammerZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);

    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ANIMATION_ID, 0);
    }
    public void setAnimationId(int id) {
        this.dataTracker.set(ANIMATION_ID, id);
    }
    public int getAnimationId() {
        return this.dataTracker.get(ANIMATION_ID);
    }
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("AnimationId", this.getAnimationId());
    }
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if(nbt.contains("AnimationId")) this.setAnimationId(nbt.getInt("AnimationId"));
    }

    public HammerZombieEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new HammerZombieAttackGoal());
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));

        this.targetSelector.add(1, new GlobalTargetGoal<>(this, TurretEntity.class, 10));
    }

    public void swingAttack(LivingEntity target) {
        if(!(this.getWorld() instanceof ServerWorld world)) return;

        if(target.squaredDistanceTo(this) > 16) return;

        DamageSource source = this.getDamageSources().mobAttack(this);
        float f = (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        target.damage(world, source, f);
        target.addStatusEffect(new StatusEffectInstance(ModEffects.STUN, 20, 0));
    }

    public static DefaultAttributeContainer.Builder createZombieAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY, 1.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.STEP_HEIGHT, 1.0);

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers
                .add(animationController(this))
                .add(DefaultAnimations.genericDeathController(this));
    }

    public static AnimationController<HammerZombieEntity> animationController(HammerZombieEntity animatable) {
        return new AnimationController<>(animatable, "Animation", 5, state -> {
            switch (animatable.getAnimationId()) {
                case 0 -> {
                    return state.setAndContinue(state.isMoving() ? WALK : IDLE);
                }
                case 1 -> {
                    return state.setAndContinue(RawAnimation.begin().thenPlay("attack.swing"));
                }
            }

            return PlayState.CONTINUE;
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    class HammerZombieAttackGoal extends Goal {
        private final HammerZombieEntity mob;

        private int progressTick = 0;
        private boolean isAttack = false;

        HammerZombieAttackGoal() {
            this.mob = HammerZombieEntity.this;
        }

        public boolean canStart() {
            LivingEntity target = this.mob.getTarget();
            if(target == null) return false;
            else if(!target.isAlive()) return false;
            else return true;
        }

        public boolean shouldContinue() {
            if(isAttack) return true;

            LivingEntity target = this.mob.getTarget();

            if (target == null) return false;
            else if (!target.isAlive()) return false;
            return true;
        }

        public void tick() {
            LivingEntity target = this.mob.getTarget();

            if(!isAttack) {
                if(target != null) this.mob.getNavigation().startMovingTo(target, 1.0);

                double distance = this.mob.squaredDistanceTo(target);
                if(distance < 9) {
                    this.mob.getNavigation().stop();
                    isAttack = true;
                    progressTick = 0;
                }
            }
            else {
                attack();
            }
        }

        private void attack() {
            LivingEntity target = this.mob.getTarget();

            if(progressTick == 0) this.mob.setAnimationId(1);
            if(progressTick == 8 && target != null) this.mob.swingAttack(target);
            if(progressTick == 17) {
                this.mob.setAnimationId(0);
                isAttack = false;
            }

            if(target != null) this.mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());

            progressTick++;
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }
    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_ZOMBIE_STEP;
    }
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }
}
