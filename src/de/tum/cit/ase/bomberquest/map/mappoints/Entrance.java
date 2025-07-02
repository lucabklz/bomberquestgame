package de.tum.cit.ase.bomberquest.map.mappoints;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.ase.bomberquest.texture.Drawable;
import de.tum.cit.ase.bomberquest.texture.Textures;

/**
 * Represents the entrance point on the game map.
 * The entrance is a static map object where the player typically starts or interacts.
 */
public class Entrance extends MapPoints implements Drawable {

    /**
     * Constructs an Entrance object at the specified position.
     *
     * @param x The x-coordinate of the entrance on the map.
     * @param y The y-coordinate of the entrance on the map.
     */
    public Entrance(float x, float y) {
        super(x, y);
    }

    /**
     * Retrieves the current appearance of the entrance.
     * This method specifies the texture to render for the entrance on the map.
     *
     * @return The TextureRegion representing the entrance's appearance.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.ENTRANCE;
    }

    /**
     * Defines the interaction logic for the entrance.
     * Currently, this method is empty, but it can be overridden or extended if interactions are needed.
     */
    @Override
    public void interact() {
    }
}
