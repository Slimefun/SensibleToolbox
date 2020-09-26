package io.github.thebusybiscuit.sensibletoolbox.api.filters;

import javax.annotation.Nonnull;

/**
 * Represents an object that can filter items.
 * 
 * @author desht
 */
@FunctionalInterface
public interface Filtering {

    /**
     * Get the owning object's filter.
     *
     * @return a filter object
     */
    @Nonnull
    Filter getFilter();
}
