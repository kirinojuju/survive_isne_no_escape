package io.github.kirinojuju.audio;

// File role: Loads and plays game sounds; shared by combat, weapons and room transitions.

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

    // Load a sound from the sound-effects folder, or return null if it is missing.
    private static Sound load(String name) {
        if (!Gdx.files.internal(ROOT + name).exists()) return null;
        return Gdx.audio.newSound(Gdx.files.internal(ROOT + name));
    }

    // Reduce the short hit-sound cooldown so repeated damage does not flood the audio.
    public void update(float dt) { hitCooldown = Math.max(0, hitCooldown - dt); }
    // Play the menu selection sound.
    public void click() { play(click, .35f); }
    // Play the LAN weapon attack sound.
    public void lanAttack() { play(lanAttack, .42f); }
    // Play the Notebook firing sound.
    public void notebookAttack() { play(click, .18f); }
    // Restart the weapon switch sound.
    public void weaponSwitch() { restart(weaponSwitch, .42f); }
    // Restart the player's skill casting sound.
    public void skillCast() { restart(skillCast, .38f); }
    // Restart the player's skill impact sound.
    public void skillImpact() { restart(skillImpact, .48f); }
    // Restart the sound for Sati's element transformation.
    public void cppTransform() { restart(cppTransform, .52f); }
    // Play the room or stage transition sound.
    public void nextStage() { play(nextStage, .50f); }
    // Play Dome's lightning charging sound.
    public void domeLightningCast() { play(domeLightningCast, .45f); }
    // Play Dome's lightning impact sound.
    public void domeLightningStrike() { play(domeLightningStrike, .60f); }
    // Play Dome's laser charging sound.
    public void domeLaserCast() { play(domeLaserCast, .45f); }
    // Play Dome's laser firing sound.
    public void domeLaserFire() { play(domeLaserFire, .60f); }

    // Play the HP or armor hit sound, with a short cooldown between hits.
    public void hit(boolean armor) {
        if (hitCooldown > 0) return;
        play(armor ? armorHit : hit, .35f);
        hitCooldown = .06f;
    }

    // Play an available sound at the requested volume.
    private static void play(Sound sound, float volume) {
        if (sound != null) sound.play(volume);
    }

    // Stop an available sound before replaying it so repeated casts do not overlap.
    private static void restart(Sound sound, float volume) {
        if (sound != null) {
            sound.stop();
            sound.play(volume);
        }
    }

    // Release loaded sound resources, safely skipping missing sounds.
    private static void dispose(Sound sound) {
        if (sound != null) sound.dispose();
    }

    // Release loaded sound resources, safely skipping missing sounds.
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
