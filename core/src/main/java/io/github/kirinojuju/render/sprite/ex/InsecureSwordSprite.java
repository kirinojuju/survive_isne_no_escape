package io.github.kirinojuju.render.sprite.ex;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.boss.BossBody;
import io.github.kirinojuju.enemy.ex.InsecureSwordBoss;
import io.github.kirinojuju.render.sprite.EnemySprite;

/** The team's five idle frames and five attack frames, played at a crisp 2x scale. */
public class InsecureSwordSprite extends EnemySprite {
    private static final String ROOT = "characters/boss_ex/insecure-sword/";
    private static final float DRAW_SIZE = BossBody.SWORD_DRAW_SIZE;
    private static final float IDLE_FRAME_TIME = .16f;
    private final Texture[] idle = frames(ROOT + "idle_%d.png", 1, 5, true);
    private final Texture[] attack = frames(ROOT + "attack_%d.png", 1, 5, true);

    public InsecureSwordSprite() { super(CombatRoom.INSECURE_SWORD); }

    @Override public void draw(SpriteBatch batch, Enemy enemy, float playerX) {
        InsecureSwordBoss sword = (InsecureSwordBoss) enemy;
        Texture frame;
        if (sword.swordAttackTime > 0) {
            float duration = sword.exState == 3 ? InsecureSwordBoss.WHIFF_DURATION
                : InsecureSwordBoss.SLASH_ANIMATION_DURATION;
            float progress = 1 - sword.swordAttackTime / duration;
            int index = MathUtils.clamp((int)(progress * attack.length), 0, attack.length - 1);
            frame = attack[index];
        } else {
            frame = idle[(int)(sword.animationTime / IDLE_FRAME_TIME) % idle.length];
        }
        drawBody(batch, sword, frame, DRAW_SIZE, false);
    }
}
