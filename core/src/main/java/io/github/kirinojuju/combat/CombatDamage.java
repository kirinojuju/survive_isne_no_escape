package io.github.kirinojuju.combat;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom.Shot;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.world.GameSpace;

/** Applies hits: armor, reflection, melee arcs, PowerBall and Notebook effects. */
final class CombatDamage {
    private final CombatRoom room;

    CombatDamage(CombatRoom room) { this.room = room; }

    void damageNearby(float x, float y, float reach, float damage) {
        for (Enemy enemy : room.enemies) {
            if (enemy.getHp() <= 0) continue;
            float dx = enemy.damageX(x) - x, dy = enemy.damageY(y) - y;
            if (dx * dx + dy * dy <= (enemy.damageRadius() + reach) * (enemy.damageRadius() + reach))
                applyToEnemy(enemy, damage, x, y);
        }
    }

    void damageArc(float x, float y, float facingX, float facingY,
                   float reach, float halfAngleDegrees, float damage,
                   boolean slow, boolean tether) {
        float threshold = MathUtils.cosDeg(halfAngleDegrees);
        for (Enemy enemy : room.enemies) {
            if (enemy.getHp() <= 0) continue;
            float dx = enemy.damageX(x) - x, dy = enemy.damageY(y) - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            if (distance > reach + enemy.damageRadius()) continue;
            if (enemy.damageMultiplier() <= 0) continue;
            if (distance < .001f || (dx * facingX + dy * facingY) / distance >= threshold) {
                applyToEnemy(enemy, damage, x, y);
                if (slow) enemy.slowTime = Math.max(enemy.slowTime, 1.5f);
                if (tether && enemy.getHp() > 0)
                    tetherEnemy(enemy, x, y, facingX, facingY);
            }
        }
    }

    private void tetherEnemy(Enemy enemy, float x, float y,
                             float facingX, float facingY) {
        if (enemy.tetherCooldown > 0) return;
        enemy.tetherTime = 1.7f;
        enemy.tetherCooldown = 2.3f;
        if (enemy.kind >= 0) return; // Ajarns and EX monsters keep their position.
        float gatherX = x + facingX * 70;
        float gatherY = y + facingY * 70;
        enemy.x = MathUtils.clamp(enemy.x + (gatherX - enemy.x) * .75f,
            GameSpace.LEFT + enemy.radius, GameSpace.RIGHT - enemy.radius);
        enemy.y = MathUtils.clamp(enemy.y + (gatherY - enemy.y) * .75f,
            GameSpace.BOTTOM + enemy.radius, GameSpace.WALK_TOP - enemy.radius);
        enemy.stunTime = Math.max(enemy.stunTime, .45f);
    }

    void applyToEnemy(Enemy enemy, float damage, float sourceX, float sourceY) {
        damage *= enemy.damageMultiplier();
        if (damage <= 0) return;
        boolean armored = enemy.armor > 0;
        if (enemy.armor > 0) {
            enemy.armor -= damage;
            if (enemy.reflector && enemy.reflectTimer <= 0) {
                float angle = MathUtils.atan2(sourceY - enemy.y, sourceX - enemy.x);
                Shot counter = new Shot(enemy.x, enemy.y,
                    MathUtils.cos(angle) * 320, MathUtils.sin(angle) * 320,
                    9, enemy.damage * .65f, 2.5f, false);
                counter.reflected = true;
                room.addEnemyShot(enemy, counter);
                enemy.reflectTimer = .55f;
            }
            if (enemy.armor < 0) enemy.takeDamage(-(enemy.armor));
            enemy.armor = Math.max(0, enemy.armor);
        } else {
            enemy.takeDamage(damage);
        }
        if (room.getAudio() != null) room.getAudio().hit(armored);
    }

    void fireNotebookBeam(float x, float y, float dx, float dy,
                          float damage, boolean poisonous) {
        float length = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        float aimX = dx / length, aimY = dy / length;
        room.beamX = x;
        room.beamY = y;
        room.beamEndX = x + aimX * CombatRoom.NOTEBOOK_BEAM_RANGE;
        room.beamEndY = y + aimY * CombatRoom.NOTEBOOK_BEAM_RANGE;
        room.beamTime = .30f;
        for (Enemy enemy : room.enemies) {
            if (enemy.getHp() <= 0) continue;
            if (!enemy.overlapsBeam(x, y, aimX, aimY,
                CombatRoom.NOTEBOOK_BEAM_RANGE, CombatRoom.NOTEBOOK_BEAM_HALF_WIDTH)) continue;
            applyToEnemy(enemy, damage, x, y);
            if (poisonous && enemy.getHp() > 0) poison(enemy, damage);
        }
    }

    void explodePowerBall(Shot shot, float sourceX, float sourceY) {
        if (room.getAudio() != null) room.getAudio().skillImpact();
        room.blastX = MathUtils.clamp(shot.x, GameSpace.LEFT, GameSpace.RIGHT);
        room.blastY = MathUtils.clamp(shot.y, GameSpace.BOTTOM, GameSpace.TOP);
        room.blastTime = .30f;
        for (Enemy enemy : room.enemies) {
            if (enemy.getHp() <= 0) continue;
            float dx = enemy.damageX(room.blastX) - room.blastX;
            float dy = enemy.damageY(room.blastY) - room.blastY;
            float reach = CombatRoom.POWER_BALL_BLAST_RADIUS + enemy.damageRadius();
            if (dx * dx + dy * dy > reach * reach) continue;
            if (enemy.damageMultiplier() <= 0) continue;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            float falloff = 1f - .35f * Math.min(1f, distance / reach);
            applyToEnemy(enemy, shot.damage * falloff
                * (enemy.tetherTime > 0 ? 1.35f : 1f), sourceX, sourceY);
            if (enemy.getHp() > 0) {
                float pushX = dx, pushY = dy;
                if (distance < .001f) {
                    pushX = enemy.x - sourceX;
                    pushY = enemy.y - sourceY;
                }
                float pushLength = (float)Math.sqrt(pushX * pushX + pushY * pushY);
                if (pushLength < .001f) { pushX = 1; pushY = 0; pushLength = 1; }
                float push = (enemy.kind >= 0 ? 16f : 92f)
                    * (1f - .35f * Math.min(1f, distance / reach));
                if (!enemy.anchored) {
                    float nextX = MathUtils.clamp(enemy.x + pushX / pushLength * push,
                        GameSpace.LEFT + enemy.radius, GameSpace.RIGHT - enemy.radius);
                    float nextY = MathUtils.clamp(enemy.y + pushY / pushLength * push,
                        GameSpace.BOTTOM + enemy.radius, GameSpace.WALK_TOP - enemy.radius);
                    room.obstacles.moveEnemy(enemy, nextX - enemy.x, nextY - enemy.y);
                }
            }
            enemy.stunTime = Math.max(enemy.stunTime, enemy.kind >= 0 ? .28f : .70f);
            enemy.slowTime = Math.max(enemy.slowTime, enemy.kind >= 0 ? 1.1f : 2.0f);
        }
    }

    void applyNotebookEffect(Enemy hit, Shot shot) {
        if (shot.poisonous && hit.getHp() > 0) poison(hit, shot.damage);
        if (!shot.splash) return;
        room.splashX = shot.x;
        room.splashY = shot.y;
        room.splashTime = .24f;
        for (Enemy enemy : room.enemies) {
            if (enemy == hit || enemy.getHp() <= 0) continue;
            float dx = enemy.damageX(room.splashX) - room.splashX;
            float dy = enemy.damageY(room.splashY) - room.splashY;
            float reach = 72 + enemy.damageRadius();
            if (dx * dx + dy * dy > reach * reach) continue;
            applyToEnemy(enemy, shot.damage * .55f, shot.x, shot.y);
            if (shot.poisonous && enemy.getHp() > 0) poison(enemy, shot.damage);
        }
    }

    private void poison(Enemy enemy, float hitDamage) {
        if (enemy.damageMultiplier() <= 0) return;
        enemy.poisonTime = Math.max(enemy.poisonTime, 3f);
        enemy.poisonDps = Math.max(enemy.poisonDps, hitDamage * .15f);
    }
}
