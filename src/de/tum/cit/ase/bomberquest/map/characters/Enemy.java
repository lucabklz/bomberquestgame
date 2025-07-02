package de.tum.cit.ase.bomberquest.map.characters;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.ase.bomberquest.BomberQuestGame;
import de.tum.cit.ase.bomberquest.map.Tile;
import de.tum.cit.ase.bomberquest.map.Bomb;
import de.tum.cit.ase.bomberquest.map.GameMap;
import de.tum.cit.ase.bomberquest.texture.Animations;
import de.tum.cit.ase.bomberquest.texture.Textures;

import java.util.*;

/**
 * Represents an enemy in the game.
 * Enemies exhibit different behaviors based on the game's difficulty level.
 * They can move randomly, chase players, place bombs, and get frozen or killed.
 *
 * Sources regarding A*-Pathfinding-Algorithm:
 * - Concept of Pathfinding Algorithm: https://www.youtube.com/watch?v=2JNEme00ZFA
 * - Concept and Examples: https://www.happycoders.eu/algorithms/a-star-algorithm-java/
 */
public class Enemy extends Character {

    private Random random = new Random(); // Attribute used for generating random directions for movement.
    private final GameMap map; // Reference to the game map, used for checking collisions and player positions.
    private int[] currentDirection = {0, 0}; // Current movement direction of the enemy (x, y).
    private float changeDirectionCooldown = 1.0f; // Time remaining before the enemy can change its movement direction.
    private String direction = "down"; // Current facing direction of the enemy for animations.
    private boolean isFrozen = false; // Indication if the enemy is frozen and unable to move.
    private World world; // The Box2D world where the enemy's physics body exists.
    private float bombPlacementCooldown = 2.0f; // Cooldown time before the enemy can place another bomb.
    private BomberQuestGame.Difficulty difficulty; // Current game difficulty (Easy, Medium, Hard), affecting enemy behavior.
    private final int detectionRadius = 5; // A* Pathfinding algorithm: Radius for detecting players using the A* pathfinding algorithm.

    /**
     * Constructor: Creates a new enemy at the specified position.
     *
     * @param world The Box2D world.
     * @param x     The starting x-coordinate.
     * @param y     The starting y-coordinate.
     * @param map   The game map to which the enemy belongs.
     */
    public Enemy(World world, float x, float y, GameMap map) {
        super(world, x, y);
        this.map = map;
        this.world = world;
        this.difficulty = map.getGame().getCurrentDifficulty();
        randomNewDirection();
    }

    /**
     * Implements the abstract createHitbox method from the Character class.
     * This method creates a rectangular hitbox for the enemy.
     * The hitbox is used for collision detection and interactions with other objects in the game.
     *
     * @param world  The Box2D world.
     * @param startX The initial X position of the enemy.
     * @param startY The initial Y position of the enemy.
     * @return The created Body instance representing the enemy's hitbox.
     */
    @Override
    protected Body createHitbox(World world, float startX, float startY) {
        // Define the properties of the body (dynamic, moves with physics forces) and set the initial position
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody; // Dynamic body affected by physics forces
        bodyDef.position.set(startX, startY); // Set the starting position of the hitbox

        // Create the body in the Box2D world
        Body body = world.createBody(bodyDef);

        // Define the shape of the body (rectangle, 0.6x0.6 units in size)
        PolygonShape polygon = new PolygonShape();
        polygon.setAsBox(0.3f, 0.3f); // Half-width and half-height (0.3 each for a 0.6x0.6 tile)

        // Attach the shape to the body as a fixture
        body.createFixture(polygon, 1.0f); // Density set to 1.0f

        // Dispose of the shape to free memory
        polygon.dispose();

        // Associate this enemy instance with the body for easy reference during interactions
        body.setUserData(this);

        return body;
    }


    /**
     * Attempts to place a bomb at the enemy's current position.
     * Ensures that bombs are only placed if both the individual and global cooldowns have expired.
     * Prevents bomb placement at the same position as an existing bomb.
     *
     * @param frameTime The time elapsed since the last frame.
     */
    public void placeBomb(float frameTime) {
        // Decrease the individual bomb placement cooldown
        bombPlacementCooldown -= frameTime;

        // Check if the cooldowns have expired and no other enemy is currently placing a bomb
        if (bombPlacementCooldown <= 0 && map.getGlobalBombCooldown() <= 0 && map.getEnemyBombPlacer() == null) {
            // Mark this enemy as the active bomb placer
            map.setEnemyBombPlacer(this);

            // Get the enemy's current position
            int positionX = Math.round(this.getX());
            int positionY = Math.round(this.getY());

            // Check if a bomb is already at this position
            for (Bomb bomb : map.getBombs()) {
                if (Math.round(bomb.getX()) == positionX && Math.round(bomb.getY()) == positionY) {
                    return; // It is not allowed to place a bomb at the same position
                }
            }

            // Create a new bomb
            Bomb bomb = new Bomb(positionX, positionY, 2, () -> {
                // Reset the active bomb placer when the bomb explodes
                map.setEnemyBombPlacer(null);
                // Reset the global cooldown
                map.setGlobalBombCooldown(2.0f);
            }, map, this);

            // Add the bomb to the game map
            map.addBomb(bomb);

            // Reset the individual cooldown
            bombPlacementCooldown = 2.0f;
        }
    }


    /**
     * Checks if a player is within the detection radius of the enemy.
     * The detection radius determines whether the enemy can sense the player's presence.
     *
     * @param player The player whose position is being checked.
     * @return True if the player is within the detection radius; false otherwise.
     */
    private boolean isPlayerInRadius(Player player) {

        // Get the players position from their hitbox
        float playerX = Math.round(player.getHitbox().getPosition().x);
        float playerY = Math.round(player.getHitbox().getPosition().y);

        // Get the enemys position from their hitbox
        float enemyX = Math.round(hitbox.getPosition().x);
        float enemyY = Math.round(hitbox.getPosition().y);

        // Calculate Euclidean distance between the enemy and the player
        float distance = Vector2.dst(enemyX, enemyY, playerX, playerY);

        // Return true if the calculated distance is within or equal to the detection radius
        return distance <= detectionRadius;
    }


    /**
     * Finds the closest player to the enemy.
     * If the game is in single-player mode, this simply returns the only player.
     * In multiplayer mode, the enemy identifies the nearest player based on distance.
     *
     * @return The closest player to the enemy, or null if no players are detected.
     */
    private Player findClosestPlayer() {

        // Single-player mode: directly return the single player.
        if (!map.getGame().isMultiplayer()) {
            return map.getPlayer(); // Singleplayer: Return the only player
        }

        Player closestPlayer = null; // Stores the closest player found
        float closestDistance = Float.MAX_VALUE; // Tracks the smallest distance found (default to max)

        // Iterate over all players in the game.
        for (Player player : map.getPlayers()) { // getPlayers() returns List of all players

            // Get the players position.
            float playerX = Math.round(player.getHitbox().getPosition().x);
            float playerY = Math.round(player.getHitbox().getPosition().y);

            // Get the enemys position.
            float enemyX = Math.round(hitbox.getPosition().x);
            float enemyY = Math.round(hitbox.getPosition().y);

            // Calculate the Euclidean distance between the enemy and the player
            float distance = Vector2.dst(enemyX, enemyY, playerX, playerY);

            // Update the closest player and distance if the current player is closer
            if (distance < closestDistance) {
                closestPlayer = player;
                closestDistance = distance;
            }
        }
        // Return the closest player found, or null if no players were detected.
        return closestPlayer;
    }


    /**
     * A* algorithm to find the shortest path from a start tile to a target tile.
     * The algorithm calculates the cost of each tile and explores the most promising tiles first
     * (those with the lowest total cost: start-to-current + heuristic to target).
     *
     * Steps:
     * 1. Add the starting tile to a priority queue.
     * 2. Iteratively explore the tile with the lowest total cost.
     * 3. For each explored tile, add its neighbors to the queue, updating their costs if a better path is found.
     * 4. Stop when the target tile is reached or there are no more tiles to explore.
     *
     * @param startX  The x-coordinate of the starting tile.
     * @param startY  The y-coordinate of the starting tile.
     * @param targetX The x-coordinate of the target tile.
     * @param targetY The y-coordinate of the target tile.
     * @param map     The game map used for pathfinding and collision checks.
     * @return A list of tile coordinates representing the path from start to target, or null if no path is found.
     */
    public List<int[]> findPath(int startX, int startY, int targetX, int targetY, GameMap map) {

        // Priority queue to store tiles to explore, sorted by total cost (lowest cost tiles are processed first)
        PriorityQueue<Tile> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.totalCost));

        // Map to track all tiles already explored or queued (avoids re-checking the same tile)
        Map<String, Tile> allTiles = new HashMap<>();

        // Create the starting tile with zero cost and calculate its heuristic cost to the target
        Tile startTile = new Tile(startX, startY, null, 0, heuristic(startX, startY, targetX, targetY));

        // Add the starting tile to the priority queue and mark it as explored in the map
        priorityQueue.add(startTile);
        allTiles.put(keyForTile(startX, startY), startTile);

        // Main loop: Process tiles from the queue until the target is found or the queue is empty
        while (!priorityQueue.isEmpty()) {

            // Remove the tile with the lowest total cost from the queue
            Tile current = priorityQueue.poll();

            // If the target tile is reached, construct and return the path
            if (current.x == targetX && current.y == targetY) {
                return constructPath(current);
            }

            // Explore all neighboring tiles (up, down, left, right)
            for (int[] direction : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}}) {

                // Calculate the coordinates of the neighbor tile
                int newX = current.x + direction[0];
                int newY = current.y + direction[1];

                // Skip the neighbor if it is blocked or contains a destructible wall
                if (map.isBlocked(newX, newY)) continue;
                if (map.isDestructibleWall(newX, newY)) continue;

                // Calculate the cost to move to this neighbor tile
                double gCost = current.costStartToCurrent + 1;

                // Check if the neighbor has already been explored or queued. If not, create it.
                Tile neighbor = allTiles.getOrDefault(
                        keyForTile(newX, newY),
                        new Tile(newX, newY, null, Double.MAX_VALUE, heuristic(newX, newY, targetX, targetY))
                );

                // If path to neighbor is better (lower cost), update its costs and set its previous tile. Update path if a better route is found.
                if (gCost < neighbor.costStartToCurrent) {
                    neighbor.costStartToCurrent = gCost; // Update the cost from start to current
                    neighbor.totalCost = gCost + neighbor.costCurrentToTarget; // Update the total cost
                    neighbor.tileBefore = current; // Set current tile as "previous tile" (to keep track of the path)

                    // Add neighbor to the priority queue if not already there and mark it as explored in the map
                    priorityQueue.add(neighbor);
                    allTiles.put(keyForTile(newX, newY), neighbor);
                }
            }
        }
        // If the loop ends without finding the target, return null (no valid path).
        return null;
    }

    /**
     * Calculates the estimated distance from the current tile to the target tile.
     * Uses Manhattan distance: only considers horizontal and vertical steps (no diagonal movement).
     * Manhattan distance = |x1 - x2| + |y1 - y2|
     *
     * @param x       The x-coordinate of the current tile.
     * @param y       The y-coordinate of the current tile.
     * @param targetX The x-coordinate of the target tile.
     * @param targetY The y-coordinate of the target tile.
     * @return The Manhattan distance between the current tile and the target tile.
     */
    private double heuristic(int x, int y, int targetX, int targetY) {
        return Math.abs(x - targetX) + Math.abs(y - targetY);
    }

    /**
     * Creates a unique key for a specific tile based on its coordinates.
     * The key is used to store and retrieve tiles from the map during pathfinding.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return A string key in the format "x,y".
     */
    private String keyForTile(int x, int y) {
        return x + "," + y;
    }


    /**
     * Constructs the path from the target tile back to the starting tile.
     * The path is built by traversing backwards through the `tileBefore` references in each tile.
     *
     * @param tile The target tile from which the path will be constructed.
     * @return A list of tile coordinates representing the path from the start to the target.
     */
    private List<int[]> constructPath(Tile tile) {
        List<int[]> path = new ArrayList<>(); // List to store the constructed path

        // Traverse backwards from the target tile to the start tile
        while (tile != null) {
            path.add(new int[]{tile.x, tile.y}); // Add the tile's coordinates to the path
            tile = tile.tileBefore; // Move to the previous tile in the path
        }
        Collections.reverse(path); // Reverse the list so it goes from start to target
        return path;
    }

    /**
     * Attempts to move the enemy towards the specified player (when using A* pathfinding).
     * The method calculates the shortest path to the player, checks for obstacles,
     * and updates the enemy's movement direction accordingly.
     *
     * @param player The target player the enemy should move towards.
     * @return True if a valid path to the player was found and followed; false otherwise.
     */
    private boolean moveTowardsPlayer(Player player) {

        // Get the enemy's current position
        int startXenemy = Math.round(hitbox.getPosition().x); //start x-coordinate of enemy
        int startYenemy = Math.round(hitbox.getPosition().y); //start y-coordinate of enemy

        // Get the player's current position
        int targetXplayer = Math.round(player.getHitbox().getPosition().x);
        int targetYplayer = Math.round(player.getHitbox().getPosition().y);

        // Use A* pathfinding to calculate a path from the enemy to the player
        // The path is a list of tile coordinates (x, y) the enemy should follow
        List<int[]> path = findPath(startXenemy, startYenemy, targetXplayer, targetYplayer, map);

        // If no path is found or the path is empty, return false
        if (path == null || path.isEmpty()) {
            return false; // No path found
        }

        // Check if any tile in the path is blocked by a destructible wall
        for (int[] tile : path) {
            if (map.isDestructibleWall(tile[0], tile[1])) {
                return false; // Path blocked by destructible wall
            }
        }

        // Get the next tile in the path
        int[] nextTile = path.get(0);

        // If the next tile is the enemy's current position and there are more steps in the path, skip to the next tile
        if (nextTile[0] == startXenemy && nextTile[1] == startYenemy && path.size() > 1) {
            nextTile = path.get(1); // Move to the next tile in the path
        }

        // Update the enemy's movement direction based on the next tile
        currentDirection[0] = nextTile[0] - startXenemy;
        currentDirection[1] = nextTile[1] - startYenemy;

        return true; // Path was successfully followed
    }


    /**
     * Chooses a new random direction for the enemy to move.
     * The direction is chosen from all valid options (not blocked by walls, destructible walls, or other enemies).
     */
    private void randomNewDirection() {
        // List with all possible directions
        List<int[]> availableDirections = new ArrayList<>();

        // Check if the enemy can move up
        if (!map.isBlocked((int) hitbox.getPosition().x, (int) hitbox.getPosition().y + 1)&&
                !map.isDestructibleWall((int) hitbox.getPosition().x, (int) hitbox.getPosition().y + 1)) {
            availableDirections.add(new int[]{0, 1});
        }

        // Check if the enemy can move down.
        if (!map.isBlocked((int) hitbox.getPosition().x, (int) hitbox.getPosition().y - 1) &&
                !map.isDestructibleWall((int) hitbox.getPosition().x, (int) hitbox.getPosition().y - 1)) {
            availableDirections.add(new int[]{0, -1});
        }
        // Checks if it is possible to move left
        if (!map.isBlocked((int) hitbox.getPosition().x - 1, (int) hitbox.getPosition().y)&&
                !map.isDestructibleWall((int) hitbox.getPosition().x - 1, (int) hitbox.getPosition().y)) {
            availableDirections.add(new int[]{-1, 0});
        }

        // Check if the enemy can move right
        if (!map.isBlocked((int) hitbox.getPosition().x + 1, (int) hitbox.getPosition().y)&&
                !map.isDestructibleWall((int) hitbox.getPosition().x + 1, (int) hitbox.getPosition().y)) {
            availableDirections.add(new int[]{1, 0});
        }

        // Remove any directions that would result in a collision with another enemy
        availableDirections.removeIf(direction -> collusionWithAnotherEnemy(
                (int) hitbox.getPosition().x + direction[0],
                (int) hitbox.getPosition().y + direction[1]
        ));

        // If there are valid directions available, choose one randomly
        if (!availableDirections.isEmpty()) {
            currentDirection = availableDirections.get(random.nextInt(availableDirections.size()));

            // Update the direction string for animations
            if (currentDirection[0] == 0 && currentDirection[1] == 1) {
                direction = "up";
            }
            else if (currentDirection[0] == 0 && currentDirection[1] == -1) {
                direction = "down";
            } else if (currentDirection[0] == -1 && currentDirection[1] == 0) {
                direction = "left";
            } else if (currentDirection[0] == 1 && currentDirection[1] == 0) {
                direction = "right";
            }

        } else {
            // If no directions are valid, stop the enemy's movement
            hitbox.setLinearVelocity(0, 0);
        }
    }

    /**
     * Checks if the enemy at the specified position will collide with another enemy.
     * This ensures that enemies do not occupy or move to the same position.
     *
     * @param x The x-coordinate of the position to check.
     * @param y The y-coordinate of the position to check.
     * @return True if the position would result in a collision with another enemy; false otherwise.
     */
    private boolean collusionWithAnotherEnemy(int x, int y){
        for (Enemy enemy : map.getEnemies()){
            if (enemy != this){  // Skip checking this enemy against itself.
                // Check if the other enemy is close to the given position (within 1.0f distance on both axes)
                if (Math.abs(enemy.getHitbox().getPosition().x - x) <= 1.0f &&
                        Math.abs(enemy.getHitbox().getPosition().y - y) <= 1.0f) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Marks the enemy as dead by setting the isDead attribute to true.
     * Prevents further updates or actions if the enemy is already dead.
     */
    @Override
    public void charactersDeath() {
        // If the enemy is already marked as dead, exit the method
        if(isDead){
            return;
        }
        // Call the superclass implementation to handle additional death logic
        super.charactersDeath();
    }


    /**
     * Updates the enemy's behavior and state every frame.
     * This includes movement, chasing players, placing bombs, and handling special states like freezing or death.
     *
     * @param frameTime Time elapsed since the last frame.
     */
    public void tick(float frameTime) {
        // Increment the elapsed time for the enemy
        elapsedTime += frameTime;

        // Stop movement if the enemy is dead
        if (isDead) {
            // Adjust the time of the death-animation
            deathStateTime += frameTime;
            // Stop the movement of the enemy
            hitbox.setLinearVelocity(0,0);
            return;
        }

        // Stop movement if the enemy is frozen
        if (isFrozen){
            // Stop the movement of the enemy
            hitbox.setLinearVelocity(0,0);
            // End the method
            return;
        }

        // Decrease cooldown timers
        changeDirectionCooldown -= frameTime;

        // Perform actions based on the current game difficulty
        switch (this.difficulty) {
            case EASY -> tickEasy(frameTime); // Logic for easy difficulty
            case MEDIUM -> tickMediumAndHard(frameTime); // Logic for medium difficulty
            case HARD -> tickMediumAndHard(frameTime); // Logic for hard difficulty
        }
    }

    /**
     * Handles enemy behavior in the "easy" difficulty mode.
     * The enemy moves randomly and changes direction at regular intervals.
     *
     * @param frameTime The time elapsed since the last frame.
     */
    private void tickEasy(float frameTime){

        // If the cooldown has expired, choose a new random direction
        if (changeDirectionCooldown <= 0) {
            randomNewDirection();
            changeDirectionCooldown = 1f; // Reset cooldown
        }

        //Calculate the next coordinates based on the current position of the enemy
        int nextX = (int) (hitbox.getPosition().x + currentDirection[0]);
        int nextY = (int) (hitbox.getPosition().y + currentDirection[1]);

        //If the next move is blocked by a game object, the enemy should choose a new Direction
        if (map.isBlocked(nextX, nextY) || collusionWithAnotherEnemy(nextX, nextY)) {
            randomNewDirection();
        }

        // Update the enemy's movement based on the current direction
        hitbox.setLinearVelocity(currentDirection[0] * 1.0f, currentDirection[1] * 1.0f);
    }

    /**
     * Handles enemy behavior in the "medium" and "hard" difficulty modes.
     * In these modes, the enemy attempts to follow the player if they are within detection range.
     * If no player is found or reachable, the enemy moves randomly.
     * In "hard" difficulty, enemies can place bombs.
     *
     * @param frameTime The time elapsed since the last frame.
     */
    private void tickMediumAndHard(float frameTime) {
        Player closestPlayer = findClosestPlayer();

        // If the player is within radius and the cooldown is finished, try moving toward the player
        if (closestPlayer != null && isPlayerInRadius(closestPlayer)) {

            if (changeDirectionCooldown <= 0) { //check if the cooldown is already at 0 (Ensure that path is not always instantly changed)
                // Attempt to move along a path toward the player
                boolean pathFound = moveTowardsPlayer(closestPlayer);

                // If no path is found, the enemy should move randomly
                if (!pathFound) {
                    randomNewDirection();
                }
                // Reset the cooldown to prevent constant direction switching.
                changeDirectionCooldown = 0.5f;
            }

        }
        // If the player is not in range or no path is found, the enemy should move randomly
        else if (changeDirectionCooldown <= 0) {
            randomNewDirection();
            changeDirectionCooldown = 0.5f;
        }

        // Apply movement velocity in the chosen direction (chasing or random)
        hitbox.setLinearVelocity(currentDirection[0] * 1.2f, currentDirection[1] * 1.2f);

        // In "hard" difficulty, enemies attempt to place bombs and are a little bit faster
        if (map.getGame().getCurrentDifficulty() == BomberQuestGame.Difficulty.HARD) {

            // Apply movement velocity in the chosen direction (chasing or random)
            hitbox.setLinearVelocity(currentDirection[0] * 1.5f, currentDirection[1] * 1.5f);

            // Select a random enemy to place a bomb
            List<Enemy> enemies = map.getEnemies();
            if (!enemies.isEmpty()) {
                Enemy randomEnemy = enemies.get(new Random().nextInt(enemies.size()));
                if (randomEnemy != null) {
                    randomEnemy.placeBomb(frameTime);
                }
            }
        }
    }

    /**
     * Determines the current appearance of the enemy based on its state.
     * The appearance is determined by the enemy's movement direction, frozen state, or death state.
     *
     * @return The appropriate TextureRegion for the enemy's current state.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        // If the enemy is dead, return the current frame of the death animation.
        if (isDead){
            return Animations.ENEMY_DEATH.getKeyFrame(deathStateTime, false);
        }

        // If the enemy is frozen, return the static frozen texture.
        if (isFrozen){
            return Textures.FROZEN_ENEMIES;
        }

        // If the enemy is not moving, return a freeze-frame from the walk-down-animation.
        if (this.hitbox.getLinearVelocity().isZero()){
            return Animations.ENEMY_WALK_DOWN.getKeyFrame(0);
        }

        // Return the appropriate animation frame based on the current movement direction.
        switch (direction){
            case "up":
                return Animations.ENEMY_WALK_UP.getKeyFrame(this.elapsedTime, true);
            case "down":
                return Animations.ENEMY_WALK_DOWN.getKeyFrame(this.elapsedTime, true);
            case "left":
                return Animations.ENEMY_WALK_LEFT.getKeyFrame(this.elapsedTime, true);
            case "right":
                return Animations.ENEMY_WALK_RIGHT.getKeyFrame(this.elapsedTime, true);
            default: // Default to the "walk down" animation if no direction is set.
                return Animations.ENEMY_WALK_DOWN.getKeyFrame(this.elapsedTime, true);
        }
    }


    /**
     * Releases resources associated with this enemy to free memory and prevent memory leaks.
     * This method should be called when the enemy is no longer needed.
     */
    public void dispose() {
        if (hitbox != null) {
            world.destroyBody(hitbox);
            hitbox = null;
        }
    }

    /**
     * Checks if the enemy is currently marked as dead.
     *
     * @return True if the enemy is dead; false otherwise.
     */
    public boolean isDead(){
        return isDead;
    }

    /**
     * Checks if the enemy is currently frozen.
     *
     * @return True if the enemy is frozen; false otherwise.
     */
    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }

    /**
     * Sets the difficulty level for the enemy.
     * The difficulty level affects the enemy's behavior.
     *
     * @param difficulty The difficulty level to set.
     */
    public void setDifficulty(BomberQuestGame.Difficulty difficulty) {
        this.difficulty = difficulty;
    }

}
