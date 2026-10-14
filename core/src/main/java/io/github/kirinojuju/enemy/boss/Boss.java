package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Shared boss rules; concrete teachers and EX bosses inherit from this class. */
public abstract class Boss extends Enemy {
    protected Boss(float x, float y, float radius, float hp, float maxSp,
                float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }

    @Override
    protected float spRecovery() { return 44; }

    @Override
    public void updateBehavior(CombatRoom room, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (restoreAfterWarp(dt)) return;
        updateStandard(room, dt, player, plan);
    }

    protected final boolean restoreAfterWarp(float dt) {
        if (this.warpReturnTime <= 0) return false;
        this.warpReturnTime = Math.max(0, this.warpReturnTime - dt);
        if (this.warpReturnTime == 0) {
            this.x = this.homeX;
            this.y = this.homeY;
        }
        return true;
    }

    @Override
    protected boolean tryWarp(CombatRoom room, float dt,
                              PlayerCharacter player, RunPlan plan) {
        if (!canWarp()) return false;
        this.warpCooldown = Math.max(0, this.warpCooldown - dt);
        if (this.warpCooldown > 0) return false;
        this.warpCooldown = Math.max(2.6f, 4.5f / plan.speedScale());
        this.x = MathUtils.clamp(player.x + (this.x > player.x ? 48 : -48),
            GameSpace.LEFT + this.radius, GameSpace.RIGHT - this.radius);
        this.y = MathUtils.clamp(player.y, GameSpace.BOTTOM + this.radius,
            GameSpace.WALK_TOP - this.radius);
        this.warpReturnTime = .38f;
        if (this.warpStrikes++ % 2 == 0) player.bind(2.8f);
        else player.hurt(26 * plan.damageScale());
        return true;
    }

    protected boolean canWarp() { return warper && !anchored; }
}
