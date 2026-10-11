package io.github.kirinojuju.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.world.IceWall;

/** Draws the player's 17-frame wall animation along its matching collision footprint. */
public final class IceWallSprite {
    private static final float FRAME_TIME = .065f;
    private static final float WIDTH = IceWall.LENGTH;
    private static final float HEIGHT = 128;
    private final Texture[] frames = new Texture[17];

    public IceWallSprite() {
        for (int i = 0; i < frames.length; i++)
            frames[i] = OptionalAssetTexture.load(
                "icewall/icewall/icewall" + (i + 1) + ".png");
    }

    public void draw(SpriteBatch batch, IceWall wall) {
        if (wall.time <= 0) return;
        int frame = Math.min(frames.length - 1, (int)(wall.animationTime / FRAME_TIME));
        float alpha = wall.time < .5f ? wall.time / .5f : 1f;
        batch.setColor(1, 1, 1, alpha);
        batch.draw(frames[frame], wall.x - WIDTH / 2, wall.y - HEIGHT / 2,
            WIDTH / 2, HEIGHT / 2, WIDTH, HEIGHT, 1, 1,
            (float)Math.toDegrees(wall.angle), 0, 0,
            frames[frame].getWidth(), frames[frame].getHeight(), false, false);
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        for (Texture frame : frames) frame.dispose();
    }
}
