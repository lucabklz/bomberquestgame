package de.tum.cit.ase.bomberquest.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.ase.bomberquest.texture.Drawable;
import de.tum.cit.ase.bomberquest.texture.Textures;

/**
 * Represents decorative flowers on the game map.
 * Flowers are static objects with no special properties or interactions.
 * They do not have a hitbox and are purely used for aesthetic purposes as floor decorations.
 */
public class Flowers extends GameObject implements Drawable {
    
    private final int x; // The x-coordinate of the flower's position on the map
    private final int y; // The y-coordinate of the flower's position on the map

    /**
     *
     *
     * @param x The x-coordinate of the flower's position on the map.
     * @param y The y-coordinate of the flower's position on the map.
     */
    public Flowers(int x, int y) {
        super(x, y);
        this.x = x;
        this.y = y;
    }

    /**
     * Retrieves the current appearance of the flowers.
     * This method specifies the texture to render for the flowers.
     *
     * @return A TextureRegion representing the appearance of the flowers.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.FLOWERS;
    }

    /**
     * Retrieves the x-coordinate of the flower's position.
     *
     * @return The x-coordinate of the flowers.
     */
    @Override
    public float getX() {
        return x;
    }

    /**
     * Retrieves the y-coordinate of the flower's position.
     *
     * @return The y-coordinate of the flowers.
     */
    @Override
    public float getY() {
        return y;
    }
}
