package com.penguinpush.cullergrader.config;

/**
 * Execution mode for the application.
 * Used to optimize behavior based on whether GUI features are needed.
 */
public enum ExecutionMode {
    /**
     * GUI mode - full thumbnail caching and UI features enabled
     */
    GUI,

    /**
     * CLI mode - minimal memory usage, skip GUI-only operations
     */
    CLI;

    /**
     * Returns true if thumbnails should be cached in memory.
     * CLI mode skips caching to reduce memory usage.
     */
    public boolean shouldCacheThumbnails() {
        return this == GUI;
    }
}
