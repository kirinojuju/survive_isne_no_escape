package io.github.kirinojuju.render.sprite.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.render.OptionalAssetTexture;

/** Plays the supplied sword-wave animation on a traveling LAN attack. */
public final class LanWaveSprite {
    private static final float FRAME_TIME = .06f;
    private static final float SIZE = 112;
    private final Texture[] frames = new Texture[12];

    public LanWaveSprite() {
        for (int i = 0; i < frames.length; i++)
            frames[i] = OptionalAssetTexture.load("weapons/sword_power/effectsword" + (i + 1) + ".png");
    }

    public void draw(SpriteBatch batch, CombatRoom.Shot shot) {
        int frame = Math.min(frames.length - 1, (int)(shot.animationTime / FRAME_TIME));
        float alpha = shot.life < .12f ? MathUtils.clamp(shot.life / .12f, 0, 1) : 1;
        batch.setColor(1, 1, 1, alpha);
        batch.draw(frames[frame], shot.x - SIZE / 2, shot.y - SIZE / 2,
            SIZE / 2, SIZE / 2, SIZE, SIZE, 1, 1,
            (float)Math.toDegrees(MathUtils.atan2(shot.vy, shot.vx)),
            0, 0, frames[frame].getWidth(), frames[frame].getHeight(), false, false);
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        for (Texture frame : frames) frame.dispose();
    }
}
