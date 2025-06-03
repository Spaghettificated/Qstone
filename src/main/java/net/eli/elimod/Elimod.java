package net.eli.elimod;

import net.eli.elimod.quantum.Qbit;
import net.eli.elimod.setup.ModBlocks;
import net.eli.elimod.setup.ModDispenserBehaviour;
import net.eli.elimod.setup.ModItemGroups;
import net.eli.elimod.setup.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Elimod implements ModInitializer {
	public static final String MOD_ID = "elimod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Elimod says hello");
		ModItemGroups.RegisterItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModDispenserBehaviour.registerDispenserBehaviours();
	}
}