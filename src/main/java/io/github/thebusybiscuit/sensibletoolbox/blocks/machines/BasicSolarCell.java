package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.cscorelib2.materials.MaterialCollections;
import io.github.thebusybiscuit.sensibletoolbox.api.LightMeterHolder;
import io.github.thebusybiscuit.sensibletoolbox.api.RedstoneBehaviour;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.ChargeDirection;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.GUIUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.LightMeter;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;
import io.github.thebusybiscuit.sensibletoolbox.items.PVCell;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.util.SunlightLevels;
import io.github.thebusybiscuit.sensibletoolbox.util.UnicodeSymbol;

public class BasicSolarCell extends BaseSTBMachine implements LightMeterHolder {

    private static final int PV_CELL_SLOT = 1;
    private static final int LIGHT_METER_SLOT = 13;

    private byte effectiveLightLevel;
    private int lightMeterId;
    private int pvCellLife;

    public BasicSolarCell() {
        pvCellLife = 0;
        setChargeDirection(ChargeDirection.CELL);
    }

    public BasicSolarCell(ConfigurationSection conf) {
        super(conf);
        pvCellLife = conf.getInt("pvCellLife", 0);
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("pvCellLife", pvCellLife);
        return conf;
    }

    @Override
    public int[] getInputSlots() {
        return new int[] { 1 };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] { 1 };
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[0]; // maybe one day
    }

    @Override
    public int getUpgradeLabelSlot() {
        return -1;
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        boolean res = super.onSlotClick(player, slot, click, inSlot, onCursor);

        if (res) {
            rescanPVCell();
        }

        return res;
    }

    @Override
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        int inserted = super.onShiftClickInsert(player, slot, toInsert);

        if (inserted > 0) {
            rescanPVCell();
        }

        return inserted;
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        boolean res = super.onShiftClickExtract(player, slot, toExtract);

        if (res) {
            rescanPVCell();
        }

        return res;
    }

    @Override
    public void onGUIOpened(HumanEntity player) {
        drawPVCell(getGUI());
    }

    @Override
    public boolean acceptsItemType(ItemStack item) {
        return SensibleToolbox.getItemRegistry().isSTBItem(item, PVCell.class);
    }

    @Override
    public int insertItems(ItemStack toInsert, BlockFace side, boolean sorting, UUID uuid) {
        int n = super.insertItems(toInsert, side, sorting, uuid);

        if (n > 0) {
            rescanPVCell();
        }

        return n;
    }

    @Override
    public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid) {
        ItemStack stack = super.extractItems(face, receiver, amount, uuid);
        if (stack != null) {
            rescanPVCell();
        }
        return stack;
    }

    @Override
    public void repaint(Block block) {
        super.repaint(block);
        drawPVLayer(block.getRelative(BlockFace.UP));
    }

    private void rescanPVCell() {
        // defer this since we need to ensure the inventory slot is actually updated
        Bukkit.getScheduler().runTask(getProviderPlugin(), () -> {
            PVCell cell = SensibleToolbox.getItemRegistry().fromItemStack(getGUI().getItem(PV_CELL_SLOT), PVCell.class);
            int pvl = cell == null ? 0 : cell.getLifespan();

            if (pvl != pvCellLife) {
                boolean doRedraw = pvl == 0 || pvCellLife == 0;
                pvCellLife = pvl;
                update(doRedraw);
                getLightMeter().repaintNeeded();
            }
        });
    }

    private void drawPVCell(InventoryGUI gui) {
        if (pvCellLife > 0) {
            PVCell pvCell = new PVCell();
            pvCell.setLifespan(pvCellLife);
            gui.setItem(PV_CELL_SLOT, pvCell.toItemStack());
        }
        else {
            gui.setItem(PV_CELL_SLOT, null);
        }
    }

    @Override
    protected void playActiveParticleEffect() {
        // nothing
    }

    @Override
    public Material getMaterial() {
        return Material.LIGHT_GRAY_STAINED_GLASS;
    }

    @Override
    public String getItemName() {
        return "Basic Solar";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Generates up to " + getPowerOutput() + " SCU/t", "while outside in bright sunlight", UnicodeSymbol.ARROW_UP.toUnicode() + " + L-click block (empty hand): ", ChatColor.WHITE + "  - extract PV cell" };
    }

    @Override
    public String[] getExtraLore() {
        String[] l = super.getExtraLore();
        String[] l2 = new String[l.length + 1];
        System.arraycopy(l, 0, l2, 0, l.length);

        if (pvCellLife == 0) {
            l2[l.length] = ChatColor.GRAY.toString() + ChatColor.ITALIC + "No PV Cell installed";
        }
        else {
            l2[l.length] = PVCell.formatCellLife(pvCellLife);
        }
        return l2;
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("DDD", "IQI", "RGR");
        recipe.setIngredient('D', Material.DAYLIGHT_DETECTOR);
        recipe.setIngredient('I', sc.getMaterial());
        recipe.setIngredient('Q', Material.QUARTZ_BLOCK);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public int getMaxCharge() {
        return 100;
    }

    @Override
    public int getChargeRate() {
        return 5;
    }

    @Override
    public int getEnergyCellSlot() {
        return 18;
    }

    @Override
    public int getChargeDirectionSlot() {
        return 19;
    }

    @Override
    public int getInventoryGUISize() {
        return 27;
    }

    @Override
    public int getTickRate() {
        return 20;
    }

    private void drawPVLayer(Block b) {
        // put a carpet on top of the main block to represent the PV cell
        DyeColor color = pvCellLife > 0 ? getCapColour() : DyeColor.GRAY;
        Material carpet = MaterialCollections.getAllCarpetColors().get(color.ordinal());
        b.setType(carpet);
    }

    @Override
    public void onBlockRegistered(Location location, boolean isPlacing) {
        if (isPlacing) {
            drawPVLayer(location.getBlock().getRelative(BlockFace.UP));
        }
        super.onBlockRegistered(location, isPlacing);
    }

    @Override
    public void onBlockUnregistered(Location location) {
        // remove any pv cell in the gui; pv level is stored separately
        getGUI().setItem(PV_CELL_SLOT, null);

        super.onBlockUnregistered(location);
    }

    protected DyeColor getCapColour() {
        return DyeColor.BLUE;
    }

    @Override
    public RelativePosition[] getBlockStructure() {
        return new RelativePosition[] { new RelativePosition(0, 1, 0) };
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null && event.getAction() == Action.LEFT_CLICK_BLOCK && player.isSneaking()) {
            ItemStack stack = extractItems(event.getBlockFace(), null, 1, event.getPlayer().getUniqueId());
            if (stack != null) {
                Block block = event.getClickedBlock();
                block.getWorld().dropItemNaturally(block.getLocation(), stack);
                player.playSound(block.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.6f);
            }
        }
        super.onInteractBlock(event);
    }

    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        // ensure carpet layer doesn't get popped off (and thus not cleared) when block is broken
        if (Tag.CARPETS.isTagged(event.getBlock().getType())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onServerTick() {
        calculateLightLevel();

        if (pvCellLife > 0 && getCharge() < getMaxCharge() && isRedstoneActive()) {
            double toAdd = getPowerOutput() * getTickRate() * getChargeMultiplier(getLightLevel());

            if (toAdd > 0) {
                setCharge(getCharge() + toAdd);
                pvCellLife = Math.min(PVCell.MAX_LIFESPAN, Math.max(0, pvCellLife - getTickRate()));

                if (pvCellLife == 0) {
                    update(true);
                }
                if (!getGUI().getViewers().isEmpty()) {
                    drawPVCell(getGUI());
                }
            }
        }

        getLightMeter().doRepaint();

        super.onServerTick();
    }

    private LightMeter getLightMeter() {
        return (LightMeter) getGUI().getMonitor(lightMeterId);
    }

    private void calculateLightLevel() {
        Block b = getLocation().getBlock().getRelative(BlockFace.UP);
        byte newLight = SunlightLevels.getSunlightLevel(b.getWorld());
        byte lightFromSky = b.getLightFromSky();

        if (lightFromSky < 14) {
            newLight = 0; // block is excessively shaded
        }
        else if (lightFromSky < 15) {
            newLight--; // partially shaded
        }

        if (b.getWorld().hasStorm()) {
            newLight -= 3; // raining: big efficiency drop
        }

        if (newLight < 0) {
            newLight = 0;
        }

        if (newLight != effectiveLightLevel) {
            getLightMeter().repaintNeeded();
            effectiveLightLevel = newLight;
        }
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = super.createGUI();

        gui.addLabel("PV Cell", 0, null, "Insert a PV Cell Here");
        gui.setSlotType(PV_CELL_SLOT, InventoryGUI.SlotType.ITEM);

        drawPVCell(gui);

        lightMeterId = gui.addMonitor(new LightMeter(gui));

        return gui;
    }

    @Override
    protected boolean shouldPaintSlotSurrounds() {
        return false;
    }

    public byte getLightLevel() {
        return effectiveLightLevel;
    }

    @Override
    public int getLightMeterSlot() {
        return LIGHT_METER_SLOT;
    }

    @Override
    public ItemStack getLightMeterIndicator() {
        if (pvCellLife == 0) {
            return GUIUtil.makeTexture(Material.BLACK_WOOL, ChatColor.WHITE + "No PV Cell inserted!", ChatColor.GRAY + "Insert a PV Cell in the top left");
        }
        else {
            DyeColor dc = colors[effectiveLightLevel];
            ChatColor cc = STBUtil.dyeColorToChatColor(dc);
            double mult = getChargeMultiplier(effectiveLightLevel);

            return GUIUtil.makeTexture(MaterialCollections.getAllWoolColors().get(dc.ordinal()), ChatColor.WHITE + "Efficiency: " + cc + (int) (getChargeMultiplier(effectiveLightLevel) * 100) + "%", ChatColor.GRAY + "Power Output: " + getPowerOutput() * mult + " SCU/t");
        }
    }

    /**
     * Return the maximum SCU per tick that this solar can generate. Note
     * time of day, weather and sky visibilty can all reduce the actual power
     * output.
     *
     * @return the maximum power output in SCU per tick
     */
    protected double getPowerOutput() {
        return 0.5;
    }

    private double getChargeMultiplier(byte light) {
        switch (light) {
        case 15:
            return 1.0;
        case 14:
            return 0.75;
        case 13:
            return 0.5;
        case 12:
            return 0.25;
        default:
            return 0.0;
        }
    }

    @Override
    public boolean acceptsEnergy(BlockFace face) {
        return false;
    }

    @Override
    public boolean suppliesEnergy(BlockFace face) {
        return true;
    }

    @Override
    public boolean supportsRedstoneBehaviour(RedstoneBehaviour behaviour) {
        return behaviour != RedstoneBehaviour.PULSED;
    }

    private static final DyeColor[] colors = new DyeColor[16];

    static {
        colors[15] = DyeColor.LIME;
        colors[14] = DyeColor.YELLOW;
        colors[13] = DyeColor.ORANGE;
        colors[12] = DyeColor.RED;
        for (int i = 0; i < 12; i++) {
            colors[i] = DyeColor.GRAY;
        }
    }
}
