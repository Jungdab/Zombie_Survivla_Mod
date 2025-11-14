package me.jungdab.zsm.entity;

import me.jungdab.zsm.client.camera.CameraShake;
import me.jungdab.zsm.entity.ai.goal.GlobalTargetGoal;
import me.jungdab.zsm.registry.ModAdvancements;
import me.jungdab.zsm.registry.ModEffects;
import me.jungdab.zsm.registry.ModEntities;
import me.jungdab.zsm.registry.ModSounds;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

import static software.bernie.geckolib.constant.DefaultAnimations.IDLE;
import static software.bernie.geckolib.constant.DefaultAnimations.WALK;


public class BossZombieEntity extends ZSMBasicEntity implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final BossZombiePart part;

    private final ServerBossBar bossBar = new ServerBossBar(this.getDisplayName(), BossBar.Color.GREEN, BossBar.Style.PROGRESS);

    public int spawnTime;
    public boolean isSpawning = true;

    private static final TrackedData<Integer> ANIMATION_ID = DataTracker.registerData(BossZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public BossZombieEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);

        part = new BossZombiePart(ModEntities.BOSS_ZOMBIE_PART, world);
        part.setOwner(this);
    }

    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);

        builder.add(ANIMATION_ID, 5);
    }
    public void setAnimationId(int id) {
        this.dataTracker.set(ANIMATION_ID, id);
    }
    public int getAnimationId() {
        return this.dataTracker.get(ANIMATION_ID);
    }
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.putInt("spawnTime", this.spawnTime);
        nbt.putInt("AnimationId", this.getAnimationId());
    }
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if(nbt.contains("AnimationId")) this.setAnimationId(nbt.getInt("AnimationId"));
        if(nbt.contains("spawnTime")) this.spawnTime = nbt.getInt("spawnTime");
    }

    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new BossZombiePatternGoal());
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));

        this.targetSelector.add(1, new GlobalTargetGoal<>(this, TurretEntity.class, 1));
    }

    public void attack(LivingEntity target) {
        this.playSound(ModSounds.ENTITY_BOSS_ZOMBIE_SWING, 5F, 1F);

        World world = this.getWorld();
        if(!(world instanceof ServerWorld serverWorld)) return;

        DamageSource source = this.getDamageSources().mobAttack(this);
        float f = (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        target.damage(serverWorld, source, f);
    }

    public void smash() {
        this.playSound(SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, 5F, 0.1F);
        this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 5F, 0.5F);

        World world = this.getWorld();
        if(!(world instanceof ServerWorld serverWorld)) return;

        List<LivingEntity> targetList = this.getWorld().getEntitiesByClass(LivingEntity.class, new Box(this.getX() - 30, this.getY() - 4, this.getZ() - 30, this.getX() + 30, this.getY() + 4, this.getZ() + 30), entity -> !entity.equals(this));
        if(targetList.isEmpty()) return;

        float f = (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE) * 1.5f;
        DamageSource damageSource = this.getDamageSources().mobAttack(this);

        for(LivingEntity entity : targetList) {
            entity.damage(serverWorld, damageSource, f);
            if(entity.isOnGround()) entity.addStatusEffect(new StatusEffectInstance(ModEffects.STUN, 60, 0));
        }
    }

    public void jump(LivingEntity target) {
        Vec3d monsterLocation = this.getPos();
        Vec3d targetLocation = target.getPos();

        // 목표와 몬스터 사이의 차이 벡터 계산
        Vec3d direction = targetLocation.subtract(monsterLocation);

        // 목표까지의 거리 계산
        double distance = new Vec3d(monsterLocation.getX(), 0, monsterLocation.getZ()).distanceTo(new Vec3d(targetLocation.getX(), 0, targetLocation.getZ()));

        // 수평 방향 벡터 계산 (Y축은 제외)
        Vec3d horizontalDirection = new Vec3d(direction.getX(), 0, direction.getZ()).normalize();

        // 수평 속도는 거리에 비례하여 증가
        double speedMultiplier = (distance / 5);  // 거리마다 속도 증가
        Vec3d horizontalVelocity = horizontalDirection.multiply(speedMultiplier);

        // 수직 속도 계산 (거리와 높이를 고려한 조정)
        double heightDifference = targetLocation.getY() - monsterLocation.getY();
        double verticalSpeed = 0.5f;  // 기본 포물선 공식

        // 높이 차이에 따른 추가 속도
        verticalSpeed += Math.max(0, heightDifference / 10);

        // 최종 속도 벡터 계산
        Vec3d velocity = new Vec3d(horizontalVelocity.getX(), verticalSpeed, horizontalVelocity.getZ());

        // 몬스터에 속도 적용
        this.setVelocity(velocity);
    }

    public void checkDespawn() {}

    public void remove(Entity.RemovalReason reason) {
        if(part != null) part.remove(reason);

        if(!this.getWorld().isClient) {
            ServerWorld world = (ServerWorld) this.getWorld();
            List<ServerPlayerEntity> players = world.getPlayers();

            if(players != null && !players.isEmpty()) {
                for(ServerPlayerEntity player : players) player.getAdvancementTracker().grantCriterion(ModAdvancements.DEFEAT_BOSS, "defeat_boss");
            }
        }

        super.remove(reason);
    }

    public void tick() {
        super.tick();
        if(!this.getWorld().isClient && this.isSpawning) {
            this.spawnTime++;

            if(this.spawnTime > 80) {
                this.setAnimationId(0);
                this.isSpawning = false;
            }
        }
    }

    @Override
    protected void mobTick(ServerWorld world) {
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        super.mobTick(world);
    }

    public void tickMovement() {
        super.tickMovement();

        movePart();
    }
    private void movePart() {
        part.setPosition(this.getX(), this.getY(), this.getZ());

        if (!this.getWorld().isClient) {
            Box box = part.getBoundingBox();
            box.expand(0.2);

            for (BlockPos blockPos : BlockPos.iterate(
                    MathHelper.floor(box.minX),
                    MathHelper.floor(box.minY + 1.5),
                    MathHelper.floor(box.minZ),
                    MathHelper.floor(box.maxX),
                    MathHelper.floor(box.maxY),
                    MathHelper.floor(box.maxZ)
            )) {
                this.getWorld().breakBlock(blockPos, false, this);
            }
        }
    }

    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    public static DefaultAttributeContainer.Builder createZombieAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 1000)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY, 1.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.STEP_HEIGHT, 1.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0);
    }
//    protected SoundEvent getAmbientSound() {
//        return ModSounds.ENTITY_BOSS_ZOMBIE_AMBIENT;
//    }
//    protected SoundEvent getHurtSound(DamageSource source) {
//        return ModSounds.ENTITY_BOSS_ZOMBIE_HURT;
//    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                animationController(this).setCustomInstructionKeyframeHandler(event -> {
                    String command = event.getKeyframeData().getInstructions();
                    switch (command) {
                        case "sShake;" -> CameraShake.startShake(0.1f);
                        case "mShake;" -> CameraShake.startShake(0.5f);
                        case "lShake;" -> CameraShake.startShake(1.0f);
                    }
                }),
                DefaultAnimations.genericDeathController(this)
        );
    }
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public static AnimationController<BossZombieEntity> animationController(BossZombieEntity animatable) {
        return new AnimationController<>(animatable, "Animation", 5, state -> {
            switch (animatable.getAnimationId()) {
                case 0 -> {
                    return state.setAndContinue(state.isMoving() ? WALK : IDLE);
                }
                case 1 -> {
                    return state.setAndContinue(RawAnimation.begin().thenPlay("attack.1"));
                }
                case 2 -> {
                    return state.setAndContinue(RawAnimation.begin().thenPlay("attack.2"));
                }
                case 3 -> {
                    return state.setAndContinue(RawAnimation.begin().thenPlay("behavior.jump"));
                }
                case 5 -> {
                    return state.setAndContinue(RawAnimation.begin().thenPlay("misc.spawn"));
                }
            }

            return PlayState.STOP;
        });
    }

    class BossZombiePatternGoal extends Goal {
        private final BossZombieEntity mob;

        private int pattern = 0;
        private int progressTick = 0;

        private int smashCooldown = 0;

        BossZombiePatternGoal() {
            this.mob = BossZombieEntity.this;
        }

        public boolean canStart() {
            if(this.mob.isSpawning) return false;

            LivingEntity target = this.mob.getTarget();
            if(target == null) return false;
            else if(!target.isAlive()) return false;
            else return true;
        }

        public boolean shouldContinue() {
            if(pattern != 0) return true;

            LivingEntity target = this.mob.getTarget();

            if (target == null) return false;
            else if (!target.isAlive()) return false;
            return true;
        }


        /*
        0 : move
        1 : attack1
        2 : attack2
        3 : jump
        4 : rock
        5 : roar
        */

        public void tick() {
            LivingEntity target = this.mob.getTarget();

            if(pattern == 0) {
                double distance = this.mob.squaredDistanceTo(target);

                if(distance < 49) {
                    this.mob.getNavigation().stop();
                    pattern = smashCooldown > 0 ? 2 : 1;
                    progressTick = 0;
                }
                else if(distance > 400) {
                    this.mob.getNavigation().stop();
                    pattern = 3;
                    progressTick = 0;
                }
            }

            switch (pattern) {
                case 0 -> {if(target != null) this.mob.getNavigation().startMovingTo(target, 1.0);}
                case 1 -> attack1();
                case 2 -> attack2();
                case 3 -> jump();
            }

            if(smashCooldown > 0) smashCooldown--;
        }

        private void attack1() {
            if(progressTick == 0) this.mob.setAnimationId(1);
            if(progressTick == 30) this.mob.smash();
            if(progressTick == 40) this.mob.setAnimationId(0);
            if(progressTick == 41) {
                pattern = 0;
                smashCooldown = 200;
            }

            progressTick++;
        }

        private void attack2() {
            LivingEntity target = this.mob.getTarget();

            if(progressTick == 0) this.mob.setAnimationId(2);
            if(progressTick == 7 && target != null) this.mob.attack(target);
            if(progressTick == 13) this.mob.setAnimationId(0);
            if(progressTick == 13) pattern = 0;

            if(target != null) this.mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());

            progressTick++;
        }


        private void jump() {
            LivingEntity target = this.mob.getTarget();
            if(progressTick == 0) this.mob.setAnimationId(3);
            if(progressTick == 13 && target != null) this.mob.jump(target);
            if(progressTick == 17) this.mob.setAnimationId(0);
            if(progressTick == 21) pattern = 0;

            if(target != null) this.mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());

            progressTick++;
        }
    }
}
