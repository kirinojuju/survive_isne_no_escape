package io.github.kirinojuju.enemy.boss;

// File role: Shared teacher boss behavior, including temporary warps and returning home.

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.StandardEnemyBehavior;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Ajarns hold their ground and may warp beside the player. */
public class AjarnBehavior extends StandardEnemyBehavior {
    // Handle the boss's return from a warp, then run shared enemy combat rules.
    @Override
    public void update(CombatRoom room, CombatRoom.Enemy boss, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (restoreAfterWarp(boss, dt)) return;
        updateStandard(room, boss, dt, player, plan);
    }

    // Count down the temporary warp visit and return the boss to its home position.
    protected final boolean restoreAfterWarp(CombatRoom.Enemy boss, float dt) {
        if (boss.warpReturnTime <= 0) return false;
        boss.warpReturnTime = Math.max(0, boss.warpReturnTime - dt);
        if (boss.warpReturnTime == 0) {
            boss.x = boss.homeX;
            boss.y = boss.homeY;
        }
        return true;
    }

    // Run the shared boss teleport when enabled and off cooldown.
    @Override
    protected boolean tryWarp(CombatRoom room, CombatRoom.Enemy boss, float dt,
                              PlayerCharacter player, RunPlan plan) {
        if (!boss.warper || boss.anchored || (boss.kind == RunPlan.SATI_ID && boss.satiCharge > 0)) return false;
        boss.warpCooldown = Math.max(0, boss.warpCooldown - dt);
        if (boss.warpCooldown > 0) return false;
        boss.warpCooldown = Math.max(2.6f, 4.5f / plan.speedScale());
        boss.x = MathUtils.clamp(player.x + (boss.x > player.x ? 48 : -48),
            GameSpace.LEFT + boss.radius, GameSpace.RIGHT - boss.radius);
        boss.y = MathUtils.clamp(player.y, GameSpace.BOTTOM + boss.radius,
            GameSpace.WALK_TOP - boss.radius);
        boss.warpReturnTime = .38f;
        if (boss.warpStrikes++ % 2 == 0) player.bind(2.8f);
        else player.hurt(26 * plan.damageScale());
        return true;
    }

}
