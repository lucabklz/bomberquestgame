package de.tum.cit.ase.bomberquest.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/**
 * This enum manages all the sound effects used in the game.
 * Each constant represents a specific sound effect that can be played during the game.
 *
 * The audio files are located in the `assets/audio` directory.
 * Developers can add new sound effects to this enum and use them throughout the game.
 * This centralized approach ensures efficient sound management and avoids redundant loading.
 */
public enum SoundEffect {

    // Define all sound effects with their corresponding file names and default volumes
    EXPLOSION("boom1.wav", 1.0f),               // Sound effect for explosions
    POWERUP("coin.wav", 1.0f),                  // Sound effect for collecting power-ups
    VICTORY("newthingget.ogg", 1.0f),           // Sound effect for victory events
    GAMEOVER("game_over_bad_chest.wav", 1.0f),  // Sound effect for losing the game
    PLACEBOMB("collect1.wav", 1.0f),            // Sound effect for placing bombs
    INVINCIBLE("bubble_01.ogg", 1.0f),          // Sound effect for activating the invincible power-up
    FREEZE("freeze.wav", 1.0f),                 // Sound effect for activating the freeze power-up
    HIGHSCORE("Hi-Score.ogg", 4.0f),            // Sound effect for achieving a new high score
    SHOWDOWN("westernguitarstab.mp3", 0.5f);    // Sound effect for the last 30 seconds of the game

    /** The sound effect instance loaded from the file. */
    private final Sound soundEffect;

    /** The volume */
    private final float volume;

    /** The default volume for sound effects */
    private static float defaulVolume = 1.0f;

    /**
     * Constructor for initializing a sound effect.
     * Loads the sound file and sets its volume.
     *
     * @param fileName The name of the audio file located in the `audio/` directory.
     * @param volume   The specific volume for this sound effect (range: 0.0 to 1.0).
     */
    SoundEffect(String fileName, float volume) {
        this.soundEffect = Gdx.audio.newSound(Gdx.files.internal("audio/" + fileName));
        this.volume = volume; // initialize the volume
    }


    /**
     * Plays the sound effect at its predefined volume adjusted by the global default volume.
     * If multiple instances of the sound are needed, this method allows them to overlap.
     */
    public void play() {
        this.soundEffect.play(this.volume * defaulVolume);
    }

    /**
     * Stops all currently playing instances of this sound effect.
     * This is useful when you want to immediately halt a looping or overlapping sound effect.
     */
    public void stop() {
        this.soundEffect.stop();
    }

    /**
     * Sets the global default volume for all sound effects.
     * This is a multiplier applied to the individual volume of each sound effect.
     *
     * @param volume The default volume (range: 0.0 to 1.0).
     */
    public static void setDefaulVolume(float volume) {
        defaulVolume = volume;
    }
}