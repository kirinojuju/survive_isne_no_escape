package io.github.kirinojuju;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import java.util.Locale;

/** The team's separate Liquid Cat animation and attack frames. */
final class LiquidCatSprite {
    private static final float DRAW_SIZE = 128;
    private static final float SMALL_DRAW_SIZE = 96;
    private static final float BULLET_SIZE = 128;
    private static final String ROOT = "characters/boss_ex/liquid-cat/";

    private final Texture[] idleLeft = loadFrames("idle/liquid_cat_idle_l", 1, 8);
    private final Texture[] idleRight = loadFrames("idle/liquid_cat_idle_r", 1, 9);
    private final Texture[] smallIdle = loadFrames("idle/liquid_cat_new_form", 1, 2);
    private final Texture[] smallWalkLeft = loadFrames("walk/liquid_cat_new_form_walk_l", 1, 4);
    private final Texture[] smallWalkRight = loadFrames("walk/liquid_cat_new_form_walk_r", 1, 4);
    private final Texture[] transform = loadFrames("transform/liquid_cat_transform", 1, 10);
    private final Texture[] divide = loadFrames("divide/liquid_cat_new_form_divide", 1, 13);
    private final Texture[] strike = loadFrames("attack/liquid_orbital_strike_animation", 0, 10);
    private final Texture strikeObject = load("attack/liquid_orbital_strike_object.png");
    private final Texture bullet = load("attack/liquid_bullet.png");

    private static Texture load(String name) {
        return OptionalAssetTexture.load(ROOT + name);
    }

    private static Texture[] loadFrames(String prefix, int first, int count) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++)
            frames[i] = load(prefix + (first == 0
                ? String.format(Locale.ROOT, "%02d", i) : Integer.toString(first + i))
                + ".png");
        return frames;
    }

    private static Texture timedFrame(Texture[] frames, float remaining, float duration) {
        float progress = 1 - MathUtils.clamp(remaining / duration, 0, 1);
        int index = MathUtils.clamp((int)(progress * frames.length), 0, frames.length - 1);
        return frames[index];
    }

    void draw(SpriteBatch batch, CombatRoom.Enemy cat) {
        Texture frame;
        if (cat.catTransformTime > 0) {
            float progress = 1 - MathUtils.clamp(cat.catTransformTime
                / CombatRoom.CAT_TRANSFORM_DURATION, 0, 1);
            int index = MathUtils.clamp((int)(progress * transform.length), 0,
                transform.length - 1);
            frame = transform[cat.exState == 1 ? index : transform.length - 1 - index];
        } else if (cat.exState == 1 && cat.catDivideTime > 0) {
            frame = timedFrame(divide, cat.catDivideTime, CombatRoom.CAT_DIVIDE_DURATION);
        } else if (cat.exState == 0 && cat.exMeter > 0) {
            frame = timedFrame(strike, cat.exMeter, CombatRoom.CAT_LEAP_WINDUP);
        } else if (cat.exState == 1) {
            if (cat.catMoving) {
                Texture[] walking = cat.catFacingRight ? smallWalkRight : smallWalkLeft;
                frame = walking[(int)(cat.animationTime / .11f) % walking.length];
            } else frame = smallIdle[(int)(cat.animationTime / .22f) % smallIdle.length];
        } else {
            Texture[] idle = cat.catFacingRight ? idleRight : idleLeft;
            frame = idle[(int)(cat.animationTime / .12f) % idle.length];
        }
        float size = cat.exState == 1 && cat.catTransformTime <= 0
            ? SMALL_DRAW_SIZE : DRAW_SIZE;
        batch.draw(frame, cat.x - size / 2, cat.y - size / 2,
            size, size);
        if (cat.exState == 0 && cat.exMeter > 0)
            batch.draw(strikeObject, cat.exAimX - DRAW_SIZE / 2,
                cat.exAimY - DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE);
    }

    void drawBullet(SpriteBatch batch, CombatRoom.Shot shot) {
        batch.draw(bullet, shot.x - BULLET_SIZE / 2, shot.y - BULLET_SIZE / 2,
            BULLET_SIZE, BULLET_SIZE);
    }

    void dispose() {
        dispose(idleLeft);
        dispose(idleRight);
        dispose(smallIdle);
        dispose(smallWalkLeft);
        dispose(smallWalkRight);
        dispose(transform);
        dispose(divide);
        dispose(strike);
        strikeObject.dispose();
        bullet.dispose();
    }

    private static void dispose(Texture[] frames) {
        for (Texture frame : frames) frame.dispose();
    }
}
