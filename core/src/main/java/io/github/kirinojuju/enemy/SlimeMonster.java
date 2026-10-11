package io.github.kirinojuju.enemy;

// File role: Ranged, wave and binding monster data; uses slime artwork while keeping each kind's attacks.

import io.github.kirinojuju.combat.CombatRoom.Enemy;

/** Slime artwork for ranged, wave and binding monsters; each kind retains its own attacks. */
public final class SlimeMonster extends Enemy {
    // Create SlimeMonster with its starting stats and state for this encounter.
    public SlimeMonster(float x, float y, float radius, float hp, float maxSp,
                        float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }
}
