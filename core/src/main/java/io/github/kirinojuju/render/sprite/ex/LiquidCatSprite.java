package io.github.kirinojuju.render.sprite.ex;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.boss.BossBody;
import io.github.kirinojuju.enemy.ex.LiquidCatBoss;
import io.github.kirinojuju.render.sprite.EnemySprite;
import io.github.kirinojuju.world.GameSpace;

/** The team's separate Liquid Cat animation and attack frames. */
public class LiquidCatSprite extends EnemySprite {
    private static final float DRAW_SIZE = BossBody.CAT_DRAW_SIZE;
    private static final float SMALL_DRAW_SIZE = BossBody.SMALL_CAT_DRAW_SIZE;
    private static final String ROOT = "characters/boss_ex/liquid-cat/";

    private final Texture[] idleLeft = loadIdle(true);
    private final Texture[] idleRight = loadIdle(false);
    private final Texture[] smallIdle = loadFrames("idle/liquid_cat_new_form", 1, 2);
    private final Texture[] smallWalkLeft = loadFrames("walk/liquid_cat_new_form_walk_l", 1, 4);
    private final Texture[] smallWalkRight = loadFrames("walk/liquid_cat_new_form_walk_r", 1, 4);
    private final Texture[] transform = loadFrames("transform/liquid_cat_transform", 1, 10);
    private final Texture[] divide = loadFrames("divide/liquid_cat_new_form_divide", 1, 13);
    private final Texture[] strike = loadFrames("attack/liquid_orbital_strike_animation", 0, 10);
    private final Texture strikeObject = load(ROOT + "attack/liquid_orbital_strike_object.png");
    private final Texture bullet = load(ROOT + "attack/liquid_bullet.png");
    private final TextureRegion bulletRegion = new TextureRegion(bullet, 27, 26, 10, 10);

    public LiquidCatSprite() { super(CombatRoom.LIQUID_CAT); }

    private Texture[] loadIdle(boolean left) {
        String updated = left ? "idle/liquid_idle2L_" : "idle/liquid_idle2_";
        if (Gdx.files.internal(ROOT + updated + "00.png").exists()) return loadFrames(updated, 0, 10);
        return loadFrames(left ? "idle/liquid_cat_idle_l" : "idle/liquid_cat_idle_r", 1, left ? 8 : 9);
    }

    private Texture[] loadFrames(String prefix, int first, int count) {
        return frames(ROOT + prefix + (first == 0 ? "%02d.png" : "%d.png"), first, count, true);
    }

    private static Texture timedFrame(Texture[] frames, float remaining, float duration) {
        float progress = 1 - MathUtils.clamp(remaining / duration, 0, 1);
        int index = MathUtils.clamp((int)(progress * frames.length), 0, frames.length - 1);
        return frames[index];
    }

    @Override public void draw(SpriteBatch batch, Enemy enemy, float playerX) {
        LiquidCatBoss cat = (LiquidCatBoss) enemy;
        Texture frame;
        boolean flip = !cat.catFacingRight;
        if (cat.catTransformTime > 0) {
            float progress = 1 - MathUtils.clamp(cat.catTransformTime
                / LiquidCatBoss.TRANSFORM_DURATION, 0, 1);
            int index = MathUtils.clamp((int)(progress * transform.length), 0,
                transform.length - 1);
            frame = transform[index];
        } else if (cat.exState == 1 && cat.catDivideTime > 0) {
            frame = timedFrame(divide, cat.catDivideTime, LiquidCatBoss.DIVIDE_DURATION);
        } else if (cat.exState == 0 && cat.exMeter > LiquidCatBoss.ORBITAL_FALL) {
            frame = timedFrame(strike, cat.exMeter - LiquidCatBoss.ORBITAL_FALL,
                LiquidCatBoss.ORBITAL_WARNING);
        } else if (cat.exState == 1) {
            if (cat.catMoving) {
                Texture[] walking = cat.catFacingRight ? smallWalkRight : smallWalkLeft;
                frame = walking[(int)(cat.animationTime / .11f) % walking.length];
                flip = false;
            } else frame = smallIdle[(int)(cat.animationTime / .22f) % smallIdle.length];
        } else {
            Texture[] idle = cat.catFacingRight ? idleRight : idleLeft;
            frame = idle[(int)(cat.animationTime / .12f) % idle.length];
            flip = false;
        }
        float size = cat.exState == 1 ? SMALL_DRAW_SIZE : DRAW_SIZE;
        if (cat.catTransformTime > 0) {
            float progress = 1 - cat.catTransformTime / LiquidCatBoss.TRANSFORM_DURATION;
            size = MathUtils.lerp(DRAW_SIZE, SMALL_DRAW_SIZE, progress);
        }
        if (LiquidCatBoss.isCopy(cat)) batch.setColor(.7f, .9f, 1, .8f);
        drawBody(batch, cat, frame, size, flip);
        batch.setColor(Color.WHITE);
        drawOrbital(batch, cat);
    }

    private void drawOrbital(SpriteBatch batch, LiquidCatBoss cat) {
        if (cat.exMeter > LiquidCatBoss.ORBITAL_FALL) return;
        if (cat.exMeter <= 0 && cat.exFlashTime <= 0) return;
        float y = cat.exAimY;
        if (cat.exMeter > 0) {
            float progress = 1 - cat.exMeter / LiquidCatBoss.ORBITAL_FALL;
            y = MathUtils.lerp(GameSpace.TOP + DRAW_SIZE,
                cat.exAimY, progress * progress);
        } else {
            batch.setColor(1, 1, 1, cat.exFlashTime / LiquidCatBoss.ORBITAL_RECOVERY);
        }
        batch.draw(strikeObject, cat.exAimX - DRAW_SIZE / 2, y - DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE);
        batch.setColor(Color.WHITE);
    }

    public void drawBullet(SpriteBatch batch, CombatRoom.Shot shot) {
        float size = shot.radius * 2;
        batch.draw(bulletRegion, shot.x - shot.radius, shot.y - shot.radius, size, size);
    }

}
