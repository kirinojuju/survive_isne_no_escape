package io.github.kirinojuju.world;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;

/** One temporary barrier: lifetime, rotated collision box and separation. */
public final class IceWall {
    public static final float DURATION = 4.5f;
    public static final float LENGTH = 180f;
    public static final float HALF_DEPTH = 10f;
    public static final float CAST_DISTANCE = 92f;
    private static final float EPSILON = .001f;
    public float x, y, angle, time, animationTime;

    public void clear() { time = animationTime = 0; }

    public void update(float dt) {
        time = Math.max(0, time - dt);
        if (time > 0) animationTime += dt;
    }

    public boolean raise(float playerX, float playerY, float faceX, float faceY) {
        float length = (float)Math.sqrt(faceX * faceX + faceY * faceY);
        if (length < EPSILON) { faceX = 1; faceY = 0; length = 1; }
        float nx = faceX / length, ny = faceY / length;
        float targetX = playerX + nx * CAST_DISTANCE;
        float targetY = playerY + ny * CAST_DISTANCE;
        // Keep the requested point fixed. Clamping it back into the room can trap the caster.
        if (targetX < GameSpace.LEFT + HALF_DEPTH || targetX > GameSpace.RIGHT - HALF_DEPTH
            || targetY < GameSpace.BOTTOM + HALF_DEPTH || targetY > GameSpace.WALK_TOP - HALF_DEPTH)
            return false;
        x = targetX;
        y = targetY;
        angle = MathUtils.atan2(ny, nx) + MathUtils.PI / 2;
        time = DURATION;
        animationTime = 0;
        return true;
    }

    /** Swept circle against the wall, so fast shots and dashes cannot tunnel through it. */
    public float hitFraction(float startX, float startY, float endX, float endY, float radius) {
        if (time <= 0) return -1;
        float cos = MathUtils.cos(angle), sin = MathUtils.sin(angle);
        float localX = (startX - x) * cos + (startY - y) * sin;
        float localY = -(startX - x) * sin + (startY - y) * cos;
        float dx = (endX - startX) * cos + (endY - startY) * sin;
        float dy = -(endX - startX) * sin + (endY - startY) * cos;
        float halfLength = LENGTH / 2 + radius, halfDepth = HALF_DEPTH + radius;
        float enter = 0, exit = 1;
        if (Math.abs(dx) < EPSILON) {
            if (Math.abs(localX) > halfLength) return -1;
        } else {
            float a = (-halfLength - localX) / dx, b = (halfLength - localX) / dx;
            enter = Math.max(enter, Math.min(a, b));
            exit = Math.min(exit, Math.max(a, b));
        }
        if (Math.abs(dy) < EPSILON) {
            if (Math.abs(localY) > halfDepth) return -1;
        } else {
            float a = (-halfDepth - localY) / dy, b = (halfDepth - localY) / dy;
            enter = Math.max(enter, Math.min(a, b));
            exit = Math.min(exit, Math.max(a, b));
        }
        return enter <= exit ? enter : -1;
    }

    public float moveX(float startX, float startY, float dx, float radius) {
        if (movesOut(startX, startY, dx, 0, radius)) return startX + dx;
        float hit = hitFraction(startX, startY, startX + dx, startY, radius);
        return startX + dx * (hit < 0 ? 1 : Math.max(0, hit - EPSILON));
    }

    public float moveY(float startX, float startY, float dy, float radius) {
        if (movesOut(startX, startY, 0, dy, radius)) return startY + dy;
        float hit = hitFraction(startX, startY, startX, startY + dy, radius);
        return startY + dy * (hit < 0 ? 1 : Math.max(0, hit - EPSILON));
    }

    /** Allow an actor pushed into the wall to leave through its nearest edge. */
    private boolean movesOut(float startX, float startY, float dx, float dy, float radius) {
        if (hitFraction(startX, startY, startX, startY, radius) < 0) return false;
        float cos = MathUtils.cos(angle), sin = MathUtils.sin(angle);
        float along = (startX - x) * cos + (startY - y) * sin;
        float across = -(startX - x) * sin + (startY - y) * cos;
        if (LENGTH / 2 + radius - Math.abs(along) < HALF_DEPTH + radius - Math.abs(across))
            return (dx * cos + dy * sin) * Math.signum(along) >= 0;
        return Math.abs(across) < EPSILON
            || (-dx * sin + dy * cos) * Math.signum(across) >= 0;
    }

    /** Ground movement is swept too, so a fast enemy cannot walk through between updates. */
    public void stopEnemy(CombatRoom.Enemy enemy, float oldX, float oldY) {
        if (enemy.anchored || movesOut(oldX, oldY, enemy.x - oldX, enemy.y - oldY, enemy.radius)) return;
        float hit = hitFraction(oldX, oldY, enemy.x, enemy.y, enemy.radius);
        if (hit < 0) return;
        float fraction = Math.max(0, hit - EPSILON);
        enemy.x = oldX + (enemy.x - oldX) * fraction;
        enemy.y = oldY + (enemy.y - oldY) * fraction;
    }

    public void resolveEnemy(CombatRoom.Enemy enemy) {
        resolveEnemy(enemy, null);
    }

    public void resolveEnemy(CombatRoom.Enemy enemy, RoomObstacles obstacles) {
        if (enemy.anchored || enemy.hp <= 0
            || hitFraction(enemy.x, enemy.y, enemy.x, enemy.y, enemy.radius) < 0) return;
        float cos = MathUtils.cos(angle), sin = MathUtils.sin(angle);
        float along = (enemy.x - x) * cos + (enemy.y - y) * sin;
        float across = -(enemy.x - x) * sin + (enemy.y - y) * cos;
        float best = Float.MAX_VALUE, bestX = enemy.x, bestY = enemy.y;
        // Pick the nearest of four exits that stays inside the arena and outside furniture.
        for (int edge = 0; edge < 4; edge++) {
            float exitAlong = along, exitAcross = across;
            if (edge < 2) exitAcross = (edge == 0 ? -1 : 1) * (HALF_DEPTH + enemy.radius + EPSILON);
            else exitAlong = (edge == 2 ? -1 : 1) * (LENGTH / 2 + enemy.radius + EPSILON);
            float candidateX = x + exitAlong * cos - exitAcross * sin;
            float candidateY = y + exitAlong * sin + exitAcross * cos;
            if (candidateX < GameSpace.LEFT + enemy.radius || candidateX > GameSpace.RIGHT - enemy.radius
                || candidateY < GameSpace.BOTTOM + enemy.radius || candidateY > GameSpace.WALK_TOP - enemy.radius)
                continue;
            if (obstacles != null && obstacles.hitFraction(candidateX, candidateY,
                candidateX, candidateY, enemy.radius) >= 0) continue;
            float dx = candidateX - enemy.x, dy = candidateY - enemy.y;
            float distance = dx * dx + dy * dy;
            if (distance < best) { best = distance; bestX = candidateX; bestY = candidateY; }
        }
        enemy.x = bestX;
        enemy.y = bestY;
    }
}
