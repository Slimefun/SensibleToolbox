package io.github.thebusybiscuit.sensibletoolbox.items.energycells;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.util.UnicodeSymbol;

public abstract class EnergyCell extends BaseSTBItem implements Chargeable {
    private static final MaterialData md = new MaterialData(Material.LEATHER_HELMET);

    private double charge;

    protected EnergyCell() {
        setCharge(0.0);
    }

    public EnergyCell(ConfigurationSection conf) {
        setCharge(conf.getDouble("charge"));
    }

    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("charge", getCharge());
        return conf;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String[] getLore() {
        return new String[]{"Stores up to " + UnicodeSymbol.ELECTRICITY.toUnicode() + " " + getMaxCharge() + " SCU"};
    }

    @Override
    public String[] getExtraLore() {
        return new String[]{STBUtil.getChargeString(this)};
    }

    @Override
    public abstract int getMaxCharge();

    public abstract Color getCellColor();

    @Override
    public double getCharge() {
        return charge;
    }

    @Override
    public void setCharge(double charge) {
        this.charge = charge;
    }

    @Override
    public boolean isWearable() {
        return false;
    }

    @Override
    public ItemStack toItemStack(int amount) {
        ItemStack res = super.toItemStack(amount);
        ItemMeta meta = res.getItemMeta();
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(getCellColor());
            res.setItemMeta(meta);
        }
        return res;
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            chargeHotbarItems(event.getPlayer());
            event.getPlayer().updateInventory();
        }
    }

    private void chargeHotbarItems(Player player) {
        if (getCharge() > 0) {
            int held = player.getInventory().getHeldItemSlot();
            for (int slot = 0; slot < 8; slot++) {
                if (slot == held)
                    continue;
                ItemStack stack = player.getInventory().getItem(slot);
                BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(stack);
                if (item != null && item instanceof Chargeable) {
                    Chargeable c = (Chargeable) item;
                    double toTransfer = Math.min(c.getMaxCharge() - c.getCharge(), c.getChargeRate());
                    if (toTransfer > 0) {
                        toTransfer = Math.min(toTransfer, getCharge());
                        setCharge(getCharge() - toTransfer);
                        player.setItemInHand(toItemStack());
                        c.setCharge(c.getCharge() + toTransfer);
                        player.getInventory().setItem(slot, item.toItemStack());
                        break;
                    }
                }
            }
        }
    }

}
