package de.tum.cit.ase.bomberquest.map.powerups;

import de.tum.cit.ase.bomberquest.map.GameObject;
import de.tum.cit.ase.bomberquest.map.characters.Player;
import de.tum.cit.ase.bomberquest.texture.Drawable;

/**
 * Abstract base class for all power-ups in the game.
 * Power-ups are collectible items that apply a specific effect to the player when collected (and partly only when activated).
 * This class provides the basic structure and behavior common to all power-ups.
 */
public abstract class PowerUps extends GameObject implements Drawable {

    /**
     * Constructor: Creates a new power-up at the specified coordinates.
     *
     * @param x The x-coordinate of the power-up's position.
     * @param y The y-coordinate of the power-up's position.
     */
    public PowerUps(float x, float y) {
        super(x, y);
    }

    /**
     * Abstract method to apply the effect of the power-up to the player.
     * Each subclass must implement the specific effect logic for the power-up.
     *
     * @param player The player collecting the power-up. This parameter is needed for multiplayer mode.
     */
    public abstract void applyEffect(Player player);

    /**
     * Retrieves the x-coordinate of this power-up.
     *
     * @return The x-coordinate of the power-up's position.
     */
    @Override
    public float getX() {
        return x;
    }

    /**
     * Retrieves the y-coordinate of this power-up.
     *
     * @return The y-coordinate of the power-up's position.
     */
    @Override
    public float getY() {
        return y;
    }
}
