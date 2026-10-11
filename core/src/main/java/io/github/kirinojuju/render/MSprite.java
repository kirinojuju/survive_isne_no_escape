package io.github.kirinojuju.render;

import io.github.kirinojuju.enemy.boss.BossBody;

// File role: Loads and draws M's idle and firing animation frames.

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import io.github.kirinojuju.combat.CombatRoom.Enemy;

public class MSprite {
    private static final String ROOT = "characters/boss/Toop(M)/";
    private static final float DRAW_SIZE = BossBody.M_DRAW_SIZE;

    private final Texture[] body = OptionalAssetTexture.loadNumberedFrames(ROOT, "mBody", 7);
    private final Texture[] attack = OptionalAssetTexture.loadNumberedFrames(ROOT, "mActack", 8);

    // Draw M artwork using the current animation and combat state.
    public void draw(SpriteBatch batch, Enemy boss) {
        Texture frame = boss.attackAnimationTime > 0
            ? OptionalAssetTexture.frame(attack, (int)((1 - boss.attackAnimationTime / .4f) * attack.length))
            : OptionalAssetTexture.frame(body, (int)(boss.animationTime / .12f) % body.length);
        if (frame == null) frame = OptionalAssetTexture.frame(body, 0);
        if (frame == null) return;
        OptionalAssetTexture.applyBodyBounds(boss, frame, false);
        batch.draw(frame,
            boss.x - DRAW_SIZE / 2,
            boss.y + boss.radius - DRAW_SIZE,
            DRAW_SIZE, DRAW_SIZE);
    }

    // Release textures and drawing resources owned by this sprite.
    public void dispose() {
        OptionalAssetTexture.disposeFrames(body);
        OptionalAssetTexture.disposeFrames(attack);
    }
}
