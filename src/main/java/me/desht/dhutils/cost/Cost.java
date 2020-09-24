package me.desht.dhutils.cost;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public abstract class Cost {

    private final double quantity;

    protected Cost(double quantity) {
        this.quantity = quantity;
    }

    public double getQuantity() {
        return quantity;
    }

    /**
     * Parse a cost specification and return a new cost object.
     *
     * @param costSpec
     *            the cost specification, in the format <i>id[:data],quantity[,D]</i>
     * @return the new Cost object
     * @throws IllegalArgumentException
     *             if the specification is not valid
     */
    public static Cost parse(String costSpec) {
        String[] itemAndQuantity = costSpec.split(",");
        double q = itemAndQuantity.length < 2 ? 1.0 : Double.parseDouble(itemAndQuantity[1]);

        String[] typeAndData = itemAndQuantity[0].split(":");
        Validate.isTrue(typeAndData.length >= 1 && typeAndData.length <= 2, "cost: item format must be <material[:data]>");
        String costType = typeAndData[0].toUpperCase();
        short data = typeAndData.length == 2 ? Short.parseShort(typeAndData[1]) : 0;

        if (costType.equals("F")) {
            return new FoodCost(q);
        }
        else if (costType.equals("H")) {
            return new HealthCost(q);
        }
        else if (costType.equals("X")) {
            return new ExperienceCost(q);
        }
        else if (costType.length() > 1) {
            // could be a material name or potion name
            Material mat = Material.matchMaterial(costType);
            if (mat != null) {
                if (itemAndQuantity.length > 2 && itemAndQuantity[2].toUpperCase().startsWith("D")) {
                    // item durability
                    return new DurabilityCost(mat, q);
                }
                else {
                    // it's a material name
                    return new ItemCost(mat, (int) q);
                }
            }
            else {
                PotionEffectType pt = PotionEffectType.getByName(costType);
                if (pt != null) {
                    // it's a potion name
                    return new PotionCost(pt, data, q);
                }
                else {
                    throw new IllegalArgumentException("Cost: unknown material or potion type: '" + costType + "'");
                }
            }
        }
        else {
            throw new IllegalArgumentException("Cost: unknown cost type '" + costType + "'");
        }
    }

    /**
     * Charge a list of costs to the given player.
     *
     * @param player
     *            the player to apply the costs to
     * @param costs
     *            a list of costs
     */
    public static void apply(Player player, List<Cost> costs) {
        for (Cost c : costs) {
            c.apply(player);
        }
    }

    /**
     * Check if the costs are applicable to the given player.
     *
     * @param player
     *            the player to check
     * @param costs
     *            a list of costs
     * @return true if the costs are applicable, false otherwise
     */
    public static boolean isApplicable(Player player, List<Cost> costs) {
        for (Cost c : costs) {
            if (!c.isApplicable(player)) return false;
        }
        return true;
    }

    /**
     * Check if the player can afford to pay the costs.
     *
     * @param player
     *            the player to check
     * @param costs
     *            a list of costs
     * @return true if the costs are affordable, false otherwise
     */
    public static boolean isAffordable(Player player, List<Cost> costs) {
        for (Cost c : costs) {
            if (!c.isAffordable(player)) return false;
        }
        return true;
    }

    /**
     * Get a printable description of what this cost is, suitable for display
     * to a player.
     *
     * @return a printable description of the cost
     */
    public abstract String getDescription();

    /**
     * Check if this cost is affordable to the player.
     *
     * @param player
     *            the player to check for
     * @return true if the player can afford this cost; false otherwise
     */
    public abstract boolean isAffordable(Player player);

    /**
     * Apply this cost to the player. This will take whatever resources the
     * implementing costs specifies. A check for affordability is not done here;
     * {@link #isAffordable(org.bukkit.entity.Player)} should be called just before
     * this call is made.
     *
     * @param player
     *            the player to take the cost from
     */
    public abstract void apply(Player player);

    /**
     * Check if applying this cost to the given player actually makes sense.
     * For example a durability cost would not make sense if the item specified
     * does not have a damage bar.
     *
     * @param player
     *            the player to check for
     * @return true if the cost makes sense; false otherwise
     */
    public boolean isApplicable(Player player) {
        return true;
    }

    protected double getAdjustedQuantity(int original, double adjust, double min, double max) {
        double newQuantity = original - adjust;

        if (newQuantity < min) {
            newQuantity = min;
        }
        else if (newQuantity > max) {
            newQuantity = max;
        }

        return newQuantity;
    }
}
