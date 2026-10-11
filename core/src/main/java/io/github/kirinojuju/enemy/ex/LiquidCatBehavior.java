package io.github.kirinojuju.enemy.ex;

// File role: Liquid Cat behavior for a generic Enemy, including form changes and leaps.

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.ex.ExBehavior.ExEnvironment;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** The cat alternates between an orbital leap and a compact, splitting form. */
public final class LiquidCatBehavior extends ExBehavior {
    // Run the generic cat's form changes, leaps, slime trail and contact attacks.
    @Override
    protected void act(CombatRoom room, CombatRoom.Enemy cat, float dt,
                       PlayerCharacter player, RunPlan plan) {
        cat.catFacingRight = player.x >= cat.x;
        cat.exStateTime -= dt;
        if (cat.exStateTime <= 0) {
            cat.exState = cat.exState == 0 ? 1 : 0;
            cat.exStateTime = cat.exState == 1 ? 2.4f : 3.5f;
            cat.radius = cat.exState == 1 ? 16 : 34;
            cat.exMeter = 0;
            cat.exObjectTimer = 0;
            cat.exFlashTime = .35f;
            cat.catTransformTime = CombatRoom.CAT_TRANSFORM_DURATION;
            cat.catDivideTime = 0;
            cat.skillTimer = cat.exState == 1 ? 1f : 1.4f;
        }
        if (cat.exState == 1) {
            cat.exObjectTimer -= dt;
            if (cat.exObjectTimer <= 0
                && ExEnvironment.count(room, CombatRoom.SLIME) < 7) {
                room.exObjects.add(new CombatRoom.ExObject(
                    cat.x, cat.y, 25, 2.6f, CombatRoom.SLIME));
                cat.exObjectTimer = .45f;
            }
            float oldX = cat.x, oldY = cat.y;
            moveToward(cat, player.x, player.y, cat.speed * .38f * dt, 8);
            cat.catMoving = Math.abs(cat.x - oldX) + Math.abs(cat.y - oldY) > .1f;
            if (cat.skillTimer <= 0) {
                cat.skillTimer = 1.35f;
                cat.catDivideTime = CombatRoom.CAT_DIVIDE_DURATION;
                float aimX = player.x - cat.x, aimY = player.y - cat.y;
                float distance = Math.max(.001f,
                    (float)Math.sqrt(aimX * aimX + aimY * aimY));
                CombatRoom.Shot bullet = new CombatRoom.Shot(cat.x, cat.y,
                    aimX / distance * 260 * plan.speedScale(),
                    aimY / distance * 260 * plan.speedScale(),
                    9, 8 * plan.damageScale(), 3f, false);
                bullet.liquidCatBullet = true;
                room.addEnemyShot(cat, bullet);
            }
            float dx = player.x - cat.x, dy = player.y - cat.y;
            if (dx * dx + dy * dy < (cat.radius + 22) * (cat.radius + 22)) {
                player.slowTime = Math.max(player.slowTime, 1.1f);
                if (cat.contactTimer <= 0) {
                    player.hurt(5 * plan.damageScale());
                    cat.contactTimer = .85f;
                }
            }
            return;
        }
        if (cat.exMeter > 0) {
            cat.exMeter = Math.max(0, cat.exMeter - dt);
            if (cat.exMeter == 0) {
                cat.x = cat.exAimX;
                cat.y = cat.exAimY;
                cat.exFlashTime = .3f;
                float dx = player.x - cat.x, dy = player.y - cat.y;
                if (dx * dx + dy * dy <= 80 * 80)
                    player.hurt(20 * plan.damageScale());
            }
            return;
        }
        if (cat.skillTimer <= 0) {
            cat.skillTimer = 3.3f;
            cat.exMeter = CombatRoom.CAT_LEAP_WINDUP;
            cat.exAimX = MathUtils.clamp(player.x,
                GameSpace.LEFT + cat.radius, GameSpace.RIGHT - cat.radius);
            cat.exAimY = MathUtils.clamp(player.y,
                GameSpace.BOTTOM + cat.radius, GameSpace.WALK_TOP - cat.radius);
            return;
        }
        float oldX = cat.x, oldY = cat.y;
        moveToward(cat, player.x, player.y, cat.speed * dt, cat.radius + 24);
        cat.catMoving = Math.abs(cat.x - oldX) + Math.abs(cat.y - oldY) > .1f;
        float dx = player.x - cat.x, dy = player.y - cat.y;
        if (dx * dx + dy * dy <= (cat.radius + 18) * (cat.radius + 18)
            && cat.contactTimer <= 0) {
            player.hurt(cat.damage);
            cat.contactTimer = .8f;
        }
    }
}
