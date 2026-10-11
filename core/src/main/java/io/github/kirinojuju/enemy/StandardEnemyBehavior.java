package io.github.kirinojuju.enemy;

// File role: Shared enemy movement, contact damage, special attack hooks and projectile patterns.

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Movement, contact damage, SP and ordinary attacks shared by monsters and Ajarns. */
public class StandardEnemyBehavior implements EnemyBehavior {
    // Run the shared monster update for this frame.
    @Override
    public void update(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                       PlayerCharacter player, RunPlan plan) {
        updateStandard(room, enemy, dt, player, plan);
    }

    // Handle movement, contact damage and SP-funded attacks unless stunned or using a special.
    protected final void updateStandard(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                                        PlayerCharacter player, RunPlan plan) {
        if (enemy.stunTime > 0) return;
        boolean warping = tryWarp(room, enemy, dt, player, plan);
        boolean special = !warping && useSpecial(room, enemy, dt, player, plan);
        float dx = player.x - enemy.x, dy = player.y - enemy.y;
        float length = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        if (!warping && !special && enemy.kind < 0) {
            float speed = enemy.speed * (enemy.slowTime > 0 ? .48f : 1f);
            if (enemy.kind == CombatRoom.RANGED || enemy.artillery) {
                float desired = enemy.artillery ? 330 : 290;
                boolean blocked = room.obstacles.hitFraction(enemy.x, enemy.y, player.x, player.y, 9) >= 0;
                if (blocked || length > desired + 35) {
                    room.obstacles.moveToward(enemy, player.x, player.y, speed * dt, enemy.radius + 20);
                } else if (length < desired - 35) {
                    room.obstacles.moveEnemy(enemy, -dx / length * speed * dt, -dy / length * speed * dt);
                }
            } else if (length > enemy.radius + 20) {
                room.obstacles.moveToward(enemy, player.x, player.y, speed * dt, enemy.radius + 20);
            }
        }
        if (enemy.anchored) {
            enemy.x = enemy.homeX;
            enemy.y = enemy.homeY;
        } else {
            enemy.x = MathUtils.clamp(enemy.x, GameSpace.LEFT + enemy.radius,
                GameSpace.RIGHT - enemy.radius);
            enemy.y = MathUtils.clamp(enemy.y, GameSpace.BOTTOM + enemy.radius,
                GameSpace.WALK_TOP - enemy.radius);
        }
        if (!warping && length < enemy.radius + 18 && enemy.contactTimer <= 0) {
            player.hurt(enemy.damage);
            enemy.contactTimer = .75f;
        }
        float cost = enemy.kind < 0 ? enemy.maxSp : 28;
        if (!warping && !special && enemy.kind != CombatRoom.WARPER && enemy.sp >= cost
            && enemy.skillTimer <= 0) {
            enemy.sp -= cost;
            enemy.skillTimer = enemy.kind < 0 ? 1.1f
                : enemy.kind == RunPlan.AE_ID ? 1.1f
                : enemy.artillery ? 1.05f : enemy.waveCaster ? .85f : .68f;
            EnemyProjectilePatterns.fire(room, enemy, player, plan);
        }
    }

    // Optional teleport hook; the base behavior does not teleport.
    protected boolean tryWarp(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                              PlayerCharacter player, RunPlan plan) { return false; }

    // Optional special-skill hook; the base behavior leaves ordinary attacks active.
    protected boolean useSpecial(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                                 PlayerCharacter player, RunPlan plan) { return false; }

}

/** Projectile patterns shared by ordinary monsters and Ajarn combat families. */
final class EnemyProjectilePatterns {
    // Prevent construction of this utility class; use its static helpers.
    private EnemyProjectilePatterns() { }

    // Choose binding, wave, giant or ordinary projectile patterns from the enemy's kind and traits.
    static void fire(CombatRoom room, CombatRoom.Enemy enemy,
                     PlayerCharacter player, RunPlan plan) {
        float angle = MathUtils.atan2(player.y - enemy.y, player.x - enemy.x);
        float speed = (enemy.kind < 0 ? 225 : enemy.kind == RunPlan.AE_ID ? 220 : 275) * plan.speedScale();
        float damage = (enemy.kind < 0 ? 5 : enemy.kind == RunPlan.AE_ID ? 8 : 12) * plan.damageScale();
        if (enemy.kind == CombatRoom.BINDER) {
            CombatRoom.Shot bindShot = new CombatRoom.Shot(enemy.x, enemy.y,
                MathUtils.cos(angle) * speed * .85f,
                MathUtils.sin(angle) * speed * .85f,
                9, 3 * plan.damageScale(), 3f, false);
            bindShot.binding = true;
            room.addEnemyShot(enemy, bindShot);
            return;
        }
        if (enemy.kind == CombatRoom.WAVE || enemy.waveCaster) {
            int count = enemy.waveCaster ? 3 : 1;
            for (int i = 0; i < count; i++) {
                float direction = angle + (i - (count - 1) / 2f) * .30f;
                CombatRoom.Shot waveShot = new CombatRoom.Shot(enemy.x, enemy.y,
                    MathUtils.cos(direction) * speed,
                    MathUtils.sin(direction) * speed,
                    enemy.waveCaster ? 25 : 20,
                    (enemy.waveCaster ? 15 : 9) * plan.damageScale(),
                    3f, false);
                waveShot.wave = true;
                room.addEnemyShot(enemy, waveShot);
            }
            return;
        }
        if (enemy.artillery) {
            CombatRoom.Shot giantShot = new CombatRoom.Shot(enemy.x, enemy.y,
                MathUtils.cos(angle) * 195 * plan.speedScale(),
                MathUtils.sin(angle) * 195 * plan.speedScale(),
                38, 24 * plan.damageScale(), 3.5f, false);
            giantShot.giant = true;
            room.addEnemyShot(enemy, giantShot);
            return;
        }
        if (enemy.kind == CombatRoom.RANGED) {
            fireSingle(room, enemy, angle, 325 * plan.speedScale(),
                9 * plan.damageScale());
            return;
        }
        int pattern = enemy.kind < 0 ? 0 : enemy.kind % 4;
        if (pattern == 2) {
            for (int i = 0; i < 8; i++)
                fireSingle(room, enemy, i * MathUtils.PI2 / 8, speed * .8f, damage);
        } else if (pattern == 1 || pattern == 3) {
            for (int i = -1; i <= 1; i++)
                fireSingle(room, enemy,
                    angle + i * (pattern == 1 ? .22f : .4f), speed, damage);
        } else {
            fireSingle(room, enemy, angle, speed, damage);
        }
    }

    // Launch one enemy shot and mark Algorithm projectiles for their special artwork.
    private static void fireSingle(CombatRoom room, CombatRoom.Enemy enemy,
                                   float angle, float speed, float damage) {
        CombatRoom.Shot shot = new CombatRoom.Shot(enemy.x, enemy.y,
            MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed,
            7, damage, 3, false);
        shot.algoBall = enemy instanceof AlgoMonster;
        room.addEnemyShot(enemy, shot);
    }
}
