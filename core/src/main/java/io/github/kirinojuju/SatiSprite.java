package io.github.kirinojuju;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import java.util.Locale;

/** Sati's C++ element, transformation animation, and thrown spear art. */
final class SatiSprite {
    private static final String ROOT = "characters/boss/Tprogram(Sati)/skill/cpp_element/";
    private static final float ELEMENT_SIZE = 92;
    private static final float SPEAR_SIZE = 96;
    private final Texture[] idle = loadFrames("cpp_idle", 6);
    private final Texture[] transform = loadFrames("cpp_transform", 16);
    private final Texture spear = load("cpp_spear00.png");
    private final TextureRegion spearRegion = new TextureRegion(spear);

    private static Texture load(String name) {
        Texture texture = new Texture(Gdx.files.internal(ROOT + name));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    private static Texture[] loadFrames(String prefix, int count) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++)
            frames[i] = load(prefix + String.format(Locale.ROOT, "%02d", i) + ".png");
        return frames;
    }

    void drawElement(SpriteBatch batch, CombatRoom.Enemy boss) {
        Texture frame;
        if (boss.satiCharge > 0) {
            float progress = 1 - boss.satiCharge / CombatRoom.SATI_CHARGE_DURATION;
            int index = MathUtils.clamp((int)(progress * transform.length),
                0, transform.length - 1);
            frame = transform[index];
        } else {
            frame = idle[(int)(boss.animationTime / .12f) % idle.length];
        }
        float x = CombatRoom.satiElementX(boss);
        float y = CombatRoom.satiElementY(boss);
        batch.draw(frame, x - ELEMENT_SIZE / 2, y - ELEMENT_SIZE / 2,
            ELEMENT_SIZE, ELEMENT_SIZE);
    }

    void drawSpear(SpriteBatch batch, CombatRoom.Shot shot) {
        float rotation = MathUtils.atan2(shot.vy, shot.vx)
            * MathUtils.radiansToDegrees + 90;
        batch.draw(spearRegion, shot.x - SPEAR_SIZE / 2,
            shot.y - SPEAR_SIZE / 2, SPEAR_SIZE / 2, SPEAR_SIZE / 2,
            SPEAR_SIZE, SPEAR_SIZE, 1, 1, rotation);
    }

    void dispose() {
        for (Texture frame : idle) frame.dispose();
        for (Texture frame : transform) frame.dispose();
        spear.dispose();
    }
}
