package me.desht.dhutils.cost;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionCost extends Cost {

    private final PotionEffectType effectType;
    private final short magnitude;

    public PotionCost(PotionEffectType effectType, short magnitude, double ticks) {
        super(ticks);
        this.effectType = effectType;
        this.magnitude = magnitude;
    }

    public PotionEffectType getEffectType() {
        return effectType;
    }

    public short getMagnitude() {
        return magnitude;
    }

    @Override
    public String getDescription() {
        if (getMagnitude() > 1) {
            return getEffectType() + " " + getMagnitude() + " [" + getQuantity() + " ticks]";
        }
        else {
            return getEffectType() + " [" + getQuantity() + " ticks]";
        }
    }

    @Override
    public boolean isAffordable(Player player) {
        return true;
    }

    @Override
    public void apply(Player player) {
        if (player.hasPotionEffect(getEffectType())) {
            player.removePotionEffect(getEffectType());
        }
        if (getQuantity() > 0) {
            int amp = Math.max(getMagnitude() - 1, 0);
            player.addPotionEffect(new PotionEffect(getEffectType(), (int) getQuantity() * 20, amp));
        }
    }
}
