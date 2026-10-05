package io.github.kirinojuju;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Arena, HUD and temporary enemy art; the player uses exported sprite frames. */
final class GameRenderer {
    private static final float SKILL_X = 1138, SKILL_Y = 155, SKILL_RADIUS = 45;
    private static final Color INK = new Color(.09f, .12f, .18f, 1);
    private static final Color PAPER = new Color(.91f, .94f, .91f, 1);
    private static final Color CYAN = new Color(.36f, .85f, .86f, 1);
    private static final Color GOLD = new Color(1, .78f, .35f, 1);
    private static final Color CORAL = new Color(.96f, .39f, .39f, 1);
    private static final Color PURPLE = new Color(.72f, .48f, .94f, 1);
    private static final Color GREEN = new Color(.43f, .87f, .57f, 1);
    private static final Color SAFE_FILL = new Color(.11f, .31f, .20f, 1);
    private static final Color BLUE = new Color(.30f, .60f, 1f, 1);
    private static final Color DISABLED = new Color(.31f, .31f, .39f, 1);

    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(GameSpace.WIDTH, GameSpace.HEIGHT, camera);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final BitmapFont bigFont = new BitmapFont();
    private final StudentSprite student = new StudentSprite();
    private final SatiSprite sati = new SatiSprite();
    private final LiquidCatSprite liquidCat = new LiquidCatSprite();
    private final Texture normalRoom = loadRoom("scene/sceneroom.png");
    private final Texture bossRoom = loadRoom("scene/sceneroom_boss.png");
    private final Texture building30 = loadRoom("scene/scene30building_entrance_withoutcar.png");
    private final Texture carObject = loadRoom("scene/scene30bCar_object.png");
    private final Vector2 touchPosition = new Vector2();
    private boolean skillPressCaptured;

    GameRenderer() {
        viewport.apply();
        camera.position.set(GameSpace.WIDTH / 2, GameSpace.HEIGHT / 2, 0);
        camera.update();
        font.getData().setScale(1.3f);
        bigFont.getData().setScale(2.4f);
        font.setColor(PAPER);
        bigFont.setColor(PAPER);
    }

    private static Texture loadRoom(String path) {
        return OptionalAssetTexture.load(path);
    }

    void draw(RunDirector run, PlayerCharacter player, CombatRoom combat) {
        ScreenUtils.clear(INK);
        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        if (isEntrancePhase(run.phase)) {
            drawEntrance(run, player);
            drawSceneFade(run.sceneFade);
            return;
        }
        batch.begin();
        drawScene(run);
        batch.end();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawArena();
        if (run.phase != RunDirector.Phase.START_SELECT) {
            for (CombatRoom.Enemy enemy : combat.enemies)
                if (enemy.kind == RunPlan.TALGO_ID && enemy.hp > 0
                    && (enemy.talgoCharge > 0 || enemy.talgoFlashTime > 0))
                    drawTalgoWarning(enemy);
            for (CombatRoom.Shot shot : combat.shots) {
                if (shot.powerBall || shot.satiSpear || shot.liquidCatBullet) continue;
                if (shot.wave) {
                    shapes.setColor(BLUE);
                    shapes.circle(shot.x, shot.y, shot.radius, 28);
                    shapes.setColor(INK);
                    shapes.circle(shot.x, shot.y, Math.max(0, shot.radius - 7), 28);
                } else if (shot.giant) {
                    shapes.setColor(CORAL);
                    shapes.circle(shot.x, shot.y, shot.radius, 36);
                    shapes.setColor(GOLD);
                    shapes.circle(shot.x, shot.y, shot.radius * .58f, 30);
                } else {
                    shapes.setColor(shot.binding ? CYAN : shot.reflected ? PURPLE
                        : shot.friendly ? CYAN : GOLD);
                    shapes.rect(shot.x - shot.radius, shot.y - shot.radius,
                        shot.radius * 2, shot.radius * 2);
                }
            }
            if (combat.blastTime > 0) drawPowerBlast(combat);
            if (combat.splashTime > 0) drawNotebookSplash(combat);
            if (combat.beamTime > 0) drawNotebookBeam(combat);
            drawExObjects(combat);
            for (CombatRoom.Enemy enemy : combat.enemies)
                if (enemy.kind >= CombatRoom.LIQUID_CAT) drawExTelegraph(enemy);
            for (CombatRoom.Enemy enemy : combat.enemies)
                if (enemy.warpWindup > 0) drawWarpWarning(enemy);
            for (CombatRoom.Enemy enemy : combat.enemies)
                if (enemy.warpReturnTime > 0) drawBossWarpFlash(enemy);
            for (CombatRoom.Enemy enemy : combat.enemies)
                if (enemy.kind == 0 && enemy.satiCharge > 0) drawSatiWarning(enemy);
            if (run.phase == RunDirector.Phase.FIGHT) drawAttackLock(player);
            for (CombatRoom.Enemy enemy : combat.enemies) {
                if (enemy.tetherTime > 0 && enemy.hp > 0) {
                    shapes.setColor(CYAN);
                    shapes.rectLine(player.x, player.y, enemy.x, enemy.y, 3);
                }
            }
            for (CombatRoom.Enemy enemy : combat.enemies) drawEnemy(enemy);
            drawAttackEffect(player);
            drawBars(player);
            if (run.phase == RunDirector.Phase.FIGHT) drawSkillButton(player);
        }
        if (run.phase != RunDirector.Phase.FIGHT) drawPanel();
        if (run.phase == RunDirector.Phase.UPGRADE) drawUpgradeChoices();
        shapes.end();

        batch.begin();
        if (run.phase == RunDirector.Phase.FIGHT) {
            for (CombatRoom.Shot shot : combat.shots)
                if (shot.powerBall) student.drawPowerBall(batch, shot);
                else if (shot.satiSpear) sati.drawSpear(batch, shot);
                else if (shot.liquidCatBullet) liquidCat.drawBullet(batch, shot);
            for (CombatRoom.Enemy enemy : combat.enemies)
                if (enemy.kind == 0) sati.drawElement(batch, enemy);
                else if (enemy.kind == CombatRoom.LIQUID_CAT)
                    liquidCat.draw(batch, enemy);
            for (CombatRoom.Enemy enemy : combat.enemies) {
                if (enemy.kind == CombatRoom.INSECURE_SWORD && enemy.exState == 3)
                    font.draw(batch, "0!", enemy.x - 12, enemy.y + 91);
                else if (enemy.kind == CombatRoom.ALLERGIC_VACUUM
                    && enemy.exState == 2 && enemy.exFlashTime > 0)
                    font.draw(batch, "ACHOO!", enemy.x - 33, enemy.y + 91);
            }
            student.draw(batch, player);
        }
        if (run.phase == RunDirector.Phase.START_SELECT) {
            title("SCHOOL LIFE: SURVIVE YEAR FOUR", 415);
            line("Four stages, four Ajarns, and an endless school-life loop.", 330);
            line("Start with BOTH weapons: LAN cable and notebook.", 285);
            line("Switch during combat with Q, 1, or 2.", 250);
            line("Press ENTER to begin.", 195);
        } else {
            drawHud(run, player, combat);
            if (run.phase == RunDirector.Phase.FIGHT) {
                font.draw(batch, "SKILL", SKILL_X - 29, SKILL_Y + 6);
                font.draw(batch, "E", SKILL_X - 6, SKILL_Y - 16);
            }
            if (run.phase == RunDirector.Phase.TRANSITION) {
                title(run.transitionTitle, 415);
                line(run.transitionDetail, 340);
                line("Press ENTER to continue", 260);
            } else if (run.phase == RunDirector.Phase.UPGRADE) {
                title("CHOOSE ONE UPGRADE", 435);
                line(run.transitionTitle + "  |  +24 HP", 390);
                for (int i = 0; i < run.offeredUpgrades.length; i++) {
                    Upgrade upgrade = run.offeredUpgrades[i];
                    float y = 345 - i * 65;
                    line("[" + (i + 1) + "] " + upgrade.title, y);
                    line(upgrade.detail, y - 25);
                }
            } else if (run.phase == RunDirector.Phase.GAME_OVER) {
                title("SCHOOL LIFE OVER", 415);
                line("You reached loop " + run.plan.loop() + ", stage "
                    + (run.plan.stage() + 1) + " and cleared " + run.clearedRooms + " rooms.", 330);
                line("Press R to start a new run", 260);
            }
        }
        batch.end();
        drawSceneFade(run.sceneFade);
    }

    private static boolean isEntrancePhase(RunDirector.Phase phase) {
        return phase == RunDirector.Phase.START_SELECT
            || phase == RunDirector.Phase.ENTRANCE
            || phase == RunDirector.Phase.CAR_PROMPT
            || phase == RunDirector.Phase.CAR_ENDING;
    }

    private void drawEntrance(RunDirector run, PlayerCharacter player) {
        batch.begin();
        batch.draw(building30, EntranceScene.DRAW_X, EntranceScene.DRAW_Y,
            EntranceScene.DRAW_SIZE, EntranceScene.DRAW_SIZE);
        if (run.phase != RunDirector.Phase.CAR_ENDING)
            batch.draw(carObject, EntranceScene.DRAW_X, EntranceScene.DRAW_Y,
                EntranceScene.DRAW_SIZE, EntranceScene.DRAW_SIZE);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (run.phase == RunDirector.Phase.ENTRANCE) {
            shapes.setColor(INK);
            shapes.rect(EntranceScene.DRAW_X, 18,
                EntranceScene.DRAW_SIZE, 51);
            shapes.setColor(CYAN);
            shapes.rect(349, 321, 82, 4);
            shapes.triangle(343, 323, 358, 334, 358, 312);
            shapes.setColor(GOLD);
            shapes.rect(EntranceScene.CAR_X - 2, 459, 4, 25);
            shapes.circle(EntranceScene.CAR_X, 487, 6, 16);
        } else if (run.phase == RunDirector.Phase.CAR_PROMPT) {
            shapes.setColor(INK);
            shapes.rect(265, 207, 750, 285);
            shapes.setColor(CYAN);
            shapes.rect(265, 487, 750, 5);
            shapes.setColor(GREEN);
            shapes.rect(300, 239, 315, 64);
            shapes.setColor(CORAL);
            shapes.rect(665, 239, 315, 64);
        } else if (run.phase == RunDirector.Phase.CAR_ENDING) {
            drawPanel();
        } else {
            drawPanel();
        }
        shapes.end();

        batch.begin();
        if (run.phase == RunDirector.Phase.ENTRANCE
            || run.phase == RunDirector.Phase.CAR_PROMPT)
            student.draw(batch, player);
        if (run.phase == RunDirector.Phase.START_SELECT) {
            title("SCHOOL LIFE: SURVIVE YEAR FOUR", 415);
            line("Four stages, four Ajarns, and an endless school-life loop.", 330);
            line("First stop: Building 30. One car. One suspiciously easy way out.", 285);
            line("Press ENTER to begin.", 210);
        } else if (run.phase == RunDirector.Phase.ENTRANCE) {
            bigFont.draw(batch, "BUILDING 30", 30, 650);
            font.draw(batch, "LEFT DOOR", 34, 555);
            font.draw(batch, "CORE GAME", 34, 526);
            font.draw(batch, "CAR", 1030, 555);
            font.draw(batch, EntranceScene.atCar(player)
                ? "E / CLICK TO INTERACT" : "WALK CLOSER", 1005, 526);
            font.draw(batch, "WASD / ARROWS: walk    LEFT DOOR: enter school"
                + "    E near car: go home?", 289, 50);
        } else if (run.phase == RunDirector.Phase.CAR_PROMPT) {
            title("GO HOME?", 407);
            line("You can skip four years of boss fights...", 345);
            font.draw(batch, "[1] YES, GO HOME", 355, 278);
            font.draw(batch, "[2] NO, FACE SCHOOL", 707, 278);
        } else {
            title("YOU WENT HOME", 415);
            line("Quest complete: avoided every Ajarn.", 340);
            line("Achievement unlocked: Professional Procrastinator.", 295);
            line("Press ENTER to try school life again.", 230);
        }
        batch.end();
    }

    private void drawSceneFade(float alpha) {
        if (alpha <= 0) return;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, alpha);
        shapes.rect(0, 0, GameSpace.WIDTH, GameSpace.HEIGHT);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawArena() {
        float left = GameSpace.LEFT, right = GameSpace.RIGHT;
        float bottom = GameSpace.BOTTOM, top = GameSpace.TOP;
        shapes.setColor(.12f, .18f, .25f, 1);
        shapes.rect(left - 10, bottom - 10, right - left + 20, 10);
        shapes.rect(left - 10, top, right - left + 20, 10);
        shapes.rect(left - 10, bottom, 10, top - bottom);
        shapes.rect(right, bottom, 10, top - bottom);
        shapes.setColor(CYAN);
        shapes.rect(left - 10, top + 6, right - left + 20, 4);
        shapes.setColor(.12f, .28f, .32f, 1);
        shapes.rect(left, GameSpace.WALK_TOP, right - left, 3);
    }

    private void drawScene(RunDirector run) {
        Texture scene = run.room == RunDirector.Room.BOSS ? bossRoom : normalRoom;
        float tileWidth = (GameSpace.RIGHT - GameSpace.LEFT) / 2;
        float height = GameSpace.TOP - GameSpace.BOTTOM;
        batch.draw(scene, GameSpace.LEFT, GameSpace.BOTTOM, tileWidth, height);
        batch.draw(scene, GameSpace.LEFT + tileWidth, GameSpace.BOTTOM,
            tileWidth, height);
    }

    private void drawSatiWarning(CombatRoom.Enemy boss) {
        float x = CombatRoom.satiElementX(boss);
        float y = CombatRoom.satiElementY(boss);
        shapes.setColor(CORAL);
        shapes.rectLine(x, y, boss.satiAimX, boss.satiAimY, 4);
        shapes.circle(boss.satiAimX, boss.satiAimY, 13, 20);
        shapes.setColor(INK);
        shapes.circle(boss.satiAimX, boss.satiAimY, 8, 20);
    }

    private void drawTalgoWarning(CombatRoom.Enemy boss) {
        shapes.setColor(CORAL);
        for (float x = GameSpace.LEFT + 18; x < GameSpace.RIGHT; x += 72)
            shapes.rect(x, GameSpace.BOTTOM, 3, GameSpace.WALK_TOP - GameSpace.BOTTOM);
        for (float y = GameSpace.BOTTOM + 18; y < GameSpace.WALK_TOP; y += 72)
            shapes.rect(GameSpace.LEFT, y, GameSpace.RIGHT - GameSpace.LEFT, 3);
        if (boss.talgoCharge <= 0) return;
        shapes.setColor(GREEN);
        shapes.circle(boss.talgoSafeX, boss.talgoSafeY,
            CombatRoom.TALGO_SAFE_RADIUS + 6, 48);
        shapes.setColor(SAFE_FILL);
        shapes.circle(boss.talgoSafeX, boss.talgoSafeY,
            CombatRoom.TALGO_SAFE_RADIUS, 48);
        shapes.setColor(GREEN);
        shapes.circle(boss.talgoSafeX, boss.talgoSafeY, 10, 20);
    }

    private void drawEnemy(CombatRoom.Enemy enemy) {
        if (enemy.kind >= CombatRoom.LIQUID_CAT) {
            drawExEnemy(enemy);
            return;
        }
        if (enemy.tetherTime > 0) {
            shapes.setColor(CYAN);
            shapes.circle(enemy.x, enemy.y, enemy.radius + 10, 32);
            shapes.setColor(INK);
            shapes.circle(enemy.x, enemy.y, enemy.radius + 5, 32);
        }
        shapes.setColor(enemy.kind == CombatRoom.BINDER ? CYAN
            : enemy.kind == CombatRoom.WARPER ? PURPLE
            : enemy.kind == CombatRoom.WAVE ? BLUE
            : enemy.kind == CombatRoom.RANGED ? GREEN
            : enemy.kind == CombatRoom.SHIELDED ? PAPER
            : enemy.kind == CombatRoom.BASIC ? GOLD
            : enemy.kind >= 8 ? PURPLE : CORAL);
        shapes.rect(enemy.x - enemy.radius, enemy.y - enemy.radius,
            enemy.radius * 2, enemy.radius * 2);
        shapes.setColor(INK);
        shapes.rect(enemy.x - 9, enemy.y + 2, 6, 6);
        shapes.rect(enemy.x + 4, enemy.y + 2, 6, 6);
        if (enemy.stunTime > 0 || enemy.slowTime > 0) {
            shapes.setColor(CYAN);
            shapes.rect(enemy.x - 3, enemy.y + enemy.radius - 3, 6, 8);
        }
        if (enemy.poisonTime > 0) {
            shapes.setColor(GREEN);
            shapes.circle(enemy.x, enemy.y + enemy.radius + 5, 5, 12);
        }
        if (enemy.kind >= 0 || enemy.maxArmor > 0) {
            shapes.setColor(INK);
            shapes.rect(enemy.x - enemy.radius, enemy.y + enemy.radius + 8,
                enemy.radius * 2, 7);
            shapes.setColor(CORAL);
            shapes.rect(enemy.x - enemy.radius, enemy.y + enemy.radius + 8,
                enemy.radius * 2 * Math.max(0, enemy.hp / enemy.maxHp), 7);
            if (enemy.maxArmor > 0 && enemy.armor > 0) {
                shapes.setColor(INK);
                shapes.rect(enemy.x - enemy.radius, enemy.y + enemy.radius + 18,
                    enemy.radius * 2, 6);
                shapes.setColor(CYAN);
                shapes.rect(enemy.x - enemy.radius, enemy.y + enemy.radius + 18,
                    enemy.radius * 2 * enemy.armor / enemy.maxArmor, 6);
            }
        }
    }

    private void drawExObjects(CombatRoom combat) {
        for (CombatRoom.ExObject object : combat.exObjects) {
            if (object.kind == CombatRoom.DUST) {
                shapes.setColor(object.ejectionTime > 0 ? CORAL : PAPER);
                shapes.rect(object.x - 6, object.y - 6, 12, 12);
                shapes.setColor(INK);
                shapes.rect(object.x - 2, object.y - 2, 4, 4);
            } else if (object.kind == CombatRoom.NOTE) {
                shapes.setColor(GOLD);
                shapes.rect(object.x - 11, object.y - 10, 22, 20);
                shapes.setColor(INK);
                shapes.rect(object.x - 7, object.y + 2, 12, 2);
                shapes.rect(object.x - 7, object.y - 3, 9, 2);
            } else {
                shapes.setColor(GREEN);
                shapes.ellipse(object.x - 25, object.y - 10, 50, 20, 20);
            }
        }
    }

    private void drawExTelegraph(CombatRoom.Enemy ex) {
        if (ex.kind == CombatRoom.LIQUID_CAT && ex.exFlashTime > 0
            && ex.exState == 0 && ex.exMeter == 0) {
            shapes.setColor(GREEN);
            shapes.circle(ex.x, ex.y, 75, 40);
            shapes.setColor(INK);
            shapes.circle(ex.x, ex.y, 67, 40);
        }
        if (ex.kind == CombatRoom.INSECURE_SWORD && ex.exFlashTime > 0
            && ex.exState == 2) {
            shapes.setColor(GOLD);
            shapes.circle(ex.exAimX, ex.exAimY, 100, 44);
            shapes.setColor(INK);
            shapes.circle(ex.exAimX, ex.exAimY, 90, 44);
        }
        float warningX, warningY, radius;
        if (ex.kind == CombatRoom.LIQUID_CAT && ex.exMeter > 0) {
            warningX = ex.exAimX;
            warningY = ex.exAimY;
            radius = 80;
        } else if (ex.kind == CombatRoom.INSECURE_SWORD && ex.exState == 1) {
            warningX = ex.exAimX;
            warningY = ex.exAimY;
            radius = 100;
        } else if (ex.kind == CombatRoom.ALLERGIC_VACUUM && ex.exState == 1) {
            warningX = ex.x;
            warningY = ex.y;
            radius = 112;
        } else return;
        shapes.setColor(CORAL);
        shapes.circle(warningX, warningY, radius, 48);
        shapes.setColor(INK);
        shapes.circle(warningX, warningY, radius - 5, 48);
    }

    private void drawExEnemy(CombatRoom.Enemy ex) {
        if (ex.kind == CombatRoom.LIQUID_CAT) {
            if (ex.exState == 1) {
                shapes.setColor(GREEN);
                shapes.ellipse(ex.x - 29, ex.y - 11, 58, 22, 24);
            }
        } else if (ex.kind == CombatRoom.ALLERGIC_VACUUM) {
            shapes.setColor(GOLD);
            shapes.rect(ex.x - 28, ex.y - 25, 58, 53);
            shapes.setColor(CORAL);
            shapes.rect(ex.x - 34, ex.y - 15, 12, 28);
            shapes.setColor(INK);
            shapes.rect(ex.x - 17, ex.y - 3, 32, 24);
            shapes.circle(ex.x - 12, ex.y - 28, 9, 16);
            shapes.circle(ex.x + 20, ex.y - 28, 9, 16);
            shapes.setColor(CYAN);
            shapes.rect(ex.x - 8, ex.y + 7, 6, 4);
            shapes.rect(ex.x + 5, ex.y + 7, 6, 4);
            shapes.setColor(PAPER);
            shapes.rectLine(ex.x - 24, ex.y - 14, ex.x - 57, ex.y - 24, 7);
            shapes.rect(ex.x - 72, ex.y - 30, 26, 8);
            if (ex.exState == 1 || ex.exFlashTime > 0) {
                shapes.setColor(PAPER);
                shapes.circle(ex.x - 63, ex.y - 23, 15, 16);
            }
        } else {
            shapes.setColor(ex.exConfidence >= 60 ? GOLD : PAPER);
            shapes.rect(ex.x - 32, ex.y - 27, 64, 44);
            shapes.triangle(ex.x - 32, ex.y - 27, ex.x - 57, ex.y - 5,
                ex.x - 32, ex.y + 17);
            shapes.setColor(CORAL);
            shapes.rect(ex.x + 18, ex.y - 33, 14, 56);
            shapes.rect(ex.x + 29, ex.y - 10, 23, 12);
            shapes.setColor(INK);
            shapes.rect(ex.x + 21, ex.y - 4, 6, 5);
            for (int i = 0; i < ex.exConfidence / 25; i++) {
                shapes.setColor(GOLD);
                shapes.rect(ex.x - 25 + i * 12, ex.y + 5, 8, 7);
            }
            if (ex.exState == 3) {
                shapes.setColor(CYAN);
                shapes.circle(ex.x - 40, ex.y + 24, 6, 12);
            }
        }
        drawExBars(ex);
    }

    private void drawExBars(CombatRoom.Enemy ex) {
        float width = 80;
        shapes.setColor(INK);
        shapes.rect(ex.x - width / 2, ex.y + 47, width, 7);
        shapes.setColor(CORAL);
        shapes.rect(ex.x - width / 2, ex.y + 47,
            width * Math.max(0, ex.hp / ex.maxHp), 7);
        if (ex.maxArmor > 0 && ex.armor > 0) {
            shapes.setColor(INK);
            shapes.rect(ex.x - width / 2, ex.y + 57, width, 6);
            shapes.setColor(CYAN);
            shapes.rect(ex.x - width / 2, ex.y + 57,
                width * ex.armor / ex.maxArmor, 6);
        } else if (ex.kind == CombatRoom.ALLERGIC_VACUUM) {
            shapes.setColor(INK);
            shapes.rect(ex.x - width / 2, ex.y + 57, width, 6);
            shapes.setColor(GOLD);
            shapes.rect(ex.x - width / 2, ex.y + 57,
                width * ex.exMeter / 100, 6);
        }
    }

    private void drawWarpWarning(CombatRoom.Enemy enemy) {
        float reach = 75;
        shapes.setColor(CORAL);
        shapes.circle(enemy.warpX, enemy.warpY, reach, 48);
        shapes.setColor(INK);
        shapes.circle(enemy.warpX, enemy.warpY, reach - 5, 48);
        shapes.setColor(CORAL);
        shapes.rect(enemy.warpX - 23, enemy.warpY - 2, 46, 4);
        shapes.rect(enemy.warpX - 2, enemy.warpY - 23, 4, 46);
    }

    private void drawBossWarpFlash(CombatRoom.Enemy boss) {
        shapes.setColor(PURPLE);
        shapes.circle(boss.x, boss.y, boss.radius + 14, 36);
        shapes.setColor(INK);
        shapes.circle(boss.x, boss.y, boss.radius + 8, 36);
    }

    private void drawPowerBlast(CombatRoom combat) {
        float progress = 1 - combat.blastTime / .30f;
        float radius = CombatRoom.POWER_BALL_BLAST_RADIUS * (.25f + .75f * progress);
        shapes.setColor(CYAN);
        shapes.circle(combat.blastX, combat.blastY, radius, 56);
        shapes.setColor(INK);
        shapes.circle(combat.blastX, combat.blastY, Math.max(0, radius - 10), 56);
    }

    private void drawNotebookSplash(CombatRoom combat) {
        float radius = 72 * (1 - combat.splashTime / .24f);
        shapes.setColor(GREEN);
        shapes.circle(combat.splashX, combat.splashY, radius, 36);
        shapes.setColor(INK);
        shapes.circle(combat.splashX, combat.splashY, Math.max(0, radius - 7), 36);
    }

    private void drawNotebookBeam(CombatRoom combat) {
        shapes.setColor(BLUE);
        shapes.rectLine(combat.beamX, combat.beamY,
            combat.beamEndX, combat.beamEndY, CombatRoom.NOTEBOOK_BEAM_HALF_WIDTH * 2);
        shapes.setColor(CYAN);
        shapes.rectLine(combat.beamX, combat.beamY,
            combat.beamEndX, combat.beamEndY, 52);
        shapes.setColor(PAPER);
        shapes.rectLine(combat.beamX, combat.beamY,
            combat.beamEndX, combat.beamEndY, 17);
    }

    private void drawAttackLock(PlayerCharacter player) {
        CombatRoom.Enemy target = player.attackTarget();
        if (target == null || player.skillPlaying) return;
        shapes.setColor(GREEN);
        shapes.circle(target.x, target.y, target.radius + 12, 36);
        shapes.setColor(INK);
        shapes.circle(target.x, target.y, target.radius + 7, 36);
    }

    private void drawAttackEffect(PlayerCharacter player) {
        if (player.swingTimer > 0) {
            float progress = 1 - player.swingTimer / PlayerCharacter.LAN_SWING_DURATION;
            float direction = MathUtils.atan2(player.swingFaceY, player.swingFaceX);
            float halfArc = player.lanAoE ? 85 : 65;
            float sweep = direction + MathUtils.degreesToRadians
                * (-halfArc + 2 * halfArc * progress);
            float previousX = player.x + player.swingFaceX * 15;
            float previousY = player.y + player.swingFaceY * 15;
            for (int segment = 1; segment <= 5; segment++) {
                float reach = segment * (PlayerCharacter.LAN_REACH
                    + (player.lanAoE ? 35 : 0)) / 5f;
                float bend = sweep - (5 - segment) * .10f;
                float nextX = player.x + MathUtils.cos(bend) * reach;
                float nextY = player.y + MathUtils.sin(bend) * reach;
                shapes.setColor(INK);
                shapes.rectLine(previousX, previousY, nextX, nextY, 13);
                shapes.setColor(CYAN);
                shapes.rectLine(previousX, previousY, nextX, nextY, 5);
                previousX = nextX;
                previousY = nextY;
            }
            shapes.setColor(GREEN);
            shapes.rect(previousX - 10, previousY - 7, 20, 14);
            shapes.setColor(GOLD);
            float plugX = previousX + MathUtils.cos(sweep) * 9;
            float plugY = previousY + MathUtils.sin(sweep) * 9;
            shapes.circle(plugX, plugY, 4, 8);
        }
    }

    private void drawBars(PlayerCharacter player) {
        shapes.setColor(.23f, .27f, .32f, 1);
        shapes.rect(73, 652, 205, 17);
        shapes.rect(292, 652, 160, 17);
        shapes.setColor(CORAL);
        shapes.rect(73, 652, 205 * player.hp / player.maxHp, 17);
        shapes.setColor(PURPLE);
        shapes.rect(292, 652, 160 * player.sp / 100, 17);
        if (player.isBound()) {
            shapes.setColor(INK);
            shapes.rect(465, 652, 168, 17);
            shapes.setColor(CYAN);
            shapes.rect(465, 652, 168 * player.bindProgress / 6f, 17);
        }
        if (player.notebookHeatBeam) {
            shapes.setColor(INK);
            shapes.rect(650, 652, 155, 17);
            shapes.setColor(player.notebookHeat >= 100 ? GOLD : CORAL);
            shapes.rect(650, 652, 155 * player.notebookHeat / 100, 17);
        }
    }

    private void drawSkillButton(PlayerCharacter player) {
        shapes.setColor(INK);
        shapes.circle(SKILL_X, SKILL_Y, SKILL_RADIUS + 4, 40);
        shapes.setColor(player.sp >= PlayerCharacter.SKILL_SP_COST
            && player.skillCooldown <= 0 && !player.skillPlaying ? PURPLE : DISABLED);
        shapes.circle(SKILL_X, SKILL_Y, SKILL_RADIUS, 40);
    }

    private void drawHud(RunDirector run, PlayerCharacter player, CombatRoom combat) {
        font.draw(batch, "HP " + (int)Math.ceil(player.hp) + "/" + (int)player.maxHp, 73, 690);
        font.draw(batch, "SP " + (int)player.sp + "/100", 292, 690);
        if (player.isBound())
            font.draw(batch, "BOUND: alternate A / D!", 465, 640);
        if (player.notebookHeatBeam)
            font.draw(batch, player.notebookHeat >= 100 ? "BEAM READY"
                : "HEAT " + (int)player.notebookHeat + "/100", 650, 640);
        if (player.slowTime > 0) font.draw(batch, "SLOWED", 825, 640);
        font.draw(batch, "LOOP " + run.plan.loop() + "  |  STAGE "
            + (run.plan.stage() + 1) + "/4  |  "
            + (run.room == RunDirector.Room.EX ? "EX ENCOUNTER" : run.plan.stageName()),
            490, 685);
        String target = run.room == RunDirector.Room.BOSS ? run.plan.bossName()
            : run.room == RunDirector.Room.EX ? RunPlan.EX_MONSTERS[run.exId]
            : "Monster room: " + combat.enemies.size + " left";
        font.draw(batch, target, 75, 62);
        if (run.room == RunDirector.Room.BOSS && run.plan.bossId() == RunPlan.TALGO_ID) {
            for (CombatRoom.Enemy enemy : combat.enemies)
                if (enemy.kind == RunPlan.TALGO_ID && enemy.talgoCharge > 0) {
                    font.draw(batch, "FULL ROOM AOE IN "
                        + (int)Math.ceil(enemy.talgoCharge * 10) / 10f
                        + "s: GREEN CIRCLE OR DASH AT IMPACT", 350, 106);
                    break;
                }
        }
        if (run.room == RunDirector.Room.EX && combat.enemies.size > 0) {
            CombatRoom.Enemy ex = combat.enemies.first();
            String status = ex.kind == CombatRoom.LIQUID_CAT
                ? (ex.exState == 1 ? "PUDDLE: smaller hitbox + slowing ooze"
                    : "CAT: watch the leap marker")
                : ex.kind == CombatRoom.ALLERGIC_VACUUM
                    ? "ALLERGY " + (int)ex.exMeter + "/100  |  "
                        + (ex.exState == 0 ? "SUCKING" : ex.exState == 1
                            ? "SNEEZE WARNING" : "RECOVERING")
                    : "CONFIDENCE " + ex.exConfidence + "%  |  NOTES "
                        + combat.carriedNotes + "  |  bring notes close";
            font.draw(batch, status, 355, 62);
        }
        if (run.room == RunDirector.Room.BOSS && combat.enemies.size > 0) {
            CombatRoom.Enemy boss = null;
            for (CombatRoom.Enemy enemy : combat.enemies)
                if (enemy.kind >= 0) { boss = enemy; break; }
            if (boss != null) {
                String trait = boss.summoner ? "SUMMONS  " : "";
                if (boss.reflector && boss.armor > 0) trait += "SHIELD/REFLECT  ";
                if (boss.warper) trait += "WARP  ";
                if (boss.artillery) trait += "HUGE SHOTS  ";
                if (boss.waveCaster) trait += "WAVES";
                if (!trait.isEmpty()) font.draw(batch, trait, 355, 62);
            }
        }
        String active = player.activeWeapon() == PlayerCharacter.Weapon.PHYSICAL
            ? "LAN CABLE" : "NOTEBOOK";
        font.draw(batch, "Weapon: " + active + (player.hasBothWeapons() ? "  |  Q / 1 / 2 switch" : ""),
            75, 35);
        font.draw(batch, "LAN +" + (int)player.physicalBonus
            + "  NB +" + (int)(player.magicalBonus + player.notebookBonus)
            + "  BALL +" + (int)player.skillDamageBonus
            + "  ATK +" + (int)(player.attackSpeedBonus * 100) + "%"
            + "  MOVE +" + (int)player.speedBonus, 75, 16);
        String effects = (player.lanSlow ? "SLOW " : "")
            + (player.lanAoE ? "WIDE " : "")
            + (player.lanTether ? "TETHER " : "")
            + (player.notebookPoison ? "POISON " : "")
            + (player.notebookSplash ? "SPLASH " : "")
            + (player.notebookHeatBeam ? "HEAT" : "");
        if (!effects.isEmpty()) font.draw(batch, effects, 700, 16);
        if (run.room == RunDirector.Room.EX)
            font.draw(batch, "EX POWER: TETHER + HEAT BEAM", 690, 35);
        font.draw(batch, "WASD move   LEFT CLICK / SPACE attack   SHIFT dash   E skill", 600, 48);
    }

    private void drawPanel() {
        shapes.setColor(.07f, .10f, .15f, 1);
        shapes.rect(165, 175, 950, 310);
        shapes.setColor(CYAN);
        shapes.rect(165, 480, 950, 5);
        shapes.rect(165, 175, 950, 5);
    }

    private void drawUpgradeChoices() {
        for (int i = 0; i < 3; i++) {
            float bottom = 307 - i * 65;
            shapes.setColor(.13f, .20f, .26f, 1);
            shapes.rect(190, bottom, 900, 58);
            shapes.setColor(CYAN);
            shapes.rect(190, bottom, 4, 58);
        }
    }

    private void title(String text, float y) { bigFont.draw(batch, text, 205, y); }
    private void line(String text, float y) { font.draw(batch, text, 205, y); }

    void resize(int width, int height) { viewport.update(width, height, true); }

    boolean skillButtonJustPressed() {
        if (!Gdx.input.isTouched()) skillPressCaptured = false;
        if (!Gdx.input.justTouched()) return false;
        touchPosition.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPosition);
        float dx = touchPosition.x - SKILL_X;
        float dy = touchPosition.y - SKILL_Y;
        skillPressCaptured = dx * dx + dy * dy <= SKILL_RADIUS * SKILL_RADIUS;
        return skillPressCaptured;
    }

    boolean mouseAttackHeld() {
        return Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !skillPressCaptured;
    }

    boolean mouseAttackJustPressed() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || skillPressCaptured)
            return false;
        touchPosition.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPosition);
        return true;
    }

    float mouseWorldX() { return touchPosition.x; }
    float mouseWorldY() { return touchPosition.y; }

    int upgradeChoiceJustPressed() {
        if (!Gdx.input.justTouched()) return -1;
        touchPosition.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPosition);
        if (touchPosition.x < 190 || touchPosition.x > 1090) return -1;
        for (int i = 0; i < 3; i++) {
            float bottom = 307 - i * 65;
            if (touchPosition.y >= bottom && touchPosition.y <= bottom + 58)
                return i;
        }
        return -1;
    }

    boolean entranceCarJustPressed() {
        if (!Gdx.input.justTouched()) return false;
        touchPosition.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPosition);
        return touchPosition.x >= 825 && touchPosition.x <= 1024
            && touchPosition.y >= 335 && touchPosition.y <= 455;
    }

    int carChoiceJustPressed() {
        if (!Gdx.input.justTouched()) return -1;
        touchPosition.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPosition);
        if (touchPosition.y < 239 || touchPosition.y > 303) return -1;
        if (touchPosition.x >= 300 && touchPosition.x <= 615) return 0;
        if (touchPosition.x >= 665 && touchPosition.x <= 980) return 1;
        return -1;
    }

    void dispose() {
        student.dispose();
        sati.dispose();
        liquidCat.dispose();
        normalRoom.dispose();
        bossRoom.dispose();
        building30.dispose();
        carObject.dispose();
        shapes.dispose();
        batch.dispose();
        font.dispose();
        bigFont.dispose();
    }
}
