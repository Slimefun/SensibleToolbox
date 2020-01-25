package me.desht.dhutils.cost;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.DHValidate;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyCost extends Cost {
	
	private static Economy economy;

	public EconomyCost(double quantity) {
		super(quantity);

		if (economy == null) {
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            DHValidate.notNull(vault, "Economy costs not available: Vault not installed");
            DHValidate.isTrue(vault.isEnabled(), "Economy costs not available: Vault not enabled");
            
            RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
            DHValidate.notNull(economyProvider, "Economy costs not available: no suitable Economy plugin detected");
            economy = economyProvider.getProvider();
		}
	}

	@Override
	public String getDescription() {
		return getEconomy().format(getQuantity());
	}

	@Override
	public boolean isAffordable(Player player) {
		return getEconomy().has(player, getQuantity());
	}

	@Override
	public void apply(Player player) {
		EconomyResponse resp;
		
		if (getQuantity() < 0.0) {
			resp = getEconomy().depositPlayer(player, -getQuantity());
		} 
		else {
			resp = getEconomy().withdrawPlayer(player, getQuantity());
		}
		
		if (!resp.transactionSuccess()) {
			throw new DHUtilsException("Economy problem: " + resp.errorMessage);
		}
	}

    /**
     * Get the Vault economy service used by this class.
     *
     * @return the Vault economy service
     */
	public static Economy getEconomy() {
		return economy;
	}
}
