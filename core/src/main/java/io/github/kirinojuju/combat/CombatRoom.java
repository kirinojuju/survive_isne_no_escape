package io.github.kirinojuju.combat;

import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.EnemyFactory;
import io.github.kirinojuju.enemy.EnemySpacing;
import io.github.kirinojuju.enemy.ex.ExBoss.ExEnvironment;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
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
    private GameAudio audio;
    public float blastX, blastY, blastTime, splashX, splashY, splashTime;
    public float beamX, beamY, beamEndX, beamEndY, beamTime;

    private final CombatDamage damageRules = new CombatDamage(this);
    private final ProjectileSystem projectiles = new ProjectileSystem(this, damageRules);

    public CombatRoom(Random random) {
        this.random = random;
        this.factory = new EnemyFactory(random);
    }
    public void setAudio(GameAudio audio) { this.audio = audio; }
    public GameAudio getAudio() { return audio; }

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
        source.attackAnimationTime = .4f;
        shot.ignoresTables = source.kind >= 0;
        shots.add(shot);
    }

    public Enemy closestEnemy(float x, float y) {
        Enemy result = null;
        float best = Float.MAX_VALUE;
        for (Enemy enemy : enemies) {
            if (enemy.getHp() <= 0) continue;
            float dx = enemy.damageX(x) - x, dy = enemy.damageY(y) - y;
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

    public Shot fireLanWave(float x, float y, float directionX, float directionY, float damage) {
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
            if (enemy.getHp() <= 0) continue;
            float dx = enemy.damageX(iceWall.x) - iceWall.x;
            float dy = enemy.damageY(iceWall.y) - iceWall.y;
            float reach = 112 + enemy.damageRadius();
            if (dx * dx + dy * dy <= reach * reach
                && enemy.damageMultiplier() > 0) {
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
        for (Enemy enemy : enemies) enemy.update(this, dt, player, plan);
        enemies.addAll(spawnQueue);
    }

    private void removeDefeatedEnemies(PlayerCharacter player) {
        // Resolve phase changes and linked copies before removing or rewarding any deaths.
        for (Enemy enemy : enemies)
            if (enemy.getHp() <= 0) enemy.onDefeated(this);
        for (int i = enemies.size - 1; i >= 0; i--) {
            if (enemies.get(i).getHp() <= 0) {
                enemies.removeIndex(i);
                player.recoverSp(5);
            }
        }
    }
}
