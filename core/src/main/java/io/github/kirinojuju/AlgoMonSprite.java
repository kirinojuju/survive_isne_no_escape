package io.github.kirinojuju;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Locale;

final class AlgoMonSprite {
    private static final String ROOT = "characters/monsters/monster_algo/";
    private static final float DRAW_SIZE = 92;

    private final Texture[] idle = loadFrames("gigiAlgo", 2);
    private final Texture ball = load("ballgigiAlgo00.png");

    private static Texture load(String name) {
        return OptionalAssetTexture.load(ROOT + name);
    }

    private static Texture[] loadFrames(String prefix, int count) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++) {
            frames[i] = load(prefix
                + String.format(Locale.ROOT, "%02d", i) + ".png");
        }
        return frames;
    }

    void draw(SpriteBatch batch, Enemy monster) {
        Texture frame = idle[(int)(monster.animationTime / .2f) % idle.length];
        batch.draw(frame,
            monster.x - DRAW_SIZE / 2,
            monster.y - DRAW_SIZE / 2,
            DRAW_SIZE, DRAW_SIZE);
    }

    void drawBall(SpriteBatch batch, CombatRoom.Shot shot) {
        float size = shot.radius * 4;
        batch.draw(ball,
            shot.x - size / 2,
            shot.y - size / 2,
            size, size);
    }

    void dispose() {
        for (Texture frame : idle) frame.dispose();
        ball.dispose();
    }
}
