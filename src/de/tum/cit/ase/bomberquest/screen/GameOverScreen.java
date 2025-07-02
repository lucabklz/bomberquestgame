package de.tum.cit.ase.bomberquest.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import de.tum.cit.ase.bomberquest.BomberQuestGame;
import de.tum.cit.ase.bomberquest.audio.MusicTrack;
import de.tum.cit.ase.bomberquest.audio.SoundEffect;

/**
 * Represents the "Game Over" screen displayed when the player loses the game.
 * Provides options to retry the map, load a new game, or return to the main menu.
 */
public class GameOverScreen extends BaseScreen {

    private final Stage stage; // The stage for managing UI elements in the Game Over screen

    /**
     * Constructor for the Game Over screen.
     * Initializes the stage, camera, and UI elements.
     * @param game The main game instance.
     */
    public GameOverScreen(BomberQuestGame game) {
        super(game);

        // Customize zoom for game over screen
        camera.zoom = 1.5f;

        // Initialize stage
        this.stage = new Stage(viewport, game.getSpriteBatch());

        // Create UI layout
        Table table = new Table();
        table.setFillParent(true);
        this.stage.addActor(table);

        // Add a "Game Over" title to the screen
        table.add(new Label("Game Over", game.getSkin(), "title")).padBottom(80).row();

        // Retry button to replay the current map
        TextButton retryButton = new TextButton("Retry", game.getSkin());
        table.add(retryButton).width(300).padBottom(20).row();
        retryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundEffect.GAMEOVER.stop(); // Stops the sound effect if it is still playing
                game.retryCurrentMap();
            }
        });

        // Load New Game Button
        TextButton loadNewGameButton = new TextButton("Load New Game", game.getSkin());
        table.add(loadNewGameButton).width(300).padBottom(20).row();
        loadNewGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundEffect.GAMEOVER.stop(); // Stops the sound effect if it is still playing
                game.selectPropertiesFileAndStartGame(); // Open file chooser for new map
                game.getHud().updateElements(); // Update all hud elements
            }
        });

        // Return to Menu Button
        TextButton returnToMenuButton = new TextButton("Main Menu", game.getSkin());
        table.add(returnToMenuButton).width(300).row();
        returnToMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundEffect.GAMEOVER.stop(); // Stops the sound effect if it is still playing
                game.setPaused(false); // Mark the game as not paused
                game.getMap().setGameOver(false); // Reset the game over state
                game.goToMenu(); // Return to the main menu
            }
        });
    }

    /**
     * Renders the Game Over screen.
     * Clears the screen, processes UI elements, and draws the stage.
     * @param delta The time elapsed since the last frame (in seconds).
     */
    @Override
    public void render(float delta) {
        super.render(delta); // Clears the screen and applies shared logic
        stage.act(delta);
        stage.draw();
    }

    /**
     * Called when the Game Over screen is shown.
     * Plays the Game Over music track and sets up input processing for the stage.
     */
    @Override
    public void show() {
        super.show();
        game.playMusicTrack(MusicTrack.GAMEOVER); // Start playing the GAMEOVER music track
        Gdx.input.setInputProcessor(stage); // Set the stage as the input processor
    }

    /**
     * Handles screen resizing by updating the viewport.
     * @param width  The new width of the screen.
     * @param height The new height of the screen.
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes of resources used by the Game Over screen.
     * Frees up memory to avoid memory leaks.
     */
    @Override
    public void dispose() {
        super.dispose();
        stage.dispose();
    }
}