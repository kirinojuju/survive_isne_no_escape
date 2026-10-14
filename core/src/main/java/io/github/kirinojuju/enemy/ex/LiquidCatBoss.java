package io.github.kirinojuju.enemy.ex;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.ex.ExBoss.ExEnvironment;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Two health phases share one boss; copies point to their original and cannot split again. */
public final class LiquidCatBoss extends ExBoss {
    public float catTransformTime, catDivideTime;
    public boolean catFacingRight, catMoving;
    private final Enemy original;

    public LiquidCatBoss(float x, float y, float radius, float hp, float maxSp,
                         float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
        original = null;
    }

    @Override
    protected void updateTimers(float dt) {
        super.updateTimers(dt);
        catMoving = false;
    }

    public LiquidCatBoss(RunPlan plan) {
        super(GameSpace.RIGHT - 75, GameSpace.WALK_TOP - 75, 34,
            190 * plan.healthScale(), 88 * plan.spScale(),
            120 * plan.speedScale(), 14 * plan.damageScale(), CombatRoom.LIQUID_CAT);
        original = null;
        skillTimer = 1.8f;
    }

    private LiquidCatBoss(Enemy original, float x, float y) {
        super(x, y, 16, original.getMaxHp() * .22f, original.maxSp,
            original.speed, original.damage * .5f, CombatRoom.LIQUID_CAT);
        this.original = original;
        exState = PHASE_TWO;
        exStateTime = 1.2f;
    }

    static LiquidCatBoss copyOf(Enemy original, float x, float y) {
        return new LiquidCatBoss(original, x, y);
    }

    public Enemy original() { return original; }
    public boolean isCopy() { return original != null; }

    @Override
    public float damageMultiplier() {
        if (this.catTransformTime > 0) return 0;
        return this.exState == PHASE_TWO && !isCopy() ? .55f : 1f;
    }

    @Override
    public void onDefeated(CombatRoom room) {
        if (isCopy()) return;
        if (this.exState == PHASE_TWO) {
            for (int i = 0; i < room.enemies.size; i++) {
                Enemy enemy = room.enemies.get(i);
                if (isCopy(enemy) && ((LiquidCatBoss)enemy).original() == this) enemy.setHp(0);
            }
            return;
        }
        // Refill only once. Clearing status and pending attacks protects the transformation.
        this.exState = PHASE_TWO;
        this.setHp(this.getMaxHp());
        this.radius = 16;
        this.catTransformTime = TRANSFORM_DURATION;
        this.catDivideTime = this.exMeter = this.exFlashTime = 0;
        this.poisonTime = this.poisonDps = this.stunTime = this.slowTime = 0;
        this.tetherTime = this.tetherCooldown = 0;
        this.catMoving = this.moving = false;
        this.skillTimer = TRANSFORM_DURATION + .9f;
        this.exStateTime = 1.2f;
        this.exObjectTimer = 0;
    }

    public static final int PHASE_ONE = 0, PHASE_TWO = 1;
    public static final int MAX_COPIES = 2;
    public static final float ORBITAL_WARNING = .4f;
    public static final float ORBITAL_FALL = .14f;
    public static final float ORBITAL_DURATION = ORBITAL_WARNING + ORBITAL_FALL;
    public static final float ORBITAL_RADIUS = 80;
    public static final float ORBITAL_RECOVERY = .25f;
    public static final float LEAP_WINDUP = ORBITAL_DURATION;
    public static final float TRANSFORM_DURATION = .65f;
    public static final float DIVIDE_DURATION = .65f;
    private static final float DIVIDE_RELEASE_TIME = .3f;

    public static boolean isCopy(Enemy cat) {
        return cat instanceof LiquidCatBoss && ((LiquidCatBoss)cat).isCopy();
    }

    @Override
    protected void act(CombatRoom room, float dt,
                       PlayerCharacter player, RunPlan plan) {

        this.catMoving = false;
        if (isCopy(this) && this.original().getHp() <= 0) {
            this.setHp(0);
            return;
        }
        if (this.catTransformTime > 0) {
            this.catTransformTime = Math.max(0, this.catTransformTime - dt);
            return;
        }
        if (this.catDivideTime > 0) {
            advanceDivide(room, dt, plan);
            return;
        }
        if (this.exMeter > 0) {
            advanceOrbital(dt, player, plan);
            return;
        }
        if (this.exFlashTime > 0) return;
        this.catFacingRight = player.x >= this.x;
        boolean copy = isCopy(this);
        if (!copy && this.skillTimer <= 0) {
            startOrbital(player, plan);
            return;
        }
        if (this.exState == PHASE_ONE) {
            contactDamage(player, plan, false);
            return; // The first phase uses idle frames and never walks toward the player.
        }

        this.exStateTime = Math.max(0, this.exStateTime - dt);
        if (this.exStateTime == 0 && (copy || countCopies(room, this) < MAX_COPIES)) {
            this.exStateTime = copy ? 2.2f : 6f;
            this.catDivideTime = DIVIDE_DURATION;
            this.exAimX = player.x;
            this.exAimY = player.y;
            return;
        }
        if (!copy) leaveOoze(room, dt);
        moveCat(room, player, this.speed * (copy ? .65f : .38f) * dt);
        contactDamage(player, plan, true);
    }

    private void startOrbital(PlayerCharacter player, RunPlan plan) {
        float interval = this.exState == PHASE_TWO ? 1.7f : 2.6f;
        this.skillTimer = Math.max(1.2f, interval / plan.speedScale());
        this.exMeter = ORBITAL_DURATION;
        this.exAimX = player.x;
        this.exAimY = player.y;
    }

    private void advanceOrbital(float dt,
                                       PlayerCharacter player, RunPlan plan) {
        this.exMeter = Math.max(0, this.exMeter - dt);
        if (this.exMeter > 0) return;
        this.exFlashTime = ORBITAL_RECOVERY;
        // The projectile falls from the sky; the boss body stays at its current position.
        float dx = player.x - this.exAimX, dy = player.y - this.exAimY;
        if (dx * dx + dy * dy <= ORBITAL_RADIUS * ORBITAL_RADIUS)
            player.hurt(20 * plan.damageScale());
    }

    private void leaveOoze(CombatRoom room, float dt) {
        this.exObjectTimer -= dt;
        if (this.exObjectTimer > 0 || ExEnvironment.count(room, CombatRoom.SLIME) >= 7) return;
        room.exObjects.add(new CombatRoom.ExObject(this.x, this.y, 25, 2.6f, CombatRoom.SLIME));
        this.exObjectTimer = .45f;
    }

    private void moveCat(CombatRoom room, PlayerCharacter player, float step) {
        float oldX = this.x, oldY = this.y;
        moveToward(player.x, player.y, step * (this.slowTime > 0 ? .48f : 1), this.radius + 18);
        room.iceWall.stopEnemy(this, oldX, oldY);
        this.catMoving = Math.abs(this.x - oldX) + Math.abs(this.y - oldY) > .1f;
    }

    private void contactDamage(PlayerCharacter player,
                                      RunPlan plan, boolean compact) {
        float dx = player.x - this.x, dy = player.y - this.y;
        float reach = this.radius + (compact ? 22 : 18);
        if (dx * dx + dy * dy > reach * reach) return;
        if (compact) player.slowTime = Math.max(player.slowTime, 1.1f);
        if (this.contactTimer > 0) return;
        player.hurt(compact ? 5 * plan.damageScale() : this.damage);
        this.contactTimer = .85f;
    }

    private void advanceDivide(CombatRoom room, float dt, RunPlan plan) {
        float previous = this.catDivideTime;
        this.catDivideTime = Math.max(0, previous - dt);
        if (previous <= DIVIDE_RELEASE_TIME || this.catDivideTime > DIVIDE_RELEASE_TIME) return;
        if (isCopy(this)) fireCopyBullet(room, plan);
        else spawnCopies(room);
    }

    private void spawnCopies(CombatRoom room) {
        int count = Math.min(MAX_COPIES - countCopies(room, this), MAX_COPIES);
        for (int i = 0; i < count; i++) {
            float x = MathUtils.clamp(this.x + (i == 0 ? -64 : 64),
                GameSpace.LEFT + 16, GameSpace.RIGHT - 16);
            float y = MathUtils.clamp(this.y - 45, GameSpace.BOTTOM + 16, GameSpace.WALK_TOP - 16);
            LiquidCatBoss copy = LiquidCatBoss.copyOf(this, x, y);
            room.obstacles.resolveEnemy(copy);
            room.iceWall.resolveEnemy(copy, room.obstacles);
            room.spawnQueue.add(copy);
        }
    }

    private static int countCopies(CombatRoom room, Enemy original) {
        int count = 0;
        for (int i = 0; i < room.enemies.size; i++)
            if (livingCopyOf(room.enemies.get(i), original)) count++;
        for (int i = 0; i < room.spawnQueue.size; i++)
            if (livingCopyOf(room.spawnQueue.get(i), original)) count++;
        return count;
    }

    private static boolean livingCopyOf(Enemy enemy, Enemy original) {
        return enemy.getHp() > 0 && isCopy(enemy) && ((LiquidCatBoss)enemy).original() == original;
    }

    private void fireCopyBullet(CombatRoom room, RunPlan plan) {
        float dx = this.exAimX - this.x, dy = this.exAimY - this.y;
        float distance = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        CombatRoom.Shot bullet = new CombatRoom.Shot(this.x, this.y,
            dx / distance * 260 * plan.speedScale(), dy / distance * 260 * plan.speedScale(),
            9, 8 * plan.damageScale(), 3f, false);
        bullet.liquidCatBullet = true;
        room.addEnemyShot(this, bullet);
    }

}
