package de.tum.cit.ase.bomberquest.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
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
 * The VictoryScreen class represents the screen displayed when a player wins a game.
 * It provides functionality to display high scores, navigate back to the menu,
 * and reset or manage high scores.
 *
 * Source:
 * - Preferences for HighScore: https://libgdx.com/wiki/preferences
 */
public class VictoryScreen extends BaseScreen {

    private final Stage stage; // Stage for managing and displaying UI elements
    private float highScore; // Stores the current high score for the map
    private float remainingTime; // Remaining time at the end of the game
    private Label highScoreLabel; // Label to display the high score

    /**
     * Constructor for the VictoryScreen. Sets up the UI components for displaying
     * victory details, high scores, and navigation options.
     *
     * @param game The main game instance.
     */
    public VictoryScreen(BomberQuestGame game) {
        super(game);

        // Adjusting zoom for victory screen
        camera.zoom = 1.5f;

        // Initialize the stage for managing UI components
        stage = new Stage(viewport, game.getSpriteBatch());

        // Create a table layout for organizing UI elements
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a "Victory!" title label
        table.add(new Label("Victory!", game.getSkin(), "title")).padBottom(80).row();

        // Add a "Return to Menu" button
        TextButton returnToMenuButton = new TextButton("Return to Menu", game.getSkin());
        table.add(returnToMenuButton).padBottom(100).width(300).row();
        returnToMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundEffect.VICTORY.stop(); // Stops the sound effect if it is still playing
                game.setPaused(false); // Reset the pause state
                game.setGameWon(false); // Reset the isGameWon state
                game.goToMenu();
            }
        });


        // Create a label for displaying the high score
        highScoreLabel = new Label("", game.getSkin(), "title");
        highScoreLabel.setColor(Color.GOLD); // Set the color to gold
        table.add(highScoreLabel).padBottom(80).row(); // Add the label to the table with padding for spacing

        // Load and retrieve the saved highscore
        String mapName = game.getLastLoadedMap(); // Retrieve the name of the last loaded map
        Preferences preferences = Gdx.app.getPreferences("HighScorePrefs"); // Access the preferences storage
        highScore = preferences.getFloat(mapName + "_highScore", 0f); // Get the stored high score for the map (default to 0 if not found)


        // Get remaining time
        remainingTime = game.getGameTimer().getRemainingTime();

        // Define the maximum time of the game and reset the highscore of the specified map if necessary
        float newMaxTime = 240f; // Set the maximum time for the level
        resetHighScoresIfNeeded(newMaxTime); // Check and reset the high scores if the time exceeds the maximum

        // Compare the current remaining time with the stored high score
        if (remainingTime > highScore) {
            highScore = remainingTime;  // Update the highscore if the remaining time is greater than the existing record
            preferences.putFloat(mapName + "_highScore", highScore); // Save the updated high score for the specified map
            preferences.flush(); // Commit the changes to permanently store the new high score

            float roundedHighScore = Math.round(highScore * 10) / 10.0f;  // Round the new highscore to one decimal place

            SoundEffect.HIGHSCORE.play();// Play a sound effect to signify a new highscore

            highScoreLabel.setText("New Highscore " + mapName + ":\n\t" + roundedHighScore + " seconds");  // Display the new highscore
        } else { // If the remaining time is not higher, just show the current highscore
            float roundedHighScore = Math.round(highScore * 10) / 10.0f;
            highScoreLabel.setText("Current Highscore " + mapName + ":\n\t" + roundedHighScore + " seconds");
        }
    }

    /**
     * Renders the screen by updating and drawing the stage.
     *
     * @param delta The time elapsed since the last frame.
     */
    @Override
    public void render(float delta) {
        super.render(delta); // Clears the screen and applies shared logic
        stage.act(delta);
        stage.draw();
    }

    /**
     * Called when the screen is shown. Plays victory music and sets up input handling.
     */
    @Override
    public void show() {
        super.show();
        game.playMusicTrack(MusicTrack.VICTORY); // Start playing the VICTORY music track
        Gdx.input.setInputProcessor(stage); // Set the stage as the input processor
    }

    /**
     * Resets all high scores by clearing every entry in the "HighScorePrefs".
     * This removes all saved high score data.
     */
    public static void resetHighScores() {
        Preferences preferences = Gdx.app.getPreferences("HighScorePrefs");  // Obtain the preferences that store high scores
        preferences.clear(); // Clear all key-value pairs from the preferences
        preferences.flush();  // Persist the changes so they are saved
    }

    /**
     * Checks each stored high score and removes it if it exceeds the specified maximum time.
     *
     * @param newMaxTime  The maximum allowed time for a valid high score
     */
    public void resetHighScoresIfNeeded(float newMaxTime) {

        Preferences preferences = Gdx.app.getPreferences("HighScorePrefs"); // Retrieve the existing preferences for high scores

        // Iterate over all stored keys in the preferences
        for (String key : preferences.get().keySet()) {
            if (key.endsWith("_highScore")) {  // Only consider keys that end with "_highScore"
                float highScore = preferences.getFloat(key, 0f); // Get the high score value for this key; default to 0 if not found
                if (highScore > newMaxTime) { // If the stored high score is greater than newMaxTime, remove it
                    preferences.remove(key);
                }
            }
        }
        preferences.flush();  // Persist any changes made to the preferences
    }

    /**
     * Disposes resources used by the screen to free memory.
     */
    @Override
    public void dispose() {
        stage.dispose();
    }
}