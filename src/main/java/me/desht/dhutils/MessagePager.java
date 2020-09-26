package me.desht.dhutils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

public class MessagePager {

    public static final String BULLET = ChatColor.LIGHT_PURPLE + "\u2022 " + ChatColor.WHITE;

    private static final int DEF_PAGE_SIZE = 18; // 20 lines total, minus 2 for header and footer

    private static String pageCmd = "";
    private static int defaultPageSize = DEF_PAGE_SIZE;

    private static final Map<String, MessagePager> pagers = new HashMap<>();

    private final List<String> messages;
    private final WeakReference<CommandSender> senderRef;

    private int currentPage;
    private int pageSize;
    private boolean parseColours;

    public MessagePager(CommandSender sender) {
        this.senderRef = new WeakReference<>(sender);
        this.currentPage = 1;
        this.parseColours = false;
        this.pageSize = getDefaultPageSize();
        this.messages = new ArrayList<>();
    }

    public static int getDefaultPageSize() {
        return defaultPageSize;
    }

    public static void setDefaultPageSize(int pageSize) {
        defaultPageSize = pageSize <= 0 ? Integer.MAX_VALUE : pageSize;
    }

    public static void setDefaultPageSize() {
        defaultPageSize = DEF_PAGE_SIZE;
    }

    /**
     * Get the message pager for the given player.
     *
     * @param sender
     *            the command sender (a player or console)
     * @return the player's message pager
     */
    public static MessagePager getPager(@Nonnull CommandSender sender) {
        if (!pagers.containsKey(sender.getName())) {
            pagers.put(sender.getName(), new MessagePager(sender));
        }

        return pagers.get(sender.getName());
    }

    /**
     * Delete the message buffer for the player. Should be called when the
     * player logs out.
     *
     * @param sender
     *            the command sender (a player or console)
     */
    public static void deletePager(@Nonnull CommandSender sender) {
        deletePager(sender.getName());
    }

    /**
     * Delete the message buffer for the player. Should be called when the
     * player logs out.
     *
     * @param playerName
     *            The player name
     */
    public static void deletePager(@Nonnull String playerName) {
        pagers.remove(playerName);
    }

    /**
     * Get the page size (number of lines in one page)
     *
     * @return The page size
     */
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Clear this message buffer and switch off automatic colour code parsing.
     *
     * @return this pager object for method chaining
     */
    public MessagePager clear() {
        currentPage = 1;
        parseColours = false;
        messages.clear();
        return this;
    }

    /**
     * Enable or disable colour code parsing.
     *
     * @param parseColours
     *            true to enable parsing, false to disable
     * @return this pager object for method chaining
     */
    public MessagePager setParseColours(boolean parseColours) {
        this.parseColours = parseColours;
        return this;
    }

    /**
     * Get the max line length in characters for this pager.
     *
     * @return the maximum line length
     */
    public int getLineLength() {
        // players have a little extra graphics at the front
        return senderRef instanceof Player ? 56 : 60;
    }

    /**
     * Add a message to the buffer.
     *
     * @param line
     *            The message line to add
     */
    public void add(@Nonnull String line) {
        Collections.addAll(messages, wrap(line));
    }

    public void addListItem(@Nonnull String line) {
        add(BULLET + line);
    }

    /**
     * Add a block of messages. All message should stay on the same page if
     * possible - add padding to ensure this where necessary. If block is larger
     * than the page size, then just add it.
     *
     * @param lines
     *            List of message lines to add
     */
    public void add(@Nonnull String[] lines) {
        add(Arrays.asList(lines));
    }

    public void add(@Nonnull List<String> lines) {
        // TODO: apply MinecraftChatStr.alignTags(lines, true)
        // in pagesize segments before adding to buffer

        List<String> actual = new ArrayList<>();
        for (String l : lines) {
            Collections.addAll(actual, wrap(l));
        }

        // if block is bigger than a page, just add it
        if (actual.size() <= getPageSize() && (messages.size() % getPageSize()) + actual.size() > getPageSize() && senderRef instanceof Player) {
            // else, add padding above to keep the block on one page
            for (int i = messages.size() % getPageSize(); i < getPageSize(); ++i) {
                // System.out.println("pad " + i);
                messages.add("");
            }
        }
        for (String line : actual) {
            messages.add(line);
        }
    }

    /**
     * Get the number of lines in the message buffer.
     *
     * @return The number of lines in the buffer
     */
    public int getSize() {
        return messages.size();
    }

    /**
     * Get the number of pages in the buffer.
     *
     * @return number of pages in the buffer, including the partial page at the end
     */
    public int getPageCount() {
        return (getSize() - 1) / getPageSize() + 1;
    }

    /**
     * Get a line of text from the buffer
     *
     * @param i
     *            The line number
     * @return The line of text at that line
     */
    public String getLine(int i) {
        return messages.get(i);
    }

    public void setPage(int page) {
        setPage(page, false);
    }

    /**
     * Set the current page for this message buffer.
     *
     * @param page
     *            The page number.
     * @param wrap
     *            If true, automatically wrap to beginning or end if the page number is out of range.
     */
    public void setPage(int page, boolean wrap) {
        if ((page < 1 || page > getPageCount()) && !wrap) {
            return;
        }

        if (page < 1) {
            page = getPageCount();
        }
        else if (page > getPageCount()) {
            page = 1;
        }
        currentPage = page;
    }

    /**
     * Move to the next page of the player's buffer.
     */
    public void nextPage() {
        setPage(getPage() + 1, true);
    }

    /**
     * Move to the previous page of the player's buffer.
     */
    public void prevPage() {
        setPage(getPage() - 1, true);
    }

    /**
     * Get the current page for the message buffer
     *
     * @return The current page for the player
     */
    public int getPage() {
        return currentPage;
    }

    /**
     * Display the current page for the player.
     */
    public void showPage() {
        showPage(currentPage);
    }

    /**
     * Display the specified page for the player.
     * 
     * @param pageStr
     *            A string containing the page number to display
     */
    public void showPage(String pageStr) throws NumberFormatException {
        int pageNum = Integer.parseInt(pageStr);
        showPage(pageNum);
    }

    /**
     * Display the specified page for the player.
     *
     * @param pageNum
     *            The page number to display
     */
    public void showPage(int pageNum) {
        CommandSender sender = senderRef.get();
        if (sender == null) {
            return;
        }
        if (sender instanceof Player) {
            // pretty paged display
            if (pageNum < 1 || pageNum > getPageCount()) {
                throw new IllegalArgumentException("Page number " + pageNum + " is out of range.");
            }

            Player player = (Player) sender;

            int i = (pageNum - 1) * getPageSize();
            int nMessages = getSize();
            String header = String.format("\u2524 %d-%d of %d lines (page %d/%d) \u251c", i + 1, Math.min(getPageSize() * pageNum, nMessages), nMessages, pageNum, getPageCount());
            MiscUtil.rawMessage(player, ChatColor.GREEN + "\u250c" + MinecraftChatStr.strPadCenterChat(header, 325, '\u2504'));

            for (; i < nMessages && i < pageNum * getPageSize(); ++i) {
                if (parseColours) {
                    MiscUtil.generalMessage(player, ChatColor.GREEN + "\u250a " + ChatColor.WHITE + getLine(i));
                }
                else {
                    MiscUtil.rawMessage(player, ChatColor.GREEN + "\u250a " + ChatColor.WHITE + getLine(i));
                }
            }

            String footer = getPageCount() > 1 ? "\u2524 Use " + pageCmd + " to see other pages \u251c" : "";
            MiscUtil.rawMessage(player, ChatColor.GREEN + "\u2514" + MinecraftChatStr.strPadCenterChat(footer, 325, '\u2504'));

            setPage(pageNum);
        }
        else {
            // just dump the whole message buffer to the console
            for (String s : messages) {
                if (parseColours) {
                    MiscUtil.generalMessage(sender, s);
                }
                else {
                    MiscUtil.rawMessage(sender, s);
                }
            }
        }
    }

    private String[] wrap(String line) {
        CommandSender sender = senderRef.get();

        if (sender instanceof Player) {
            String s = parseColours ? MiscUtil.parseColourSpec(sender, line) : line;
            return ChatPaginator.wordWrap(s, getLineLength());
        }
        else {
            return new String[] { line };
        }
    }

    public static void setPageCmd(String string) {
        pageCmd = string;
    }
}
