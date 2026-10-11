package io.github.kirinojuju.combat;

import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.combat.CombatRoom.NotebookImpact;
import io.github.kirinojuju.combat.CombatRoom.Shot;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Moves shots, checks cover before actors, and retires shots and impact animations. */
final class ProjectileSystem {
    private final CombatRoom room;
    private final CombatDamage damageRules;

    ProjectileSystem(CombatRoom room, CombatDamage damageRules) {
        this.room = room;
        this.damageRules = damageRules;
    }

    void update(float dt, PlayerCharacter player) {
        // Reflections can append shots. Reverse traversal leaves new shots for the next frame.
        for (int i = room.shots.size - 1; i >= 0; i--) {
            if (updateShot(room.shots.get(i), dt, player)) room.shots.removeIndex(i);
        }
    }

    private boolean updateShot(Shot shot, float dt, PlayerCharacter player) {
        if (shot.friendly && shot.target != null) steerHomingShot(shot);
        float oldX = shot.x, oldY = shot.y;
        advanceShot(shot, dt);
        if (hitCover(shot, oldX, oldY, player)) return true;
        if (hasExpired(shot)) {
            if (shot.powerBall) damageRules.explodePowerBall(shot, player.x, player.y);
            return true;
        }
        return shot.friendly ? hitEnemyWithShot(shot, player) : hitPlayerWithShot(shot, player);
    }

    private static void advanceShot(Shot shot, float dt) {
        shot.x += shot.vx * dt;
        shot.y += shot.vy * dt;
        shot.life -= dt;
        shot.animationTime += dt;
        if (shot.wave) shot.radius = Math.min(42, shot.radius + 24 * dt);
    }

    private boolean hitCover(Shot shot, float oldX, float oldY, PlayerCharacter player) {
        float tableHit = shot.ignoresTables ? -1
            : room.obstacles.hitFraction(oldX, oldY, shot.x, shot.y, shot.radius);
        float wallHit = shot.friendly ? -1
            : room.iceWall.hitFraction(oldX, oldY, shot.x, shot.y, shot.radius);
        if (wallHit >= 0 && (tableHit < 0 || wallHit <= tableHit)) return true;
        if (tableHit < 0) return false;

        shot.x = oldX + (shot.x - oldX) * tableHit;
        shot.y = oldY + (shot.y - oldY) * tableHit;
        // An actor touching the front of cover can still be hit first.
        boolean hitActor = shot.friendly ? hitEnemyWithShot(shot, player) : hitPlayerWithShot(shot, player);
        if (!hitActor) {
            if (shot.powerBall) damageRules.explodePowerBall(shot, player.x, player.y);
            else if (shot.notebook) room.notebookImpacts.add(new NotebookImpact(shot.x, shot.y));
        }
        return true;
    }

    private static boolean hasExpired(Shot shot) {
        return shot.life <= 0 || shot.x < GameSpace.LEFT || shot.x > GameSpace.RIGHT
            || shot.y < GameSpace.BOTTOM || shot.y > GameSpace.TOP;
    }

    private boolean hitEnemyWithShot(Shot shot, PlayerCharacter player) {
        for (int i = 0; i < room.enemies.size; i++) {
            Enemy enemy = room.enemies.get(i);
            if (enemy.hp <= 0 || !shotTouches(shot, enemy.x, enemy.y, enemy.radius)) continue;
            if (shot.powerBall) damageRules.explodePowerBall(shot, player.x, player.y);
            else {
                damageRules.applyToEnemy(enemy, shot.damage, player.x, player.y);
                damageRules.applyNotebookEffect(enemy, shot);
                if (shot.notebook) room.notebookImpacts.add(new NotebookImpact(shot.x, shot.y));
            }
            return true;
        }
        return false;
    }

    private static boolean hitPlayerWithShot(Shot shot, PlayerCharacter player) {
        if (!shotTouches(shot, player.x, player.y, 17)) return false;
        boolean vulnerable = player.invulnerable <= 0;
        player.hurt(shot.damage);
        if (vulnerable && shot.binding) player.bind(3.5f);
        return true;
    }

    private static boolean shotTouches(Shot shot, float x, float y, float radius) {
        float dx = shot.x - x, dy = shot.y - y;
        float reach = shot.radius + radius;
        return dx * dx + dy * dy < reach * reach;
    }

    void updateImpacts(float dt) {
        for (int i = room.notebookImpacts.size - 1; i >= 0; i--) {
            NotebookImpact impact = room.notebookImpacts.get(i);
            impact.time += dt;
            if (impact.time >= CombatRoom.NOTEBOOK_IMPACT_DURATION) room.notebookImpacts.removeIndex(i);
        }
    }

    private void steerHomingShot(Shot shot) {
        if (shot.target == null || shot.target.hp <= 0)
            shot.target = room.closestEnemy(shot.x, shot.y);
        if (shot.target == null) return;
        float dx = shot.target.x - shot.x, dy = shot.target.y - shot.y;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length < .001f) return;
        float speed = (float)Math.sqrt(shot.vx * shot.vx + shot.vy * shot.vy);
        shot.vx = dx / length * speed;
        shot.vy = dy / length * speed;
    }
}
