package io.github.kirinojuju.enemy;

// File role: Programming monster teleport warnings, jumps and attacks.

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Warpers override the movement step with a warned teleport. */
public final class WarperMonsterBehavior extends StandardEnemyBehavior {
    // Warn at a locked destination, then teleport the monster and resolve its attack.
    @Override
    protected boolean tryWarp(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                              PlayerCharacter player, RunPlan plan) {
        enemy.warpCooldown = Math.max(0, enemy.warpCooldown - dt);
        if (enemy.warpWindup > 0) {
            enemy.warpWindup = Math.max(0, enemy.warpWindup - dt);
            if (enemy.warpWindup == 0) {
                enemy.x = enemy.warpX;
                enemy.y = enemy.warpY;
                float dx = player.x - enemy.x, dy = player.y - enemy.y;
                if (dx * dx + dy * dy <= 75 * 75)
                    player.hurt(25 * plan.damageScale());
            }
            return true;
        }
        if (enemy.warpCooldown <= 0 && enemy.sp >= enemy.maxSp) {
            enemy.sp = 0;
            enemy.warpWindup = .58f;
            enemy.warpCooldown = 4f;
            enemy.warpX = MathUtils.clamp(player.x, GameSpace.LEFT + enemy.radius,
                GameSpace.RIGHT - enemy.radius);
            enemy.warpY = MathUtils.clamp(player.y, GameSpace.BOTTOM + enemy.radius,
                GameSpace.WALK_TOP - enemy.radius);
            return true;
        }
        return false;
    }
}
