package io.github.kirinojuju.enemy;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Warpers override the movement step with a warned teleport. */
public final class WarperMonster extends Monster {
    public WarperMonster(float x, float y, float radius, float hp, float maxSp,
                         float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }

    @Override
    protected boolean tryWarp(CombatRoom room, float dt,
                              PlayerCharacter player, RunPlan plan) {
        this.warpCooldown = Math.max(0, this.warpCooldown - dt);
        if (this.warpWindup > 0) {
            this.warpWindup = Math.max(0, this.warpWindup - dt);
            if (this.warpWindup == 0) {
                this.x = this.warpX;
                this.y = this.warpY;
                float dx = player.x - this.x, dy = player.y - this.y;
                if (dx * dx + dy * dy <= 75 * 75)
                    player.hurt(25 * plan.damageScale());
            }
            return true;
        }
        if (this.warpCooldown <= 0 && this.sp >= this.maxSp) {
            this.sp = 0;
            this.warpWindup = .58f;
            this.warpCooldown = 4f;
            this.warpX = MathUtils.clamp(player.x, GameSpace.LEFT + this.radius,
                GameSpace.RIGHT - this.radius);
            this.warpY = MathUtils.clamp(player.y, GameSpace.BOTTOM + this.radius,
                GameSpace.WALK_TOP - this.radius);
            return true;
        }
        return false;
    }
}
