package io.github.kirinojuju.weapon;

// File role: Extension point for abilities that change an attack or react after it succeeds.

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.weapon.WeaponAttack.WeaponAttackSpec;

/** A weapon-specific upgrade can alter an attack and react after it succeeds. */
public interface WeaponAbility {
    // Change the current attack's values or effect flags before the weapon uses them.
    void modify(PlayerCharacter player, WeaponAttackSpec attack);

    // Optional callback for extra effects after a successful weapon attack.
    default void afterAttack(PlayerCharacter player, CombatRoom combat) { }
}
