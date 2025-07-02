package de.tum.cit.ase.bomberquest.map.walls;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.ase.bomberquest.map.GameObject;
import de.tum.cit.ase.bomberquest.texture.Drawable;

/**
 * Abstract superclass for all wall objects in the game.
 * Walls are immovable objects that serve as obstacles or boundaries in the map.
 * This class provides the structure for creating and managing wall objects..
 */
public abstract class Wall extends GameObject implements Drawable {

    private final Body body; //Box2D body representing the wall's physical presence in the game world

    /**
     * Constructor: Constructs a new wall at the specified position in the Box2D world.
     *
     * @param world The Box2D world in which the wall exists.
     * @param x     The x-coordinate of the wall's position.
     * @param y     The y-coordinate of the wall's position.
     */
    public Wall(World world, float x, float y) {
        super(x, y);
        this.body = createHitbox(world);
    }

    /**
     * Creates a hitbox for the wall in the Box2D world.
     * The hitbox defines the wall's collision properties and dimensions.
     *
     * @param world The Box2D world to which the hitbox will be added.
     * @return The created Box2D body representing the wall.
     */
    protected Body createHitbox(World world) {

        // Define the properties of the body (static, doesn't move) and set the initial position
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(this.x, this.y);

        // Create the body in the Box2D world
        Body body = world.createBody(bodyDef);

        // Define the shape of the body (square, 1x1 tile)
        PolygonShape box = new PolygonShape();
        box.setAsBox(0.5f, 0.5f); // Half-width and half-height

        // Attach the shape to the body as a fixture
        body.createFixture(box, 1.0f);

        // Dispose of the shape to free memory
        box.dispose();

        // Link the wall object to the body for reference
        body.setUserData(this);

        return body;
    }

    /**
     * Retrieves the current visual representation of the wall.
     *
     * @return The texture to display for the wall.
     */
    @Override
    public abstract TextureRegion getCurrentAppearance();

    /**
     * Retrieves the x-coordinate of the wall's position.
     *
     * @return The x-coordinate of the wall.
     */
    @Override
    public float getX() {
        return x;
    }

    /**
     * Retrieves the y-coordinate of the wall's position.
     *
     * @return The y-coordinate of the wall.
     */
    @Override
    public float getY() {
        return y;
    }

    /**
     * Retrieves the Box2D body representing the wall.
     *
     * @return The physical body of the wall.
     */
    public Body getBody() {
        return body;
    }
}
