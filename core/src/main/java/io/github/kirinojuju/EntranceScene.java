package io.github.kirinojuju;

import com.badlogic.gdx.math.MathUtils;

/** Walk-only arrival scene in front of Building 30. */
final class EntranceScene {
    static final float DRAW_SIZE = 768; // 256px art at an exact 3x scale.
    static final float DRAW_X = (GameSpace.WIDTH - DRAW_SIZE) / 2f;
    static final float DRAW_Y = (GameSpace.HEIGHT - DRAW_SIZE) / 2f;
    static final float DOOR_X = 382, DOOR_Y = 350, DOOR_REACH = 105;
    static final float CAR_X = 930, CAR_Y = 395, CAR_REACH = 110;
    private static final float LEFT = 345, RIGHT = 945;
    private static final float BOTTOM = 145, TOP = 465;
    private static final float WALK_SPEED = 225;

    private EntranceScene() { }

    static void placePlayer(PlayerCharacter player) {
        player.x = 640;
        player.y = 255;
        player.faceX = -1;
        player.faceY = 0;
        player.facingRight = false;
        player.moving = false;
        player.walkTime = 0;
        player.invulnerable = 0;
    }

    static void move(PlayerCharacter player, float dt, float dx, float dy) {
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        player.moving = length > 0;
        if (length == 0) {
            player.walkTime = 0;
            return;
        }
        dx /= length;
        dy /= length;
        player.faceX = dx;
        player.faceY = dy;
        if (dx != 0) player.facingRight = dx > 0;
        player.walkTime += dt;
        player.x = MathUtils.clamp(player.x + dx * WALK_SPEED * dt, LEFT, RIGHT);
        player.y = MathUtils.clamp(player.y + dy * WALK_SPEED * dt, BOTTOM, TOP);
    }

    static boolean atDoor(PlayerCharacter player) {
        return near(player, DOOR_X, DOOR_Y, DOOR_REACH);
    }

    static boolean atCar(PlayerCharacter player) {
        return near(player, CAR_X, CAR_Y, CAR_REACH);
    }

    private static boolean near(PlayerCharacter player, float x, float y, float radius) {
        float dx = player.x - x, dy = player.y - y;
        return dx * dx + dy * dy <= radius * radius;
    }
}
