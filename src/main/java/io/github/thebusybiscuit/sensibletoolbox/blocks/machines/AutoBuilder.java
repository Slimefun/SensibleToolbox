package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.SlotType;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.ButtonGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.CyclerGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;
import io.github.thebusybiscuit.sensibletoolbox.items.LandMarker;
import io.github.thebusybiscuit.sensibletoolbox.items.components.IntegratedCircuit;
import io.github.thebusybiscuit.sensibletoolbox.items.components.ToughMachineFrame;
import io.github.thebusybiscuit.sensibletoolbox.utils.ColoredMaterial;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.cuboid.Cuboid;
import me.desht.dhutils.cuboid.CuboidDirection;

public class AutoBuilder extends BaseSTBMachine {

    private static final int LANDMARKER_SLOT_1 = 10;
    private static final int LANDMARKER_SLOT_2 = 12;
    public static final int MODE_SLOT = 14;
    public static final int STATUS_SLOT = 15;
    private static final int START_BUTTON_SLOT = 53;
    private static final int MAX_DISTANCE = 5;

    private AutoBuilderMode buildMode;
    private Cuboid workArea;
    private int buildX;
    private int buildY;
    private int buildZ;

    // the inventory slot index (into getInputSlots()) being pulled from
    private int invSlot;
    private BuilderStatus status = BuilderStatus.NO_WORKAREA;
    private int baseScuPerOp;

    public AutoBuilder() {
        super();
        buildMode = AutoBuilderMode.CLEAR;
    }

    public AutoBuilder(ConfigurationSection conf) {
        super(conf);
        buildMode = AutoBuilderMode.valueOf(conf.getString("buildMode"));
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("buildMode", buildMode.toString());
        return conf;
    }

    @Override
    public int[] getInputSlots() {
        return new int[] { 38, 39, 40, 41, 42, 47, 48, 49, 50, 51 };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    protected boolean shouldPaintSlotSurrounds() {
        return false;
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[0];
    }

    @Override
    public int getUpgradeLabelSlot() {
        return -1;
    }

    @Override
    public int getEnergyCellSlot() {
        return 45;
    }

    @Override
    public int getChargeDirectionSlot() {
        return 36;
    }

    @Override
    public Material getMaterial() {
        return Material.YELLOW_TERRACOTTA;
    }

    @Override
    public String getItemName() {
        return "Auto Builder";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Can build or clear", "an area.  Use Land Markers", "to define the area" };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        ToughMachineFrame mf = new ToughMachineFrame();
        IntegratedCircuit ic = new IntegratedCircuit();
        registerCustomIngredients(mf, ic);
        recipe.shape("OCO", "DFP", "RGR");
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('C', ic.getMaterial());
        recipe.setIngredient('D', Material.DISPENSER);
        recipe.setIngredient('F', mf.getMaterial());
        recipe.setIngredient('P', Material.DIAMOND_PICKAXE);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public boolean acceptsEnergy(BlockFace face) {
        return true;
    }

    @Override
    public boolean suppliesEnergy(BlockFace face) {
        return false;
    }

    @Override
    public int getMaxCharge() {
        return 10000;
    }

    @Override
    public int getChargeRate() {
        return 50;
    }

    @Override
    public InventoryGUI createGUI() {
        InventoryGUI gui = super.createGUI();

        gui.setSlotType(LANDMARKER_SLOT_1, SlotType.ITEM);
        setupLandMarkerLabel(gui, null, null);
        gui.setSlotType(LANDMARKER_SLOT_2, SlotType.ITEM);

        gui.addGadget(new AutoBuilderGadget(gui, MODE_SLOT));
        gui.addGadget(new ButtonGadget(gui, START_BUTTON_SLOT, "Start", null, null, () -> {
            if (getStatus() == BuilderStatus.RUNNING) {
                stop(false);
            } else {
                startup();
            }
        }));

        ChatColor c = STBUtil.dyeColorToChatColor(status.getColor());
        gui.addLabel(ChatColor.WHITE + "Status: " + c + status, STATUS_SLOT, status.makeTexture(), status.getText());

        gui.addLabel("Building Inventory", 29, null);

        return gui;
    }

    @Nonnull
    public AutoBuilderMode getBuildMode() {
        return buildMode;
    }

    public void setBuildMode(@Nonnull AutoBuilderMode buildMode) {
        Validate.notNull(buildMode, "The Build mode cannot be null");
        this.buildMode = buildMode;
        setStatus(workArea == null ? BuilderStatus.NO_WORKAREA : BuilderStatus.READY);
    }

    private void startup() {
        baseScuPerOp = getItemConfig().getInt("scu_per_op");
        if (workArea == null) {
            BuilderStatus bs = setupWorkArea();
            if (bs != BuilderStatus.READY) {
                setStatus(bs);
                return;
            }
        }
        if (getStatus().resetBuildPosition()) {
            buildX = workArea.getLowerX();
            buildY = getBuildMode().yDirection < 0 ? workArea.getUpperY() : workArea.getLowerY();
            buildZ = workArea.getLowerZ();
        }

        if (getBuildMode() != AutoBuilderMode.CLEAR && initInventoryPointer()) {
            setStatus(BuilderStatus.NO_INVENTORY);
        } else {
            setStatus(BuilderStatus.RUNNING);
        }
    }

    private void stop(boolean finished) {
        setStatus(finished ? BuilderStatus.FINISHED : BuilderStatus.PAUSED);
    }

    private boolean initInventoryPointer() {
        invSlot = -1;

        for (int slot = 0; slot < getInputSlots().length; slot++) {
            if (getInventoryItem(getInputSlots()[slot]) != null) {
                invSlot = slot;
                return true;
            }
        }

        return false;
    }

    @Nonnull
    public BuilderStatus getStatus() {
        return status;
    }

    private void setStatus(@Nonnull BuilderStatus status) {
        Validate.notNull(status, "The Status cannot be null");

        if (status != this.status) {
            this.status = status;
            ChatColor c = STBUtil.dyeColorToChatColor(status.getColor());
            getGUI().addLabel(ChatColor.WHITE + "Status: " + c + status, STATUS_SLOT, status.makeTexture(), status.getText());
            updateAttachedLabelSigns();
        }
    }

    @Override
    public void onServerTick() {
        if (isRedstoneActive() && getStatus() == BuilderStatus.RUNNING && workArea != null) {
            Block b = getLocation().getWorld().getBlockAt(buildX, buildY, buildZ);
            double scuNeeded = 0.0;
            boolean advanceBuildPos = true;
            OfflinePlayer owner = Bukkit.getOfflinePlayer(getOwner());

            switch (getBuildMode()) {
                case CLEAR:
                    if (!SensibleToolbox.getProtectionManager().hasPermission(owner, b, ProtectableAction.BREAK_BLOCK)) {
                        setStatus(BuilderStatus.NO_PERMISSION);
                        return;
                    }

                    // just skip over any "unbreakable" blocks (bedrock, ender portal etc.)
                    if (b.getType().getHardness() < 3600000) {
                        scuNeeded = baseScuPerOp * b.getType().getHardness();

                        if (scuNeeded > getCharge()) {
                            advanceBuildPos = false;
                        } else if (b.getType() != Material.AIR) {
                            b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
                            BaseSTBBlock stb = SensibleToolbox.getBlockAt(b.getLocation());

                            if (stb != null) {
                                stb.breakBlock(false);
                            } else {
                                b.setType(Material.AIR);
                            }
                        }
                    }
                    break;
                case FILL:
                case WALLS:
                case FRAME:
                    if (!SensibleToolbox.getProtectionManager().hasPermission(owner, b, ProtectableAction.PLACE_BLOCK)) {
                        setStatus(BuilderStatus.NO_PERMISSION);
                        return;
                    }

                    if (shouldBuildHere()) {
                        scuNeeded = baseScuPerOp;
                        if (scuNeeded > getCharge()) {
                            advanceBuildPos = false;
                        } else if (b.isEmpty() || b.isLiquid()) {
                            ItemStack item = fetchNextBuildItem();

                            if (item == null) {
                                setStatus(BuilderStatus.NO_INVENTORY);
                                advanceBuildPos = false;
                            } else {
                                b.setType(item.getType());
                                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
                            }
                        }
                    }
                    break;
            }

            if (scuNeeded <= getCharge()) {
                setCharge(getCharge() - scuNeeded);
            }

            if (advanceBuildPos) {
                buildX++;

                if (buildX > workArea.getUpperX()) {
                    buildX = workArea.getLowerX();
                    buildZ++;

                    if (buildZ > workArea.getUpperZ()) {
                        buildZ = workArea.getLowerZ();
                        buildY += getBuildMode().getYDirection();

                        if (getBuildMode().getYDirection() < 0 && buildY < workArea.getLowerY() || getBuildMode().getYDirection() > 0 && buildY > workArea.getUpperY()) {
                            // finished!
                            stop(true);
                        }
                    }
                }
            }
        }

        super.onServerTick();
    }

    private boolean shouldBuildHere() {
        switch (getBuildMode()) {
            case FILL:
                return true;
            case WALLS:
                return onOuterFace();
            case FRAME:
                return onOuterEdge();
            default:
                return false;
        }
    }

    private boolean onOuterFace() {
        return isXonEdge() || isYonEdge() || isZonEdge();
    }

    private boolean onOuterEdge() {
        return isXonEdge() && isYonEdge() || isXonEdge() && isZonEdge() || isYonEdge() && isZonEdge();
    }

    private boolean isXonEdge() {
        return buildX <= workArea.getLowerX() || buildX >= workArea.getUpperX();
    }

    private boolean isYonEdge() {
        return buildY <= workArea.getLowerY() || buildY >= workArea.getUpperY();
    }

    private boolean isZonEdge() {
        return buildZ <= workArea.getLowerZ() || buildZ >= workArea.getUpperZ();
    }

    private ItemStack fetchNextBuildItem() {
        ItemStack stack = getGUI().getItem(getInputSlots()[invSlot]);

        if (stack == null) {
            int scanSlot = invSlot;

            for (int i = 0; i < getInputSlots().length; i++) {
                scanSlot++;

                if (scanSlot >= getInputSlots().length) {
                    scanSlot = 0;
                }

                if (getGUI().getItem(getInputSlots()[scanSlot]) != null) {
                    break;
                }
            }

            if (scanSlot == invSlot) {
                return null;
            } else {
                invSlot = scanSlot;
                stack = getGUI().getItem(getInputSlots()[invSlot]);
            }
        }

        if (stack.getAmount() == 1) {
            setInventoryItem(getInputSlots()[invSlot], null);
            return stack;
        } else {
            ItemStack res = stack.clone();
            res.setAmount(1);
            stack.setAmount(stack.getAmount() - 1);
            setInventoryItem(getInputSlots()[invSlot], stack);
            return res;
        }
    }

    @Override
    public void setInventoryItem(int slot, ItemStack item) {
        super.setInventoryItem(slot, item);
        if (slot == LANDMARKER_SLOT_1 || slot == LANDMARKER_SLOT_2) {
            BuilderStatus bs = setupWorkArea();
            setStatus(bs);
        }
    }

    private BuilderStatus setupWorkArea() {
        workArea = null;

        LandMarker lm1 = SensibleToolbox.getItemRegistry().fromItemStack(getInventoryItem(LANDMARKER_SLOT_1), LandMarker.class);
        LandMarker lm2 = SensibleToolbox.getItemRegistry().fromItemStack(getInventoryItem(LANDMARKER_SLOT_2), LandMarker.class);

        if (lm1 != null && lm2 != null) {
            Location loc1 = lm1.getMarkedLocation();
            Location loc2 = lm2.getMarkedLocation();

            if (!loc1.getWorld().equals(loc2.getWorld())) {
                return BuilderStatus.LM_WORLDS_DIFFERENT;
            }

            Location ourLoc = getLocation();
            Cuboid w = new Cuboid(loc1, loc2);

            if (w.contains(ourLoc)) {
                return BuilderStatus.TOO_NEAR;
            }

            if (!w.outset(CuboidDirection.BOTH, MAX_DISTANCE).contains(ourLoc)) {
                return BuilderStatus.TOO_FAR;
            }

            workArea = w;
            setupLandMarkerLabel(getGUI(), loc1, loc2);
            return BuilderStatus.READY;
        } else {
            setupLandMarkerLabel(getGUI(), null, null);
            return BuilderStatus.NO_WORKAREA;
        }
    }

    private void setupLandMarkerLabel(InventoryGUI gui, Location loc1, Location loc2) {
        if (workArea == null) {
            gui.addLabel("Land Markers", 11, null, "Place two Land Markers", "in these slots, set", "to two opposite corners", "of the area to work.");
        } else {
            int v = workArea.getVolume();
            String s = v == 1 ? "" : "s";
            gui.addLabel("Land Markers", 11, null, "Work Area:", MiscUtil.formatLocation(loc1), MiscUtil.formatLocation(loc2), v + " block" + s);
        }
    }

    @Override
    public boolean acceptsItemType(ItemStack stack) {
        // solid blocks, no special metadata
        return stack.getType().isSolid() && !stack.hasItemMeta();
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        if (slot == LANDMARKER_SLOT_1 || slot == LANDMARKER_SLOT_2) {
            if (getStatus() != BuilderStatus.RUNNING) {
                if (onCursor.getType() != Material.AIR) {
                    LandMarker item = SensibleToolbox.getItemRegistry().fromItemStack(onCursor, LandMarker.class);
                    if (item != null) {
                        ItemStack stack = onCursor.clone();
                        stack.setAmount(1);
                        getGUI().getInventory().setItem(slot, stack);
                    }
                } else if (inSlot != null) {
                    getGUI().getInventory().setItem(slot, null);
                }
                setStatus(setupWorkArea());
            }

            return false; // we just put a copy of the land marker into the builder
        } else {
            return super.onSlotClick(player, slot, click, inSlot, onCursor);
        }
    }

    @Override
    public boolean onPlayerInventoryClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return true;
    }

    @Override
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(toInsert);

        if (item instanceof LandMarker && getStatus() != BuilderStatus.RUNNING) {
            if (((LandMarker) item).getMarkedLocation() != null) {
                insertLandMarker(toInsert);
            } else {
                STBUtil.complain((Player) player, "Land Marker doesn't have a location set!");
            }
            return 0; // we just put a copy of the land marker into the builder
        } else {
            return super.onShiftClickInsert(player, slot, toInsert);
        }
    }

    private void insertLandMarker(ItemStack toInsert) {
        ItemStack stack = toInsert.clone();
        stack.setAmount(1);

        if (getInventoryItem(LANDMARKER_SLOT_1) == null) {
            setInventoryItem(LANDMARKER_SLOT_1, stack);
            setStatus(setupWorkArea());
        } else if (getInventoryItem(LANDMARKER_SLOT_2) == null) {
            setInventoryItem(LANDMARKER_SLOT_2, stack);
            setStatus(setupWorkArea());
        }
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        if ((slot == LANDMARKER_SLOT_1 || slot == LANDMARKER_SLOT_2) && getStatus() != BuilderStatus.RUNNING) {
            setInventoryItem(slot, null);
            setStatus(BuilderStatus.NO_WORKAREA);
            return false;
        } else {
            return super.onShiftClickExtract(player, slot, toExtract);
        }
    }

    @Override
    public boolean onClickOutside(HumanEntity player) {
        return false;
    }

    @Override
    public void onGUIClosed(HumanEntity player) {
        if (player instanceof Player) {
            highlightWorkArea((Player) player);
        }
    }

    private void highlightWorkArea(@Nonnull Player p) {
        if (workArea != null) {
            Block[] corners = workArea.getCorners();

            for (Block b : corners) {
                p.sendBlockChange(b.getLocation(), Material.LIME_STAINED_GLASS.createBlockData());
            }

            Bukkit.getScheduler().runTaskLater(getProviderPlugin(), () -> {
                if (p.isOnline()) {
                    for (Block b : corners) {
                        p.sendBlockChange(b.getLocation(), Material.GREEN_STAINED_GLASS.createBlockData());
                    }
                }
            }, 25L);

            Bukkit.getScheduler().runTaskLater(getProviderPlugin(), () -> {
                if (p.isOnline()) {
                    for (Block b : corners) {
                        p.sendBlockChange(b.getLocation(), b.getBlockData());
                    }
                }
            }, 50L);
        }
    }

    @Override
    protected String[] getSignLabel(BlockFace face) {
        String[] label = super.getSignLabel(face);
        label[2] = "-( " + STBUtil.dyeColorToChatColor(getStatus().getColor()) + "⬤" + ChatColor.WHITE + " )-";
        return label;
    }

    private enum AutoBuilderMode {

        CLEAR(-1),
        FILL(1),
        WALLS(1),
        FRAME(1);

        private final int yDirection;

        AutoBuilderMode(int yDir) {
            this.yDirection = yDir;
        }

        public int getYDirection() {
            return yDirection;
        }
    }

    private enum BuilderStatus {

        READY(DyeColor.LIME, "Ready to Operate!"),
        NO_WORKAREA(DyeColor.YELLOW, "No work area has", "been defined yet"),
        NO_INVENTORY(DyeColor.RED, "Out of building material!", "Place more blocks in", "the inventory and", "press Start to resume"),
        NO_PERMISSION(DyeColor.RED, "Builder doesn't have", "building rights in", "this area"),
        TOO_NEAR(DyeColor.RED, "Auto Builder is inside", "the work area!"),
        TOO_FAR(DyeColor.RED, "Auto Builder is too far", "away from the work area!", "Place it " + MAX_DISTANCE + " blocks or less from", "the edge of the work area"),
        LM_WORLDS_DIFFERENT(DyeColor.RED, "Land Markers are", "from different worlds!"),
        RUNNING(DyeColor.LIGHT_BLUE, "Builder is running", "Press Start button to pause"),
        PAUSED(DyeColor.ORANGE, "Builder has been paused", "Press Start button to resume"),
        FINISHED(DyeColor.WHITE, "Builder has finished!", "Ready for next operation");

        private final DyeColor color;
        private final String[] text;

        BuilderStatus(@Nonnull DyeColor color, String... label) {
            this.color = color;
            this.text = label;
        }

        @Nonnull
        public String[] getText() {
            return text;
        }

        @Nonnull
        public ItemStack makeTexture() {
            return new ItemStack(ColoredMaterial.WOOL.get(color.ordinal()));
        }

        @Nonnull
        public DyeColor getColor() {
            return color;
        }

        public boolean resetBuildPosition() {
            // returning true causes the build position to be reset
            // when the Start button is pressed
            return this != PAUSED && this != NO_INVENTORY;
        }
    }

    private class AutoBuilderGadget extends CyclerGadget<AutoBuilderMode> {

        protected AutoBuilderGadget(InventoryGUI gui, int slot) {
            super(gui, slot, "Build Mode");
            add(AutoBuilderMode.CLEAR, ChatColor.YELLOW, Material.WHITE_STAINED_GLASS, "Clear all blocks", "in the work area");
            add(AutoBuilderMode.FILL, ChatColor.YELLOW, Material.BRICK, "Use inventory to replace", "all empty blocks", "in the work area");
            add(AutoBuilderMode.WALLS, ChatColor.YELLOW, Material.COBBLESTONE_WALL, "Use inventory to build", "walls around the", "the work area");
            add(AutoBuilderMode.FRAME, ChatColor.YELLOW, Material.OAK_FENCE, "Use inventory to build", "a frame around the", "the work area");
            setInitialValue(((AutoBuilder) gui.getOwningBlock()).getBuildMode());
        }

        @Override
        protected boolean ownerOnly() {
            return false;
        }

        @Override
        protected void apply(BaseSTBItem stbItem, AutoBuilderMode newValue) {
            ((AutoBuilder) getGUI().getOwningBlock()).setBuildMode(newValue);
        }

        @Override
        public boolean isEnabled() {
            // no changing build mode in mid-operation
            return getStatus() != BuilderStatus.RUNNING;
        }
    }
}
