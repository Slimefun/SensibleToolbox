package io.github.thebusybiscuit.sensibletoolbox.listeners;

import org.bukkit.event.Listener;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;

abstract class STBBaseListener implements Listener {

    protected final SensibleToolboxPlugin plugin;

    public STBBaseListener(SensibleToolboxPlugin plugin) {
        this.plugin = plugin;
    }

}
