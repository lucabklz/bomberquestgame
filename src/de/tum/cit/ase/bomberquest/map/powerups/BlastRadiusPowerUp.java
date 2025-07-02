package de.tum.cit.ase.bomberquest.map.powerups;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.ase.bomberquest.audio.SoundEffect;
import de.tum.cit.ase.bomberquest.map.characters.Player;
import de.tum.cit.ase.bomberquest.texture.Textures;

/**
 * Represents a power-up that increases the blast radius of the player's bombs.
 * When collected, the player's bomb blast radius is incremented, to a maximum of 8.
 */
public class BlastRadiusPowerUp extends PowerUps {

    /**
     * Constructor: Creates a new BlastRadiusPowerUp at a specified location.
     *
     * @param x The x-coordinate of the power-up on the map.
     * @param y The y-coordinate of the power-up on the map.
     */
    public BlastRadiusPowerUp(float x, float y) {
        super(x, y);
    }

    /**
     * Applies the effect of the power-up to the given player.
     * Increases the player's bomb blast radius by 1, to a maximum value of 8.
     * Plays a sound effect when  the power-up is collected.
     *
     * @param player The player collecting the power-up.
     */
    @Override
    public void applyEffect(Player player) {
        if (player.getBlastRadius() < 8) { // Maximum blast radius: 8
            player.increaseBlastRadius();

            // Play the POWERUP sound effect
            SoundEffect.POWERUP.play();
        }
    }

    /**
     * Retrieves the current visual representation of this power-up.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.BLAST_RADIUS_POWER_UP;
    }

}
