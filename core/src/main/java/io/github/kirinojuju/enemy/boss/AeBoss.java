package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

public class AeBoss extends Boss {
    public enum LaserPhase { IDLE, WARNING, SWEEP }
    private boolean sweepingLaser;
    private LaserPhase laserPhase = LaserPhase.IDLE;
    private float laserTimer = 2f;
    private float laserX = GameSpace.LEFT;

    public LaserPhase laserPhase() { return laserPhase; }
    public float laserX() { return laserX; }

    public AeBoss(float x, float y, float radius, float hp, float maxSp,
                  float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }

    public AeBoss(RunPlan plan) {
        super(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (202 + plan.stage() * 22) * plan.healthScale(),
            72 * plan.spScale(), 99 * plan.speedScale(),
            14 * plan.damageScale(), RunPlan.AE_ID);
        sweepingLaser = true;
        warper = true;
        warpCooldown = 6f;
    }

    public static final float AE_WARP_WARNING = .8f;
    public static final float AE_WARP_RADIUS = 75;
    private static final float AE_WARP_INTERVAL = 7f;
    private static final float AE_BIND_DURATION = 1f;
    private static final float AE_WARP_DAMAGE = 14;

    public static final float LASER_WARNING = .9f;
    public static final float LASER_DURATION = 5f;
    public static final float LASER_COOLDOWN = 5f;
    public static final float LASER_HALF_WIDTH = 16f;
    public static final float LASER_DAMAGE = 16f;
    private static final float PLAYER_RADIUS = 17f;

    @Override
    public void updateBehavior(CombatRoom room, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (this.stunTime > 0) return;
        if (sweepingLaser && !anchored) updateLaser(dt, player, plan);
        // Keep Ae's existing warned warp and three aimed projectiles, including during the sweep.
        super.updateBehavior(room, dt, player, plan);
    }

    private void updateLaser(float dt, PlayerCharacter player, RunPlan plan) {
        // Consume elapsed time across phase boundaries, so long frames cannot skip the sweep.
        float remaining = dt;
        while (remaining > 0) {
            float step = Math.min(remaining, this.laserTimer);
            if (this.laserPhase == AeBoss.LaserPhase.SWEEP) {
                float previousX = this.laserX;
                this.laserX = Math.min(GameSpace.RIGHT, previousX
                    + (GameSpace.RIGHT - GameSpace.LEFT) / LASER_DURATION * step);
                if (player.y >= GameSpace.BOTTOM - PLAYER_RADIUS
                    && player.y <= GameSpace.WALK_TOP + PLAYER_RADIUS
                    && player.x >= previousX - LASER_HALF_WIDTH - PLAYER_RADIUS
                    && player.x <= this.laserX + LASER_HALF_WIDTH + PLAYER_RADIUS)
                    player.hurt(LASER_DAMAGE * plan.damageScale());
            }
            this.laserTimer = Math.max(0, this.laserTimer - step);
            remaining -= step;
            if (this.laserTimer > 0) break;
            switch (this.laserPhase) {
                case IDLE:
                    this.laserX = GameSpace.LEFT;
                    this.laserPhase = AeBoss.LaserPhase.WARNING;
                    this.laserTimer = LASER_WARNING;
                    break;
                case WARNING:
                    this.laserPhase = AeBoss.LaserPhase.SWEEP;
                    this.laserTimer = LASER_DURATION;
                    break;
                case SWEEP:
                    this.laserPhase = AeBoss.LaserPhase.IDLE;
                    this.laserTimer = LASER_COOLDOWN;
                    break;
            }
        }
    }

    /** Ae locks one visible destination, giving the player time to leave it or dash. */
    @Override
    protected boolean tryWarp(CombatRoom room, float dt,
                                     PlayerCharacter player, RunPlan plan) {
        if (!this.warper || this.anchored) return false;
        if (this.warpWindup > 0) {
            this.warpWindup = Math.max(0, this.warpWindup - dt);
            if (this.warpWindup > 0) return true;
            this.x = this.warpX;
            this.y = this.warpY;
            this.warpReturnTime = .38f;
            boolean bind = this.warpStrikes++ % 2 == 0;
            float dx = player.x - this.warpX, dy = player.y - this.warpY;
            if (dx * dx + dy * dy <= AE_WARP_RADIUS * AE_WARP_RADIUS && player.invulnerable <= 0) {
                if (bind) player.bind(AE_BIND_DURATION);
                else player.hurt(AE_WARP_DAMAGE * plan.damageScale());
            }
            return true;
        }
        this.warpCooldown = Math.max(0, this.warpCooldown - dt);
        if (this.warpCooldown > 0) return false;
        this.warpCooldown = Math.max(5f, AE_WARP_INTERVAL / plan.speedScale());
        this.warpWindup = AE_WARP_WARNING;
        this.warpX = MathUtils.clamp(player.x, GameSpace.LEFT + this.radius,
            GameSpace.RIGHT - this.radius);
        this.warpY = MathUtils.clamp(player.y, GameSpace.BOTTOM + this.radius,
            GameSpace.WALK_TOP - this.radius);
        return true;
    }

}
