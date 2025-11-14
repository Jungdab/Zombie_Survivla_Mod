package me.jungdab.zsm.entity;

import me.jungdab.zsm.client.animation.ZSMAnimations;
import me.jungdab.zsm.entity.ai.goal.BombZombieIgniteGoal;
import me.jungdab.zsm.entity.ai.goal.GlobalTargetGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BombZombieEntity extends ZSMBasicEntity implements GeoEntity {
    private static final TrackedData<Boolean> IGNITED = DataTracker.registerData(BombZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public int currentFuseTime;
    public boolean isThrow = false;
    public boolean isTargeting = false;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BombZombieEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);

        builder.add(IGNITED, false);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.putBoolean("ignited", this.isIgnited());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.getBoolean("ignited")) {
            this.ignite();
        }
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            if(this.isIgnited()) {
                this.getNavigation().stop();

                this.currentFuseTime++;
                if(this.currentFuseTime > 32) {
                    this.explode();
                }
            }
        }

        super.tick();
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);

        if(this.isThrow && this.getVelocity().length() < 0.5) this.explode();
    }

    private void explode() {
        if (!this.getWorld().isClient) {
            this.dead = true;
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 5f, World.ExplosionSourceType.MOB);
            this.onRemoval((ServerWorld) this.getWorld(), Entity.RemovalReason.KILLED);
            this.discard();
        }
    }


    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        return true;
    }

    public boolean isIgnited() {
        return this.dataTracker.get(IGNITED);
    }

    public void ignite() {
        this.dataTracker.set(IGNITED, true);
    }

    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new BombZombieIgniteGoal(this));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));

        this.targetSelector.add(1, new GlobalTargetGoal<>(this, TurretEntity.class, 10));
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                ZSMAnimations.genericWalkIdleController(this),
                explodeController(this),
                DefaultAnimations.genericDeathController(this)
        );
    }

    public void knockBackResistance() {
        EntityAttributeInstance instance = this.getAttributes().getCustomInstance(EntityAttributes.KNOCKBACK_RESISTANCE);
        if(instance != null) instance.setBaseValue(1.0f);
    }

    private  <T extends BombZombieEntity & GeoAnimatable> AnimationController<T> explodeController(T animatable) {
        return new AnimationController<T>(animatable, "Explode", 0, state -> animatable.isIgnited() ? state.setAndContinue(ZSMAnimations.EXPLODE) : PlayState.STOP);
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public static DefaultAttributeContainer.Builder createZombieAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY, 1.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.STEP_HEIGHT, 1.0);

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
