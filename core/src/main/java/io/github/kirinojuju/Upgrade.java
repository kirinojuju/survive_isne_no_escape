package io.github.kirinojuju;

/** One permanent choice earned after defeating an Ajarn. */
enum Upgrade {
    LAN_DAMAGE("LAN DAMAGE +7", "The cable hits harder."),
    NOTEBOOK_DAMAGE("NOTEBOOK DAMAGE +6", "Thrown notebooks hit harder."),
    ATTACK_SPEED("ATTACK SPEED +18%", "Both weapons attack faster."),
    MOVE_SPEED("MOVE SPEED +20", "Walk faster across the classroom."),
    POWER_BALL_DAMAGE("POWERBALL DAMAGE +10", "The skill hits and bursts harder."),
    LAN_SLOW("LAN: SLOW", "Cable hits slow monsters for 1.5 seconds."),
    LAN_AOE("LAN: WIDE SWEEP", "Longer cable and a wider attack arc."),
    NOTEBOOK_POISON("NOTEBOOK: POISON", "Hits deal damage over 3 seconds."),
    NOTEBOOK_SPLASH("NOTEBOOK: SPLASH", "Hits burst into a small area attack.");

    final String title, detail;

    Upgrade(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    boolean available(PlayerCharacter player) {
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

    void apply(PlayerCharacter player) {
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
