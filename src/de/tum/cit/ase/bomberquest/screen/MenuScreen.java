package de.tum.cit.ase.bomberquest.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import de.tum.cit.ase.bomberquest.BomberQuestGame;
import de.tum.cit.ase.bomberquest.audio.MusicTrack;


/**
 * The MenuScreen class is responsible for displaying the main menu of the game.
 * It extends the BaseScreen class and sets up the UI components for the menu.
 */
public class MenuScreen extends BaseScreen {

    private final Stage stage; //Stage for rendering UI components
    private final SpriteBatch batch; //SpriteBatch for rendering graphics
    private final Texture backgroundPictureTexture; // Background image texture for the menu

    /**
     * Constructor for MenuScreen. Sets up the menu UI.
     *
     * @param game     The main game instance (BomberQuestGame).
     * @param isPaused Indicates if the screen is accessed while the game is paused.
     */
    public MenuScreen(BomberQuestGame game, boolean isPaused) {
        super(game);

        // Adjust the camera zoom for the menu screen and update the viewport to match the screen dimensions
        camera.zoom = 1.5f; // Revert zoom to its smaller original value
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialize the stage and the SpriteBatch for rendering UI elements
        stage = new Stage(viewport, game.getSpriteBatch());
        batch = game.getSpriteBatch();

        // Load the background image
        backgroundPictureTexture = new Texture(Gdx.files.internal("images/BackgroundMenu.jpeg"));

        // Create a table to layout the UI components
        Table table = new Table();
        table.setFillParent(true); // Make the table fill the stage
        table.padTop(Gdx.graphics.getHeight() * 0.4f); // Add padding to move buttons lower

        // Add a "Go To Game" button if the game is not paused
        if (!isPaused) {
            TextButton goToGameButton = new TextButton("Go to Game", game.getSkin());
            table.add(goToGameButton).width(350).padBottom(20).row();
            goToGameButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    FileHandle defaultMap = Gdx.files.internal("maps/map-1.properties");  // Set the default map as map 1
                    game.goToDefaultGame("maps/map-1.properties");
                }
            });
        }

        // Add a "Continue Game" button if the game is paused
        if (isPaused) {
            TextButton continueButton = new TextButton("Continue Game", game.getSkin());
            table.add(continueButton).width(350).padBottom(20).row();
            continueButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToGame();
                }
            });
        }


        // Add a "Load New Map" button for the main menu
        if(!isPaused){
            TextButton loadMapButton = new TextButton("Load New Map", game.getSkin());
            table.add(loadMapButton).width(350).padBottom(20).row();
            loadMapButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setMultiplayer(false);
                    Gdx.app.postRunnable(game::selectPropertiesFileAndStartGame);
                }
            });
        }

        // Add a "Multiplayer" button for the main menu
        if(!isPaused){
            TextButton multiplayerButton = new TextButton("Multiplayer", game.getSkin());
            table.add(multiplayerButton).width(350).padBottom(20).row();
            multiplayerButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setMultiplayer(true);
                    Gdx.app.postRunnable(game::selectPropertiesFileAndStartGame);
                }
            });
        }

        // Add a "Main Menu" button for the pause menu
        if (isPaused) {
            TextButton mainMenuButton = new TextButton("Main Menu", game.getSkin());
            table.add(mainMenuButton).width(350).padBottom(20).row();
            mainMenuButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new MenuScreen(game, false)); // Transition to the Main Menu screen
                }
            });
        }

        // Main Menu: Add a "Difficulty" button to adjust the game difficulty
        if(!isPaused){
            // Create a button to change the difficulty level
            TextButton difficultyButton = new TextButton("Difficulty: Easy", game.getSkin());
            table.add(difficultyButton).width(350).padBottom(20).row();

            // Adjust the initial state based on the current difficulty
            switch (game.getCurrentDifficulty()) {
                case EASY -> {
                    difficultyButton.setText("Difficulty: Easy");
                    difficultyButton.setColor(Color.GREEN);
                }
                case MEDIUM -> {
                    difficultyButton.setText("Difficulty: Medium");
                    difficultyButton.setColor(Color.YELLOW);
                }
                case HARD -> {
                    difficultyButton.setText("Difficulty: Hard");
                    difficultyButton.setColor(Color.RED);
                }
            }

            // Add a listener to handle difficulty level changes and update the button text and color accordingly
            difficultyButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {

                    // Check the current difficulty and cycle through EASY -> MEDIUM -> HARD -> EASY
                    switch (game.getCurrentDifficulty()) {
                        case EASY -> {  // Change the difficulty to MEDIUM, set the button color to yellow, and update the text
                            game.setCurrentDifficulty(BomberQuestGame.Difficulty.MEDIUM);
                            difficultyButton.setColor(Color.YELLOW);
                            difficultyButton.setText("Difficulty: Medium");
                        }
                        case MEDIUM -> {  // Change the difficulty to HARD, set the button color to red, and update the text
                            game.setCurrentDifficulty(BomberQuestGame.Difficulty.HARD);
                            difficultyButton.setColor(Color.RED);
                            difficultyButton.setText("Difficulty: Hard");
                        }
                        case HARD -> {  // Change the difficulty back to EASY, set the button color to green, and update the text
                            game.setCurrentDifficulty(BomberQuestGame.Difficulty.EASY);
                            difficultyButton.setColor(Color.GREEN);
                            difficultyButton.setText("Difficulty: Easy");
                        }
                    }
                    if (game.getMap() != null) {
                        game.getMap().updateDifficulty();
                    }
                }
            });
        }

        // Add a "Mute/Unmute" button
        TextButton muteButton = new TextButton(game.isMuted() ? "Unmute" : "Mute", game.getSkin());
        table.add(muteButton).width(350).padBottom(20).row();
        muteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Toggle the mute state
                boolean newMuteState = !game.isMuted();
                game.setMuted(newMuteState);
                // Update the button text based on the new state
                muteButton.setText(newMuteState ? "Unmute" : "Mute");
            }
        });

        // Add a "Reset Highscores" button to clear game scores
        if(!isPaused){
            TextButton resetHighscores = new TextButton("Reset Highscores", game.getSkin());
            table.add(resetHighscores).width(350).padBottom(20).row();
            resetHighscores.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    VictoryScreen.resetHighScores();  // Call the method to reset high scores
                    resetHighscores.setColor(Color.GRAY);  // Change the button color to indicate that scores have been reset
                }
            });
        }

        // Add an "Exit Game" button to close the application
        TextButton exitButton = new TextButton("Exit Game", game.getSkin());
        table.add(exitButton).width(350).row();
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit(); // Exit the game
            }
        });

        stage.addActor(table);
    }


    /**
     * The render method is called every frame to render the menu screen.
     * It clears the screen, draws the background image, and renders the stage.
     *
     * @param deltaTime The time in seconds since the last render.
     */
    @Override
    public void render(float deltaTime) {

        // Cap frame time to a maximum of 250ms to avoid performance degradation (spiral of death)
        float frameTime = Math.min(deltaTime, 0.250f);
        ScreenUtils.clear(Color.BLACK); // Clear the screen with a black color

        // Begin the batch to draw graphics
        batch.begin();

        // Adjusting the size of the background image
        float backgroundWidth = Gdx.graphics.getWidth() * 1.5f;  // Increase width by 50%
        float backgroundHeight = Gdx.graphics.getHeight() * 1.5f; // Increase height by 50%

        //Centering the Background Image on the screen
        float xCenter = (Gdx.graphics.getWidth() - backgroundWidth) / 2; // width --> centering horizontally
        float ycenter = (Gdx.graphics.getHeight() - backgroundHeight) / 2; // height --> centering vertically
        // Draw the background image at the calculated position and size
        batch.draw(backgroundPictureTexture, xCenter, ycenter, backgroundWidth, backgroundHeight);

        batch.end();

        // Update the stage with the elapsed time and draw the stage
        stage.act(deltaTime);
        stage.draw();
    }


    /**
     * The resize method is called whenever the screen size is changed.
     * It updates the viewport and adjusts the stage accordingly.
     *
     * @param width  The new width of the screen.
     * @param height The new height of the screen.
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height); // Update the viewport via BaseScreen
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    /**
     * Dispose method is called to clean up resources when the screen is no longer used.
     * It releases resources to avoid memory leaks.
     */
    @Override
    public void dispose() {
        super.dispose(); // Dispose resources in BaseScreen
        stage.dispose();
        backgroundPictureTexture.dispose();

    }

    /**
     * The show method is called when the MenuScreen becomes visible.
     * It starts playing the main menu music and sets the stage as the input processor.
     */
    @Override
    public void show() {
        game.playMusicTrack(MusicTrack.MAINMENU); // Start playing the MAINMENU music track
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
    }

    /** The following methods are part of the Screen interface but are not used in this screen. */
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }
}
