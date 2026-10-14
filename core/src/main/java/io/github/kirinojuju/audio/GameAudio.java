package io.github.kirinojuju.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import io.github.kirinojuju.flow.RunDirector.Phase;

/** Looping background music, screen cues and immediate sword effects. */
public final class GameAudio {
    private static final String ROOT = "characters/soundfx/";
    private final Sound click = load("click_sound.ogg");
    private final Sound hit = load("hit_sound.ogg");
    private final Sound armorHit = load("hit_sound2.ogg");
    private final Sound lanAttack = load("lan_attack_sound.ogg");
    private final Sound nextStage = load("next_stage_level_sound.ogg");
    private final Sound weaponSwitch = load("skill_cast_change_sound.ogg");
    private final Sound skillCast = load("skill_cast_quick_change_sound.ogg");
    private final Sound skillImpact = load("skill_impact_hit_sound.ogg");
    private final Sound cppTransform = load("boss_fx/cpp_transform_sound.ogg");
    private final Sound domeLightningCast = load("boss_fx/Dome/lighting_efx_cast_sound.ogg");
    private final Sound domeLightningStrike = load("boss_fx/Dome/lighting_efx_hit_sound.ogg");
    private final Sound domeLaserCast = load("boss_fx/Dome/laser_efx_cast_sound.ogg");
    private final Sound domeLaserFire = load("boss_fx/Dome/laser_efx_hit_sound.ogg");
    private final Sound[] swordWaves = {loadSword("getsuga1.mp3"), loadSword("getsuga2.mp3")};
    private final Music background = loadBackground();
    private final Sound gameOver = loadSound("audio/game_over.ogg");
    private Phase previousPhase;
    private Sound playingSwordWave;
    private int swordWaveIndex;
    private float hitCooldown;

    private static Sound load(String name) {
        return loadSound(ROOT + name);
    }

    private static Sound loadSword(String name) {
        return loadSound("weapons/sword_power/" + name);
    }

    private static Sound loadSound(String path) {
        return Gdx.files.internal(path).exists() ? Gdx.audio.newSound(Gdx.files.internal(path)) : null;
    }

    private static Music loadBackground() {
        String path = "audio/BGM_sound.ogg";
        if (!Gdx.files.internal(path).exists()) return null;
        Music music = Gdx.audio.newMusic(Gdx.files.internal(path));
        music.setLooping(true);
        music.setVolume(.22f);
        return music;
    }

    /** React once per screen change: continuous BGM, quiet cutscenes and a single defeat cue. */
    public void updatePhase(Phase phase) {
        if (phase == previousPhase) return;
        previousPhase = phase;
        if (phase == Phase.GAME_OVER) {
            if (background != null) background.stop();
            restart(gameOver, .55f);
        } else {
            if (gameOver != null) gameOver.stop();
            if (background == null) return;
            if (phase == Phase.INTRO || phase == Phase.CUTSCENE || phase == Phase.CAR_ENDING) background.pause();
            else if (!background.isPlaying()) background.play();
        }
    }

    public void update(float dt) { hitCooldown = Math.max(0, hitCooldown - dt); }
    public void click() { play(click, .35f); }
    public void lanAttack() { play(lanAttack, .42f); }
    public void lanWave() {
        Sound cue = swordWaves[swordWaveIndex];
        swordWaveIndex = 1 - swordWaveIndex;
        if (cue == null) cue = swordWaves[swordWaveIndex];
        if (cue == null) { lanAttack(); return; }
        if (playingSwordWave != null) playingSwordWave.stop();
        play(cue, .45f);
        playingSwordWave = cue;
    }
    public void notebookAttack() { play(click, .18f); }
    public void weaponSwitch() { restart(weaponSwitch, .42f); }
    public void skillCast() { restart(skillCast, .38f); }
    public void skillImpact() { restart(skillImpact, .48f); }
    public void cppTransform() { restart(cppTransform, .52f); }
    public void nextStage() { play(nextStage, .50f); }
    public void domeLightningCast() { play(domeLightningCast, .45f); }
    public void domeLightningStrike() { play(domeLightningStrike, .60f); }
    public void domeLaserCast() { play(domeLaserCast, .45f); }
    public void domeLaserFire() { play(domeLaserFire, .60f); }

    public void hit(boolean armor) {
        if (hitCooldown > 0) return;
        play(armor ? armorHit : hit, .35f);
        hitCooldown = .06f;
    }

    private static void play(Sound sound, float volume) {
        if (sound != null) sound.play(volume);
    }

    private static void restart(Sound sound, float volume) {
        if (sound != null) {
            sound.stop();
            sound.play(volume);
        }
    }

    private static void dispose(Sound sound) {
        if (sound != null) sound.dispose();
    }

    public void dispose() {
        dispose(click);
        dispose(hit);
        dispose(armorHit);
        dispose(lanAttack);
        dispose(nextStage);
        dispose(weaponSwitch);
        dispose(skillCast);
        dispose(skillImpact);
        dispose(cppTransform);
        dispose(domeLightningCast);
        dispose(domeLightningStrike);
        dispose(domeLaserCast);
        dispose(domeLaserFire);
        for (Sound cue : swordWaves) dispose(cue);
        dispose(gameOver);
        if (background != null) background.dispose();
    }
}
