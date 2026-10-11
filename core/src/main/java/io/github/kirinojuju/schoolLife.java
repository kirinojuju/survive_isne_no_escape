package io.github.kirinojuju;

// File role: Game entry point: creates the systems, updates each frame and draws the result.

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

    // Choose whether the game starts normally or through a desktop test mode.
    public schoolLife() { this(TestMode.NONE); }

    // Choose whether the game starts normally or through a desktop test mode.
    public schoolLife(TestMode testMode) { this.testMode = testMode; }

    // Create rendering and audio resources, then initialize a fresh run.
    @Override
    public void create() {
        renderer = new GameRenderer();
        audio = new GameAudio();
        newRun();
    }

    // Create a new player, run director and combat room and connect their audio.
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

    // Advance the game, route pointer input and draw the current frame.
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

    // Update the renderer's viewport when the window size changes.
    @Override
    public void resize(int width, int height) { renderer.resize(width, height); }

    // Release graphics and audio resources when the application closes.
    @Override
    public void dispose() {
        renderer.dispose();
        audio.dispose();
    }
}
