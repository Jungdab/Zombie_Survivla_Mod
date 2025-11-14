package me.jungdab.zsm.entity;

import me.jungdab.zsm.client.animation.ZSMAnimations;
import me.jungdab.zsm.entity.ai.goal.DisableableMeleeAttackTargetGoal;
import me.jungdab.zsm.entity.ai.goal.GlobalTargetGoal;
import me.jungdab.zsm.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

import static software.bernie.geckolib.constant.DefaultAnimations.*;

public class SuperZombieEntity extends ZSMBasicEntity implements GeoEntity {
    private static final TrackedData<Boolean> ATTACK = DataTracker.registerData(SuperZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private DisableableMeleeAttackTargetGoal meleeAttackTargetGoal;
    private boolean isFindBombZombie;

    public SuperZombieEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);

        this.setPathfindingPenalty(PathNodeType.LEAVES, 0.0F);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == ModEntityStatuses.ADD_SUPER_ZOMBIE_SMASH_GROUND_PARTICLE) {
            this.produceParticles(ParticleTypes.CRIT);
        } else {
            super.handleStatus(status);
        }
    }


    @Override
    public void tick() {
        if(this.isAttack()) this.getNavigation().stop();

        super.tick();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient && this.isAlive()) {
            this.meleeAttackTargetGoal.setEnable(!this.isFindBombZombie);
        }

        if (this.horizontalCollision && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            boolean bl = false;
            Box box = this.getBoundingBox().expand(0.2);

            for (BlockPos blockPos : BlockPos.iterate(
                    MathHelper.floor(box.minX),
                    MathHelper.floor(box.minY),
                    MathHelper.floor(box.minZ),
                    MathHelper.floor(box.maxX),
                    MathHelper.floor(box.maxY),
                    MathHelper.floor(box.maxZ)
            )) {
                BlockState blockState = this.getWorld().getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (block instanceof LeavesBlock) {
                    bl = this.getWorld().breakBlock(blockPos, true, this) || bl;
                }
            }

            if (!bl && this.isOnGround()) {
                this.jump();
            }
        }
    }


    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);

        builder.add(ATTACK, false);
    }

    public void throwBombZombie(LivingEntity target) {
        if(!this.hasPassengers()) return;

        Entity passenger = this.getPassengerList().getFirst();

        if(!(passenger instanceof BombZombieEntity)) return;

        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - passenger.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);

        Vec3d velocity = new Vec3d(d, e + g * 0.2F, f);

        passenger.stopRiding();
        passenger.setVelocity(velocity.normalize().multiply(1.6f));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("attack", this.isAttack());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setAttack(nbt.getBoolean("attack"));
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                ZSMAnimations.genericWalkIdleController(this),
                genericAttackController(this),
                DefaultAnimations.genericDeathController(this)
        );
    }

    private  <T extends SuperZombieEntity & GeoAnimatable> AnimationController<T> genericAttackController(T animatable) {
        return new AnimationController<>(animatable, "Attack", 5, state -> {
            if (animatable.isAttack())
                return state.setAndContinue(ATTACK_CAST);

            state.getController().forceAnimationReset();
            return PlayState.STOP;
        });
    }

    public boolean isAttack() {
        return this.dataTracker.get(ATTACK);
    }

    public void setAttack(boolean attack) {
        this.dataTracker.set(ATTACK, attack);
    }

    public void produceParticles(ParticleEffect parameters) {
        for (int i = 0; i < 100; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double r = Math.sqrt(Math.random()) * 5;

            double xOffset = Math.cos(theta) * r;
            double zOffset = Math.sin(theta) * r;

            this.getWorld().addParticle(parameters, this.getX() + xOffset, this.getY() + 0.2f, this.getZ() + zOffset, 0, 0, 0);
        }
    }

    protected void initGoals() {
        this.meleeAttackTargetGoal = new DisableableMeleeAttackTargetGoal(this, 1.0, false);

        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new SuperZombieEntity.AttackGoal());
        this.goalSelector.add(3, new SuperZombieEntity.ThrowGoal());
        this.goalSelector.add(3, this.meleeAttackTargetGoal);
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));

        this.targetSelector.add(1, new GlobalTargetGoal<>(this, TurretEntity.class, 10));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
    public static DefaultAttributeContainer.Builder createZombieAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 100)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY, 1.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.STEP_HEIGHT, 1.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0);
    }
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_SUPER_ZOMBIE_AMBIENT;
    }
    public void playAmbientSound() {
        if (this.getTarget() == null || !this.isOnGround()) {
            this.getWorld().playSoundFromEntity(this, this.getAmbientSound(), this.getSoundCategory(), 0.3F, 1.0F);
        }
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_SUPER_ZOMBIE_HURT;
    }
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_SUPER_ZOMBIE_DEATH;
    }
    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_ZOMBIE_STEP;
    }
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    class AttackGoal extends Goal {
        private final SuperZombieEntity mob;
        private int cooldown;

        AttackGoal() {
            this.mob = SuperZombieEntity.this;
        }

        public boolean shouldRunEveryTick() {
            return true;
        }

        public boolean canStart() {
            LivingEntity target = mob.getTarget();
            if(target == null) return false;
            return this.mob.isAttack() || this.mob.squaredDistanceTo(target) < 9;
        }

        public void stop() {
            this.mob.setAttack(false);
            this.cooldown = 0;
        }

        public void tick() {
            LivingEntity target = mob.getTarget();
            if(target != null) {
                this.cooldown++;

                if(this.cooldown == 1) this.mob.setAttack(true);
                if(this.cooldown == 20) this.attack();
                if(this.cooldown >= 35) this.mob.setAttack(false);
                if(this.cooldown >= 36) this.cooldown = 0;
            }
        }

        private void attack() {
            this.mob.getWorld().sendEntityStatus(this.mob, ModEntityStatuses.ADD_SUPER_ZOMBIE_SMASH_GROUND_PARTICLE);
            this.mob.playSound(SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, 1F, 1F);

            if(!(this.mob.getWorld() instanceof  ServerWorld serverWorld)) return;

            List<LivingEntity> targetList = this.mob.getWorld().getEntitiesByClass(LivingEntity.class, new Box(this.mob.getX() - 5, this.mob.getY() - 1, this.mob.getZ() - 5, this.mob.getX() + 5, this.mob.getY() + 1.5, this.mob.getZ() + 5), entity -> entity instanceof PlayerEntity || entity instanceof TurretEntity);
            if(targetList.isEmpty()) return;

            float f = (float)this.mob.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            DamageSource damageSource = this.mob.getDamageSources().mobAttack(this.mob);

            for(LivingEntity player : targetList) {
                player.damage(serverWorld, damageSource, f);
            }
        }
    }

    class ThrowGoal extends Goal {
        private final SuperZombieEntity mob;
        private BombZombieEntity targetEntity;

        protected final int reciprocalChance;
        private int status;
        private int range = 15 * 15;

        public ThrowGoal() {
            this.mob = SuperZombieEntity.this;
            this.reciprocalChance = toGoalTicks(10);
        }

        public boolean canStart() {
            if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
                return false;
            } else {
                this.findClosestTarget();
                return this.targetEntity != null;
            }
        }

        public boolean shouldContinue() {
            return this.targetEntity != null && this.targetEntity.isAlive() && !this.targetEntity.isIgnited();
        }

        public void start() {
            this.mob.isFindBombZombie = true;
            this.targetEntity.isTargeting = true;
            this.status = 0;
        }

        public void stop() {
            this.mob.isFindBombZombie = false;
            this.targetEntity = null;
        }

        public void tick() {
            if(this.status == 0) {
                this.mob.getNavigation().startMovingTo(targetEntity, 1.0);
                if(this.mob.squaredDistanceTo(this.targetEntity) < 9) {
                    this.targetEntity.startRiding(this.mob);
                    this.status = 1;
                }
            }
            if(this.status == 1) {
                LivingEntity livingEntity = this.mob.getTarget();
                if(livingEntity != null) {
                    double d = this.mob.squaredDistanceTo(livingEntity);
                    if(d > this.range) {
                        this.mob.getNavigation().startMovingTo(livingEntity, 1.0);
                    }
                    else {
                        this.targetEntity.knockBackResistance();
                        this.targetEntity.isThrow = true;
                        this.mob.getNavigation().stop();
                        this.mob.throwBombZombie(livingEntity);
                    }
                }
            }
        }

        protected void findClosestTarget() {
                this.targetEntity = this.mob
                        .getWorld()
                        .getEntities(
                                this.mob.getWorld().getEntitiesByClass(BombZombieEntity.class, this.getSearchBox(35), livingEntity -> !livingEntity.isIgnited() && !livingEntity.hasVehicle() && !livingEntity.isThrow && !livingEntity.isTargeting),
                                TargetPredicate.createNonAttackable().setBaseMaxDistance(35),
                                this.mob,
                                this.mob.getX(),
                                this.mob.getEyeY(),
                                this.mob.getZ()
                        );
        }

        protected Box getSearchBox(double distance) {
            return this.mob.getBoundingBox().expand(distance, 4.0, distance);
        }
    }
}
