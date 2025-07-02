package de.tum.cit.ase.bomberquest.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * This enum is used to manage and control the music tracks in the game.
 * Each enum constant represents a specific music track that can be played during the game.
 * This implementation ensures efficient management of audio resources by avoiding repeated loading of tracks.
 *
 * The audio files are located in the `assets/audio` directory.
 * Developers can add new music tracks to this enum and use them throughout the game.
 */
public enum MusicTrack {

    // Define all music tracks with their corresponding file names and default volumes
    BACKGROUND("background.mp3", 0.2f),      // General background music
    MAINMENU("boss theme.ogg", 0.4f),        // Music track for the menu screen
    GAME("battleThemeA.mp3", 0.2f),          // Music track for the game
    GAMEOVER("Game Over.ogg", 0.2f),         // Music track for the game-over screen
    VICTORY("Heroes Theme.mp3", 0.2f),       // Music track for the victory screen
    INVINCIBLE("invincible.wav", 0.4f),      // Music track for invincibility state
    FREEZE("Rising_Moon.mp3", 0.4f);         // Music track for frozen enemies state

    /** The music file owned by this variant. */
    private final Music music;

    /** The default volume for music */
    private final float defaultVolume;

    /**
     * Constructor for each music track.
     * Loads the audio file, sets it to loop, and assigns a default volume.
     *
     * @param fileName The name of the audio file located in the `audio/` directory.
     * @param volume   The default volume for this track (range: 0.0 to 1.0).
     */
    MusicTrack(String fileName, float volume) {
        this.music = Gdx.audio.newMusic(Gdx.files.internal("audio/" + fileName));
        this.music.setLooping(true);
        this.music.setVolume(volume);
        this.defaultVolume = volume;
    }

    /**
     * Plays the music track.
     * Note: This method does not stop any other music tracks that may be playing.
     * To manage multiple tracks, ensure you explicitly stop other tracks as needed.
     */
    public void play() {
        this.music.play();
    }

    /**
     * Stops the music track.
     * This can be used to halt the playback of the current music.
     */
    public void stop() {
        this.music.stop();
    }

    /**
     * Retrieves the `Music` instance associated with this track.
     * Can be used for advanced audio controls or querying the track's state.
     *
     * @return The `Music` instance.
     */
    public Music getMusic() {
        return music;
    }

    /**
     * Retrieves the default volume of the music track.
     * This is useful for resetting the track to its original volume.
     *
     * @return The default volume for this track.
     */
    public float getDefaultVolume() {
        return defaultVolume;
    }
}
