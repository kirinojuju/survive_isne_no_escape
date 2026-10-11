package io.github.kirinojuju.render;

import io.github.kirinojuju.enemy.boss.BossBody;

// File role: Loads and draws Sati's body, element transformation and spear artwork.

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import java.util.Locale;

/** Sati's C++ element, transformation animation, and thrown spear art. */
public final class SatiSprite {
    private static final String ROOT = "characters/boss/Tprogram(Sati)/skill/cpp_element/";
    private static final float ELEMENT_SIZE = 92;
    private static final float DRAW_SIZE = BossBody.SATI_DRAW_SIZE;
    private final Texture[] body = OptionalAssetTexture.loadNumberedFrames(ROOT, "satiBody", 7);
    private final Texture[] attack = OptionalAssetTexture.loadNumberedFrames(ROOT, "satiActack", 10);
    private static final float SPEAR_WIDTH = 192;
    private static final float SPEAR_LENGTH = 192;
    private final Texture[] idle = loadFrames("cpp_idle", 6);
    private final Texture[] transform = loadFrames("cpp_transform", 16);
    private final Texture spear = load("cpp_spear00.png");
    private final TextureRegion spearRegion = new TextureRegion(spear);

    // Load a texture from this sprite's asset folder.
    private static Texture load(String name) {
        return OptionalAssetTexture.load(ROOT + name);
    }

    // Load the numbered image files used by this animation.
    private static Texture[] loadFrames(String prefix, int count) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++)
            frames[i] = load(prefix + String.format(Locale.ROOT, "%02d", i) + ".png");
        return frames;
    }

    // Draw Sati's idle or transforming element beside the boss.
    public void drawElement(SpriteBatch batch, CombatRoom.Enemy boss) {
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

    // Check whether body artwork is available before replacing the shape placeholder.
    public boolean hasBody() { return OptionalAssetTexture.frame(body, 0) != null; }

    // Draw Sati's body using idle frames or the current charge animation.
    public void drawBody(SpriteBatch batch, CombatRoom.Enemy boss) {
        Texture frame;
        if (boss.satiCharge > 0) {
            float progress = 1 - boss.satiCharge / CombatRoom.SATI_CHARGE_DURATION;
            frame = OptionalAssetTexture.frame(attack, (int)(progress * attack.length));
        } else {
            frame = OptionalAssetTexture.frame(body, (int)(boss.animationTime / .12f) % body.length);
        }
        if (frame == null) frame = OptionalAssetTexture.frame(body, 0);
        if (frame == null) return;
        batch.draw(frame, boss.x - DRAW_SIZE / 2, boss.y + boss.radius - DRAW_SIZE,
            DRAW_SIZE, DRAW_SIZE);
    }

    // Rotate and draw the spear image along the projectile's movement direction.
    public void drawSpear(SpriteBatch batch, CombatRoom.Shot shot) {
        float rotation = MathUtils.atan2(shot.vy, shot.vx)
            * MathUtils.radiansToDegrees + 90;
        batch.draw(spearRegion, shot.x - SPEAR_WIDTH / 2,
            shot.y - SPEAR_LENGTH / 2, SPEAR_WIDTH / 2, SPEAR_LENGTH / 2,
            SPEAR_WIDTH, SPEAR_LENGTH, 1, 1, rotation);
    }

    // Release textures and drawing resources owned by this sprite.
    public void dispose() {
        OptionalAssetTexture.disposeFrames(body);
        OptionalAssetTexture.disposeFrames(attack);
        for (Texture frame : idle) frame.dispose();
        for (Texture frame : transform) frame.dispose();
        spear.dispose();
    }
}
