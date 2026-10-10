package io.github.kirinojuju;

import com.badlogic.gdx.utils.Array;
import java.util.Random;

class KenBoss extends Enemy {

    static final float WARNING_TIME = 3f;
    static final float BOMB_RADIUS = 68f;
    static final float BLAST_TIME = .56f;    // Time to show bomb
    static final float BOMB_DAMAGE = 20f;
    static final float BOMB_ATTACK_DURATION = .9f;
    float bombAttackTime;

    static final class Bomb {
        float x, y, warningTime, blastTime;
        boolean exploded;

        Bomb(float x, float y) {
            this.x = x;
            this.y = y;
            warningTime = WARNING_TIME;
        }
    }

    final Array<Bomb> bombs = new Array<Bomb>();
    private final Random random;

    KenBoss(RunPlan plan, Random random) {
        super(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (190 + plan.stage() * 22 + (1 % 3) * 12) * plan.healthScale(),
            72 * plan.spScale(),
            (90 + (1 % 4) * 9) * plan.speedScale(),
            (12 + (1 % 3) * 2) * plan.damageScale(),
            1);

        this.random = random;
        skillTimer = 1.8f;
    }

    void updateBombSkill(float dt, PlayerCharacter player,
                         RunPlan plan, boolean canStartSkill) {

        bombAttackTime = Math.max(0, bombAttackTime - dt);

        for (int i = bombs.size - 1; i >= 0; i--) {
            Bomb bomb = bombs.get(i);

            if (!bomb.exploded) {
                bomb.warningTime -= dt;

                if (bomb.warningTime <= 0) {
                    bomb.exploded = true;
                    bomb.blastTime = BLAST_TIME;

                    float dx = player.x - bomb.x;
                    float dy = player.y - bomb.y;
                    float hitRadius = BOMB_RADIUS + 18f;

                    if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                        player.hurt(BOMB_DAMAGE * plan.damageScale());
                    }
                }
            } else {
                bomb.blastTime -= dt;
                if (bomb.blastTime <= 0) bombs.removeIndex(i);
            }
        }

        if (!canStartSkill || skillTimer > 0) return;

        skillTimer = Math.max(3.2f, 5.2f / plan.speedScale());

        bombAttackTime = BOMB_ATTACK_DURATION;

        int count = 3 + random.nextInt(3);

        for (int i = 0; i < count; i++) {
            float x = GameSpace.LEFT + BOMB_RADIUS
                + random.nextFloat() * (GameSpace.RIGHT - GameSpace.LEFT
                - BOMB_RADIUS * 2);
            float y = GameSpace.BOTTOM + BOMB_RADIUS
                + random.nextFloat() * (GameSpace.WALK_TOP - GameSpace.BOTTOM
                - BOMB_RADIUS * 2);

            bombs.add(new Bomb(x, y));
        }
    }
}
