package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Talgo's full-room blast resolves before ordinary boss actions. */
public final class TalgoBossBehavior extends AjarnBehavior {
    @Override
    public void update(CombatRoom room, CombatRoom.Enemy boss, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (restoreAfterWarp(boss, dt)) return;
        if (updateBlast(room, boss, dt, player, plan)) return;
        updateStandard(room, boss, dt, player, plan);
    }

    private boolean updateBlast(CombatRoom room, CombatRoom.Enemy boss, float dt,
                                PlayerCharacter player, RunPlan plan) {
        if (boss.talgoCharge > 0) {
            boss.talgoCharge = Math.max(0, boss.talgoCharge - dt);
            if (boss.talgoCharge == 0) {
                float dx = player.x - boss.talgoSafeX;
                float dy = player.y - boss.talgoSafeY;
                boolean inSafeCircle = dx * dx + dy * dy
                    <= CombatRoom.TALGO_SAFE_RADIUS * CombatRoom.TALGO_SAFE_RADIUS;
                if (!inSafeCircle) player.hurt(27 * plan.damageScale());
                boss.talgoFlashTime = .3f;
            }
            return true;
        }
        boss.talgoCooldown -= dt;
        if (boss.talgoCooldown > 0 || boss.stunTime > 0) return false;
        boss.talgoCooldown = Math.max(5.2f, 7.2f / plan.speedScale());
        boss.talgoCharge = CombatRoom.TALGO_CAST_DURATION;
        float angle = room.random.nextFloat() * MathUtils.PI2;
        float distance = 125 + room.random.nextFloat() * 55;
        boss.talgoSafeX = MathUtils.clamp(player.x + MathUtils.cos(angle) * distance,
            GameSpace.LEFT + CombatRoom.TALGO_SAFE_RADIUS,
            GameSpace.RIGHT - CombatRoom.TALGO_SAFE_RADIUS);
        boss.talgoSafeY = MathUtils.clamp(player.y + MathUtils.sin(angle) * distance,
            GameSpace.BOTTOM + CombatRoom.TALGO_SAFE_RADIUS,
            GameSpace.WALK_TOP - CombatRoom.TALGO_SAFE_RADIUS);
        return true;
    }
}
