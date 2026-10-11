package io.github.kirinojuju.enemy.ex;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Liquid Cat's spawn state; its attack cycle is shared through LiquidCatBehavior. */
public final class LiquidCatBoss extends CombatRoom.Enemy {
    public static final float LEAP_WINDUP = LiquidCatBehavior.LEAP_WINDUP;
    public static final float TRANSFORM_DURATION = LiquidCatBehavior.TRANSFORM_DURATION;
    public static final float DIVIDE_DURATION = LiquidCatBehavior.DIVIDE_DURATION;

    public LiquidCatBoss(RunPlan plan) {
        super(GameSpace.RIGHT - 75, GameSpace.WALK_TOP - 75, 34,
            380 * plan.healthScale(), 88 * plan.spScale(),
            120 * plan.speedScale(), 14 * plan.damageScale(), CombatRoom.LIQUID_CAT);
        skillTimer = 1.8f;
        exStateTime = 3.5f;
    }

    @Override
    public void updateBehavior(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
        updateSkill(room, dt, player, plan);
    }

    public void updateSkill(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
        behavior.update(room, this, dt, player, plan);
    }

    public float damageMultiplier() { return exState == 1 ? .55f : 1f; }
}
