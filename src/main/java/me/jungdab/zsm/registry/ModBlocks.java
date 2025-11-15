package me.jungdab.zsm.registry;

import me.jungdab.zsm.ZSM;
import me.jungdab.zsm.block.Fence;
import me.jungdab.zsm.block.ReviveBlock;
import me.jungdab.zsm.block.TemperedBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModBlocks {
    public static final ReviveBlock REVIVE_BLOCK = register(
            "revive_block",
            ReviveBlock::new,
            AbstractBlock.Settings.create().nonOpaque().strength(0.6F),
            true
    );
    public static final Block ZOMBIE_INGOT_BLOCK = register(
            "zombie_ingot_block",
            Block::new,
            AbstractBlock.Settings.create()
                .mapColor(MapColor.IRON_GRAY)
                .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                .requiresTool()
                .strength(1.0F, 6.0F)
                .sounds(BlockSoundGroup.METAL),
            true
    );

    public static final Block FENCE = register(
            "fence",
            Fence::new,
            AbstractBlock.Settings.create()
                    .requiresTool()
                    .strength(1.0f, 6.0f)
                    .sounds(BlockSoundGroup.METAL)
                    .nonOpaque(),
            true
    );

    public static final Block CHIPPED_FENCE = register(
            "chipped_fence",
            Fence::new,
            AbstractBlock.Settings.create()
                    .requiresTool()
                    .strength(1.0f, 6.0f)
                    .sounds(BlockSoundGroup.METAL)
                   .nonOpaque(),
            true
    );

    public static final Block DAMAGED_FENCE = register(
            "damaged_fence",
            Fence::new,
            AbstractBlock.Settings.create()
                    .requiresTool()
                    .strength(1.0f, 6.0f)
                    .sounds(BlockSoundGroup.METAL)
                    .nonOpaque(),
            true
    );

    public static final Block TEMPERED_BLOCK = register(
            "tempered_block",
            TemperedBlock::new,
            AbstractBlock.Settings.create()
                    .requiresTool()
                    .strength(1.0f, 6.0f)
                    .sounds(BlockSoundGroup.METAL),
            true
    );

    public static final Block CHIPPED_TEMPERED_BLOCK = register(
            "chipped_tempered_block",
            TemperedBlock::new,
            AbstractBlock.Settings.create()
                    .requiresTool()
                    .strength(1.0f, 6.0f)
                    .sounds(BlockSoundGroup.METAL),
            true
    );

    public static final Block DAMAGED_TEMPERED_BLOCK = register(
            "damaged_tempered_block",
            TemperedBlock::new,
            AbstractBlock.Settings.create()
                    .requiresTool()
                    .strength(1.0f, 6.0f)
                    .sounds(BlockSoundGroup.METAL),
            true
    );


    private static <T extends Block> T register(String name, T block) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        RegistryKey<Item> itemKey = keyOfItem(name);

        BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, blockItem);

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static <T extends Block> T register(String name, Function<AbstractBlock.Settings, T> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        // Create a registry key for the block
        RegistryKey<Block> blockKey = keyOfBlock(name);
        // Create the block instance
        T block = blockFactory.apply(settings.registryKey(blockKey));

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            // Items need to be registered with a different type of registry key, but the ID
            // can be the same.
            RegistryKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ZSM.MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ZSM.MOD_ID, name));
    }

    public static void init() {}
}
