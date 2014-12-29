package me.mrCookieSlime.sensibletoolbox.api;

import me.mrCookieSlime.sensibletoolbox.api.util.Filter;

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
