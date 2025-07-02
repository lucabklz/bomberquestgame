package de.tum.cit.ase.bomberquest.map;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import de.tum.cit.ase.bomberquest.map.characters.Enemy;
import de.tum.cit.ase.bomberquest.map.characters.Player;
import de.tum.cit.ase.bomberquest.map.walls.DestructibleWall;

/**
 * Custom contact listener for handling collisions between objects in the game.
 * Processes collisions and implements specific logic for interactions such as player-enemy,
 * player-blast, enemy-blast, and destructible wall-blast interactions.
 *
 * Sources:
 * - Physics 2D Body & Hitbox: https://libgdx.com/wiki/extensions/physics/box2d
 * - Contact Listener: https://www.gamedevelopment.blog/full-libgdx-game-tutorial-box2d-contact-listener/
 */
public class ContactListenerGame implements ContactListener {

    private final GameMap map; //Attribute of the current game map

    /**
     * Constructs a contact listener for handling collisions in the game.
     *
     * @param map The game map where collisions will be processed.
     */
    public ContactListenerGame(GameMap map) {
        this.map = map; // Initializing the Map.
    }

    /**
     * Handles collision events when two objects begin contact.
     * Determines the types of objects involved and executes the corresponding logic.
     *
     * @param contact The contact information between two colliding objects.
     */
    @Override
    public void beginContact(Contact contact) {
        // Retrieve the userData for both colliding objects
        Object objectA = contact.getFixtureA().getBody().getUserData();
        Object objectB = contact.getFixtureB().getBody().getUserData();

        // Player-enemy collision: Trigger game over if the player is not invincible
        if (objectA instanceof Player && objectB instanceof Enemy){
            playerEnemyCollision((Player) objectA);
        } else if (objectA instanceof Enemy && objectB instanceof Player) {
            playerEnemyCollision((Player) objectB);
        }
        // Player-blast collision: Trigger game over if the player is not invincible
        else if (objectA instanceof Player && objectB instanceof Blast){
            playerBlastCollision((Player) objectA);
        } else if (objectA instanceof Blast && objectB instanceof Player){
            playerBlastCollision((Player) objectB);
        }
        // Enemy-blast collision: Trigger enemy death unless the blast is from the same enemy.
        else if (objectA instanceof Enemy && objectB instanceof Blast){
            enemyBlastCollision((Enemy) objectA, (Blast) objectB);
        } else if (objectA instanceof Blast && objectB instanceof Enemy) {
            enemyBlastCollision((Enemy) objectB, (Blast) objectA);
        }
        // Destructible wall-blast collision: Destroy the wall.
        else if (objectA instanceof DestructibleWall && objectB instanceof Blast){
            destructibleWallBlastCollision((DestructibleWall) objectA);
        } else if (objectA instanceof Blast && objectB instanceof DestructibleWall) {
            destructibleWallBlastCollision((DestructibleWall) objectB);
        }
        // Player-player collision in multiplayer mode: Disable collision so players can pass through each other.
        else if (objectA instanceof Player && objectB instanceof Player){
            contact.setEnabled(false);
        }
    }


    /**
     * Handles logic for when a player collides with an enemy.
     * Triggers game over if the player is not invincible and not already dead.
     *
     * @param player The player involved in the collision.
     */
    private void playerEnemyCollision(Player player){
        // If the player is not already dead and is not invincible, call the gameOver-method
        if (!player.isDead() && !player.isInvincible()){
            map.gameOver();
        }
    }

    /**
     * Handles logic for when a player is hit by a blast.
     * Triggers game over if the player is not invincible and not already dead.
     *
     * @param player The player hit by the blast.
     */    private void playerBlastCollision(Player player){
        // If the enemy is not already dead and is not invincible, call the gameOver-method
        if (!player.isDead() && !player.isInvincible()){
            map.gameOver();
        }
    }

    /** When an enemy is hit by a blast, the death logic of an enemy should be triggered.
     * The enemy should not die from the blast if it was the creator of the bomb (is needed for difficulty "HARD")
     *
     * @param enemy The enemy who is hit by a blast
     * @param blast The blast which hits the enemy
     * */
    private void enemyBlastCollision(Enemy enemy, Blast blast) {
        // Check that the blast originated from a bomb that was not created by an enemy
        if (blast.getBomb() != null && !(blast.getBomb().getBombCreator() instanceof Enemy))  {
            // Check if the enemy is already dead
            if (!enemy.isDead()) {
                // Trigger the death logic
                enemy.charactersDeath();
            }
        }
    }

    /**
     * Handles logic for when a destructible wall is hit by a blast.
     * Removes the wall from the game map.
     *
     * @param destructibleWall The destructible wall hit by the blast.
     */    private void destructibleWallBlastCollision(DestructibleWall destructibleWall){
        map.destroyDestructibleWall(Math.round(destructibleWall.getX()), Math.round(destructibleWall.getY()));
    }

    /**
     * Handles the end of a collision between two objects.
     * Currently, no specific logic is needed when collisions end.
     *
     * @param contact The contact information between two colliding objects.
     */
    @Override
    public void endContact(Contact contact) {

    }

    /**
     * Adjusts or disables physical collision responses before they are processed by the physics engine.
     *
     * @param contact The contact information between two colliding objects.
     * @param oldManifold  The information about the previous contact state.
     */
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Get the userData attached to the two objects that are part of the collision
        Object objectA = contact.getFixtureA().getBody().getUserData();
        Object objectB = contact.getFixtureB().getBody().getUserData();

        // Checks if a player collides with an enemy
        // Disable the physical response, e.g. to avoid a kickback
        if (objectA instanceof Player && objectB instanceof Enemy){
            contact.setEnabled(false);
        } else if (objectA instanceof Enemy && objectB instanceof Player) {
            contact.setEnabled(false);
        }
        // Checks if the player collides with a blast
        // Disable the physical response, e.g. to avoid a kickback
        else if (objectA instanceof Player && objectB instanceof Blast){
            contact.setEnabled(false);
        } else if (objectA instanceof Blast && objectB instanceof Player){
            contact.setEnabled(false);
        }
        // Checks if an enemy collides with a blast
        // Disable the physical response, e.g. to avoid a kickback
        else if (objectA instanceof Enemy && objectB instanceof Blast){
            contact.setEnabled(false);
        } else if (objectA instanceof Blast && objectB instanceof Enemy) {
            contact.setEnabled(false);
        }
    }

    /**
     * Implementing abstract method.
     *
     * @param contact
     * @param contactImpulse
     */
    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }


}
