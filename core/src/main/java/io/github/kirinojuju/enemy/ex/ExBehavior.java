package io.github.kirinojuju.enemy.ex;

// File role: Shared EX update rules and helpers for dust, notes and movement.

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.EnemyBehavior;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Shared EX movement and arena bounds; each EX supplies its own attack cycle. */
public abstract class ExBehavior implements EnemyBehavior {
    // Run the EX-specific action unless stunned, then keep the enemy inside the arena.
    @Override
    public final void update(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                             PlayerCharacter player, RunPlan plan) {
        if (enemy.stunTime > 0) return;
        act(room, enemy, dt, player, plan);
        enemy.x = MathUtils.clamp(enemy.x, GameSpace.LEFT + enemy.radius,
            GameSpace.RIGHT - enemy.radius);
        enemy.y = MathUtils.clamp(enemy.y, GameSpace.BOTTOM + enemy.radius,
            GameSpace.WALK_TOP - enemy.radius);
    }

    // Implement this EX enemy's unique actions for the current frame.
    protected abstract void act(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                                PlayerCharacter player, RunPlan plan);

    // Move toward a point and stop at the requested distance.
    protected final void moveToward(CombatRoom.Enemy enemy, float targetX, float targetY,
                                    float step, float stopDistance) {
        float dx = targetX - enemy.x, dy = targetY - enemy.y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        if (distance <= stopDistance || distance < .001f) return;
        float move = Math.min(step, distance - stopDistance);
        enemy.x += dx / distance * move;
        enemy.y += dy / distance * move;
    }

    /** Shared pickups and hazards used by EX encounters. */
    public static final class ExEnvironment {
        // Prevent construction of this utility class; use its static helpers.
        private ExEnvironment() { }

        // Create a dust object for the Vacuum encounter.
        public static void spawnDust(CombatRoom room, CombatRoom.Enemy vacuum) {
            float x = MathUtils.clamp(vacuum.x - 260 + room.random.nextFloat() * 300,
                GameSpace.LEFT + 12, GameSpace.RIGHT - 12);
            float y = GameSpace.BOTTOM + 25
                + room.random.nextFloat() * (GameSpace.WALK_TOP - GameSpace.BOTTOM - 50);
            room.exObjects.add(new CombatRoom.ExObject(x, y, 8, 12f, CombatRoom.DUST));
        }

        // Place a note object at a random arena position for the Sword encounter.
        public static void spawnNote(CombatRoom room) {
            float x = MathUtils.clamp(GameSpace.WIDTH / 2 - 215
                + room.random.nextFloat() * 430, GameSpace.LEFT + 20, GameSpace.RIGHT - 20);
            float y = GameSpace.BOTTOM + 55
                + room.random.nextFloat() * (GameSpace.WALK_TOP - GameSpace.BOTTOM - 110);
            room.exObjects.add(new CombatRoom.ExObject(x, y, 11, 20f, CombatRoom.NOTE));
        }

        // Count the room's EX objects of the requested kind.
        public static int count(CombatRoom room, int kind) {
            int count = 0;
            for (CombatRoom.ExObject object : room.exObjects)
                if (object.kind == kind) count++;
            return count;
        }
    }
}
