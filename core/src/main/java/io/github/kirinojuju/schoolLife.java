package io.github.kirinojuju;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.render.GameRenderer;
import java.util.Random;

/** Desktop game entry point. Each gameplay area lives in its own class. */
public class schoolLife extends ApplicationAdapter {
    public static final String GAME_TITLE = "มาทั้งที่ก็เป็นนักเรียนติดลูปไปซะได้";
    public enum TestMode {
        NONE(-1), FINAL_EXAM(-1), OBSTACLES(-1),
        EX_LIQUID_CAT(0), EX_ALLERGIC_VACUUM(1), EX_INSECURE_SWORD(2);

        public final int exId;
        TestMode(int exId) { this.exId = exId; }
    }
    private final Random random = new Random();
    private TestMode testMode;
    private PlayerCharacter player;
    private CombatRoom combat;
    private RunDirector run;
    private GameRenderer renderer;
    private GameAudio audio;

    public schoolLife() { this(TestMode.NONE); }

    public schoolLife(TestMode testMode) { this.testMode = testMode; }

    @Override
    public void create() {
        renderer = new GameRenderer();
        audio = new GameAudio();
        newRun();
    }

    private void newRun() {
        player = new PlayerCharacter();
        combat = new CombatRoom(random);
        run = new RunDirector(random);
        player.setAudio(audio);
        combat.setAudio(audio);
        run.setAudio(audio);
        if (testMode == TestMode.FINAL_EXAM) run.startFinalExamTest(player);
        else if (testMode == TestMode.OBSTACLES) run.startObstaclesTest(player, combat);
        else if (testMode.exId >= 0) run.startExTest(player, combat, testMode.exId);
    }

    private void updateExTestControls() {
        if (testMode.exId < 0) return;
        TestMode selected = selectedExTestMode();
        boolean retry = Gdx.input.isKeyJustPressed(Input.Keys.R)
            || (run.phase == RunDirector.Phase.TRANSITION
                && Gdx.input.isKeyJustPressed(Input.Keys.ENTER));
        if (selected != null) testMode = selected;
        if (selected != null || retry) newRun();
    }

    private static TestMode selectedExTestMode() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) return TestMode.EX_LIQUID_CAT;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) return TestMode.EX_ALLERGIC_VACUUM;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) return TestMode.EX_INSECURE_SWORD;
        return null;
    }

    @Override
    public void render() {
        updateExTestControls();
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        audio.update(dt);
        updateRun(dt);
        renderer.draw(run, player, combat);
    }

    private void updateRun(float dt) {
        boolean touchSkill = run.phase == RunDirector.Phase.FIGHT
            && renderer.skillButtonJustPressed();
        boolean touchIceWall = run.phase == RunDirector.Phase.FIGHT
            && renderer.iceWallButtonJustPressed();
        boolean mouseAttack = run.phase == RunDirector.Phase.FIGHT
            && renderer.mouseAttackHeld();
        if (mouseAttack && renderer.mouseAttackJustPressed())
            player.lockAttackTarget(combat, renderer.mouseWorldX(), renderer.mouseWorldY());
        handleMenuClicks();
        if (run.update(dt, player, combat, touchSkill, touchIceWall, mouseAttack)) newRun();
    }

    private void handleMenuClicks() {
        switch (run.phase) {
            case UPGRADE:
                int upgrade = renderer.upgradeChoiceJustPressed();
                if (upgrade >= 0) run.chooseUpgrade(upgrade, player, combat);
                break;
            case ENTRANCE:
                if (renderer.entranceCarJustPressed()) run.openCarPrompt(player);
                break;
            case CAR_PROMPT:
                int carChoice = renderer.carChoiceJustPressed();
                if (carChoice >= 0) run.chooseCar(carChoice == 0);
                break;
            default:
                break;
        }
    }

    @Override
    public void resize(int width, int height) { renderer.resize(width, height); }

    @Override
    public void dispose() {
        renderer.dispose();
        audio.dispose();
    }
}
