package me.jungdab.zsm.block;

import me.jungdab.zsm.registry.ModBlocks;
import me.jungdab.zsm.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.function.BiConsumer;

public class TemperedBlock extends Block {
    public TemperedBlock(Settings settings) {
        super(settings);
    }

    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(stack.isOf(ModItems.TEMPERED_INGOT) && !state.isOf(ModBlocks.TEMPERED_BLOCK)) {
            world.playSound(player, pos, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.BLOCKS, 1.0f, 1.0f);

            if(world.isClient) {
                return ActionResult.SUCCESS;
            }
            else {
                for(BlockPos pos1 : BlockPos.iterate(pos.add(1, 1, 1), pos.add(-1,-1, -1))) this.repairBlock(world, world.getBlockState(pos1), pos1);
                stack.decrementUnlessCreative(1, player);

                return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
            }
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if(state.isOf(ModBlocks.TEMPERED_BLOCK)) {
            world.setBlockState(pos, ModBlocks.CHIPPED_TEMPERED_BLOCK.getDefaultState());
            return;
        }
        else if(state.isOf(ModBlocks.CHIPPED_TEMPERED_BLOCK)) {
            world.setBlockState(pos, ModBlocks.DAMAGED_TEMPERED_BLOCK.getDefaultState());
            return;
        }

        super.onExploded(state, world, pos, explosion, stackMerger);
    }


    private void repairBlock(World world, BlockState state, BlockPos pos) {
        if(state.isOf(ModBlocks.DAMAGED_TEMPERED_BLOCK) || state.isOf(ModBlocks.CHIPPED_TEMPERED_BLOCK))
            world.setBlockState(pos, ModBlocks.TEMPERED_BLOCK.getDefaultState());
    }
}
