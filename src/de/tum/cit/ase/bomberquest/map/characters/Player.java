package de.tum.cit.ase.bomberquest.map.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;
import de.tum.cit.ase.bomberquest.audio.MusicTrack;
import de.tum.cit.ase.bomberquest.audio.SoundEffect;
import de.tum.cit.ase.bomberquest.map.Bomb;
import de.tum.cit.ase.bomberquest.map.GameMap;
import de.tum.cit.ase.bomberquest.texture.Animations;

/**
 * Represents the player character in the game.
 * The player interacts with the game world through a physical hitbox
 * and can perform actions like moving, placing bombs, and activating power-ups.
 */
public class Player extends Character {

    private int concurrentBombLimit = 1; // Current number of bombs the player can place
    private int blastRadius = 1; // Current radius of the bomb's explosion
    private int currentAmountBombs = 0; // Current amount of bombs which are placed in the game
    private final GameMap map; // Reference to the game map for interactions
    private String direction = "down"; // Player's current facing direction (used for animations)
    private boolean isInvincible = false; // Indicates if the player is invincible (from power-up)
    private int collectedInvinciblePowerUp = 0; // Number of invincible power-ups collected
    private int collectedFreezePowerUp = 0; // Number of freeze power-ups collected
    private World world; // The Box2D world where the player's hitbox exists

    /**
     * Constructs a new Player instance at the specified position.
     * Initializes the player's hitbox and associates the player with the game world and map.
     *
     * @param world The Box2D world.
     * @param x     The initial x-coordinate of the player's position.
     * @param y     The initial y-coordinate of the player's position.
     * @param map   The game map the player interacts with.
     */
    public Player(World world, float x, float y, GameMap map) {
        super(world, x, y);
        this.map = map;
        this.world = world;
    }


    /**
     * Implements the abstract createHitbox method from the Character class.
     * This method creates a circular hitbox for the player.
     * The hitbox is used for collision detection and interactions with the environment and enemies, while ignoring collisions with other players
     * .
     * @param world  The Box2D world where the hitbox will be created.
     * @param startX The initial x-coordinate of the player.
     * @param startY The initial y-coordinate of the player.
     * @return The created Body instance representing the player's hitbox.
     */
    @Override
    protected Body createHitbox(World world, float startX, float startY) {
        // Define the properties of the body (dynamic, moves with physics forces) and set the initial position
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX, startY);

        // Create the body in the Box2D world
        Body body = world.createBody(bodyDef);

        // Define the shape of the body (circle with a radius of 0.3 units)
        CircleShape circle = new CircleShape();
        circle.setRadius(0.3f); // Define the radius of the circular hitbox

        // Attach the shape to the body with a fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1.0f;

        // Define collision filtering to prevent collisions with other players
        short PLAYER_CATEGORY = 0x0002; // Collision category for players, e.g. 0x0002
        short PLAYER_MASK = (short) ~PLAYER_CATEGORY; // Collide with everything except other players
        fixtureDef.filter.categoryBits = PLAYER_CATEGORY;
        fixtureDef.filter.maskBits = PLAYER_MASK;

        // Attach fixture to body
        body.createFixture(fixtureDef);

        // Dispose of the shape, not needed anymore
        circle.dispose();

        // Associate this player instance with the body
        body.setUserData(this);

        return body;
    }

    /**
     * Updates the player's state each frame, handling movement, bomb placement, and power-up activation.
     * The method processes user input to determine the player's actions.
     *
     * @param frameTime The time elapsed since the last frame.
     */
    public void tick(float frameTime) {
        this.elapsedTime += frameTime;
        float xDirection = 0;
        float yDirection = 0;

        // Handle player actions if they are not dead
        if (!isDead) {
            // Process directional movement based on input
            // Priority order: UP, DOWN, LEFT, RIGHT (to prevent simultaneous vertical and horizontal movement)
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                yDirection = 3.0f;
                xDirection = 0; // Cancel horizontal movement
                direction = "up";
            } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                yDirection = -3.0f;
                xDirection = 0; // Cancel horizontal movement
                direction = "down";
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                xDirection = -3.0f;
                yDirection = 0; // Cancel vertical movement
                direction = "left";
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                xDirection = 3.0f;
                yDirection = 0; // Cancel vertical movement
                direction = "right";
            }

            // Update the player's movement based on the determined direction
            this.hitbox.setLinearVelocity(xDirection, yDirection);

            // Place a bomb when the SPACE key is pressed
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
                placeBomb();
            }

            // Activate the invincible power-up when ENTER key is pressed
            if (collectedInvinciblePowerUp > 0) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    // Check if no other invincible or freeze power-up is already activated
                    if (!isInvincible() && !map.isFreezeActive()) {
                        // Call the activateInvincibility method
                        activateInvincibility(this, map.getPlayer2());
                    }
                }
            }

            // Activate the freeze power-up when either SHIFT key is pressed.
            if (collectedFreezePowerUp > 0) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) ||
                        Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {
                    activateFreezePowerUp();
                }
            }
        } else {
            this.hitbox.setLinearVelocity(0, 0);
        }

    }

    /**
     * Updates the second players state each frame, handling movement and bomb placement.
     * The method processes user input for Player 2's controls.
     *
     * @param frameTime The time elapsed since the last frame.
     */
    public void tickPlayer2(float frameTime) {
        this.elapsedTime += frameTime;
        float xDirection = 0;
        float yDirection = 0;

        // Handle Player 2's actions if they are not dead
        if (!isDead) {
            // Process directional movement based on input for Player 2
            // Priority order: UP (W), DOWN (S), LEFT (A), RIGHT (D)
            if (Gdx.input.isKeyPressed(Input.Keys.W)) { //up
                yDirection = 3.0f;
                xDirection = 0; // Cancel horizontal movement
                direction = "up";
            } else if (Gdx.input.isKeyPressed(Input.Keys.S)) { //down
                yDirection = -3.0f;
                xDirection = 0; // Cancel horizontal movement
                direction = "down";
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) { //left
                xDirection = -3.0f;
                yDirection = 0; // Cancel vertical movement
                direction = "left";
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) { //right
                xDirection = 3.0f;
                yDirection = 0; // Cancel vertical movement
                direction = "right";
            }
            // Update Player 2's movement based on the determined direction
            this.hitbox.setLinearVelocity(xDirection, yDirection);
        } else {
            // If Player 2 is dead, stop all movement
            this.hitbox.setLinearVelocity(0, 0);
        }

        // Place a bomb when the F key is pressed for Player 2
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)){
            placeBomb();
        }
    }

    /**
     * Activates invincibility for one or two players for a limited duration.
     *
     * @param player1 The first player (always present).
     * @param player2 The second player (might be null in single-player).
     */
    private void activateInvincibility(Player player1, Player player2) {
        // Decrement the amount of collected invincibility power-ups
        decreaseCollectedInvinciblePowerUps();

        // Make player1 invincible
        player1.setInvincible(true);
        // If player2 exists, make player2 invincible too
        if (player2 != null){
            player2.setInvincible(true);
        }

        // Indicate that the map is in an invincible state
        map.setInvincibleActive(true);

        // Play the invincibility sound effect
        SoundEffect.INVINCIBLE.play();

        // Play the invincibility music track
        map.getGame().playMusicTrack(MusicTrack.INVINCIBLE);

        // Start the HUD’s special power-up timer
        map.getGame().getHud().startSpecialPowerUpTimer();

        // Schedule a timer to revert the invincibility after 15 seconds
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Deactivate invincibility for player 1
                player1.setInvincible(false);
                // If player2 exists, deactivate invincibility for player 2 too
                if (player2 != null){
                    player2.setInvincible(false);
                }

                // Mark the invincible state as inactive on the map
                map.setInvincibleActive(false);

                // Switch back to the regular game music
                map.getGame().playMusicTrack(MusicTrack.GAME);
            }
        }, 15);
    }

    /**
     * Retrieves the player's current appearance based on their state.
     * Returns the appropriate animation frame or texture.
     *
     * @return A TextureRegion representing the player's current visual state.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        // Play the death-animation when the player dies
        if (isDead) {
            deathStateTime += Gdx.graphics.getDeltaTime();
            return Animations.CHARACTER_DEATH.getKeyFrame(deathStateTime, false);
        }

        // Freeze-frame when the player is not moving
        if (this.hitbox.getLinearVelocity().isZero()){
            return Animations.CHARACTER_WALK_DOWN.getKeyFrame(0);
        }

        // Return the animation corresponding to the player's current direction.
        switch (direction){
            case "up":
                return Animations.CHARACTER_WALK_UP.getKeyFrame(this.elapsedTime, true);
            case "down":
                return Animations.CHARACTER_WALK_DOWN.getKeyFrame(this.elapsedTime, true);
            case "left":
                return Animations.CHARACTER_WALK_LEFT.getKeyFrame(this.elapsedTime, true);
            case "right":
                return Animations.CHARACTER_WALK_RIGHT.getKeyFrame(this.elapsedTime, true);
            default:
                return Animations.CHARACTER_WALK_DOWN.getKeyFrame(this.elapsedTime, true);
        }
    }

    /**
     * Increases the maximum number of bombs the player can place at once, up to a limit of 8.
     */
    public void increaseConcurrentBombLimit() {
        if (concurrentBombLimit < 8) {  // Maximum limit is 8
            concurrentBombLimit++;
        }
    }

    /**
     * Increases the blast radius of the player's bombs, up to a limit of 8.
     */
    public void increaseBlastRadius() {
        if (blastRadius < 8) {  // Maximum radius is 8
            blastRadius++;
        }
    }

    /**
     * Increase the number of collected invincible power-ups by one
     **/
    public void increaseCollectedInvinciblePowerUps() {
        collectedInvinciblePowerUp++;
    }

    /**
     * Decrease the number of collected invincible power-ups by one
     **/
    public void decreaseCollectedInvinciblePowerUps() {
        collectedInvinciblePowerUp--;
    }

    /**
     * Increase the number of collected freeze power-ups by one
     **/
    public void increaseCollectedFreezePowerUps() {
        collectedFreezePowerUp++;
    }

    /**
     * Decrease the number of collected freeze power-ups by one
     * */
    public void decreaseCollectedFreezePowerUps() {
        collectedFreezePowerUp--;
    }

    /**
     * Activates the freeze power-up if no other freeze or invincible power-up is currently active.
     * This method ensures that the player can only use a freeze power-up they have collected.
     */
    public void activateFreezePowerUp() {
        // Check if no other invincible or freeze power-up is already activated and if the player already collected a freeze power-up
        if (!isInvincible && !map.isFreezeActive() && collectedFreezePowerUp > 0) {
            // Freeze all enemies
            map.freezeEnemies(map.getEnemies());
            // Decrease the amount of collected freeze power-ups
            decreaseCollectedFreezePowerUps();
            // Play the sound effect of activating a freeze power-up
            SoundEffect.FREEZE.play();
        }
    }

    /**
     * Places a bomb at the player's current position.
     * Ensures the bomb limit is not exceeded and prevents duplicate bombs at the same position.
     */
    public void placeBomb(){
        if (currentAmountBombs >= concurrentBombLimit){
            return; //It is not allowed to place another bomb
        }

        int positionX = Math.round(this.getX());
        int positionY = Math.round(this.getY());

        // Prevent placing a bomb at a position already occupied by another bomb
        for (Bomb bomb : map.getBombs()) {
            if (Math.round(bomb.getX()) == positionX && Math.round(bomb.getY()) == positionY){
                return;
            }
        }

        // Create and place the bomb on the map
        Bomb bomb = new Bomb(positionX, positionY, blastRadius, () -> {
            currentAmountBombs--; // Decrease the current amount of bombs when the bomb explodes
        }, map, this);

        currentAmountBombs ++; // Increase the current amount of bombs when a bomb is placed
        map.addBomb(bomb);

        // Play the sound effect for placing a bomb
        SoundEffect.PLACEBOMB.play();
    }

    /** Sets the isDead-attribute to true when the player dies.
     * This is only possible when he is not invincible
     * */
    @Override
    public void charactersDeath() {
        if(!isInvincible) {
            super.charactersDeath();
        }
    }

    /**
     * Disposes resources used by this object to free up memory and avoid memory leaks.
     */
    public void dispose() {
        // Check if the hitbox exists
        if (hitbox != null) {
            // Remove the hitbox from the Box2D world
            world.destroyBody(hitbox);
            // Set the hitbox to null
            hitbox = null;
        }
    }

    /**
     * Retrieves the maximum number of bombs the player can place at one time.
     *
     * @return The current bomb placement limit.
     */
    public int getConcurrentBombLimit() {
        return concurrentBombLimit;
    }


    /**
     * Retrieves the current blast radius of the player's bombs.
     *
     * @return The bomb's blast radius.
     */
    public int getBlastRadius() {
        return blastRadius;
    }

    /**
     * Checks if the player is currently in an invincible state.
     *
     * @return True if the player is invincible, false otherwise.
     */
    public boolean isInvincible() {
        return isInvincible;
    }

    /**
     * Sets the invincibility state of the player.
     *
     * @param invincible True to make the player invincible, false to disable invincibility.
     */
    public void setInvincible(boolean invincible) {
        isInvincible = invincible;
    }

    /**
     * Retrieves the number of invincible power-ups collected by the player.
     *
     * @return The count of collected invincible power-ups.
     */
    public int getCollectedInvinciblePowerUp() {
        return collectedInvinciblePowerUp;
    }

    /**
     * Retrieves the number of freeze power-ups collected by the player.
     *
     * @return The count of collected freeze power-ups.
     */
    public int getCollectedFreezePowerUp() {
        return collectedFreezePowerUp;
    }
}
