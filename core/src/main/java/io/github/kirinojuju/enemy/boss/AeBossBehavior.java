package io.github.kirinojuju.enemy.boss;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** A warned vertical laser sweeps left to right alongside Ae's ordinary bullet volleys. */
public final class AeBossBehavior extends AjarnBehavior {
    public static final float LASER_WARNING = .9f;
    public static final float LASER_DURATION = 5f;
    public static final float LASER_COOLDOWN = 5f;
    public static final float LASER_HALF_WIDTH = 16f;
    public static final float LASER_DAMAGE = 16f;
    private static final float PLAYER_RADIUS = 17f;

    @Override
    public void update(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                       PlayerCharacter player, RunPlan plan) {
        AeBoss boss = (AeBoss) enemy;
        if (boss.stunTime > 0) return;
        updateLaser(boss, dt, player, plan);
        // Keep Ae's existing warned warp and three aimed projectiles, including during the sweep.
        super.update(room, boss, dt, player, plan);
    }

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
}
