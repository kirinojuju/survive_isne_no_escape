package io.github.kirinojuju.enemy;

import io.github.kirinojuju.combat.CombatRoom.Enemy;

public class NetworkMonster extends Enemy {
    public NetworkMonster(float x, float y, float radius, float hp, float maxSp,
                float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }
}
