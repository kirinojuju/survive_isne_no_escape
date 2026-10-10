package io.github.kirinojuju.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/** Short game cues loaded from the team's converted sound effects. */
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
    private float hitCooldown;

    private static Sound load(String name) {
        if (!Gdx.files.internal(ROOT + name).exists()) return null;
        return Gdx.audio.newSound(Gdx.files.internal(ROOT + name));
    }

    public void update(float dt) { hitCooldown = Math.max(0, hitCooldown - dt); }
    public void click() { play(click, .35f); }
    public void lanAttack() { play(lanAttack, .42f); }
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
    }
}
