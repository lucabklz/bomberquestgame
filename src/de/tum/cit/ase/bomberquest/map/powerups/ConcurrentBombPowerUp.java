package de.tum.cit.ase.bomberquest.map.powerups;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.ase.bomberquest.audio.SoundEffect;
import de.tum.cit.ase.bomberquest.map.characters.Player;
import de.tum.cit.ase.bomberquest.texture.Textures;

/**
 * Represents a power-up that increases the player's concurrent bomb placement limit.
 * When collected, the player can place an additional bomb at the same time, to a maximum of 8 bombs.
 */
public class ConcurrentBombPowerUp extends PowerUps {

    /**
     * Constructor: Creates a new ConcurrentBombPowerUp at the specified location.
     *
     * @param x The x-coordinate of the power-up on the map.
     * @param y The y-coordinate of the power-up on the map.
     */
    public ConcurrentBombPowerUp(float x, float y) {
        super(x, y);
    }

    /**
     * Applies the effect of this power-up to the  player.
     * Increases the player's concurrent bomb placement limit by 1, up to a maximum value of 8.
     * Plays a sound effect to indicate the power-up is collected.
     *
     * @param player The player collecting this power-up.
     */
    @Override
    public void applyEffect(Player player) {
        if (player.getConcurrentBombLimit() < 8) { //Maximum limit is 8
            player.increaseConcurrentBombLimit();

            // Play the POWERUP sound effect
            SoundEffect.POWERUP.play();
        }
    }

    /**
     * Retrieves and returns the current visual representation of this power-up.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.CONCURRENT_BOMB_POWER_UP;
    }
}
