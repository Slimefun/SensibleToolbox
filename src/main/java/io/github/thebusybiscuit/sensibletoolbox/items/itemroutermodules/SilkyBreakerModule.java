package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class SilkyBreakerModule extends BreakerModule {

    private static final ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE, 1);

    static {
        pick.addEnchantment(Enchantment.SILK_TOUCH, 1);
    }

    public SilkyBreakerModule() {
        super();
    }

    public SilkyBreakerModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Silky Breaker";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Insert into an Item Router", "Silk touches a block in its", " configured direction and", " pulls it into the item router.", "NOTE: must use a Silk Touch", "  enchanted book to craft." };
    }

    @Override
    public Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        BreakerModule b = new BreakerModule();
        registerCustomIngredients(b);
        recipe.addIngredient(b.getMaterial());
        recipe.addIngredient(Material.ENCHANTED_BOOK);
        return recipe;
    }

    @Override
    public boolean validateCrafting(CraftingInventory inventory) {
        for (ItemStack stack : inventory.getMatrix()) {
            if (stack != null && stack.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
                if (meta.getStoredEnchantLevel(Enchantment.SILK_TOUCH) < 1) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected ItemStack getBreakerTool() {
        return pick;
    }
}
