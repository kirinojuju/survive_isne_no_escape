package io.github.kirinojuju.player;

// File role: Lists player upgrades, their availability rules and stat or attack changes.

/** One permanent choice earned after defeating an Ajarn. */
public enum Upgrade {
    LAN_DAMAGE("LAN DAMAGE +7", "The cable hits harder."),
    NOTEBOOK_DAMAGE("NOTEBOOK DAMAGE +6", "Thrown notebooks hit harder."),
    ATTACK_SPEED("ATTACK SPEED +18%", "Both weapons attack faster."),
    MOVE_SPEED("MOVE SPEED +20", "Walk faster across the classroom."),
    POWER_BALL_DAMAGE("POWERBALL DAMAGE +10", "The skill hits and bursts harder."),
    LAN_SLOW("LAN: SLOW", "Cable hits slow monsters for 1.5 seconds."),
    LAN_AOE("LAN: WIDE SWEEP", "Longer cable and a wider attack arc."),
    NOTEBOOK_POISON("NOTEBOOK: POISON", "Hits deal damage over 3 seconds."),
    NOTEBOOK_SPLASH("NOTEBOOK: SPLASH", "Hits burst into a small area attack.");

    public final String title, detail;

    // Create Upgrade with its starting stats and state for this encounter.
    Upgrade(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    // Check whether this upgrade can currently be offered to the player.
    public boolean available(PlayerCharacter player) {
        switch (this) {
            case LAN_SLOW: return !player.lanSlow;
            case LAN_AOE: return !player.lanAoE;
            case NOTEBOOK_POISON: return !player.notebookPoison;
            case NOTEBOOK_SPLASH: return !player.notebookSplash;
            case ATTACK_SPEED: return player.attackSpeedBonus < 1.8f;
            case MOVE_SPEED: return player.speedBonus < 160;
            default: return true;
        }
    }

    // Apply this upgrade's stat increase or weapon effect to the player.
    public void apply(PlayerCharacter player) {
        switch (this) {
            case LAN_DAMAGE: player.physicalBonus += 7; break;
            case NOTEBOOK_DAMAGE: player.notebookBonus += 6; break;
            case ATTACK_SPEED:
                player.attackSpeedBonus = Math.min(1.8f, player.attackSpeedBonus + .18f);
                break;
            case MOVE_SPEED: player.speedBonus = Math.min(160, player.speedBonus + 20); break;
            case POWER_BALL_DAMAGE: player.skillDamageBonus += 10; break;
            case LAN_SLOW: player.lanSlow = true; break;
            case LAN_AOE: player.lanAoE = true; break;
            case NOTEBOOK_POISON: player.notebookPoison = true; break;
            case NOTEBOOK_SPLASH: player.notebookSplash = true; break;
        }
    }
}
