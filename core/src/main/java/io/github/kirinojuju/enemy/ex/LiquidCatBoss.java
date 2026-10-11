package io.github.kirinojuju.enemy.ex;

// File role: Custom Liquid Cat state and skills: form changes, leaps, slime trails and bullets.

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

public class LiquidCatBoss extends Enemy {

    // Advance the custom cat skill logic for this frame unless stunned.
    @Override
    public void updateBehavior(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
        if (stunTime > 0) return;
        updateSkill(room, dt, player, plan);
    }

    public static final float LEAP_WINDUP = .48f;
    public static final float TRANSFORM_DURATION = .45f;
    public static final float DIVIDE_DURATION = .65f;


    // Create LiquidCatBoss with its starting stats and state for this encounter.
    public LiquidCatBoss(RunPlan plan) {
        super(GameSpace.RIGHT - 75,
            GameSpace.WALK_TOP - 75,
            34,
            380 * plan.healthScale(),
            88 * plan.spScale(),
            120 * plan.speedScale(),
            14 * plan.damageScale(),
            CombatRoom.LIQUID_CAT);

        skillTimer = 1.8f;
        exStateTime = 3.5f;
    }

    // Switch cat forms and handle leap warnings, small-form shots, slime trails and contact damage.
    public void updateSkill(CombatRoom room, float dt, PlayerCharacter player,
                     RunPlan plan) {

        LiquidCatBoss cat = this;

        cat.catMoving = false;

        cat.catFacingRight = player.x >= cat.x;
        cat.exStateTime -= dt;
        if (cat.exStateTime <= 0) {
            cat.exState = cat.exState == 0 ? 1 : 0;
            cat.exStateTime = cat.exState == 1 ? 2.4f : 3.5f;
            cat.radius = cat.exState == 1 ? 16 : 34;
            cat.exMeter = 0;
            cat.exObjectTimer = 0;
            cat.exFlashTime = .35f;
            cat.catTransformTime = TRANSFORM_DURATION;
            cat.catDivideTime = 0;
            cat.skillTimer = cat.exState == 1 ? 1f : 1.4f;
        }
        if (cat.exState == 1) {
            cat.exObjectTimer -= dt;
            if (cat.exObjectTimer <= 0 && room.countExObjects(CombatRoom.SLIME) < 7) {
                room.exObjects.add(new CombatRoom.ExObject(cat.x, cat.y, 25, 2.6f, CombatRoom.SLIME));
                cat.exObjectTimer = .45f;
            }
            float oldX = cat.x, oldY = cat.y;
            room.moveExToward(cat, player.x, player.y, cat.speed * .38f * dt, 8);
            cat.catMoving = Math.abs(cat.x - oldX) + Math.abs(cat.y - oldY) > .1f;
            if (cat.skillTimer <= 0) {
                cat.skillTimer = 1.35f;
                cat.catDivideTime = DIVIDE_DURATION;
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
            cat.exMeter = LEAP_WINDUP;
            cat.exAimX = MathUtils.clamp(player.x,
                GameSpace.LEFT + cat.radius, GameSpace.RIGHT - cat.radius);
            cat.exAimY = MathUtils.clamp(player.y,
                GameSpace.BOTTOM + cat.radius, GameSpace.WALK_TOP - cat.radius);
            return;
        }
        float oldX = cat.x, oldY = cat.y;
        room.moveExToward(cat, player.x, player.y, cat.speed * dt, cat.radius + 24);
        cat.catMoving = Math.abs(cat.x - oldX) + Math.abs(cat.y - oldY) > .1f;
        float dx = player.x - cat.x, dy = player.y - cat.y;
        if (dx * dx + dy * dy <= (cat.radius + 18) * (cat.radius + 18)
            && cat.contactTimer <= 0) {
            player.hurt(cat.damage);
            cat.contactTimer = .8f;
        }
    }

    // Reduce incoming damage while the cat is in its small form.
    public float damageMultiplier() {
        return exState == 1 ? .55f : 1f;
    }

}
