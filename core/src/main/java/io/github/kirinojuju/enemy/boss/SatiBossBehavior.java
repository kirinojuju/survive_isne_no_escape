package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Sati's charge, target snapshot and spear attack; cast state stays on the boss. */
public final class SatiBossBehavior extends AjarnBehavior {
    public static final float CHARGE_DURATION = 1.05f;

    @Override
    protected boolean useSpecial(CombatRoom room, CombatRoom.Enemy boss, float dt,
                                 PlayerCharacter player, RunPlan plan) {
        if (boss.satiCharge > 0) {
            boss.satiCharge = Math.max(0, boss.satiCharge - dt);
            if (boss.satiCharge == 0) {
                float x = elementX(boss);
                float y = elementY(boss);
                float dx = boss.satiAimX - x, dy = boss.satiAimY - y;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                if (distance < .001f) { dx = 0; dy = -1; distance = 1; }
                // Keep the custom Sati's original stats and the generic enemy's cleanup stats.
                float speed = (boss instanceof SatiBoss ? SatiBoss.SPEAR_SPEED : 390) * plan.speedScale();
                float radius = boss instanceof SatiBoss ? SatiBoss.SPEAR_RADIUS : 24;
                CombatRoom.Shot spear = new CombatRoom.Shot(x, y, dx / distance * speed,
                    dy / distance * speed, radius, 22 * plan.damageScale(), 3f, false);
                spear.satiSpear = true;
                room.addEnemyShot(boss, spear);
            }
            return true;
        }
        boss.satiCooldown -= dt;
        if (boss.satiCooldown > 0) return false;
        boss.satiCooldown = Math.max(3.2f, 5.4f / plan.speedScale());
        boss.satiCharge = CHARGE_DURATION;
        boss.satiAimX = player.x;
        boss.satiAimY = player.y;
        if (room.audio != null) room.audio.cppTransform();
        return true;
    }

    /** Shared origin for the charging element, its warning and the released spear. */
    public static float elementX(CombatRoom.Enemy boss) {
        float side = boss.x > GameSpace.WIDTH / 2 ? -1 : 1;
        return MathUtils.clamp(boss.x + side * 72,
            GameSpace.LEFT + 35, GameSpace.RIGHT - 35);
    }

    public static float elementY(CombatRoom.Enemy boss) {
        return MathUtils.clamp(boss.y + 25,
            GameSpace.BOTTOM + 35, GameSpace.WALK_TOP - 35);
    }
}
