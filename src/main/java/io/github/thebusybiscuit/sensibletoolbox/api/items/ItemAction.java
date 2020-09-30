package io.github.thebusybiscuit.sensibletoolbox.api.items;

import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * An action that can be taken on or with an STB item.
 * 
 * @author desht
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

    ItemAction() {
        node = toString().toLowerCase(Locale.ROOT);
    }

    @Nonnull
    public String getNode() {
        return node;
    }
}
