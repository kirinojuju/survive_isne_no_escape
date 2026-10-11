package io.github.kirinojuju.enemy.boss;

// File role: Shared boss drawing sizes and full-body hit areas, independent of texture loading.

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;

/** Sprites and incoming attacks use the same sizes so enlarging a boss also enlarges its hit area. */
public final class BossBody {
    public static final float KEN_DRAW_SIZE = 250f;
    public static final float AE_DRAW_SIZE = 275f;
    public static final float FAR_DRAW_SIZE = 275f;
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
            case RunPlan.FAR_ID: return FAR_DRAW_SIZE;
            case RunPlan.M_ID: return M_DRAW_SIZE;
            case RunPlan.SATI_ID: return SATI_DRAW_SIZE;
            case RunPlan.DOME_ID: return DOME_DRAW_SIZE;
            case CombatRoom.LIQUID_CAT:
                return boss.exState == 1 && boss.catTransformTime <= 0 ? SMALL_CAT_DRAW_SIZE : CAT_DRAW_SIZE;
            case CombatRoom.INSECURE_SWORD: return SWORD_DRAW_SIZE;
            default: return 0;
        }
    }

    // Match the width of non-square artwork; square sprites keep an aspect ratio of one.
    public static float width(Enemy boss) {
        return height(boss) * boss.bodyAspectRatio;
    }
}
