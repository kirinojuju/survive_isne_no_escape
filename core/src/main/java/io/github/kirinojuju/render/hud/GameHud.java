package io.github.kirinojuju.render.hud;

import static io.github.kirinojuju.render.GameRenderer.CORAL;
import static io.github.kirinojuju.render.GameRenderer.CYAN;
import static io.github.kirinojuju.render.GameRenderer.DISABLED;
import static io.github.kirinojuju.render.GameRenderer.GOLD;
import static io.github.kirinojuju.render.GameRenderer.ICE_WALL_X;
import static io.github.kirinojuju.render.GameRenderer.INK;
import static io.github.kirinojuju.render.GameRenderer.LAN_WAVE_X;
import static io.github.kirinojuju.render.GameRenderer.PURPLE;
import static io.github.kirinojuju.render.GameRenderer.SKILL_RADIUS;
import static io.github.kirinojuju.render.GameRenderer.SKILL_X;
import static io.github.kirinojuju.render.GameRenderer.SKILL_Y;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.boss.DomeBoss;
import io.github.kirinojuju.enemy.ex.ExBoss;
import io.github.kirinojuju.enemy.ex.InsecureSwordBoss;
import io.github.kirinojuju.enemy.ex.LiquidCatBoss;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.player.Upgrade;
import io.github.kirinojuju.render.OptionalAssetTexture;

/** Player HUD, skill buttons, upgrades and combat menu text. */
public final class GameHud implements Disposable {
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont font, bigFont;
    private final Texture hpPotion = OptionalAssetTexture.load("weapons/potion/Potion_hp.png");
    private final Texture spPotion = OptionalAssetTexture.load("weapons/potion/Potion_mp.png");

    public GameHud(SpriteBatch batch, ShapeRenderer shapes, BitmapFont font, BitmapFont bigFont) {
        this.batch = batch; this.shapes = shapes; this.font = font; this.bigFont = bigFont;
    }

    public void drawShapes(RunDirector run, PlayerCharacter player) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawBars(player);
        if (run.phase == RunDirector.Phase.FIGHT) {
            drawSkillButton(player); drawIceWallButton(player); drawLanWaveButton(player);
        } else drawPanel();
        if (run.phase == RunDirector.Phase.UPGRADE) drawUpgradeChoices();
        shapes.end();
    }

    public void drawCutscene() {
        batch.begin();
        title("Good...Let's us prepare the final EXAM", 390);
        line("Press ENTER to face Ae and Dome", 325);
        batch.end();
    }

    public void drawCombatPanel(RunDirector run, PlayerCharacter player, CombatRoom combat) {
        drawHud(run, player, combat);
        if (run.phase == RunDirector.Phase.FIGHT) {
            font.draw(batch, "BALL", SKILL_X - 25, SKILL_Y + 6);
            font.draw(batch, "E", SKILL_X - 6, SKILL_Y - 16);
            font.draw(batch, "ICE", ICE_WALL_X - 18, SKILL_Y + 6);
            font.draw(batch, "F", ICE_WALL_X - 5, SKILL_Y - 16);
            font.draw(batch, "LAN", LAN_WAVE_X - 22, SKILL_Y + 6);
            font.draw(batch, "R", LAN_WAVE_X - 5, SKILL_Y - 16);
        }
        if (run.phase == RunDirector.Phase.TRANSITION) {
            title(run.transitionTitle, 415);
            line(run.transitionDetail, 340);
            line("Press ENTER to continue", 260);
        } else if (run.phase == RunDirector.Phase.UPGRADE) {
            title("CHOOSE ONE UPGRADE  |  R: REROLL (" + run.upgradeRerollsRemaining() + ")", 435);
            line(run.transitionTitle + "  |  +" + RunDirector.BOSS_CLEAR_HEAL + " HP", 390);
            for (int i = 0; i < run.offeredUpgrades.length; i++) {
                Upgrade upgrade = run.offeredUpgrades[i];
                float y = 345 - i * 65;
                line("[" + (i + 1) + "] " + upgrade.title, y);
                line(upgrade.detail, y - 25);
            }
        } else if (run.phase == RunDirector.Phase.GAME_OVER) {
            title("GAME OVER", 415);
            line("You reached loop " + run.plan.loop() + ", stage "
                + (run.plan.stage() + 1) + " and cleared " + run.clearedRooms + " rooms.", 330);
            line("Press R to start a new run", 260);
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

    private void drawIceWallButton(PlayerCharacter player) {
        shapes.setColor(INK);
        shapes.circle(ICE_WALL_X, SKILL_Y, SKILL_RADIUS + 4, 40);
        shapes.setColor(player.sp >= PlayerCharacter.ICE_WALL_SP_COST
            && player.iceWallCooldown <= 0 && !player.skillPlaying ? CYAN : DISABLED);
        shapes.circle(ICE_WALL_X, SKILL_Y, SKILL_RADIUS, 40);
    }

    private void drawLanWaveButton(PlayerCharacter player) {
        shapes.setColor(INK);
        shapes.circle(LAN_WAVE_X, SKILL_Y, SKILL_RADIUS + 4, 40);
        boolean ready = player.activeWeapon() == PlayerCharacter.Weapon.PHYSICAL
            && player.lanWaveCooldown <= 0 && !player.skillPlaying;
        shapes.setColor(ready ? CYAN : DISABLED);
        shapes.circle(LAN_WAVE_X, SKILL_Y, SKILL_RADIUS, 40);
    }

    private void drawHud(RunDirector run, PlayerCharacter player, CombatRoom combat) {
        if (run.exTest)
            font.draw(batch, "EX TEST | F1 CAT  F2 VACUUM  F3 SWORD  F4 RETRY", 73, 635);
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
            + (run.room == RunDirector.Room.EX ? "EX ENCOUNTER"
                : run.room == RunDirector.Room.FINAL_EXAM ? "FINAL EXAM"
                : run.plan.stageName()),
            490, 685);
        drawEncounterHud(run, combat);
        drawWeaponHud(run, player);
        batch.draw(hpPotion, 975, 635, 48, 48);
        font.draw(batch, "Z  x" + player.potions.hpCount(), 1020, 662);
        batch.draw(spPotion, 1100, 635, 48, 48);
        font.draw(batch, "X  x" + player.potions.spCount(), 1145, 662);
    }

    private void drawEncounterHud(RunDirector run, CombatRoom combat) {
        font.draw(batch, encounterTitle(run, combat), 75, 62);
        for (Enemy enemy : combat.enemies) {
            if (!(enemy instanceof DomeBoss)) continue;
            if (((DomeBoss) enemy).dome().phase == DomeBoss.Phase.LIGHTNING_CHARGE)
                font.draw(batch, "DOME: LIGHTNING - MOVE FROM THE MARK", 350, 106);
            else if (((DomeBoss) enemy).dome().phase == DomeBoss.Phase.LASER_CHARGE)
                font.draw(batch, "DOME: LASER - CLEAR THE LINE", 350, 106);
        }
        if (run.room == RunDirector.Room.EX && combat.enemies.size > 0) {
            Enemy ex = combat.enemies.first();
            font.draw(batch, exStatus((ExBoss) ex, combat.carriedNotes), 355, 62);
        }
        if (run.room == RunDirector.Room.BOSS && combat.enemies.size > 0)
            drawBossTraits(combat);
    }

    private static String encounterTitle(RunDirector run, CombatRoom combat) {
        switch (run.room) {
            case BOSS: return run.plan.bossName();
            case EX: return RunPlan.EX_MONSTERS[run.exId];
            case FINAL_EXAM: return "Ae / Dome: " + combat.enemies.size + " left";
            default: return "Monster room: " + combat.enemies.size + " left";
        }
    }

    private static String exStatus(ExBoss ex, int carriedNotes) {
        switch (ex.kind) {
            case CombatRoom.LIQUID_CAT:
                if (((LiquidCatBoss) ex).catTransformTime > 0) return "PHASE 2: TRANSFORMING";
                return ex.exState == 1 ? "PHASE 2: DIVIDE + FAST ORBITAL"
                    : "PHASE 1: watch the orbital marker";
            case CombatRoom.ALLERGIC_VACUUM:
                String phase = ex.exState == 0 ? "SUCKING" : ex.exState == 1
                    ? "SNEEZE WARNING" : "RECOVERING";
                return "ALLERGY " + (int)ex.exMeter + "/100  |  " + phase;
            default:
                return "CONFIDENCE " + ((InsecureSwordBoss) ex).exConfidence + "%  |  NOTES "
                    + carriedNotes + "  |  bring notes close";
        }
    }

    private void drawBossTraits(CombatRoom combat) {
        Enemy boss = null;
        for (Enemy enemy : combat.enemies)
            if (enemy.kind >= 0) { boss = enemy; break; }
        int minions = combat.enemies.size - (boss == null ? 0 : 1);
        String trait = (boss == null ? "BOSS DOWN  |  " : "") + "MINIONS: " + minions + "  ";
        if (boss != null) {
            if (boss.reflector && boss.armor > 0) trait += "SHIELD/REFLECT  ";
            if (boss.warper) trait += "WARP  ";
            if (boss.artillery) trait += "HUGE SHOTS  ";
            if (boss.waveCaster) trait += "WAVES";
        }
        font.draw(batch, trait, 355, 62);
    }

    private void drawWeaponHud(RunDirector run, PlayerCharacter player) {
        String active = player.activeWeapon() == PlayerCharacter.Weapon.PHYSICAL
            ? "LAN CABLE" : "NOTEBOOK";
        font.draw(batch, "Weapon: " + active + (player.hasBothWeapons() ? "  |  TAB / 1 / 2 switch" : ""),
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
        font.getData().setScale(1);
        font.draw(batch, run.room == RunDirector.Room.FINAL_EXAM
            ? "A/D move   CLICK/SPACE attack   SHIFT dash   R LAN   E ball   F wall"
            : "WASD move   CLICK/SPACE attack   SHIFT dash   R LAN   E ball   F wall", 540, 35);
        font.getData().setScale(1.3f);
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

    @Override public void dispose() { hpPotion.dispose(); spPotion.dispose(); }
}
