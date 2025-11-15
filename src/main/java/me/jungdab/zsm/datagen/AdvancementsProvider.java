package me.jungdab.zsm.datagen;

import me.jungdab.zsm.ZSM;
import me.jungdab.zsm.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementsProvider extends FabricAdvancementProvider {
    public AdvancementsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {
        final RegistryWrapper.Impl<Item> itemLookup = wrapperLookup.getOrThrow(RegistryKeys.ITEM);

        AdvancementEntry root = Advancement.Builder.create()
                .display(
                        ModItems.BASIC_ZOMBIE_SPAWN_EGG,
                        Text.translatable("advancements.zsm.root.title"),
                        Text.translatable("advancements.zsm.root.description"),
                        Identifier.of(ZSM.MOD_ID, "textures/gui/advancements/backgrounds/zsm.png"),
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("entered_game", TickCriterion.Conditions.createTick())
                .build(consumer, "zsm:root");

        AdvancementEntry gotBone = Advancement.Builder.create().parent(root)
                .display(
                        ModItems.ZOMBIE_BONE,
                        Text.translatable("advancements.zsm.got_zombie_bone.title"),
                        Text.translatable("advancements.zsm.got_zombie_bone.description"),
                        Identifier.of(ZSM.MOD_ID, "textures/gui/advancements/backgrounds/zsm.png"),
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_zombie_bone", InventoryChangedCriterion.Conditions.items(ModItems.ZOMBIE_BONE))
                .rewards(AdvancementRewards.Builder
                        .recipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "fence")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "night_vision_device")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "tempered_block")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "tempered_ingot")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "turret")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "turret_core")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "zombie_bone_boots")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "zombie_bone_chestplate")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "zombie_bone_helmet")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "zombie_bone_leggings")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "zombie_bone_sword")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "zombie_ingot")))
                        .addRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ZSM.MOD_ID, "zombie_ingot_block")))
                )
                .build(consumer, "zsm:got_zombie_bone");

        AdvancementEntry usingTurret = Advancement.Builder.create().parent(root)
                .display(
                        ModItems.TURRET_ITEM,
                        Text.translatable("advancements.zsm.using_turret.title"),
                        Text.translatable("advancements.zsm.using_turret.description"),
                        Identifier.of(ZSM.MOD_ID, "textures/gui/advancements/backgrounds/zsm.png"),
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("using_turret", ItemCriterion.Conditions.createItemUsedOnBlock(LocationPredicate.Builder.create(), ItemPredicate.Builder.create().items(itemLookup, ModItems.TURRET_ITEM)))
                .build(consumer, "zsm:using_turret");

        AdvancementEntry equipNightVisionDevice = Advancement.Builder.create().parent(root)
                .display(
                        ModItems.NIGHT_VISION_DEVICE,
                        Text.translatable("advancements.zsm.equip_night_vision_device.title"),
                        Text.translatable("advancements.zsm.equip_night_vision_device.description"),
                        Identifier.of(ZSM.MOD_ID, "textures/gui/advancements/backgrounds/zsm.png"),
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("equip_night_vision_device", ItemCriterion.Conditions.createItemUsedOnBlock(LocationPredicate.Builder.create(), ItemPredicate.Builder.create().items(itemLookup, ModItems.NIGHT_VISION_DEVICE)))
                .build(consumer, "zsm:equip_night_vision_device");

        //갑옷, 펜스
        AdvancementEntry lastMorning = Advancement.Builder.create().parent(root)
                .display(

                        Items.CLOCK,
                        Text.translatable("advancements.zsm.last_morning.title"),
                        Text.translatable("advancements.zsm.last_morning.description"),
                        Identifier.of(ZSM.MOD_ID, "textures/gui/advancements/backgrounds/zsm.png"),
                        AdvancementFrame.GOAL,
                        true,
                        true,
                        false
                )
                .criterion("last_morning", Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, "zsm:last_morning");

        Advancement.Builder.create().parent(lastMorning)
                .display(

                        ModItems.ZOMBIE_BOSS_HEAD,
                        Text.translatable("advancements.zsm.defeat_boss.title"),
                        Text.translatable("advancements.zsm.defeat_boss.description"),
                        Identifier.of(ZSM.MOD_ID, "textures/gui/advancements/backgrounds/zsm.png"),
                        AdvancementFrame.CHALLENGE,
                        true,
                        true,
                        true
                )
                .criterion("defeat_boss", Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, "zsm:defeat_boss");
    }
}
