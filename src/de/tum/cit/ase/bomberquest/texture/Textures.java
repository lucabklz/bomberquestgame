package de.tum.cit.ase.bomberquest.texture;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Contains all texture constants used in the game.
 * It is good practice to keep all textures and animations in constants to avoid loading them multiple times.
 * These can be referenced anywhere they are needed.
 */
public class Textures {

    /** Decorative textures */

    // A texture representing flowers (from the BASIC_TILES spritesheet).
    public static final TextureRegion FLOWERS = SpriteSheet.BASIC_TILES.at(2, 5);


    /** Textures for game objects */

    // Indestructible wall texture (from the ORIGINAL_BOMBERMAN spritesheet).
    public static final TextureRegion INDESTRUCTIBLE_WALL = SpriteSheet.ORIGINAL_BOMBERMAN.at(4, 4);

    // Destructible wall texture (from the ORIGINAL_BOMBERMAN spritesheet).
    public static final TextureRegion DESTRUCTIBLE_WALL = SpriteSheet.ORIGINAL_BOMBERMAN.at(4, 5);

    // Entrance texture (from the BASIC_TILES spritesheet).
    public static final TextureRegion ENTRANCE = SpriteSheet.BASIC_TILES.at(8, 3);

    // Exit texture (from the BASIC_TILES spritesheet).
    public static final TextureRegion EXIT = SpriteSheet.BASIC_TILES.at(7, 1);

    // Locked exit texture (from the BASIC_TILES spritesheet).
    public static final TextureRegion EXIT_LOCKED = SpriteSheet.BASIC_TILES.at(7, 2);

    /** Power-up textures */

    // Power-up to increase the number of concurrent bombs (from the ORIGINAL_BOMBERMAN spritesheet).
    public static final TextureRegion CONCURRENT_BOMB_POWER_UP = SpriteSheet.ORIGINAL_BOMBERMAN.at(15, 1);

    // Power-up to increase the blast radius of bombs (from the ORIGINAL_BOMBERMAN spritesheet).
    public static final TextureRegion BLAST_RADIUS_POWER_UP = SpriteSheet.ORIGINAL_BOMBERMAN.at(15, 2);

    // Texture for invincibility power-up (from the ORIGINAL_BOMBERMAN spritesheet).
    public static final TextureRegion INVINCIBLE = SpriteSheet.ORIGINAL_BOMBERMAN.at(15, 11);

    // Texture for the freeze effect (from the BASIC_TILES spritesheet).
    public static final TextureRegion FREEZE = SpriteSheet.BASIC_TILES.at(9,6);

    // Texture for frozen enemies (from the ORIGINAL_BOMBERMAN spritesheet).
    public static final TextureRegion FROZEN_ENEMIES = SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 7);
}
