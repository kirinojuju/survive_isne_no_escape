package io.github.kirinojuju.enemy.boss;

// File role: Far's room-wide blast skill; the player avoids damage by staying in the safe circle.

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Far's full-room blast resolves before ordinary boss actions. */
public final class FarBossBehavior extends AjarnBehavior {
    // Advance Far's special blast, then run shared teacher boss attacks.
    @Override
    public void update(CombatRoom room, CombatRoom.Enemy boss, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (restoreAfterWarp(boss, dt)) return;
        if (updateBlast(room, boss, dt, player, plan)) return;
        updateStandard(room, boss, dt, player, plan);
    }

    // Choose a safe circle, count down the warning and damage the player outside that circle.
    private boolean updateBlast(CombatRoom room, CombatRoom.Enemy boss, float dt,
                                PlayerCharacter player, RunPlan plan) {
        if (boss.farCharge > 0) {
            boss.farCharge = Math.max(0, boss.farCharge - dt);
            if (boss.farCharge == 0) {
                float dx = player.x - boss.farSafeX;
                float dy = player.y - boss.farSafeY;
                boolean inSafeCircle = dx * dx + dy * dy
                    <= CombatRoom.FAR_SAFE_RADIUS * CombatRoom.FAR_SAFE_RADIUS;
                if (!inSafeCircle) player.hurt(27 * plan.damageScale());
                boss.farFlashTime = .3f;
            }
            return true;
        }
        boss.farCooldown -= dt;
        if (boss.farCooldown > 0 || boss.stunTime > 0) return false;
        boss.farCooldown = Math.max(5.2f, 7.2f / plan.speedScale());
        boss.farCharge = CombatRoom.FAR_CAST_DURATION;
        float angle = room.random.nextFloat() * MathUtils.PI2;
        float distance = 125 + room.random.nextFloat() * 55;
        boss.farSafeX = MathUtils.clamp(player.x + MathUtils.cos(angle) * distance,
            GameSpace.LEFT + CombatRoom.FAR_SAFE_RADIUS,
            GameSpace.RIGHT - CombatRoom.FAR_SAFE_RADIUS);
        boss.farSafeY = MathUtils.clamp(player.y + MathUtils.sin(angle) * distance,
            GameSpace.BOTTOM + CombatRoom.FAR_SAFE_RADIUS,
            GameSpace.WALK_TOP - CombatRoom.FAR_SAFE_RADIUS);
        return true;
    }
}
