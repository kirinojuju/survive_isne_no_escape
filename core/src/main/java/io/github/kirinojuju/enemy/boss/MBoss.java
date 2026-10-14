package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

public class MBoss extends Boss {
    private final boolean splitCopy;
    private boolean alreadySplit;

    public MBoss(RunPlan plan) {
        this(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (190 + plan.stage() * 22 + 24) * plan.healthScale(),
            72 * plan.spScale(), 108 * plan.speedScale(),
            16 * plan.damageScale(), false);
    }

    MBoss(float x, float y, float radius, float hp, float maxSp,
          float speed, float damage, boolean splitCopy) {
        super(x, y, radius, hp, maxSp, speed, damage, RunPlan.M_ID);
        this.splitCopy = splitCopy;
        this.alreadySplit = splitCopy;
        this.skillTimer = splitCopy
            ? MBoss.SPLIT_ATTACK_INTERVAL : MBoss.FIRST_ATTACK_INTERVAL;
    }

    static final float FIRST_ATTACK_INTERVAL = 2f;
    static final float SPLIT_ATTACK_INTERVAL = 2f;

    @Override
    public void updateBehavior(CombatRoom room, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (stunTime > 0) return;

        if (!this.splitCopy && !this.alreadySplit && this.getHp() <= this.getMaxHp() * .5f) {
            this.alreadySplit = true;
            float oppositeX = MathUtils.clamp(GameSpace.LEFT + GameSpace.RIGHT - this.x,
                GameSpace.LEFT + this.radius, GameSpace.RIGHT - this.radius);
            room.spawnQueue.add(new MBoss(oppositeX, this.y, this.radius, this.getMaxHp() * .5f,
                this.maxSp, this.speed, this.damage, true));
            this.skillTimer = SPLIT_ATTACK_INTERVAL;
        }
        if (this.skillTimer > 0) return;

        this.skillTimer = .5f;
        float angle = MathUtils.atan2(player.y - this.y, player.x - this.x);
        float speed = 350f * plan.speedScale();
        float damage = (this.alreadySplit ? 8f : 7f) * plan.damageScale();
        room.addEnemyShot(this, new CombatRoom.Shot(this.x, this.y,
            MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed,
            10, damage, 3f, false));
    }

}
