package de.tum.cit.ase.bomberquest.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.ase.bomberquest.BomberQuestGame;
import de.tum.cit.ase.bomberquest.map.GameMap;
import de.tum.cit.ase.bomberquest.map.GameTimer;
import de.tum.cit.ase.bomberquest.map.characters.Player;
import de.tum.cit.ase.bomberquest.texture.Textures;

/**
 * A Heads-Up Display (HUD) that displays information on the screen.
 * It uses a separate camera so that it is always fixed on the screen.
 * The HUD shows details such as player stats, remaining enemies, power-up effects, and the game timer.
 *
 * Sources:
 * - SpriteBatch, TextureRegions, and Sprites: https://libgdx.com/wiki/graphics/2d/spritebatch-textureregions-and-sprites
 * - Rendering shapes: https://libgdx.com/wiki/graphics/opengl-utils/rendering-shapes
 * - Transparent ShapeRenderer: https://stackoverflow.com/questions/14700577/drawing-transparent-shaperenderer-in-libgdx
 */
public class Hud {

    private  BomberQuestGame game; // Reference to the main game instance
    private final SpriteBatch spriteBatch; // The SpriteBatch used to draw the HUD. This is the same as the one used in the GameScreen.
    private final BitmapFont font; //The font used to draw text on the screen
    private final OrthographicCamera camera; //The camera used to render the HUD
    private int blastRadiusP1; // Blast radius of player 1
    private int bombLimitP1; // Bomb limit of player 1
    private int blastRadiusP2 = 1; // Blast radius of player 2
    private int bombLimitP2 = 1; // Bomb limit of player 2
    private int remainingEnemies; // The number of enemies remaining on the map.
    private String remainingEnemiesString; //String used for easy rendering
    private boolean exitLocked; // Indicates whether the exit is locked.
    private float specialPowerUpTimer = 0; //Timer attribute for the invincible and freeze power-ups
    private boolean isTimerActive = false; //Boolean attribute to track if the timer for an invincible or a freeze power-up is active
    private Player player;
    private Player player2;
    private GameMap gameMap; //The game map which we need to access
    private final ShapeRenderer shapeRenderer; //Attribute which is needed to draw shapes like a rectangle. We can use this for the background of the HUD.
    private final GameTimer gameTimer; // Reference to the game timer for managing countdowns and time displays

    /** Colors which are used for the Hud */
    private static final Color RED = new Color(1, 0, 0, 1);
    private static final Color GREEN = new Color(0, 1, 0, 1);
    private static final Color WHITE = new Color(1,1,1,1);

    /**
     * Constructor for the Hud class, responsible for rendering the Heads-Up Display (HUD)
     * with player stats, game information, and other visual elements on the screen.
     *
     * @param game       The main game instance, used to access global settings and states.
     * @param spriteBatch The SpriteBatch for rendering HUD elements efficiently.
     * @param font       The BitmapFont used to draw text on the HUD.
     * @param player     The first player instance, whose stats will be displayed on the HUD.
     * @param player2    The second player instance, used in multiplayer mode (can be null for single-player).
     * @param gameMap    The current game map, providing access to enemies, exit state, and other map details.
     * @param gameTimer  The central game timer, used to display the remaining time.
     */
    public Hud(BomberQuestGame game, SpriteBatch spriteBatch, BitmapFont font, Player player, Player player2, GameMap gameMap, GameTimer gameTimer) {
        this.game = game;
        this.spriteBatch = spriteBatch;
        this.font = font;
        this.camera = new OrthographicCamera();
        this.shapeRenderer = new ShapeRenderer();
        this.player = player;
        this.player2 = player2;
        this.gameMap = gameMap;
        this.gameTimer = gameTimer;

        // Initialize Player 1 stats
        this.blastRadiusP1 = player.getBlastRadius(); // Current blast radius of the player
        this.bombLimitP1 = player.getConcurrentBombLimit(); // Current bomb limit of the player

        // Initialize Player 2 stats if multiplayer mode is active
        if (player2 != null) {
            this.blastRadiusP2 = player2.getBlastRadius();
            this.bombLimitP2 = player2.getConcurrentBombLimit();
        }

        // Set the initial number of enemies remaining
        this.remainingEnemies = gameMap.getEnemies().size();
        // Initialize the exit state as locked
        this.exitLocked = true;
    }


    /**
     * Renders the HUD on the screen.
     * Uses a separate OrthographicCamera to ensure the HUD remains fixed on the screen.
     * Dynamically adjusts font size and updates HUD elements before rendering.
     */
    public void render() {
        // All elements of the Hud must be updated constantly
        updateElements();

        // Retrieve the screen height and width
        float screenHeight = Gdx.graphics.getHeight();
        float screenWidth = Gdx.graphics.getWidth();

        // Save the original font size
        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;

        // Dynamically adjust the font size based on screen dimensions, ensuring a minimum size
        float baseFontScale = Math.min(screenWidth, screenHeight) / 1080.0f; // Base scaling factor
        float fontSize = Math.max(baseFontScale, 0.65f); // Ensure a minimum font size of 0.65
        font.getData().setScale(fontSize); // Apply the new font scale

        // Render the power-up HUD and main HUD
        renderPowerUpHud(screenWidth, screenHeight, fontSize);
        renderMainHud(screenWidth, screenHeight, fontSize);

        // Reset the font to its original scale
        font.getData().setScale(originalScaleX, originalScaleY);
    }

    /**
     * Renders the power-up heads-up display (HUD) in the top-right corner of the screen.
     * This includes icons and text for the Invincible and Freeze power-ups, as well as a
     * timer if one is active.
     *
     * @param screenWidth  The width of the screen or viewport in pixels.
     * @param screenHeight The height of the screen or viewport in pixels.
     * @param fontSize     The scale factor for the font size when drawing text.
     *
     * Source:
     * - GlyphLayout: https://javadoc.io/doc/com.badlogicgames.gdx/gdx/1.9.5/com/badlogic/gdx/graphics/g2d/GlyphLayout.html
     */
    private void renderPowerUpHud(float screenWidth, float screenHeight, float fontSize) {
        // Calculate the height of a single line of text
        float lineHeight = font.getLineHeight() * fontSize;

        // Size of the icons representing the power-ups
        float symbolSize = 32f;

        // The icons representing the invincible and freeze power-up
        TextureRegion invincibleSymbol = Textures.INVINCIBLE;
        TextureRegion freezeSymbol = Textures.FREEZE;

        // Display the number of collected invincible & freeze power-ups and instructions
        String invincibleText = player.getCollectedInvinciblePowerUp() + " Invincible (Press Enter)";
        String freezeText = player.getCollectedFreezePowerUp() + " Freeze (Press Shift)";

        // Dynamically calculate the width of the text
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, invincibleText);
        float textWidthRight = layout.width;

        // Calculate the total width of the rectangle
        float rectWidth = symbolSize + textWidthRight + 20f; // Add a small space between the edge of the rectangle and the text
        // Calculate the height of the rectangle
        float invincibleRectHeight = Math.max(symbolSize, font.getLineHeight()) + 10f; // Height of the line for the invincible power-up
        float freezeRectHeight = Math.max(symbolSize, font.getLineHeight()) + 10f; // Height of the line for the freeze power-up
        float timerHeight = isTimerActive ? lineHeight + 10f : 0f; // Height of the line for the timer (only when the timer is active)

        // Combined height of all three elements
        float combinedHeight = invincibleRectHeight + freezeRectHeight + timerHeight + 10f;

        // Position the rectangle in the top-right corner of the screen
        float rectX = Gdx.graphics.getWidth() - rectWidth - 10f;
        float rectY = screenHeight - combinedHeight - 10f;

        // Calculate the y-coordinates of the invincible and freeze power-up elements
        float invincibleY = rectY + combinedHeight - invincibleRectHeight -2f; // Top of the invincible power-up
        float freezeY = invincibleY - freezeRectHeight; // Below the invincible power-up with spacing

        //Helper method
        renderBackground(rectX, rectY, rectWidth, combinedHeight);

        // Render from the camera's perspective
        spriteBatch.setProjectionMatrix(camera.combined);
        // Start drawing
        spriteBatch.begin();
        // Drawing the icons
        spriteBatch.draw(invincibleSymbol, rectX + 10f, invincibleY + (invincibleRectHeight - symbolSize) / 2, symbolSize, symbolSize);
        spriteBatch.draw(freezeSymbol, rectX + 10f, freezeY + (freezeRectHeight - symbolSize) / 2, symbolSize, symbolSize);

        // Set the font color to white
        font.setColor(Color.WHITE);
        // Draw the texts next to the icons
        font.draw(spriteBatch, invincibleText, rectX + symbolSize + 15f, invincibleY + invincibleRectHeight / 2 + font.getLineHeight() / 2);
        font.draw(spriteBatch, freezeText, rectX + symbolSize + 15f, freezeY + freezeRectHeight / 2 + font.getLineHeight() / 2);

        // Check if the timer is active
        if (isTimerActive) {
            // Calculate y-coordinate of the timer
            float timerY = freezeY - 10f;
            // Set color to red
            font.setColor(Color.RED);
            // Text of the timer
            String timerText = String.format("%.1f seconds", specialPowerUpTimer);
            // Draw the timer
            font.draw(spriteBatch, timerText, rectX + 10f, timerY);
            // Set color to white again for later drawings
            font.setColor(Color.WHITE);
        }
        // End drawing
        spriteBatch.end();
    }

    /**
     * Renders the main HUD on the screen.
     * Displays information such as timer, player stats, remaining enemies, and the exit status.
     *
     * @param screenWidth  The width of the screen in pixels.
     * @param screenHeight The height of the screen in pixels.
     * @param fontSize     The dynamically adjusted font size for the HUD text.
     */
    private void renderMainHud(float screenWidth, float screenHeight, float fontSize) {

        // Determines the HUD text content dynamically based on the game mode (single-player or multiplayer)
        String[] hudTexts;
        if (!game.isMultiplayer()) {
            // Single Player
            hudTexts = new String[] {
                    "Press Esc to Pause!",
                    "Time: " + String.format("%.1f", gameTimer.getTimeRemaining()),
                    "Blast Radius: " + blastRadiusP1,
                    "Bomb Limit: " + bombLimitP1,
                    remainingEnemiesString,
                    "Exit " + (exitLocked ? "Locked" : "Unlocked")
            };
        } else { // Multiplayer
            hudTexts = new String[] {
                    "Press Esc to Pause!",
                    "Time: " + String.format("%.1f", gameTimer.getTimeRemaining()),
                    "P1 Blast Radius: " + blastRadiusP1,
                    "P1 Bomb Limit: " + bombLimitP1,
                    "P2 Blast Radius: " + blastRadiusP2,
                    "P2 Bomb Limit: " + bombLimitP2,
                    remainingEnemiesString,
                    "Exit " + (exitLocked ? "Locked" : "Unlocked")
            };
        }

        // Calculate spacing, line height, and maximum text width
        float hudSpacing = 10f; // Spacing between the text and the edges of the hud background
        float lineHeight = font.getLineHeight() * fontSize; // Calculate the height of a single line of text
        float maxTextWidth = calculateMaxTextWidth(hudTexts);  // Helper method: Calculation of the maximum width of the text content in the hud

        // Calculate the height of the HUD and adjust its dimensions dynamically
        float totalHeight = (lineHeight * 1.2f) * hudTexts.length + 2 * hudSpacing;
        float hudHeight = Math.min(totalHeight, screenHeight * 0.4f);
        hudHeight = Math.max(hudHeight, 110f);

        // Position the HUD in the top-left corner of the screen
        float hudX = hudSpacing;
        float hudY = screenHeight - hudHeight;

        //Helper method: Render HUD background
        renderBackground(hudX, hudY, maxTextWidth + 2 * hudSpacing, hudHeight);

        // Render text elements from the camera's perspective
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Start showing the text from the top of the hud
        float textY = hudY + hudHeight - hudSpacing;

        // Render each line of the HUD with dynamic coloring based on content
        for (String text : hudTexts) {
            String[] splitText = text.split("\n");  // Split the text if an element contains more than one line
            for (String line : splitText) {
                if (text.contains("Time")) {
                    font.setColor(gameTimer.getTimeRemaining() < 30 ? RED : WHITE); // Set the color of the countdown timer to red if it es less than 30 seconds
                } else if (text.contains("Exit")) {
                    font.setColor(exitLocked ? RED : GREEN);  // Set the color of exitLocked to red. When it gets unlocked, the color is set to green
                } else {
                    font.setColor(WHITE); // In all other cases the color should be white
                }
                font.draw(spriteBatch, line, hudX + hudSpacing, textY);  // Draw current line
                textY -= lineHeight * 1.2f;  // Switch to the next line, taking into account the selected line spacing
            }
        }
        spriteBatch.end(); // Finish drawing
    }

    /**
     * Calculates the maximum width of the given text lines for dynamic HUD layout.
     *
     * @param hudTexts An array of text lines to render on the HUD.
     * @return The width of the longest line of text.
     */
    private float calculateMaxTextWidth(String[] hudTexts) {
        float maxWidth = 0;
        // Begin the SpriteBatch for text measurement
        spriteBatch.begin();

        // Iterate through all HUD text elements
        for (String text : hudTexts) {
            // Measure the width of the current text
            float textWidth = font.draw(spriteBatch, text, 0, 0).width;
            // Update the maximum width if the current text is wider
            if (textWidth > maxWidth) {
                maxWidth = textWidth;
            }
        }

        // End the SpriteBatch to complete the measurement
        spriteBatch.end();

        // Return the maximum text width
        return maxWidth;
    }

    /**
     * Renders the background of the HUD as a semi-transparent black rectangle.
     *
     * @param x      The x-coordinate of the HUD background.
     * @param y      The y-coordinate of the HUD background.
     * @param width  The width of the HUD background.
     * @param height The height of the HUD background.
     */
    private void renderBackground(float x, float y, float width, float height) {
        // Set the projection matrix for the shape renderer to match the camera
        shapeRenderer.setProjectionMatrix(camera.combined);
        // Enable blending to allow transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        // Begin drawing a filled shape
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Set the color to semi-transparent black (50% opacity)
        shapeRenderer.setColor(new Color(0, 0, 0, 0.5f));
        // Draw the rectangle at the specified position with the given dimensions
        shapeRenderer.rect(x, y, width, height);
        // End the shape rendering
        shapeRenderer.end();
        // Disable blending to revert to normal rendering
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Resizes the HUD when the screen size changes.
     * This is called when the window is resized.
     *
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */
    public void resize(int width, int height) {
        // Set the camera to an orthographic projection with the new screen dimensions
        camera.setToOrtho(false, width, height);
    }

    /**
     * Updates all elements of the HUD to ensure the displayed information is always up to date.
     * This method is called every frame and dynamically updates stats such as player power-ups,
     * remaining enemies, and whether the exit is locked.
     */
    public void updateElements() {
        // Update Player 1's current blast radius and bomb limit
        this.blastRadiusP1 = player.getBlastRadius();
        this.bombLimitP1 = player.getConcurrentBombLimit();

        // Update Player 2's stats if multiplayer mode
        if (game.isMultiplayer() && player2 != null) {
            this.blastRadiusP2 = player2.getBlastRadius();
            this.bombLimitP2 = player2.getConcurrentBombLimit();
        }

        // Update the number of remaining enemies on the map
        remainingEnemies = gameMap.getEnemies().size();
        if (remainingEnemies > 0) {
            this.remainingEnemiesString = "Enemies Remaining: " + remainingEnemies;
        } else {
            this.remainingEnemiesString = "All enemies defeated!";
        }

        // Update the exit's locked status
        this.exitLocked = remainingEnemies > 0;
    }


    /**
     * Initializes and starts the special power-up timer (e.g., for invincible or freeze power-ups).
     * Sets the timer to 15 seconds and activates it.
     */
    public void startSpecialPowerUpTimer(){
        this.specialPowerUpTimer = 15f; // Set the timer to 15 seconds
        this.isTimerActive = true; // Mark the timer as active
    }

    /**
     * Updates the special power-up timer each frame.
     * Decreases the timer by the elapsed frame time (`deltaTime`) and deactivates it when it expires.
     *
     * @param deltaTime The time elapsed since the last frame, used to decrement the timer.
     */
    public void updateTimer(float deltaTime) {
        // Check if the timer is active
        if (isTimerActive) {
            specialPowerUpTimer -= deltaTime; // Decrease the timer by the elapsed time

            // Check if the timer goes below or equals zero
            if (specialPowerUpTimer <= 0) {
                specialPowerUpTimer = 0; // Set the timer to zero again
                isTimerActive = false; // Deactivate the timer
            }
        }
    }
}
