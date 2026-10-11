package io.github.kirinojuju.combat;

// File role: Owns the current room's enemies, shots, hit detection, damage and temporary combat effects.

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.enemy.EnemyBehavior.EnemyBehaviors;
import io.github.kirinojuju.enemy.EnemyBehavior;
import io.github.kirinojuju.enemy.EnemyFactory;
import io.github.kirinojuju.enemy.EnemySpacing;
import io.github.kirinojuju.enemy.boss.DomeBossBehavior;
import io.github.kirinojuju.enemy.boss.BossBody;
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
    public static final float FAR_CAST_DURATION = 1.8f;
    public static final float FAR_SAFE_RADIUS = 76f;
    public static final float SATI_CHARGE_DURATION = 1.05f;
    public static final float NOTEBOOK_BEAM_RANGE = 760;
    public static final float NOTEBOOK_BEAM_HALF_WIDTH = 42;
    public static final float NOTEBOOK_IMPACT_DURATION = .36f;
    public static class Enemy {
        public float x, y, radius, hp, maxHp, sp, maxSp, speed, damage;
        public float contactTimer, skillTimer, armor, maxArmor, reflectTimer;
        public float attackAnimationTime;
        // Ae and Far sprites update this ratio to match their actual body artwork.
        public float bodyAspectRatio = 1f;
        public float warpCooldown, warpWindup, warpX, warpY, warpReturnTime;
        public final float homeX, homeY;
        public int warpStrikes;
        public float satiCooldown, satiCharge, satiAimX, satiAimY, animationTime;
        public float farCooldown, farCharge, farSafeX, farSafeY, farFlashTime;
        public float catTransformTime, catDivideTime;
        public float swordAttackTime;
        public boolean catFacingRight, catMoving;
        public float stunTime, slowTime, poisonTime, poisonDps;
        public float tetherTime, tetherCooldown;
        public float exStateTime, exMeter, exAimX, exAimY, exFlashTime, exObjectTimer;
        public int exState, exStoredDust, exConfidence;
        public boolean reflector, warper, artillery, waveCaster;
        public boolean summoner;
        public float summonTimer = 3.8f;
        public boolean anchored;
        public final EnemyBehavior behavior;
        public final DomeBossBehavior.State dome;
        public final int kind; // Negative: monster class; 0..7 Ajarn; 8..10 EX.

        // Create enemy stats and select a behavior from its kind; subclasses can supply their own behavior.
        public Enemy(float x, float y, float radius, float hp, float maxSp,
              float speed, float damage, int kind) {
            this(x, y, radius, hp, maxSp, speed, damage, kind, EnemyBehaviors.forKind(kind));
        }

        // Create enemy stats and select a behavior from its kind; subclasses can supply their own behavior.
        protected Enemy(float x, float y, float radius, float hp, float maxSp,
                        float speed, float damage, int kind, EnemyBehavior behavior) {
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
            this.behavior = behavior;
            this.dome = kind == RunPlan.DOME_ID ? new DomeBossBehavior.State() : null;
        }

        // Run this enemy's AI for one frame using the room, player and difficulty plan.
        public void updateBehavior(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
            behavior.update(room, this, dt, player, plan);
        }

        // Damage geometry is separate from the radius used for movement and contact.
        // Return the horizontal hit-test point; custom bosses can use the nearest point on their body.
        public float damageX(float sourceX) {
            float width = BossBody.width(this);
            return width > 0 ? MathUtils.clamp(sourceX, x - width / 2, x + width / 2) : x;
        }
        // Return the vertical hit-test point; custom bosses can use the nearest point on their body.
        public float damageY(float sourceY) {
            float height = BossBody.height(this);
            return height > 0 ? MathUtils.clamp(sourceY, y + radius - height, y + radius) : y;
        }
        // Return the radius used by incoming attacks, separately from movement collision.
        public float damageRadius() { return BossBody.height(this) > 0 ? 0 : radius; }
        // Return the horizontal point that player attacks should aim toward.
        public float aimX() { return x; }
        // Return the vertical point that player attacks should aim toward.
        public float aimY() {
            float height = BossBody.height(this);
            return height > 0 ? y + radius - height / 2 : y;
        }

        // Check whether the enemy's damage shape touches the directed beam.
        public boolean overlapsBeam(float originX, float originY, float aimX, float aimY,
                                    float range, float halfWidth) {
            float height = BossBody.height(this);
            if (height > 0) {
                float width = BossBody.width(this);
                Rectangle body = new Rectangle(x - width / 2 - halfWidth,
                    y + radius - height - halfWidth, width + halfWidth * 2, height + halfWidth * 2);
                return Intersector.intersectSegmentRectangle(new Vector2(originX, originY),
                    new Vector2(originX + aimX * range, originY + aimY * range), body);
            }
            float toX = x - originX, toY = y - originY;
            float along = toX * aimX + toY * aimY;
            float across = Math.abs(toX * aimY - toY * aimX);
            return along >= -radius && along <= range + radius && across <= halfWidth + radius;
        }
    }

    public static final class ExObject {
        public float x, y, vx, vy, radius, life, ejectionTime;
        public int kind;

        // Create a temporary EX object such as dust, a note or a slime patch.
        public ExObject(float x, float y, float radius, float life, int kind) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.life = life;
            this.kind = kind;
        }
    }

    // all power boss & gigi
    public static final class Shot {
        public float x, y, vx, vy, radius, damage, life;
        public float animationTime;
        public boolean friendly, powerBall, binding, reflected, wave, giant, satiSpear;
        public boolean liquidCatBullet;
        public boolean algoBall;
        public boolean poisonous, splash, notebook;
        public boolean ignoresTables;
        public Enemy target;

        // Create a moving projectile with its damage, lifetime and friendly or enemy ownership.
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

        // Store a separate hit animation at this position so rapid hits can overlap.
        public NotebookImpact(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    // create to stored enemy
    public final Array<Enemy> enemies = new Array<Enemy>();
    public final Array<Enemy> spawnQueue = new Array<Enemy>();
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

    // random room
    // Create the room's enemy factory using the supplied random generator.
    public CombatRoom(Random random) {
        this.random = random;
        this.factory = new EnemyFactory(random);
    }
    // Connect this system to the shared game sound player.
    public void setAudio(GameAudio audio) { this.audio = audio; }

    // when start game will clear status = 0
    // Clear the previous fight, place tables and spawn enemies for the requested room.
    public void begin(RunDirector.Room room, RunPlan plan, int exId) {
        enemies.clear();
        spawnQueue.clear();
        shots.clear();
        notebookImpacts.clear();
        exObjects.clear();
        carriedNotes = 0;
        blastTime = splashTime = beamTime = 0;
        factory.populate(this, room, plan, exId);
        obstacles.begin(room, random);
        for (Enemy enemy : enemies) obstacles.resolveEnemy(enemy);
    }

    // Boss attack above object
    /** Boss shots travel above furniture; ordinary monster shots still hit cover. */
    public void addEnemyShot(Enemy source, Shot shot) {
        source.attackAnimationTime = .4f;
        shot.ignoresTables = source.kind >= 0;
        shots.add(shot);
    }

    // Sati boss draw effect boss (C++), stay at top right of screen
    // Place Sati's element 72 units toward the middle of the screen, within arena limits.
    public static float satiElementX(Enemy boss) {
        float side = boss.x > GameSpace.WIDTH / 2 ? -1 : 1;
        return MathUtils.clamp(boss.x + side * 72,
            GameSpace.LEFT + 35, GameSpace.RIGHT - 35);
    }

    // Sati boss draw stay above boss a little bit
    // Place Sati's element 25 units above the boss, within the walkable area.
    public static float satiElementY(Enemy boss) {
        return MathUtils.clamp(boss.y + 25,
            GameSpace.BOTTOM + 35, GameSpace.WALK_TOP - 35);
    }

    // hitbox of monster (donot to mix each other)
    // Find the nearest living enemy using its damage shape, including Ken's full body.
    public Enemy closestEnemy(float x, float y) {
        Enemy result = null;
        float best = Float.MAX_VALUE;
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.damageX(x) - x, dy = enemy.damageY(y) - y;
            float distance = dx * dx + dy * dy;
            if (distance < best) {
                best = distance;
                result = enemy;
            }
        }
        return result;
    }

    // effect skill splash, draw skill, if in area go function enemy
    // Damage every living enemy whose hit area overlaps a circle centered at x and y.
    public void damageNearby(float x, float y, float reach, float damage) {
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.damageX(x) - x, dy = enemy.damageY(y) - y;
            if (dx * dx + dy * dy <= (enemy.damageRadius() + reach) * (enemy.damageRadius() + reach))
                damageEnemy(enemy, damage, x, y);
        }
    }

    //
    // Hit living enemies inside the facing melee arc; the overload also applies slow and tether.
    public void damageArc(float x, float y, float facingX, float facingY,
                   float reach, float halfAngleDegrees, float damage) {
        damageArc(x, y, facingX, facingY, reach, halfAngleDegrees,
            damage, false, false);
    }

    // Hit living enemies inside the facing melee arc; the overload also applies slow and tether.
    public void damageArc(float x, float y, float facingX, float facingY,
                   float reach, float halfAngleDegrees, float damage,
                   boolean slow, boolean tether) {
        float threshold = MathUtils.cosDeg(halfAngleDegrees);
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.damageX(x) - x, dy = enemy.damageY(y) - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            if (distance > reach + enemy.damageRadius()) continue;
            if (distance < .001f || (dx * facingX + dy * facingY) / distance >= threshold) {
                damageEnemy(enemy, damage, x, y);
                if (slow) enemy.slowTime = Math.max(enemy.slowTime, 1.5f);
                if (tether && enemy.hp > 0)
                    tetherEnemy(enemy, x, y, facingX, facingY);
            }
        }
    }

    // Temporarily hold the enemy in place and pull it toward the melee hit.
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

    // Apply an incoming hit through armor and HP, with enemy-specific damage modifiers.
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

    // Create a player projectile; the overload attaches poison, splash and Notebook flags.
    public Shot fireFriendly(float x, float y, float vx, float vy, float damage, Enemy target) {
        return fireFriendly(x, y, vx, vy, damage, target, false, false);
    }

    // Create a player projectile; the overload attaches poison, splash and Notebook flags.
    public Shot fireFriendly(float x, float y, float vx, float vy, float damage,
                      Enemy target, boolean poisonous, boolean splash) {
        Shot shot = new Shot(x, y, vx, vy, 9, damage, 1.4f, true);
        shot.target = target;
        shot.poisonous = poisonous;
        shot.splash = splash;
        shots.add(shot);
        return shot;
    }

    // Launch the player's homing skill projectile, which explodes on impact.
    public void firePowerBall(float x, float y, float vx, float vy, float damage, Enemy target) {
        Shot shot = new Shot(x, y, vx, vy, 40, damage, 2.4f, true);
        shot.powerBall = true;
        shot.target = target;
        shots.add(shot);
    }

    // Damage enemies along the Notebook beam and store its short visual effect.
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
            if (!enemy.overlapsBeam(x, y, aimX, aimY, NOTEBOOK_BEAM_RANGE, NOTEBOOK_BEAM_HALF_WIDTH)) continue;
            damageEnemy(enemy, damage, x, y);
            if (poisonous && enemy.hp > 0) poison(enemy, damage);
        }
    }

    // Apply the skill's area damage and crowd-control effects, then show the blast.
    private void explodePowerBall(Shot shot, float sourceX, float sourceY) {
        if (audio != null) audio.skillImpact();
        blastX = MathUtils.clamp(shot.x, GameSpace.LEFT, GameSpace.RIGHT);
        blastY = MathUtils.clamp(shot.y, GameSpace.BOTTOM, GameSpace.TOP);
        blastTime = .30f;
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.damageX(blastX) - blastX, dy = enemy.damageY(blastY) - blastY;
            float reach = POWER_BALL_BLAST_RADIUS + enemy.damageRadius();
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

    // Advance enemies, shots and effects; return whether all enemies in the room are defeated.
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

    // Apply poison, recover SP, advance enemy timers and AI, then append queued summons.
    private void updateEnemies(float dt, PlayerCharacter player, RunPlan plan) {
        spawnQueue.clear();
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
            if (enemy.summoner && enemy.stunTime <= 0) queueSummons(enemy, dt, plan);
            enemy.updateBehavior(this, dt, player, plan);
        }
        enemies.addAll(spawnQueue);
    }

    // Queue helpers for summoning bosses without modifying enemies during iteration.
    private void queueSummons(Enemy boss, float dt, RunPlan plan) {
        boss.summonTimer -= dt;
        if (boss.summonTimer > 0) return;
        boss.summonTimer = Math.max(3f, 5.2f / plan.speedScale());
        int living = 0;
        // updateEnemies is already iterating this Array; LibGDX reuses its iterators.
        for (int i = 0; i < enemies.size; i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.kind < 0 && enemy.hp > 0) living++;
        }
        int count = Math.min(plan.loop() == 1 ? 1 : 2, 4 - living - spawnQueue.size);
        for (int i = 0; i < count; i++) {
            int[] specials = {BINDER, WAVE, RANGED, WARPER};
            int kind = specials[random.nextInt(plan.loop() == 1 ? 3 : specials.length)];
            Enemy minion = factory.createMonster(boss.homeX - 85 - i * 65,
                boss.homeY + (i == 0 ? -55 : 55), kind, plan);
            minion.maxArmor = (28 + plan.stage() * 5) * plan.healthScale();
            minion.armor = minion.maxArmor;
            spawnQueue.add(minion);
        }
    }

    // Count active EX objects of the requested kind.
    public int countExObjects(int kind) {
        int count = 0;
        for (ExObject object : exObjects) if (object.kind == kind) count++;
        return count;
    }

    // Move an EX enemy toward a target through the room's obstacle-aware movement.
    public void moveExToward(Enemy enemy, float targetX, float targetY, float step, float stopDistance) {
        obstacles.moveToward(enemy, targetX, targetY, step, stopDistance);
    }

    // Advance animation time and reduce enemy cooldowns and temporary status timers.
    private static void updateEnemyTimers(Enemy enemy, float dt) {
        enemy.animationTime += dt;
        enemy.contactTimer -= dt;
        enemy.skillTimer -= dt;
        enemy.attackAnimationTime = Math.max(0, enemy.attackAnimationTime - dt);
        enemy.reflectTimer -= dt;
        enemy.stunTime = Math.max(0, enemy.stunTime - dt);
        enemy.slowTime = Math.max(0, enemy.slowTime - dt);
        enemy.tetherTime = Math.max(0, enemy.tetherTime - dt);
        enemy.tetherCooldown = Math.max(0, enemy.tetherCooldown - dt);
        enemy.exFlashTime = Math.max(0, enemy.exFlashTime - dt);
        enemy.catTransformTime = Math.max(0, enemy.catTransformTime - dt);
        enemy.catDivideTime = Math.max(0, enemy.catDivideTime - dt);
        enemy.farFlashTime = Math.max(0, enemy.farFlashTime - dt);
        if (enemy.kind == LIQUID_CAT) enemy.catMoving = false;
    }

    // Move and steer projectiles, resolve collisions and remove expired or consumed shots.
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

    // Check a friendly shot against enemy damage shapes and apply its hit effects.
    private boolean hitEnemyWithShot(Shot shot, PlayerCharacter player) {
        for (int i = 0; i < enemies.size; i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.hp <= 0 || !shotTouches(shot, enemy.damageX(shot.x), enemy.damageY(shot.y),
                enemy.damageRadius())) continue;
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

    // Check an enemy shot against the player and apply damage or binding.
    private static boolean hitPlayerWithShot(Shot shot, PlayerCharacter player) {
        if (!shotTouches(shot, player.x, player.y, 17)) return false;
        boolean vulnerable = player.invulnerable <= 0;
        player.hurt(shot.damage);
        if (vulnerable && shot.binding) player.bind(3.5f);
        return true;
    }

    // Test whether a projectile circle overlaps another circle.
    private static boolean shotTouches(Shot shot, float x, float y, float radius) {
        float dx = shot.x - x, dy = shot.y - y;
        float reach = shot.radius + radius;
        return dx * dx + dy * dy < reach * reach;
    }

    // Advance and remove the short Notebook hit animations.
    private void updateNotebookImpacts(float dt) {
        for (int i = notebookImpacts.size - 1; i >= 0; i--) {
            NotebookImpact impact = notebookImpacts.get(i);
            impact.time += dt;
            if (impact.time >= NOTEBOOK_IMPACT_DURATION) notebookImpacts.removeIndex(i);
        }
    }

    // Remove dead enemies and reward the player with SP.
    private void removeDefeatedEnemies(PlayerCharacter player) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            if (enemies.get(i).hp <= 0) {
                enemies.removeIndex(i);
                player.recoverSp(5);
            }
        }
    }

    // Advance dust, notes and slime patches and handle their effects on the player.
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

    // Apply poison and splash effects carried by a Notebook projectile.
    private void applyNotebookEffect(Enemy hit, Shot shot) {
        if (shot.poisonous && hit.hp > 0) poison(hit, shot.damage);
        if (!shot.splash) return;
        splashX = shot.x;
        splashY = shot.y;
        splashTime = .24f;
        for (Enemy enemy : enemies) {
            if (enemy == hit || enemy.hp <= 0) continue;
            float dx = enemy.damageX(splashX) - splashX, dy = enemy.damageY(splashY) - splashY;
            float reach = 72 + enemy.damageRadius();
            if (dx * dx + dy * dy > reach * reach) continue;
            damageEnemy(enemy, shot.damage * .55f, shot.x, shot.y);
            if (shot.poisonous && enemy.hp > 0) poison(enemy, shot.damage);
        }
    }

    // Start or refresh the enemy's poison effect using the incoming hit damage.
    private void poison(Enemy enemy, float hitDamage) {
        enemy.poisonTime = Math.max(enemy.poisonTime, 3f);
        enemy.poisonDps = Math.max(enemy.poisonDps, hitDamage * .15f);
    }

    // Turn a targeted projectile toward the target's current aim point.
    private void steerHomingShot(Shot shot) {
        if (shot.target == null || shot.target.hp <= 0)
            shot.target = closestEnemy(shot.x, shot.y);
        if (shot.target == null) return;
        float dx = shot.target.aimX() - shot.x, dy = shot.target.aimY() - shot.y;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length < .001f) return;
        float speed = (float)Math.sqrt(shot.vx * shot.vx + shot.vy * shot.vy);
        shot.vx = dx / length * speed;
        shot.vy = dy / length * speed;
    }

}
