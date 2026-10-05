package io.github.kirinojuju;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/** Short game cues loaded from the team's converted sound effects. */
final class GameAudio {
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
    private float hitCooldown;

    private static Sound load(String name) {
        if (!Gdx.files.internal(ROOT + name).exists()) return null;
        return Gdx.audio.newSound(Gdx.files.internal(ROOT + name));
    }

    void update(float dt) { hitCooldown = Math.max(0, hitCooldown - dt); }
    void click() { play(click, .35f); }
    void lanAttack() { play(lanAttack, .42f); }
    void notebookAttack() { play(click, .18f); }
    void weaponSwitch() { restart(weaponSwitch, .42f); }
    void skillCast() { restart(skillCast, .38f); }
    void skillImpact() { restart(skillImpact, .48f); }
    void cppTransform() { restart(cppTransform, .52f); }
    void nextStage() { play(nextStage, .50f); }

    void hit(boolean armor) {
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

    void dispose() {
        dispose(click);
        dispose(hit);
        dispose(armorHit);
        dispose(lanAttack);
        dispose(nextStage);
        dispose(weaponSwitch);
        dispose(skillCast);
        dispose(skillImpact);
        dispose(cppTransform);
    }
}
