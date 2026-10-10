package io.github.kirinojuju.combat;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.enemy.EnemyBehavior.EnemyBehaviors;
import io.github.kirinojuju.enemy.EnemyBehavior;
import io.github.kirinojuju.enemy.EnemyFactory;
import io.github.kirinojuju.enemy.EnemySpacing;
import io.github.kirinojuju.enemy.boss.DomeBossBehavior;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;
import io.github.kirinojuju.world.RoomObstacles;
import java.util.Random;

/** Owns one room's enemies, projectiles, collision and damage. */
public final class CombatRoom {
    public static final int BASIC = -1, BINDER = -2, WARPER = -3;
    public static final int WAVE = -4, RANGED = -5, SHIELDED = -6;
    public static final int LIQUID_CAT = 8, ALLERGIC_VACUUM = 9, INSECURE_SWORD = 10;
    public static final int DUST = 0, NOTE = 1, SLIME = 2;
    public static final float POWER_BALL_BLAST_RADIUS = 130;
    public static final float CAT_LEAP_WINDUP = .48f;
    public static final float CAT_TRANSFORM_DURATION = .45f;
    public static final float CAT_DIVIDE_DURATION = .65f;
    public static final float TALGO_CAST_DURATION = 1.8f;
    public static final float TALGO_SAFE_RADIUS = 76f;
    public static final float SATI_CHARGE_DURATION = 1.05f;
    public static final float NOTEBOOK_BEAM_RANGE = 760;
    public static final float NOTEBOOK_BEAM_HALF_WIDTH = 42;
    public static final float NOTEBOOK_IMPACT_DURATION = .36f;
    public static final class Enemy {
        public float x, y, radius, hp, maxHp, sp, maxSp, speed, damage;
        public float contactTimer, skillTimer, armor, maxArmor, reflectTimer;
        public float warpCooldown, warpWindup, warpX, warpY, warpReturnTime;
        public final float homeX, homeY;
        public int warpStrikes;
        public float satiCooldown, satiCharge, satiAimX, satiAimY, animationTime;
        public float talgoCooldown, talgoCharge, talgoSafeX, talgoSafeY, talgoFlashTime;
        public float catTransformTime, catDivideTime;
        public float swordAttackTime;
        public boolean catFacingRight, catMoving;
        public float stunTime, slowTime, poisonTime, poisonDps;
        public float tetherTime, tetherCooldown;
        public float exStateTime, exMeter, exAimX, exAimY, exFlashTime, exObjectTimer;
        public int exState, exStoredDust, exConfidence;
        public boolean reflector, warper, artillery, waveCaster;
        public boolean anchored;
        public final EnemyBehavior behavior;
        public final DomeBossBehavior.State dome;
        public final int kind; // Negative: monster class; 0..7 Ajarn; 8..10 EX.

        public Enemy(float x, float y, float radius, float hp, float maxSp,
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
            this.behavior = EnemyBehaviors.forKind(kind);
            this.dome = kind == RunPlan.DOME_ID ? new DomeBossBehavior.State() : null;
        }
    }

    public static final class ExObject {
        public float x, y, vx, vy, radius, life, ejectionTime;
        public int kind;

        public ExObject(float x, float y, float radius, float life, int kind) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.life = life;
            this.kind = kind;
        }
    }

    public static final class Shot {
        public float x, y, vx, vy, radius, damage, life;
        public float animationTime;
        public boolean friendly, powerBall, binding, reflected, wave, giant, satiSpear;
        public boolean liquidCatBullet;
        public boolean poisonous, splash, notebook;
        public boolean ignoresTables;
        public Enemy target;

        public Shot(float x, float y, float vx, float vy, float radius,
             float damage, float life, boolean friendly) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.damage = damage;
            this.life = life;
            this.friendly = friendly;
        }
    }

    /** Visual only: each hit keeps its own animation, even during rapid fire. */
    public static final class NotebookImpact {
        public final float x, y;
        public float time;

        public NotebookImpact(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public final Array<Enemy> enemies = new Array<Enemy>();
    public final Array<Shot> shots = new Array<Shot>();
    public final Array<NotebookImpact> notebookImpacts = new Array<NotebookImpact>();
    public final Array<ExObject> exObjects = new Array<ExObject>();
    public int carriedNotes;
    public final Random random;
    public final EnemyFactory factory;
    public final RoomObstacles obstacles = new RoomObstacles();
    public GameAudio audio;
    public float blastX, blastY, blastTime, splashX, splashY, splashTime;
    public float beamX, beamY, beamEndX, beamEndY, beamTime;

    public CombatRoom(Random random) {
        this.random = random;
        this.factory = new EnemyFactory(random);
    }
    public void setAudio(GameAudio audio) { this.audio = audio; }

    public void begin(RunDirector.Room room, RunPlan plan, int exId) {
        enemies.clear();
        shots.clear();
        notebookImpacts.clear();
        exObjects.clear();
        carriedNotes = 0;
        blastTime = splashTime = beamTime = 0;
        factory.populate(this, room, plan, exId);
        obstacles.begin(room, random);
        for (Enemy enemy : enemies) obstacles.resolveEnemy(enemy);
    }

    /** Boss shots travel above furniture; ordinary monster shots still hit cover. */
    public void addEnemyShot(Enemy source, Shot shot) {
        shot.ignoresTables = source.kind >= 0;
        shots.add(shot);
    }

    public static float satiElementX(Enemy boss) {
        float side = boss.x > GameSpace.WIDTH / 2 ? -1 : 1;
        return MathUtils.clamp(boss.x + side * 72,
            GameSpace.LEFT + 35, GameSpace.RIGHT - 35);
    }

    public static float satiElementY(Enemy boss) {
        return MathUtils.clamp(boss.y + 25,
            GameSpace.BOTTOM + 35, GameSpace.WALK_TOP - 35);
    }

    public Enemy closestEnemy(float x, float y) {
        Enemy result = null;
        float best = Float.MAX_VALUE;
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.x - x, dy = enemy.y - y;
            float distance = dx * dx + dy * dy;
            if (distance < best) {
                best = distance;
                result = enemy;
            }
        }
        return result;
    }

    public void damageNearby(float x, float y, float reach, float damage) {
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.x - x, dy = enemy.y - y;
            if (dx * dx + dy * dy <= (enemy.radius + reach) * (enemy.radius + reach))
                damageEnemy(enemy, damage, x, y);
        }
    }

    public void damageArc(float x, float y, float facingX, float facingY,
                   float reach, float halfAngleDegrees, float damage) {
        damageArc(x, y, facingX, facingY, reach, halfAngleDegrees,
            damage, false, false);
    }

    public void damageArc(float x, float y, float facingX, float facingY,
                   float reach, float halfAngleDegrees, float damage,
                   boolean slow, boolean tether) {
        float threshold = MathUtils.cosDeg(halfAngleDegrees);
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.x - x, dy = enemy.y - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            if (distance > reach + enemy.radius) continue;
            if (distance < .001f || (dx * facingX + dy * facingY) / distance >= threshold) {
                damageEnemy(enemy, damage, x, y);
                if (slow) enemy.slowTime = Math.max(enemy.slowTime, 1.5f);
                if (tether && enemy.hp > 0)
                    tetherEnemy(enemy, x, y, facingX, facingY);
            }
        }
    }

    private void tetherEnemy(Enemy enemy, float x, float y,
                             float facingX, float facingY) {
        if (enemy.tetherCooldown > 0) return;
        enemy.tetherTime = 1.7f;
        enemy.tetherCooldown = 2.3f;
        if (enemy.kind >= 0) return; // Ajarns and EX monsters keep their position.
        float gatherX = x + facingX * 70;
        float gatherY = y + facingY * 70;
        enemy.x = MathUtils.clamp(enemy.x + (gatherX - enemy.x) * .75f,
            GameSpace.LEFT + enemy.radius, GameSpace.RIGHT - enemy.radius);
        enemy.y = MathUtils.clamp(enemy.y + (gatherY - enemy.y) * .75f,
            GameSpace.BOTTOM + enemy.radius, GameSpace.WALK_TOP - enemy.radius);
        enemy.stunTime = Math.max(enemy.stunTime, .45f);
    }

    private void damageEnemy(Enemy enemy, float damage, float sourceX, float sourceY) {
        if (enemy.kind == LIQUID_CAT && enemy.exState == 1) damage *= .55f;
        if (enemy.kind == INSECURE_SWORD && enemy.exState == 2) damage *= 1.5f;
        boolean armored = enemy.armor > 0;
        if (enemy.armor > 0) {
            enemy.armor -= damage;
            if (enemy.reflector && enemy.reflectTimer <= 0) {
                float angle = MathUtils.atan2(sourceY - enemy.y, sourceX - enemy.x);
                Shot counter = new Shot(enemy.x, enemy.y,
                    MathUtils.cos(angle) * 320, MathUtils.sin(angle) * 320,
                    9, enemy.damage * .65f, 2.5f, false);
                counter.reflected = true;
                addEnemyShot(enemy, counter);
                enemy.reflectTimer = .55f;
            }
            if (enemy.armor < 0) enemy.hp += enemy.armor;
            enemy.armor = Math.max(0, enemy.armor);
        } else {
            enemy.hp -= damage;
        }
        if (audio != null) audio.hit(armored);
    }

    public Shot fireFriendly(float x, float y, float vx, float vy, float damage, Enemy target) {
        return fireFriendly(x, y, vx, vy, damage, target, false, false);
    }

    public Shot fireFriendly(float x, float y, float vx, float vy, float damage,
                      Enemy target, boolean poisonous, boolean splash) {
        Shot shot = new Shot(x, y, vx, vy, 9, damage, 1.4f, true);
        shot.target = target;
        shot.poisonous = poisonous;
        shot.splash = splash;
        shots.add(shot);
        return shot;
    }

    public void firePowerBall(float x, float y, float vx, float vy, float damage, Enemy target) {
        Shot shot = new Shot(x, y, vx, vy, 40, damage, 2.4f, true);
        shot.powerBall = true;
        shot.target = target;
        shots.add(shot);
    }

    public void fireNotebookBeam(float x, float y, float dx, float dy,
                          float damage, boolean poisonous) {
        float length = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        float aimX = dx / length, aimY = dy / length;
        beamX = x;
        beamY = y;
        beamEndX = x + aimX * NOTEBOOK_BEAM_RANGE;
        beamEndY = y + aimY * NOTEBOOK_BEAM_RANGE;
        beamTime = .30f;
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float toX = enemy.x - x, toY = enemy.y - y;
            float along = toX * aimX + toY * aimY;
            float across = Math.abs(toX * aimY - toY * aimX);
            if (along < -enemy.radius || along > NOTEBOOK_BEAM_RANGE + enemy.radius
                || across > NOTEBOOK_BEAM_HALF_WIDTH + enemy.radius) continue;
            damageEnemy(enemy, damage, x, y);
            if (poisonous && enemy.hp > 0) poison(enemy, damage);
        }
    }

    private void explodePowerBall(Shot shot, float sourceX, float sourceY) {
        if (audio != null) audio.skillImpact();
        blastX = MathUtils.clamp(shot.x, GameSpace.LEFT, GameSpace.RIGHT);
        blastY = MathUtils.clamp(shot.y, GameSpace.BOTTOM, GameSpace.TOP);
        blastTime = .30f;
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.x - blastX, dy = enemy.y - blastY;
            float reach = POWER_BALL_BLAST_RADIUS + enemy.radius;
            if (dx * dx + dy * dy > reach * reach) continue;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            float falloff = 1f - .35f * Math.min(1f, distance / reach);
            damageEnemy(enemy, shot.damage * falloff
                * (enemy.tetherTime > 0 ? 1.35f : 1f), sourceX, sourceY);
            if (enemy.hp > 0) {
                float pushX = dx, pushY = dy;
                if (distance < .001f) {
                    pushX = enemy.x - sourceX;
                    pushY = enemy.y - sourceY;
                }
                float pushLength = (float)Math.sqrt(pushX * pushX + pushY * pushY);
                if (pushLength < .001f) { pushX = 1; pushY = 0; pushLength = 1; }
                float push = (enemy.kind >= 0 ? 16f : 92f)
                    * (1f - .35f * Math.min(1f, distance / reach));
                if (!enemy.anchored) {
                    float nextX = MathUtils.clamp(enemy.x + pushX / pushLength * push,
                        GameSpace.LEFT + enemy.radius, GameSpace.RIGHT - enemy.radius);
                    float nextY = MathUtils.clamp(enemy.y + pushY / pushLength * push,
                        GameSpace.BOTTOM + enemy.radius, GameSpace.WALK_TOP - enemy.radius);
                    obstacles.moveEnemy(enemy, nextX - enemy.x, nextY - enemy.y);
                }
            }
            enemy.stunTime = Math.max(enemy.stunTime, enemy.kind >= 0 ? .28f : .70f);
            enemy.slowTime = Math.max(enemy.slowTime, enemy.kind >= 0 ? 1.1f : 2.0f);
        }
    }

    public boolean update(float dt, PlayerCharacter player, RunPlan plan) {
        blastTime = Math.max(0, blastTime - dt);
        splashTime = Math.max(0, splashTime - dt);
        beamTime = Math.max(0, beamTime - dt);
        updateNotebookImpacts(dt);
        updateExObjects(dt, player, plan);
        updateEnemies(dt, player, plan);
        EnemySpacing.separate(enemies);
        for (Enemy enemy : enemies) obstacles.resolveEnemy(enemy);
        updateShots(dt, player);
        for (Enemy enemy : enemies) obstacles.resolveEnemy(enemy);
        removeDefeatedEnemies(player);
        return enemies.size == 0;
    }

    private void updateEnemies(float dt, PlayerCharacter player, RunPlan plan) {
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float poisonStep = Math.min(dt, enemy.poisonTime);
            enemy.poisonTime -= poisonStep;
            enemy.hp -= enemy.poisonDps * poisonStep;
            if (enemy.hp <= 0) continue;
            updateEnemyTimers(enemy, dt);
            float spRecovery = 44;
            if (enemy.kind == RANGED || enemy.kind == WAVE) spRecovery = 20;
            else if (enemy.kind < 0) spRecovery = 11;
            enemy.sp = Math.min(enemy.maxSp, enemy.sp + spRecovery * plan.spScale() * dt);
            enemy.behavior.update(this, enemy, dt, player, plan);
        }
    }

    private static void updateEnemyTimers(Enemy enemy, float dt) {
        enemy.animationTime += dt;
        enemy.contactTimer -= dt;
        enemy.skillTimer -= dt;
        enemy.reflectTimer -= dt;
        enemy.stunTime = Math.max(0, enemy.stunTime - dt);
        enemy.slowTime = Math.max(0, enemy.slowTime - dt);
        enemy.tetherTime = Math.max(0, enemy.tetherTime - dt);
        enemy.tetherCooldown = Math.max(0, enemy.tetherCooldown - dt);
        enemy.exFlashTime = Math.max(0, enemy.exFlashTime - dt);
        enemy.catTransformTime = Math.max(0, enemy.catTransformTime - dt);
        enemy.catDivideTime = Math.max(0, enemy.catDivideTime - dt);
        enemy.talgoFlashTime = Math.max(0, enemy.talgoFlashTime - dt);
        if (enemy.kind == LIQUID_CAT) enemy.catMoving = false;
    }

    private void updateShots(float dt, PlayerCharacter player) {
        for (int i = shots.size - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            if (shot.friendly && shot.target != null) steerHomingShot(shot);
            float oldX = shot.x, oldY = shot.y;
            shot.x += shot.vx * dt;
            shot.y += shot.vy * dt;
            shot.life -= dt;
            shot.animationTime += dt;
            if (shot.wave) shot.radius = Math.min(42, shot.radius + 24 * dt);
            float tableHit = shot.ignoresTables ? -1
                : obstacles.hitFraction(oldX, oldY, shot.x, shot.y, shot.radius);
            if (tableHit >= 0) {
                shot.x = oldX + (shot.x - oldX) * tableHit;
                shot.y = oldY + (shot.y - oldY) * tableHit;
                // An actor touching the front of cover can still be hit first.
                boolean hitActor = shot.friendly ? hitEnemyWithShot(shot, player) : hitPlayerWithShot(shot, player);
                if (!hitActor) {
                    if (shot.powerBall) explodePowerBall(shot, player.x, player.y);
                    else if (shot.notebook) notebookImpacts.add(new NotebookImpact(shot.x, shot.y));
                }
                shots.removeIndex(i);
                continue;
            }
            boolean remove = shot.life <= 0 || shot.x < GameSpace.LEFT
                || shot.x > GameSpace.RIGHT || shot.y < GameSpace.BOTTOM
                || shot.y > GameSpace.TOP;
            if (remove) {
                if (shot.powerBall) explodePowerBall(shot, player.x, player.y);
                shots.removeIndex(i);
                continue;
            }

            if (shot.friendly) remove = hitEnemyWithShot(shot, player);
            else remove = hitPlayerWithShot(shot, player);
            if (remove) shots.removeIndex(i);
        }
    }

    private boolean hitEnemyWithShot(Shot shot, PlayerCharacter player) {
        for (int i = 0; i < enemies.size; i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.hp <= 0 || !shotTouches(shot, enemy.x, enemy.y, enemy.radius)) continue;
            if (shot.powerBall) explodePowerBall(shot, player.x, player.y);
            else {
                damageEnemy(enemy, shot.damage, player.x, player.y);
                applyNotebookEffect(enemy, shot);
                if (shot.notebook) notebookImpacts.add(new NotebookImpact(shot.x, shot.y));
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

    private void updateNotebookImpacts(float dt) {
        for (int i = notebookImpacts.size - 1; i >= 0; i--) {
            NotebookImpact impact = notebookImpacts.get(i);
            impact.time += dt;
            if (impact.time >= NOTEBOOK_IMPACT_DURATION) notebookImpacts.removeIndex(i);
        }
    }

    private void removeDefeatedEnemies(PlayerCharacter player) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            if (enemies.get(i).hp <= 0) {
                enemies.removeIndex(i);
                player.recoverSp(5);
            }
        }
    }

    private void updateExObjects(float dt, PlayerCharacter player, RunPlan plan) {
        for (int i = exObjects.size - 1; i >= 0; i--) {
            ExObject object = exObjects.get(i);
            object.life -= dt;
            if (object.ejectionTime > 0) {
                object.ejectionTime = Math.max(0, object.ejectionTime - dt);
                object.x = MathUtils.clamp(object.x + object.vx * dt,
                    GameSpace.LEFT + object.radius, GameSpace.RIGHT - object.radius);
                object.y = MathUtils.clamp(object.y + object.vy * dt,
                    GameSpace.BOTTOM + object.radius, GameSpace.WALK_TOP - object.radius);
                object.vx *= Math.max(0, 1 - dt * 1.4f);
                object.vy *= Math.max(0, 1 - dt * 1.4f);
            }
            float dx = object.x - player.x, dy = object.y - player.y;
            float reach = object.radius + 18;
            boolean touching = dx * dx + dy * dy <= reach * reach;
            if (touching && object.kind == NOTE) {
                carriedNotes = Math.min(3, carriedNotes + 1);
                exObjects.removeIndex(i);
            } else if (touching && object.kind == SLIME) {
                player.slowTime = Math.max(player.slowTime, .7f);
                player.hurt(3 * plan.damageScale());
            } else if (touching && object.kind == DUST && object.ejectionTime > 0) {
                player.hurt(9 * plan.damageScale());
                object.ejectionTime = 0;
                object.vx = object.vy = 0;
            }
            if (i < exObjects.size && exObjects.get(i) == object && object.life <= 0)
                exObjects.removeIndex(i);
        }
    }

    private void applyNotebookEffect(Enemy hit, Shot shot) {
        if (shot.poisonous && hit.hp > 0) poison(hit, shot.damage);
        if (!shot.splash) return;
        splashX = shot.x;
        splashY = shot.y;
        splashTime = .24f;
        for (Enemy enemy : enemies) {
            if (enemy == hit || enemy.hp <= 0) continue;
            float dx = enemy.x - splashX, dy = enemy.y - splashY;
            float reach = 72 + enemy.radius;
            if (dx * dx + dy * dy > reach * reach) continue;
            damageEnemy(enemy, shot.damage * .55f, shot.x, shot.y);
            if (shot.poisonous && enemy.hp > 0) poison(enemy, shot.damage);
        }
    }

    private void poison(Enemy enemy, float hitDamage) {
        enemy.poisonTime = Math.max(enemy.poisonTime, 3f);
        enemy.poisonDps = Math.max(enemy.poisonDps, hitDamage * .15f);
    }

    private void steerHomingShot(Shot shot) {
        if (shot.target == null || shot.target.hp <= 0)
            shot.target = closestEnemy(shot.x, shot.y);
        if (shot.target == null) return;
        float dx = shot.target.x - shot.x, dy = shot.target.y - shot.y;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length < .001f) return;
        float speed = (float)Math.sqrt(shot.vx * shot.vx + shot.vy * shot.vy);
        shot.vx = dx / length * speed;
        shot.vy = dy / length * speed;
    }

}
