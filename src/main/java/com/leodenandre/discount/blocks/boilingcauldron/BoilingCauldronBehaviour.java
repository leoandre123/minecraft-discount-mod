package com.leodenandre.discount.blocks.boilingcauldron;

import com.leodenandre.discount.blocks.BoilingCauldronBlock;
import com.leodenandre.discount.blocks.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.world.event.GameEvent;

import java.util.Map;

public interface BoilingCauldronBehaviour {

    CauldronBehavior.CauldronBehaviorMap BOILING_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("boiling");

    static void registerBehavior() {
        Map<Item, CauldronBehavior> map = BOILING_CAULDRON_BEHAVIOR.map();
        map.put(Items.WATER_BUCKET, ((state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
                player.incrementStat(Stats.FILL_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                world.setBlockState(pos, ModBlocks.BOILING_CAULDRON.getDefaultState().with(BoilingCauldronBlock.LEVEL, 3));
                world.playSound((Entity)null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent((Entity)null, GameEvent.FLUID_PLACE, pos);
            }

            return ActionResult.SUCCESS;
        }));
        map.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
            if ((Integer)state.get(BoilingCauldronBlock.LEVEL) != 3) {
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            } else {
                if (!world.isClient) {
                    Item item = stack.getItem();
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.WATER_BUCKET)));
                    player.incrementStat(Stats.USE_CAULDRON);
                    player.incrementStat(Stats.USED.getOrCreateStat(item));
                    world.setBlockState(pos, ModBlocks.BOILING_CAULDRON.getDefaultState());
                    world.playSound((Entity)null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.emitGameEvent((Entity)null, GameEvent.FLUID_PICKUP, pos);
                }

                return ActionResult.SUCCESS;
            }
        });
        map.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, PotionContentsComponent.createStack(Items.POTION, Potions.WATER)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                BoilingCauldronBlock.decrementFluidLevel(state, world, pos);
                world.playSound((Entity)null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent((Entity)null, GameEvent.FLUID_PICKUP, pos);
            }

            return ActionResult.SUCCESS;
        });
        map.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            if ((Integer)state.get(BoilingCauldronBlock.LEVEL) == 3) {
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            } else {
                PotionContentsComponent potionContentsComponent = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
                if (potionContentsComponent != null && potionContentsComponent.matches(Potions.WATER)) {
                    if (!world.isClient) {
                        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                        player.incrementStat(Stats.USE_CAULDRON);
                        player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                        world.setBlockState(pos, (BlockState)state.cycle(BoilingCauldronBlock.LEVEL));
                        world.playSound((Entity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        world.emitGameEvent((Entity)null, GameEvent.FLUID_PLACE, pos);
                    }

                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                }
            }
        });

        map.put(Items.FLINT_AND_STEEL, (state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                BoilingCauldronBlock.setBoiling(state, world, pos);
                world.playSound((Entity)null, pos, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            return ActionResult.SUCCESS;
        });
    }
}
