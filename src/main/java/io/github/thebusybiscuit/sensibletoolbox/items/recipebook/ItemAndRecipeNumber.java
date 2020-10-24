package io.github.thebusybiscuit.sensibletoolbox.items.recipebook;

final class ItemAndRecipeNumber {

    private final int item;
    private final int recipe;

    ItemAndRecipeNumber(int item, int recipe) {
        this.item = item;
        this.recipe = recipe;
    }

    public int getItem() {
        return item;
    }

    public int getRecipe() {
        return recipe;
    }

}