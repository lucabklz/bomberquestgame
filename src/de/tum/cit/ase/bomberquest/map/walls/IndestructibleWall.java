package de.tum.cit.ase.bomberquest.map.walls;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.ase.bomberquest.texture.Textures;

/**
 * Represents an indestructible wall in the game.
 * Indestructible walls cannot be destroyed and serve as permanent obstacles in the map..
 */
public class IndestructibleWall extends Wall {

    private World world; //Box2D physics world

    /**
     * Constructor: creates a new indestructible wall at the specified position in the Box2D world.
     * @param world The Box2D world the wall exists in.
     * @param x     The x-coordinate of the wall's position.
     * @param y     The y-coordinate of the wall's position.
     */
    public IndestructibleWall(World world, float x, float y) {
        super(world, x, y);
        this.world = world;
    }


    /**
     * Disposes resources used by this object to free up memory.
     */
    public void dispose() {
        // Checks if the body exists
        Body currentBody = getBody();
        if (currentBody != null) {
            // Removes the body from the Box2D world
            world.destroyBody(currentBody);
            // Sets the body to null
            currentBody = null;
        }
    }

    /**
     * Retrieves and returns the current visual representation of the indestructible wall.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.INDESTRUCTIBLE_WALL;
    }
}
