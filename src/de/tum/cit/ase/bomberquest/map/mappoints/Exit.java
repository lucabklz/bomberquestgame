package de.tum.cit.ase.bomberquest.map.mappoints;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.ase.bomberquest.audio.MusicTrack;
import de.tum.cit.ase.bomberquest.audio.SoundEffect;
import de.tum.cit.ase.bomberquest.texture.Drawable;
import de.tum.cit.ase.bomberquest.texture.Textures;
import de.tum.cit.ase.bomberquest.texture.Animations;
import de.tum.cit.ase.bomberquest.BomberQuestGame;

/**
 * Represents the exit point in the game.
 * The exit can be locked or unlocked and is associated with the victory condition.
 * Includes an animation sequence when the exit is unlocked and the game is won.
 */
public class Exit extends MapPoints implements Drawable {

    private float animationTime = 0f; // Tracks progress of the exit opening animation
    private final BomberQuestGame game; // Reference to the main game instance
    private boolean animationPlaying = false; // Indicates if the opening animation is currently playing
    private boolean locked = true; // Indicates whether the exit is locked
    private boolean isGameWon = false; // Tracks whether the game has been won

    /**
     * Constructs an Exit object at the specified position.
     *
     * @param x    The x-coordinate of the exit on the map.
     * @param y    The y-coordinate of the exit on the map.
     * @param game Reference to the main game instance.
     */
    public Exit(float x, float y, BomberQuestGame game) {
        super(x, y);
        this.game = game;
    }

    /**
     * Handles interaction logic when the player interacts with the exit.
     * If the exit is unlocked and the game has not been won yet, it triggers the victory sequence.
     */
    @Override
    public void interact() {
        // Display the victory logic only once (otherwise, the sound will be played several times)
        if (isGameWon){
            return;
        }

        isGameWon = true; // Mark the game as won

        if (!locked && !animationPlaying) {
            animationPlaying = true;
            animationTime = 0f; // Reset animation time
        }

        // Stop the current game music track and play the VICTORY sound effect
        MusicTrack.GAME.stop();
        SoundEffect.VICTORY.play();
    }

    /**
     * Renders the exit on the screen.
     * Displays either the locked or unlocked state, or plays the opening animation if triggered.
     *
     * @param spriteBatch  Used for rendering textures.
     * @param shapeRenderer Used for rendering shapes.
     * @param deltaTime    The time elapsed since the last frame.
     */
    public void render(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer, float deltaTime) {
        float x = getX() * 16 * 4; // TILE_SIZE_PX * SCALE for proper placement
        float y = getY() * 16 * 4;
        float width = 16 * 4; // Scaled width of the exit texture
        float height = 16 * 4; // Scaled height of the exit texture

        if (animationPlaying) {
            animationTime += deltaTime; // Update animation time

            // Render black background during animation
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();

            // Render the animation frame
            TextureRegion currentFrame = Animations.EXIT_OPEN.getKeyFrame(animationTime, false);
            spriteBatch.begin();
            spriteBatch.draw(currentFrame, x, y, width, height);
            spriteBatch.end();

            // Check if animation is finished
            if (Animations.EXIT_OPEN.isAnimationFinished(animationTime)) {
                game.goToVictory();
                game.setGameActive(false); // Reset the active game state
                game.setGameWon(true); // Mark the game as won
                animationPlaying = false;
            }
        } else {
            // Render the static texture (locked or unlocked)
            TextureRegion texture = locked ? Textures.EXIT_LOCKED : Textures.EXIT;
            spriteBatch.begin();
            spriteBatch.draw(texture, x, y, width, height);
            spriteBatch.end();
        }
    }

    /**
     * Retrieves the current appearance of the exit.
     * If the animation is playing, it returns the current frame; otherwise, it returns the static locked or unlocked texture.
     *
     * @return The TextureRegion representing the exit's appearance.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        if (animationPlaying) {
            // Return the current frame of the animation
            return Animations.EXIT_OPEN.getKeyFrame(animationTime, false);
        }
        // Return locked or unlocked texture
        return locked ? Textures.EXIT_LOCKED : Textures.EXIT;
    }

    /**
     * Unlocks the exit, allowing the player to trigger the victory sequence.
     */
    public void unlock() {
        this.locked = false;
    }

    /**
     * Checks whether the exit is currently locked.
     *
     * @return True if the exit is locked, false otherwise.
     */
    public boolean isLocked() {
        return locked;
    }


}