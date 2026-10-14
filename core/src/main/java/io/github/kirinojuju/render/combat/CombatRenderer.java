package io.github.kirinojuju.render.combat;

import static io.github.kirinojuju.render.GameRenderer.BLUE;
import static io.github.kirinojuju.render.GameRenderer.CORAL;
import static io.github.kirinojuju.render.GameRenderer.CYAN;
import static io.github.kirinojuju.render.GameRenderer.GOLD;
import static io.github.kirinojuju.render.GameRenderer.GREEN;
import static io.github.kirinojuju.render.GameRenderer.INK;
import static io.github.kirinojuju.render.GameRenderer.PAPER;
import static io.github.kirinojuju.render.GameRenderer.PURPLE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.boss.AeBoss;
import io.github.kirinojuju.enemy.boss.BossBody;
import io.github.kirinojuju.enemy.boss.DomeBoss;
import io.github.kirinojuju.enemy.boss.KenBoss;
import io.github.kirinojuju.enemy.boss.SatiBoss;
import io.github.kirinojuju.enemy.ex.ExBoss;
import io.github.kirinojuju.enemy.ex.LiquidCatBoss;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.render.sprite.AnimatedEnemySprite;
import io.github.kirinojuju.render.sprite.EnemySprite;
import io.github.kirinojuju.render.sprite.MonsterSprite;
import io.github.kirinojuju.render.sprite.StudentSprite;
import io.github.kirinojuju.render.sprite.boss.AeSprite;
import io.github.kirinojuju.render.sprite.boss.DomeSprite;
import io.github.kirinojuju.render.sprite.boss.KenSprite;
import io.github.kirinojuju.render.sprite.boss.SatiSprite;
import io.github.kirinojuju.render.sprite.effect.IceWallSprite;
import io.github.kirinojuju.render.sprite.effect.LanWaveSprite;
import io.github.kirinojuju.render.sprite.ex.InsecureSwordSprite;
import io.github.kirinojuju.render.sprite.ex.LiquidCatSprite;
import io.github.kirinojuju.weapon.NotebookWeaponAttack.NotebookSprite;
import java.util.Arrays;
import java.util.List;

/** Combat sprites, ground warnings and indicators that follow visible enemy bodies. */
public final class CombatRenderer implements Disposable {
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final StudentSprite student;
    private final IceWallSprite iceWall = new IceWallSprite();
    private final LanWaveSprite lanWave = new LanWaveSprite();
    private final SatiSprite sati = new SatiSprite();
    private final DomeSprite dome = new DomeSprite();
    private final KenSprite ken = new KenSprite();
    private final AeSprite ae = new AeSprite();
    private final EnemySprite m = new AnimatedEnemySprite(RunPlan.M_ID, BossBody.M_DRAW_SIZE,
        "characters/boss/Toop(M)/mBody%d.png", 7, "characters/boss/Toop(M)/mActack%d.png", 8);
    private final MonsterSprite monsters = new MonsterSprite();
    private final NotebookSprite notebook = new NotebookSprite();
    private final LiquidCatSprite liquidCat = new LiquidCatSprite();
    private final InsecureSwordSprite insecureSword = new InsecureSwordSprite();
    private final List<EnemySprite> enemySprites = Arrays.asList(
        monsters, sati, dome, ken, ae, m, liquidCat, insecureSword);

    public CombatRenderer(SpriteBatch batch, ShapeRenderer shapes, BitmapFont font, StudentSprite student) {
        this.batch = batch; this.shapes = shapes; this.font = font; this.student = student;
    }

    public void drawGround(PlayerCharacter player, CombatRoom combat) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy enemy : combat.enemies) drawEnemyBody(enemy);
        drawProjectiles(combat);
        if (combat.blastTime > 0) drawPowerBlast(combat);
        if (combat.splashTime > 0) drawNotebookSplash(combat);
        if (combat.beamTime > 0) drawNotebookBeam(combat);
        drawExObjects(combat);
        drawEnemyWarnings(combat);
        drawAttackEffect(player);
        shapes.end();
    }

    public void drawOverlays(RunDirector run, PlayerCharacter player, CombatRoom combat) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawEnemyTethers(player, combat);
        for (Enemy enemy : combat.enemies) drawEnemyIndicators(enemy);
        if (run.phase == RunDirector.Phase.FIGHT) drawAttackLock(player);
        shapes.end();
    }

    public void drawNotebookIcon(PlayerCharacter player) {
        if (player.activeWeapon() == PlayerCharacter.Weapon.MAGICAL)
            notebook.drawIcon(batch, 36, 19);
    }

    private void drawProjectiles(CombatRoom combat) {
        for (CombatRoom.Shot shot : combat.shots) {
            if (shot.powerBall || shot.satiSpear || shot.liquidCatBullet
                || shot.notebook || shot.algoBall || shot.lanWave) continue;
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
    }

    private void drawEnemyWarnings(CombatRoom combat) {
        for (Enemy enemy : combat.enemies) {
            if (enemy instanceof ExBoss) drawExTelegraph((ExBoss) enemy);
            if (enemy.warpWindup > 0) drawWarpWarning(enemy);
            if (enemy.warpReturnTime > 0) drawBossWarpFlash(enemy);
            if (enemy instanceof SatiBoss && ((SatiBoss) enemy).satiCharge > 0)
                drawSatiWarning((SatiBoss) enemy);
            if (enemy instanceof DomeBoss) drawDomeWarning((DomeBoss) enemy);
            if (enemy instanceof AeBoss) ae.drawLaserShapes(shapes, (AeBoss) enemy);
        }
    }

    private void drawEnemyTethers(PlayerCharacter player, CombatRoom combat) {
        for (Enemy enemy : combat.enemies) {
            if (enemy.tetherTime > 0 && enemy.getHp() > 0) {
                shapes.setColor(CYAN);
                shapes.rectLine(player.x, player.y,
                    enemy.damageX(player.x), enemy.damageY(player.y), 3);
            }
        }
    }

    public void drawCombatSprites(RunDirector run, PlayerCharacter player, CombatRoom combat) {
        iceWall.draw(batch, combat.iceWall);
        for (Enemy enemy : combat.enemies)
            if (enemy.kind == RunPlan.DOME_ID) dome.drawEffect(batch, (DomeBoss) enemy);
            else if (enemy instanceof KenBoss) ken.drawBombWarnings(batch, (KenBoss) enemy);
            else if (enemy instanceof AeBoss) ae.drawLaser(batch, (AeBoss) enemy);
        for (CombatRoom.Shot shot : combat.shots)
            if (shot.lanWave) lanWave.draw(batch, shot);
            else if (shot.powerBall) student.drawPowerBall(batch, shot);
            else if (shot.satiSpear) sati.drawSpear(batch, shot);
            else if (shot.liquidCatBullet) liquidCat.drawBullet(batch, shot);
            else if (shot.notebook) notebook.drawShot(batch, shot);
            else if (shot.algoBall) monsters.drawBall(batch, shot);
        for (CombatRoom.NotebookImpact impact : combat.notebookImpacts)
            notebook.drawImpact(batch, impact);
        for (Enemy enemy : combat.enemies) {
            EnemySprite sprite = spriteFor(enemy);
            if (sprite != null) sprite.draw(batch, enemy, player.x);
        }
        for (Enemy enemy : combat.enemies) {
            if (enemy.kind == CombatRoom.INSECURE_SWORD && ((ExBoss) enemy).exState == 3)
                font.draw(batch, "0!", enemy.x - 12, enemy.y + 111);
            else if (enemy.kind == CombatRoom.ALLERGIC_VACUUM
                && ((ExBoss) enemy).exState == 2 && ((ExBoss) enemy).exFlashTime > 0)
                font.draw(batch, "ACHOO!", enemy.x - 33, enemy.y + 91);
        }
        if (run.room == RunDirector.Room.FINAL_EXAM)
            for (Enemy enemy : combat.enemies)
                font.draw(batch, RunPlan.bossName(enemy.kind),
                    enemy.x - 48, enemy.y + enemy.radius + 28);
        student.draw(batch, player);
    }

    private void drawSatiWarning(SatiBoss boss) {
        float x = SatiBoss.elementX(boss);
        float y = SatiBoss.elementY(boss);
        shapes.setColor(CORAL);
        shapes.rectLine(x, y, boss.satiAimX, boss.satiAimY, 4);
        shapes.circle(boss.satiAimX, boss.satiAimY, 13, 20);
    }

    private void drawDomeWarning(DomeBoss boss) {
        DomeBoss.State state = boss.dome();
        if (state.phase == DomeBoss.Phase.LIGHTNING_CHARGE) {
            ring(state.aimX, state.aimY, DomeBoss.LIGHTNING_RADIUS, 4, GOLD);
            shapes.setColor(GOLD);
            shapes.rectLine(state.aimX - 12, state.aimY, state.aimX + 12, state.aimY, 3);
            shapes.rectLine(state.aimX, state.aimY - 12, state.aimX, state.aimY + 12, 3);
        } else if (state.phase == DomeBoss.Phase.LASER_CHARGE) {
            shapes.setColor(CYAN);
            shapes.rectLine(state.originX, state.originY, state.endX, state.endY, 3);
            shapes.circle(state.originX, state.originY, 7, 16);
        }
    }

    private void drawEnemyBody(Enemy enemy) {
        if (enemy instanceof ExBoss) {
            drawExBody((ExBoss) enemy);
            return;
        }
        EnemySprite sprite = spriteFor(enemy);
        if (enemy.kind >= 0 && (sprite == null || !sprite.hasBody())) {
            shapes.setColor(enemyColor(enemy, enemy.anchored));
            shapes.rect(enemy.x - enemy.radius, enemy.y - enemy.radius,
                enemy.radius * 2, enemy.radius * 2);
            shapes.setColor(INK);
            shapes.rect(enemy.x - 9, enemy.y + 2, 6, 6);
            shapes.rect(enemy.x + 4, enemy.y + 2, 6, 6);
        }
        if (enemy.kind < 0) {
            shapes.setColor(enemyColor(enemy, false));
            shapes.ellipse(enemy.x - 19, enemy.y - 22, 38, 8, 20);
        }
    }

    private void drawEnemyIndicators(Enemy enemy) {
        if (enemy.tetherTime > 0) outlineBody(enemy, CYAN);
        if (enemy.stunTime > 0 || enemy.slowTime > 0) {
            shapes.setColor(CYAN);
            shapes.rect(enemy.aimX() - 3, bodyTop(enemy) + 23, 6, 8);
        }
        if (enemy.poisonTime > 0) {
            shapes.setColor(GREEN);
            shapes.circle(enemy.aimX() + 10, bodyTop(enemy) + 27, 5, 12);
        }
        if (enemy instanceof ExBoss) {
            drawExBars((ExBoss) enemy);
        } else if (enemy.kind >= 0 || enemy.maxArmor > 0) {
            float width = enemy.radius * 2;
            drawEnemyBar(enemy.aimX() - width / 2, bodyTop(enemy) + 8,
                width, 7, width * Math.max(0, enemy.getHp() / enemy.getMaxHp()), CORAL);
            if (enemy.maxArmor > 0 && enemy.armor > 0) {
                drawEnemyBar(enemy.aimX() - width / 2, bodyTop(enemy) + 18,
                    width, 6, width * enemy.armor / enemy.maxArmor, CYAN);
            }
        }
    }

    private EnemySprite spriteFor(Enemy enemy) {
        for (EnemySprite sprite : enemySprites)
            if (sprite.supports(enemy)) return sprite;
        return null;
    }

    private static Color enemyColor(Enemy enemy, boolean finalExam) {
        if (finalExam && enemy.kind == RunPlan.AE_ID) return CYAN;
        switch (enemy.kind) {
            case CombatRoom.BINDER: return CYAN;
            case CombatRoom.WARPER: return PURPLE;
            case CombatRoom.WAVE: return BLUE;
            case CombatRoom.RANGED: return GREEN;
            case CombatRoom.SHIELDED: return PAPER;
            case CombatRoom.BASIC: return GOLD;
            default: return CORAL;
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

    private void drawExTelegraph(ExBoss ex) {
        if (ex.kind == CombatRoom.LIQUID_CAT && ex.exFlashTime > 0 && ex.exMeter == 0) {
            ring(ex.exAimX, ex.exAimY, LiquidCatBoss.ORBITAL_RADIUS, 6, GREEN);
        }
        if (ex.kind == CombatRoom.INSECURE_SWORD && ex.exFlashTime > 0
            && ex.exState == 2) {
            ring(ex.exAimX, ex.exAimY, 100, 6, GOLD);
        }
        float warningX, warningY, radius;
        if (ex.kind == CombatRoom.LIQUID_CAT && ex.exMeter > 0) {
            warningX = ex.exAimX;
            warningY = ex.exAimY;
            radius = LiquidCatBoss.ORBITAL_RADIUS;
        } else if (ex.kind == CombatRoom.INSECURE_SWORD && ex.exState == 1) {
            warningX = ex.exAimX;
            warningY = ex.exAimY;
            radius = 100;
        } else if (ex.kind == CombatRoom.ALLERGIC_VACUUM && ex.exState == 1) {
            warningX = ex.x;
            warningY = ex.y;
            radius = 112;
        } else return;
        ring(warningX, warningY, radius, 4, CORAL);
    }

    private void drawExBody(ExBoss ex) {
        if (ex.kind == CombatRoom.ALLERGIC_VACUUM) {
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
        }
    }

    private void drawExBars(ExBoss ex) {
        float width = 80;
        float barY = bodyTop(ex) + 10;
        drawEnemyBar(ex.aimX() - width / 2, barY, width, 7,
            width * Math.max(0, ex.getHp() / ex.getMaxHp()), CORAL);
        if (ex.maxArmor > 0 && ex.armor > 0) {
            drawEnemyBar(ex.aimX() - width / 2, barY + 10, width, 6,
                width * ex.armor / ex.maxArmor, CYAN);
        } else if (ex.kind == CombatRoom.ALLERGIC_VACUUM) {
            drawEnemyBar(ex.aimX() - width / 2, barY + 10, width, 6,
                width * ex.exMeter / 100, GOLD);
        }
    }

    private void drawEnemyBar(float x, float y, float width, float height,
                              float fillWidth, Color color) {
        shapes.setColor(INK);
        shapes.rect(x, y, width, height);
        shapes.setColor(color);
        shapes.rect(x, y, fillWidth, height);
    }

    private void drawWarpWarning(Enemy enemy) {
        float reach = enemy.kind == RunPlan.AE_ID ? AeBoss.AE_WARP_RADIUS : 75;
        ring(enemy.warpX, enemy.warpY, reach, 4, CORAL);
        shapes.setColor(CORAL);
        shapes.rect(enemy.warpX - 23, enemy.warpY - 2, 46, 4);
        shapes.rect(enemy.warpX - 2, enemy.warpY - 23, 4, 46);
    }

    private void drawBossWarpFlash(Enemy boss) {
        outlineBody(boss, PURPLE);
    }

    private void drawPowerBlast(CombatRoom combat) {
        float progress = 1 - combat.blastTime / .30f;
        float radius = CombatRoom.POWER_BALL_BLAST_RADIUS * (.25f + .75f * progress);
        ring(combat.blastX, combat.blastY, radius, 8, CYAN);
    }

    private void drawNotebookSplash(CombatRoom combat) {
        float radius = 72 * (1 - combat.splashTime / .24f);
        ring(combat.splashX, combat.splashY, radius, 5, GREEN);
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
        Enemy target = player.attackTarget();
        if (target == null || player.skillPlaying) return;
        outlineBody(target, GREEN);
    }

    private float bodyTop(Enemy enemy) {
        return BossBody.hasBounds(enemy) ? BossBody.top(enemy) : enemy.y + enemy.radius;
    }

    /** Use the same visible rectangle as incoming damage, excluding transparent image padding. */
    private void outlineBody(Enemy enemy, Color color) {
        if (!BossBody.hasBounds(enemy)) {
            ring(enemy.x, enemy.y, enemy.radius + 7, 3, color);
            return;
        }
        float left = BossBody.left(enemy), right = BossBody.right(enemy);
        float bottom = BossBody.bottom(enemy), top = BossBody.top(enemy);
        shapes.setColor(color);
        shapes.rectLine(left, bottom, right, bottom, 3);
        shapes.rectLine(right, bottom, right, top, 3);
        shapes.rectLine(right, top, left, top, 3);
        shapes.rectLine(left, top, left, bottom, 3);
    }

    /** An open circle: draw only its edge so the room and player remain visible inside. */
    private void ring(float x, float y, float radius, float thickness, Color color) {
        if (radius <= 0) return;
        shapes.setColor(color);
        float inner = Math.max(0, radius - thickness);
        for (int i = 0; i < 48; i++) {
            float a = i * MathUtils.PI2 / 48, b = (i + 1) * MathUtils.PI2 / 48;
            float ax = MathUtils.cos(a), ay = MathUtils.sin(a);
            float bx = MathUtils.cos(b), by = MathUtils.sin(b);
            shapes.triangle(x + ax * radius, y + ay * radius,
                x + bx * radius, y + by * radius, x + ax * inner, y + ay * inner);
            shapes.triangle(x + ax * inner, y + ay * inner,
                x + bx * radius, y + by * radius, x + bx * inner, y + by * inner);
        }
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

    @Override public void dispose() {
        iceWall.dispose(); lanWave.dispose(); notebook.dispose();
        for (EnemySprite sprite : enemySprites) sprite.dispose();
    }
}
