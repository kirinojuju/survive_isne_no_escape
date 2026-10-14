package io.github.kirinojuju.weapon;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.weapon.WeaponAttack.WeaponAttackSpec;

/** A weapon-specific upgrade can alter an attack and react after it succeeds. */
public interface WeaponAbility {
    void modify(PlayerCharacter player, WeaponAttackSpec attack);

    default void afterAttack(PlayerCharacter player, CombatRoom combat) { }
}
