package io.github.kirinojuju.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.ex.InsecureSwordBehavior;

/** The team's five idle frames and five attack frames, played at a crisp 2x scale. */
public final class InsecureSwordSprite {
    private static final String ROOT = "characters/boss_ex/insecure-sword/";
    private static final float SIZE = 128;
    private static final float IDLE_FRAME_TIME = .16f;
    private final Texture[] idle = loadFrames("idle_");
    private final Texture[] attack = loadFrames("attack_");

    private static Texture[] loadFrames(String prefix) {
        Texture[] frames = new Texture[5];
        for (int i = 0; i < frames.length; i++)
            frames[i] = OptionalAssetTexture.load(ROOT + prefix + (i + 1) + ".png");
        return frames;
    }

    public void draw(SpriteBatch batch, CombatRoom.Enemy sword) {
        Texture frame;
        if (sword.swordAttackTime > 0) {
            float duration = sword.exState == 3 ? InsecureSwordBehavior.WHIFF_DURATION
                : InsecureSwordBehavior.SLASH_ANIMATION_DURATION;
            float progress = 1 - sword.swordAttackTime / duration;
            int index = MathUtils.clamp((int)(progress * attack.length), 0, attack.length - 1);
            frame = attack[index];
        } else {
            frame = idle[(int)(sword.animationTime / IDLE_FRAME_TIME) % idle.length];
        }
        batch.draw(frame, sword.x - SIZE / 2, sword.y - SIZE / 2, SIZE, SIZE);
    }

    public void dispose() {
        for (Texture frame : idle) frame.dispose();
        for (Texture frame : attack) frame.dispose();
    }
}
