package io.github.kirinojuju.enemy.boss;

// File role: Ae's stats and per-boss laser state; AeBossBehavior advances its skills.

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.world.GameSpace;

public class AeBoss extends CombatRoom.Enemy {
    private static final AeBossBehavior BEHAVIOR = new AeBossBehavior();
    public enum LaserPhase { IDLE, WARNING, SWEEP }
    public LaserPhase laserPhase = LaserPhase.IDLE;
    public float laserTimer = 2f;
    public float laserX = GameSpace.LEFT;

    // Create AeBoss with its starting stats and state for this encounter.
    public AeBoss(RunPlan plan) {
        super(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (202 + plan.stage() * 22) * plan.healthScale(),
            72 * plan.spScale(), 99 * plan.speedScale(),
            14 * plan.damageScale(), RunPlan.AE_ID, BEHAVIOR);
        warper = true;
        warpCooldown = 6f;
    }
}
