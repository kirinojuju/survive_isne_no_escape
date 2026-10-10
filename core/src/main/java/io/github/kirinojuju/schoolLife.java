package io.github.kirinojuju;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.render.GameRenderer;
import java.util.Random;

/** Desktop game entry point. Each gameplay area lives in its own class. */
public class schoolLife extends ApplicationAdapter {
    public enum TestMode { NONE, FINAL_EXAM, OBSTACLES }
    private final Random random = new Random();
    private final TestMode testMode;
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
    }

    @Override
    public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        audio.update(dt);
        boolean touchSkill = run.phase == RunDirector.Phase.FIGHT
            && renderer.skillButtonJustPressed();
        boolean mouseAttack = run.phase == RunDirector.Phase.FIGHT
            && renderer.mouseAttackHeld();
        if (mouseAttack && renderer.mouseAttackJustPressed())
            player.lockAttackTarget(combat, renderer.mouseWorldX(), renderer.mouseWorldY());
        if (run.phase == RunDirector.Phase.UPGRADE) {
            int choice = renderer.upgradeChoiceJustPressed();
            if (choice >= 0) run.chooseUpgrade(choice, player, combat);
        }
        if (run.phase == RunDirector.Phase.ENTRANCE
            && renderer.entranceCarJustPressed()) run.openCarPrompt(player);
        else if (run.phase == RunDirector.Phase.CAR_PROMPT) {
            int choice = renderer.carChoiceJustPressed();
            if (choice >= 0) run.chooseCar(choice == 0);
        }
        if (run.update(dt, player, combat, touchSkill, mouseAttack)) newRun();
        renderer.draw(run, player, combat);
    }

    @Override
    public void resize(int width, int height) { renderer.resize(width, height); }

    @Override
    public void dispose() {
        renderer.dispose();
        audio.dispose();
    }
}
