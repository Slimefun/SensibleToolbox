package me.mrCookieSlime.sensibletoolbox.slimefun;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.Slimefun.Lists.Categories;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.sensibletoolbox.slimefun.machines.NuclearReactor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class STBSlimefunItems {

	public static void load() {
		new SlimefunItem(Categories.TECH_MISC, new CustomItem(new MaterialData(Material.DISPENSER), "&bReactor Coolant Port", "", "&rAttach this to the Bottom of a Reactor"), "REACTOR_COOLANT_PORT", RecipeType.ENHANCED_CRAFTING_TABLE,
		new ItemStack[] {null, SlimefunItems.LEAD_INGOT, null, SlimefunItems.REINFORCED_PLATE, SlimefunItems.COOLING_UNIT, SlimefunItems.REINFORCED_PLATE, null, SlimefunItems.LEAD_INGOT, null})
		.register();
		
		new SlimefunItem(Categories.TECH_MISC, NuclearReactor.COOLANT_ITEM, "COOLANT_CELL", RecipeType.ENHANCED_CRAFTING_TABLE,
		new ItemStack[] {null, SlimefunItems.TIN_INGOT, null, SlimefunItems.TIN_INGOT, new ItemStack(Material.PACKED_ICE), SlimefunItems.TIN_INGOT, null, SlimefunItems.TIN_INGOT, null},
		new CustomItem(new CustomItem(NuclearReactor.COOLANT_ITEM, 16)))
		.register();
		
		new SlimefunItem(Categories.RESOURCES, NuclearReactor.NEPTUNIUM, "NEPTUNIUM", new RecipeType(new CustomItem(new MaterialData(Material.IRON_BLOCK), "&2Nuclear Reactor", "", "&rResult of burning up Uranium", "&rin a Nuclear Reactor")),
		new ItemStack[] {null, null, null, null, SlimefunItems.URANIUM, null, null, null, null})
		.register();
		
		new SlimefunItem(Categories.RESOURCES, NuclearReactor.PLUTONIUM, "PLUTONIUM", new RecipeType(new CustomItem(new MaterialData(Material.IRON_BLOCK), "&2Nuclear Reactor", "", "&rResult of burning up Neptunium", "&rin a Nuclear Reactor")),
		new ItemStack[] {null, null, null, null, NuclearReactor.NEPTUNIUM, null, null, null, null})
		.register();
	}

	
}
