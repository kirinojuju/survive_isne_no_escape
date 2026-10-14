package io.github.kirinojuju.enemy.ex;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.ex.ExBoss.ExEnvironment;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** The vacuum collects dust, warns before sneezing, then ejects the dust. */
public final class AllergicVacuumBoss extends ExBoss {
    public int exStoredDust;
    public AllergicVacuumBoss(RunPlan plan) {
        this(GameSpace.RIGHT - 75, GameSpace.WALK_TOP - 75, 34,
            380 * plan.healthScale(), 88 * plan.spScale(),
            120 * plan.speedScale(), 14 * plan.damageScale(), CombatRoom.ALLERGIC_VACUUM);
    }

    public AllergicVacuumBoss(float x, float y, float radius, float hp, float maxSp,
                       float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }

    @Override
    protected void act(CombatRoom room, float dt,
                       PlayerCharacter player, RunPlan plan) {

        if (this.exState == 1 || this.exState == 2) {
            this.exStateTime -= dt;
            if (this.exStateTime <= 0 && this.exState == 1) {
                int count = this.exStoredDust;
                for (int i = 0; i < count; i++) {
                    float angle = MathUtils.PI2 * i / Math.max(1, count)
                        + room.random.nextFloat() * .28f;
                    CombatRoom.ExObject dust = new CombatRoom.ExObject(
                        this.x, this.y, 10, 6f, CombatRoom.DUST);
                    float speed = 225 + room.random.nextFloat() * 65;
                    dust.vx = MathUtils.cos(angle) * speed;
                    dust.vy = MathUtils.sin(angle) * speed;
                    dust.ejectionTime = 1.1f;
                    room.exObjects.add(dust);
                }
                this.exStoredDust = 0;
                this.exMeter = 0;
                this.exState = 2;
                this.exStateTime = 1f;
                this.exFlashTime = .38f;
                this.x = Math.min(GameSpace.RIGHT - this.radius, this.x + 40);
            } else if (this.exStateTime <= 0) {
                this.exState = 0;
            }
            return; // No suction while sneezing or recovering.
        }

        this.exObjectTimer -= dt;
        if (this.exObjectTimer <= 0
            && ExEnvironment.count(room, CombatRoom.DUST) < 8) {
            ExEnvironment.spawnDust(room, this);
            this.exObjectTimer = 1.25f;
        }
        moveToward(player.x, player.y,
            this.speed * .43f * dt, 205);
        for (int i = room.exObjects.size - 1; i >= 0; i--) {
            CombatRoom.ExObject dust = room.exObjects.get(i);
            if (dust.kind != CombatRoom.DUST || dust.ejectionTime > 0) continue;
            float dx = this.x - dust.x, dy = this.y - dust.y;
            float distance = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
            float step = Math.min(distance, 175 * dt);
            dust.x += dx / distance * step;
            dust.y += dy / distance * step;
            if (distance <= this.radius + dust.radius + step) {
                room.exObjects.removeIndex(i);
                this.exStoredDust++;
                this.exMeter = Math.min(100, this.exMeter + 25);
            }
        }
        float dx = this.x - player.x, dy = this.y - player.y;
        float distance = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        if (distance < 285 && !player.skillPlaying)
            player.moveBy(dx / distance * 82 * dt, dy / distance * 82 * dt);
        if (distance < this.radius + 22 && this.contactTimer <= 0) {
            player.hurt(this.damage);
            this.contactTimer = .8f;
        }
        if (this.exMeter >= 100) {
            this.exState = 1;
            this.exStateTime = .85f;
        }
    }

}
