package io.github.kirinojuju.enemy.boss;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.ex.ExBoss;
import io.github.kirinojuju.enemy.ex.LiquidCatBoss;
import io.github.kirinojuju.flow.RunPlan;

/** Shared drawing sizes; incoming attacks use only the visible bounds inside each sprite canvas. */
public final class BossBody {
    public static final float KEN_DRAW_SIZE = 250f;
    public static final float AE_DRAW_SIZE = 275f;
    public static final float M_DRAW_SIZE = 325f;
    public static final float SATI_DRAW_SIZE = 275f;
    public static final float DOME_DRAW_SIZE = 275f;
    public static final float CAT_DRAW_SIZE = 128f;
    public static final float SMALL_CAT_DRAW_SIZE = 96f;
    public static final float SWORD_DRAW_SIZE = 128f;

    // Static size definitions do not need an instance.
    private BossBody() { }

    // Return the body height for normal bosses; anchored Final Exam keeps its original hit geometry.
    public static float height(Enemy boss) {
        if (boss.anchored) return 0;
        switch (boss.kind) {
            case RunPlan.KEN_ID: return KEN_DRAW_SIZE;
            case RunPlan.AE_ID: return AE_DRAW_SIZE;
            case RunPlan.M_ID: return M_DRAW_SIZE;
            case RunPlan.SATI_ID: return SATI_DRAW_SIZE;
            case RunPlan.DOME_ID: return DOME_DRAW_SIZE;
            case CombatRoom.LIQUID_CAT:
                if (((LiquidCatBoss) boss).catTransformTime > 0)
                    return SMALL_CAT_DRAW_SIZE + (CAT_DRAW_SIZE - SMALL_CAT_DRAW_SIZE)
                        * ((LiquidCatBoss) boss).catTransformTime / LiquidCatBoss.TRANSFORM_DURATION;
                return ((ExBoss) boss).exState == 1 ? SMALL_CAT_DRAW_SIZE : CAT_DRAW_SIZE;
            case CombatRoom.INSECURE_SWORD: return SWORD_DRAW_SIZE;
            default: return 0;
        }
    }

    // Match the width of non-square artwork; square sprites keep an aspect ratio of one.
    public static float width(Enemy boss) {
        return height(boss) * boss.bodyAspectRatio;
    }

    // A missing body image keeps the ordinary circular hit area instead of an invisible large box.
    public static boolean hasBounds(Enemy boss) {
        return height(boss) > 0 && boss.bodyBoundsReady;
    }

    // Convert the visible pixels' left edge from normalized image coordinates to world coordinates.
    public static float left(Enemy boss) {
        return boss.x + (boss.bodyLeft - .5f) * width(boss);
    }

    // Convert the visible pixels' right edge to world coordinates.
    public static float right(Enemy boss) {
        return boss.x + (boss.bodyRight - .5f) * width(boss);
    }

    // Convert the visible pixels' bottom edge, excluding transparent padding below the feet.
    public static float bottom(Enemy boss) {
        return boss.y + boss.radius - height(boss) + boss.bodyBottom * height(boss);
    }

    // Convert the visible pixels' top edge, excluding transparent padding above the head.
    public static float top(Enemy boss) {
        return boss.y + boss.radius - height(boss) + boss.bodyTop * height(boss);
    }

    // Scan RGBA pixels once per loaded image; ignore transparent padding and very faint pixels.
    public static float[] visibleBounds(int width, int height, int[] rgba) {
        int left = width, top = height, right = -1, bottom = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((rgba[y * width + x] & 255) < 32) continue;
                left = Math.min(left, x);
                right = Math.max(right, x);
                top = Math.min(top, y);
                bottom = Math.max(bottom, y);
            }
        }
        if (right < left) return null;
        return new float[] {left / (float)width, 1 - (bottom + 1) / (float)height,
            (right + 1) / (float)width, 1 - top / (float)height};
    }

    // Attach the current frame's visible rectangle, mirroring it with right-facing artwork.
    public static void applyBounds(Enemy boss, float[] bounds, boolean flipped) {
        boss.bodyBoundsReady = bounds != null;
        if (bounds == null) return;
        boss.bodyLeft = flipped ? 1 - bounds[2] : bounds[0];
        boss.bodyBottom = bounds[1];
        boss.bodyRight = flipped ? 1 - bounds[0] : bounds[2];
        boss.bodyTop = bounds[3];
    }
}
