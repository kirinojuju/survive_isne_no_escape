package io.github.kirinojuju.enemy.boss;

import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.world.GameSpace;

/** M's spawn statistics and per-instance split state. Attacks live in MBossBehavior. */
public class MBoss extends Enemy {
    private static final MBossBehavior BEHAVIOR = new MBossBehavior();
    final boolean splitCopy;
    boolean alreadySplit;

    public MBoss(RunPlan plan) {
        this(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (190 + plan.stage() * 22 + 24) * plan.healthScale(),
            72 * plan.spScale(), 108 * plan.speedScale(),
            16 * plan.damageScale(), false);
    }

    MBoss(float x, float y, float radius, float hp, float maxSp,
          float speed, float damage, boolean splitCopy) {
        super(x, y, radius, hp, maxSp, speed, damage, RunPlan.M_ID, BEHAVIOR);
        this.splitCopy = splitCopy;
        this.alreadySplit = splitCopy;
        this.skillTimer = splitCopy
            ? MBossBehavior.SPLIT_ATTACK_INTERVAL : MBossBehavior.FIRST_ATTACK_INTERVAL;
    }
}
