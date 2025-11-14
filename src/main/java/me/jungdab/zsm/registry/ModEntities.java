package me.jungdab.zsm.registry;

import me.jungdab.zsm.ZSM;
import me.jungdab.zsm.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<BasicZombieEntity> BASIC_ZOMBIE =
            registerEntity(
            "basic_zombie",
            EntityType.Builder.create(BasicZombieEntity::new, SpawnGroup.MONSTER).dimensions(0.6f,1.95f)
    );


    public static final EntityType<BombZombieEntity> BOMB_ZOMBIE =
            registerEntity(
                    "bomb_zombie",
                    EntityType.Builder.create(BombZombieEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 2.2f)
            );

    public static final EntityType<HammerZombieEntity> HAMMER_ZOMBIE =
            registerEntity(
                    "hammer_zombie",
                    EntityType.Builder.create(HammerZombieEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 2.2f)
            );

    public static final EntityType<SuperZombieEntity> SUPER_ZOMBIE =
            registerEntity(
                    "super_zombie",
                    EntityType.Builder.create(SuperZombieEntity::new, SpawnGroup.MONSTER).dimensions(1.95f, 2.8f)
            );

    public static final EntityType<BossZombieEntity> BOSS_ZOMBIE =
            registerEntity(
                    "boss_zombie",
                    EntityType.Builder.create(BossZombieEntity::new, SpawnGroup.MISC).dimensions(0.1f, 1f).eyeHeight(3.5f)
            );

    public static final EntityType<BossZombiePart> BOSS_ZOMBIE_PART =
            registerEntity(
                    "boss_zombie_part",
                    EntityType.Builder.create(BossZombiePart::new, SpawnGroup.MISC).dimensions(5f, 5f)
            );

    public static final EntityType<TurretEntity> TURRET =
            registerEntity(
                    "turret",
                    EntityType.Builder.create(TurretEntity::new, SpawnGroup.MISC).dimensions(1f, 1.1f)
            );

    public static <T extends Entity>EntityType<T> registerEntity(String id, EntityType.Builder<T> type) {
        RegistryKey<EntityType<?>> registryKey = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(ZSM.MOD_ID, id));
        return Registry.register(Registries.ENTITY_TYPE, registryKey, type.build(registryKey));
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(ModEntities.BASIC_ZOMBIE, BasicZombieEntity.createZombieAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.BOMB_ZOMBIE, BombZombieEntity.createZombieAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.HAMMER_ZOMBIE, HammerZombieEntity.createZombieAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SUPER_ZOMBIE, SuperZombieEntity.createZombieAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.TURRET, TurretEntity.createTurretAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.BOSS_ZOMBIE, BossZombieEntity.createZombieAttributes());
    }
}
