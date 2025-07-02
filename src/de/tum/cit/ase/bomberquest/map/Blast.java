package de.tum.cit.ase.bomberquest.map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import de.tum.cit.ase.bomberquest.texture.Drawable;

/**
 * Represents a blast created by a bomb explosion in the game.
 * The blast is a temporary entity that interacts with the game map and entities within its radius.
 * It includes visual animations, physical interactions, and timed removal.
 */
public class Blast extends GameObject implements Drawable {
    private Animation<TextureRegion> animation; // The visual blast animation
    private float stateTime; // Time elapsed, used for managing animation states
    private Body body; // The physical body of the blast
    private World world; // The Box2D world
    private Bomb bomb; // The bomb that triggered the blast


    /**
     * Constructor for the blast class.
     * Initializes a blast with its position, animation and physical body in the game world.
     *
     * @param x         The x-coordinate of the blast's position.
     * @param y         The y-coordinate of the blast's position.
     * @param animation The animation of the blast.
     * @param world     The physics world where the blast exists.
     * @param bomb      The bomb
     * */
    public Blast(float x, float y, Animation<TextureRegion> animation, World world, Bomb bomb) {
        super(x, y);
        this.animation = animation;
        this.stateTime = 0; // Initialize the state time to 0 for animation purposes
        this.body = createBlastBody(world, x, y); // Create a new physical body for the blast
        this.world = world;
        this.bomb = bomb;
    }

    /**
     * Creates the physical body of the blast in the Box2D world.
     * This method sets up the shape, position, and collision properties of the blast.
     *
     * @param world The physics world where the blast exists.
     * @param x     The x-coordinate of the blast.
     * @param y     The y-coordinate of the blast.
     * @return The Box2D body representing the blast.
     */
    private Body createBlastBody(World world, float x, float y) {
        // Define the properties of the body (static, does not move) and set its initial position.
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        // Create the body in the Box2D world using the defined properties.
        Body body = world.createBody(bodyDef);

        // Define the shape of the blast body as a square (1x1 tile).
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);

        // Attach the shape to the body with a fixture.
        body.createFixture(shape, 0.0f);

        // Dispose of the shape to free memory after attaching it to the body.
        shape.dispose();

        // Associate this blast object with the body for reference during interactions.
        body.setUserData(this);

        return body;
    }

    /**
     * Adds the blast to the game map and schedules its removal after the animation completes.
     * The blast is rendered and interacts with the game world for a short duration.
     *
     * @param map The game map where the blast exists.
     */
    public void createBlast(GameMap map) {
        // Add the blast to the game for rendering and interaction
        map.addBlast(this);

        // Schedule the removal of the blast after 0.4 seconds (this is the time the animation takes)
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Checks if the body of the current blast is not null
                Body currentBody = getBody();
                if (currentBody != null) {
                    // Mark the current blast for its removal from the game map
                    map.markBlastForRemoval(Blast.this);
                }
            }
        }, 0.4f);
    }

    /**
     * Updates the state of the blast each frame.
     * Primarily used to update the animation's state time.
     *
     * @param frameTime The time elapsed since the last frame.
     */
    public void tick(float frameTime) {
        stateTime += frameTime;
    }

    /**
     * Retrieves the current appearance of the blast based on its animation state.
     *
     * @return A TextureRegion representing the current frame of the blast animation.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        return animation.getKeyFrame(stateTime, true);
    }

    /**
     * Disposes resources used by this object to free up memory and avoid memory leaks.
     * This method should be called when the object is no longer needed.
     */
    public void dispose() {
        // Checks if the body exists
        if (body != null) {
            // Removes the body from the Box2D world
            world.destroyBody(body);
            // Sets the body to null
            body = null;
        }
    }

    /**
     * Retrieves the x-coordinate of the blast's position.
     *
     * @return The x-coordinate of the blast.
     */
    @Override
    public float getX() {
        return x;
    }

    /**
     * Retrieves the y-coordinate of the blast's position.
     *
     * @return The y-coordinate of the blast.
     */
    @Override
    public float getY() {
        return y;
    }

    /**
     * Retrieves the Box2D body associated with the blast.
     *
     * @return The Body instance representing the blast.
     */
    public Body getBody() {
        return body;
    }

    /**
     * Retrieves the bomb that triggered this blast.
     *
     * @return The Bomb instance that caused the blast.
     */
    public Bomb getBomb() {
        return bomb;
    }
}
