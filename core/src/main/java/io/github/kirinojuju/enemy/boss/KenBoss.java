package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;
import java.util.Random;

public class KenBoss extends Boss {
    public static final float DRAW_SIZE = BossBody.KEN_DRAW_SIZE;

    public static final float WARNING_TIME = 3f;
    public static final float BOMB_RADIUS = 68f;
    public static final float BLAST_TIME = .56f;    // Time to show bomb
    public static final float BOMB_DAMAGE = 20f;
    public static final float BOMB_ATTACK_DURATION = .9f;
    public float bombAttackTime;

    public static final class Bomb {
        public float x, y, warningTime, blastTime;
        public boolean exploded;

        Bomb(float x, float y) {
            this.x = x;
            this.y = y;
            warningTime = WARNING_TIME;
        }
    }

    public final Array<Bomb> bombs = new Array<Bomb>();
    private final Random random;

    public KenBoss(RunPlan plan, Random random) {
        super(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (202 + plan.stage() * 22) * plan.healthScale(),
            72 * plan.spScale(),
            99 * plan.speedScale(),
            14 * plan.damageScale(),
            RunPlan.KEN_ID);

        this.random = random;
        skillTimer = 1.8f;
    }

    public void advanceBombs(float dt, PlayerCharacter player,
                         RunPlan plan, boolean canStartSkill) {

        this.bombAttackTime = Math.max(0, this.bombAttackTime - dt);

        for (int i = this.bombs.size - 1; i >= 0; i--) {
            KenBoss.Bomb bomb = this.bombs.get(i);

            if (!bomb.exploded) {
                bomb.warningTime -= dt;

                if (bomb.warningTime <= 0) {
                    bomb.exploded = true;
                    bomb.blastTime = KenBoss.BLAST_TIME;

                    float dx = player.x - bomb.x;
                    float dy = player.y - bomb.y;
                    float hitRadius = KenBoss.BOMB_RADIUS + 18f;

                    if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                        player.hurt(KenBoss.BOMB_DAMAGE * plan.damageScale());
                    }
                }
            } else {
                bomb.blastTime -= dt;
                if (bomb.blastTime <= 0) this.bombs.removeIndex(i);
            }
        }

        if (!canStartSkill || this.skillTimer > 0) return;

        this.skillTimer = Math.max(3.2f, 5.2f / plan.speedScale());

        this.bombAttackTime = KenBoss.BOMB_ATTACK_DURATION;

        int count = 7 + this.random.nextInt(3);

        for (int i = 0; i < count; i++) {
            float x = GameSpace.LEFT + KenBoss.BOMB_RADIUS
                + this.random.nextFloat() * (GameSpace.RIGHT - GameSpace.LEFT
                - KenBoss.BOMB_RADIUS * 2);
            float y = GameSpace.BOTTOM + KenBoss.BOMB_RADIUS
                + this.random.nextFloat() * (GameSpace.WALK_TOP - GameSpace.BOTTOM
                - KenBoss.BOMB_RADIUS * 2);

            this.bombs.add(new KenBoss.Bomb(x, y));
        }
    }

    @Override
    public void updateBehavior(CombatRoom room, float dt,
                       PlayerCharacter player, RunPlan plan) {

        this.advanceBombs(dt, player, plan, this.stunTime <= 0);
        super.updateBehavior(room, dt, player, plan);
    }

}
