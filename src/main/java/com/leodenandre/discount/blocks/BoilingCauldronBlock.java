package com.leodenandre.discount.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.CollisionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

public class BoilingCauldronBlock extends AbstractCauldronBlock {
    public static final MapCodec<BoilingCauldronBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(CauldronBehavior.CODEC.fieldOf("interactions").forGetter((block) -> {
            return block.behaviorMap;
        }), createSettingsCodec()).apply(instance, BoilingCauldronBlock::new);
    });
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 3;
    public static final IntProperty LEVEL;
    private static final int BASE_FLUID_HEIGHT = 6;
    private static final double FLUID_HEIGHT_PER_LEVEL = 3.0;
    private static final VoxelShape[] INSIDE_COLLISION_SHAPE_BY_LEVEL;
    public static final BooleanProperty BOILING;

    public MapCodec<BoilingCauldronBlock> getCodec() {
        return CODEC;
    }

    public BoilingCauldronBlock(CauldronBehavior.CauldronBehaviorMap behaviorMap, AbstractBlock.Settings settings) {
        super(settings, behaviorMap);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState())
                .with(LEVEL, 1)
                .with(BOILING, false)
        );
    }

    public boolean isFull(BlockState state) {
        return state.get(LEVEL) == 3;
    }

    protected boolean canBeFilledByDripstone(Fluid fluid) {return true;}
    protected double getFluidHeight(BlockState state) {
        return getFluidHeight((Integer)state.get(LEVEL)) / 16.0;
    }
    private static double getFluidHeight(int level) {
        return 6.0 + (double)level * 3.0;
    }
    protected VoxelShape getInsideCollisionShape(BlockState state, BlockView world, BlockPos pos, Entity entity) {
        return INSIDE_COLLISION_SHAPE_BY_LEVEL[(Integer)state.get(LEVEL) - 1];
    }
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        if (world instanceof ServerWorld serverWorld) {
            BlockPos blockPos = pos.toImmutable();
            handler.addPreCallback(CollisionEvent.EXTINGUISH, (collidedEntity) -> {
                if (collidedEntity.isOnFire() && collidedEntity.canModifyAt(serverWorld, blockPos)) {
                    decrementFluidLevel(state, world, pos);
                }
            });
            if (state.contains(BOILING) && state.get(BOILING) && entity instanceof LivingEntity living) {
                double surfaceY = pos.getY() + getFluidHeight(state);
                boolean feetInWater = living.getBoundingBox().minY < surfaceY - 0.01;

                if (feetInWater && !living.isInvulnerableTo(serverWorld,serverWorld.getDamageSources().hotFloor())) {
                    living.damage(serverWorld, serverWorld.getDamageSources().hotFloor(), 1.0f);

                    if(living instanceof VillagerEntity villager){

                        int discount = 3 + world.random.nextInt(3); // 3–5 off, example
                        for (var offer : villager.getOffers()) {
                            //offer.clearSpecialPrice();
                            offer.increaseSpecialPrice(-discount);
                        }
                    }
                }

            }
        }
        handler.addEvent(CollisionEvent.EXTINGUISH);
    }

    public static void decrementFluidLevel(BlockState state, World world, BlockPos pos) {
        int i = state.get(LEVEL) - 1;
        BlockState blockState = state.with(LEVEL, i);
        world.setBlockState(pos, blockState);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
    }

    public static void setBoiling(BlockState state, World world, BlockPos pos){
        var newState = !state.get(BOILING);
        BlockState blockState = (BlockState)state.with(BOILING, newState);
        world.setBlockState(pos, blockState);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
    }

    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {

        var canFillWithPrecipitation = false;
        if (precipitation == Biome.Precipitation.RAIN) {
            canFillWithPrecipitation = world.getRandom().nextFloat() < 0.05F;
        }

        if (canFillWithPrecipitation && state.get(LEVEL) != 3) {
            BlockState blockState = state.cycle(LEVEL);
            world.setBlockState(pos, blockState);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
        }
    }

    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return (Integer)state.get(LEVEL);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{LEVEL, BOILING});
    }

    protected void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid) {
        if (!this.isFull(state)) {
            BlockState blockState = (BlockState)state.with(LEVEL, (Integer)state.get(LEVEL) + 1);
            world.setBlockState(pos, blockState);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
            world.syncWorldEvent(1047, pos, 0);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (!state.contains(LEVEL)) return;
        if(!state.get(BOILING)) return;

        double surface = pos.getY() + getFluidHeight(state);
        for (int i = 0; i < 2 + random.nextInt(2); i++) {
            double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
            double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;

            world.addParticleClient(net.minecraft.particle.ParticleTypes.CLOUD, x, surface + 0.2, z, 0.0, 0.02, 0.0);

            if (random.nextFloat() < 0.25f) {
                world.addParticleClient(net.minecraft.particle.ParticleTypes.SPLASH, x, surface, z, 0.0, 0.01, 0.0);
            }
        }

        if (random.nextInt(200) == 0) {
            world.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
        }
    }

    static {
        LEVEL = Properties.LEVEL_3;
        BOILING = BooleanProperty.of("boiling");
        INSIDE_COLLISION_SHAPE_BY_LEVEL = (VoxelShape[]) Util.make(() -> {
            return Block.createShapeArray(2, (level) -> {
                return VoxelShapes.union(AbstractCauldronBlock.OUTLINE_SHAPE, Block.createColumnShape(12.0, 4.0, getFluidHeight(level + 1)));
            });
        });
    }
}
