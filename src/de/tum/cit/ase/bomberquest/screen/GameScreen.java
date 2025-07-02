package de.tum.cit.ase.bomberquest.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import de.tum.cit.ase.bomberquest.BomberQuestGame;
import de.tum.cit.ase.bomberquest.audio.SoundEffect;
import de.tum.cit.ase.bomberquest.map.GameMap;
import de.tum.cit.ase.bomberquest.map.GameTimer;
import de.tum.cit.ase.bomberquest.map.mappoints.Exit;
import de.tum.cit.ase.bomberquest.texture.Drawable;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It manages the game logic, user interface, and rendering of all game elements.
 * This is the main screen where the actual game takes place.
 *
 * Source:
 * - Orthographic Camera: https://libgdx.com/wiki/graphics/2d/orthographic-camera
 */
public class GameScreen extends BaseScreen {

    /** The size of a grid cell in pixels (e.g. x=1, y=1 is the bottom left corner of the map) */
    public static final int TILE_SIZE_PX = 16;

    /** The scale of the game.*/
    public static final int SCALE = 4;

    private final SpriteBatch spriteBatch; // The SpriteBatch used for rendering game elements
    private final GameMap map; // The game map
    private final Hud hud; // The Heads-Up Display (HUD) displaying game information
    private final ShapeRenderer shapeRenderer; // The ShapeRenderer used for drawing shapes
    private final GameTimer gameTimer; // The central game timer that tracks the remaining time
    private float timerRedFrame = 0f; // Tracks the elapsed time for the red warning frame animation
    private boolean isFrameVisible = true; // Indicates whether the red frame is currently visible
    private boolean alreadyPlayed = false; // Boolean attribute to track if the showdown sound effect was already played


    /**
     * Constructor for GameScreen. Sets up the game screen.
     * Initializes the gameplay elements such as the game map, HUD, and rendering tools.
     * @param game The main game instance.
     * @param existingHud The existing HUD instance with the current game state, such as the remaining time.
     *                    If null, a new HUD instance will be created.
     */
    public GameScreen(BomberQuestGame game, Hud existingHud) {
        super(game);
        this.gameTimer = game.getGameTimer(); // Centralized timer
        this.spriteBatch = game.getSpriteBatch();
        this.map = game.getMap();
        // If there already exists a hud, this one should continue to be used. Otherwise, a new hud should be created. (Is needed for the pause logic)
        this.hud = existingHud != null ? existingHud : new Hud(game, spriteBatch, game.getSkin().getFont("font"), map.getPlayer(), map.getPlayer2(), map, gameTimer);
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * The render method is called every frame to update and render the game.
     * It handles game logic, rendering, and user interaction.
     * @param deltaTime The time in seconds since the last render.
     */
    @Override
    public void render(float deltaTime) {

        //Check if the game is paused
        if (game.isPaused()) {
            // Pauses all bombs on the map and stops their timers
            map.setBombsPaused(true);
            return;
        } else {
            // Resume all bombs on the map to continue their timer
            map.setBombsPaused(false);
        }

        // Check for escape key press to go back to the menu (The esc-pause logic)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setPaused(true);
            gameTimer.togglePause();
            game.goToMenu();
            return;
        }

        // Cap frame time to 250ms to prevent spiral of death
        float frameTime = Math.min(deltaTime, 0.250f);

        // Update the timer based on the past time in the game. When the time has expired, the game is over
        if (!game.isPaused()){
            gameTimer.update(frameTime);
        }
        if (gameTimer.getTimeRemaining() <= 0){
            map.removeBombsAndBlasts();
            map.gameOver(); // Call the game over method when the time is over
        }

        // Clear the previous frame from the screen, or else the picture smears
        ScreenUtils.clear(Color.BLACK);

        // Update the map state
        map.tick(frameTime);

        // Check for exit interaction and animation
        map.checkPlayerAtExit(spriteBatch, shapeRenderer, frameTime);

        //Update the camera
        if (!game.isMultiplayer()) { // Check if the game is in single player mode
            updateCamera(); // Update the camera for single player mode
        } else {
            fullMapView(); // Set the camera to full map view for multiplayer mode
        }

        // Render the map on the screen
        renderMap(deltaTime);

        // When the remaining timer is 30 seconds or less, a red frame should blink around the screen
        if (gameTimer.getTimeRemaining() <= 30) {
            // Increment the blink timer for the red frame
            timerRedFrame += deltaTime;

            // Toggle the visibility of the frame every 0.25 seconds
            if (timerRedFrame >= 0.25f) {
                isFrameVisible = !isFrameVisible; // Switch visibility state
                timerRedFrame = 0f; // Reset the blink timer
            }

            // Render the red frame
            renderRedFrame();
        }

        // Render the HUD on the screen
        hud.render();
    }


    /**
     * Updates the camera to follow the player while keeping them within a designated free area.
     * Ensures the camera stays within the boundaries of the game map.
     */
    private void updateCamera() {
    // Retrieve camera and map width and height in pixels
        float cameraWidth = camera.viewportWidth;
        float cameraHeight = camera.viewportHeight;
        float mapPixelWidth = map.getMapWidth() * TILE_SIZE_PX * SCALE;
        float mapPixelHeight = map.getMapHeight() * TILE_SIZE_PX * SCALE;

        // Calculate horizontal and vertical offsets to adjust how the player is centered in the camera view
        // Is necessary because the center of the player's hitbox does not match with the center of the player's texture
        float offsetX = 0.5f * TILE_SIZE_PX * SCALE; // horizontal offset
        float offsetY = 0.3f * TILE_SIZE_PX * SCALE; // vertical offset

        // Calculate the player's visual center by applying the offsets to the player's position
        float playerCenterX = map.getPlayer().getX() * TILE_SIZE_PX * SCALE + offsetX;
        float playerCenterY = map.getPlayer().getY() * TILE_SIZE_PX * SCALE + offsetY;

        // Retrieve the current center of the camera
        float currentCamX = camera.position.x;
        float currentCamY = camera.position.y;

        // Define a 25% margin that the camera even before the player leaves the middle 80% of the screen horizontally and vertically during gameplay
        float marginFactor = 0.25f;
        float marginX = cameraWidth * marginFactor;
        float marginY = cameraHeight * marginFactor;

        // Calculate the boundaries of the free area within the current camera position
        float leftBoundary = currentCamX - (cameraWidth / 2f) + marginX;
        float rightBoundary = currentCamX + (cameraWidth / 2f) - marginX;
        float bottomBoundary = currentCamY - (cameraHeight / 2f) + marginY;
        float topBoundary = currentCamY + (cameraHeight / 2f) - marginY;

        // Adjustment of the camera position
        float adjustedPositionX = currentCamX;
        float adjustedPositionY = currentCamY;

        // Horizontal adjustment (left / right)
        if (playerCenterX < leftBoundary) {
            adjustedPositionX -= (leftBoundary - playerCenterX);
        } else if (playerCenterX > rightBoundary) {
            adjustedPositionX += (playerCenterX - rightBoundary);
        }

        // Vertical adjustment (top / bottom)
        if (playerCenterY < bottomBoundary) {
            adjustedPositionY -= (bottomBoundary - playerCenterY);
        } else if (playerCenterY > topBoundary) {
            adjustedPositionY += (playerCenterY - topBoundary);
        }

        // Clamp the camera position to ensure it stays within the map's boundaries
        float halfW = cameraWidth / 2f;
        float halfH = cameraHeight / 2f;

        // Clamp the camera horizontally
        if (mapPixelWidth <= cameraWidth) {
            adjustedPositionX = mapPixelWidth / 2f; // Center the camera if the map is smaller than the viewport
        } else {
            adjustedPositionX = Math.max(adjustedPositionX, halfW); // Camera is not allowed to exceed the left boundary
            adjustedPositionX = Math.min(adjustedPositionX, mapPixelWidth - halfW); // Camera is not allowed to exceed the right boundary
        }

        // Clamp the camera vertically
        if (mapPixelHeight <= cameraHeight) {
            adjustedPositionY = mapPixelHeight / 2f; // Center the camera if the map is smaller than the viewport
        } else {
            adjustedPositionY = Math.max(adjustedPositionY, halfH); // Camera is not allowed to exceed the bottom boundary
            adjustedPositionY = Math.min(adjustedPositionY, mapPixelHeight - halfH); // Camera is not allowed to exceed the top boundary
        }

        // Update the camera and apply all changes
        camera.position.set(adjustedPositionX, adjustedPositionY, 0);
        camera.update();

    }


    /**
     * Adjusts the camera to ensure the entire map is visible on the screen.
     * This method calculates the required zoom level based on the map's dimensions
     * and centers the camera on the map. Primarily used for multiplayer mode.
     */
    public void fullMapView() {
        // Calculate the map dimensions in pixels
        float mapPixelWidth  = map.getMapWidth() * TILE_SIZE_PX * SCALE;
        float mapPixelHeight = map.getMapHeight() * TILE_SIZE_PX * SCALE;

        // Camera's view area without any zoom
        float viewWidth = camera.viewportWidth;
        float viewHeight = camera.viewportHeight;

        // Calculate the zoom factor for both dimensions. The map width or height should not be greater than the width of the camera field of view times the zoom.
        float zoomX = mapPixelWidth  / viewWidth;
        float zoomY = mapPixelHeight / viewHeight;

        // Take the maximum to ensure the entire map is visible
        float maxZoom = Math.max(zoomX, zoomY);

        // Set the camera's zoom level
        camera.zoom = maxZoom;

        // Position the camera at the center of the map
        camera.position.set(mapPixelWidth / 2f, mapPixelHeight / 2f, 0);

        // Update the camera
        camera.update();
    }

    /**
     * Renders a red frame around the screen when the timer reaches a critical state.
     * The frame is only drawn if it is currently visible (blinking effect).
     */
    private void renderRedFrame() {
        // Do not render if the frame is invisible
        if (!isFrameVisible) {
            return;
        }

        // Play the showdown sound effect once when the frame becomes visible
        if (alreadyPlayed == false){
            SoundEffect.SHOWDOWN.play();
            alreadyPlayed = true;
        }

        // Get the camera's viewport dimensions (visible area)
        float screenWidth = camera.viewportWidth;
        float screenHeight = camera.viewportHeight;

        // Thickness of the frame
        float frameThickness = 10f;

        // Calculate the bottom-left corner of the visible area
        float camX = camera.position.x - screenWidth / 2;
        float camY = camera.position.y - screenHeight / 2;

        // Configure the shape renderer to draw relative to the camera's projection
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Set the color for the frame to red
        shapeRenderer.setColor(Color.RED);

        // Draw the four sides of the frame
        // Top
        shapeRenderer.rect(camX, camY + screenHeight - frameThickness, screenWidth, frameThickness);
        // Bottom
        shapeRenderer.rect(camX, camY, screenWidth, frameThickness);
        // Left
        shapeRenderer.rect(camX, camY, frameThickness, screenHeight);
        // Right
        shapeRenderer.rect(camX + screenWidth - frameThickness, camY, frameThickness, screenHeight);

        // Finish rendering
        shapeRenderer.end();
    }


    /**
     * Renders the game map and its elements.
     * @param deltaTime Time since the last frame.
     */
    private void renderMap(float deltaTime) {
        spriteBatch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        for (Drawable drawable : map.getDrawablesInRenderOrder()) {
            if (drawable instanceof Exit exit) {
                spriteBatch.end(); // Temporarily end batch to allow for shape rendering
                exit.render(spriteBatch, shapeRenderer, deltaTime);
                spriteBatch.begin(); // Restart batch
            } else {
                draw(spriteBatch, drawable);
            }
        }
        spriteBatch.end();
    }

    /**
     * Draws this object on the screen.
     * The texture will be scaled by the game scale and the tile size.
     * This should only be called between spriteBatch.begin() and spriteBatch.end(), e.g. in the renderMap() method.
     * @param spriteBatch The SpriteBatch to draw with.
     */
    private static void draw(SpriteBatch spriteBatch, Drawable drawable) {
        TextureRegion texture = drawable.getCurrentAppearance();

        // Drawable coordinates are in tiles, so we need to scale them to pixels
        float x = drawable.getX() * TILE_SIZE_PX * SCALE;
        float y = drawable.getY() * TILE_SIZE_PX * SCALE;

        // Additionally scale everything by the game scale
        float width = texture.getRegionWidth() * SCALE;
        float height = texture.getRegionHeight() * SCALE;

        // Drawing
        spriteBatch.draw(texture, x, y, width, height);
    }

    /**
     * Called when the window is resized.
     * This is where the camera is updated to match the new window size.
     * @param width The new window width.
     * @param height The new window height.
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        // Adjust the camera's viewport dimensions to match the height and width of the new window
        camera.viewportWidth = width;
        camera.viewportHeight = height;

        // Resizing of the hud
        hud.resize(width, height);
    }

    /** Getter of the hud */
    public Hud getHud() {
        return hud;
    }

    /** Unused methods from the Screen interface */
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }


}
