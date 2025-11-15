package me.jungdab.zsm.registry;

import me.jungdab.zsm.ZSM;
import me.jungdab.zsm.item.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Function;

public class ModItems {
    public static final TurretItem TURRET_ITEM = register("turret", TurretItem::new, new Item.Settings());
    public static final Item TURRET_CORE = register("turret_core", Item::new, new Item.Settings());

    public static final Item ZOMBIE_BONE = register("zombie_bone", Item::new, new Item.Settings());
    public static final Item ZOMBIE_INGOT = register("zombie_ingot", Item::new, new Item.Settings());
    public static final Item TEMPERED_INGOT = register("tempered_ingot", Item::new, new Item.Settings());

    public static final Item NIGHT_VISION_DEVICE = register("night_vision_device", NightVisionDeviceItem::new, new Item.Settings().maxCount(1).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()));

    public static final Item ZOMBIE_BONE_SWORD = register("zombie_bone_sword", settings -> new SwordItem(ZombieBoneMaterial.ZOMBIE_BONE, 3, -1.4f, settings), new Item.Settings());
    public static final Item ZOMBIE_HAMMER = register("zombie_hammer", ZombieHammerItem::new, new Item.Settings());

    public static final Item ZOMBIE_BONE_HELMET =
            register("zombie_bone_helmet",
                    settings -> new ZombieBoneArmorItem(
                            ModArmorMaterials.ZOMBIE_BONE,
                            EquipmentType.HELMET,
                            settings
                    ),
                    new Item.Settings()
                            .maxCount(1)
                            .maxDamage(EquipmentType.HELMET.getMaxDamage(15))
                            .component(DataComponentTypes.LORE,
                                    new LoreComponent(List.of(
                                            Text.literal(""),
                                            Text.translatable("item.zsm.zombie_bone_armor.lore"),
                                            Text.translatable("item.zsm.zombie_bone_armor.lore2")
                                    )))
            );
    public static final Item ZOMBIE_BONE_CHESTPLATE =
            register("zombie_bone_chestplate",
                    settings -> new ZombieBoneArmorItem(
                            ModArmorMaterials.ZOMBIE_BONE,
                            EquipmentType.CHESTPLATE,
                            settings
                    ),
                    new Item.Settings()
                            .maxCount(1)
                            .maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(15))
                            .component(DataComponentTypes.LORE,
                                    new LoreComponent(List.of(
                                            Text.literal(""),
                                            Text.translatable("item.zsm.zombie_bone_armor.lore"),
                                            Text.translatable("item.zsm.zombie_bone_armor.lore2")
                                    )))
            );
    public static final Item ZOMBIE_BONE_LEGGINGS =
            register("zombie_bone_leggings",
                    settings -> new ZombieBoneArmorItem(
                            ModArmorMaterials.ZOMBIE_BONE,
                            EquipmentType.LEGGINGS,
                            settings
                    ),
                    new Item.Settings()
                            .maxCount(1)
                            .maxDamage(EquipmentType.LEGGINGS.getMaxDamage(15))
                            .component(DataComponentTypes.LORE,
                                    new LoreComponent(List.of(
                                            Text.literal(""),
                                            Text.translatable("item.zsm.zombie_bone_armor.lore"),
                                            Text.translatable("item.zsm.zombie_bone_armor.lore2")
                                    )))
            );
    public static final Item ZOMBIE_BONE_BOOTS =
            register("zombie_bone_boots",
                    settings -> new ZombieBoneArmorItem(
                            ModArmorMaterials.ZOMBIE_BONE,
                            EquipmentType.BOOTS,
                            settings
                    ),
                    new Item.Settings()
                            .maxCount(1)
                            .maxDamage(EquipmentType.BOOTS.getMaxDamage(15))
                            .component(DataComponentTypes.LORE,
                                    new LoreComponent(List.of(
                                            Text.literal(""),
                                            Text.translatable("item.zsm.zombie_bone_armor.lore"),
                                            Text.translatable("item.zsm.zombie_bone_armor.lore2")
                                    )))
            );
    public static final Item ZOMBIE_BOSS_HEAD =
            register("zombie_boss_head", Item::new,
                    new Item.Settings()
                            .maxCount(1)
                            .component(DataComponentTypes.EQUIPPABLE,
                                    EquippableComponent.builder(EquipmentSlot.HEAD).build())
            );

    public static final SpawnEggItem BASIC_ZOMBIE_SPAWN_EGG =
            register("basic_zombie_spawn_egg",
                    settings -> new SpawnEggItem(ModEntities.BASIC_ZOMBIE, settings),
                    new Item.Settings()
            );

    public static final SpawnEggItem BOMB_ZOMBIE_SPAWN_EGG =
            register("bomb_zombie_spawn_egg",
                    settings -> new SpawnEggItem(ModEntities.BOMB_ZOMBIE, settings),
                    new Item.Settings()
            );

    public static final SpawnEggItem HAMMER_ZOMBIE_SPAWN_EGG =
            register("hammer_zombie_spawn_egg",
                    settings -> new SpawnEggItem(ModEntities.HAMMER_ZOMBIE, settings),
                    new Item.Settings()
            );

    public static final SpawnEggItem SUPER_ZOMBIE_SPAWN_EGG =
            register("super_zombie_spawn_egg",
                    settings -> new SpawnEggItem(ModEntities.SUPER_ZOMBIE, settings),
                    new Item.Settings()
            );


    public static <T extends Item> T register(String path, T item) {
        return Registry.register(Registries.ITEM, Identifier.of(ZSM.MOD_ID, path), item);
    }

    public static <T extends Item> T register(String name, Function<Item.Settings, T> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ZSM.MOD_ID, name));

        // Create the item instance.
        T item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void init() {
    }
}
