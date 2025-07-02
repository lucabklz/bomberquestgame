package de.tum.cit.ase.bomberquest;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.ase.bomberquest.audio.MusicTrack;
import de.tum.cit.ase.bomberquest.audio.SoundEffect;
import de.tum.cit.ase.bomberquest.map.GameMap;
import de.tum.cit.ase.bomberquest.map.GameTimer;
import de.tum.cit.ase.bomberquest.map.characters.Player;
import de.tum.cit.ase.bomberquest.screen.*;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The BomberQuestGame class represents the core of the Bomber Quest game.
 * It manages the game's lifecycle, screens, and shared resources such as the sprite batch and UI skin.
 */
public class BomberQuestGame extends Game {

    private SpriteBatch spriteBatch; //SpriteBatch used for rendering game elements across all screens.
    private Skin skin; //The game's UI skin. This is used to style the game's UI elements.
    private final NativeFileChooser fileChooser; //The file chooser for loading map files from the user's computer.
    private GameMap map; // The map. This is where all the game objects are stored.


    private boolean isPaused = false; // Indicates paused-state.
    private FileHandle lastLoadedMap; // Store the last loaded map file.
    private MusicTrack currentMusicTrack; //Attribute for the current music track which is played.
    private Hud currentHud; //Attribute for saving the current hud.
    private GameTimer gameTimer; //Attribute Timer to track game duration and manage gameplay mechanics.
    private boolean isMultiplayer = false; //Indicates if the current game session is a multiplayer game.
    private boolean isGameActive = false; //Indicates if a game is currently active.
    private boolean isGameWon = false; //Indicates if the current game has been won.
    private boolean isMuted = false; //Indicates if the game is muted.

    /**
     * An enumeration of possible difficulty levels: EASY, MEDIUM, and HARD.
     */
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    private Difficulty currentDifficulty = Difficulty.EASY; //Stores the current difficulty for the game. Defaults to EASY unless something else is specified.


    /**
     * Constructor for BomberQuestGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public BomberQuestGame(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    /**
     * Called when the game is created.
     * Initializes core resources such as the sprite batch and UI skin,
     * and navigates to the main menu screen.
     */
    @Override
    public void create() {
        this.spriteBatch = new SpriteBatch(); // Create SpriteBatch for rendering
        this.skin = new Skin(Gdx.files.internal("skin/craftacular/craftacular-ui.json")); // Load UI skin
        gameTimer = new GameTimer(240); // Initialize with 4 minutes
        goToMenu(); // Navigate to the menu screen (main menu)
    }

    /**
     * Plays a specified music track.
     * Stops the currently playing track (if any) before starting the new one.
     *
     * @param musicTrack The music track to play.
     */
    public void playMusicTrack(MusicTrack musicTrack) {
        // Checks if a music track is currently played
        if (currentMusicTrack != null) {
            currentMusicTrack.stop(); // Stop the currently playing track.
        }
        currentMusicTrack = musicTrack; // Set the new music track.
        currentMusicTrack.play(); // Start playing the new track.
    }

    /**
     * Navigates to the main menu screen.
     * If the game is paused, the pause menu state is retained.
     */
    public void goToMenu() {
        playMusicTrack(MusicTrack.MAINMENU); // Start playing the MAINMENU music track
        setScreen(new MenuScreen(this, isPaused())); // Pass the isPaused state to the MenuScreen
    }


    /**
     * Starts or resumes the game.
     * Handles both new game sessions and continuing from a paused state.
     */
    public void goToGame() {
        playMusicTrack(MusicTrack.GAME); // Start playing the GAME music track
        if (isPaused) { // If the game is paused, the current hud can still be used when continuing the game
            setScreen(new GameScreen(this, currentHud));
            gameTimer.resume();
        } else { // Start a new game and initialize a new hud
            GameScreen gameScreen = new GameScreen(this, null);
            currentHud = gameScreen.getHud(); // Save the new hud as the current hud
            gameTimer.reset(240);
            setScreen(gameScreen); // Set the current screen to gameScreen
            isGameActive = true; // Set the active game state to true
        }
        isPaused = false; // Reset pause state
    }



    /**
     * Opens a file chooser dialog to select a `.properties` file for loading a new game map.
     * Once a valid file is selected, the map is loaded, and the game transitions to the game screen.
     * If an error occurs or the dialog is canceled, the user is returned to the appropriate menu screen.
     *
     * Functionality includes:
     * - Ensuring that only valid `.properties` files can be selected.
     * - Creating and loading a new game map from the selected properties file.
     * - Resetting the game timer and HUD for the new game session.
     * - Handling file chooser errors or cancellations gracefully.
     *
     * Sources:
     * - JFileChooser: https://stackoverflow.com/questions/19479877/jfilechooser-in-libgdx/44958709#44958709
     * - Properties: https://www-baeldung-com.translate.goog/java-properties?_x_tr_sl=en&_x_tr_tl=de&_x_tr_hl=de&_x_tr_pto=sc
     */
    public void selectPropertiesFileAndStartGame() {

        // Set up the file chooser dialog configuration
        NativeFileChooserConfiguration config = new NativeFileChooserConfiguration();
        config.title = "Choose a Properties File"; // Set dialog title
        config.directory = Gdx.files.external(""); // Set the directory to external storage

        // Open the file chooser with the specified configuration
        fileChooser.chooseFile(config, new NativeFileChooserCallback() {

            @Override
            public void onFileChosen(FileHandle fileHandle) {
                // Ensure only properties files are selected
                if (fileHandle.extension().equalsIgnoreCase("properties")) {
                    try {
                        // Load the properties file into a Properties object
                        Properties properties = new Properties();
                        properties.load(new FileInputStream(fileHandle.file()));

                        // Store the selected file as the last loaded map for retry functionality
                        lastLoadedMap = fileHandle;

                        // Create a new map using the properties file
                        GameMap newMap = new GameMap(BomberQuestGame.this, properties);

                        // Dispose of the current map and screen before loading the new one
                        if (map != null) {
                            map.dispose();
                        }
                        if (getScreen() != null) {
                            getScreen().dispose();
                        }

                        // Assign the new map and set up the game screen
                        map = newMap;

                        // Reset the game timer to its initial value (240 seconds)
                        gameTimer.reset(240);

                        // Initialize or recreate the HUD for the new game session
                        currentHud = new Hud(BomberQuestGame.this, getSpriteBatch(), getSkin().getFont("font"),
                                map.getPlayer(), map.getPlayer2(), map, gameTimer);

                        // Update the HUD elements to reflect the new game state
                        currentHud.updateElements();

                        // Transition to the game screen
                        goToGame();

                    } catch (IOException e) {
                        // Show an error dialog if the properties file could not be loaded
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                                "Error while loading properties file: " + e.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE));
                    }
                } else {
                    // Show a warning dialog if the selected file is not a valid .properties file
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                            "Invalid file selected!\nPlease select a valid .properties file.", "Invalid File",
                            JOptionPane.WARNING_MESSAGE));
                }
            }


            // If the user cancels the selection of a file
            @Override
            public void onCancellation() {
                // Log that the file chooser was canceled
                Gdx.app.log("BomberQuestGame", "File chooser cancelled");
                setMultiplayer(false);
                setPaused(false);
                // return to the menu
                goToMenu();

            }

            // If an error occurs
            @Override
            public void onError(Exception e) {
                // Log an error if there was an issue with the file chooser
                Gdx.app.error("BomberQuestGame", "Error in file chooser: " + e.getMessage(), e);
                setMultiplayer(false);
                setPaused(false);
                // Return to the menu if an error occurs
                goToMenu();
            }
        });
    }


    /**
     * Retry the same map after game over.
     */
    public void retryCurrentMap() {
        if (lastLoadedMap != null) {
            try {
                // Reload the last map
                Properties properties = new Properties();
                properties.load(new FileInputStream(lastLoadedMap.file()));

                // Dispose the current map and screen
                if (map != null) {
                    map.dispose();
                }
                if (getScreen() != null) {
                    getScreen().dispose();
                }

                // Create a new map instance
                map = new GameMap(this, properties);

                // Update the HUD
                if (currentHud != null) {
                    currentHud.updateElements();
                }

                // Transition to the game screen
                goToGame();
            } catch (IOException e) {
                System.err.println("Error retrying current map: " + e.getMessage());
            }
        } else {
            System.out.println("No map to retry. Returning to menu.");
            goToMenu();
        }
    }

    /**
     * Start the game with a default map.
     *
     * @param defaultMapPath The path to the default map file.
     */
    public void goToDefaultGame(String defaultMapPath) {
        FileHandle defaultMap = Gdx.files.internal(defaultMapPath);
        if (defaultMap.exists()) {
            try {
                // Set the default map as the last loaded map
                lastLoadedMap = defaultMap;

                // Load the default map
                Properties properties = new Properties();
                properties.load(new FileInputStream(defaultMap.file()));

                // Dispose the current map and screen
                if (map != null) {
                    map.dispose();
                }
                if (getScreen() != null) {
                    getScreen().dispose();
                }

                // Create a new map instance
                map = new GameMap(this, properties);

                // Update the HUD
                if (currentHud != null) {
                    currentHud.updateElements();
                }

                // Transition to the game screen
                goToGame();
            } catch (IOException e) {
                System.err.println("Error loading default map: " + e.getMessage());
            }
        } else {
            System.out.println("Default map not found: " + defaultMapPath);
        }
    }


    /**
     * Handles the pause logic for the game state.
     * This is triggered when the window is minimized or paused.
     */
    @Override
    public void pause() {
        super.pause();
        // If the map is not initialized, do nothing
        if (map == null){
            return;
        }
        // If the game is currently active, set the state to paused
        if (isGameActive) {
            setPaused(true);
        }
        // If the game is already over, no further action is needed
        if (map.isGameOver()) {
            return;
        }
        // If the game is already won, return without any additional steps
        if (isGameWon) {
            return;
        }
        // If none of the conditions above are met, navigate to the menu
        goToMenu();
    }

    /**
     * Navigates to the game over screen.
     */
    public void goToGameOver() {
        this.setScreen(new GameOverScreen(this)); // Transition to the game-over screen
    }


    /**
     * Transition to the victory screen.
     */
    public void goToVictory() {
        setScreen(new VictoryScreen(this)); // Replace `VictoryScreen` with your victory screen implementation
    }


    /**
     * Cleans up resources when the game is closed.
     */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose the skin
    }



    /**
     * Toggles the mute state of the game's audio output.
     * @param muted True to mute, false to unmute.
     */
    public void setMuted(boolean muted) {
        isMuted = muted;

        // Update the volume for all music tracks
        for (MusicTrack track : MusicTrack.values()) {
            track.getMusic().setVolume(muted ? 0 : track.getDefaultVolume());
        }
        // Adjust the global volume for sound effects
        SoundEffect.setDefaulVolume(muted ? 0 : 1.0f);
    }


    /**
     * Sets the pause state of the game timer and synchronizes the game timer.
     *
     * @param paused True to pause the game, false to resume it.
     */
    public void setPaused(boolean paused) {
        // Updates the pause state of the game
        this.isPaused = paused;

        // Checks if the game is paused
        if (paused) {
            // When the game is paused, pause the game timer to stop time tracking
            gameTimer.pause();
        } else {
            // When the game is resumed, resume the game timer to continue time tracking
            gameTimer.resume();
        }
    }


    /**
     * Retrieves the current HUD (Heads-Up Display) for the game.
     * If the HUD is not yet initialized and the map exists, a new HUD is created.
     *
     * @return The current HUD instance.
     */
    public Hud getHud() {
        // Initialize the hud if it is null and the map is available
        if (currentHud == null && map != null) {
            currentHud = new Hud(this, getSpriteBatch(), getSkin().getFont("font"), map.getPlayer(), map.getPlayer2(), map, gameTimer);
        }
        // Return current hud
        return currentHud;
    }

    /**
     * Retrieves the name of the last loaded map.
     * If no map has been loaded, it returns "Unknown map" as the default value.
     *
     * @return The name of the last loaded map without its file extension, or "Unknown map".
     */
    public String getLastLoadedMap() {
        // Returns the map name without its extension or "Unknown map" if none is loaded
        return lastLoadedMap != null ? lastLoadedMap.nameWithoutExtension() : "Unknown map";
    }

    /**
     * Checks if the game is currently paused.
     *
     * @return True if the game is paused, false otherwise.
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Checks if the game is currently muted.
     *
     * @return True if the game is muted, false otherwise.
     */
    public boolean isMuted() {
        return isMuted;
    }

    /**
     * Retrieves the current game timer.
     *
     * @return The GameTimer instance used in the game.
     */
    public GameTimer getGameTimer() {
        return gameTimer;
    }

    /**
     * Checks if the game is currently in multiplayer mode.
     *
     * @return True if the game is in multiplayer mode, false otherwise.
     */
    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    /**
     * Sets the game to multiplayer or single-player mode.
     *
     * @param multiplayer True to enable multiplayer mode, false for single-player mode.
     */
    public void setMultiplayer(boolean multiplayer) {
        this.isMultiplayer = multiplayer;
    }

    /**
     * Sets the active state of the game.
     * When set to true, the game is considered active.
     *
     * @param gameActive True to set the game as active, false otherwise.
     */
    public void setGameActive(boolean gameActive) {
        isGameActive = gameActive;
    }

    /**
     * Sets the game's win state.
     * When set to true, the game is considered won.
     *
     * @param gameWon True to set the game as won, false otherwise.
     */
    public void setGameWon(boolean gameWon) {
        isGameWon = gameWon;
    }

    /**
     * Retrieves the current difficulty level of the game.
     *
     * @return The current difficulty as an instance of the {@link Difficulty} enum.
     */
    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    /**
     * Sets the difficulty level of the game.
     *
     * @param currentDifficulty The new difficulty level to be set.
     */
    public void setCurrentDifficulty(Difficulty currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
    }

    /**
     * Retrieves the game's UI skin.
     *
     * @return The game's UI skin.
     */
    public Skin getSkin() {
        return skin;
    }

    /**
     * Retrieves the main SpriteBatch used for rendering.
     *
     * @return The SpriteBatch instance.
     */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    /**
     * Retrieves the current game map.
     *
     * @return The current game map, or null if no map is loaded.
     */
    public GameMap getMap() {
        return map;
    }

    /**
     * Retrieves the current player from the game map.
     *
     * @return The player object, or null if no map is loaded.
     */
    public Player getPlayer() {
        return map != null ? map.getPlayer() : null;
    }

    /**
     * Switches to a new screen, disposing of the previous one.
     *
     * @param screen The new screen to switch to.
     */
    @Override
    public void setScreen(Screen screen) {
        Screen previousScreen = super.screen;
        super.setScreen(screen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }


}
