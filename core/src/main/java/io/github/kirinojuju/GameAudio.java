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
        return Gdx.audio.newSound(Gdx.files.internal(ROOT + name));
    }

    void update(float dt) { hitCooldown = Math.max(0, hitCooldown - dt); }
    void click() { click.play(.35f); }
    void lanAttack() { lanAttack.play(.42f); }
    void notebookAttack() { click.play(.18f); }
    void weaponSwitch() { weaponSwitch.stop(); weaponSwitch.play(.42f); }
    void skillCast() { skillCast.stop(); skillCast.play(.38f); }
    void skillImpact() { skillImpact.stop(); skillImpact.play(.48f); }
    void cppTransform() { cppTransform.stop(); cppTransform.play(.52f); }
    void nextStage() { nextStage.play(.50f); }

    void hit(boolean armor) {
        if (hitCooldown > 0) return;
        (armor ? armorHit : hit).play(.35f);
        hitCooldown = .06f;
    }

    void dispose() {
        click.dispose();
        hit.dispose();
        armorHit.dispose();
        lanAttack.dispose();
        nextStage.dispose();
        weaponSwitch.dispose();
        skillCast.dispose();
        skillImpact.dispose();
        cppTransform.dispose();
    }
}
