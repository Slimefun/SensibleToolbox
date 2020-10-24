package io.github.thebusybiscuit.sensibletoolbox.blocks.router;

import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.ItemRouterModule;

/**
 * This represents a module instance which was installed in an {@link ItemRouter}.
 * 
 * @author desht
 * @author TheBusyBiscuit
 *
 */
class InstalledModule {

    private final ItemRouterModule module;
    private final int amount;

    InstalledModule(@Nonnull ItemRouterModule module, int amount) {
        this.module = module;
        this.amount = amount;
    }

    @Nonnull
    public ItemRouterModule getModule() {
        return module;
    }

    public int getAmount() {
        return amount;
    }

    @Nonnull
    public ItemStack toItemStack() {
        return module.toItemStack(amount);
    }
}
