package io.github.kirinojuju.weapon;

// File role: Defines weapon attacks and per-attack values, with shared ability registration.

import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.player.PlayerCharacter;
import java.util.ArrayList;
import java.util.List;

/** Attack implementation selected by the player's equipped weapon. */
public interface WeaponAttack {
    // Attempt an attack using this weapon's SP, cooldown and combat rules; return whether it succeeds.
    boolean attack(PlayerCharacter player, CombatRoom combat, GameAudio audio);
    // Register an ability that modifies this weapon's future attacks.
    void addAbility(WeaponAbility ability);

    /** Values for one attack; abilities change this copy without changing base stats. */
    public static final class WeaponAttackSpec {
        public float damage, interval, spCost, reach, halfAngle, projectileSpeed;
        public boolean slow, tether, poison, splash, beam;

        // Create editable damage, interval and SP values for one attack.
        public WeaponAttackSpec(float damage, float interval, float spCost) {
            this.damage = damage;
            this.interval = interval;
            this.spCost = spCost;
        }
    }
}

/** Common ability registration for both weapons. */
abstract class AbstractWeaponAttack implements WeaponAttack {
    private final List<WeaponAbility> abilities = new ArrayList<WeaponAbility>();

    // Register an ability that modifies this weapon's future attacks.
    @Override
    public final void addAbility(WeaponAbility ability) {
        if (ability == null) throw new IllegalArgumentException("ability cannot be null");
        abilities.add(ability);
    }

    // Apply registered abilities to this attack's values before it is executed.
    protected final void modify(PlayerCharacter player, WeaponAttackSpec attack) {
        for (WeaponAbility ability : abilities) ability.modify(player, attack);
    }

    // Notify registered abilities after an attack succeeds.
    protected final void afterAttack(PlayerCharacter player, CombatRoom combat) {
        for (WeaponAbility ability : abilities) ability.afterAttack(player, combat);
    }
}
