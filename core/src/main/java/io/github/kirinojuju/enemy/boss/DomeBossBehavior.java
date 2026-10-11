package io.github.kirinojuju.enemy.boss;

// File role: Dome's lightning and laser phase logic; DomeSprite displays the same phase state.

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Dome alternates a marked lightning strike and a charged laser. */
public final class DomeBossBehavior extends AjarnBehavior {
    // Balance values: seconds, SP, damage and world-space size.
    public static final float LIGHTNING_CHARGE = .85f;
    public static final float LIGHTNING_STRIKE = .4f;
    public static final float LIGHTNING_RADIUS = 100;
    public static final float LASER_CHARGE = 1f;
    public static final float LASER_FIRE = .6f;
    public static final float LASER_HALF_WIDTH = 22;
    // Hand positions in the 64x64 source frames, measured from the image's left/top edges.
    // Fractions scale with the sprite; right-facing frames mirror the side hand horizontally.
    public static final float LASER_SIDE_HAND_X = 11.5f / 64f;
    public static final float LASER_SIDE_HAND_Y = 40.5f / 64f;
    public static final float LASER_DOWN_HAND_X = 27f / 64f;
    public static final float LASER_DOWN_HAND_Y = 52f / 64f;
    public static final float RECOVERY = .3f;
    private static final float LIGHTNING_SP_COST = 28;
    private static final float LIGHTNING_DAMAGE = 22;
    private static final float LASER_SP_COST = 38;
    private static final float LASER_DAMAGE = 18;
    private static final float ATTACK_DELAY = .9f;
    private static final float MIN_ATTACK_DELAY = .55f;
    private static final float LASER_MAX_LENGTH = 1600;
    private static final float PLAYER_RADIUS = 17;
    public enum Phase { IDLE, LIGHTNING_CHARGE, LIGHTNING_STRIKE, LASER_CHARGE, LASER_FIRE, RECOVERY }

    /** Each Dome owns its own cast state; the behavior instance can be shared. */
    public static final class State {
        public Phase phase = Phase.IDLE;
        public float remaining = ATTACK_DELAY, elapsed;
        public float aimX, aimY, originX, originY, endX, endY;
        public boolean nextLaser, castingLaser, downward, facingRight;
    }

    // Advance Dome's cast timer and active laser damage unless the boss is stunned.
    @Override
    public void update(CombatRoom room, CombatRoom.Enemy boss, float dt,
                       PlayerCharacter player, RunPlan plan) {
        if (boss.stunTime > 0) return;
        if (boss.anchored) {
            boss.x = boss.homeX;
            boss.y = boss.homeY;
        }

        State state = boss.dome;
        state.elapsed += dt;
        state.remaining = Math.max(0, state.remaining - dt);
        if (state.phase == Phase.LASER_CHARGE || state.phase == Phase.LASER_FIRE)
            updateBeam(boss, state);
        if (state.phase == Phase.LASER_FIRE) damageLaser(state, player, plan);
        if (state.remaining == 0) advancePhase(room, boss, player, plan);
    }

    // Move between idle, charge, strike or fire, and recovery phases.
    private static void advancePhase(CombatRoom room, CombatRoom.Enemy boss,
                                     PlayerCharacter player, RunPlan plan) {
        State state = boss.dome;
        switch (state.phase) {
            case IDLE:
                startCast(room, boss, player);
                break;
            case LIGHTNING_CHARGE:
                enter(room, state, Phase.LIGHTNING_STRIKE, LIGHTNING_STRIKE);
                damageLightning(state, player, plan);
                break;
            case LASER_CHARGE:
                enter(room, state, Phase.LASER_FIRE, LASER_FIRE);
                damageLaser(state, player, plan);
                break;
            case LIGHTNING_STRIKE:
            case LASER_FIRE:
                enter(room, state, Phase.RECOVERY, RECOVERY);
                break;
            case RECOVERY:
                state.nextLaser = !state.castingLaser;
                enter(room, state, Phase.IDLE, Math.max(MIN_ATTACK_DELAY, ATTACK_DELAY / plan.speedScale()));
                break;
        }
    }

    // Spend SP and snapshot the player's position for the next lightning or laser attack.
    private static void startCast(CombatRoom room, CombatRoom.Enemy boss, PlayerCharacter player) {
        State state = boss.dome;
        float cost = state.nextLaser ? LASER_SP_COST : LIGHTNING_SP_COST;
        if (boss.sp < cost) return;
        boss.sp -= cost;
        state.castingLaser = state.nextLaser;
        state.downward = boss.anchored;
        state.facingRight = player.x > boss.x;
        // Snapshot once: the player can move away during the warning.
        state.aimX = player.x;
        state.aimY = player.y;
        if (state.castingLaser) {
            enter(room, state, Phase.LASER_CHARGE, LASER_CHARGE);
            updateBeam(boss, state);
        } else {
            enter(room, state, Phase.LIGHTNING_CHARGE, LIGHTNING_CHARGE);
        }
    }

    // Damage the player if still inside the marked lightning circle when it strikes.
    private static void damageLightning(State state, PlayerCharacter player, RunPlan plan) {
        float dx = player.x - state.aimX;
        float dy = player.y - state.aimY;
        if (dx * dx + dy * dy <= LIGHTNING_RADIUS * LIGHTNING_RADIUS)
            player.hurt(LIGHTNING_DAMAGE * plan.damageScale());
    }

    // Damage the player when their circle overlaps Dome's active beam.
    private static void damageLaser(State state, PlayerCharacter player, RunPlan plan) {
        if (onBeam(state, player.x, player.y, PLAYER_RADIUS))
            player.hurt(LASER_DAMAGE * plan.damageScale());
    }

    // Start a new cast phase, reset its timers and play its phase sound once.
    private static void enter(CombatRoom room, State state, Phase phase, float duration) {
        state.phase = phase;
        state.remaining = duration;
        state.elapsed = 0;
        // Play once on entry, including when the player successfully dodges.
        if (room.audio == null) return;
        switch (phase) {
            case LIGHTNING_CHARGE: room.audio.domeLightningCast(); break;
            case LIGHTNING_STRIKE: room.audio.domeLightningStrike(); break;
            case LASER_CHARGE: room.audio.domeLaserCast(); break;
            case LASER_FIRE: room.audio.domeLaserFire(); break;
            default: break;
        }
    }

    // Calculate Dome's beam origin and direction, stopping its endpoint at the arena edge.
    private static void updateBeam(CombatRoom.Enemy boss, State state) {
        // Use the same top edge and size as DomeSprite, rather than the old boss-center offsets.
        float size = BossBody.DOME_DRAW_SIZE;
        float top = boss.y + boss.radius;
        if (state.downward) {
            state.originX = boss.x + (LASER_DOWN_HAND_X - .5f) * size;
            state.originY = top - LASER_DOWN_HAND_Y * size;
        } else {
            float handOffset = (LASER_SIDE_HAND_X - .5f) * size;
            state.originX = boss.x + (state.facingRight ? -handOffset : handOffset);
            state.originY = top - LASER_SIDE_HAND_Y * size;
        }
        float dx = state.downward ? 0 : state.aimX - state.originX;
        float dy = state.downward ? -1 : state.aimY - state.originY;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length < .001f) {
            dx = state.facingRight ? 1 : -1;
            dy = 0;
            length = 1;
        }
        dx /= length;
        dy /= length;
        // Stop at the arena edge so the warning, art and hit area share an endpoint.
        float distance = LASER_MAX_LENGTH;
        if (dx > .001f) distance = Math.min(distance, (GameSpace.RIGHT - state.originX) / dx);
        if (dx < -.001f) distance = Math.min(distance, (GameSpace.LEFT - state.originX) / dx);
        if (dy > .001f) distance = Math.min(distance, (GameSpace.TOP - state.originY) / dy);
        if (dy < -.001f) distance = Math.min(distance, (GameSpace.BOTTOM - state.originY) / dy);
        state.endX = state.originX + dx * distance;
        state.endY = state.originY + dy * distance;
    }

    // Check whether a circle overlaps the finite beam used by laser damage.
    public static boolean onBeam(State state, float x, float y, float radius) {
        float dx = state.endX - state.originX, dy = state.endY - state.originY;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length < .001f) return false;
        float toX = x - state.originX, toY = y - state.originY;
        float along = (toX * dx + toY * dy) / length;
        float across = Math.abs(toX * dy - toY * dx) / length;
        return along >= 0 && along <= length && across <= LASER_HALF_WIDTH + radius;
    }
}
