package io.github.kirinojuju.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.world.IceWall;

/** Upright ice clusters follow the collision line without rotating their height into the floor. */
public final class IceWallSprite {
    private static final float FRAME_TIME = .065f;
    private static final float SCALE = 1.5f;
    private final Texture[] frames = new Texture[10];

    public IceWallSprite() {
        for (int i = 0; i < frames.length; i++)
            frames[i] = OptionalAssetTexture.load("weapons/icewall/icewall" + (i + 1) + ".png");
    }

    public void draw(SpriteBatch batch, IceWall wall) {
        if (wall.time <= 0) return;
        int frame = Math.min(frames.length - 1, (int)(wall.animationTime / FRAME_TIME));
        float alpha = wall.time < .5f ? wall.time / .5f : 1f;
        batch.setColor(1, 1, 1, alpha);
        float alongX = MathUtils.cos(wall.angle), alongY = MathUtils.sin(wall.angle);
        float footprint = (Math.abs(alongX) * 52 + Math.abs(alongY) * 22) * SCALE;
        float span = IceWall.LENGTH - footprint;
        // Far pieces first: every piece grows upward from its ground point.
        for (int i = 0; i < 5; i++) {
            float offset = span * (.5f - i / 4f) * (alongY < 0 ? -1 : 1);
            batch.draw(frames[frame], wall.x + alongX * offset - 33 * SCALE,
                wall.y + alongY * offset - 16 * SCALE, 64 * SCALE, 64 * SCALE);
        }
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        for (Texture frame : frames) frame.dispose();
    }
}
