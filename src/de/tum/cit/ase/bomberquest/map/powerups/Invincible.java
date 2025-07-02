package de.tum.cit.ase.bomberquest.map.powerups;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Timer;
import de.tum.cit.ase.bomberquest.BomberQuestGame;
import de.tum.cit.ase.bomberquest.audio.MusicTrack;
import de.tum.cit.ase.bomberquest.map.characters.Player;
import de.tum.cit.ase.bomberquest.texture.Textures;

/**
 * Represents the Invincibility power-up in the game.
 * When activated, it makes the player immune to damage for a limited duration.
 */
public class Invincible extends PowerUps {

    private boolean isActivated = false; // Tracks whether the power-up has been activated

    /**
     * Constructs an Invincible power-up at the specified position.
     *
     * @param x    The x-coordinate of the power-up on the map.
     * @param y    The y-coordinate of the power-up on the map.
     */
    public Invincible(float x, float y) {
        super(x, y);
    }

    /**
     * Applies the invincibility effect to the specified player.
     * Ensures the effect is activated only once.
     *
     * @param player The player who will gain invincibility.
     */
    @Override
    public void applyEffect(Player player) {
        if (!isActivated){
            isActivated = true;
        }
    }

    /**
     * Retrieves the current visual representation of the Invincible power-up.
     *
     * @return A TextureRegion representing the invincibility power-up texture.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.INVINCIBLE;
    }
}
