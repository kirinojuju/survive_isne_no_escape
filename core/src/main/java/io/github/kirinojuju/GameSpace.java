package io.github.kirinojuju;

/** Shared logical screen size and arena bounds. */
final class GameSpace {
    static final float WIDTH = 1280, HEIGHT = 720;
    static final float LEFT = 65, RIGHT = 1215, BOTTOM = 90, TOP = 610;
    // The room artwork's windows end here; actors stay on the floor below them.
    static final float WALK_TOP = 500;

    private GameSpace() { }
}
