package de.tum.cit.ase.bomberquest.map;

/**
 * Helper class for the A* pathfinding algorithm used in the Enemy class.
 * Represents a tile on the game map, storing its coordinates and pathfinding costs.
 * This class is essential for navigating through the map step-by-step and reconstructing paths.
 */
public class Tile {


    public int x;  //x-coordinate of the tile on the game map.
    public int y;  //y-coordinate of the tile on the game map.
    public Tile tileBefore; // Reference to the previous tile in the path (used to reconstruct the path step by step)

    // Pathfinding costs for the A* algorithm.
    public double costStartToCurrent; // cost to move from the starting tile to this tile
    public double costCurrentToTarget; // estimated cost (heuristic) to move from this tile to the target
    public double totalCost; // The total cost of this tile, which is the sum of gCost and hCost

    /**
     * Constructs a new Tile object.
     *
     * @param x                   The x-coordinate of the tile.
     * @param y                   The y-coordinate of the tile.
     * @param tileBefore          The previous tile in the path, or null if this is the starting tile.
     * @param costStartToCurrent  The cost to move from the start tile to this tile (gCost).
     * @param costCurrentToTarget The estimated cost to move from this tile to the target tile (hCost).
     */
    public Tile(int x, int y, Tile tileBefore, double costStartToCurrent, double costCurrentToTarget) {
        this.x = x;
        this.y = y;
        this.tileBefore = tileBefore;
        this.costStartToCurrent = costStartToCurrent;
        this.costCurrentToTarget = costCurrentToTarget;
        // total cost of this tile as the sum of gCost and hCost
        this.totalCost = costStartToCurrent + costCurrentToTarget;
    }

}
