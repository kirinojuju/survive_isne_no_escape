package io.github.kirinojuju.enemy;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.boss.BossBody;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;

/** Shared enemy state and frame order; subclasses override their own combat behavior. */
public abstract class Enemy {
    public float x, y, radius, sp, maxSp, speed, damage;
    private float hp;
    private final float maxHp;
    public float contactTimer, skillTimer, armor, maxArmor, reflectTimer;
    // Animation and visible hit bounds shared by all enemies.
    public float attackAnimationTime;
    public float bodyAspectRatio = 1f;
    public boolean bodyBoundsReady;
    public float bodyLeft, bodyBottom, bodyRight = 1f, bodyTop = 1f;
    public float warpCooldown, warpWindup, warpX, warpY, warpReturnTime;
    public final float homeX, homeY;
    public int warpStrikes;
    public float animationTime;
    public boolean moving;
    // Temporary effects applied by player attacks.
    public float stunTime, slowTime, poisonTime, poisonDps;
    public float tetherTime, tetherCooldown;
    public boolean reflector, warper, artillery, waveCaster;
    public boolean summoner;
    public float summonTimer = 3.8f;
    public boolean anchored;
    public final int kind; // Negative: monster class; 0..7 Ajarn; 8..10 EX.

    protected Enemy(float x, float y, float radius, float hp, float maxSp,
                    float speed, float damage, int kind) {
        this.x = x;
        this.y = y;
        this.homeX = x;
        this.homeY = y;
        this.radius = radius;
        this.hp = hp;
        this.maxHp = hp;
        this.maxSp = maxSp;
        this.sp = maxSp * .35f;
        this.speed = speed;
        this.damage = damage;
        this.kind = kind;
    }

    public float getHp() { return hp; }
    public float getMaxHp() { return maxHp; }

    /** Health stays in range even when a hit exceeds the remaining HP. */
    public void setHp(float value) { hp = MathUtils.clamp(value, 0, maxHp); }
    public void takeDamage(float amount) { setHp(hp - Math.max(0, amount)); }

    public abstract void updateBehavior(CombatRoom room, float dt,
                                        PlayerCharacter player, RunPlan plan);

    /** Every enemy follows the same frame order; subclasses supply their own rates and timers. */
    public final void update(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
        if (hp <= 0) return;
        float poisonStep = Math.min(dt, poisonTime);
        poisonTime -= poisonStep;
        takeDamage(poisonDps * poisonStep);
        if (hp <= 0) return;
        updateTimers(dt);
        sp = Math.min(maxSp, sp + spRecovery() * plan.spScale() * dt);
        if (summoner && stunTime <= 0) room.factory.queueSummons(room, this, dt, plan);
        float oldX = x, oldY = y;
        updateBehavior(room, dt, player, plan);
        room.iceWall.stopEnemy(this, oldX, oldY);
        moving = Math.abs(x - oldX) + Math.abs(y - oldY) > .01f;
    }

    protected abstract float spRecovery();

    protected void updateTimers(float dt) {
        animationTime += dt;
        contactTimer -= dt;
        skillTimer -= dt;
        attackAnimationTime = Math.max(0, attackAnimationTime - dt);
        reflectTimer -= dt;
        stunTime = Math.max(0, stunTime - dt);
        slowTime = Math.max(0, slowTime - dt);
        tetherTime = Math.max(0, tetherTime - dt);
        tetherCooldown = Math.max(0, tetherCooldown - dt);
    }

    public float damageMultiplier() { return 1f; }
    public void onDefeated(CombatRoom room) { }

    public float damageX(float sourceX) {
        return BossBody.hasBounds(this) ? MathUtils.clamp(sourceX, BossBody.left(this), BossBody.right(this)) : x;
    }
    public float damageY(float sourceY) {
        return BossBody.hasBounds(this) ? MathUtils.clamp(sourceY, BossBody.bottom(this), BossBody.top(this)) : y;
    }
    public float damageRadius() { return BossBody.hasBounds(this) ? 0 : radius; }
    public float aimX() { return BossBody.hasBounds(this) ? (BossBody.left(this) + BossBody.right(this)) / 2 : x; }
    public float aimY() {
        return BossBody.hasBounds(this) ? (BossBody.bottom(this) + BossBody.top(this)) / 2 : y;
    }

    public boolean overlapsBeam(float originX, float originY, float aimX, float aimY,
                                float range, float halfWidth) {
        if (BossBody.hasBounds(this)) {
            float left = BossBody.left(this), bottom = BossBody.bottom(this);
            Rectangle body = new Rectangle(left - halfWidth, bottom - halfWidth,
                BossBody.right(this) - left + halfWidth * 2, BossBody.top(this) - bottom + halfWidth * 2);
            return Intersector.intersectSegmentRectangle(new Vector2(originX, originY),
                new Vector2(originX + aimX * range, originY + aimY * range), body);
        }
        float toX = x - originX, toY = y - originY;
        float along = toX * aimX + toY * aimY;
        float across = Math.abs(toX * aimY - toY * aimX);
        return along >= -radius && along <= range + radius && across <= halfWidth + radius;
    }
    protected final void updateStandard(CombatRoom room, float dt,
                                        PlayerCharacter player, RunPlan plan) {
        if (this.stunTime > 0) return;
        boolean warping = tryWarp(room, dt, player, plan);
        boolean special = !warping && useSpecial(room, dt, player, plan);
        float dx = player.x - this.x, dy = player.y - this.y;
        float length = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        if (!warping && !special && this.kind < 0) {
            float speed = this.speed * (this.slowTime > 0 ? .48f : 1f);
            if (this.kind == CombatRoom.RANGED || this.artillery) {
                float desired = this.artillery ? 330 : 290;
                boolean blocked = room.obstacles.hitFraction(this.x, this.y, player.x, player.y, 9) >= 0;
                if (blocked || length > desired + 35) {
                    room.obstacles.moveToward(this, player.x, player.y, speed * dt, this.radius + 20);
                } else if (length < desired - 35) {
                    room.obstacles.moveEnemy(this, -dx / length * speed * dt, -dy / length * speed * dt);
                }
            } else if (length > this.radius + 20) {
                room.obstacles.moveToward(this, player.x, player.y, speed * dt, this.radius + 20);
            }
        }
        if (this.anchored) {
            this.x = this.homeX;
            this.y = this.homeY;
        } else {
            this.x = MathUtils.clamp(this.x, GameSpace.LEFT + this.radius,
                GameSpace.RIGHT - this.radius);
            this.y = MathUtils.clamp(this.y, GameSpace.BOTTOM + this.radius,
                GameSpace.WALK_TOP - this.radius);
        }
        if (!warping && length < this.radius + 18 && this.contactTimer <= 0) {
            player.hurt(this.damage);
            this.contactTimer = .75f;
        }
        float cost = this.kind < 0 ? this.maxSp : 28;
        if (!warping && !special && this.kind != CombatRoom.WARPER && this.sp >= cost
            && this.skillTimer <= 0) {
            this.sp -= cost;
            this.skillTimer = this.kind < 0 ? 1.1f
                : this.kind == RunPlan.AE_ID ? 1.1f
                : this.artillery ? 1.05f : this.waveCaster ? .85f : .68f;
            EnemyProjectilePatterns.fire(room, this, player, plan);
        }
    }

    protected boolean tryWarp(CombatRoom room, float dt,
                              PlayerCharacter player, RunPlan plan) { return false; }

    protected boolean useSpecial(CombatRoom room, float dt,
                                 PlayerCharacter player, RunPlan plan) { return false; }

}

/** Projectile patterns shared by ordinary monsters and Ajarn combat families. */
final class EnemyProjectilePatterns {
    private EnemyProjectilePatterns() { }

    static void fire(CombatRoom room, Enemy enemy,
                     PlayerCharacter player, RunPlan plan) {
        float angle = MathUtils.atan2(player.y - enemy.y, player.x - enemy.x);
        float speed = (enemy.kind < 0 ? 225 : enemy.kind == RunPlan.AE_ID ? 220 : 275) * plan.speedScale();
        float damage = (enemy.kind < 0 ? 5 : enemy.kind == RunPlan.AE_ID ? 8 : 12) * plan.damageScale();
        if (enemy.kind == CombatRoom.BINDER) {
            CombatRoom.Shot bindShot = new CombatRoom.Shot(enemy.x, enemy.y,
                MathUtils.cos(angle) * speed * .85f,
                MathUtils.sin(angle) * speed * .85f,
                9, 3 * plan.damageScale(), 3f, false);
            bindShot.binding = true;
            room.addEnemyShot(enemy, bindShot);
            return;
        }
        if (enemy.kind == CombatRoom.WAVE || enemy.waveCaster) {
            int count = enemy.waveCaster ? 3 : 1;
            for (int i = 0; i < count; i++) {
                float direction = angle + (i - (count - 1) / 2f) * .30f;
                CombatRoom.Shot waveShot = new CombatRoom.Shot(enemy.x, enemy.y,
                    MathUtils.cos(direction) * speed,
                    MathUtils.sin(direction) * speed,
                    enemy.waveCaster ? 25 : 20,
                    (enemy.waveCaster ? 15 : 9) * plan.damageScale(),
                    3f, false);
                waveShot.wave = true;
                room.addEnemyShot(enemy, waveShot);
            }
            return;
        }
        if (enemy.artillery) {
            CombatRoom.Shot giantShot = new CombatRoom.Shot(enemy.x, enemy.y,
                MathUtils.cos(angle) * 195 * plan.speedScale(),
                MathUtils.sin(angle) * 195 * plan.speedScale(),
                38, 24 * plan.damageScale(), 3.5f, false);
            giantShot.giant = true;
            room.addEnemyShot(enemy, giantShot);
            return;
        }
        if (enemy.kind == CombatRoom.RANGED) {
            fireSingle(room, enemy, angle, 325 * plan.speedScale(),
                9 * plan.damageScale());
            return;
        }
        int pattern = enemy.kind < 0 ? 0 : enemy.kind % 4;
        if (pattern == 2) {
            for (int i = 0; i < 8; i++)
                fireSingle(room, enemy, i * MathUtils.PI2 / 8, speed * .8f, damage);
        } else if (pattern == 1 || pattern == 3) {
            for (int i = -1; i <= 1; i++)
                fireSingle(room, enemy,
                    angle + i * (pattern == 1 ? .22f : .4f), speed, damage);
        } else {
            fireSingle(room, enemy, angle, speed, damage);
        }
    }

    private static void fireSingle(CombatRoom room, Enemy enemy,
                                   float angle, float speed, float damage) {
        CombatRoom.Shot shot = new CombatRoom.Shot(enemy.x, enemy.y,
            MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed,
            7, damage, 3, false);
        shot.algoBall = enemy instanceof Monster
            && ((Monster)enemy).appearance() == Monster.Appearance.ALGORITHM;
        room.addEnemyShot(enemy, shot);
    }
}
