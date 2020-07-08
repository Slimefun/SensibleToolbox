package me.desht.dhutils.cost;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class HealthCost extends Cost {

    protected HealthCost(double quantity) {
        super(quantity);
    }

    @Override
    public String getDescription() {
        return getQuantity() + " health";
    }

    @Override
    public boolean isAffordable(Player player) {
        return player.getHealth() > getQuantity();
    }

    @Override
    public void apply(Player player) {
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double minHealth = getQuantity() > maxHealth ? 0.0 : 1.0;
        player.setHealth(getAdjustedQuantity((int) player.getHealth(), getQuantity(), minHealth, maxHealth));
    }

}
