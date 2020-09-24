package me.desht.sensibletoolbox.api;

import me.desht.sensibletoolbox.api.util.Filter;

/**
 * Represents an object that can filter items.
 */
public interface Filtering {
    /**
     * Get the owning object's filter.
     *
     * @return a filter object
     */
    public Filter getFilter();
}
