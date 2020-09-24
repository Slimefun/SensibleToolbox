package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.dhutils.Debugger;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import java.util.UUID;

public class ReceiverModule extends ItemRouterModule {
    private static final Dye md = makeDye(DyeColor.ORANGE);

    public ReceiverModule() {
    }

    public ReceiverModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Receiver";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Insert into an Item Router",
                "Passive module; receives items",
                "from a facing Sender module OR",
                "linked Adv. Sender module"
        };
    }

    @Override
    public Recipe getRecipe() {
        BlankModule bm = new BlankModule();
        registerCustomIngredients(bm);
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
        recipe.addIngredient(bm.getMaterialData());
        recipe.addIngredient(Material.TRAP_DOOR);
        return recipe;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    public int receiveItem(ItemStack item, UUID senderUUID) {
        int received = getItemRouter().insertItems(item, BlockFace.SELF, false, senderUUID);
        if (received > 0) {
            Debugger.getInstance().debug(2, "receiver in " + getItemRouter() + " received " + received + " of " + item +
                    ", now has " + getItemRouter().getBufferItem());
        }
        return received;
    }
}
