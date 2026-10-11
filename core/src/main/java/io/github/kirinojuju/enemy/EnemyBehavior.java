package io.github.kirinojuju.enemy;

// File role: Defines the enemy update contract and selects the shared behavior for each enemy kind.

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.boss.AjarnBehavior;
import io.github.kirinojuju.enemy.boss.AeBossBehavior;
import io.github.kirinojuju.enemy.boss.DomeBossBehavior;
import io.github.kirinojuju.enemy.boss.SatiBossBehavior;
import io.github.kirinojuju.enemy.boss.FarBossBehavior;
import io.github.kirinojuju.enemy.ex.AllergicVacuumBehavior;
import io.github.kirinojuju.enemy.ex.InsecureSwordBehavior;
import io.github.kirinojuju.enemy.ex.LiquidCatBehavior;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;

/** One enemy's decisions for a combat frame. The room owns collisions and projectiles. */
public interface EnemyBehavior {
    // Advance this enemy's movement and attacks for one frame using the room, player and run plan.
    void update(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                PlayerCharacter player, RunPlan plan);

    /** Chooses behavior once when an enemy is created, instead of branching every frame. */
    public static final class EnemyBehaviors {
        private static final EnemyBehavior MONSTER = new StandardEnemyBehavior();
        private static final EnemyBehavior WARPER = new WarperMonsterBehavior();
        private static final EnemyBehavior AJARN = new AjarnBehavior();
        private static final EnemyBehavior AE = new AeBossBehavior();
        private static final EnemyBehavior SATI = new SatiBossBehavior();
        private static final EnemyBehavior FAR = new FarBossBehavior();
        private static final EnemyBehavior DOME = new DomeBossBehavior();
        private static final EnemyBehavior CAT = new LiquidCatBehavior();
        private static final EnemyBehavior VACUUM = new AllergicVacuumBehavior();
        private static final EnemyBehavior SWORD = new InsecureSwordBehavior();

        // Prevent construction of this utility class; use its static helpers.
        private EnemyBehaviors() { }

        // Select the shared AI implementation for a monster, teacher boss or EX kind.
        public static EnemyBehavior forKind(int kind) {
            if (kind == CombatRoom.LIQUID_CAT) return CAT;
            if (kind == CombatRoom.ALLERGIC_VACUUM) return VACUUM;
            if (kind == CombatRoom.INSECURE_SWORD) return SWORD;
            if (kind == CombatRoom.WARPER) return WARPER;
            if (kind == RunPlan.SATI_ID) return SATI;
            if (kind == RunPlan.AE_ID) return AE;
            if (kind == RunPlan.FAR_ID) return FAR;
            if (kind == RunPlan.DOME_ID) return DOME;
            return kind >= 0 ? AJARN : MONSTER;
        }
    }
}
