package io.github.kirinojuju.world;

// File role: Shared game dimensions, arena limits and Final Exam server positions.

/** Shared logical screen size and arena bounds. */
public final class GameSpace {
    public static final float WIDTH = 1280, HEIGHT = 720;
    public static final float LEFT = 65, RIGHT = 1215, BOTTOM = 90, TOP = 610;
    // The room artwork's windows end here; actors stay on the floor below them.
    public static final float WALK_TOP = 500;
    public static final float SERVER_PLAYER_Y = 158;
    public static final float SERVER_LANE_TOP = 220;
    public static final float SERVER_BOSS_Y = 465;

    // Return the horizontal position of a Final Exam server boss slot.
    public static float serverBossX(int slot) {
        return LEFT + (RIGHT - LEFT) * (slot + .5f) / 3f;
    }

    // Prevent construction of this utility class; use its static helpers.
    private GameSpace() { }
}
