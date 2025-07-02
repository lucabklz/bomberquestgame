package de.tum.cit.ase.bomberquest.map;

import java.util.ArrayList;
import java.util.List;

public class GameTimer {

    private float timeRemaining; // Time left in seconds
    private boolean isPaused; // Tracks if the game is paused
    private final List<Runnable> pauseListeners = new ArrayList<>();
    private final List<Runnable> resumeListeners = new ArrayList<>();

    /**
     * Constructor to initialize the game timer.
     * @param initialTime The initial time for the timer in seconds.
     */
    public GameTimer(float initialTime) {
        this.timeRemaining = initialTime;
        this.isPaused = false;

    }

    /**
     * Updates the timer if it is not paused.
     * @param deltaTime The time elapsed since the last frame in seconds.
     */
    public void update(float deltaTime) {
        if (!isPaused) {
            timeRemaining = Math.max(0, timeRemaining - deltaTime);
        }
    }

    /**
     * Pauses the timer and notifies all listeners.
     */
    public void pause() {
        if (!isPaused) {
            isPaused = true;
            notifyListeners(pauseListeners);
        }
    }

    /**
     * Resumes the timer and notifies all listeners.
     */
    public void resume() {
        if (isPaused) {
            isPaused = false;
            notifyListeners(resumeListeners);
        }
    }

    /**
     * Toggles the paused state.
     */
    public void togglePause() {
        if (isPaused) {
            resume();
        } else {
            pause();
        }
    }

    /**
     * Checks if the timer is paused.
     * @return True if the timer is paused, otherwise false.
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Gets the remaining time on the timer.
     * @return The remaining time in seconds.
     */
    public float getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Resets the timer to a new initial time.
     * @param newTime The new time for the timer in seconds.
     */
    public void reset(float newTime) {
        this.timeRemaining = newTime;
        this.isPaused = false;
    }

    /**
     * Notifies all listeners in the given list.
     * @param listeners The list of listeners to notify.
     */
    private void notifyListeners(List<Runnable> listeners) {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    /**
     * Retrieves the remaining time for the timer.
     * Ensures the returned value is never negative by capping it at zero.
     */
    public float getRemainingTime() {
        // Return the remaining time, ensuring it is at least 0
        return Math.max(0, timeRemaining);
    }

}