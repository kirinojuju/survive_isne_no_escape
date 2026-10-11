package io.github.kirinojuju.enemy.ex;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.EnemyBehavior;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Shared EX movement and arena bounds; each EX supplies its own attack cycle. */
public abstract class ExBehavior implements EnemyBehavior {
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

    protected abstract void act(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                                PlayerCharacter player, RunPlan plan);

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
        private ExEnvironment() { }

        /** Pickups and hazards resolve before the EX boss acts in the same frame. */
        public static void updateObjects(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
            for (int i = room.exObjects.size - 1; i >= 0; i--) {
                CombatRoom.ExObject object = room.exObjects.get(i);
                object.life -= dt;
                moveEjectedObject(object, dt);

                float dx = object.x - player.x, dy = object.y - player.y;
                float reach = object.radius + 18;
                boolean touching = dx * dx + dy * dy <= reach * reach;
                if (touching && object.kind == CombatRoom.NOTE) {
                    room.carriedNotes = Math.min(3, room.carriedNotes + 1);
                    room.exObjects.removeIndex(i);
                    continue;
                }
                if (touching && object.kind == CombatRoom.SLIME) {
                    player.slowTime = Math.max(player.slowTime, .7f);
                    player.hurt(3 * plan.damageScale());
                } else if (touching && object.kind == CombatRoom.DUST && object.ejectionTime > 0) {
                    player.hurt(9 * plan.damageScale());
                    object.ejectionTime = 0;
                    object.vx = object.vy = 0;
                }
                if (object.life <= 0) room.exObjects.removeIndex(i);
            }
        }

        private static void moveEjectedObject(CombatRoom.ExObject object, float dt) {
            if (object.ejectionTime <= 0) return;
            object.ejectionTime = Math.max(0, object.ejectionTime - dt);
            object.x = MathUtils.clamp(object.x + object.vx * dt,
                GameSpace.LEFT + object.radius, GameSpace.RIGHT - object.radius);
            object.y = MathUtils.clamp(object.y + object.vy * dt,
                GameSpace.BOTTOM + object.radius, GameSpace.WALK_TOP - object.radius);
            object.vx *= Math.max(0, 1 - dt * 1.4f);
            object.vy *= Math.max(0, 1 - dt * 1.4f);
        }

        public static void spawnDust(CombatRoom room, CombatRoom.Enemy vacuum) {
            float x = MathUtils.clamp(vacuum.x - 260 + room.random.nextFloat() * 300,
                GameSpace.LEFT + 12, GameSpace.RIGHT - 12);
            float y = GameSpace.BOTTOM + 25
                + room.random.nextFloat() * (GameSpace.WALK_TOP - GameSpace.BOTTOM - 50);
            room.exObjects.add(new CombatRoom.ExObject(x, y, 8, 12f, CombatRoom.DUST));
        }

        public static void spawnNote(CombatRoom room) {
            float x = MathUtils.clamp(GameSpace.WIDTH / 2 - 215
                + room.random.nextFloat() * 430, GameSpace.LEFT + 20, GameSpace.RIGHT - 20);
            float y = GameSpace.BOTTOM + 55
                + room.random.nextFloat() * (GameSpace.WALK_TOP - GameSpace.BOTTOM - 110);
            room.exObjects.add(new CombatRoom.ExObject(x, y, 11, 20f, CombatRoom.NOTE));
        }

        public static int count(CombatRoom room, int kind) {
            int count = 0;
            for (CombatRoom.ExObject object : room.exObjects)
                if (object.kind == kind) count++;
            return count;
        }
    }
}
