package net.eli.elimod.setup;

import net.eli.elimod.Elimod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final RegistryKey<ItemGroup> MISC_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(Elimod.MOD_ID, "itemgroup.misc"));
    public static final ItemGroup MISC_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.FRAGILE_BLOCK.asItem()))
            .displayName(Text.translatable("miscellaneous"))
            .build();
    public static final RegistryKey<ItemGroup> QUANTUM_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(Elimod.MOD_ID, "itemgroup.quantum"));
    public static final ItemGroup QUANTUM_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(Items.NETHER_STAR))
            .displayName(Text.translatable("quantum"))
            .build();

            // Register the group.
    
    public static void RegisterItemGroups(){
        Registry.register(Registries.ITEM_GROUP, MISC_ITEM_GROUP_KEY, MISC_ITEM_GROUP);
        Registry.register(Registries.ITEM_GROUP, QUANTUM_ITEM_GROUP_KEY, QUANTUM_ITEM_GROUP);
    }
}
