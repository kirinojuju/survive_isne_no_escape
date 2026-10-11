package io.github.kirinojuju.render;

import io.github.kirinojuju.enemy.boss.BossBody;

// File role: Loads and draws Ken's body, attack frames, bomb warnings and explosions.

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import io.github.kirinojuju.enemy.boss.KenBoss;
import java.util.Locale;

public class KenSprite {
    private static final String ROOT = "characters/boss/Talgorithm(Ken)/";
    private static final float DRAW_SIZE = BossBody.KEN_DRAW_SIZE;

    private final Texture[] bodyFrames = loadFrames("kenBody", 1, 17, false);
    private final Texture[] bombFrames = loadFrames("kenBomb", 7);
    private final Texture[] warningBomb = loadFrames("warningBomb", 3);
    private static final int[] PING_PONG = {0, 1, 2, 2, 1, 0};

    // Load a texture from this sprite's asset folder.
    private static Texture load(String name) {
        return OptionalAssetTexture.load(ROOT + name);
    }

    // Load Ken's animation frames; the overload supports padded and unpadded filenames.
    private static Texture[] loadFrames(String prefix, int count) {
        return loadFrames(prefix, 0, count, true);
    }

    // Load Ken's animation frames; the overload supports padded and unpadded filenames.
    private static Texture[] loadFrames(String prefix, int first, int count,
                                        boolean zeroPadded) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++) {
            String number = zeroPadded
                ? String.format(Locale.ROOT, "%02d", first + i)
                : Integer.toString(first + i);
            frames[i] = load(prefix + number + ".png");
        }
        return frames;
    }

    // Draw Ken artwork using the current animation and combat state.
    public void draw(SpriteBatch batch, KenBoss boss) {
        Texture frame;
        if (boss.bombAttackTime > 0) {
            float progress = 1f - boss.bombAttackTime / KenBoss.BOMB_ATTACK_DURATION;
            int index = Math.min(16, 2 + (int)(progress * 15));
            frame = bodyFrames[index];
        } else {
            frame = bodyFrames[(int)(boss.animationTime / .3f) % 2];
        }
        OptionalAssetTexture.applyBodyBounds(boss, frame, false);
        batch.draw(frame,
            boss.x - DRAW_SIZE / 2,
            // Keep the top below the HP bar at boss.y + boss.radius + 8.
            boss.y + boss.radius - DRAW_SIZE,
            DRAW_SIZE, DRAW_SIZE);
    }

    // Draw each bomb's warning animation or its explosion frames from its remaining timer.
    public void drawBombWarnings(SpriteBatch batch, KenBoss boss) {
        float size = KenBoss.BOMB_RADIUS * 2;

        for (KenBoss.Bomb bomb : boss.bombs) {
            if (bomb.exploded) {
                float progress = 1 - bomb.blastTime / KenBoss.BLAST_TIME;
                int index = Math.max(0, Math.min(
                    bombFrames.length - 1,
                    (int)(progress * bombFrames.length)));

                batch.draw(bombFrames[index],
                    bomb.x - size / 2, bomb.y - size / 2,
                    size, size);
            } else {
                int step = (int)((KenBoss.WARNING_TIME - bomb.warningTime) / .12f);
                int index = PING_PONG[step % PING_PONG.length];

                batch.draw(warningBomb[index],
                    bomb.x - size / 2, bomb.y - size / 2,
                    size, size);
            }
        }
    }

    // Release textures and drawing resources owned by this sprite.
    public void dispose() {
        for (Texture frame : bodyFrames) {
            frame.dispose();
        }
        for (Texture frame : warningBomb) {
            frame.dispose();
        }
        for (Texture frame : bombFrames) {
            frame.dispose();
        }
    }
}
