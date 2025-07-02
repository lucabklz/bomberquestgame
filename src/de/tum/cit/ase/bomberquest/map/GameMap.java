package de.tum.cit.ase.bomberquest.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import de.tum.cit.ase.bomberquest.BomberQuestGame;
import de.tum.cit.ase.bomberquest.audio.MusicTrack;
import de.tum.cit.ase.bomberquest.audio.SoundEffect;
import de.tum.cit.ase.bomberquest.map.characters.Enemy;
import de.tum.cit.ase.bomberquest.map.characters.Player;
import de.tum.cit.ase.bomberquest.map.mappoints.Entrance;
import de.tum.cit.ase.bomberquest.map.mappoints.Exit;
import de.tum.cit.ase.bomberquest.map.powerups.BlastRadiusPowerUp;
import de.tum.cit.ase.bomberquest.map.powerups.ConcurrentBombPowerUp;
import de.tum.cit.ase.bomberquest.map.powerups.Freeze;
import de.tum.cit.ase.bomberquest.map.powerups.Invincible;
import de.tum.cit.ase.bomberquest.map.walls.DestructibleWall;
import de.tum.cit.ase.bomberquest.map.walls.IndestructibleWall;
import de.tum.cit.ase.bomberquest.texture.Animations;
import de.tum.cit.ase.bomberquest.texture.Drawable;
import java.util.*;

/**
 * Represents the game map.
 * The GameMap class holds all objects, entities, and game logic, including players, enemies, power-ups, bombs, walls, and blasts.
 * It also manages physics simulation and rendering of game elements.
 */
public class GameMap {

    /** A static block is executed once when the class is referenced for the first time. Initialize the Box2D physics engine */
    static {
        com.badlogic.gdx.physics.box2d.Box2D.init();
    }

    /** Box2D physics simulation parameters */
    private static final float TIME_STEP = 1f / Gdx.graphics.getDisplayMode().refreshRate; // Time step for physics simulation (1/refreshRate)
    private static final int VELOCITY_ITERATIONS = 6; // Number of velocity iterations in physics simulation
    private static final int POSITION_ITERATIONS = 2; // Number of position iterations in physics simulation
    private float physicsTime = 0; // Tracks accumulated time for maintaining a constant physics update rate

    /** Game and physics world references */
    private final BomberQuestGame game; // Reference to the main game instance
    private final World world; // Box2D world for physics simulation

    /** Player-related attributes */
    private Player player;
    private Player player2;
    private final List<Player> players = new ArrayList<>();  //Keeping track of player(s) that are active in game, in order to incorporate them in pathfinding algorithm of the enemies

    /** Map objects */
    private Exit exit; // The exit point of the map
    List<Entrance> entrances = new ArrayList<>(); // List of entrances for multiplayer or large maps

    /** Walls */
    private final List<IndestructibleWall> indestructibleWalls = new ArrayList<>();
    private final List<DestructibleWall> destructibleWalls = new ArrayList<>();

    /** Enemies */
    private final List<Enemy> enemies = new ArrayList<>();

    /** Power-ups */
    private final List<ConcurrentBombPowerUp> concurrentPowerUps = new ArrayList<>();
    private final List<BlastRadiusPowerUp> radiusPowerUps = new ArrayList<>();
    private final List<Invincible> invinciblePowerUps = new ArrayList<>();
    private final List<Freeze> freezePowerUps = new ArrayList<>();

    /** Bombs and blasts */
    private final List<Bomb> bombs = new ArrayList<>();
    private final List<Blast> blasts = new ArrayList<>();
    private final List<Bomb> bombsToRemove = new ArrayList<>();
    private final List<Blast> blastsToRemove = new ArrayList<>();

    /** Decorative elements */
    private final Flowers[][] flowers; // Flowers attribute for the background

    /** Power-up states */
    private boolean isFreezeActive = false; // Boolean attribute to track whether the freeze power-up is active
    private boolean isInvincibleActive = false; // Boolean attribute to track whether the invincible power-up is active

    /** Game state */
    private boolean isGameOver = false; // Boolean attribute to track whether the game is over

    /** Map dimensions */
    private float mapWidth = 0;
    private float mapHeight = 0;

    /** Enemy-specific logic */
    private Enemy enemyBombPlacer = null; // The enemy that places a bomb on difficulty "Hard"
    private float globalBombCooldown = 2.0f; // The global cooldown time (in seconds) for bombs placed by enemies

    /**
     * Constructs a new GameMap instance.
     * Initializes the game world, loads the map data, sets up collision handling, and generates decorative elements.
     *
     * @param game       The main game instance used to manage the game state and logic.
     * @param properties The properties file containing map data (e.g., layout, objects, and settings).
     */
    public GameMap(BomberQuestGame game, Properties properties) {
        this.game = game;
        this.world = new World(Vector2.Zero, true); // Initialize the Box2D world with no gravity and enable sleeping
        loadMap(properties); // Load the map layout and objects from the provided properties file
        world.setContactListener(new ContactListenerGame(this)); //Initialize the ContactListener to handle collision events in the game world
        this.flowers = new Flowers[(int) mapWidth][(int) mapHeight]; // Create flowers in a grid which corresponds to the mapWidth and mapHeight
        for (int i = 0; i < flowers.length; i++) {
            for (int j = 0; j < flowers[i].length; j++) {
                this.flowers[i][j] = new Flowers(i, j);
            }
        }
    }

    /**
     * Loads the game map from a .properties file.
     * Parses the map data, dynamically creates objects, and sets up initial game states such as entrances and power-ups.
     *
     * @param properties The .properties file containing the map data, where keys are coordinates and values are object types.
     */
    private void loadMap(Properties properties) {

        properties.forEach((key, value) -> { // Iterate through each entry in the properties file
            // Split the coordinates and parse them into floats
            String[] coordinates = key.toString().split(",");
            float x = Float.parseFloat(coordinates[0]);
            float y = Float.parseFloat(coordinates[1]);

            // Update map dimensions (using max for dynamic size adjustment)
            mapWidth = Math.max(mapWidth, x + 1);
            mapHeight = Math.max(mapHeight, y + 1);

            // Parse the object type (value) into an integer for object creation
            int objectType = Integer.parseInt(value.toString());

            // Create the corresponding object on the map based on its type
            createObjectFromType(x, y, objectType);
        });

        // Dynamically create a second entrance for multiplayer mode if only one entrance exists
        if (entrances.size() == 1 && game.isMultiplayer()) {
            createDynamicSecondEntrance();
        }

        // Assign Player 2 to the second entrance (if multiplayer mode is active)
        if (game.isMultiplayer() && player2 == null) {
            createPlayer2();
        }

        ensureExitExists();

        // Invincible & Freeze power-ups will spawn randomly
        randomSpawnInvinciblePowerUp();
        randomSpawnFreezePowerUp();

        // Reset the timer to start at the correct time when the map loads
        game.getGameTimer().reset(240); // Assuming 240 seconds as the default countdown
    }

    /**
     * Creates a game object based on its type using a switch statement.
     * The type is determined by the objectType parameter, which corresponds to a specific map entity.
     *
     * @param x          The x-coordinate of the object.
     * @param y          The y-coordinate of the object.
     * @param objectType The integer representing the type of object to create.
     */
    private void createObjectFromType(float x, float y, int objectType) {
        switch (objectType) {
            case 0 -> indestructibleWalls.add(new IndestructibleWall(world, x, y));
            case 1 -> destructibleWalls.add(new DestructibleWall(world, x, y));
            case 2 -> { // Create an entrance and spawn the first player at this location.
                Entrance entrance = new Entrance(x, y);
                entrances.add(entrance);
                player = new Player(world, x, y, this);
                players.add(player); // Add the player to the overall player list
            }
            case 3 -> enemies.add(new Enemy(world, x, y, this));
            case 4 -> {
                exit = new Exit(x, y, game);
            }
            case 5 -> {  // Create a destructible wall with a concurrent bomb power-up behind it
                destructibleWalls.add(new DestructibleWall(world, x, y));
                concurrentPowerUps.add(new ConcurrentBombPowerUp(x, y));
            }
            case 6 -> {  // Create a destructible wall with a blast radius power-up behind it
                destructibleWalls.add(new DestructibleWall(world, x, y));
                radiusPowerUps.add(new BlastRadiusPowerUp(x, y));
            }
            default -> System.err.println("Unknown object type: " + objectType);
        }
    }

    /**
     * Ensures that the map contains at least one exit point.
     * If no exit exists, this method places an exit behind a valid destructible wall.
     */
    private void ensureExitExists() {
        // Exit early if an exit already exists or if there are no destructible walls
        if (exit != null || destructibleWalls.isEmpty()) {
            return;
        }

        // Filter the destructible walls to find walls that would fit for exit
        List<DestructibleWall> potentialWallsForExit = new ArrayList<>();
        for (DestructibleWall wall : destructibleWalls) {
            if (isPotentialWallForExit(wall) && isWithinMapBounds(wall.getX(), wall.getY())) {
                potentialWallsForExit.add(wall); // Add valid walls to the list
            }
        }

        // If no valid walls are found, error and return
        if (potentialWallsForExit.isEmpty()) {
            return;
        }

        // Randomly select a valid wall from the filtered list
        DestructibleWall selectedWall = potentialWallsForExit.get((int) (Math.random() * potentialWallsForExit.size()));

        // Create the exit behind the selected wall
        exit = new Exit(selectedWall.getX(), selectedWall.getY(), game);
    }

    /**
     * Helper method to validate if a destructible wall is suitable for placing an exit.
     *
     * @param wall The destructible wall being validated.
     * @return True if the wall is valid for exit placement; false otherwise.
     */
    private boolean isPotentialWallForExit(DestructibleWall wall) {
        if (wall == null) {
            return false;
        }

        // Ensure the wall is within the bounds of the map
        if (wall.getX() < 0 || wall.getY() < 0 || wall.getX() >= mapWidth || wall.getY() >= mapHeight) {
            return false;
        }

        // Check if the wall already has an exit
        if (exit != null && Math.round(exit.getX()) == Math.round(wall.getX()) &&
                Math.round(exit.getY()) == Math.round(wall.getY())) {
            return false;
        }

        // Ensure no concurrent bomb power-up is at the same tile
        for (ConcurrentBombPowerUp powerUp : concurrentPowerUps) {
            if (Math.round(powerUp.getX()) == Math.round(wall.getX()) &&
                    Math.round(powerUp.getY()) == Math.round(wall.getY())) {
                return false;
            }
        }

        // Ensure no blast radius power-up is at the same tile
        for (BlastRadiusPowerUp powerUp : radiusPowerUps) {
            if (Math.round(powerUp.getX()) == Math.round(wall.getX()) &&
                    Math.round(powerUp.getY()) == Math.round(wall.getY())) {
                return false;
            }
        }
        return true; // Wall is valid for exit placement
    }

    /**
     * Ensures that an exit is placed only within the valid bounds of the map.
     *
     * @param x The x-coordinate of the potential exit location.
     * @param y The y-coordinate of the potential exit location.
     * @return True if the exit is within the map bounds; false otherwise.
     */
    private boolean isWithinMapBounds(float x, float y) {
        return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight;
    }

    /**
     * Adds a bomb to the game map.
     *
     * @param bomb The bomb to be added.
     */
    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
    }


    /**
     * Adds a blast to the game map.
     *
     * @param blast The blast to be added.
     */
    public void addBlast(Blast blast) {
        blasts.add(blast);
    }


    /**
     * Triggers the game over logic.
     * Handles player deaths and transitions to the Game Over screen.
     */
    public void gameOver(){

        game.setGameActive(false); // Mark the game as inactive

        // Check if the game is already over to prevent duplicate executions
        if(isGameOver){
            return; // Exit the method if the game is already over
        }

        // Handle multiplayer game-over logic.
        if(game.isMultiplayer()){
            // Trigger Player 1's death if not already dead
            if (player != null && !player.isDead()) {
                player.charactersDeath();
            }
            // Trigger Player 2's death if not already dead
            if (player2 != null && !player2.isDead()) {
                player2.charactersDeath();
            }
            // End the game only if both players are dead
            if ((player == null || player.isDead()) && (player2 == null || player2.isDead())) {
                endGameOver();
            }
        } else { // if only one player
            if (player != null && !player.isDead()) {
                player.charactersDeath();
            }
            endGameOver();
        }
    }

    /**
     * Helper method to handle the final game-over logic.
     * Stops the game music, plays the Game Over sound, and transitions to the Game Over screen.
     */
    private void endGameOver() {
        isGameOver = true; // Mark the game as over

        MusicTrack.GAME.stop();
        SoundEffect.GAMEOVER.play();

        // Schedule a transition to the GameOver-screen after the death-animation is finished
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                game.goToGameOver();
            }
        }, Animations.CHARACTER_DEATH.getAnimationDuration());
    }

    /**
     * Checks if a tile is blocked by an indestructible wall or if it's out of bounds.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return True if the tile is blocked; false otherwise.
     */
    public boolean isBlocked(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) return true;
        for (IndestructibleWall wall : indestructibleWalls) {
            if (Math.round(wall.getX()) == x && Math.round(wall.getY()) == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a tile contains a destructible wall.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return True if the tile contains a destructible wall; false otherwise.
     */
    public boolean isDestructibleWall(int x, int y) {
        for (DestructibleWall wall : destructibleWalls) {
            if (Math.round(wall.getX()) == x && Math.round(wall.getY()) == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a destructible wall from the game map.
     *
     * @param x The x-coordinate of the wall.
     * @param y The y-coordinate of the wall.
     */
    public void destroyDestructibleWall(int x, int y) {
        Iterator<DestructibleWall> iterator = destructibleWalls.iterator();
        while (iterator.hasNext()) {
            DestructibleWall wall = iterator.next();
            if (Math.round(wall.getX()) == x && Math.round(wall.getY()) == y) {
                iterator.remove();
                world.destroyBody(wall.getBody());
                break;
            }
        }
    }

    /**
     * Handles the collection of concurrent bomb power-ups by players.
     * Applies the power-up's effect and removes it from the map if collected.
     */
    public void collectingConcurrentBombPowerUp() {
        Iterator<ConcurrentBombPowerUp> iterator = concurrentPowerUps.iterator();
        while (iterator.hasNext()) {
            ConcurrentBombPowerUp concurrentBombPowerUp = iterator.next();

            //Player 1
            if (Math.round(player.getX()) == Math.round(concurrentBombPowerUp.getX()) &&
                    Math.round(player.getY()) == Math.round(concurrentBombPowerUp.getY())) {
                int currentLimit = player.getConcurrentBombLimit(); // Save the current limit
                concurrentBombPowerUp.applyEffect(player); // Apply the effect to the player
                if (player.getConcurrentBombLimit() > currentLimit) {
                    iterator.remove();
                }
            }

            //Player 2
            if (game.isMultiplayer() && player2 != null &&
                    Math.round(player2.getX()) == Math.round(concurrentBombPowerUp.getX()) &&
                    Math.round(player2.getY()) == Math.round(concurrentBombPowerUp.getY())) {
                int currentLimit = player2.getConcurrentBombLimit();
                concurrentBombPowerUp.applyEffect(player2);
                if (player2.getConcurrentBombLimit() > currentLimit) {
                    iterator.remove(); // Power-up has been consumed
                }
            }

        }
    }


    /**
     * Handles the collection of blast radius power-ups by players.
     * Applies the power-up's effect and removes it from the map if collected.
     */
    public void collectingBlastRadiusPowerUp() {
        Iterator<BlastRadiusPowerUp> iterator = radiusPowerUps.iterator();
        while (iterator.hasNext()) {
            BlastRadiusPowerUp blastRadiusPowerUp = iterator.next();

            //Player 1
            if (Math.round(player.getX()) == Math.round(blastRadiusPowerUp.getX()) &&
                    Math.round(player.getY()) == Math.round(blastRadiusPowerUp.getY())) {
                // Save the current limit
                int currentLimit = player.getBlastRadius();
                // Apply the effect to the player
                blastRadiusPowerUp.applyEffect(player);
                // Remove the power-up from the game map if the effect could be applied. The power-up should not be removed when the limit is reached.
                if (player.getBlastRadius() > currentLimit) {
                    iterator.remove();
                }
            }

            //Player 2
            // Check Player 2 in multiplayer mode
            if (game.isMultiplayer() && player2 != null &&
                    Math.round(player2.getX()) == Math.round(blastRadiusPowerUp.getX()) &&
                    Math.round(player2.getY()) == Math.round(blastRadiusPowerUp.getY())) {
                int currentLimit = player2.getBlastRadius();
                blastRadiusPowerUp.applyEffect(player2);
                if (player2.getBlastRadius() > currentLimit) {
                    iterator.remove(); // Power-up has been consumed
                }
            }
        }
    }

    /**
     * Randomly spawns invincible power-ups behind destructible walls.
     * Ensures that no other power-ups are already present behind the same wall.
     */
    private void randomSpawnInvinciblePowerUp(){
        // Filter destructible walls to find valid positions
        List<DestructibleWall> possibleWalls = destructibleWalls.stream()
                // Exclude walls that already cover an invincible power-up
                .filter(wall -> invinciblePowerUps.stream().noneMatch(p ->
                        Math.round(p.getX()) == Math.round(wall.getX()) &&
                                Math.round(p.getY()) == Math.round(wall.getY())))
                // Exclude walls that already cover a concurrent bomb limit power-up
                .filter(wall -> concurrentPowerUps.stream().noneMatch(p ->
                        Math.round(p.getX()) == Math.round(wall.getX()) &&
                                Math.round(p.getY()) == Math.round(wall.getY())))
                // Exclude walls that already cover a concurrent blast radius power-up
                .filter(wall -> radiusPowerUps.stream().noneMatch(p ->
                        Math.round(p.getX()) == Math.round(wall.getX()) &&
                                Math.round(p.getY()) == Math.round(wall.getY())))
                // Exclude walls that already cover a freeze power-up
                .filter(wall -> freezePowerUps.stream().noneMatch(p ->
                        Math.round(p.getX()) == Math.round(wall.getX()) &&
                                Math.round(p.getY()) == Math.round(wall.getY())))
                // Exclude the wall that is positioned on the exit
                .filter(wall -> Math.round(exit.getX()) != Math.round(wall.getX()) ||
                        Math.round(exit.getY()) != Math.round(wall.getY()))
                .toList();

        // Ensure there are enough possible walls to spawn the power-ups (2 power-ups should spawn)
        if (possibleWalls.size() >= 2) {
            for (int i = 0; i < 2; i++) {
                // Randomly select a wall from the possible walls
                DestructibleWall selectedWall = possibleWalls.get((int) (Math.random() * possibleWalls.size()));
                // Add an invincible power-up at the selected position
                invinciblePowerUps.add(new Invincible(selectedWall.getX(), selectedWall.getY()));
            }
        }
    }

    /**
     * Checks if either player collects an invincible power-up. When a power-up is collected:
     * - The amount of collected invincible power-ups is increased.
     * - The effects are applied.
     * - A sound effect is played.
     * - The power-up is removed from the map.
     */
    public void collectingInvinciblePowerUp() {
        // Iterate through the list of available invincible power-ups
        Iterator<Invincible> iterator = invinciblePowerUps.iterator();
        while (iterator.hasNext()) {
            Invincible invincible = iterator.next();

            // Single player mode
            if (player2 == null){
                // Check if the player collects an invincible power-up
                if (Math.round(player.getX()) == Math.round(invincible.getX()) &&
                        Math.round(player.getY()) == Math.round(invincible.getY())) {
                    // Play the sound for collecting a power-up
                    SoundEffect.POWERUP.play();
                    // Apply the effect to the player
                    invincible.applyEffect(player);
                    // Increase the amount of collected invincible power-ups
                    player.increaseCollectedInvinciblePowerUps();
                    // Remove the invincible power-up from the map
                    iterator.remove();
                }
            } else {
                // Multiplayer modus
                // Check if one of the two player collects invincible power-up
                if (Math.round(player.getX()) == Math.round(invincible.getX()) &&
                        Math.round(player.getY()) == Math.round(invincible.getY()) ||
                        Math.round(player2.getX()) == Math.round(invincible.getX()) &&
                                Math.round(player2.getY()) == Math.round(invincible.getY())) {
                    // Play the sound for collecting a power-up
                    SoundEffect.POWERUP.play();
                    // Apply the effect to both players
                    invincible.applyEffect(player);
                    invincible.applyEffect(player2);
                    // Increase the amount of collected invincible power-ups
                    player.increaseCollectedInvinciblePowerUps();
                    // Remove the invincible power-up from the map
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Freezes all enemies in the game for 15 seconds if no invincible or freeze power-up is currently active.
     * During this time, enemies cannot move or animate.
     *
     * @param enemies The list of enemies to freeze.
     */
    public void freezeEnemies(List<Enemy> enemies){
        // Check if not invincible or freeze power-up is active
        if (!isInvincibleActive && !isFreezeActive) {

            // Start the timer for the freeze power-up
            game.getHud().startSpecialPowerUpTimer();

            // Mark freeze as active
            isFreezeActive = true;

            // Freeze all enemies
            enemies.forEach(enemy -> enemy.setFrozen(true));

            // Play a specific music track during the time the enmies are frozen
            game.playMusicTrack(MusicTrack.FREEZE);

            // Schedule a timer to unfreeze enemies after 15 seconds
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    // Unfreeze all enemies
                    enemies.forEach(enemy -> enemy.setFrozen(false));
                    // Resume the regular game music track
                    game.playMusicTrack(MusicTrack.GAME);
                    // Mark freeze as inactive
                    isFreezeActive = false;
                }
            }, 15);
        }
    }

    /**
     * Randomly spawns freeze power-ups behind destructible walls.
     * Ensures that no other power-ups are already present behind the same wall.
     */
    private void randomSpawnFreezePowerUp(){

        // Filter destructible walls to find valid positions
        List<DestructibleWall> possibleWalls = destructibleWalls.stream()
                // Exclude walls that already cover an invincible power-up
                .filter(wall -> invinciblePowerUps.stream().noneMatch(p ->
                        Math.round(p.getX()) == Math.round(wall.getX()) &&
                                Math.round(p.getY()) == Math.round(wall.getY())))
                // Exclude walls that already cover a concurrent bomb limit power-up
                .filter(wall -> concurrentPowerUps.stream().noneMatch(p ->
                        Math.round(p.getX()) == Math.round(wall.getX()) &&
                                Math.round(p.getY()) == Math.round(wall.getY())))
                // Exclude walls that already cover a concurrent blast radius power-up
                .filter(wall -> radiusPowerUps.stream().noneMatch(p ->
                        Math.round(p.getX()) == Math.round(wall.getX()) &&
                                Math.round(p.getY()) == Math.round(wall.getY())))
                // Exclude walls that already cover a freeze power-up
                .filter(wall -> freezePowerUps.stream().noneMatch(p ->
                        Math.round(p.getX()) == Math.round(wall.getX()) &&
                                Math.round(p.getY()) == Math.round(wall.getY())))
                // Exclude the wall that is positioned on the exit
                .filter(wall -> Math.round(exit.getX()) != Math.round(wall.getX()) ||
                        Math.round(exit.getY()) != Math.round(wall.getY()))
                .toList();

        // Ensure there are enough possible walls to spawn the power-ups (2 power-ups should spawn)
        if (possibleWalls.size() >= 2) {
            for (int i = 0; i < 2; i++) {
                // Randomly select a wall from the possible walls
                DestructibleWall selectedWall = possibleWalls.get((int) (Math.random() * possibleWalls.size()));
                // Add an invincible power-up at the selected position
                freezePowerUps.add(new Freeze(selectedWall.getX(), selectedWall.getY()));
            }
        }
    }

     /** Checks if either player collects a freeze power-up. When a power-up is collected:
     * - The amount of collected freeze power-ups is increased.
     * - A sound effect is played.
     * - The power-up is removed from the map.
     */
    public void collectingFreezePowerUp() {
        // Iterate through the list of available invincible power-ups
        Iterator<Freeze> iterator = freezePowerUps.iterator();
        while (iterator.hasNext()) {
            Freeze freeze = iterator.next();

            // Single player mode
            if (player2 == null){
                // Check if the player collects an invincible power-up
                if (Math.round(player.getX()) == Math.round(freeze.getX()) &&
                        Math.round(player.getY()) == Math.round(freeze.getY())) {
                    // Play the sound for collecting a power-up
                    SoundEffect.POWERUP.play();
                    // Increase the amount of collected invincible power-ups
                    player.increaseCollectedFreezePowerUps();
                    // Remove the invincible power-up from the map
                    iterator.remove();
                }
            } else {
                // Multiplayer modus
                // Check if one of the two player collects invincible power-up
                if (Math.round(player.getX()) == Math.round(freeze.getX()) &&
                        Math.round(player.getY()) == Math.round(freeze.getY()) ||
                        Math.round(player2.getX()) == Math.round(freeze.getX()) &&
                                Math.round(player2.getY()) == Math.round(freeze.getY())) {
                    // Play the sound for collecting a power-up
                    SoundEffect.POWERUP.play();
                    // Increase the amount of collected invincible power-ups
                    player.increaseCollectedFreezePowerUps();
                    // Remove the invincible power-up from the map
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Checks if the player is at the exit and triggers interaction if conditions are met.
     * NOTE: For multiplayer mode: both players must be at the exit.
     */
    public void checkPlayerAtExit(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer, float deltaTime) {
        if (exit != null && !exit.isLocked()){

            // Check if Player 1 is at exit
            boolean player1AtExit = player != null &&
                    Math.round(player.getX()) == Math.round(exit.getX()) &&
                    Math.round(player.getY()) == Math.round(exit.getY());

            // Check if Player2 is at exit (if multiplayer mode is active)
            boolean player2AtExit = player2 != null &&
                    Math.round(player2.getX()) == Math.round(exit.getX()) &&
                    Math.round(player2.getY()) == Math.round(exit.getY());

            // Interact with the exit if all conditions are met
            if (!game.isMultiplayer() && player1AtExit) {
                exit.interact(); // Single-player: only Player 1 needs to reach the exit
            } else if (game.isMultiplayer()) {
                if (player1AtExit && player2AtExit) {
                    exit.interact(); // Multiplayer: Both players must reach the exit
                }
            }
        }
    }

    /**
     * Updates the difficulty level for all enemies in the game.
     * This method retrieves the current difficulty setting from the game and applies it to each enemy.
     * The difficulty level affects enemy behavior.
     */
    public void updateDifficulty() {
        // Iterate through all enemies in the game
        for (Enemy enemy : enemies) {
            // Update each enemy's difficulty to match the current game difficulty
            enemy.setDifficulty(game.getCurrentDifficulty());
        }
    }

    /**
     * Marks the bomb for its removal (after the bomb has exploded).
     * Ensures that the bomb will be removed in the next cleanup process.
     *
     * @param bomb The bomb
     */
    public void markBombForRemoval(Bomb bomb) {
        // Checks if the bomb is not already in the removal list
        if (!bombsToRemove.contains(bomb)) {
            // Adds the bomb to the removal list
            bombsToRemove.add(bomb);
        }
    }

    /**
     * Marks the blast for its removal (after the explosion).
     * Ensures that the blast will be removed in the next cleanup process.
     *
     * @param blast The blast
     */
    public void markBlastForRemoval(Blast blast) {
        // Checks if the blast is not already in the removal list
        if (!blastsToRemove.contains(blast)) {
            // Adds the blast to the removal list
            blastsToRemove.add(blast);
        }
    }

    /**
     * Removes all bombs and blasts that have been marked for removal.
     * This method is called during the cleanup process of the game loop.
     */
    public void removeBombsAndBlasts() {

        // Remove all marked bombs
        for (Bomb bomb : bombsToRemove) {
            bombs.remove(bomb);
        }

        // Clears the removal list of the bombs after processing
        bombsToRemove.clear();

        // Remove all marked blasts
        for (Blast blast : blastsToRemove) {
            blasts.remove(blast);

            // Delete the body of the blast from the physical world
            if (blast.getBody() != null) {
                world.destroyBody(blast.getBody());
            }
        }

        // Clears the removal list of the blasts after processing
        blastsToRemove.clear();
    }

    /**
     * Sets the paused state of all bombs in the game to true or false.
     *
     * @param isPaused True to pause the bomb, false to resume its countdown.
     */
    public void setBombsPaused(boolean isPaused){
        // Iterates through all bombs int the game
        for (Bomb bomb : bombs){
            // Sets the paused state of the current bomb
            bomb.setPaused(isPaused);
        }
    }

    /**
     * Updates the game state. This is called once per frame.
     * Every dynamic object in the game should update its state here.
     *
     * @param frameTime the time that has passed since the last update
     */
    public void tick(float frameTime) {

        /** Skip updates if the game is paused */
        if(game.isPaused()){
            return;
        }

        // Update the timer for the invincible and freeze power-up
        game.getHud().updateTimer(frameTime);

        // Update the status of the player
        this.player.tick(frameTime);

        //Update second player
        if(game.isMultiplayer() && player2 != null){
            this.player2.tickPlayer2(frameTime);
        }

        // List for all defeated Enemies
        List<Enemy> defeatedEnemies = new ArrayList<>();
        List<Enemy> removeBodies = new ArrayList<>();

        // Iterate through all enemies in the game
        for (Enemy enemy : this.enemies) {
            // Update the status of all enemies
            enemy.tick(frameTime);

            // Check if the enemy is dead and if the death animation has completed
            if (enemy.isDead() && enemy.getDeathStateTime() >= Animations.ENEMY_DEATH.getAnimationDuration()) {
                // Add the enemy to the removeBodies-list for the destruction of the body
                removeBodies.add(enemy);
                // Add the enemy to the defeatedEnemies-list to remove them from the game
                defeatedEnemies.add(enemy);
            }
        }

        // Remove the physical bodies of defeated enemies
        for (Enemy enemy : removeBodies) {
            if (enemy.getHitbox() != null && enemy.getHitbox().getWorld() != null) {

                // Destroy the enemy's body
                enemy.getHitbox().getWorld().destroyBody(enemy.getHitbox());

                // Clear the reference to the body to avoid further interactions
                enemy.setHitbox(null);
            }
        }

        // Remove all defeated enemies
        enemies.removeAll(defeatedEnemies);

        // Check if exit should be unlocked
        if (enemies.isEmpty() && exit.isLocked()) {
            exit.unlock(); // Unlock the exit
        }

        // Iterate over all bombs in the game, using a copy of the bombs list to avoid modification issues
        for (Bomb bomb : new ArrayList<>(bombs)) {
            // Updates the state of each bomb based on the elapsed frame time
            bomb.tick(frameTime);
        }

        // Iterate over all blasts in the game, using a copy of the blasts list to avoid modification issues
        for (Blast blast : new ArrayList<>(blasts)) {
            // Updates the state of each bomb based on the elapsed frame time
            blast.tick(frameTime);
        }

        // Checks if the global cooldown is higher than 0
        if (globalBombCooldown > 0) {
            // Reduce the global cooldown by the elapsed frame tim
            globalBombCooldown -= frameTime;
        }

        // Remove all bombs and blasts (cleanup process)
        removeBombsAndBlasts();

        collectingConcurrentBombPowerUp(); // Handle concurrent bomb power-ups
        collectingBlastRadiusPowerUp();   // Handle blast radius power-ups
        collectingInvinciblePowerUp();    // Handle invincible power-ups
        collectingFreezePowerUp();        // Handle freeze power-ups
        doPhysicsStep(frameTime);         // Call the doPhysicsStep method
    }

    /**
     * Performs as many physics steps as necessary to catch up to the given frame time.
     * This will update the Box2D world by the given time step.
     *
     * @param frameTime Time since last frame in seconds
     */
    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            synchronized (world) {
                this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            }
            this.physicsTime -= TIME_STEP;
        }
    }


    /**
     * Retrieves all drawable objects on the map in the correct rendering order.
     * This ensures that objects are drawn in layers, providing proper visual representation.
     *
     * Rendering Order:
     * 1. Flowers as the background
     * 2. Entrance (stones)
     * 3. Exit (door)
     * 4. Power-ups (including all types of power-ups like invincible, freeze, etc.).
     * 5. Destructible walls (may obscure power-ups placed behind them).
     * 6. Blasts (temporary explosion effects).
     * 7. Indestructible walls (stationary map elements).
     * 8. Bombs (active bombs placed by players or enemies).
     * 9. Enemies (characters controlled by the game AI).
     * 10. Players (player characters in the game).
     *
     * @return A list of drawable objects in the specified rendering order.
     */
    public List<Drawable> getDrawablesInRenderOrder() {
        List<Drawable> drawables = new ArrayList<>();
        for (int i = 0; i < flowers.length; i++) {
            for (int j = 0; j < flowers[i].length; j++) {
                if (flowers[i][j] != null) {
                    drawables.add(flowers[i][j]);
                }
            }
        }
        // Add all entrances from the list
        if (entrances != null && !entrances.isEmpty()) {
            drawables.addAll(entrances);
        }
        if (exit != null) drawables.add(exit);
        drawables.addAll(concurrentPowerUps);
        drawables.addAll(radiusPowerUps);
        drawables.addAll(invinciblePowerUps);
        drawables.addAll(freezePowerUps);
        drawables.addAll(destructibleWalls);
        drawables.addAll(blasts);
        drawables.addAll(indestructibleWalls);
        drawables.addAll(bombs);
        drawables.addAll(enemies);
        if (player != null) drawables.add(player);
        if(game.isMultiplayer()){
            if(player2 != null) drawables.add(player2);
        }
        return drawables;
    }

    /**
     * Dynamically creates a second entrance for Player 2 in multiplayer mode.
     * Iterates over the map to find a suitable location based on specific criteria.
     */
    private void createDynamicSecondEntrance() {
        boolean isASecondEntranceCreated = false;

        // Loop through the map to find a suitable location for the second entrance.
        for (int x = 1; x < mapWidth - 1; x++) {
            for (int y = 1; y < mapHeight - 1; y++) {
                if (suitableSecondEntrance(x, y)) {
                    // Create a new entrance at the selected location and add it to the list.
                    Entrance secondEntrance = new Entrance(x, y);
                    entrances.add(secondEntrance);
                    isASecondEntranceCreated = true;
                    break;
                }
            }
            if (isASecondEntranceCreated) break;
        }
    }

    /**
     * Creates Player 2 and assigns them to the second entrance in multiplayer mode.
     */
    private void createPlayer2() {
        // Get the second entrance from the list
        Entrance secondEntrance = entrances.get(1);
        player2 = new Player(world, secondEntrance.getX(), secondEntrance.getY(), this);
        players.add(player2); // add to overall list of existing players
    }

    /**
     * Checks if a tile is suitable for placing the second entrance.
     * A suitable location must:
     * 1. Be free of walls, players, enemies, or power-ups.
     * 2. Have at least two adjacent free tiles.
     * 3. Not have enemies directly adjacent.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return True if the tile is suitable; false otherwise.
     */
    private boolean suitableSecondEntrance(int x, int y) {
        // Ensure the tile itself is free
        if (getTileType(x, y) != -1) {
            return false;
        }

        // Count the number of free tiles around this position
        int freeTileCount = 0;
        if (getTileType(x + 1, y) == -1) freeTileCount++;
        if (getTileType(x - 1, y) == -1) freeTileCount++;
        if (getTileType(x, y + 1) == -1) freeTileCount++;
        if (getTileType(x, y - 1) == -1) freeTileCount++;

        // Ensure at least 2 free adjacent tiles (current + 2 adjacent = 3 total)
        if (freeTileCount < 2) {
            return false;
        }

        // Ensure no enemies are directly adjacent
        if (isEnemyNear(x, y)) {
            return false;
        }

        // The location is suitable if all conditions are met
        return true;
    }

    /**
     * Checks if there is an enemy adjacent to the given position.
     * An enemy is considered adjacent if it is on one of the four neighboring tiles.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return True if an enemy is adjacent; false otherwise.
     */
    private boolean isEnemyNear(int x, int y) {
        for (Enemy enemy : enemies) {
            int enemyX = Math.round(enemy.getX());
            int enemyY = Math.round(enemy.getY());

            // Check all 4 adjacent tiles
            if ((enemyX == x + 1 && enemyY == y) || (enemyX == x - 1 && enemyY == y) || (enemyX == x && enemyY == y + 1) || (enemyX == x && enemyY == y - 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines the type of tile at the given coordinates.
     * This method checks if the tile contains any walls, entrances, enemies, power-ups, or the exit.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return An integer representing the tile type:
     *         0 - Indestructible Wall
     *         1 - Destructible Wall
     *         2 - Entrance
     *         3 - Enemy
     *         4 - Exit
     *         5 - Concurrent Bomb Power-up
     *         6 - Blast Radius Power-up
     *        -1 - Free Tile
     */
    private int getTileType(int x, int y) {
        for (IndestructibleWall wall : indestructibleWalls) {
            if (wall.getX() == x && wall.getY() == y) return 0; // Indestructible Wall
        }
        for (DestructibleWall wall : destructibleWalls) {
            if (wall.getX() == x && wall.getY() == y) return 1; // Destructible Wall
        }
        // Check for entrances in the list of entrances
        if (entrances != null) {
            for (Entrance entrance : entrances) {
                if (entrance.getX() == x && entrance.getY() == y) return 2; // Entrance
            }
        }
        if (exit != null && exit.getX() == x && exit.getY() == y) return 4; // Exit
        for (ConcurrentBombPowerUp powerUp : concurrentPowerUps) {
            if (powerUp.getX() == x && powerUp.getY() == y) return 5; // Concurrent Bomb Power-up
        }
        for (BlastRadiusPowerUp powerUp : radiusPowerUps) {
            if (powerUp.getX() == x && powerUp.getY() == y) return 6; // Blast Radius Power-up
        }
        return -1; // Tile is empty
    }

    /**
     * Disposes of all resources used by the GameMap.
     * This includes freeing up memory allocated for the Box2D world.
     */
    public void dispose() {
        // Disposes the Box2D world
        if (world != null) {
            world.dispose();
        }

    }

    /** Getters */
    public BomberQuestGame getGame() {
        return game;
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Bomb> getBombs() {
        return bombs;
    }

    public float getMapWidth() {
        return mapWidth;
    }

    public float getMapHeight() {
        return mapHeight;
    }

    public Player getPlayer2() {
        return player2;
    }

    public boolean isFreezeActive() {
        return isFreezeActive;
    }

    public void setInvincibleActive(boolean invincibleActive) {
        isInvincibleActive = invincibleActive;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public Enemy getEnemyBombPlacer() {
        return enemyBombPlacer;
    }

    public void setEnemyBombPlacer(Enemy enemyBombPlacer) {
        this.enemyBombPlacer = enemyBombPlacer;
    }

    public float getGlobalBombCooldown() {
        return globalBombCooldown;
    }

    public void setGlobalBombCooldown(float cooldown) {
        this.globalBombCooldown = cooldown;
    }
}




