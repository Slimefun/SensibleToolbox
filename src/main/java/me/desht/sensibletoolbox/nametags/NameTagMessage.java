package me.desht.sensibletoolbox.nametags;

import java.awt.image.BufferedImage;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.google.common.base.Preconditions;

public class NameTagMessage extends ImageMessage {
	private NameTagSpawner spawner;
	private Location location;

	private double lineSpacing = 0.25d;

	/**
	 * Construct the next frame of an animated message.
	 *
	 * @param previousFrame - container of the previous frame.
	 * @param nextFrame - the next frame.
	 * @param imgChar - the character to use in this frame.
	 */
	public NameTagMessage(NameTagMessage previousFrame, BufferedImage nextFrame, char imgChar) {
		super(nextFrame, previousFrame.lines.length, imgChar);
		this.spawner = previousFrame.spawner; // reuse spawner
	}

	public NameTagMessage(BufferedImage image, int height, char imgChar) {
		super(image, height, imgChar);
		initialize(height);
	}

	public NameTagMessage(ChatColor[][] chatColors, char imgChar) {
		super(chatColors, imgChar);
		this.location = Preconditions.checkNotNull(location, "location cannot be NULL");
		initialize(chatColors.length);
	}

	public NameTagMessage(String... imgLines) {
		super(imgLines);
		initialize(imgLines.length);
	}

	private void initialize(int height) {
		this.spawner = new NameTagSpawner(height);
	}

	@Override
	public NameTagMessage appendCenteredText(String... text) {
		super.appendCenteredText(text);
		return this;
	}

	@Override
	public NameTagMessage appendText(String... text) {
		super.appendText(text);
		return this;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	/**
	 * Retrieve the default amount of meters in the y-axis between each name
	 * tag.
	 *
	 * @return The line spacing.
	 */
	public double getLineSpacing() {
		return lineSpacing;
	}

	/**
	 * Set the default amount of meters in the y-axis between each name tag.
	 *
	 * @param lineSpacing - the name spacing.
	 */
	public void setLineSpacing(double lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	@Override
	public void sendToPlayer(Player player) {
		sendToPlayer(player, location != null ? location : player.getLocation());
	}

	/**
	 * Send a floating image message to the given player at the specified
	 * starting location.
	 *
	 * @param player - the player.
	 * @param location - the starting location.
	 */
	public void sendToPlayer(Player player, Location location) {
		for (int i = 0; i < lines.length; i++) {
			spawner.setNameTag(i, player, location, -i * lineSpacing, lines[i]);
		}
	}

	/**
	 * Nove the floating message to the new location.
	 *
	 * @param player - the player.
	 * @param location - the new location.
	 */
	public void move(Player player, Location location) {
		Location copy = location.clone();

		for (int i = 0; i < lines.length; i++) {
			spawner.moveNameTag(i, player, copy);
			copy.setY(copy.getY() - lineSpacing);
		}
	}

	/**
	 * Clear the floating image displayed for a given player.
	 *
	 * @param player - the player.
	 */
	public void clear(Player player) {
		spawner.clearNameTags(player);
	}
}
