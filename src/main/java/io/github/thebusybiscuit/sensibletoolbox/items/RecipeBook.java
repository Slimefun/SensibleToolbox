package io.github.thebusybiscuit.sensibletoolbox.items;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import io.github.thebusybiscuit.sensibletoolbox.api.STBInventoryHolder;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.ButtonGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.CyclerGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.GUIUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.SlotType;
import io.github.thebusybiscuit.sensibletoolbox.api.items.AbstractProcessingMachine;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.items.ItemAction;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.CustomRecipe;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.CustomRecipeManager;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.RecipeUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.STBFurnaceRecipe;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.SimpleCustomRecipe;
import io.github.thebusybiscuit.sensibletoolbox.core.STBItemRegistry;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.util.VanillaInventoryUtils;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.cost.ItemCost;

/**
 * A recipe book allows browsing of all known recipes, and also fabrication of
 * items. Player permissions are taken into account; the player must have the
 * stb.craft.{item-name} permission node to see the item in the recipe book.
 */
public class RecipeBook extends BaseSTBItem {

    private static final int ITEMS_PER_PAGE = 45;

    private static final List<ItemStack> fullItemList = new ArrayList<>();
    private static final Map<ItemStack, Integer> itemListPos = new HashMap<>();

    private static final ItemStack SHAPED_ICON = STBUtil.makeStack(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Shaped Recipe");
    private static final ItemStack SHAPELESS_ICON = STBUtil.makeStack(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Shapeless Recipe");
    private static final ItemStack FURNACE_ICON = STBUtil.makeStack(Material.FURNACE, ChatColor.YELLOW + "Furnace Recipe");
    private static final ItemStack GO_BACK_TEXTURE = new ItemStack(Material.IRON_DOOR);
    private static final ItemStack GO_BACK_TEXTURE_2 = new ItemStack(Material.OAK_DOOR);
    private static final ItemStack WEB_TEXTURE = new ItemStack(Material.COBWEB);

    // slots for the item list page...
    public static final int PAGE_LABEL_SLOT = 45;
    public static final int FILTER_TYPE_BUTTON_SLOT = 46;
    public static final int FILTER_STRING_BUTTON_SLOT = 47;
    public static final int PREV_PAGE_SLOT = 52;
    public static final int NEXT_PAGE_SLOT = 53;

    // slots for the recipe display page
    private static final int[] RECIPE_SLOTS = { 10, 11, 12, 19, 20, 21, 28, 29, 30 };
    public static final int TYPE_SLOT = 23;
    public static final int RESULT_SLOT = 25;
    public static final int NEXT_RECIPE_SLOT = 18;
    public static final int PREV_RECIPE_SLOT = 26;
    public static final int TRAIL_BACK_SLOT = 52;
    public static final int ITEM_LIST_SLOT = 53;
    public static final String FREEFAB_PERMISSION = "stb.recipebook.freefab";
    public static final String FABRICATION_TITLE = ChatColor.BLUE + "Fabrication";

    private int page;
    private int viewingItem;
    private Recipe viewingRecipe;
    private int recipeNumber;
    private String recipeNameFilter;
    private RecipeType recipeTypeFilter;
    private List<ItemStack> filteredItems;
    private InventoryGUI gui;
    private boolean fabricationAvailable;
    private boolean fabricationFree;
    private final Deque<ItemAndRecipeNumber> trail = new ArrayDeque<>();
    private Player player;
    private int inventorySlot;
    private final List<InventoryHolder> resourceInventories = new ArrayList<>();
    private final Set<String> providerNames = new HashSet<>();
    private final List<ItemStack> currentIngredients = new ArrayList<>();

    /**
     * Constructs a new recipe book.
     */
    public RecipeBook() {
        super();
        fabricationAvailable = fabricationFree = false;
        page = 0;
        viewingItem = -1;
        recipeNumber = 0;
        recipeNameFilter = "";
        recipeTypeFilter = RecipeType.ALL;
        filteredItems = fullItemList;
    }

    /**
     * Constructs a recipe book from frozen data.
     *
     * @param conf
     *            frozen data; see {@link #freeze()}
     */
    public RecipeBook(ConfigurationSection conf) {
        super(conf);

        fabricationAvailable = fabricationFree = false;
        page = conf.getInt("page");
        viewingItem = conf.getInt("viewingItem");
        recipeNumber = conf.getInt("recipeNumber");
        recipeNameFilter = conf.getString("filter", "");
        recipeTypeFilter = RecipeType.valueOf(conf.getString("typeFilter", "ALL"));
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("page", page);
        conf.set("viewingItem", viewingItem);
        conf.set("recipeNumber", recipeNumber);
        conf.set("filter", recipeNameFilter);
        conf.set("typeFilter", recipeTypeFilter.toString());
        return conf;
    }

    /**
     * Builds the complete recipe list; all known craftable items in the game.
     * Don't call this directly - it's called once when Sensible Toolbox
     * initialises.
     */
    public static void buildRecipes() {
        Iterator<Recipe> iter = Bukkit.recipeIterator();
        Set<ItemStack> itemSet = new HashSet<>();

        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            ItemStack stack = recipe.getResult().clone();
            stack.setAmount(1);
            itemSet.add(stack);
        }

        CustomRecipeManager crm = CustomRecipeManager.getManager();
        itemSet.addAll(crm.getAllResults());

        fullItemList.addAll(itemSet);
        fullItemList.sort(new StackComparator());

        for (int i = 0; i < fullItemList.size(); i++) {
            itemListPos.put(fullItemList.get(i), i);
        }
    }

    private void buildFilteredList() {
        String filterString = recipeNameFilter.toLowerCase();

        if (filterString.isEmpty() && recipeTypeFilter == RecipeType.ALL) {
            filteredItems = fullItemList;
        }
        else {
            filteredItems = new ArrayList<>();

            for (ItemStack stack : fullItemList) {
                if (filterString.isEmpty() || ItemUtils.getItemName(stack).toLowerCase().contains(filterString)) {
                    BaseSTBItem stbItem = SensibleToolbox.getItemRegistry().fromItemStack(stack);

                    if (includeItem(stbItem)) {
                        filteredItems.add(stack);
                    }
                }
            }
        }
    }

    /**
     * Set a list of STB item provider plugin names to filter the list of
     * matched items by. Plugins can extend this class and override this
     * method if they want to create a custom recipe book which only includes
     * recipes provided by them.
     * <p/>
     * The plugin names should be strings, as returned by
     * {@link org.bukkit.plugin.Plugin#getName()}. Passing an empty array of
     * plugin names clears the filter.
     *
     * @param pluginNames
     *            zero or more provider plugin names
     */
    protected void setMatchProviders(String... pluginNames) {
        providerNames.clear();
        providerNames.addAll(Arrays.asList(pluginNames));
    }

    private boolean includeItem(@Nullable BaseSTBItem stbItem) {
        switch (recipeTypeFilter) {
        case ALL:
            return stbItem == null || (stbItem.checkPlayerPermission(player, ItemAction.CRAFT) && (providerNames.isEmpty() || providerNames.contains(stbItem.getProviderPlugin().getName())));
        case VANILLA:
            return stbItem == null;
        case STB:
            return stbItem != null && stbItem.checkPlayerPermission(player, ItemAction.CRAFT) && (providerNames.isEmpty() || providerNames.contains(stbItem.getProviderPlugin().getName()));
        default:
            return true;
        }
    }

    public boolean isFabricationAvailable() {
        return fabricationAvailable;
    }

    public void setFabricationAvailable(boolean fabricationAvailable) {
        this.fabricationAvailable = fabricationAvailable;
    }

    public String getRecipeNameFilter() {
        return recipeNameFilter;
    }

    public void setRecipeNameFilter(String recipeNameFilter) {
        this.recipeNameFilter = recipeNameFilter;
    }

    public RecipeType getRecipeTypeFilter() {
        return recipeTypeFilter;
    }

    public void setRecipeTypeFilter(RecipeType filter) {
        this.recipeTypeFilter = filter;
    }

    @Override
    public Material getMaterial() {
        return Material.BOOK;
    }

    @Override
    public String getItemName() {
        return "Recipe Book";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Allows browsing/fabrication", "of all known recipes" };
    }

    public void setInventorySlot(int inventorySlot) {
        this.inventorySlot = inventorySlot;
    }

    public int getInventorySlot() {
        return inventorySlot;
    }

    public boolean isAdvanced() {
        return false;
    }

    @Override
    public boolean hasGlow() {
        return isAdvanced();
    }

    @Override
    public Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(Material.BOOK);
        recipe.addIngredient(Material.CRAFTING_TABLE);
        return recipe;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clicked = event.getClickedBlock();
            boolean isWorkbench = STBUtil.canFabricateWith(clicked);

            if (clicked != null && clicked.getType().isInteractable() && !isWorkbench && !event.getPlayer().isSneaking()) {
                // allow opening doors, throwing levers etc. with a recipe book in hand
                return;
            }

            openBook(event.getPlayer(), isWorkbench ? clicked : null);
            setInventorySlot(event.getPlayer().getInventory().getHeldItemSlot());
            event.setCancelled(true);
        }
    }

    /**
     * Make the book go to the item list screen (leaving the recipe view
     * screen if the book was showing that).
     */
    public void goToItemList() {
        viewingItem = -1;
    }

    /**
     * Open this recipe book for the given player.
     *
     * @param player
     *            the player to show the book GUI to
     * @param fabricationBlock
     *            a block which can be used for fabricating items,
     *            most commonly a workbench block; may also be
     *            null
     */
    public void openBook(Player player, Block fabricationBlock) {
        this.player = player;
        fabricationFree = player.hasPermission(FREEFAB_PERMISSION);
        setFabricationAvailable(fabricationFree || fabricationBlock != null || hasFabricatorInInventory(player));
        findResourceInventories(fabricationBlock);
        gui = GUIUtil.createGUI(player, this, 54, "Recipe Book");
        buildFilteredList();

        if (viewingItem < 0) {
            drawItemsPage();
        }
        else {
            drawRecipePage();
        }

        gui.show(player);
    }

    private void findResourceInventories(Block fabricationBlock) {
        resourceInventories.clear();
        if (fabricationBlock == null || !isAdvanced()) {
            return;
        }
        for (BlockFace face : STBUtil.directFaces) {
            Block b = fabricationBlock.getRelative(face);
            if (VanillaInventoryUtils.isVanillaInventory(b) && SensibleToolbox.getProtectionManager().hasPermission(player, b, ProtectableAction.ACCESS_INVENTORIES)) {
                resourceInventories.add(VanillaInventoryUtils.getVanillaInventory(b).map(Inventory::getHolder).get());
            }
            else {
                BaseSTBBlock stb = SensibleToolbox.getBlockAt(b.getLocation());
                if (stb instanceof STBInventoryHolder && stb.hasAccessRights(player)) {
                    resourceInventories.add((STBInventoryHolder) stb);
                }
            }
        }
        Debugger.getInstance().debug("recipebook: found " + resourceInventories.size() + " resource inventories adjacent to " + fabricationBlock + " for " + player.getName());
    }

    private boolean hasFabricatorInInventory(Player player) {
        PlayerInventory inv = player.getInventory();

        for (int slot = 0; slot < 36; slot++) {
            if (STBUtil.canFabricateWith(inv.getItem(slot))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        if (viewingItem == -1) {
            // switch to viewing the item's recipe
            if (itemListPos.containsKey(inSlot)) {
                viewingItem = itemListPos.get(inSlot);
                recipeNumber = 0;
                drawRecipePage();
            }
            else {
                LogUtils.warning("could not find item " + inSlot + " in the recipe list!");
            }
        }
        else {
            // in the recipe view - clicking an ingredient?
            if (gui.getSlotType(slot) == SlotType.ITEM) {
                if (slot == RESULT_SLOT) {
                    // possibly fabricate the resulting item
                    if (fabricationFree || (fabricationAvailable && (viewingRecipe instanceof ShapedRecipe || viewingRecipe instanceof ShapelessRecipe))) {
                        tryFabrication(viewingRecipe);
                    }
                }
                else if (inSlot != null) {
                    // drill down into the description for an item in the recipe
                    if (inSlot.getDurability() == 32767 && !itemListPos.containsKey(inSlot)) {
                        inSlot.setDurability(inSlot.getType().getMaxDurability());
                    }
                    if (itemListPos.containsKey(inSlot)) {
                        trail.push(new ItemAndRecipeNumber(viewingItem, recipeNumber));
                        viewingItem = itemListPos.get(inSlot);
                        recipeNumber = 0;
                        drawRecipePage();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        return false;
    }

    private void showShapedRecipe(ShapedRecipe recipe) {
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(recipe.getResult());
        String[] shape = recipe.getShape();
        Map<Character, ItemStack> map = recipe.getIngredientMap();
        currentIngredients.clear();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length(); j++) {
                char c = shape[i].charAt(j);
                int slot = 10 + i * 9 + j;
                ItemStack ingredient = getIngredient(item, map.get(c));
                currentIngredients.add(ingredient);
                gui.getInventory().setItem(slot, makeGuiIngredient(ingredient));
            }
        }

        gui.getInventory().setItem(TYPE_SLOT, SHAPED_ICON);
    }

    private void showShapelessRecipe(ShapelessRecipe recipe) {
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(recipe.getResult());
        List<ItemStack> ingredients = recipe.getIngredientList();
        currentIngredients.clear();

        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack ingredient = getIngredient(item, ingredients.get(i));
            currentIngredients.add(ingredient);
            gui.getInventory().setItem(RECIPE_SLOTS[i], makeGuiIngredient(ingredient));
        }

        gui.getInventory().setItem(TYPE_SLOT, SHAPELESS_ICON);
    }

    private ItemStack makeGuiIngredient(ItemStack ingredient) {
        // work around MC 1.8 which doesn't render item stacks with wildcard data
        if (ingredient == null) {
            return null;
        }
        else if (ingredient.getDurability() == 32767) {
            ItemStack ingredient2 = ingredient.clone();
            ((Damageable) ingredient2.getItemMeta()).setDamage((short) 0);
            return ingredient2;
        }
        else {
            return ingredient;
        }
    }

    private void showFurnaceRecipe(STBFurnaceRecipe recipe) {
        // 4 is the middle of the 9 item slots
        gui.getInventory().setItem(RECIPE_SLOTS[4], recipe.getIngredient());
        gui.getInventory().setItem(TYPE_SLOT, FURNACE_ICON);
    }

    private void showCustomRecipe(SimpleCustomRecipe recipe) {
        gui.getInventory().setItem(RESULT_SLOT, recipe.getResult());
        BaseSTBItem item = SensibleToolbox.getItemRegistry().getItemById(recipe.getProcessorID());
        ItemStack processor = new ItemStack(getMaterial());
        ItemMeta meta = processor.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + item.getItemName());
        List<String> lore = new ArrayList<>();
        lore.add(STBItemRegistry.LORE_PREFIX + item.getProviderPlugin().getName() + " (STB) item");

        if (item instanceof AbstractProcessingMachine) {
            AbstractProcessingMachine machine = (AbstractProcessingMachine) item;
            lore.add(ChatColor.WHITE.toString() + machine.getScuPerTick() + " SCU/t over " + recipe.getProcessingTime() / 20.0 + "s");
            lore.add(ChatColor.WHITE.toString() + "Total SCU: " + machine.getScuPerTick() * recipe.getProcessingTime());
        }

        meta.setLore(lore);
        processor.setItemMeta(meta);
        gui.getInventory().setItem(TYPE_SLOT, processor);
        // 4 is the middle of the 9 item slots
        gui.getInventory().setItem(RECIPE_SLOTS[4], recipe.getIngredient());
    }

    public static ItemStack getIngredient(BaseSTBItem resultingItem, ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (resultingItem != null) {
            Class<? extends BaseSTBItem> c = resultingItem.getCraftingRestriction(stack.getType());
            if (c != null) {
                try {
                    BaseSTBItem item2 = c.getDeclaredConstructor().newInstance();
                    ItemStack stack2 = item2.toItemStack();
                    stack2.setDurability(stack.getDurability());
                    return stack2;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return stack;
    }

    @Override
    public void onGUIClosed(HumanEntity player) {
        int slot = getInventorySlot();
        PlayerInventory inventory = player.getInventory();

        if (SensibleToolbox.getItemRegistry().isSTBItem(inventory.getItem(slot), RecipeBook.class)) {
            // If the player moved his recipe book to a different slot, we don't want to
            // overwrite the old slot with the updated book
            inventory.setItem(slot, toItemStack(inventory.getItem(slot).getAmount()));
        }
    }

    private void tryFabrication(Recipe recipe) {
        Debugger.getInstance().debug("recipe book: attempt to fabricate " + recipe.getResult() + " for " + player.getName());

        fabricationFree = player.hasPermission(FREEFAB_PERMISSION);
        if (fabricationFree) {
            fabricateFree(recipe.getResult());
            return;
        }

        // we only fabricate things that can be made in a vanilla crafting table
        if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) {
            return;
        }

        List<Inventory> vanillaInventories = new ArrayList<>();

        for (InventoryHolder h : resourceInventories) {
            if (h instanceof STBInventoryHolder) {
                Inventory inv = ((STBInventoryHolder) h).showOutputItems(player.getUniqueId());

                if (inv != null) {
                    vanillaInventories.add(inv);
                }
            }
            else if (h instanceof BlockState && SensibleToolbox.getProtectionManager().hasPermission(player, ((BlockState) h).getBlock(), ProtectableAction.ACCESS_INVENTORIES)) {
                vanillaInventories.add(h.getInventory());
            }
        }

        Inventory[] inventories = vanillaInventories.toArray(new Inventory[0]);

        List<ItemStack> ingredients = mergeIngredients();
        List<ItemCost> costs = new ArrayList<>(ingredients.size());
        boolean ok = true;

        for (ItemStack ingredient : ingredients) {
            ItemCost cost = new ItemCost(ingredient);

            if (!cost.isAffordable(player, false, inventories)) {
                MiscUtil.errorMessage(player, "Missing: &f" + ItemUtils.getItemName(ingredient));
                ok = false;
            }

            costs.add(cost);
        }

        if (ok) {
            List<ItemStack> taken = new ArrayList<>();

            for (ItemCost cost : costs) {
                Debugger.getInstance().debug(2, this + ": apply cost " + cost.getDescription() + " to player");
                cost.apply(player, false, inventories);
                taken.addAll(cost.getActualItemsTaken());
            }

            fabricateNormal(taken, recipe.getResult());

            for (Inventory inv : vanillaInventories) {
                if (inv.getHolder() instanceof STBInventoryHolder) {
                    ((STBInventoryHolder) inv.getHolder()).updateOutputItems(player.getUniqueId(), inv);
                }
            }
        }
        else {
            STBUtil.complain(player);
        }
    }

    private void fabricateFree(ItemStack result) {
        BaseSTBItem stb = SensibleToolbox.getItemRegistry().fromItemStack(result);

        if (stb instanceof Chargeable) {
            Chargeable c = (Chargeable) stb;
            c.setCharge(c.getMaxCharge());
            result = stb.toItemStack();
        }

        player.getInventory().addItem(result);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
        MiscUtil.statusMessage(player, "Fabricated (free): &f" + ItemUtils.getItemName(result));
    }

    private void fabricateNormal(List<ItemStack> taken, ItemStack result) {
        double totalCharge = 0.0;

        for (ItemStack stack : taken) {
            // the SCU level of any chargeable ingredient will contribute
            // to the charge on the resulting item
            BaseSTBItem stb = SensibleToolbox.getItemRegistry().fromItemStack(stack);

            if (stb instanceof Chargeable) {
                totalCharge += ((Chargeable) stb).getCharge();
            }
        }

        BaseSTBItem stb = SensibleToolbox.getItemRegistry().fromItemStack(result);

        if (stb instanceof Chargeable) {
            Chargeable c = (Chargeable) stb;
            c.setCharge(Math.min(totalCharge, c.getMaxCharge()));
            result = stb.toItemStack();
        }

        player.getInventory().addItem(result);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
        MiscUtil.statusMessage(player, "Fabricated: &f" + ItemUtils.getItemName(result));
    }

    private List<ItemStack> mergeIngredients() {
        Map<ItemStack, Integer> amounts = new HashMap<>();

        for (ItemStack stack : currentIngredients) {
            // for (int slot : RECIPE_SLOTS) {
            // ItemStack stack = gui.getInventory().getItem(slot);
            if (stack != null) {
                Integer existing = amounts.get(stack);

                if (existing == null) {
                    amounts.put(stack, 1);
                }
                else {
                    amounts.put(stack, existing + 1);
                }
            }
        }

        List<ItemStack> res = new ArrayList<>();

        for (Map.Entry<ItemStack, Integer> e : amounts.entrySet()) {
            ItemStack stack = e.getKey().clone();
            stack.setAmount(e.getValue());
            res.add(stack);
        }

        return res;
    }

    private void drawRecipePage() {
        ItemStack result = fullItemList.get(viewingItem);
        List<Recipe> recipes = new ArrayList<>();

        // the isSimilar() checks are necessary to ensure we don't pick up recipes for items
        // of the same material but with different item meta information...
        for (Recipe recipe : Bukkit.getRecipesFor(result)) {
            if (recipe.getResult().isSimilar(result) && !(recipe instanceof FurnaceRecipe)) {
                recipes.add(recipe);
            }
        }

        // Furnace recipes need special treatment, since there could be multiple
        // recipes per material (STB item ingredients), but Bukkit FurnaceRecipe
        // doesn't support that concept.
        for (ItemStack stack : RecipeUtil.getSmeltingIngredientsFor(result)) {
            recipes.add(new STBFurnaceRecipe(result, stack));
        }

        // Custom STB recipes: items which are created in a machine added by STB
        for (CustomRecipe customRecipe : CustomRecipeManager.getManager().getRecipesFor(result)) {
            if (customRecipe.getResult().isSimilar(result)) {
                recipes.add(customRecipe);
            }
        }

        for (int slot = 0; slot < 54; slot++) {
            gui.setSlotType(slot, SlotType.BACKGROUND);
        }

        for (int slot : RECIPE_SLOTS) {
            gui.setSlotType(slot, SlotType.ITEM);
        }

        gui.setSlotType(RESULT_SLOT, SlotType.ITEM);

        gui.addGadget(new ButtonGadget(gui, ITEM_LIST_SLOT, "< Back to Item List", new String[0], GO_BACK_TEXTURE, () -> {
            trail.clear();
            viewingItem = -1;
            viewingRecipe = null;
            drawItemsPage();
        }));

        if (!trail.isEmpty()) {
            ItemStack prevStack = fullItemList.get(trail.peek().item);
            String label = "< Back to Last Recipe";
            gui.addGadget(new ButtonGadget(gui, TRAIL_BACK_SLOT, label, new String[] { ItemUtils.getItemName(prevStack) }, GO_BACK_TEXTURE_2, () -> {
                ItemAndRecipeNumber ir = trail.pop();
                viewingItem = ir.item;
                recipeNumber = ir.recipe;
                drawRecipePage();
            }));
        }

        String lore = "for " + ItemUtils.getItemName(result);
        int nRecipes = recipes.size();

        if (nRecipes > 1) {
            gui.addGadget(new ButtonGadget(gui, NEXT_RECIPE_SLOT, "< Prev Recipe", new String[] { lore }, null, () -> {
                recipeNumber--;
                if (recipeNumber < 0) recipeNumber = nRecipes - 1;
                drawRecipePage();
            }));

            gui.addGadget(new ButtonGadget(gui, PREV_RECIPE_SLOT, "Next Recipe >", new String[] { lore }, null, () -> {
                recipeNumber++;
                if (recipeNumber >= nRecipes) recipeNumber = 0;
                drawRecipePage();
            }));
        }

        if (nRecipes == 0) {
            return;
        }

        if (recipeNumber >= nRecipes) {
            recipeNumber = nRecipes - 1;
        }
        else if (recipeNumber < 0) {
            recipeNumber = 0;
        }

        viewingRecipe = recipes.get(recipeNumber);

        gui.getInventory().setItem(RESULT_SLOT, viewingRecipe.getResult());

        if (viewingRecipe instanceof STBFurnaceRecipe) {
            showFurnaceRecipe((STBFurnaceRecipe) viewingRecipe);
        }
        else if (viewingRecipe instanceof ShapedRecipe) {
            showShapedRecipe((ShapedRecipe) viewingRecipe);
        }
        else if (viewingRecipe instanceof ShapelessRecipe) {
            showShapelessRecipe((ShapelessRecipe) viewingRecipe);
        }
        else if (viewingRecipe instanceof SimpleCustomRecipe) {
            showCustomRecipe((SimpleCustomRecipe) viewingRecipe);
        }

        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(result);

        if (item != null && item.getCraftingNotes() != null) {
            gui.addLabel(item.getCraftingNotes(), 2, null);
        }

        if (fabricationFree || (fabricationAvailable && (viewingRecipe instanceof ShapedRecipe || viewingRecipe instanceof ShapelessRecipe))) {
            String fabLabel = fabricationFree ? "Fabricate (free)" : "Fabricate";
            gui.addGadget(new ButtonGadget(gui, 46, fabLabel, new String[0], SHAPED_ICON, () -> tryFabrication(viewingRecipe)));
        }

        ItemStack pageStack = new ItemStack(Material.PAPER, recipeNumber + 1);
        gui.addLabel("Recipe " + (recipeNumber + 1) + "/" + nRecipes, 45, pageStack);
    }

    private void drawItemsPage() {
        int totalPages = (filteredItems.size() / ITEMS_PER_PAGE) + 1;
        page = Math.min(page, totalPages - 1);
        int start = page * ITEMS_PER_PAGE;

        for (int i = start, slot = 0; i < start + ITEMS_PER_PAGE; i++, slot++) {
            if (i < filteredItems.size()) {
                gui.setSlotType(slot, SlotType.ITEM);
                gui.getInventory().setItem(slot, filteredItems.get(i));
            }
            else {
                gui.setSlotType(slot, SlotType.BACKGROUND);
            }
        }

        gui.addGadget(new ButtonGadget(gui, PREV_PAGE_SLOT, "< Prev Page", null, null, () -> {
            page--;
            if (page < 0) page = totalPages - 1;
            drawItemsPage();
        }));

        gui.addGadget(new ButtonGadget(gui, NEXT_PAGE_SLOT, "Next Page >", null, null, () -> {
            page++;
            if (page >= totalPages) page = 0;
            drawItemsPage();
        }));

        gui.addGadget(new RecipeTypeFilter(gui, FILTER_TYPE_BUTTON_SLOT, "Recipe Type"));
        if (recipeNameFilter != null && !recipeNameFilter.isEmpty()) {
            gui.addGadget(new ButtonGadget(gui, FILTER_STRING_BUTTON_SLOT, "Filter:" + ChatColor.YELLOW + " " + recipeNameFilter, new String[] { "Click to clear filter " }, WEB_TEXTURE, () -> {
                setRecipeNameFilter("");
                buildFilteredList();
                drawItemsPage();
            }));

        }
        else {
            gui.setSlotType(FILTER_STRING_BUTTON_SLOT, SlotType.BACKGROUND);
        }

        ItemStack pageStack = new ItemStack(Material.PAPER, page + 1);
        gui.addLabel("Page " + (page + 1) + "/" + totalPages, PAGE_LABEL_SLOT, pageStack);
    }

    private static class StackComparator implements Comparator<ItemStack> {

        @Override
        public int compare(ItemStack o1, ItemStack o2) {
            return ChatColor.stripColor(ItemUtils.getItemName(o1) == null ? "" : ItemUtils.getItemName(o1)).compareTo(ChatColor.stripColor(ItemUtils.getItemName(o2) == null ? "" : ItemUtils.getItemName(o2)));
        }

    }

    private static final class ItemAndRecipeNumber {

        private final int item;
        private final int recipe;

        private ItemAndRecipeNumber(int item, int recipe) {
            this.item = item;
            this.recipe = recipe;
        }

    }

    private enum RecipeType {
        ALL,
        VANILLA,
        STB
    }

    private class RecipeTypeFilter extends CyclerGadget<RecipeType> {

        protected RecipeTypeFilter(InventoryGUI gui, int slot, String label) {
            super(gui, slot, label);
            add(RecipeType.ALL, ChatColor.GRAY, Material.BLACK_STAINED_GLASS, "All Recipes");
            add(RecipeType.VANILLA, ChatColor.WHITE, Material.WHITE_STAINED_GLASS, "Vanilla Recipes");
            add(RecipeType.STB, ChatColor.YELLOW, Material.YELLOW_STAINED_GLASS, "STB Recipes");
            setInitialValue(((RecipeBook) getGUI().getOwningItem()).getRecipeTypeFilter());
        }

        @Override
        protected boolean ownerOnly() {
            return false;
        }

        @Override
        protected void apply(BaseSTBItem stbItem, RecipeType newValue) {
            RecipeBook book = (RecipeBook) stbItem;
            book.setRecipeTypeFilter(newValue);
            book.buildFilteredList();
            book.drawItemsPage();
        }
    }
}
