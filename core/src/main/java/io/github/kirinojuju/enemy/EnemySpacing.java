package io.github.kirinojuju.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.world.GameSpace;

/** Resolves overlap after enemies have moved, keeping bosses in place near minions. */
public final class EnemySpacing {
    private static final int SEPARATION_PASSES = 3;
    private static final float GAP = 12;
    private EnemySpacing() { }

    public static void separate(Array<Enemy> enemies) {
        for (int pass = 0; pass < SEPARATION_PASSES; pass++) {
            for (int i = 0; i < enemies.size; i++) {
                Enemy a = enemies.get(i);
                if (a.getHp() <= 0) continue;
                for (int j = i + 1; j < enemies.size; j++) {
                    Enemy b = enemies.get(j);
                    if (b.getHp() <= 0) continue;
                    if (a.anchored && b.anchored) continue;
                    float dx = a.x - b.x, dy = a.y - b.y;
                    float minimum = a.radius + b.radius + GAP;
                    float distanceSquared = dx * dx + dy * dy;
                    if (distanceSquared >= minimum * minimum) continue;
                    boolean sameSpot = distanceSquared < .001f;
                    if (sameSpot) {
                        dx = i % 2 == 0 ? 1 : -1;
                        dy = j % 2 == 0 ? 1 : -1;
                    }
                    float distance = (float)Math.sqrt(dx * dx + dy * dy);
                    float overlap = sameSpot ? minimum : minimum - distance;
                    float aShare = pushShare(a, b);
                    a.x += dx / distance * overlap * aShare;
                    a.y += dy / distance * overlap * aShare;
                    b.x -= dx / distance * overlap * (1 - aShare);
                    b.y -= dy / distance * overlap * (1 - aShare);
                }
            }
            for (Enemy enemy : enemies) {
                if (enemy.anchored) continue;
                enemy.x = MathUtils.clamp(enemy.x, GameSpace.LEFT + enemy.radius,
                    GameSpace.RIGHT - enemy.radius);
                enemy.y = MathUtils.clamp(enemy.y, GameSpace.BOTTOM + enemy.radius,
                    GameSpace.WALK_TOP - enemy.radius);
            }
        }
    }

    /** Anchored enemies stay fixed; ordinary monsters yield to bosses. */
    private static float pushShare(Enemy a, Enemy b) {
        if (a.anchored) return 0;
        if (b.anchored) return 1;
        if (a.kind >= 0 && b.kind < 0) return 0;
        if (a.kind < 0 && b.kind >= 0) return 1;
        return .5f;
    }
}
