package net.eli.elimod.setup;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import net.eli.elimod.Elimod;
import net.eli.elimod.quantum.Gate;
import net.eli.elimod.quantum.Qbit;
import net.eli.elimod.quantum.QbitControlBlock;
import net.eli.elimod.quantum.QbitGateBlock;
import net.eli.elimod.quantum.QbitMeasurementBlock;
import net.eli.elimod.quantum.QbitSourceBlock;
import net.eli.elimod.quantum.QbitVoidBlock;
import net.eli.elimod.quantum.QbitWireBlock;
import net.eli.elimod.things.FragileBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block FRAGILE_BLOCK = registerBlock(
		"fragile_block",
		FragileBlock::new,
		AbstractBlock.Settings.create().sounds(BlockSoundGroup.GRASS),
		true
    );
	public static final Block QBIT_BLOCK = registerBlock(
		"qbit_block",
		QbitSourceBlock::new,
		AbstractBlock.Settings.create().nonOpaque(),
		true
	);
	public static final Block QBIT_WIRE = registerBlock(
		"qbit_wire",
		QbitWireBlock::new,
		AbstractBlock.Settings.create().nonOpaque(),
		true
	);
	// public static final Block QBIT_GATE = registerBlock(
	// 	"qbit_gate_x",
	// 	settings ->  new QbitGateBlock(settings, Qbit.SINGLE_GATES.get("X")),
	// 	AbstractBlock.Settings.create().nonOpaque(),
	// 	true
	// );
	// public static final Block[] QBIT_GATE_BLOCKS = {QBIT_GATE};
	public static final Block[] QBIT_GATE_BLOCKS = new Block[Qbit.SINGLE_GATES.size()];
	static{
		int i =0;
		for (Map.Entry<String, Gate> entry : Qbit.SINGLE_GATES.entrySet()) {
			String gatename = entry.getKey();
			Gate gate = entry.getValue();
			QBIT_GATE_BLOCKS[i] = registerBlock(
				"qbit_gate_" + gatename,
				settings ->  new QbitGateBlock(settings, gate),
				AbstractBlock.Settings.create().nonOpaque(),
				true
			);
			i++;
		}
	}
	public static final Block QBIT_CONTROL = registerBlock(
		"qbit_control",
		QbitControlBlock::new,
		AbstractBlock.Settings.create().nonOpaque(),
		true
	);
	public static final Block QBIT_MEASUREMENT = registerBlock(
		"qbit_measurement",
		QbitMeasurementBlock::new,
		AbstractBlock.Settings.create().nonOpaque(),
		true
	);
	public static final Block QBIT_VOID = registerBlock(
		"qbit_void",
		QbitVoidBlock::new,
		AbstractBlock.Settings.create().nonOpaque(),
		true
	);
	public static final Block[] QBIT_MAIN_BLOCKS = {QBIT_BLOCK, QBIT_WIRE, QBIT_CONTROL, QBIT_VOID, QBIT_MEASUREMENT};
	public static final Block[] QBIT_BLOCKS = Stream.concat(Arrays.stream(QBIT_MAIN_BLOCKS), Arrays.stream(QBIT_GATE_BLOCKS)).toArray(Block[]::new);


    public static void registerModBlocks() {
        Elimod.LOGGER.info("registering items");
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(itemGroup -> {
            // itemGroup.add(TEST_BLOCK.asItem());
            // itemGroup.add(QBIT_BLOCK.asItem());
            // itemGroup.add(QBIT_WIRE.asItem());
			// for (Block block : QBIT_GATE_BLOCKS) {
			// 	itemGroup.add(block.asItem());
			// }
            // itemGroup.add(QBIT_GATE.asItem());
        });
		ItemGroupEvents.modifyEntriesEvent(ModItemGroups.MISC_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(FRAGILE_BLOCK.asItem());
            // itemGroup.add(QBIT_GATE.asItem());
        });
		ItemGroupEvents.modifyEntriesEvent(ModItemGroups.QUANTUM_ITEM_GROUP_KEY).register(itemGroup -> {
			for (Block block : QBIT_BLOCKS) {
				itemGroup.add(block.asItem());
			}
            // itemGroup.add(QBIT_GATE.asItem());
        });
    }

    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
		RegistryKey<Block> blockKey = keyOfBlock(name);
		Block block = blockFactory.apply(settings.registryKey(blockKey));
		if (shouldRegisterItem) {
			RegistryKey<Item> itemKey = keyOfItem(name);
			BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
			Registry.register(Registries.ITEM, itemKey, blockItem);
		}
		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	private static RegistryKey<Block> keyOfBlock(String name) {
		return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Elimod.MOD_ID, name));
	}

	private static RegistryKey<Item> keyOfItem(String name) {
		return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Elimod.MOD_ID, name));
	}
}
