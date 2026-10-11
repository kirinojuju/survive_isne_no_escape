package io.github.kirinojuju.enemy;

// File role: Network shield monster data; armor is assigned by EnemyFactory.

import io.github.kirinojuju.combat.CombatRoom.Enemy;

public class NetworkMonster extends Enemy {
    // Create NetworkMonster with its starting stats and state for this encounter.
    public NetworkMonster(float x, float y, float radius, float hp, float maxSp,
                float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }
}
