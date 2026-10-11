package io.github.kirinojuju;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import java.util.Random;

/** Desktop game entry point. Each gameplay area lives in its own class. */
public class schoolLife extends ApplicationAdapter {
    private final Random random = new Random();
    private PlayerCharacter player;
    private CombatRoom combat;
    private RunDirector run;
    private GameRenderer renderer;
    private GameAudio audio;

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
    }

    @Override
    public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        audio.update(dt);
        boolean touchSkill = run.phase == RunDirector.Phase.FIGHT
            && renderer.skillButtonJustPressed();
        boolean touchIceWall = run.phase == RunDirector.Phase.FIGHT
            && renderer.iceWallButtonJustPressed();
        boolean touchLanWave = run.phase == RunDirector.Phase.FIGHT
            && renderer.lanWaveButtonJustPressed();
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
        if (run.update(dt, player, combat, touchSkill, touchIceWall,
            touchLanWave, mouseAttack)) newRun();
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
