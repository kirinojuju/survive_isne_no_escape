package io.github.kirinojuju.enemy.boss;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;
import java.util.Random;

public class SatiBoss extends Boss {
    public float satiCooldown, satiCharge, satiAimX, satiAimY;
    private float spearSpeed = 390, spearRadius = 24;

    public SatiBoss(float x, float y, float radius, float hp, float maxSp,
                    float speed, float damage, int kind) {
        super(x, y, radius, hp, maxSp, speed, damage, kind);
    }

    public static final float SATI_CHARGE_DURATION = SatiBoss.CHARGE_DURATION;
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
        spearSpeed = SPEAR_SPEED;
        spearRadius = SPEAR_RADIUS;
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

    public static final float CHARGE_DURATION = 1.05f;

    @Override
    protected boolean useSpecial(CombatRoom room, float dt,
                                 PlayerCharacter player, RunPlan plan) {

        if (this.satiCharge > 0) {
            this.satiCharge = Math.max(0, this.satiCharge - dt);
            if (this.satiCharge == 0) {
                float x = elementX(this);
                float y = elementY(this);
                float dx = this.satiAimX - x, dy = this.satiAimY - y;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                if (distance < .001f) { dx = 0; dy = -1; distance = 1; }
                // Keep the custom Sati's original stats and the generic enemy's cleanup stats.
                float speed = this.spearSpeed * plan.speedScale();
                float radius = this.spearRadius;
                CombatRoom.Shot spear = new CombatRoom.Shot(x, y, dx / distance * speed,
                    dy / distance * speed, radius, 22 * plan.damageScale(), 3f, false);
                spear.satiSpear = true;
                room.addEnemyShot(this, spear);
            }
            return true;
        }
        this.satiCooldown -= dt;
        if (this.satiCooldown > 0) return false;
        this.satiCooldown = Math.max(3.2f, 5.4f / plan.speedScale());
        this.satiCharge = CHARGE_DURATION;
        this.satiAimX = player.x;
        this.satiAimY = player.y;
        if (room.getAudio() != null) room.getAudio().cppTransform();
        return true;
    }

    /** Shared origin for the charging element, its warning and the released spear. */
    public static float elementX(Enemy boss) {
        float side = boss.x > GameSpace.WIDTH / 2 ? -1 : 1;
        return MathUtils.clamp(boss.x + side * 72,
            GameSpace.LEFT + 35, GameSpace.RIGHT - 35);
    }

    public static float elementY(Enemy boss) {
        return MathUtils.clamp(boss.y + 25,
            GameSpace.BOTTOM + 35, GameSpace.WALK_TOP - 35);
    }

    @Override
    protected boolean canWarp() { return super.canWarp() && satiCharge <= 0; }
}
