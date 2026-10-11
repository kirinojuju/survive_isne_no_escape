package io.github.kirinojuju;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Renders the Ice Wall cast animation and its sustained barrier. */
final class IceWallSprite {
    private static final float FRAME_TIME = .065f;
    private static final float WIDTH = CombatRoom.ICE_WALL_LENGTH, HEIGHT = 128;
    private final Texture[] frames = new Texture[17];

    IceWallSprite() {
        for (int i = 0; i < frames.length; i++)
            frames[i] = OptionalAssetTexture.load("icewall/icewall/icewall" + (i + 1) + ".png");
    }

    void draw(SpriteBatch batch, CombatRoom combat) {
        if (combat.iceWallTime <= 0) return;
        int frame = Math.min(frames.length - 1,
            (int)(combat.iceWallAnimationTime / FRAME_TIME));
        float alpha = combat.iceWallTime < .5f ? combat.iceWallTime / .5f : 1f;
        batch.setColor(1, 1, 1, alpha);
        batch.draw(frames[frame], combat.iceWallX - WIDTH / 2,
            combat.iceWallY - HEIGHT / 2, WIDTH / 2, HEIGHT / 2,
            WIDTH, HEIGHT, 1, 1, (float)Math.toDegrees(combat.iceWallAngle),
            0, 0, frames[frame].getWidth(), frames[frame].getHeight(), false, false);
        batch.setColor(Color.WHITE);
    }

    void dispose() {
        for (Texture frame : frames) frame.dispose();
    }
}
