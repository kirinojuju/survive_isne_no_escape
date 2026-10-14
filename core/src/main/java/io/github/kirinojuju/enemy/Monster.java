package io.github.kirinojuju.enemy;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;

/** One monster model; combat role selects its appearance and injected AI. */
public class Monster extends Enemy {
    public enum Appearance { ALGORITHM, NETWORK, PROGRAM, SLIME }

    private final Appearance appearance;

    public Monster(float x, float y, float radius, float hp, float maxSp,
                   float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
        switch (kind) {
            case CombatRoom.BASIC: appearance = Appearance.ALGORITHM; break;
            case CombatRoom.SHIELDED: appearance = Appearance.NETWORK; break;
            case CombatRoom.WARPER: appearance = Appearance.PROGRAM; break;
            default: appearance = Appearance.SLIME; break;
        }
    }

    public Appearance appearance() { return appearance; }

    @Override
    protected float spRecovery() {
        return kind == CombatRoom.RANGED || kind == CombatRoom.WAVE ? 20 : 11;
    }
    @Override
    public void updateBehavior(CombatRoom room, float dt, PlayerCharacter player, RunPlan plan) {
        updateStandard(room, dt, player, plan);
    }
}
