package io.github.kirinojuju.render.scene;

import static io.github.kirinojuju.render.GameRenderer.CAR_ENDING_MESSAGE;
import static io.github.kirinojuju.render.GameRenderer.CAR_NO_BUTTON;
import static io.github.kirinojuju.render.GameRenderer.CAR_YES_BUTTON;
import static io.github.kirinojuju.render.GameRenderer.CORAL;
import static io.github.kirinojuju.render.GameRenderer.CYAN;
import static io.github.kirinojuju.render.GameRenderer.GOLD;
import static io.github.kirinojuju.render.GameRenderer.GREEN;
import static io.github.kirinojuju.render.GameRenderer.INK;
import static io.github.kirinojuju.render.GameRenderer.PAPER;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.render.OptionalAssetTexture;
import io.github.kirinojuju.render.sprite.StudentSprite;
import io.github.kirinojuju.world.EntranceScene;
import io.github.kirinojuju.world.GameSpace;
import io.github.kirinojuju.world.RoomObstacles.TableSprites;

/** Backgrounds, entrance/car screens and scene fades. */
public final class SceneRenderer implements Disposable {
    private final Rectangle startButton = new Rectangle(540, 60, 200, 116);
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont font, bigFont;
    private final StudentSprite student;
    private final TableSprites tables = new TableSprites();
    private final Texture classroom = loadRoom(Gdx.files.internal("scene/sceneroom_new_no_table.png").exists()
        ? "scene/sceneroom_new_no_table.png" : "scene/sceneroom_not_table.png");
    private final Texture serverRoom = loadRoom("scene/EX_Room/ServerRoom.png");
    private final Texture building30 = loadRoom("scene/scene30building_entrance_withoutcar.png");
    private final Texture carObject = loadRoom("scene/scene30bCar_object.png");
    private final Texture cover = Gdx.files.internal("scene/ปกเกม/Whatzz.png").exists()
        ? loadRoom("scene/ปกเกม/Whatzz.png") : null;
    private final Texture gameTitle = loadUiTexture("game-title.png");
    private final Texture introThai = loadUiTexture("intro-story-th.png");
    private final TextureRegion[][] introThaiLines = introThai == null ? null
        : TextureRegion.split(introThai, introThai.getWidth(), introThai.getHeight() / RunDirector.introPageCount());
    private final Texture startNormal = optional("click1_before_c.png");
    private final Texture startFocus = optional("click2_focus.png");
    // Both exports use the same 64x64 canvas. Crop their shared visible area at a crisp 4x scale.
    private final TextureRegion normalButton = buttonRegion(startNormal);
    private final TextureRegion focusButton = buttonRegion(startFocus);

    public SceneRenderer(SpriteBatch batch, ShapeRenderer shapes, BitmapFont font,
                  BitmapFont bigFont, StudentSprite student) {
        this.batch = batch; this.shapes = shapes;
        this.font = font; this.bigFont = bigFont; this.student = student;
    }

    public void drawRoom(RunDirector run, CombatRoom combat) {
        batch.begin();
        drawScene(run);
        tables.draw(batch, combat.obstacles);
        batch.end();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawArena(run.room);
        shapes.end();
    }

    private static Texture loadRoom(String path) {
        return OptionalAssetTexture.load(path);
    }

    private static Texture optional(String path) {
        return Gdx.files.internal(path).exists() ? loadRoom(path) : null;
    }

    private static TextureRegion buttonRegion(Texture texture) {
        return texture == null ? null : new TextureRegion(texture, 8, 19, 50, 29);
    }

    public boolean isStartButtonAt(float x, float y) { return startButton.contains(x, y); }

    private static Texture loadUiTexture(String name) {
        FileHandle file = Gdx.files.classpath("ui/" + name);
        // IDE launches may not have copied resources yet. Support the project and assets folders.
        if (!file.exists()) file = Gdx.files.local("core/src/main/resources/ui/" + name);
        if (!file.exists()) file = Gdx.files.local("../core/src/main/resources/ui/" + name);
        if (!file.exists()) {
            Gdx.app.log("SceneRenderer", "UI text image missing: ui/" + name);
            return null;
        }
        return new Texture(file);
    }

    public static boolean isEntrancePhase(RunDirector.Phase phase) {
        return phase == RunDirector.Phase.INTRO || phase == RunDirector.Phase.START_SELECT
            || phase == RunDirector.Phase.ENTRANCE
            || phase == RunDirector.Phase.CAR_PROMPT
            || phase == RunDirector.Phase.CAR_ENDING;
    }

    public void drawEntrance(RunDirector run, PlayerCharacter player, boolean startHovered) {
        if (run.phase == RunDirector.Phase.INTRO) {
            drawIntro(run);
            return;
        }
        if (run.phase == RunDirector.Phase.START_SELECT && cover != null) {
            drawTitleScreen(startHovered);
            return;
        }
        if (run.phase == RunDirector.Phase.CAR_ENDING) {
            drawCarEnding();
            return;
        }
        drawEntranceBackground();
        drawEntranceOverlay(run.phase);
        batch.begin();
        if (run.phase == RunDirector.Phase.ENTRANCE
            || run.phase == RunDirector.Phase.CAR_PROMPT)
            student.draw(batch, player);
        drawEntranceText(run.phase, player);
        if (run.phase == RunDirector.Phase.START_SELECT) drawStartButton(startHovered);
        batch.end();
    }

    private void drawTitleScreen(boolean hovered) {
        float scale = Math.min(GameSpace.WIDTH / cover.getWidth(), GameSpace.HEIGHT / cover.getHeight());
        float width = cover.getWidth() * scale, height = cover.getHeight() * scale;
        batch.begin();
        batch.draw(cover, (GameSpace.WIDTH - width) / 2, (GameSpace.HEIGHT - height) / 2, width, height);
        drawStartButton(hovered);
        batch.end();
    }

    private void drawStartButton(boolean hovered) {
        TextureRegion image = hovered && focusButton != null ? focusButton : normalButton;
        if (image != null)
            batch.draw(image, startButton.x, startButton.y, startButton.width, startButton.height);
        else centeredText(bigFont, hovered ? "[ START ]" : "START", startButton.y + 72);
        centeredText(font, "CLICK START / ENTER", 40);
    }

    private void drawGameTitle(float centerY, float maxWidth) {
        if (gameTitle == null) return;
        float width = Math.min(maxWidth, gameTitle.getWidth());
        float height = gameTitle.getHeight() * width / gameTitle.getWidth();
        batch.draw(gameTitle, (GameSpace.WIDTH - width) / 2, centerY - height / 2, width, height);
    }

    private void centeredText(BitmapFont textFont, String text, float y) {
        GlyphLayout layout = new GlyphLayout(textFont, text);
        float scaleX = textFont.getData().scaleX, scaleY = textFont.getData().scaleY;
        if (layout.width > GameSpace.WIDTH - 160) {
            float fit = (GameSpace.WIDTH - 160) / layout.width;
            textFont.getData().setScale(scaleX * fit, scaleY * fit);
            layout.setText(textFont, text);
        }
        textFont.draw(batch, text, (GameSpace.WIDTH - layout.width) / 2, y);
        textFont.getData().setScale(scaleX, scaleY);
    }

    private void drawIntro(RunDirector run) {
        ScreenUtils.clear(0, 0, 0, 1);
        float alpha = run.introAlpha();
        batch.begin();
        bigFont.setColor(PAPER.r, PAPER.g, PAPER.b, alpha);
        centeredText(bigFont, run.introEnglish(), 400);
        bigFont.setColor(PAPER);
        if (introThaiLines != null) {
            batch.setColor(PAPER.r, PAPER.g, PAPER.b, alpha);
            batch.draw(introThaiLines[run.introPage()][0], 240, 300, 800, 64);
            batch.setColor(Color.WHITE);
        }
        centeredText(font, (run.introPage() + 1) + " / " + RunDirector.introPageCount(), 155);
        centeredText(font, "ENTER / SPACE / CLICK: continue    ESC: skip", 95);
        batch.end();
    }

    private void drawCarEnding() {
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();
        centeredText(bigFont, CAR_ENDING_MESSAGE, 400);
        centeredText(font, "Life really is too short...", 335);
        centeredText(font, "Press ENTER or R to start a new run", 260);
        batch.end();
    }

    private void drawEntranceBackground() {
        batch.begin();
        batch.draw(building30, EntranceScene.DRAW_X, EntranceScene.DRAW_Y,
            EntranceScene.DRAW_SIZE, EntranceScene.DRAW_SIZE);
        batch.draw(carObject, EntranceScene.DRAW_X, EntranceScene.DRAW_Y,
            EntranceScene.DRAW_SIZE, EntranceScene.DRAW_SIZE);
        batch.end();
    }

    private void drawEntranceOverlay(RunDirector.Phase phase) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        switch (phase) {
            case START_SELECT:
                drawPanel();
                break;
            case ENTRANCE:
                shapes.setColor(INK);
                shapes.rect(EntranceScene.DRAW_X, 18, EntranceScene.DRAW_SIZE, 51);
                shapes.setColor(CYAN);
                shapes.rect(349, 321, 82, 4);
                shapes.triangle(343, 323, 358, 334, 358, 312);
                shapes.setColor(GOLD);
                shapes.rect(EntranceScene.CAR_X - 2, 459, 4, 25);
                shapes.circle(EntranceScene.CAR_X, 487, 6, 16);
                break;
            case CAR_PROMPT:
                shapes.setColor(INK);
                shapes.rect(265, 207, 750, 285);
                shapes.setColor(CYAN);
                shapes.rect(265, 487, 750, 5);
                shapes.setColor(GREEN);
                shapes.rect(CAR_YES_BUTTON.x, CAR_YES_BUTTON.y,
                    CAR_YES_BUTTON.width, CAR_YES_BUTTON.height);
                shapes.setColor(CORAL);
                shapes.rect(CAR_NO_BUTTON.x, CAR_NO_BUTTON.y,
                    CAR_NO_BUTTON.width, CAR_NO_BUTTON.height);
                break;
            default:
                break;
        }
        shapes.end();
    }

    private void drawEntranceText(RunDirector.Phase phase, PlayerCharacter player) {
        switch (phase) {
            case START_SELECT:
                drawGameTitle(415, 850);
                line("Four stages, four Ajarns, and an endless school-life loop.", 330);
                line("First stop: Building 30. One car. One suspiciously easy way out.", 285);
                line("Press ENTER to begin.", 210);
                break;
            case ENTRANCE:
                bigFont.draw(batch, "BUILDING 30", 30, 650);
                font.draw(batch, "LEFT DOOR", 34, 555);
                font.draw(batch, "CORE GAME", 34, 526);
                font.draw(batch, "CAR", 1030, 555);
                font.draw(batch, EntranceScene.atCar(player)
                    ? "E / CLICK TO INTERACT" : "WALK CLOSER", 1005, 526);
                font.draw(batch, "WASD / ARROWS: walk    LEFT DOOR: enter school"
                    + "    E near car: go home?", 289, 50);
                break;
            case CAR_PROMPT:
                title("GO HOME?", 407);
                line("You can skip four years of boss fights...", 345);
                font.draw(batch, "[1] YES, GO HOME", CAR_YES_BUTTON.x + 55, CAR_YES_BUTTON.y + 39);
                font.draw(batch, "[2] NO, FACE SCHOOL", CAR_NO_BUTTON.x + 42, CAR_NO_BUTTON.y + 39);
                break;
            default:
                break;
        }
    }

    public void drawSceneFade(float alpha) {
        if (alpha <= 0) return;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, alpha);
        shapes.rect(0, 0, GameSpace.WIDTH, GameSpace.HEIGHT);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawArena(RunDirector.Room room) {
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
        shapes.rect(left, room == RunDirector.Room.FINAL_EXAM
            ? GameSpace.SERVER_LANE_TOP : GameSpace.WALK_TOP, right - left, 3);
    }

    private void drawScene(RunDirector run) {
        if (run.room == RunDirector.Room.FINAL_EXAM) {
            batch.draw(serverRoom, GameSpace.LEFT, GameSpace.BOTTOM,
                GameSpace.RIGHT - GameSpace.LEFT, GameSpace.TOP - GameSpace.BOTTOM);
            return;
        }
        Texture scene = classroom;
        float tileWidth = (GameSpace.RIGHT - GameSpace.LEFT) / 2;
        float height = GameSpace.TOP - GameSpace.BOTTOM;
        batch.draw(scene, GameSpace.LEFT, GameSpace.BOTTOM, tileWidth, height);
        batch.draw(scene, GameSpace.LEFT + tileWidth, GameSpace.BOTTOM,
            tileWidth, height);
    }

    private void drawPanel() {
        shapes.setColor(.07f, .10f, .15f, 1);
        shapes.rect(165, 175, 950, 310);
        shapes.setColor(CYAN);
        shapes.rect(165, 480, 950, 5);
        shapes.rect(165, 175, 950, 5);
    }

    private void title(String text, float y) { bigFont.draw(batch, text, 205, y); }

    private void line(String text, float y) { font.draw(batch, text, 205, y); }

    @Override public void dispose() {
        tables.dispose(); classroom.dispose(); serverRoom.dispose();
        building30.dispose(); carObject.dispose();
        if (cover != null) cover.dispose();
        if (gameTitle != null) gameTitle.dispose();
        if (introThai != null) introThai.dispose();
        if (startNormal != null) startNormal.dispose();
        if (startFocus != null) startFocus.dispose();
    }
}
