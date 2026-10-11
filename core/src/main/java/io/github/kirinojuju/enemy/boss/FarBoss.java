package io.github.kirinojuju.enemy.boss;

// File role: Far's initial stats and safe-circle blast state, used by FarBossBehavior.

import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.world.GameSpace;

/** Far's statistics and initial cast state; attacks live in FarBossBehavior. */
public final class FarBoss extends Enemy {
    // Create FarBoss with its starting stats and state for this encounter.
    public FarBoss(RunPlan plan) {
        super(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (190 + plan.stage() * 22) * plan.healthScale(),
            72 * plan.spScale(), 108 * plan.speedScale(),
            12 * plan.damageScale(), RunPlan.FAR_ID);
        artillery = true;
        farCooldown = 3.4f;
    }
}
