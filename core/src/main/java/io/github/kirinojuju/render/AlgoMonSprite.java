package io.github.kirinojuju.render;

// File role: Loads and draws Algorithm monster frames and its projectile artwork.

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import java.util.Locale;

public class AlgoMonSprite {
    private static final String ROOT = "characters/monsters/monster_algo/";
    private static final float DRAW_SIZE = 92;

    private final Texture[] idle = loadFrames("gigiAlgo", 2);
    private final Texture ball = load("ballgigiAlgo00.png");

    // Load a texture from this sprite's asset folder.
    private static Texture load(String name) {
        return OptionalAssetTexture.load(ROOT + name);
    }

    // Load the numbered image files used by this animation.
    private static Texture[] loadFrames(String prefix, int count) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++) {
            frames[i] = load(prefix
                + String.format(Locale.ROOT, "%02d", i) + ".png");
        }
        return frames;
    }

    // Draw AlgoMon artwork using the current animation and combat state.
    public void draw(SpriteBatch batch, Enemy monster) {
        Texture frame = idle[(int)(monster.animationTime / .2f) % idle.length];
        batch.draw(frame,
            monster.x - DRAW_SIZE / 2,
            monster.y + monster.radius - DRAW_SIZE,
            DRAW_SIZE, DRAW_SIZE);
    }

    // Draw the Algorithm monster's projectile artwork.
    public void drawBall(SpriteBatch batch, CombatRoom.Shot shot) {
        float size = shot.radius * 4;
        batch.draw(ball,
            shot.x - size / 2,
            shot.y - size / 2,
            size, size);
    }

    // Release textures and drawing resources owned by this sprite.
    public void dispose() {
        for (Texture frame : idle) frame.dispose();
        ball.dispose();
    }
}
