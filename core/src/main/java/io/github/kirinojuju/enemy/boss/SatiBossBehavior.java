package io.github.kirinojuju.enemy.boss;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;

/** Sati's C++ element charges, then becomes a aimed spear. */
public final class SatiBossBehavior extends AjarnBehavior {
    @Override
    protected boolean useSpecial(CombatRoom room, CombatRoom.Enemy boss, float dt,
                                 PlayerCharacter player, RunPlan plan) {
        if (boss.satiCharge > 0) {
            boss.satiCharge = Math.max(0, boss.satiCharge - dt);
            if (boss.satiCharge == 0) {
                float x = CombatRoom.satiElementX(boss);
                float y = CombatRoom.satiElementY(boss);
                float dx = boss.satiAimX - x, dy = boss.satiAimY - y;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                if (distance < .001f) { dx = 0; dy = -1; distance = 1; }
                float speed = 390 * plan.speedScale();
                CombatRoom.Shot spear = new CombatRoom.Shot(x, y, dx / distance * speed,
                    dy / distance * speed, 12, 22 * plan.damageScale(), 3f, false);
                spear.satiSpear = true;
                room.addEnemyShot(boss, spear);
            }
            return true;
        }
        boss.satiCooldown -= dt;
        if (boss.satiCooldown > 0) return false;
        boss.satiCooldown = Math.max(3.2f, 5.4f / plan.speedScale());
        boss.satiCharge = CombatRoom.SATI_CHARGE_DURATION;
        boss.satiAimX = player.x;
        boss.satiAimY = player.y;
        if (room.audio != null) room.audio.cppTransform();
        return true;
    }
}
