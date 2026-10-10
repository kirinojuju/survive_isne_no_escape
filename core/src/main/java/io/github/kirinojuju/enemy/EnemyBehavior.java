package io.github.kirinojuju.enemy;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.boss.AjarnBehavior;
import io.github.kirinojuju.enemy.boss.DomeBossBehavior;
import io.github.kirinojuju.enemy.boss.SatiBossBehavior;
import io.github.kirinojuju.enemy.boss.TalgoBossBehavior;
import io.github.kirinojuju.enemy.ex.AllergicVacuumBehavior;
import io.github.kirinojuju.enemy.ex.InsecureSwordBehavior;
import io.github.kirinojuju.enemy.ex.LiquidCatBehavior;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;

/** One enemy's decisions for a combat frame. The room owns collisions and projectiles. */
public interface EnemyBehavior {
    void update(CombatRoom room, CombatRoom.Enemy enemy, float dt,
                PlayerCharacter player, RunPlan plan);

    /** Chooses behavior once when an enemy is created, instead of branching every frame. */
    public static final class EnemyBehaviors {
        private static final EnemyBehavior MONSTER = new StandardEnemyBehavior();
        private static final EnemyBehavior WARPER = new WarperMonsterBehavior();
        private static final EnemyBehavior AJARN = new AjarnBehavior();
        private static final EnemyBehavior SATI = new SatiBossBehavior();
        private static final EnemyBehavior TALGO = new TalgoBossBehavior();
        private static final EnemyBehavior DOME = new DomeBossBehavior();
        private static final EnemyBehavior CAT = new LiquidCatBehavior();
        private static final EnemyBehavior VACUUM = new AllergicVacuumBehavior();
        private static final EnemyBehavior SWORD = new InsecureSwordBehavior();

        private EnemyBehaviors() { }

        public static EnemyBehavior forKind(int kind) {
            if (kind == CombatRoom.LIQUID_CAT) return CAT;
            if (kind == CombatRoom.ALLERGIC_VACUUM) return VACUUM;
            if (kind == CombatRoom.INSECURE_SWORD) return SWORD;
            if (kind == CombatRoom.WARPER) return WARPER;
            if (kind == 0) return SATI;
            if (kind == RunPlan.TALGO_ID) return TALGO;
            if (kind == RunPlan.DOME_ID) return DOME;
            return kind >= 0 ? AJARN : MONSTER;
        }
    }
}
