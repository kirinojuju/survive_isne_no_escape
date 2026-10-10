package io.github.kirinojuju;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

/** Enemy spawning, AI, projectiles and collision for one room. */
final class CombatRoom {
    static final int BASIC = -1, BINDER = -2, WARPER = -3;
    static final int WAVE = -4, RANGED = -5, SHIELDED = -6;
    static final int LIQUID_CAT = 8, ALLERGIC_VACUUM = 9, INSECURE_SWORD = 10;
    static final int DUST = 0, NOTE = 1, SLIME = 2;

    static final float POWER_BALL_BLAST_RADIUS = 130;

    static final float TALGO_CAST_DURATION = 1.8f;
    static final float TALGO_SAFE_RADIUS = 76f;

    static final float NOTEBOOK_BEAM_RANGE = 760;
    static final float NOTEBOOK_BEAM_HALF_WIDTH = 42;

    static final class ExObject {
        float x, y, vx, vy, radius, life, ejectionTime;
        int kind;

        ExObject(float x, float y, float radius, float life, int kind) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.life = life;
            this.kind = kind;
        }
    }

    static final class Shot {
        float x, y, vx, vy, radius, damage, life;
        boolean friendly, powerBall, binding, reflected, wave, giant, satiSpear;
        boolean liquidCatBullet, algoBall;
        boolean poisonous, splash;
        Enemy target;

        Shot(float x, float y, float vx, float vy, float radius,
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

    final Array<Enemy> enemies = new Array<Enemy>();
    final Array<Shot> shots = new Array<Shot>();
    final Array<ExObject> exObjects = new Array<ExObject>();
    int carriedNotes;
    private final Array<Enemy> spawnQueue = new Array<Enemy>();
    private final Random random;
    private GameAudio audio;
    float blastX, blastY, blastTime, splashX, splashY, splashTime;
    float beamX, beamY, beamEndX, beamEndY, beamTime;

    CombatRoom(Random random) { this.random = random; }
    void setAudio(GameAudio audio) { this.audio = audio; }

    void begin(RunDirector.Room room, RunPlan plan, int exId) {
        enemies.clear();
        shots.clear();
        exObjects.clear();
        carriedNotes = 0;
        spawnQueue.clear();
        blastTime = splashTime = beamTime = 0;
        if (room == RunDirector.Room.MOBS) {
            int count = 6 + plan.stage() + Math.min(3, plan.loop() - 1);
            for (int i = 0; i < count; i++) {
                float angle = MathUtils.PI2 * i / count + random.nextFloat() * .25f;
                float x = GameSpace.WIDTH / 2 + MathUtils.cos(angle) * (330 + random.nextInt(90));
                float y = GameSpace.HEIGHT / 2 + MathUtils.sin(angle) * (95 + random.nextInt(50));
                int kind = mobKind(i, plan);
                enemies.add(createMonster(x, y, kind, plan, i == 0));
            }

        } else if (room == RunDirector.Room.BOSS) {
            int id = 1;
            Enemy boss;

            if (id == 0) {
                boss = new SatiBoss(plan, random);
            } else {
                int family = id % 4;

                if (id == 1) {
                    boss = new KenBoss(plan, random);
                } else if (id == 2) {
                    boss = new MBoss(plan);
                } else {
                    boss = new Enemy(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
                        (190 + plan.stage() * 22 + (id % 3) * 12) * plan.healthScale(),
                        72 * plan.spScale(),
                        (90 + (id % 4) * 9) * plan.speedScale(),
                        (12 + (id % 3) * 2) * plan.damageScale(), id);
                }

                boss.summoner = family == 0 || family == 1;
                boss.reflector = family == 0 || family == 3;
                boss.warper = family == 1;
                boss.artillery = family == 2;
                boss.waveCaster = family == 3;
                boss.summonTimer = plan.loop() == 1 ? 5.2f : 3.8f;
                boss.warpCooldown = plan.loop() == 1 ? 4f : 2.8f;

                if (boss instanceof KenBoss) boss.warper = false;

                if (id == RunPlan.TALGO_ID) boss.talgoCooldown = 3.4f;

                if (plan.loop() >= 3) {
                    int bonus = random.nextInt(3);
                    if (bonus == 0) boss.summoner = true;
                    else if (bonus == 1) boss.reflector = true;
                    else boss.warper = true;
                }

                if (plan.loop() >= 5 && random.nextBoolean()) {
                    boss.waveCaster = true;
                }

                if (boss.reflector) {
                    boss.maxArmor = ((plan.loop() == 1 ? 75 : 110)
                        + plan.stage() * 22) * plan.healthScale();
                    boss.armor = boss.maxArmor;
                }
            }
            enemies.add(boss);

        } else {
            Enemy ex;
            if (exId == 0) {
                ex = new LiquidCatBoss(plan);
            } else {
                ex = new Enemy(GameSpace.RIGHT - 75, GameSpace.WALK_TOP - 75, 34,
                    380 * plan.healthScale(), 88 * plan.spScale(),
                    120 * plan.speedScale(), 14 * plan.damageScale(), 8 + exId);
            }
            ex.skillTimer = 1.8f;

            if (ex.kind == ALLERGIC_VACUUM) {
                ex.exObjectTimer = 1.2f;
                for (int i = 0; i < 6; i++) spawnDust(ex);
            } else {
                ex.maxArmor = 150 * plan.healthScale();
                ex.armor = ex.maxArmor;
                ex.exObjectTimer = 4f;
                for (int i = 0; i < 3; i++) spawnNote();
            }
            enemies.add(ex);
        }
    }

    private int mobKind(int index, RunPlan plan) {
        if (index == 0) return RANGED;
        if (index == 1 && plan.stage() >= 1) return WAVE;
        if (index == 2 && plan.stage() >= 2) return SHIELDED;
        if (index == 3 && plan.stage() >= 2) return BINDER;
        if (index == 4 && plan.stage() >= 3) return WARPER;
        float extraChance = Math.min(.50f, .12f * (plan.loop() - 1));
        if (plan.loop() >= 2 && random.nextFloat() < extraChance) {
            int[] specials = {RANGED, WAVE, SHIELDED, BINDER, WARPER};
            return specials[random.nextInt(specials.length)];
        }
        return BASIC;
    }

    private Enemy createMonster(float x, float y, int kind, RunPlan plan) {
        return createMonster(x, y, kind, plan, false);
    }

    private Enemy createMonster(float x, float y, int kind, RunPlan plan,
                                boolean algoMonster) {
        float hp = kind == BINDER ? 58 : kind == WARPER ? 56 : kind == WAVE ? 48 : kind == RANGED ? 46 : kind == SHIELDED ? 52 : 42 + plan.stage() * 5;
        float sp = kind == BINDER ? 48 : kind == WARPER ? 42 : kind == WAVE ? 43 : kind == RANGED ? 36 : 37;
        float speed = kind == BINDER ? 68 : kind == WARPER ? 92 : kind == WAVE ? 65 : kind == RANGED ? 80 : kind == SHIELDED ? 62 : 80;
        float damage = kind == WARPER ? 10 : kind == WAVE ? 9 : 7;

        float spawnX = MathUtils.clamp(
            x, GameSpace.LEFT + 17, GameSpace.RIGHT - 17);
        float spawnY = MathUtils.clamp(
            y, GameSpace.BOTTOM + 17, GameSpace.WALK_TOP - 17);

        Enemy enemy;
        if (algoMonster) {
            enemy = new AlgoMonster(spawnX, spawnY, 17,
                hp * plan.healthScale(),
                sp * plan.spScale(),
                speed * plan.speedScale(),
                damage * plan.damageScale(), kind);
        } else {
            enemy = new Enemy(spawnX, spawnY, 17,
                hp * plan.healthScale(),
                sp * plan.spScale(),
                speed * plan.speedScale(),
                damage * plan.damageScale(), kind);
        }

        if (kind == SHIELDED) {
            enemy.maxArmor = (36 + plan.stage() * 7) * plan.healthScale();
            enemy.armor = enemy.maxArmor;
        }
        return enemy;
    }

    private void spawnDust(Enemy vacuum) {
        float x = MathUtils.clamp(vacuum.x - 260 + random.nextFloat() * 300,
            GameSpace.LEFT + 12, GameSpace.RIGHT - 12);
        float y = GameSpace.BOTTOM + 25
            + random.nextFloat() * (GameSpace.WALK_TOP - GameSpace.BOTTOM - 50);
        exObjects.add(new ExObject(x, y, 8, 12f, DUST));
    }

    private void spawnNote() {
        float x = MathUtils.clamp(GameSpace.WIDTH / 2 - 215
            + random.nextFloat() * 430, GameSpace.LEFT + 20, GameSpace.RIGHT - 20);
        float y = GameSpace.BOTTOM + 55
            + random.nextFloat() * (GameSpace.WALK_TOP - GameSpace.BOTTOM - 110);
        exObjects.add(new ExObject(x, y, 11, 20f, NOTE));
    }

    Enemy closestEnemy(float x, float y) {
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

    void damageNearby(float x, float y, float reach, float damage) {
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float dx = enemy.x - x, dy = enemy.y - y;
            if (dx * dx + dy * dy <= (enemy.radius + reach) * (enemy.radius + reach))
                damageEnemy(enemy, damage, x, y);
        }
    }

    void damageArc(float x, float y, float facingX, float facingY,
                   float reach, float halfAngleDegrees, float damage) {
        damageArc(x, y, facingX, facingY, reach, halfAngleDegrees,
            damage, false, false);
    }

    void damageArc(float x, float y, float facingX, float facingY,
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

        if (enemy instanceof LiquidCatBoss)
            damage *= ((LiquidCatBoss) enemy).damageMultiplier();

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
                shots.add(counter);
                enemy.reflectTimer = .55f;
            }
            if (enemy.armor < 0) enemy.hp += enemy.armor;
            enemy.armor = Math.max(0, enemy.armor);
        } else {
            enemy.hp -= damage;
        }
        if (audio != null) audio.hit(armored);
    }

    void fireFriendly(float x, float y, float vx, float vy, float damage, Enemy target) {
        fireFriendly(x, y, vx, vy, damage, target, false, false);
    }

    void fireFriendly(float x, float y, float vx, float vy, float damage,
                      Enemy target, boolean poisonous, boolean splash) {
        Shot shot = new Shot(x, y, vx, vy, 9, damage, 1.4f, true);
        shot.target = target;
        shot.poisonous = poisonous;
        shot.splash = splash;
        shots.add(shot);
    }

    void firePowerBall(float x, float y, float vx, float vy, float damage, Enemy target) {
        Shot shot = new Shot(x, y, vx, vy, 40, damage, 2.4f, true);
        shot.powerBall = true;
        shot.target = target;
        shots.add(shot);
    }

    void fireNotebookBeam(float x, float y, float dx, float dy,
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
                enemy.x = MathUtils.clamp(enemy.x + pushX / pushLength * push,
                    GameSpace.LEFT + enemy.radius, GameSpace.RIGHT - enemy.radius);
                enemy.y = MathUtils.clamp(enemy.y + pushY / pushLength * push,
                    GameSpace.BOTTOM + enemy.radius, GameSpace.WALK_TOP - enemy.radius);
            }
            enemy.stunTime = Math.max(enemy.stunTime, enemy.kind >= 0 ? .28f : .70f);
            enemy.slowTime = Math.max(enemy.slowTime, enemy.kind >= 0 ? 1.1f : 2.0f);
        }
    }

    boolean update(float dt, PlayerCharacter player, RunPlan plan) {
        spawnQueue.clear();
        blastTime = Math.max(0, blastTime - dt);
        splashTime = Math.max(0, splashTime - dt);
        beamTime = Math.max(0, beamTime - dt);
        updateExObjects(dt, player, plan);
        for (Enemy enemy : enemies) {
            if (enemy.hp <= 0) continue;
            float poisonStep = Math.min(dt, enemy.poisonTime);
            enemy.poisonTime -= poisonStep;
            enemy.hp -= enemy.poisonDps * poisonStep;
            if (enemy.hp <= 0) continue;
            enemy.animationTime += dt;
            enemy.contactTimer -= dt;
            enemy.skillTimer -= dt;
            enemy.reflectTimer -= dt;
            enemy.stunTime = Math.max(0, enemy.stunTime - dt);
            enemy.slowTime = Math.max(0, enemy.slowTime - dt);
            enemy.tetherTime = Math.max(0, enemy.tetherTime - dt);
            enemy.tetherCooldown = Math.max(0, enemy.tetherCooldown - dt);
            enemy.exFlashTime = Math.max(0, enemy.exFlashTime - dt);

            if (enemy instanceof KenBoss) {
                ((KenBoss) enemy).updateBombSkill(
                    dt, player, plan, enemy.stunTime <= 0);
            }

            enemy.talgoFlashTime = Math.max(0, enemy.talgoFlashTime - dt);
            enemy.sp = Math.min(enemy.maxSp,
                enemy.sp + (enemy.kind == RANGED || enemy.kind == WAVE ? 20
                    : enemy.kind < 0 ? 11 : 44) * plan.spScale() * dt);
            if (enemy.kind >= 0 && enemy.kind < 8 && enemy.warpReturnTime > 0) {
                enemy.warpReturnTime = Math.max(0, enemy.warpReturnTime - dt);
                if (enemy.warpReturnTime == 0) {
                    enemy.x = enemy.homeX;
                    enemy.y = enemy.homeY;
                }
                continue;
            }
            if (enemy.kind == RunPlan.TALGO_ID
                && updateTalgo(enemy, dt, player, plan)) continue;
            if (enemy.stunTime > 0) continue;

            if (enemy instanceof MBoss) {
                ((MBoss) enemy).updateSplitAndAttack(
                    player, plan, shots, spawnQueue);
                continue;
            }

            if (enemy.kind >= LIQUID_CAT) {
                updateExEnemy(enemy, dt, player, plan);
                continue;
            }
            if (enemy.summoner) queueSummons(enemy, dt, plan);
            boolean warping = enemy.kind == WARPER
                ? updateWarper(enemy, dt, player, plan)
                : enemy.kind >= 0 && enemy.kind < 8 && enemy.warper
                  && (!(enemy instanceof SatiBoss)
                      || ((SatiBoss) enemy).satiCharge <= 0)
                    && updateBossWarp(enemy, dt, player, plan);

            boolean satiCharging = enemy instanceof SatiBoss
                && !warping
                && ((SatiBoss) enemy).updateSkill(dt, player, plan, shots, audio);

            float dx = player.x - enemy.x, dy = player.y - enemy.y;
            float length = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
            if (!warping && !satiCharging && (enemy.kind < 0 || enemy.kind >= 8)) {
                float speed = enemy.speed * (enemy.slowTime > 0 ? .48f : 1f);
                if (enemy.kind == RANGED || enemy.artillery) {
                    float desired = enemy.artillery ? 330 : 290;
                    if (length > desired + 35) {
                        enemy.x += dx / length * speed * dt;
                        enemy.y += dy / length * speed * dt;
                    } else if (length < desired - 35) {
                        enemy.x -= dx / length * speed * dt;
                        enemy.y -= dy / length * speed * dt;
                    }
                } else if (length > enemy.radius + 20 + (enemy.kind < 0 ? 0 : 100)) {
                    enemy.x += dx / length * speed * dt;
                    enemy.y += dy / length * speed * dt;
                }
            }
            enemy.x = MathUtils.clamp(enemy.x, GameSpace.LEFT + enemy.radius,
                GameSpace.RIGHT - enemy.radius);
            enemy.y = MathUtils.clamp(enemy.y, GameSpace.BOTTOM + enemy.radius,
                GameSpace.WALK_TOP - enemy.radius);
            if (!warping && length < enemy.radius + 18 && enemy.contactTimer <= 0) {
                player.hurt(enemy.damage);
                enemy.contactTimer = .75f;
            }
            float cost = enemy.kind < 0 ? enemy.maxSp : 28;
            if (!warping && !satiCharging && enemy.kind != WARPER && enemy.sp >= cost
                && enemy.skillTimer <= 0) {
                enemy.sp -= cost;
                enemy.skillTimer = enemy.kind < 0 ? 1.1f
                    : enemy.artillery ? 1.05f : enemy.waveCaster ? .85f : .68f;
                useSkill(enemy, player, plan);
            }
        }
        enemies.addAll(spawnQueue);
        separateEnemies();

        for (int i = shots.size - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            if (shot.friendly && shot.target != null) steerHomingShot(shot);
            shot.x += shot.vx * dt;
            shot.y += shot.vy * dt;
            shot.life -= dt;
            if (shot.wave) shot.radius = Math.min(42, shot.radius + 24 * dt);
            boolean remove = shot.life <= 0 || shot.x < GameSpace.LEFT
                || shot.x > GameSpace.RIGHT || shot.y < GameSpace.BOTTOM
                || shot.y > GameSpace.TOP;
            if (shot.powerBall && remove) {
                explodePowerBall(shot, player.x, player.y);
            } else if (!remove && shot.friendly) {
                Enemy hit = null;
                for (int j = 0; j < enemies.size; j++) {
                    Enemy enemy = enemies.get(j);
                    if (enemy.hp <= 0) continue;
                    float dx = shot.x - enemy.x, dy = shot.y - enemy.y;
                    if (dx * dx + dy * dy < (shot.radius + enemy.radius)
                        * (shot.radius + enemy.radius)) {
                        hit = enemy;
                        break;
                    }
                }
                if (hit != null) {
                    if (shot.powerBall) explodePowerBall(shot, player.x, player.y);
                    else {
                        damageEnemy(hit, shot.damage, player.x, player.y);
                        applyNotebookEffect(hit, shot);
                    }
                    remove = true;
                }
            } else if (!remove) {
                float dx = shot.x - player.x, dy = shot.y - player.y;
                if (dx * dx + dy * dy < (shot.radius + 17) * (shot.radius + 17)) {
                    boolean vulnerable = player.invulnerable <= 0;
                    player.hurt(shot.damage);
                    if (vulnerable && shot.binding) player.bind(3.5f);
                    remove = true;
                }
            }
            if (remove) shots.removeIndex(i);
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            if (enemies.get(i).hp <= 0) {
                enemies.removeIndex(i);
                player.recoverSp(5);
            }
        }
        return enemies.size == 0;
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

    private void updateExEnemy(Enemy ex, float dt, PlayerCharacter player,
                               RunPlan plan) {

        if (ex instanceof LiquidCatBoss)
            ((LiquidCatBoss) ex).updateSkill(this, dt, player, plan);

        else if (ex.kind == ALLERGIC_VACUUM) updateAllergicVacuum(ex, dt, player, plan);
        else updateInsecureSword(ex, dt, player, plan);
        ex.x = MathUtils.clamp(ex.x, GameSpace.LEFT + ex.radius,
            GameSpace.RIGHT - ex.radius);
        ex.y = MathUtils.clamp(ex.y, GameSpace.BOTTOM + ex.radius,
            GameSpace.WALK_TOP - ex.radius);
    }

    private void updateAllergicVacuum(Enemy vacuum, float dt,
                                      PlayerCharacter player, RunPlan plan) {
        if (vacuum.exState == 1 || vacuum.exState == 2) {
            vacuum.exStateTime -= dt;
            if (vacuum.exStateTime <= 0 && vacuum.exState == 1) {
                int count = vacuum.exStoredDust;
                for (int i = 0; i < count; i++) {
                    float angle = MathUtils.PI2 * i / Math.max(1, count)
                        + random.nextFloat() * .28f;
                    ExObject dust = new ExObject(vacuum.x, vacuum.y, 10, 6f, DUST);
                    float speed = 225 + random.nextFloat() * 65;
                    dust.vx = MathUtils.cos(angle) * speed;
                    dust.vy = MathUtils.sin(angle) * speed;
                    dust.ejectionTime = 1.1f;
                    exObjects.add(dust);
                }
                vacuum.exStoredDust = 0;
                vacuum.exMeter = 0;
                vacuum.exState = 2;
                vacuum.exStateTime = 1f;
                vacuum.exFlashTime = .38f;
                vacuum.x = Math.min(GameSpace.RIGHT - vacuum.radius, vacuum.x + 40);
            } else if (vacuum.exStateTime <= 0) {
                vacuum.exState = 0;
            }
            return; // No suction while sneezing or recovering.
        }

        vacuum.exObjectTimer -= dt;
        if (vacuum.exObjectTimer <= 0 && countExObjects(DUST) < 8) {
            spawnDust(vacuum);
            vacuum.exObjectTimer = 1.25f;
        }
        moveExToward(vacuum, player.x, player.y,
            vacuum.speed * .43f * dt, 205);
        for (int i = exObjects.size - 1; i >= 0; i--) {
            ExObject dust = exObjects.get(i);
            if (dust.kind != DUST || dust.ejectionTime > 0) continue;
            float dx = vacuum.x - dust.x, dy = vacuum.y - dust.y;
            float distance = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
            float step = Math.min(distance, 175 * dt);
            dust.x += dx / distance * step;
            dust.y += dy / distance * step;
            if (distance <= vacuum.radius + dust.radius + step) {
                exObjects.removeIndex(i);
                vacuum.exStoredDust++;
                vacuum.exMeter = Math.min(100, vacuum.exMeter + 25);
            }
        }
        float dx = vacuum.x - player.x, dy = vacuum.y - player.y;
        float distance = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        if (distance < 285 && !player.skillPlaying)
            player.moveBy(dx / distance * 82 * dt, dy / distance * 82 * dt);
        if (distance < vacuum.radius + 22 && vacuum.contactTimer <= 0) {
            player.hurt(vacuum.damage);
            vacuum.contactTimer = .8f;
        }
        if (vacuum.exMeter >= 100) {
            vacuum.exState = 1;
            vacuum.exStateTime = .85f;
        }
    }

    private void updateInsecureSword(Enemy sword, float dt,
                                     PlayerCharacter player, RunPlan plan) {
        sword.exObjectTimer -= dt;
        if (sword.exObjectTimer <= 0 && countExObjects(NOTE) < 3) {
            spawnNote();
            sword.exObjectTimer = 4f;
        }
        float dx = player.x - sword.x, dy = player.y - sword.y;
        if (carriedNotes > 0
            && dx * dx + dy * dy <= (sword.radius + 78) * (sword.radius + 78)) {
            carriedNotes--;
            sword.exConfidence = Math.min(100, sword.exConfidence + 25);
            sword.armor = Math.max(0, sword.armor - 55 * plan.healthScale());
            sword.exState = 2;
            sword.exStateTime = .5f;
            sword.exFlashTime = .35f;
            sword.exAimX = sword.x;
            sword.exAimY = sword.y;
            sword.skillTimer = Math.max(sword.skillTimer, 1.3f);
            return;
        }
        if (sword.exState == 1 || sword.exState == 2 || sword.exState == 3) {
            sword.exStateTime -= dt;
            if (sword.exStateTime <= 0 && sword.exState == 1) {
                sword.exState = 2;
                sword.exStateTime = 1.05f;
                sword.exFlashTime = .3f;
                sword.exConfidence = Math.max(0, sword.exConfidence - 15);
                float aimDx = player.x - sword.exAimX;
                float aimDy = player.y - sword.exAimY;
                if (aimDx * aimDx + aimDy * aimDy <= 100 * 100)
                    player.hurt(29 * plan.damageScale());
            } else if (sword.exStateTime <= 0) {
                sword.exState = 0;
            }
            return;
        }
        if (sword.skillTimer <= 0) {
            sword.skillTimer = 2.2f;
            boolean confident = sword.exConfidence >= 100
                || random.nextFloat() < .15f + sword.exConfidence * .006f;
            if (confident) {
                sword.exState = 1;
                sword.exStateTime = .65f;
                sword.exAimX = player.x;
                sword.exAimY = player.y;
            } else {
                sword.exState = 3;
                sword.exStateTime = .6f; // The insecure attack visibly whiffs for 0 damage.
                sword.exFlashTime = .25f;
            }
        }
    }

    int countExObjects(int kind) {
        int count = 0;
        for (ExObject object : exObjects) if (object.kind == kind) count++;
        return count;
    }

    void moveExToward(Enemy enemy, float targetX, float targetY,
                              float step, float stopDistance) {
        float dx = targetX - enemy.x, dy = targetY - enemy.y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        if (distance <= stopDistance || distance < .001f) return;
        float move = Math.min(step, distance - stopDistance);
        enemy.x += dx / distance * move;
        enemy.y += dy / distance * move;
    }

    private boolean updateTalgo(Enemy boss, float dt, PlayerCharacter player,
                                RunPlan plan) {
        if (boss.talgoCharge > 0) {
            boss.talgoCharge = Math.max(0, boss.talgoCharge - dt);
            if (boss.talgoCharge == 0) {
                float dx = player.x - boss.talgoSafeX;
                float dy = player.y - boss.talgoSafeY;
                boolean inSafeCircle = dx * dx + dy * dy
                    <= TALGO_SAFE_RADIUS * TALGO_SAFE_RADIUS;
                if (!inSafeCircle) player.hurt(27 * plan.damageScale());
                boss.talgoFlashTime = .3f;
            }
            return true;
        }
        boss.talgoCooldown -= dt;
        if (boss.talgoCooldown > 0 || boss.stunTime > 0) return false;
        boss.talgoCooldown = Math.max(5.2f, 7.2f / plan.speedScale());
        boss.talgoCharge = TALGO_CAST_DURATION;
        float angle = random.nextFloat() * MathUtils.PI2;
        float distance = 125 + random.nextFloat() * 55;
        boss.talgoSafeX = MathUtils.clamp(player.x + MathUtils.cos(angle) * distance,
            GameSpace.LEFT + TALGO_SAFE_RADIUS,
            GameSpace.RIGHT - TALGO_SAFE_RADIUS);
        boss.talgoSafeY = MathUtils.clamp(player.y + MathUtils.sin(angle) * distance,
            GameSpace.BOTTOM + TALGO_SAFE_RADIUS,
            GameSpace.WALK_TOP - TALGO_SAFE_RADIUS);
        return true;
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

    private boolean updateWarper(Enemy enemy, float dt, PlayerCharacter player, RunPlan plan) {
        enemy.warpCooldown = Math.max(0, enemy.warpCooldown - dt);
        if (enemy.warpWindup > 0) {
            enemy.warpWindup = Math.max(0, enemy.warpWindup - dt);
            if (enemy.warpWindup == 0) {
                enemy.x = enemy.warpX;
                enemy.y = enemy.warpY;
                float dx = player.x - enemy.x, dy = player.y - enemy.y;
                float reach = 75;
                if (dx * dx + dy * dy <= reach * reach)
                    player.hurt(25 * plan.damageScale());
            }
            return true;
        }
        if (enemy.warpCooldown <= 0 && enemy.sp >= enemy.maxSp) {
            enemy.sp = 0;
            enemy.warpWindup = .58f;
            enemy.warpCooldown = 4f;
            enemy.warpX = MathUtils.clamp(player.x, GameSpace.LEFT + enemy.radius,
                GameSpace.RIGHT - enemy.radius);
            enemy.warpY = MathUtils.clamp(player.y, GameSpace.BOTTOM + enemy.radius,
                GameSpace.WALK_TOP - enemy.radius);
            return true;
        }
        return false;
    }

    private boolean updateBossWarp(Enemy boss, float dt, PlayerCharacter player,
                                   RunPlan plan) {
        boss.warpCooldown = Math.max(0, boss.warpCooldown - dt);
        if (boss.warpCooldown > 0) return false;
        boss.warpCooldown = Math.max(2.6f, 4.5f / plan.speedScale());
        boss.x = MathUtils.clamp(player.x + (boss.x > player.x ? 48 : -48),
            GameSpace.LEFT + boss.radius, GameSpace.RIGHT - boss.radius);
        boss.y = MathUtils.clamp(player.y, GameSpace.BOTTOM + boss.radius,
            GameSpace.WALK_TOP - boss.radius);
        boss.warpReturnTime = .38f;
        if (boss.warpStrikes++ % 2 == 0) player.bind(2.8f);
        else player.hurt(26 * plan.damageScale());
        return true;
    }

    private void queueSummons(Enemy boss, float dt, RunPlan plan) {
        boss.summonTimer -= dt;
        if (boss.summonTimer > 0) return;
        boss.summonTimer = Math.max(3f, 5.2f / plan.speedScale());
        int available = 4 - livingMinions() - spawnQueue.size;
        int count = Math.min(plan.loop() == 1 ? 1 : 2, available);
        for (int i = 0; i < count; i++) {
            int[] specials = {BINDER, WAVE, RANGED, WARPER};
            int kind = specials[random.nextInt(plan.loop() == 1 ? 3 : specials.length)];
            float x = MathUtils.clamp(boss.homeX - 85 - i * 65,
                GameSpace.LEFT + 20, GameSpace.RIGHT - 20);
            float y = MathUtils.clamp(boss.homeY + (i == 0 ? -55 : 55),
                GameSpace.BOTTOM + 20, GameSpace.WALK_TOP - 20);
            Enemy minion = createMonster(x, y, kind, plan);
            minion.maxArmor = (28 + plan.stage() * 5) * plan.healthScale();
            minion.armor = minion.maxArmor;
            spawnQueue.add(minion);
        }
    }

    private int livingMinions() {
        int count = 0;
        for (int i = 0; i < enemies.size; i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.kind < 0 && enemy.hp > 0) count++;
        }
        return count;
    }

    /** Prevent enemies from stacking while they chase the same player position. */
    private void separateEnemies() {
        for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i < enemies.size; i++) {
                Enemy a = enemies.get(i);
                if (a.hp <= 0) continue;
                for (int j = i + 1; j < enemies.size; j++) {
                    Enemy b = enemies.get(j);
                    if (b.hp <= 0) continue;
                    float dx = a.x - b.x, dy = a.y - b.y;
                    float minimum = a.radius + b.radius + 12;
                    float distanceSquared = dx * dx + dy * dy;
                    if (distanceSquared >= minimum * minimum) continue;
                    boolean sameSpot = distanceSquared < .001f;
                    if (sameSpot) {
                        dx = i % 2 == 0 ? 1 : -1;
                        dy = j % 2 == 0 ? 1 : -1;
                    }
                    float distance = (float)Math.sqrt(dx * dx + dy * dy);
                    float overlap = sameSpot ? minimum : minimum - distance;
                    float aShare = a.kind >= 0 && b.kind < 0 ? 0f
                        : a.kind < 0 && b.kind >= 0 ? 1f : .5f;
                    a.x += dx / distance * overlap * aShare;
                    a.y += dy / distance * overlap * aShare;
                    b.x -= dx / distance * overlap * (1 - aShare);
                    b.y -= dy / distance * overlap * (1 - aShare);
                }
            }
            for (Enemy enemy : enemies) {
                enemy.x = MathUtils.clamp(enemy.x, GameSpace.LEFT + enemy.radius,
                    GameSpace.RIGHT - enemy.radius);
                enemy.y = MathUtils.clamp(enemy.y, GameSpace.BOTTOM + enemy.radius,
                    GameSpace.WALK_TOP - enemy.radius);
            }
        }
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

    private void useSkill(Enemy enemy, PlayerCharacter player, RunPlan plan) {
        float angle = MathUtils.atan2(player.y - enemy.y, player.x - enemy.x);
        float speed = (enemy.kind < 0 ? 225 : 275) * plan.speedScale();
        float damage = (enemy.kind < 0 ? 5 : 12) * plan.damageScale();
        if (enemy.kind == BINDER) {
            Shot bindShot = new Shot(enemy.x, enemy.y,
                MathUtils.cos(angle) * speed * .85f,
                MathUtils.sin(angle) * speed * .85f,
                9, 3 * plan.damageScale(), 3f, false);
            bindShot.binding = true;
            shots.add(bindShot);
            return;
        }
        if (enemy.kind == WAVE || enemy.waveCaster) {
            int count = enemy.waveCaster ? 3 : 1;
            for (int i = 0; i < count; i++) {
                float direction = angle + (i - (count - 1) / 2f) * .30f;
                Shot waveShot = new Shot(enemy.x, enemy.y,
                    MathUtils.cos(direction) * speed,
                    MathUtils.sin(direction) * speed,
                    enemy.waveCaster ? 25 : 20,
                    (enemy.waveCaster ? 15 : 9) * plan.damageScale(),
                    3f, false);
                waveShot.wave = true;
                shots.add(waveShot);
            }
            return;
        }
        if (enemy.artillery) {
            Shot giantShot = new Shot(enemy.x, enemy.y,
                MathUtils.cos(angle) * 195 * plan.speedScale(),
                MathUtils.sin(angle) * 195 * plan.speedScale(),
                38, 24 * plan.damageScale(), 3.5f, false);
            giantShot.giant = true;
            shots.add(giantShot);
            return;
        }
        if (enemy.kind == RANGED) {
            fireEnemy(enemy, angle, 325 * plan.speedScale(), 9 * plan.damageScale());
            return;
        }
        int pattern = enemy.kind < 0 ? 0 : enemy.kind % 4;
        if (pattern == 2) {
            for (int i = 0; i < 8; i++) fireEnemy(enemy, i * MathUtils.PI2 / 8, speed * .8f, damage);
        } else if (pattern == 1 || pattern == 3) {
            for (int i = -1; i <= 1; i++)
                fireEnemy(enemy, angle + i * (pattern == 1 ? .22f : .4f), speed, damage);
        } else {
            fireEnemy(enemy, angle, speed, damage);
        }
    }

    private void fireEnemy(Enemy enemy, float angle, float speed, float damage) {
        Shot shot = new Shot(enemy.x, enemy.y, MathUtils.cos(angle) * speed,
            MathUtils.sin(angle) * speed, 7, damage, 3, false);
        shot.algoBall = enemy instanceof AlgoMonster;
        shots.add(shot);
    }
}
