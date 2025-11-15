package me.jungdab.zsm.entity;

import me.jungdab.zsm.entity.ai.goal.AdvancedTargetGoal;
import me.jungdab.zsm.registry.ModDamageTypes;
import me.jungdab.zsm.registry.ModSounds;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

import static software.bernie.geckolib.constant.DefaultAnimations.ATTACK_SHOOT;

public class TurretEntity extends MobEntity implements GeoEntity {
    private static final TrackedData<Boolean> ATTACK = DataTracker.registerData(TurretEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IS_TARGET_FOCUS = DataTracker.registerData(TurretEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int deathTimer;

    protected void updatePostDeath() {
        this.deathTimer++;
        if (this.deathTimer >= 30 && !this.getWorld().isClient() && !this.isRemoved()) {
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    protected float turnHead(float bodyRotation, float headRotation) {
        float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
        boolean bl = g < -90.0F || g >= 90.0F;
        if (bl) {
            headRotation *= -1.0F;
        }
        this.bodyYaw = headRotation;
        return headRotation;
    }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public TurretEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == ModEntityStatuses.ADD_TURRET_SHOOT_PARTICLE) {
            this.shootParticle();
        } else {
            super.handleStatus(status);
        }
    }

    public boolean isPushable() {return false;}
    public boolean isCollidable() {return true;}

    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ATTACK, false);
        builder.add(IS_TARGET_FOCUS, false);
    }

    protected void initGoals() {
        this.goalSelector.add(1, new TurretEntity.AttackGoal(5, 16));
        this.goalSelector.add(1, new LookAroundGoal(this));

        this.targetSelector.add(1, new AdvancedTargetGoal<>(this, ZSMBasicEntity.class, true));
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

    public void setTargetFocus(boolean targetFocus) {this.dataTracker.set(IS_TARGET_FOCUS, targetFocus);}
    public void setAttack(boolean attack) {this.dataTracker.set(ATTACK, attack);}
    public boolean isAttack() {return this.dataTracker.get(ATTACK);}


    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        float f = (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        DamageSource damageSource = this.mobAttack(this);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            f = EnchantmentHelper.getDamage(serverWorld, this.getWeaponStack(), target, damageSource, f);
        }
        boolean bl = target.damage(world, damageSource, f);
        if (bl) {
            float g = this.getKnockbackAgainst(target, damageSource);
            if (g > 0.0F && target instanceof LivingEntity livingEntity) {
                livingEntity.takeKnockback(
                        g * 0.5F,
                        MathHelper.sin(this.getYaw() * (float) (Math.PI / 180.0)),
                        -MathHelper.cos(this.getYaw() * (float) (Math.PI / 180.0))
                );
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            }
            if (this.getWorld() instanceof ServerWorld serverWorld2) {
                EnchantmentHelper.onTargetDamaged(serverWorld2, target, damageSource);
            }
            this.onAttacking(target);
            this.playAttackSound();
        }

        return bl;
    }

    public DamageSource mobAttack(LivingEntity attacker) {return this.getDamageSources().create(ModDamageTypes.TURRET_ATTACK, attacker);}

//    protected float turnHead(float bodyRotation, float headRotation) {
//        float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
//        boolean bl = g < -90.0F || g >= 90.0F;
//        if (bl) {
//            headRotation *= -1.0F;
//        }
//        return headRotation;
//    }

    public static DefaultAttributeContainer.Builder createTurretAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20)
                .add(EntityAttributes.MOVEMENT_SPEED, 0)
                .add(EntityAttributes.ATTACK_DAMAGE, 2.5)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericIdleController(this),
                DefaultAnimations.getSpawnController(this, AnimationState::getAnimatable, 30),
                genericAttackController(this),
                DefaultAnimations.genericDeathController(this)
        );
    }

    public void shootParticle() {
        Vec3d dir = this.getRotationVector(0, this.getYaw(1.0f));
        Vec3d pos = this.getEyePos();

        pos = pos.add(0, -0.1, 0);
        pos = pos.add(dir.multiply(1.2));
        dir = dir.rotateY(90f * MathHelper.RADIANS_PER_DEGREE);

        Vec3d rightPos = pos;
        pos = pos.add(dir.multiply(-0.5));
        rightPos = rightPos.add(dir.multiply(0.5));

        for(int i = 0; i < 3; i++) {
            this.getWorld().addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0, 0, 0);
            this.getWorld().addParticle(ParticleTypes.SMOKE, rightPos.x, rightPos.y, rightPos.z, 0, 0, 0);
        }
    }

    private <T extends TurretEntity & GeoAnimatable> AnimationController<T> genericAttackController(T animatable) {
        return new AnimationController<>(animatable, "Attack", 0, state -> {
            if (animatable.isAttack())
                return state.setAndContinue(ATTACK_SHOOT);

            state.getController().forceAnimationReset();
            return PlayState.STOP;
        });
    }

    protected void playAttackSound() {
        this.getWorld().playSoundFromEntity(null, this, ModSounds.ENTITY_TURRET_SHOOT, SoundCategory.NEUTRAL, 1f, 1);
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_TURRET_HURT;
    }

    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_TURRET_DEATH;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    class AttackGoal extends Goal {
        private final TurretEntity mob;
        private final double range;
        private final int cooldown;
        private int currentCooldown;


        public AttackGoal(int cooldown, double range) {
            this.mob = TurretEntity.this;
            this.cooldown = cooldown;
            this.range = range * range;

            this.setControls(EnumSet.of(Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity livingEntity = mob.getTarget();
            if(livingEntity == null) return false;
            else if (!livingEntity.isAlive()) return false;
            return this.mob.squaredDistanceTo(livingEntity) < range;
        }

        public void start() {
            this.mob.setTargetFocus(true);
            this.currentCooldown = 0;
        }

        public void stop() {
            this.mob.setTargetFocus(false);
            this.mob.setAttack(false);
        }

        public void tick() {
            if(!(this.mob.getWorld() instanceof  ServerWorld world)) return;

            LivingEntity livingEntity = this.mob.getTarget();
            if (livingEntity != null) {
                this.mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, livingEntity.getPos());

                this.currentCooldown++;
                if(this.currentCooldown == 1) {
                    this.mob.setAttack(true);
                    this.mob.tryAttack(world, livingEntity);

                    this.mob.playAttackSound();
                    this.mob.getWorld().sendEntityStatus(this.mob, ModEntityStatuses.ADD_TURRET_SHOOT_PARTICLE);
                }
                if(this.currentCooldown == 5) this.mob.setAttack(false);
                if(this.currentCooldown >= 6) this.currentCooldown = 0;
            }
        }
    }
}
