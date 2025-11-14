package me.jungdab.zsm.block;

import com.mojang.serialization.MapCodec;
import me.jungdab.zsm.registry.ModBlocks;
import me.jungdab.zsm.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.function.BiConsumer;

public class Fence extends HorizontalConnectingBlock {
    public static final MapCodec<FenceBlock> CODEC = createCodec(FenceBlock::new);

    @Override
    public MapCodec<FenceBlock> getCodec() {
        return CODEC;
    }

    public Fence(AbstractBlock.Settings settings) {
        super(2.0F, 2.0F, 16.0F, 16.0F, 16.0F, settings);
        this.setDefaultState(
                this.stateManager
                        .getDefaultState()
                        .with(NORTH, false)
                        .with(EAST, false)
                        .with(SOUTH, false)
                        .with(WEST, false)
                        .with(WATERLOGGED, false)
        );
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(stack.isOf(ModItems.TEMPERED_INGOT) && !state.isOf(ModBlocks.FENCE)) {
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
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    public boolean canConnect(BlockState state, boolean neighborIsFullSquare) {
        boolean bl = state.isOf(ModBlocks.FENCE) || state.isOf(ModBlocks.CHIPPED_FENCE) || state.isOf(ModBlocks.DAMAGED_FENCE);
        return !cannotConnect(state) && neighborIsFullSquare || bl;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.east();
        BlockPos blockPos4 = blockPos.south();
        BlockPos blockPos5 = blockPos.west();
        BlockState blockState = blockView.getBlockState(blockPos2);
        BlockState blockState2 = blockView.getBlockState(blockPos3);
        BlockState blockState3 = blockView.getBlockState(blockPos4);
        BlockState blockState4 = blockView.getBlockState(blockPos5);
        return super.getPlacementState(ctx)
                .with(NORTH, this.canConnect(blockState, blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.SOUTH)))
                .with(EAST, this.canConnect(blockState2, blockState2.isSideSolidFullSquare(blockView, blockPos3, Direction.WEST)))
                .with(SOUTH, this.canConnect(blockState3, blockState3.isSideSolidFullSquare(blockView, blockPos4, Direction.NORTH)))
                .with(WEST, this.canConnect(blockState4, blockState4.isSideSolidFullSquare(blockView, blockPos5, Direction.EAST)))
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return direction.getAxis().getType() == Direction.Type.HORIZONTAL
                ? state.with(
                FACING_PROPERTIES.get(direction),
                this.canConnect(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()))
        )
                : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if(state.isOf(ModBlocks.FENCE)) {
            world.setBlockState(pos, ModBlocks.CHIPPED_FENCE.getDefaultState()
                    .with(NORTH, state.get(NORTH))
                    .with(SOUTH, state.get(SOUTH))
                    .with(WEST, state.get(WEST))
                    .with(EAST, state.get(EAST))
            );
            return;
        }
        else if(state.isOf(ModBlocks.CHIPPED_FENCE)) {
            world.setBlockState(pos, ModBlocks.DAMAGED_FENCE.getDefaultState()
                    .with(NORTH, state.get(NORTH))
                    .with(SOUTH, state.get(SOUTH))
                    .with(WEST, state.get(WEST))
                    .with(EAST, state.get(EAST))
            );
            return;
        }

        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if(!world.isClient && entity.horizontalCollision) entity.horizontalCollision = false;
    }

    private void repairBlock(World world, BlockState state, BlockPos pos) {
        if(state.isOf(ModBlocks.DAMAGED_FENCE) || state.isOf(ModBlocks.CHIPPED_FENCE))
            world.setBlockState(pos, ModBlocks.FENCE.getDefaultState()
                .with(NORTH, state.get(NORTH))
                .with(SOUTH, state.get(SOUTH))
                .with(WEST, state.get(WEST))
                .with(EAST, state.get(EAST)));
    }
}
