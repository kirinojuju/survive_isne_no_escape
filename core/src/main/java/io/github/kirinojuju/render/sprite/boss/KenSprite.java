package io.github.kirinojuju.render.sprite.boss;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.boss.BossBody;
import io.github.kirinojuju.enemy.boss.KenBoss;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.render.sprite.EnemySprite;

public class KenSprite extends EnemySprite {
    private static final String ROOT = "characters/boss/Talgorithm(Ken)/";
    private static final float DRAW_SIZE = BossBody.KEN_DRAW_SIZE;

    private final Texture[] bodyFrames = frames(ROOT + "kenBody%d.png", 1, 17, true);
    private final Texture[] bombFrames = frames(ROOT + "kenBomb%02d.png", 0, 7, true);
    private final Texture[] warningBomb = frames(ROOT + "warningBomb%02d.png", 0, 3, true);
    private static final int[] PING_PONG = {0, 1, 2, 2, 1, 0};

    public KenSprite() { super(RunPlan.KEN_ID); }
    @Override public boolean supports(Enemy enemy) { return enemy instanceof KenBoss; }

    @Override public void draw(SpriteBatch batch, Enemy enemy, float playerX) {
        KenBoss boss = (KenBoss)enemy;
        Texture frame;
        if (boss.bombAttackTime > 0) {
            float progress = 1f - boss.bombAttackTime / KenBoss.BOMB_ATTACK_DURATION;
            int index = Math.min(16, 2 + (int)(progress * 15));
            frame = bodyFrames[index];
        } else {
            frame = bodyFrames[(int)(boss.animationTime / .3f) % 2];
        }
        drawBody(batch, boss, frame, DRAW_SIZE, false);
    }

    public void drawBombWarnings(SpriteBatch batch, KenBoss boss) {
        float size = KenBoss.BOMB_RADIUS * 2;

        for (KenBoss.Bomb bomb : boss.bombs) {
            if (bomb.exploded) {
                float progress = 1 - bomb.blastTime / KenBoss.BLAST_TIME;
                int index = Math.max(0, Math.min(
                    bombFrames.length - 1,
                    (int)(progress * bombFrames.length)));

                batch.draw(bombFrames[index],
                    bomb.x - size / 2, bomb.y - size / 2,
                    size, size);
            } else {
                int step = (int)((KenBoss.WARNING_TIME - bomb.warningTime) / .12f);
                int index = PING_PONG[step % PING_PONG.length];

                batch.draw(warningBomb[index],
                    bomb.x - size / 2, bomb.y - size / 2,
                    size, size);
            }
        }
    }

}
