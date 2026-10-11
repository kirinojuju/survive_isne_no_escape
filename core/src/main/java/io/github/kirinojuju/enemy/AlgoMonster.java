package io.github.kirinojuju.enemy;

// File role: Ordinary Algorithm monster data; uses standard combat behavior and Algorithm artwork.

import io.github.kirinojuju.combat.CombatRoom.Enemy;

public class AlgoMonster extends Enemy {
    // Create AlgoMonster with its starting stats and state for this encounter.
    public AlgoMonster(float x, float y, float radius, float hp, float maxSp,
                float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }
}
