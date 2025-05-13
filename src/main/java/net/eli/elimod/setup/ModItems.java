package net.eli.elimod.setup;

import java.util.function.Function;

import net.eli.elimod.Elimod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.model.Model;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item Y_SWORD = registerItem("y_sword", Item::new, new Item.Settings());

    public static Item registerItem(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Elimod.MOD_ID, name));
		Item item = itemFactory.apply(settings.registryKey(itemKey));
		Registry.register(Registries.ITEM, itemKey, item);
		return item;
	}


    public static void registerModItems() {
        Elimod.LOGGER.info("registering items");
        ItemGroupEvents.modifyEntriesEvent(ModItemGroups.MISC_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(Y_SWORD);
        });
    }
    
}
