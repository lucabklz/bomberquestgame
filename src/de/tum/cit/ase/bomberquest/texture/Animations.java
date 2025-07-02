package de.tum.cit.ase.bomberquest.texture;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * The Animations class contains all animation constants used in the game.
 * It centralizes and organizes the animations for characters, enemies, bombs, and effects.
 * Using constants avoids redundant loading of animations and improves code maintainability.
 */
public class Animations {
    
    /**
     * The animations for the player.
     */

    // Walking animations for the player character in four directions
    public static final Animation<TextureRegion> CHARACTER_WALK_DOWN = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(1, 4),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(1, 5),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(1, 6)
    );

    public static final Animation<TextureRegion> CHARACTER_WALK_UP = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(2, 4),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(2, 5),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(2, 6)
    );

    public static final Animation<TextureRegion> CHARACTER_WALK_LEFT = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(1, 1),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(1, 2),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(1, 3)
    );

    public static final Animation<TextureRegion> CHARACTER_WALK_RIGHT = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(2, 1),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(2, 2),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(2, 3)
    );

    // Death animation for the player character
    public static final Animation<TextureRegion> CHARACTER_DEATH = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(3, 1),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(3, 2),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(3, 3),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(3, 4),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(3, 5),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(3, 6),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(3, 7)
    );


    /** Animations of the enemies */

    // Walking animations for enemies in all directions
    public static final Animation<TextureRegion> ENEMY_WALK_DOWN = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 1),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 2),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 3)
    );

    public static final Animation<TextureRegion> ENEMY_WALK_LEFT = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 4),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 5),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 6)
    );

    public static final Animation<TextureRegion> ENEMY_WALK_RIGHT = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 1),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 2),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 3)
    );

    public static final Animation<TextureRegion> ENEMY_WALK_UP = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 4),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 5),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 6)
    );

    // Death animation for enemies
    public static final Animation<TextureRegion> ENEMY_DEATH = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 7),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 8),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 9),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 10),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(16, 11)
            );


    /** Animation of the bomb */

    // Bomb placement animation
    public static final Animation<TextureRegion> BOMB_PLACEMENT = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(4, 1),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(4, 2),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(4, 3)
    );

    // Explosion (blast) center animation
    public static final Animation<TextureRegion> BLAST_CENTER = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(7, 3),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(7, 8),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(12, 3),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(12, 8)

    );

    // Explosion animations for all directions
    public static final Animation<TextureRegion> BLAST_UP = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(5, 3),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(5, 8),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(10, 3),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(10, 8)
    );

    public static final Animation<TextureRegion> BLAST_DOWN = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(9, 3),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(9, 8),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(14, 3),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(14, 8)
    );

    public static final Animation<TextureRegion> BLAST_LEFT = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(7, 1),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(7, 6),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(12,1),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(12,6)
    );

    public static final Animation<TextureRegion> BLAST_RIGHT = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(7, 5),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(7, 10),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(12, 5),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(12, 10)
    );

    // Extended blast radius animations for vertical and horizontal power-ups
    public static final Animation<TextureRegion> BLAST_VERTICAL_EXTENSION = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(8, 3),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(8, 8),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(13, 3),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(13, 8)
    );

    public static final Animation<TextureRegion> BLAST_HORIZONTAL_EXTENSION = new Animation<>(0.1f,
            SpriteSheet.ORIGINAL_BOMBERMAN.at(7, 4),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(7, 9),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(12, 4),
            SpriteSheet.ORIGINAL_BOMBERMAN.at(12, 9)
    );

    /** Animations for other game elements */

    // Animation for the exit door opening
    public static final Animation<TextureRegion> EXIT_OPEN = new Animation<>(0.2f,
            SpriteSheet.THINGS.at(1, 1),
            SpriteSheet.THINGS.at(2, 1),
            SpriteSheet.THINGS.at(3, 1),
            SpriteSheet.THINGS.at(4, 1)
    );




}
