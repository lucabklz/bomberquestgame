package de.tum.cit.ase.bomberquest.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.ase.bomberquest.BomberQuestGame;

/**
 * BaseScreen serves as a superclass for all screens in the game.
 * Provides common functionality and resources, such as camera and viewport management.
 * Other screens can extend this class to inherit and reuse its functionality.
 */
public abstract class BaseScreen implements Screen {

    protected final BomberQuestGame game; // Reference to the game instance
    protected  OrthographicCamera camera; // Camera for rendering
    protected  Viewport viewport; // Viewport to handle resizing

    /**
     * Constructor for initializing the base screen with a game instance.
     * @param game The main BomberQuestGame instance.
     */
    public BaseScreen(BomberQuestGame game) {
        this.game = game;

        // Camera and viewport initialization
        this.camera = new OrthographicCamera();
        this.viewport = new ScreenViewport(camera);
    }

    /**
     * Called every frame to render the screen.
     * Clears the screen with a default black background.
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        // Clear the screen with a default color
        ScreenUtils.clear(Color.BLACK);
    }

    /**
     * Called when the screen size changes.
     * Updates the viewport to fit the new width and height.
     * @param width  The new screen width.
     * @param height The new screen height.
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    /**
     * Called when the game is paused.
     * This can happen when the user switches to another application or minimizes the game.
     */
    @Override
    public void pause() {}

    /**
     * Called when the game is resumed after being paused.
     */
    @Override
    public void resume() {}

    /**
     * Called when the screen becomes visible.
     * Subclasses can override this to define specific logic when the screen is shown.
     */
    @Override
    public void show() {}

    /**
     * Called when the screen is no longer visible.
     * Subclasses can override this to define specific logic when the screen is hidden.
     */
    @Override
    public void hide() {}

    /**
     * Disposes resources used by the screen.
     * Subclasses should override this to clean up resources specific to the screen.
     */
    @Override
    public void dispose() {}
}