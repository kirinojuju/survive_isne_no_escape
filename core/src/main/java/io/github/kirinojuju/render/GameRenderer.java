package io.github.kirinojuju.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.render.combat.CombatRenderer;
import io.github.kirinojuju.render.hud.GameHud;
import io.github.kirinojuju.render.scene.SceneRenderer;
import io.github.kirinojuju.render.sprite.StudentSprite;
import io.github.kirinojuju.world.GameSpace;

/** Coordinates scene, combat and HUD drawing; converts pointer input to world coordinates. */
public final class GameRenderer {
    public static final float SKILL_X = 1138, SKILL_Y = 155, SKILL_RADIUS = 45;
    public static final float ICE_WALL_X = 1035;
    public static final float LAN_WAVE_X = 932;
    // Drawing and clicking use the same car choice bounds.
    public static final Rectangle CAR_TARGET = new Rectangle(825, 335, 199, 120);
    public static final Rectangle CAR_YES_BUTTON = new Rectangle(300, 239, 315, 64);
    public static final Rectangle CAR_NO_BUTTON = new Rectangle(665, 239, 315, 64);
    public static final String CAR_ENDING_MESSAGE = "You didn't graduate in four years.";
    public static final Color INK = new Color(.09f, .12f, .18f, 1);
    public static final Color PAPER = new Color(.91f, .94f, .91f, 1);
    public static final Color CYAN = new Color(.36f, .85f, .86f, 1);
    public static final Color GOLD = new Color(1, .78f, .35f, 1);
    public static final Color CORAL = new Color(.96f, .39f, .39f, 1);
    public static final Color PURPLE = new Color(.72f, .48f, .94f, 1);
    public static final Color GREEN = new Color(.43f, .87f, .57f, 1);
    public static final Color BLUE = new Color(.30f, .60f, 1f, 1);
    public static final Color DISABLED = new Color(.31f, .31f, .39f, 1);

    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(GameSpace.WIDTH, GameSpace.HEIGHT, camera);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final BitmapFont bigFont = new BitmapFont();
    private final StudentSprite student = new StudentSprite();
    private final SceneRenderer scene = new SceneRenderer(batch, shapes, font, bigFont, student);
    private final CombatRenderer combatView = new CombatRenderer(batch, shapes, font, student);
    private final GameHud hud = new GameHud(batch, shapes, font, bigFont);
    private final Vector2 touchPosition = new Vector2();
    private boolean skillPressCaptured;

    public GameRenderer() {
        viewport.apply();
        camera.position.set(GameSpace.WIDTH / 2, GameSpace.HEIGHT / 2, 0);
        camera.update();
        font.getData().setScale(1.3f);
        bigFont.getData().setScale(2.4f);
        font.setColor(PAPER);
        bigFont.setColor(PAPER);
    }

    public void draw(RunDirector run, PlayerCharacter player, CombatRoom combat) {
        ScreenUtils.clear(INK);
        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        if (run.phase == RunDirector.Phase.CUTSCENE) {
            ScreenUtils.clear(0, 0, 0, 1);
            hud.drawCutscene();
            return;
        }
        if (SceneRenderer.isEntrancePhase(run.phase)) {
            updateTouchPosition();
            scene.drawEntrance(run, player, scene.isStartButtonAt(touchPosition.x, touchPosition.y));
        } else {
            scene.drawRoom(run, combat);
            combatView.drawGround(player, combat);
            if (run.phase == RunDirector.Phase.FIGHT) {
                batch.begin();
                combatView.drawCombatSprites(run, player, combat);
                batch.end();
            }
            // Body drawing has now selected this frame's visible bounds.
            combatView.drawOverlays(run, player, combat);
            hud.drawShapes(run, player);
            batch.begin();
            combatView.drawNotebookIcon(player);
            hud.drawCombatPanel(run, player, combat);
            batch.end();
        }
        scene.drawSceneFade(run.sceneFade);
    }

    public void resize(int width, int height) { viewport.update(width, height, true); }

    public boolean skillButtonJustPressed() {
        if (!Gdx.input.isTouched()) skillPressCaptured = false;
        if (!touchJustPressed()) return false;
        float dx = touchPosition.x - SKILL_X;
        float dy = touchPosition.y - SKILL_Y;
        skillPressCaptured = dx * dx + dy * dy <= SKILL_RADIUS * SKILL_RADIUS;
        return skillPressCaptured;
    }

    public boolean mouseAttackHeld() {
        return Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !skillPressCaptured;
    }

    public boolean iceWallButtonJustPressed() {
        if (!Gdx.input.isTouched()) skillPressCaptured = false;
        if (!touchJustPressed()) return false;
        float dx = touchPosition.x - ICE_WALL_X, dy = touchPosition.y - SKILL_Y;
        boolean pressed = dx * dx + dy * dy <= SKILL_RADIUS * SKILL_RADIUS;
        skillPressCaptured |= pressed;
        return pressed;
    }

    public boolean lanWaveButtonJustPressed() {
        if (!Gdx.input.isTouched()) skillPressCaptured = false;
        if (!touchJustPressed()) return false;
        float dx = touchPosition.x - LAN_WAVE_X, dy = touchPosition.y - SKILL_Y;
        boolean pressed = dx * dx + dy * dy <= SKILL_RADIUS * SKILL_RADIUS;
        skillPressCaptured |= pressed;
        return pressed;
    }

    public boolean mouseAttackJustPressed() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || skillPressCaptured)
            return false;
        updateTouchPosition();
        return true;
    }

    public float mouseWorldX() { return touchPosition.x; }

    public float mouseWorldY() { return touchPosition.y; }

    public int upgradeChoiceJustPressed() {
        if (!touchJustPressed()) return -1;
        if (touchPosition.x < 190 || touchPosition.x > 1090) return -1;
        for (int i = 0; i < 3; i++) {
            float bottom = 307 - i * 65;
            if (touchPosition.y >= bottom && touchPosition.y <= bottom + 58)
                return i;
        }
        return -1;
    }

    public boolean entranceCarJustPressed() {
        return touchJustPressed() && CAR_TARGET.contains(touchPosition);
    }

    public boolean startButtonJustPressed() {
        return touchJustPressed() && scene.isStartButtonAt(touchPosition.x, touchPosition.y);
    }

    public int carChoiceJustPressed() {
        if (!touchJustPressed()) return -1;
        if (CAR_YES_BUTTON.contains(touchPosition)) return 0;
        if (CAR_NO_BUTTON.contains(touchPosition)) return 1;
        return -1;
    }

    private boolean touchJustPressed() {
        if (!Gdx.input.justTouched()) return false;
        updateTouchPosition();
        return true;
    }

    private void updateTouchPosition() {
        touchPosition.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPosition);
    }

    public void dispose() {
        scene.dispose(); combatView.dispose(); hud.dispose(); student.dispose();
        shapes.dispose(); batch.dispose(); font.dispose(); bigFont.dispose();
    }
}
