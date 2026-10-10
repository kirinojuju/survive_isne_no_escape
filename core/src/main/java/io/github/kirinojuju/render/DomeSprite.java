package io.github.kirinojuju.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.boss.DomeBossBehavior;
import io.github.kirinojuju.world.GameSpace;

/** TDome's exported character frames and lightning/laser effects. */
public final class DomeSprite {
    private static final String ROOT = "characters/boss/Tnetwork(Dome)/";
    private static final float SIZE = 128;
    private static final float PREPARE_DURATION = .5f;
    private final Texture[] prepare = loadFrames("Casting_state_loop", 5);
    private final Texture[] lightningCast = loadFrames("Casting_lighting_loop", 6);
    private final Texture[] lightningRecovery = loadFrames("Recovery_Lighting", 5);
    private final Texture[] laserCast = loadFrames("Laser_casting_loop", 19);
    private final Texture[] laserSide = loadFrames("Laser_shooting_", 5);
    private final Texture[] laserDown = loadFrames("Laser_shooting_down_loop", 5);
    private final Texture[] lightning = loadFrames("Elements/Lighting_strom", 4);
    private final Texture[] laser = loadFrames("Elements/Laser", 4);
    private final TextureRegion[] laserRegions = regions(laser);

    private static Texture[] loadFrames(String prefix, int count) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++)
            frames[i] = OptionalAssetTexture.load(ROOT + prefix + (i + 1) + ".png");
        return frames;
    }

    private static TextureRegion[] regions(Texture[] textures) {
        TextureRegion[] result = new TextureRegion[textures.length];
        for (int i = 0; i < textures.length; i++) result[i] = new TextureRegion(textures[i]);
        return result;
    }

    private static int frameIndex(int count, float elapsed, float duration) {
        return MathUtils.clamp((int)(elapsed / duration * count), 0, count - 1);
    }

    public void draw(SpriteBatch batch, CombatRoom.Enemy boss) {
        DomeBossBehavior.State state = boss.dome;
        Texture frame = characterFrame(state);
        boolean flip = state.facingRight && !state.downward;
        batch.draw(frame, boss.x + (flip ? SIZE / 2 : -SIZE / 2),
            boss.y - SIZE / 2, flip ? -SIZE : SIZE, SIZE);
    }

    private Texture characterFrame(DomeBossBehavior.State state) {
        switch (state.phase) {
            case LIGHTNING_CHARGE:
                if (state.elapsed < PREPARE_DURATION)
                    return frame(prepare, state.elapsed, PREPARE_DURATION);
                return frame(lightningCast, state.elapsed - PREPARE_DURATION,
                    DomeBossBehavior.LIGHTNING_CHARGE - PREPARE_DURATION);
            case LIGHTNING_STRIKE:
                return lightningCast[lightningCast.length - 1];
            case LASER_CHARGE:
                return frame(laserCast, state.elapsed, DomeBossBehavior.LASER_CHARGE);
            case LASER_FIRE:
                Texture[] shooting = state.downward ? laserDown : laserSide;
                return frame(shooting, state.elapsed, DomeBossBehavior.LASER_FIRE);
            case RECOVERY:
                if (!state.castingLaser)
                    return frame(lightningRecovery, state.elapsed, DomeBossBehavior.RECOVERY);
                int index = frameIndex(prepare.length, state.elapsed, DomeBossBehavior.RECOVERY);
                return prepare[prepare.length - 1 - index];
            default:
                return prepare[0];
        }
    }

    private static Texture frame(Texture[] frames, float elapsed, float duration) {
        return frames[frameIndex(frames.length, elapsed, duration)];
    }

    public void drawEffect(SpriteBatch batch, CombatRoom.Enemy boss) {
        DomeBossBehavior.State state = boss.dome;
        if (state.phase == DomeBossBehavior.Phase.LIGHTNING_STRIKE) drawLightning(batch, state);
        else if (state.phase == DomeBossBehavior.Phase.LASER_FIRE) drawLaser(batch, state);
    }

    private void drawLightning(SpriteBatch batch, DomeBossBehavior.State state) {
        Texture frame = frame(lightning, state.elapsed, DomeBossBehavior.LIGHTNING_STRIKE);
        float width = DomeBossBehavior.LIGHTNING_RADIUS * 2;
        batch.draw(frame, state.aimX - width / 2, state.aimY - 8,
            width, GameSpace.TOP - state.aimY + 8);
    }

    private void drawLaser(SpriteBatch batch, DomeBossBehavior.State state) {
        float dx = state.endX - state.originX, dy = state.endY - state.originY;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        float width = DomeBossBehavior.LASER_HALF_WIDTH * 2;
        float rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees + 90;
        TextureRegion frame = laserRegions[frameIndex(laserRegions.length,
            state.elapsed, DomeBossBehavior.LASER_FIRE)];
        batch.draw(frame, state.originX - width / 2, state.originY - length,
            width / 2, length, width, length, 1, 1, rotation);
    }

    public void dispose() {
        for (Texture[] frames : new Texture[][] {prepare, lightningCast, lightningRecovery,
            laserCast, laserSide, laserDown, lightning, laser})
            for (Texture frame : frames) frame.dispose();
    }
}
