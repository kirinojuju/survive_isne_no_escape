package io.github.kirinojuju.render;

import io.github.kirinojuju.enemy.boss.BossBody;

// File role: Loads and draws the Sword EX idle and attack animation frames.

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.ex.InsecureSwordBehavior;

/** The team's five idle frames and five attack frames, played at a crisp 2x scale. */
public final class InsecureSwordSprite {
    private static final String ROOT = "characters/boss_ex/insecure-sword/";
    private static final float DRAW_SIZE = BossBody.SWORD_DRAW_SIZE;
    private static final float IDLE_FRAME_TIME = .16f;
    private final Texture[] idle = loadFrames("idle_");
    private final Texture[] attack = loadFrames("attack_");

    // Load the numbered image files used by this animation.
    private static Texture[] loadFrames(String prefix) {
        Texture[] frames = new Texture[5];
        for (int i = 0; i < frames.length; i++)
            frames[i] = OptionalAssetTexture.load(ROOT + prefix + (i + 1) + ".png");
        return frames;
    }

    // Draw InsecureSword artwork using the current animation and combat state.
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
        OptionalAssetTexture.applyBodyBounds(sword, frame, false);
        batch.draw(frame, sword.x - DRAW_SIZE / 2, sword.y + sword.radius - DRAW_SIZE, DRAW_SIZE, DRAW_SIZE);
    }

    // Release textures and drawing resources owned by this sprite.
    public void dispose() {
        for (Texture frame : idle) frame.dispose();
        for (Texture frame : attack) frame.dispose();
    }
}
