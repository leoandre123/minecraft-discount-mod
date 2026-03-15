package com.leodenandre.discount;

import com.leodenandre.discount.blocks.ModBlocks;
import com.leodenandre.discount.blocks.boilingcauldron.BoilingCauldronBehaviour;
import com.leodenandre.discount.items.ModItems;
import com.leodenandre.discount.network.ModPackets;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discount implements ModInitializer {
	public static final String MOD_ID = "discount";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Discount Mod...");

		ModPackets.registerPackets();
		ModPackets.registerServerReceivers();
		ModItems.initialize();
		ModBlocks.initialize();
		BoilingCauldronBehaviour.registerBehavior();
	}
}