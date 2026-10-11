package io.github.kirinojuju.enemy.boss;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.world.GameSpace;
import java.util.Random;

public class SatiBoss extends Enemy {

    public static final float SATI_CHARGE_DURATION = SatiBossBehavior.CHARGE_DURATION;
    public static final float SPEAR_SPEED = 500f;
    public static final float SPEAR_RADIUS = 36f;

    public SatiBoss(RunPlan plan, Random random) {
        super(GameSpace.RIGHT - 60,
            GameSpace.WALK_TOP - 75,
            36,
            (190 + plan.stage() * 22) * plan.healthScale(),
            72 * plan.spScale(),
            90 * plan.speedScale(),
            12 * plan.damageScale(),
            RunPlan.SATI_ID);

        summoner = true;
        reflector = true;
        satiCooldown = 3.4f;

        summonTimer = plan.loop() == 1 ? 5.2f : 3.8f;
        warpCooldown = plan.loop() == 1 ? 4f : 2.8f;

        if (plan.loop() >= 3) {
            int bonus = random.nextInt(3);
            if (bonus == 0) summoner = true;
            else if (bonus == 1) reflector = true;
            else warper = true;
        }

        if (plan.loop() >= 5 && random.nextBoolean()) {
            waveCaster = true;
        }

        if (reflector) {
            maxArmor = ((plan.loop() == 1 ? 75 : 110)
                + plan.stage() * 22) * plan.healthScale();
            armor = maxArmor;
        }
    }
}
