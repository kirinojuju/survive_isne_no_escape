package io.github.kirinojuju.enemy.boss;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Ken's bomb placement, warnings, explosions and ordinary boss attacks. */
public final class KenBossBehavior extends AjarnBehavior {
    @Override
    public void update(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                       PlayerCharacter player, RunPlan plan) {
        KenBoss boss = (KenBoss) enemy;
        updateBombSkill(boss, dt, player, plan, boss.stunTime <= 0);
        super.update(room, boss, dt, player, plan);
    }

    public static void updateBombSkill(KenBoss boss, float dt, PlayerCharacter player,
                         RunPlan plan, boolean canStartSkill) {

        boss.bombAttackTime = Math.max(0, boss.bombAttackTime - dt);

        for (int i = boss.bombs.size - 1; i >= 0; i--) {
            KenBoss.Bomb bomb = boss.bombs.get(i);

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
                if (bomb.blastTime <= 0) boss.bombs.removeIndex(i);
            }
        }

        if (!canStartSkill || boss.skillTimer > 0) return;

        boss.skillTimer = Math.max(3.2f, 5.2f / plan.speedScale());

        boss.bombAttackTime = KenBoss.BOMB_ATTACK_DURATION;

        int count = 3 + boss.random.nextInt(3);

        for (int i = 0; i < count; i++) {
            float x = GameSpace.LEFT + KenBoss.BOMB_RADIUS
                + boss.random.nextFloat() * (GameSpace.RIGHT - GameSpace.LEFT
                - KenBoss.BOMB_RADIUS * 2);
            float y = GameSpace.BOTTOM + KenBoss.BOMB_RADIUS
                + boss.random.nextFloat() * (GameSpace.WALK_TOP - GameSpace.BOTTOM
                - KenBoss.BOMB_RADIUS * 2);

            boss.bombs.add(new KenBoss.Bomb(x, y));
        }
    }
}
