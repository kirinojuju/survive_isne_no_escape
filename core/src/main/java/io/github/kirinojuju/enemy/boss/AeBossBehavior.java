package io.github.kirinojuju.enemy.boss;

// File role: Ae's warned warp and vertical laser sweep, alongside shared boss projectile attacks.

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** A warned vertical laser sweeps left to right alongside Ae's ordinary bullet volleys. */
public final class AeBossBehavior extends AjarnBehavior {
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

    // Run Ae's laser and shared boss attacks; generic Final Exam enemies keep their original attacks.
    @Override
    public void update(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                       PlayerCharacter player, RunPlan plan) {
        // Generic Ae (including Final Exam) keeps the original attacks without a laser.
        if (!(enemy instanceof AeBoss)) {
            super.update(room, enemy, dt, player, plan);
            return;
        }
        AeBoss boss = (AeBoss) enemy;
        if (boss.stunTime > 0) return;
        updateLaser(boss, dt, player, plan);
        // Keep Ae's existing warned warp and three aimed projectiles, including during the sweep.
        super.update(room, boss, dt, player, plan);
    }

    // Advance warning, sweep and cooldown phases, checking the whole distance swept this frame.
    private static void updateLaser(AeBoss boss, float dt, PlayerCharacter player, RunPlan plan) {
        // Consume elapsed time across phase boundaries, so long frames cannot skip the sweep.
        float remaining = dt;
        while (remaining > 0) {
            float step = Math.min(remaining, boss.laserTimer);
            if (boss.laserPhase == AeBoss.LaserPhase.SWEEP) {
                float previousX = boss.laserX;
                boss.laserX = Math.min(GameSpace.RIGHT, previousX
                    + (GameSpace.RIGHT - GameSpace.LEFT) / LASER_DURATION * step);
                if (player.y >= GameSpace.BOTTOM - PLAYER_RADIUS
                    && player.y <= GameSpace.WALK_TOP + PLAYER_RADIUS
                    && player.x >= previousX - LASER_HALF_WIDTH - PLAYER_RADIUS
                    && player.x <= boss.laserX + LASER_HALF_WIDTH + PLAYER_RADIUS)
                    player.hurt(LASER_DAMAGE * plan.damageScale());
            }
            boss.laserTimer = Math.max(0, boss.laserTimer - step);
            remaining -= step;
            if (boss.laserTimer > 0) break;
            switch (boss.laserPhase) {
                case IDLE:
                    boss.laserX = GameSpace.LEFT;
                    boss.laserPhase = AeBoss.LaserPhase.WARNING;
                    boss.laserTimer = LASER_WARNING;
                    break;
                case WARNING:
                    boss.laserPhase = AeBoss.LaserPhase.SWEEP;
                    boss.laserTimer = LASER_DURATION;
                    break;
                case SWEEP:
                    boss.laserPhase = AeBoss.LaserPhase.IDLE;
                    boss.laserTimer = LASER_COOLDOWN;
                    break;
            }
        }
    }

    /** Ae locks one visible destination, giving the player time to leave it or dash. */
    @Override
    protected boolean tryWarp(CombatRoom room, CombatRoom.Enemy boss, float dt,
                                     PlayerCharacter player, RunPlan plan) {
        if (!boss.warper || boss.anchored) return false;
        if (boss.warpWindup > 0) {
            boss.warpWindup = Math.max(0, boss.warpWindup - dt);
            if (boss.warpWindup > 0) return true;
            boss.x = boss.warpX;
            boss.y = boss.warpY;
            boss.warpReturnTime = .38f;
            boolean bind = boss.warpStrikes++ % 2 == 0;
            float dx = player.x - boss.warpX, dy = player.y - boss.warpY;
            if (dx * dx + dy * dy <= AE_WARP_RADIUS * AE_WARP_RADIUS && player.invulnerable <= 0) {
                if (bind) player.bind(AE_BIND_DURATION);
                else player.hurt(AE_WARP_DAMAGE * plan.damageScale());
            }
            return true;
        }
        boss.warpCooldown = Math.max(0, boss.warpCooldown - dt);
        if (boss.warpCooldown > 0) return false;
        boss.warpCooldown = Math.max(5f, AE_WARP_INTERVAL / plan.speedScale());
        boss.warpWindup = AE_WARP_WARNING;
        boss.warpX = MathUtils.clamp(player.x, GameSpace.LEFT + boss.radius,
            GameSpace.RIGHT - boss.radius);
        boss.warpY = MathUtils.clamp(player.y, GameSpace.BOTTOM + boss.radius,
            GameSpace.WALK_TOP - boss.radius);
        return true;
    }
}
