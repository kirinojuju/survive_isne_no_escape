package io.github.kirinojuju.weapon;

// File role: Runs the LAN melee attack, including SP cost, arc damage and attached abilities.

import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.weapon.WeaponAttack.WeaponAttackSpec;

/** A free close-range arc attack; abilities may modify its one-attack values. */
public final class LanWeaponAttack extends AbstractWeaponAttack {
    // Spend SP and apply a LAN melee arc with enabled slow, area or tether upgrades.
    @Override
    public boolean attack(PlayerCharacter player, CombatRoom combat, GameAudio audio) {
        WeaponAttackSpec spec = new WeaponAttackSpec(
            27 + player.physicalBonus, PlayerCharacter.LAN_ATTACK_INTERVAL, 0);
        spec.reach = PlayerCharacter.LAN_REACH + (player.lanAoE ? 35 : 0);
        spec.halfAngle = player.lanAoE ? 85 : 65;
        spec.slow = player.lanSlow;
        spec.tether = player.lanTether;
        modify(player, spec);
        if (player.sp < spec.spCost) return false;
        player.sp -= spec.spCost;
        player.attackTimer = spec.interval / (1 + player.attackSpeedBonus);
        player.swingTimer = PlayerCharacter.LAN_SWING_DURATION;
        player.swingFaceX = player.faceX;
        player.swingFaceY = player.faceY;
        if (audio != null) audio.lanAttack();
        combat.damageArc(player.x, player.y, player.swingFaceX, player.swingFaceY,
            spec.reach, spec.halfAngle, spec.damage, spec.slow, spec.tether);
        afterAttack(player, combat);
        return true;
    }
}
