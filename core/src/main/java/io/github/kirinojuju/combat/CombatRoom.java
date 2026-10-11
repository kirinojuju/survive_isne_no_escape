package io.github.kirinojuju.combat;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.enemy.EnemyBehavior.EnemyBehaviors;
import io.github.kirinojuju.enemy.EnemyBehavior;
import io.github.kirinojuju.enemy.EnemyFactory;
import io.github.kirinojuju.enemy.EnemySpacing;
import io.github.kirinojuju.enemy.ex.ExBehavior.ExEnvironment;
import io.github.kirinojuju.enemy.boss.DomeBossBehavior;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;
import io.github.kirinojuju.world.IceWall;
import io.github.kirinojuju.world.RoomObstacles;
import java.util.Random;

/** Owns room state and coordinates each combat frame. Hit rules and projectiles have their own helpers. */
public final class CombatRoom {
    public static final int BASIC = -1, BINDER = -2, WARPER = -3;
    public static final int WAVE = -4, RANGED = -5, SHIELDED = -6;
    public static final int LIQUID_CAT = 8, ALLERGIC_VACUUM = 9, INSECURE_SWORD = 10;
    public static final int DUST = 0, NOTE = 1, SLIME = 2;
    public static final float POWER_BALL_BLAST_RADIUS = 130;
    public static final float NOTEBOOK_BEAM_RANGE = 760;
    public static final float NOTEBOOK_BEAM_HALF_WIDTH = 42;
    public static final float NOTEBOOK_IMPACT_DURATION = .36f;

    /** Shared runtime state; each enemy's behavior owns its decisions and special attacks. */
    public static class Enemy {
        public float x, y, radius, hp, maxHp, sp, maxSp, speed, damage;
        public float contactTimer, skillTimer, armor, maxArmor, reflectTimer;
        // Boss-specific cast state is read by both behaviors and sprites.
        public float warpCooldown, warpWindup, warpX, warpY, warpReturnTime;
        public final float homeX, homeY;
        public int warpStrikes;
        public float satiCooldown, satiCharge, satiAimX, satiAimY, animationTime;
        public float talgoCooldown, talgoCharge, talgoSafeX, talgoSafeY, talgoFlashTime;
        public float catTransformTime, catDivideTime;
        public float swordAttackTime;
        public boolean catFacingRight, catMoving, moving;
        // Temporary effects applied by player attacks.
        public float stunTime, slowTime, poisonTime, poisonDps;
        public float tetherTime, tetherCooldown;
        // EX form, aim and resource state.
        public float exStateTime, exMeter, exAimX, exAimY, exFlashTime, exObjectTimer;
        public int exState, exStoredDust, exConfidence;
        public boolean reflector, warper, artillery, waveCaster;
        public boolean summoner;
        public float summonTimer = 3.8f;
        public boolean anchored;
        public final EnemyBehavior behavior;
        public final DomeBossBehavior.State dome;
        public final int kind; // Negative: monster class; 0..7 Ajarn; 8..10 EX.

        public Enemy(float x, float y, float radius, float hp, float maxSp,
              float speed, float damage, int kind) {
            this(x, y, radius, hp, maxSp, speed, damage, kind, EnemyBehaviors.forKind(kind));
        }

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

        public void updateBehavior(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
            behavior.update(room, this, dt, player, plan);
        }
    }

    /** A pickup or hazard left in the room by an EX encounter. */
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

    /** Runtime projectile data shared by weapons, enemy attacks and the renderer. */
    public static final class Shot {
        public float x, y, vx, vy, radius, damage, life;
        public float animationTime;
        public boolean friendly, powerBall, binding, reflected, wave, giant, satiSpear;
        public boolean lanWave;
        public boolean liquidCatBullet;
        public boolean algoBall;
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
    public final Array<Enemy> spawnQueue = new Array<Enemy>();
    public final Array<Shot> shots = new Array<Shot>();
    public final Array<NotebookImpact> notebookImpacts = new Array<NotebookImpact>();
    public final Array<ExObject> exObjects = new Array<ExObject>();
    public int carriedNotes;
    public final Random random;
    public final EnemyFactory factory;
    public final RoomObstacles obstacles = new RoomObstacles();
    public final IceWall iceWall = new IceWall();
    public GameAudio audio;
    public float blastX, blastY, blastTime, splashX, splashY, splashTime;
    public float beamX, beamY, beamEndX, beamEndY, beamTime;

    private final CombatDamage damageRules = new CombatDamage(this);
    private final ProjectileSystem projectiles = new ProjectileSystem(this, damageRules);

    public CombatRoom(Random random) {
        this.random = random;
        this.factory = new EnemyFactory(random);
    }
    public void setAudio(GameAudio audio) { this.audio = audio; }

    public void begin(RunDirector.Room room, RunPlan plan, int exId) {
        enemies.clear();
        spawnQueue.clear();
        shots.clear();
        notebookImpacts.clear();
        exObjects.clear();
        carriedNotes = 0;
        blastTime = splashTime = beamTime = 0;
        iceWall.clear();
        factory.populate(this, room, plan, exId);
        obstacles.begin(room, random);
        for (Enemy enemy : enemies) obstacles.resolveEnemy(enemy);
    }

    /** Boss shots travel above furniture; ordinary monster shots still hit cover. */
    public void addEnemyShot(Enemy source, Shot shot) {
        shot.ignoresTables = source.kind >= 0;
        shots.add(shot);
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

    // Weapons call these entry points; CombatDamage owns their shared hit rules.
    public void damageNearby(float x, float y, float reach, float amount) {
        damageRules.damageNearby(x, y, reach, amount);
    }

    public void damageArc(float x, float y, float facingX, float facingY,
                          float reach, float halfAngleDegrees, float amount) {
        damageArc(x, y, facingX, facingY, reach, halfAngleDegrees, amount, false, false);
    }

    public void damageArc(float x, float y, float facingX, float facingY,
                          float reach, float halfAngleDegrees, float amount,
                          boolean slow, boolean tether) {
        damageRules.damageArc(x, y, facingX, facingY, reach, halfAngleDegrees, amount, slow, tether);
    }

    public void fireNotebookBeam(float x, float y, float dx, float dy,
                                 float amount, boolean poisonous) {
        damageRules.fireNotebookBeam(x, y, dx, dy, amount, poisonous);
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

    public Shot fireLanWave(float x, float y, float directionX, float directionY,
                            float damage) {
        Shot shot = new Shot(x, y, directionX * 600, directionY * 600,
            15, damage, .72f, true);
        shot.lanWave = true;
        shots.add(shot);
        return shot;
    }

    public void firePowerBall(float x, float y, float vx, float vy, float damage, Enemy target) {
        Shot shot = new Shot(x, y, vx, vy, 40, damage, 2.4f, true);
        shot.powerBall = true;
        shot.target = target;
        shots.add(shot);
    }

    public boolean raiseIceWall(float playerX, float playerY, float faceX, float faceY) {
        if (!iceWall.raise(playerX, playerY, faceX, faceY)) return false;
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.x - iceWall.x, dy = enemy.y - iceWall.y;
            if (dx * dx + dy * dy <= 112 * 112) {
                damageRules.applyToEnemy(enemy, 24, playerX, playerY);
                enemy.stunTime = Math.max(enemy.stunTime, .8f);
            }
            iceWall.resolveEnemy(enemy, obstacles);
        }
        return true;
    }

    /** Returns true once no living enemies remain after this frame. */
    public boolean update(float dt, PlayerCharacter player, RunPlan plan) {
        updateEffects(dt);
        ExEnvironment.updateObjects(this, dt, player, plan);
        updateEnemies(dt, player, plan);
        EnemySpacing.separate(enemies);
        resolveObstacles();
        projectiles.update(dt, player);
        // PowerBall can push an enemy into cover, so resolve again after projectile hits.
        resolveObstacles();
        removeDefeatedEnemies(player);
        return enemies.size == 0;
    }

    private void updateEffects(float dt) {
        blastTime = Math.max(0, blastTime - dt);
        splashTime = Math.max(0, splashTime - dt);
        beamTime = Math.max(0, beamTime - dt);
        iceWall.update(dt);
        projectiles.updateImpacts(dt);
    }

    private void resolveObstacles() {
        for (Enemy enemy : enemies) {
            obstacles.resolveEnemy(enemy);
            iceWall.resolveEnemy(enemy, obstacles);
        }
    }

    private void updateEnemies(float dt, PlayerCharacter player, RunPlan plan) {
        spawnQueue.clear();
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            // Poison resolves before AI; a defeated enemy must not get a final attack.
            float poisonStep = Math.min(dt, enemy.poisonTime);
            enemy.poisonTime -= poisonStep;
            enemy.hp -= enemy.poisonDps * poisonStep;
            if (enemy.hp <= 0) continue;
            updateEnemyTimers(enemy, dt);
            float spRecovery = 44;
            if (enemy.kind == RANGED || enemy.kind == WAVE) spRecovery = 20;
            else if (enemy.kind < 0) spRecovery = 11;
            enemy.sp = Math.min(enemy.maxSp, enemy.sp + spRecovery * plan.spScale() * dt);
            if (enemy.summoner && enemy.stunTime <= 0) factory.queueSummons(this, enemy, dt, plan);
            float oldX = enemy.x, oldY = enemy.y;
            boolean leaping = enemy.kind == LIQUID_CAT && enemy.exMeter > 0;
            enemy.updateBehavior(this, dt, player, plan);
            if (!leaping) iceWall.stopEnemy(enemy, oldX, oldY);
            enemy.moving = Math.abs(enemy.x - oldX) + Math.abs(enemy.y - oldY) > .01f;
        }
        enemies.addAll(spawnQueue);
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
        enemy.talgoFlashTime = Math.max(0, enemy.talgoFlashTime - dt);
        if (enemy.kind == LIQUID_CAT) enemy.catMoving = false;
    }

    private void removeDefeatedEnemies(PlayerCharacter player) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            if (enemies.get(i).hp <= 0) {
                enemies.removeIndex(i);
                player.recoverSp(5);
            }
        }
    }
}
