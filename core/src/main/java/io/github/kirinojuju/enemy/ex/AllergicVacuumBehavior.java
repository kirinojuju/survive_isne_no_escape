package io.github.kirinojuju.enemy.ex;

// File role: Vacuum EX logic: dust, suction, stored dust and attacks.

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.ex.ExBehavior.ExEnvironment;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** The vacuum collects dust, warns before sneezing, then ejects the dust. */
public final class AllergicVacuumBehavior extends ExBehavior {
    // Advance Vacuum's suction and release cycle, collecting dust and firing stored dust.
    @Override
    protected void act(CombatRoom room, CombatRoom.Enemy vacuum, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (vacuum.exState == 1 || vacuum.exState == 2) {
            vacuum.exStateTime -= dt;
            if (vacuum.exStateTime <= 0 && vacuum.exState == 1) {
                int count = vacuum.exStoredDust;
                for (int i = 0; i < count; i++) {
                    float angle = MathUtils.PI2 * i / Math.max(1, count)
                        + room.random.nextFloat() * .28f;
                    CombatRoom.ExObject dust = new CombatRoom.ExObject(
                        vacuum.x, vacuum.y, 10, 6f, CombatRoom.DUST);
                    float speed = 225 + room.random.nextFloat() * 65;
                    dust.vx = MathUtils.cos(angle) * speed;
                    dust.vy = MathUtils.sin(angle) * speed;
                    dust.ejectionTime = 1.1f;
                    room.exObjects.add(dust);
                }
                vacuum.exStoredDust = 0;
                vacuum.exMeter = 0;
                vacuum.exState = 2;
                vacuum.exStateTime = 1f;
                vacuum.exFlashTime = .38f;
                vacuum.x = Math.min(GameSpace.RIGHT - vacuum.radius, vacuum.x + 40);
            } else if (vacuum.exStateTime <= 0) {
                vacuum.exState = 0;
            }
            return; // No suction while sneezing or recovering.
        }

        vacuum.exObjectTimer -= dt;
        if (vacuum.exObjectTimer <= 0
            && ExEnvironment.count(room, CombatRoom.DUST) < 8) {
            ExEnvironment.spawnDust(room, vacuum);
            vacuum.exObjectTimer = 1.25f;
        }
        moveToward(vacuum, player.x, player.y,
            vacuum.speed * .43f * dt, 205);
        for (int i = room.exObjects.size - 1; i >= 0; i--) {
            CombatRoom.ExObject dust = room.exObjects.get(i);
            if (dust.kind != CombatRoom.DUST || dust.ejectionTime > 0) continue;
            float dx = vacuum.x - dust.x, dy = vacuum.y - dust.y;
            float distance = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
            float step = Math.min(distance, 175 * dt);
            dust.x += dx / distance * step;
            dust.y += dy / distance * step;
            if (distance <= vacuum.radius + dust.radius + step) {
                room.exObjects.removeIndex(i);
                vacuum.exStoredDust++;
                vacuum.exMeter = Math.min(100, vacuum.exMeter + 25);
            }
        }
        float dx = vacuum.x - player.x, dy = vacuum.y - player.y;
        float distance = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        if (distance < 285 && !player.skillPlaying)
            player.moveBy(dx / distance * 82 * dt, dy / distance * 82 * dt);
        if (distance < vacuum.radius + 22 && vacuum.contactTimer <= 0) {
            player.hurt(vacuum.damage);
            vacuum.contactTimer = .8f;
        }
        if (vacuum.exMeter >= 100) {
            vacuum.exState = 1;
            vacuum.exStateTime = .85f;
        }
    }
}
