package io.github.kirinojuju.render.sprite.boss;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.boss.BossBody;
import io.github.kirinojuju.enemy.boss.DomeBoss;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.render.OptionalAssetTexture;
import io.github.kirinojuju.render.sprite.EnemySprite;
import io.github.kirinojuju.world.GameSpace;

/** TDome's exported character frames and lightning/laser effects. */
public class DomeSprite extends EnemySprite {
    private static final String ROOT = "characters/boss/Tnetwork(Dome)/";
    private static final float DRAW_SIZE = BossBody.DOME_DRAW_SIZE;
    private static final float PREPARE_DURATION = .5f;
    private final Texture[] prepare = frames(ROOT + "Casting_state_loop%d.png", 1, 5, false);
    private final Texture[] lightningCast = frames(ROOT + "Casting_lighting_loop%d.png", 1, 6, false);
    private final Texture[] lightningRecovery = frames(ROOT + "Recovery_Lighting%d.png", 1, 5, false);
    private final Texture[] laserCast = frames(ROOT + "Laser_casting_loop%d.png", 1, 19, false);
    private final Texture[] laserSide = frames(ROOT + "Laser_shooting_%d.png", 1, 5, false);
    private final Texture[] laserDown = frames(ROOT + "Laser_shooting_down_loop%d.png", 1, 5, false);
    private final Texture[] lightning = frames(ROOT + "Elements/Lighting_strom%d.png", 1, 4, false);
    private final Texture[] laser = frames(ROOT + "Elements/Laser%d.png", 1, 4, false);
    private final TextureRegion[] laserRegions = regions(laser);

    public DomeSprite() { super(RunPlan.DOME_ID); }
    @Override public boolean hasBody() { return OptionalAssetTexture.frame(prepare, 0) != null; }

    private static TextureRegion[] regions(Texture[] textures) {
        TextureRegion[] result = new TextureRegion[textures.length];
        for (int i = 0; i < textures.length; i++)
            if (textures[i] != null) result[i] = new TextureRegion(textures[i]);
        return result;
    }

    private static int frameIndex(int count, float elapsed, float duration) {
        return MathUtils.clamp((int)(elapsed / duration * count), 0, count - 1);
    }

    @Override public void draw(SpriteBatch batch, Enemy boss, float playerX) {
        DomeBoss.State state = ((DomeBoss) boss).dome();
        Texture frame = characterFrame(state);
        if (frame == null) return;
        boolean flip = state.facingRight && !state.downward;
        drawBody(batch, boss, frame, DRAW_SIZE, flip);
    }

    private Texture characterFrame(DomeBoss.State state) {
        switch (state.phase) {
            case LIGHTNING_CHARGE:
                if (state.elapsed < PREPARE_DURATION)
                    return frame(prepare, state.elapsed, PREPARE_DURATION);
                return frame(lightningCast, state.elapsed - PREPARE_DURATION,
                    DomeBoss.LIGHTNING_CHARGE - PREPARE_DURATION);
            case LIGHTNING_STRIKE:
                return lightningCast[lightningCast.length - 1];
            case LASER_CHARGE:
                return frame(laserCast, state.elapsed, DomeBoss.LASER_CHARGE);
            case LASER_FIRE:
                Texture[] shooting = state.downward ? laserDown : laserSide;
                return frame(shooting, state.elapsed, DomeBoss.LASER_FIRE);
            case RECOVERY:
                if (!state.castingLaser)
                    return frame(lightningRecovery, state.elapsed, DomeBoss.RECOVERY);
                int index = frameIndex(prepare.length, state.elapsed, DomeBoss.RECOVERY);
                return prepare[prepare.length - 1 - index];
            default:
                return prepare[0];
        }
    }

    private static Texture frame(Texture[] frames, float elapsed, float duration) {
        return frames[frameIndex(frames.length, elapsed, duration)];
    }

    public void drawEffect(SpriteBatch batch, DomeBoss boss) {
        DomeBoss.State state = boss.dome();
        if (state.phase == DomeBoss.Phase.LIGHTNING_STRIKE) drawLightning(batch, state);
        else if (state.phase == DomeBoss.Phase.LASER_FIRE) drawLaser(batch, state);
    }

    private void drawLightning(SpriteBatch batch, DomeBoss.State state) {
        Texture frame = frame(lightning, state.elapsed, DomeBoss.LIGHTNING_STRIKE);
        if (frame == null) return;
        float width = DomeBoss.LIGHTNING_RADIUS * 2;
        batch.draw(frame, state.aimX - width / 2, state.aimY - 8,
            width, GameSpace.TOP - state.aimY + 8);
    }

    private void drawLaser(SpriteBatch batch, DomeBoss.State state) {
        float dx = state.endX - state.originX, dy = state.endY - state.originY;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        float width = DomeBoss.LASER_HALF_WIDTH * 2;
        float rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees + 90;
        TextureRegion frame = laserRegions[frameIndex(laserRegions.length,
            state.elapsed, DomeBoss.LASER_FIRE)];
        if (frame == null) return;
        batch.draw(frame, state.originX - width / 2, state.originY - length,
            width / 2, length, width, length, 1, 1, rotation);
    }

}
