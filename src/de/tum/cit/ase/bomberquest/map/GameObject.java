package de.tum.cit.ase.bomberquest.map;

/**
 * Represents the base class for all game objects on the map.
 * Each game object has a fixed position defined by its x and y coordinates.
 * This class can be extended by other specific game object types (e.g., walls, power-ups, players).
 */
public class GameObject {

    protected final float x; // x-coordinate of the game object on the map
    protected final float y; // y-coordinate of the game object on the map

    /**
     * Constructor to initialize a game object with a specific position.
     * @param x The x-coordinate of the game object's position.
     * @param y The y-coordinate of the game object's position.
     */
    public GameObject(float x, float y) {
        this.x = x;
        this.y = y;
    }

}
