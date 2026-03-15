package com.leodenandre.discount.items;

import com.leodenandre.discount.Discount;
import com.leodenandre.discount.blocks.BoilingCauldronBlock;
import com.leodenandre.discount.blocks.boilingcauldron.BoilingCauldronBehaviour;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {

    public static final Item CAMERA = register(
            "camera",
            CameraItem::new,
            new Item.Settings(),
            ItemGroups.FUNCTIONAL
    );

    public static Item register(String name, Function<Item.Settings, Item> itemFactory,
                                Item.Settings settings, RegistryKey<ItemGroup> group) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Discount.MOD_ID, name));
        Item item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);

        ItemGroupEvents.modifyEntriesEvent(group)
                .register((itemGroup) -> itemGroup.add(item));

        return item;
    }

    public static void initialize() {}
}
