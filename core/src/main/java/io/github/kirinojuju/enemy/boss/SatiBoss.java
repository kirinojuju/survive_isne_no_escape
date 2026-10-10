package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;
import java.util.Random;

public class SatiBoss extends Enemy {

    public static final float SATI_CHARGE_DURATION = 1.05f;

    public SatiBoss(RunPlan plan, Random random) {
        super(GameSpace.RIGHT - 60,
            GameSpace.WALK_TOP - 75,
            36,
            (190 + plan.stage() * 22) * plan.healthScale(),
            72 * plan.spScale(),
            90 * plan.speedScale(),
            12 * plan.damageScale(),
            0);

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

    public boolean updateSkill(float dt, PlayerCharacter player, RunPlan plan,
                        Array<CombatRoom.Shot> shots, GameAudio audio) {
        if (satiCharge > 0) {
            satiCharge = Math.max(0, satiCharge - dt);

            if (satiCharge == 0) {
                float x = satiElementX(this);
                float y = satiElementY(this);
                float dx = satiAimX - x;
                float dy = satiAimY - y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < .001f) {
                    dx = 0;
                    dy = -1;
                    distance = 1;
                }

                float speed = 500 * plan.speedScale();
                CombatRoom.Shot spear = new CombatRoom.Shot(
                    x, y,
                    dx / distance * speed,
                    dy / distance * speed,
                    36, 22 * plan.damageScale(), 3f, false
                );
                spear.satiSpear = true;
                shots.add(spear);
            }
            return true;
        }

        satiCooldown -= dt;
        if (satiCooldown > 0) return false;

        satiCooldown = Math.max(3.2f, 5.4f / plan.speedScale());
        satiCharge = SATI_CHARGE_DURATION;
        satiAimX = player.x;
        satiAimY = player.y;

        if (audio != null) audio.cppTransform();
        return true;
    }

    static float satiElementX(SatiBoss boss) {
        float side = boss.x > GameSpace.WIDTH / 2 ? -1 : 1;
        return MathUtils.clamp(boss.x + side * 72,
            GameSpace.LEFT + 35, GameSpace.RIGHT - 35);
    }


    static float satiElementY(SatiBoss boss) {
        return MathUtils.clamp(boss.y + 25,
            GameSpace.BOTTOM + 35, GameSpace.WALK_TOP - 35);
    }

}
