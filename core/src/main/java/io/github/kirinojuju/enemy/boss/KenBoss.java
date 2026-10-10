package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;
import java.util.Random;

public class KenBoss extends Enemy {

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
    private static final AjarnBehavior STANDARD = new AjarnBehavior();

    public KenBoss(RunPlan plan, Random random) {
        super(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (190 + plan.stage() * 22 + (1 % 3) * 12) * plan.healthScale(),
            72 * plan.spScale(),
            (90 + (1 % 4) * 9) * plan.speedScale(),
            (12 + (1 % 3) * 2) * plan.damageScale(),
            RunPlan.TALGO_ID);

        this.random = random;
        skillTimer = 1.8f;
    }

    @Override
    public void updateBehavior(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
        updateBombSkill(dt, player, plan, stunTime <= 0);
        // Run the original bomb attack alongside ordinary Ajarn contact and projectile rules.
        STANDARD.update(room, this, dt, player, plan);
    }

    public void updateBombSkill(float dt, PlayerCharacter player,
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
