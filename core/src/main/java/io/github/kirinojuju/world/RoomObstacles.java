package io.github.kirinojuju.world;

// File role: Creates tables and handles movement collision, projectile contact and enemy detours.

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.render.OptionalAssetTexture;
import java.util.Arrays;
import java.util.Random;

/** Table placement, movement collision and routes around a few separate tables. */
public final class RoomObstacles {
    private static final float EPSILON = .01f;
    private static final float ROUTE_GAP = 3;
    // table03 is a silhouette; use the three finished furniture images.
    private static final int[] TABLE_ARTS = {0, 1, 3};
    // Visible pixel bounds in the artist's 360 x 373 transparent canvases.
    public static final int[][] ART_BOUNDS = {
        {162, 181, 35, 33}, {146, 177, 49, 33},
        {146, 177, 49, 33}, {168, 160, 27, 76}
    };

    public static final class Table {
        public final int art;
        public final float x, y, width, height;
        public final Rectangle solid;

        // Create a table's artwork dimensions and its solid collision rectangle.
        public Table(int art, float x, float y) {
            this.art = art;
            this.x = x;
            this.y = y;
            width = ART_BOUNDS[art][2] * 2;
            height = ART_BOUNDS[art][3] * 2;
            // Leave a small margin around the visible furniture.
            solid = new Rectangle(x - width / 2 + 6, y - height / 2 + 6,
                width - 12, height - 12);
        }
    }

    public final Array<Table> tables = new Array<Table>();
    private final Vector2 waypoint = new Vector2();
    // Start, destination and four corners per table. Reused during routing.
    private final float[] nodeX = new float[14], nodeY = new float[14], cost = new float[14];
    private final int[] previous = new int[14];
    private final boolean[] visited = new boolean[14];

    // Clear old tables and place separated tables for monster and boss rooms.
    public void begin(RunDirector.Room room, Random random) {
        tables.clear();
        if (room != RunDirector.Room.MOBS && room != RunDirector.Room.BOSS) return;
        // Separate columns leave wide detours. The middle table avoids the player spawn.
        tables.add(randomTable(random, 365, 310, true));
        tables.add(randomTable(random, 640, random.nextBoolean() ? 435 : 195, false));
        tables.add(randomTable(random, 940, 300, true));
    }

    // Choose a furniture image and slightly randomize the table's position.
    private static Table randomTable(Random random, float x, float y, boolean allowTall) {
        int art = allowTall ? TABLE_ARTS[random.nextInt(TABLE_ARTS.length)] : random.nextInt(2);
        return new Table(art, x + random.nextFloat() * 60 - 30,
            y + random.nextFloat() * 20 - 10);
    }

    // Return the horizontal destination after stopping movement at table collision edges.
    public float moveX(float x, float y, float dx, float radius) {
        float destination = x + dx;
        for (Table table : tables) {
            Rectangle box = table.solid;
            if (y <= box.y - radius || y >= box.y + box.height + radius) continue;
            float left = box.x - radius, right = box.x + box.width + radius;
            if (dx > 0 && x <= left && destination >= left)
                destination = Math.min(destination, left - EPSILON);
            else if (dx < 0 && x >= right && destination <= right)
                destination = Math.max(destination, right + EPSILON);
        }
        return destination;
    }

    // Return the vertical destination after stopping movement at table collision edges.
    public float moveY(float x, float y, float dy, float radius) {
        float destination = y + dy;
        for (Table table : tables) {
            Rectangle box = table.solid;
            if (x <= box.x - radius || x >= box.x + box.width + radius) continue;
            float bottom = box.y - radius, top = box.y + box.height + radius;
            if (dy > 0 && y <= bottom && destination >= bottom)
                destination = Math.min(destination, bottom - EPSILON);
            else if (dy < 0 && y >= top && destination <= top)
                destination = Math.max(destination, top + EPSILON);
        }
        return destination;
    }

    // Move an enemy on both axes while respecting its radius and the tables.
    public void moveEnemy(CombatRoom.Enemy enemy, float dx, float dy) {
        enemy.x = moveX(enemy.x, enemy.y, dx, enemy.radius);
        enemy.y = moveY(enemy.x, enemy.y, dy, enemy.radius);
    }

    // Find a route around tables and move toward its next waypoint.
    public void moveToward(CombatRoom.Enemy enemy, float x, float y, float step, float stop) {
        float distance = Vector2.dst(enemy.x, enemy.y, x, y);
        if (distance <= stop) return;
        findWaypoint(enemy.x, enemy.y, x, y, enemy.radius);
        float dx = waypoint.x - enemy.x, dy = waypoint.y - enemy.y;
        float length = Vector2.len(dx, dy);
        if (length < EPSILON) return;
        float movement = Math.min(step, Math.min(length, distance - stop));
        moveEnemy(enemy, dx / length * movement, dy / length * movement);
    }

    /** Returns the earliest contact along a segment, or -1 when it is clear. */
    public float hitFraction(float x, float y, float endX, float endY, float radius) {
        float nearest = Float.POSITIVE_INFINITY;
        float dx = endX - x, dy = endY - y;
        for (Table table : tables) {
            Rectangle box = table.solid;
            float left = box.x - radius, right = box.x + box.width + radius;
            float bottom = box.y - radius, top = box.y + box.height + radius;
            float enter = 0, exit = 1;
            if (Math.abs(dx) < EPSILON) {
                if (x < left || x > right) continue;
            } else {
                float a = (left - x) / dx, b = (right - x) / dx;
                enter = Math.max(enter, Math.min(a, b));
                exit = Math.min(exit, Math.max(a, b));
            }
            if (Math.abs(dy) < EPSILON) {
                if (y < bottom || y > top) continue;
            } else {
                float a = (bottom - y) / dy, b = (top - y) / dy;
                enter = Math.max(enter, Math.min(a, b));
                exit = Math.min(exit, Math.max(a, b));
            }
            if (enter <= exit) nearest = Math.min(nearest, enter);
        }
        return nearest == Float.POSITIVE_INFINITY ? -1 : nearest;
    }

    /** Spawn, teleport, spacing and knockback can all place an enemy inside a table. */
    public void resolveEnemy(CombatRoom.Enemy enemy) {
        if (enemy.anchored || enemy.hp <= 0) return;
        for (Table table : tables) {
            Rectangle box = table.solid;
            float left = box.x - enemy.radius, right = box.x + box.width + enemy.radius;
            float bottom = box.y - enemy.radius, top = box.y + box.height + enemy.radius;
            if (enemy.x <= left || enemy.x >= right || enemy.y <= bottom || enemy.y >= top) continue;
            // An exit past the room boundary would be clamped back inside the table.
            float leftGap = left - EPSILON >= GameSpace.LEFT + enemy.radius
                ? enemy.x - left : Float.POSITIVE_INFINITY;
            float rightGap = right + EPSILON <= GameSpace.RIGHT - enemy.radius
                ? right - enemy.x : Float.POSITIVE_INFINITY;
            float bottomGap = bottom - EPSILON >= GameSpace.BOTTOM + enemy.radius
                ? enemy.y - bottom : Float.POSITIVE_INFINITY;
            float topGap = top + EPSILON <= GameSpace.WALK_TOP - enemy.radius
                ? top - enemy.y : Float.POSITIVE_INFINITY;
            float closest = Math.min(Math.min(leftGap, rightGap), Math.min(bottomGap, topGap));
            if (closest == leftGap) enemy.x = left - EPSILON;
            else if (closest == rightGap) enemy.x = right + EPSILON;
            else if (closest == bottomGap) enemy.y = bottom - EPSILON;
            else enemy.y = top + EPSILON;
        }
        enemy.x = MathUtils.clamp(enemy.x, GameSpace.LEFT + enemy.radius, GameSpace.RIGHT - enemy.radius);
        enemy.y = MathUtils.clamp(enemy.y, GameSpace.BOTTOM + enemy.radius, GameSpace.WALK_TOP - enemy.radius);
    }

    // Find a reachable path through visible table corners toward the destination.
    private void findWaypoint(float x, float y, float targetX, float targetY, float radius) {
        waypoint.set(targetX, targetY);
        if (hitFraction(x, y, targetX, targetY, radius) < 0) return;
        nodeX[0] = x;
        nodeY[0] = y;
        nodeX[1] = targetX;
        nodeY[1] = targetY;
        int count = 2;
        for (Table table : tables) {
            Rectangle box = table.solid;
            float margin = radius + ROUTE_GAP;
            for (int corner = 0; corner < 4; corner++) {
                nodeX[count] = corner % 2 == 0 ? box.x - margin : box.x + box.width + margin;
                nodeY[count] = corner < 2 ? box.y - margin : box.y + box.height + margin;
                count++;
            }
        }
        Arrays.fill(cost, Float.POSITIVE_INFINITY);
        Arrays.fill(previous, -1);
        Arrays.fill(visited, false);
        cost[0] = 0;
        // With only three tables, checking visible corners needs no pathfinding library.
        for (int pass = 0; pass < count; pass++) {
            int current = -1;
            for (int i = 0; i < count; i++)
                if (!visited[i] && (current < 0 || cost[i] < cost[current])) current = i;
            if (current < 0 || cost[current] == Float.POSITIVE_INFINITY) break;
            if (current == 1) {
                int next = 1;
                while (previous[next] > 0) next = previous[next];
                waypoint.set(nodeX[next], nodeY[next]);
                return;
            }
            visited[current] = true;
            for (int next = 1; next < count; next++) {
                if (visited[next] || !insideArena(nodeX[next], nodeY[next], radius)) continue;
                if (hitFraction(nodeX[current], nodeY[current], nodeX[next], nodeY[next], radius) >= 0)
                    continue;
                float candidate = cost[current] + Vector2.dst(nodeX[current], nodeY[current], nodeX[next], nodeY[next]);
                if (candidate < cost[next]) {
                    cost[next] = candidate;
                    previous[next] = current;
                }
            }
        }
        waypoint.set(x, y); // No valid route: stay still rather than walk through furniture.
    }

    // Check whether the entire circular body fits inside the walkable arena.
    private static boolean insideArena(float x, float y, float radius) {
        return x >= GameSpace.LEFT + radius && x <= GameSpace.RIGHT - radius
            && y >= GameSpace.BOTTOM + radius && y <= GameSpace.WALK_TOP - radius;
    }

    /** Art and geometry share one table definition, including its world-space size. */
    public static final class TableSprites {
        private final Texture[] textures = new Texture[4];
        private final TextureRegion[] regions = new TextureRegion[4];

        // Load table images and crop them to the visible furniture bounds.
        public TableSprites() {
            String[] names = {"table00", "table01", "table03", "table04"};
            for (int i = 0; i < names.length; i++) {
                textures[i] = OptionalAssetTexture.load("scene/" + names[i] + ".png");
                int[] bounds = RoomObstacles.ART_BOUNDS[i];
                if (textures[i].getWidth() >= bounds[0] + bounds[2]
                    && textures[i].getHeight() >= bounds[1] + bounds[3])
                    regions[i] = new TextureRegion(textures[i], bounds[0], bounds[1], bounds[2], bounds[3]);
                else regions[i] = new TextureRegion(textures[i]);
            }
        }

        // Draw each table using the same dimensions as the obstacle geometry.
        public void draw(SpriteBatch batch, RoomObstacles obstacles) {
            for (RoomObstacles.Table table : obstacles.tables)
                batch.draw(regions[table.art], table.x - table.width / 2, table.y - table.height / 2,
                    table.width, table.height);
        }

        // Release textures and drawing resources owned by this sprite.
        public void dispose() {
            for (Texture texture : textures) texture.dispose();
        }
    }
}
