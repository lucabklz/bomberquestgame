package de.tum.cit.ase.bomberquest.map.powerups;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.ase.bomberquest.map.characters.Player;
import de.tum.cit.ase.bomberquest.texture.Textures;


/**
 * Represents a power-up that applies a freeze effect to the enemies.
 * The freeze effect does stop the enemy movement and interactions for a defined duration.
 * The effect can only be activated once per instance of the power-up.
 */
public class Freeze extends PowerUps{

    private boolean isActivated = false; //Boolean Attribute to capture the activation state

    /**
     * Constructor: Creates a new Freeze power-up at the specified location.
     *
     * @param x The x-coordinate of the power-up on the map.
     * @param y The y-coordinate of the power-up on the map.
     */
    public Freeze(float x, float y) {
        super(x, y);
    }

    /**
     * Applies the freeze effect to the game.
     * When activated, the enemies are frozen or restricted in their actions.
     * The effect is only applied if it hasn't already been activated.
     *
     * @param player The player collecting this power-up. This parameter is not used in the method,
     *               but as we´re overriding the method of the superclass, the Player is a parameter.
     */
    @Override
    public void applyEffect(Player player) {
        if (!isActivated){
            isActivated = true;
        }
    }

    /**
     * Retrieves and returns the current visual representation of this power-up.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.FREEZE;
    }
}
