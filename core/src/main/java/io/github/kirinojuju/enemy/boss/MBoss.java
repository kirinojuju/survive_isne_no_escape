package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

public class MBoss extends Enemy {
    private static final float FIRST_ATTACK_INTERVAL = 2f;
    private static final float SPLIT_ATTACK_INTERVAL = 5f;

    private final boolean splitCopy;
    private boolean alreadySplit;

    @Override
    public void updateBehavior(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
        if (stunTime > 0) return;
        int firstShot = room.shots.size;
        updateSplitAndAttack(player, plan, room.shots, room.spawnQueue);
        for (int i = firstShot; i < room.shots.size; i++) room.shots.get(i).ignoresTables = true;
    }

    public MBoss(RunPlan plan) {
        this(
            GameSpace.RIGHT - 60,
            GameSpace.WALK_TOP - 75,
            36,
            (190 + plan.stage() * 22 + 24) * plan.healthScale(),
            72 * plan.spScale(),
            108 * plan.speedScale(),
            16 * plan.damageScale(),
            false
        );
    }

    private MBoss(float x, float y, float radius, float hp, float maxSp,
                  float speed, float damage, boolean splitCopy) {
        super(x, y, radius, hp, maxSp, speed, damage, 2);
        this.splitCopy = splitCopy;
        this.alreadySplit = splitCopy;
        this.skillTimer = splitCopy
            ? SPLIT_ATTACK_INTERVAL : FIRST_ATTACK_INTERVAL;
    }

    public void updateSplitAndAttack(PlayerCharacter player, RunPlan plan,
                              Array<CombatRoom.Shot> shots,
                              Array<Enemy> spawnQueue) {
        if (!splitCopy && !alreadySplit && hp <= maxHp * 0.5f) {
            alreadySplit = true;

            float oppositeX = MathUtils.clamp(
                GameSpace.LEFT + GameSpace.RIGHT - x,
                GameSpace.LEFT + radius,
                GameSpace.RIGHT - radius
            );

            spawnQueue.add(new MBoss(
                oppositeX, y, radius, maxHp * 0.5f,
                maxSp, speed, damage, true
            ));

            skillTimer = SPLIT_ATTACK_INTERVAL;
        }

        if (skillTimer > 0) return;

        boolean phase2 = alreadySplit;
        skillTimer = phase2 ? .5f : .5f;

        float angle = MathUtils.atan2(player.y - y, player.x - x);
        float bulletSpeed = (phase2 ? 350f : 350f) * plan.speedScale();
        float bulletDamage = (phase2 ? 8f : 7f) * plan.damageScale();

        shots.add(new CombatRoom.Shot(
            x, y,
            MathUtils.cos(angle) * bulletSpeed,
            MathUtils.sin(angle) * bulletSpeed,
            10, bulletDamage, 3f, false
        ));
    }
}
