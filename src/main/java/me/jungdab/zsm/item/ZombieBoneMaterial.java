package me.jungdab.zsm.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;

public class ZombieBoneMaterial {

    public static final ToolMaterial ZOMBIE_BONE;


    static {
        ZOMBIE_BONE = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 250, 6.0F, 2.0F, 15, ItemTags.WOODEN_TOOL_MATERIALS);
    }
}
