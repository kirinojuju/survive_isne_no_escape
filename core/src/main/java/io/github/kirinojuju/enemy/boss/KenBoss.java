package io.github.kirinojuju.enemy.boss;

// File role: Ken's stats, bomb data and full-body damage geometry; KenBossBehavior runs the skills.

import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.world.GameSpace;
import java.util.Random;

public class KenBoss extends Enemy {
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

        // Create a bomb at this position and start its warning countdown.
        Bomb(float x, float y) {
            this.x = x;
            this.y = y;
            warningTime = WARNING_TIME;
        }
    }

    public final Array<Bomb> bombs = new Array<Bomb>();
    final Random random;
    private static final KenBossBehavior BEHAVIOR = new KenBossBehavior();

    // Create KenBoss with its starting stats and state for this encounter.
    public KenBoss(RunPlan plan, Random random) {
        super(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (190 + plan.stage() * 22 + (1 % 3) * 12) * plan.healthScale(),
            72 * plan.spScale(),
            (90 + (1 % 4) * 9) * plan.speedScale(),
            (12 + (1 % 3) * 2) * plan.damageScale(),
            RunPlan.KEN_ID, BEHAVIOR);

        this.random = random;
        skillTimer = 1.8f;
    }

    // Full-body damage and aiming use CombatRoom.Enemy's shared BossBody geometry.

}
