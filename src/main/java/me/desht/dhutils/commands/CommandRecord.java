package me.desht.dhutils.commands;

import javax.annotation.Nonnull;

import com.google.common.base.Joiner;

/**
 * Represents a single command record: command plus subcommands.
 * A command object contains one or more of these records.
 * 
 * @author desht
 */
class CommandRecord {

    private final String command;
    private final String[] subCommands;

    public CommandRecord(@Nonnull String[] fields) {
        this.command = fields[0];
        this.subCommands = new String[fields.length - 1];

        for (int i = 1; i < fields.length; i++) {
            subCommands[i - 1] = fields[i];
        }
    }

    @Override
    public String toString() {
        return command + " " + Joiner.on(" ").join(subCommands);
    }

    public int size() {
        return subCommands.length;
    }

    @Nonnull
    public String getCommand() {
        return command;
    }

    @Nonnull
    public String getSubCommand(int idx) {
        return subCommands[idx];
    }

    @Nonnull
    public String getLastSubCommand() {
        return subCommands[subCommands.length - 1];
    }
}