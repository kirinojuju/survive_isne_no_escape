package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.EnemyBehavior;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** M splits once at half health and fires aimed projectiles. State stays on each MBoss. */
public final class MBossBehavior implements EnemyBehavior {
    static final float FIRST_ATTACK_INTERVAL = 2f;
    static final float SPLIT_ATTACK_INTERVAL = 2f;

    @Override
    public void update(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (enemy.stunTime > 0) return;
        MBoss boss = (MBoss) enemy;
        if (!boss.splitCopy && !boss.alreadySplit && boss.hp <= boss.maxHp * .5f) {
            boss.alreadySplit = true;
            float oppositeX = MathUtils.clamp(GameSpace.LEFT + GameSpace.RIGHT - boss.x,
                GameSpace.LEFT + boss.radius, GameSpace.RIGHT - boss.radius);
            room.spawnQueue.add(new MBoss(oppositeX, boss.y, boss.radius, boss.maxHp * .5f,
                boss.maxSp, boss.speed, boss.damage, true));
            boss.skillTimer = SPLIT_ATTACK_INTERVAL;
        }
        if (boss.skillTimer > 0) return;

        boss.skillTimer = .5f;
        float angle = MathUtils.atan2(player.y - boss.y, player.x - boss.x);
        float speed = 350f * plan.speedScale();
        float damage = (boss.alreadySplit ? 8f : 7f) * plan.damageScale();
        room.addEnemyShot(boss, new CombatRoom.Shot(boss.x, boss.y,
            MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed,
            10, damage, 3f, false));
    }
}
