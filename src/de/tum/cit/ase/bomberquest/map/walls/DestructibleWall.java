package de.tum.cit.ase.bomberquest.map.walls;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.ase.bomberquest.texture.Textures;

/**
 * Represents a destructible wall in the game.
 * Destructible walls can be destroyed by bomb explosions and possible have power-ups or the exit below them.
 */
public class DestructibleWall extends Wall {

    private World world; //Box2D world

    /**
     * Constructor: Creates a new destructible wall at the specified position in the Box2D world.
     * @param world The Box2D world the wall exists in.
     * @param x     The x-coordinate of the wall's position.
     * @param y     The y-coordinate of the wall's position.
     */
    public DestructibleWall(World world, float x, float y) {
        super(world, x, y);
        this.world = world;
    }

    /**
     * Disposes of the resources associated with this wall.
     * Removes the wall's body from the Box2D world and sets it to null.
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
     * Retrieves and returns the current visual representation of the destructible wall.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.DESTRUCTIBLE_WALL;
    }

}
