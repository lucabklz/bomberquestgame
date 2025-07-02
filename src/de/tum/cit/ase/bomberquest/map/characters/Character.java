package de.tum.cit.ase.bomberquest.map.characters;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.ase.bomberquest.map.GameObject;
import de.tum.cit.ase.bomberquest.texture.Drawable;

/**
 * Abstract base class representing a character in the game (player or enemy).
 * This class defines shared behavior regarding death state and animation timing.
 * The hitbox logic is delegated to subclasses.
 */
public abstract class Character extends GameObject implements Drawable {

    protected float elapsedTime;  // Tracks the total time elapsed since the character was initialized, used for animations and movement logic.
    protected Body hitbox; // Represents the character's physical hitbox in the Box2D world, used for collision detection and positional tracking.
    protected boolean isDead = false; // Attribute that captures whether the character is currently dead.
    protected float deathStateTime = 0; // Tracks the duration of the character's death state.

    /**
     * Constructor for a character at a specified position within the given Box2D world.
     * Subclasses are responsible for defining their specific hitbox behavior by implementing
     * the abstract createHitbox method.
     *
     * @param world The Box2D world.
     * @param x     The initial x-coordinate of the character.
     * @param y     The initial y-coordinate of the character.
     */
    public Character(World world, float x, float y) {
        super(x ,y);
        this.hitbox = createHitbox(world, x, y);
    }

    /**
     * Abstract method for creating the character's hitbox.
     *
     * @param world  The Box2D world.
     * @param startX The x-coordinate for the hitbox's initial position.
     * @param startY The y-coordinate for the hitbox's initial position.
     * @return The Box2D body representing the character's hitbox.
     */
    protected abstract Body createHitbox(World world, float startX, float startY);

    /**
     * Marks the character as dead and resets the timer used for tracking the death state.
     * Subclasses can use this method to trigger death-related behaviors such as animations or logic changes.
     */
    public void charactersDeath(){
        isDead = true;
        deathStateTime = 0;
    }

    /**
     * Abstract method for retrieving the character's current visual appearance.
     *
     * @return The TextureRegion representing the character's current appearance.
     */
    @Override
    public abstract TextureRegion getCurrentAppearance();

    /**
     * Retrieves the character's current x-coordinate based on its hitbox position.
     *
     * @return The x-coordinate of the character.
     */
    @Override
    public float getX() {
        // The x-coordinate of the player is the x-coordinate of the hitbox (this can change every frame).
        return hitbox.getPosition().x;
    }

    /**
     * Retrieves the character's current y-coordinate based on its hitbox position.
     *
     * @return The y-coordinate of the character.
     */
    @Override
    public float getY() {
        return hitbox.getPosition().y; // The y-coordinate of the player is the y-coordinate of the hitbox (this can change every frame).
    }

    /**
     * Returns the character's hitbox.
     *
     * @return The Box2D body representing the character's hitbox.
     */
    public Body getHitbox() {
        return hitbox;
    }

    /**
     * Returns the time elapsed since the character entered the death state.
     *
     * @return The elapsed time in the death state.
     */
    public float getDeathStateTime() {
        return deathStateTime;
    }

    /**
     * Checks whether the character is currently dead.
     *
     * @return True if the character is dead, false otherwise.
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Sets a new hitbox for the character.
     *
     * @param hitbox The Box2D body to set as the character's hitbox.
     */
    public void setHitbox(Body hitbox) {
        this.hitbox = hitbox;
    }

}
