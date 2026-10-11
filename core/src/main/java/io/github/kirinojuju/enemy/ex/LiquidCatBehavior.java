package io.github.kirinojuju.enemy.ex;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.ex.ExBehavior.ExEnvironment;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** The cat alternates between an orbital leap and a compact, splitting form. */
public final class LiquidCatBehavior extends ExBehavior {
    public static final float LEAP_WINDUP = .48f;
    public static final float TRANSFORM_DURATION = .45f;
    public static final float DIVIDE_DURATION = .65f;

    @Override
    public float incomingDamageMultiplier(CombatRoom.Enemy cat) {
        return cat.exState == 1 ? .55f : 1f;
    }

    @Override
    protected void act(CombatRoom room, CombatRoom.Enemy cat, float dt,
                       PlayerCharacter player, RunPlan plan) {
        cat.catMoving = false;
        if (cat.catTransformTime > 0) {
            cat.catTransformTime = Math.max(0, cat.catTransformTime - dt);
            return;
        }
        if (cat.catDivideTime > 0) {
            advanceDivide(room, cat, dt, plan);
            return;
        }
        if (cat.exMeter > 0) {
            advanceLeap(room, cat, dt, player, plan);
            return;
        }
        if (cat.exState == 0 && cat.exFlashTime > 0) return;
        cat.catFacingRight = player.x >= cat.x;
        cat.exStateTime -= dt;
        if (cat.exStateTime <= 0) {
            cat.exState = cat.exState == 0 ? 1 : 0;
            cat.exStateTime = cat.exState == 1 ? 2.4f : 3.5f;
            cat.radius = cat.exState == 1 ? 16 : 34;
            cat.exMeter = 0;
            cat.exObjectTimer = 0;
            cat.exFlashTime = 0;
            cat.catTransformTime = LiquidCatBehavior.TRANSFORM_DURATION;
            cat.catDivideTime = 0;
            cat.skillTimer = cat.exState == 1 ? 1f : 1.4f;
            return;
        }
        if (cat.exState == 1) {
            cat.exObjectTimer -= dt;
            if (cat.exObjectTimer <= 0
                && ExEnvironment.count(room, CombatRoom.SLIME) < 7) {
                room.exObjects.add(new CombatRoom.ExObject(
                    cat.x, cat.y, 25, 2.6f, CombatRoom.SLIME));
                cat.exObjectTimer = .45f;
            }
            moveCat(room, cat, player, cat.speed * .38f * dt, cat.radius + 18);
            if (cat.skillTimer <= 0) {
                cat.skillTimer = 1.35f;
                cat.catDivideTime = LiquidCatBehavior.DIVIDE_DURATION;
                cat.exAimX = player.x;
                cat.exAimY = player.y;
                cat.catMoving = false;
                return;
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
        if (cat.skillTimer <= 0) {
            cat.skillTimer = 3.3f;
            cat.exMeter = LiquidCatBehavior.LEAP_WINDUP;
            cat.exAimX = MathUtils.clamp(player.x,
                GameSpace.LEFT + cat.radius, GameSpace.RIGHT - cat.radius);
            cat.exAimY = MathUtils.clamp(player.y,
                GameSpace.BOTTOM + cat.radius, GameSpace.WALK_TOP - cat.radius);
            return;
        }
        moveCat(room, cat, player, cat.speed * dt, cat.radius + 24);
        float dx = player.x - cat.x, dy = player.y - cat.y;
        if (dx * dx + dy * dy <= (cat.radius + 18) * (cat.radius + 18)
            && cat.contactTimer <= 0) {
            player.hurt(cat.damage);
            cat.contactTimer = .8f;
        }
    }

    private void moveCat(CombatRoom room, CombatRoom.Enemy cat, PlayerCharacter player,
                         float step, float stop) {
        float oldX = cat.x, oldY = cat.y;
        moveToward(cat, player.x, player.y, step * (cat.slowTime > 0 ? .48f : 1), stop);
        room.iceWall.stopEnemy(cat, oldX, oldY);
        cat.catMoving = Math.abs(cat.x - oldX) + Math.abs(cat.y - oldY) > .1f;
    }

    private void advanceDivide(CombatRoom room, CombatRoom.Enemy cat, float dt, RunPlan plan) {
        float previous = cat.catDivideTime;
        cat.catDivideTime = Math.max(0, previous - dt);
        // Release once, halfway through the animation, toward the locked point.
        if (previous <= .3f || cat.catDivideTime > .3f) return;
        float dx = cat.exAimX - cat.x, dy = cat.exAimY - cat.y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        if (distance < .001f) { dx = cat.catFacingRight ? 1 : -1; dy = 0; distance = 1; }
        CombatRoom.Shot bullet = new CombatRoom.Shot(cat.x, cat.y,
            dx / distance * 260 * plan.speedScale(), dy / distance * 260 * plan.speedScale(),
            9, 8 * plan.damageScale(), 3f, false);
        bullet.liquidCatBullet = true;
        room.addEnemyShot(cat, bullet);
    }

    private void advanceLeap(CombatRoom room, CombatRoom.Enemy cat, float dt,
                             PlayerCharacter player, RunPlan plan) {
        cat.exMeter = Math.max(0, cat.exMeter - dt);
        if (cat.exMeter > 0) return;
        cat.x = cat.exAimX;
        cat.y = cat.exAimY;
        room.iceWall.resolveEnemy(cat, player.x, player.y, room.obstacles);
        cat.exFlashTime = .3f;
        // Damage stays on the warning marker, even if the wall separates the landing body.
        float dx = player.x - cat.exAimX, dy = player.y - cat.exAimY;
        if (dx * dx + dy * dy <= 80 * 80) player.hurt(20 * plan.damageScale());
    }
}
