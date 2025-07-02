package de.tum.cit.ase.bomberquest.map.mappoints;

import de.tum.cit.ase.bomberquest.map.GameObject;
import de.tum.cit.ase.bomberquest.texture.Drawable;

public abstract class MapPoints extends GameObject implements Drawable {

    /**
     * Represents a point of interest on the game map.
     * MapPoints are static objects that can interact with the player or the environment,
     * such as entrances, exits, or special markers.
     * This class serves as a base for more specific map points and defines common behavior.
     */
    public MapPoints(float x, float y) {
        super(x, y);
    }

    /**
     * Retrieves the x-coordinate of the MapPoint.
     * @return The x-coordinate of this MapPoint.
     */
    public float getX() {
        return x;
    }

    /**
     * Retrieves the y-coordinate of the MapPoint.
     *
     * @return The y-coordinate of this MapPoint.
     */
    public float getY() {
        return y;
    }

    /**
     * Defines the interaction logic for the MapPoint.
     * Subclasses must implement this method to specify the behavior when the MapPoint is interacted with.
     */
    public abstract void interact();
}