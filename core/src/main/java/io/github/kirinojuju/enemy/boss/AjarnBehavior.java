package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.StandardEnemyBehavior;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Ajarns hold their ground and may warp beside the player. */
public class AjarnBehavior extends StandardEnemyBehavior {
    public static final float AE_WARP_WARNING = .8f;
    public static final float AE_WARP_RADIUS = 75;
    private static final float AE_WARP_INTERVAL = 7f;
    private static final float AE_BIND_DURATION = 1f;
    private static final float AE_WARP_DAMAGE = 14;

    @Override
    public void update(CombatRoom room, CombatRoom.Enemy boss, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (restoreAfterWarp(boss, dt)) return;
        updateStandard(room, boss, dt, player, plan);
    }

    protected final boolean restoreAfterWarp(CombatRoom.Enemy boss, float dt) {
        if (boss.warpReturnTime <= 0) return false;
        boss.warpReturnTime = Math.max(0, boss.warpReturnTime - dt);
        if (boss.warpReturnTime == 0) {
            boss.x = boss.homeX;
            boss.y = boss.homeY;
        }
        return true;
    }

    @Override
    protected boolean tryWarp(CombatRoom room, CombatRoom.Enemy boss, float dt,
                              PlayerCharacter player, RunPlan plan) {
        if (!boss.warper || boss.anchored || (boss.kind == 0 && boss.satiCharge > 0)) return false;
        if (boss.kind == RunPlan.AE_ID) return tryAeWarp(boss, dt, player, plan);
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

    /** Ae locks one visible destination, giving the player time to leave it or dash. */
    private static boolean tryAeWarp(CombatRoom.Enemy boss, float dt,
                                     PlayerCharacter player, RunPlan plan) {
        if (boss.warpWindup > 0) {
            boss.warpWindup = Math.max(0, boss.warpWindup - dt);
            if (boss.warpWindup > 0) return true;
            boss.x = boss.warpX;
            boss.y = boss.warpY;
            boss.warpReturnTime = .38f;
            boolean bind = boss.warpStrikes++ % 2 == 0;
            float dx = player.x - boss.warpX, dy = player.y - boss.warpY;
            if (dx * dx + dy * dy <= AE_WARP_RADIUS * AE_WARP_RADIUS && player.invulnerable <= 0) {
                if (bind) player.bind(AE_BIND_DURATION);
                else player.hurt(AE_WARP_DAMAGE * plan.damageScale());
            }
            return true;
        }
        boss.warpCooldown = Math.max(0, boss.warpCooldown - dt);
        if (boss.warpCooldown > 0) return false;
        boss.warpCooldown = Math.max(5f, AE_WARP_INTERVAL / plan.speedScale());
        boss.warpWindup = AE_WARP_WARNING;
        boss.warpX = MathUtils.clamp(player.x, GameSpace.LEFT + boss.radius,
            GameSpace.RIGHT - boss.radius);
        boss.warpY = MathUtils.clamp(player.y, GameSpace.BOTTOM + boss.radius,
            GameSpace.WALK_TOP - boss.radius);
        return true;
    }
}
