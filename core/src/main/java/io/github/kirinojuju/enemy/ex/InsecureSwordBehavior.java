package io.github.kirinojuju.enemy.ex;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.ex.ExBehavior.ExEnvironment;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;

/** Post-it notes strip armor but make the sword confident enough to slash. */
public final class InsecureSwordBehavior extends ExBehavior {
    public static final float SLASH_ANIMATION_DURATION = .5f;
    public static final float WHIFF_DURATION = .6f;

    @Override
    protected void act(CombatRoom room, CombatRoom.Enemy sword, float dt,
                       PlayerCharacter player, RunPlan plan) {
        sword.swordAttackTime = Math.max(0, sword.swordAttackTime - dt);
        sword.exObjectTimer -= dt;
        if (sword.exObjectTimer <= 0
            && ExEnvironment.count(room, CombatRoom.NOTE) < 3) {
            ExEnvironment.spawnNote(room);
            sword.exObjectTimer = 4f;
        }
        float dx = player.x - sword.x, dy = player.y - sword.y;
        if (room.carriedNotes > 0
            && dx * dx + dy * dy <= (sword.radius + 78) * (sword.radius + 78)) {
            room.carriedNotes--;
            sword.exConfidence = Math.min(100, sword.exConfidence + 25);
            sword.armor = Math.max(0, sword.armor - 55 * plan.healthScale());
            sword.exState = 2;
            sword.exStateTime = .5f;
            sword.swordAttackTime = 0; // Applying a note interrupts the attack animation.
            sword.exFlashTime = .35f;
            sword.exAimX = sword.x;
            sword.exAimY = sword.y;
            sword.skillTimer = Math.max(sword.skillTimer, 1.3f);
            return;
        }
        if (sword.exState == 1 || sword.exState == 2 || sword.exState == 3) {
            sword.exStateTime -= dt;
            if (sword.exStateTime <= 0 && sword.exState == 1) {
                sword.exState = 2;
                sword.exStateTime = 1.05f;
                sword.swordAttackTime = SLASH_ANIMATION_DURATION;
                sword.exFlashTime = .3f;
                sword.exConfidence = Math.max(0, sword.exConfidence - 15);
                float aimDx = player.x - sword.exAimX;
                float aimDy = player.y - sword.exAimY;
                if (aimDx * aimDx + aimDy * aimDy <= 100 * 100)
                    player.hurt(29 * plan.damageScale());
            } else if (sword.exStateTime <= 0) {
                sword.exState = 0;
            }
            return;
        }
        if (sword.skillTimer <= 0) {
            sword.skillTimer = 2.2f;
            boolean confident = sword.exConfidence >= 100
                || room.random.nextFloat() < .15f + sword.exConfidence * .006f;
            if (confident) {
                sword.exState = 1;
                sword.exStateTime = .65f;
                sword.exAimX = player.x;
                sword.exAimY = player.y;
            } else {
                sword.exState = 3;
                sword.exStateTime = WHIFF_DURATION; // This attack deals 0 damage.
                sword.swordAttackTime = WHIFF_DURATION;
                sword.exFlashTime = .25f;
            }
        }
    }
}
