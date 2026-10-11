package io.github.kirinojuju.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.player.PlayerCharacter;
import java.util.Locale;

/** Loads the artist's separate 64x64 PNG frames and chooses a player animation. */
public final class StudentSprite {
    private static final String ROOT = "characters/student_player/";
    private static final float WALK_FRAME_TIME = .12f;
    private static final float SKILL_FRAME_TIME = .08f;
    private static final float DRAW_SIZE = 128; // 2x integer scale keeps pixels crisp.
    private static final float POWER_BALL_SIZE = 176;

    private final Texture idleLeft = load(ROOT + "idle/s_idle_l00.png");
    private final Texture idleRight = load(ROOT + "idle/s_idle_r00.png");
    private final Texture[] walkLeft = loadFrames("walk", "s_walk_l", 4);
    private final Texture[] walkRight = loadFrames("walk", "s_walk_r", 4);
    private final Texture[] skillLeft = loadFrames("skill_case", "s_skill_l", 8);
    private final Texture[] skillRight = loadFrames("skill_case", "s_skill_r", 9);
    private final Texture powerBall = load(ROOT + "skill_case/power_ball.png");

    private static Texture load(String path) {
        return OptionalAssetTexture.load(path);
    }

    private static Texture[] loadFrames(String folder, String prefix, int count) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++)
            frames[i] = load(ROOT + folder + "/" + prefix
                + String.format(Locale.ROOT, "%02d", i) + ".png");
        return frames;
    }

    private Texture currentFrame(PlayerCharacter player) {
        if (player.skillPlaying) {
            Texture[] frames = player.facingRight ? skillRight : skillLeft;
            int index = Math.min(frames.length - 1,
                (int)(player.skillTime / SKILL_FRAME_TIME));
            return frames[index];
        }
        if (player.moving) {
            Texture[] frames = player.facingRight ? walkRight : walkLeft;
            int index = (int)(player.walkTime / WALK_FRAME_TIME) % frames.length;
            return frames[index];
        }
        return player.facingRight ? idleRight : idleLeft;
    }

    public void draw(SpriteBatch batch, PlayerCharacter player) {
        float alpha = player.invulnerable > 0
            && (int)(player.invulnerable * 18) % 2 == 0 ? .45f : 1f;
        batch.setColor(1, 1, 1, alpha);
        batch.draw(currentFrame(player), player.x - DRAW_SIZE / 2,
            player.y - DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE);
        batch.setColor(Color.WHITE);
    }

    public void drawPowerBall(SpriteBatch batch, CombatRoom.Shot shot) {
        batch.draw(powerBall, shot.x - POWER_BALL_SIZE / 2,
            shot.y - POWER_BALL_SIZE / 2, POWER_BALL_SIZE, POWER_BALL_SIZE);
    }

    public void dispose() {
        idleLeft.dispose();
        idleRight.dispose();
        dispose(walkLeft);
        dispose(walkRight);
        dispose(skillLeft);
        dispose(skillRight);
        powerBall.dispose();
    }

    private static void dispose(Texture[] frames) {
        for (Texture frame : frames) frame.dispose();
    }
}
