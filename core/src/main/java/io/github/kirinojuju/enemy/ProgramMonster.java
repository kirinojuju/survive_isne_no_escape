package io.github.kirinojuju.enemy;

// File role: Programming teleport monster data; its attacks are handled by WarperMonsterBehavior.

import io.github.kirinojuju.combat.CombatRoom.Enemy;

/** Programming monster artwork, with the existing monster statistics and behavior. */
public final class ProgramMonster extends Enemy {
    // Create ProgramMonster with its starting stats and state for this encounter.
    public ProgramMonster(float x, float y, float radius, float hp, float maxSp,
                          float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }
}
