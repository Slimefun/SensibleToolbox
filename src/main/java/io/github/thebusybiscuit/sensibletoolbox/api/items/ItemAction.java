package io.github.thebusybiscuit.sensibletoolbox.api.items;

/**
 * An action that can be taken on or with an STB item.
 */
public enum ItemAction {

    /**
     * Craft the item.
     */
    CRAFT,
    /**
     * Place the item down as a block.
     */
    PLACE,
    /**
     * Break a block item.
     */
    BREAK,
    /**
     * Interact with the item in hand.
     */
    INTERACT,
    /**
     * Interact with the item as a block.
     */
    INTERACT_BLOCK;

    private final String node;

    private ItemAction() {
        node = toString().toLowerCase();
    }

    public String getNode() {
        return node;
    }
}
