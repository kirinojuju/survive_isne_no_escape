package io.github.kirinojuju.enemy.ex;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.ex.ExBoss.ExEnvironment;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Post-it notes strip armor but make the sword confident enough to slash. */
public final class InsecureSwordBoss extends ExBoss {
    public float swordAttackTime;
    public int exConfidence;
    public InsecureSwordBoss(RunPlan plan) {
        this(GameSpace.RIGHT - 75, GameSpace.WALK_TOP - 75, 34,
            380 * plan.healthScale(), 88 * plan.spScale(),
            120 * plan.speedScale(), 14 * plan.damageScale(), CombatRoom.INSECURE_SWORD);
    }

    public InsecureSwordBoss(float x, float y, float radius, float hp, float maxSp,
                       float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }

    public static final float SLASH_ANIMATION_DURATION = .5f;
    public static final float WHIFF_DURATION = .6f;

    @Override
    public float damageMultiplier() {

        return this.exState == 2 ? 1.5f : 1f;
    }

    @Override
    protected void act(CombatRoom room, float dt,
                       PlayerCharacter player, RunPlan plan) {

        this.swordAttackTime = Math.max(0, this.swordAttackTime - dt);
        this.exObjectTimer -= dt;
        if (this.exObjectTimer <= 0
            && ExEnvironment.count(room, CombatRoom.NOTE) < 3) {
            ExEnvironment.spawnNote(room);
            this.exObjectTimer = 4f;
        }
        float dx = player.x - this.x, dy = player.y - this.y;
        if (room.carriedNotes > 0
            && dx * dx + dy * dy <= (this.radius + 78) * (this.radius + 78)) {
            room.carriedNotes--;
            this.exConfidence = Math.min(100, this.exConfidence + 25);
            this.armor = Math.max(0, this.armor - 55 * plan.healthScale());
            this.exState = 2;
            this.exStateTime = .5f;
            this.swordAttackTime = 0; // Applying a note interrupts the attack animation.
            this.exFlashTime = .35f;
            this.exAimX = this.x;
            this.exAimY = this.y;
            this.skillTimer = Math.max(this.skillTimer, 1.3f);
            return;
        }
        if (this.exState == 1 || this.exState == 2 || this.exState == 3) {
            this.exStateTime -= dt;
            if (this.exStateTime <= 0 && this.exState == 1) {
                this.exState = 2;
                this.exStateTime = 1.05f;
                this.swordAttackTime = SLASH_ANIMATION_DURATION;
                this.exFlashTime = .3f;
                this.exConfidence = Math.max(0, this.exConfidence - 15);
                float aimDx = player.x - this.exAimX;
                float aimDy = player.y - this.exAimY;
                if (aimDx * aimDx + aimDy * aimDy <= 100 * 100)
                    player.hurt(29 * plan.damageScale());
            } else if (this.exStateTime <= 0) {
                this.exState = 0;
            }
            return;
        }
        if (this.skillTimer <= 0) {
            this.skillTimer = 2.2f;
            boolean confident = this.exConfidence >= 100
                || room.random.nextFloat() < .15f + this.exConfidence * .006f;
            if (confident) {
                this.exState = 1;
                this.exStateTime = .65f;
                this.exAimX = player.x;
                this.exAimY = player.y;
            } else {
                this.exState = 3;
                this.exStateTime = WHIFF_DURATION; // This attack deals 0 damage.
                this.swordAttackTime = WHIFF_DURATION;
                this.exFlashTime = .25f;
            }
        }
    }

}
