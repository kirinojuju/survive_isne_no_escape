package io.github.kirinojuju.enemy;

import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.ex.ExBehavior.ExEnvironment;
import io.github.kirinojuju.enemy.ex.LiquidCatBoss;
import io.github.kirinojuju.enemy.boss.KenBoss;
import io.github.kirinojuju.enemy.boss.MBoss;
import io.github.kirinojuju.enemy.boss.AeBoss;
import io.github.kirinojuju.enemy.boss.SatiBoss;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.world.GameSpace;
import java.util.Random;

/** Creates the enemies for a room; combat and AI do not decide spawn statistics. */
public final class EnemyFactory {
    private static final int[] BOSS_MINION_KINDS = {
        CombatRoom.BASIC, CombatRoom.RANGED, CombatRoom.WAVE,
        CombatRoom.SHIELDED, CombatRoom.BINDER, CombatRoom.WARPER
    };
    private static final float[][] BOSS_MINION_POSITIONS = {
        {270, 180}, {470, 460}, {780, 220}, {880, 460}, {1080, 180}
    };
    private final Random random;

    public EnemyFactory(Random random) { this.random = random; }

    public void populate(CombatRoom combat, RunDirector.Room room, RunPlan plan, int exId) {
        switch (room) {
            case MOBS:
                populateMonsters(combat, plan);
                break;
            case BOSS:
                populateBossRoom(combat, plan);
                break;
            case FINAL_EXAM:
                int[] bosses = {RunPlan.AE_ID, RunPlan.FINAL_EXAM_FAR_ID, RunPlan.DOME_ID};
                for (int slot = 0; slot < bosses.length; slot++)
                    combat.enemies.add(createFinalExamBoss(bosses[slot], slot, plan));
                break;
            case EX:
                combat.enemies.add(createEx(combat, plan, exId));
                break;
        }
    }

    private void populateMonsters(CombatRoom combat, RunPlan plan) {
        int count = 4 + plan.stage() + Math.min(2, plan.loop() - 1);
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.PI2 * i / count + random.nextFloat() * .25f;
            float x = GameSpace.WIDTH / 2 + MathUtils.cos(angle) * (330 + random.nextInt(90));
            float y = GameSpace.HEIGHT / 2 + MathUtils.sin(angle) * (95 + random.nextInt(50));
            combat.enemies.add(createMonster(x, y, mobKind(i, plan), plan, i == 0));
        }
    }

    /** Fewer helpers in early loops; introduce their types alongside normal rooms. */
    private void populateBossRoom(CombatRoom combat, RunPlan plan) {
        CombatRoom.Enemy boss = createBoss(plan);
        combat.enemies.add(boss);
        int count = plan.loop() == 1 ? 2 : plan.loop() == 2 ? 3 : BOSS_MINION_POSITIONS.length;
        int available = plan.loop() == 1 ? Math.min(BOSS_MINION_KINDS.length, 2 + plan.stage())
            : BOSS_MINION_KINDS.length;
        for (int i = 0; i < count; i++) {
            int kind = BOSS_MINION_KINDS[(boss.kind + i) % available];
            float[] position = BOSS_MINION_POSITIONS[i];
            combat.enemies.add(createMonster(position[0], position[1], kind, plan));
        }
    }

    private int mobKind(int index, RunPlan plan) {
        if (index == 0) return CombatRoom.RANGED;
        if (index == 1 && plan.stage() >= 1) return CombatRoom.WAVE;
        if (index == 2 && plan.stage() >= 2) return CombatRoom.SHIELDED;
        if (index == 3 && plan.stage() >= 2) return CombatRoom.BINDER;
        if (index == 4 && plan.stage() >= 3) return CombatRoom.WARPER;
        float extraChance = Math.min(.50f, .12f * (plan.loop() - 1));
        if (plan.loop() >= 2 && random.nextFloat() < extraChance) {
            int[] specials = {CombatRoom.RANGED, CombatRoom.WAVE,
                CombatRoom.SHIELDED, CombatRoom.BINDER, CombatRoom.WARPER};
            return specials[random.nextInt(specials.length)];
        }
        return CombatRoom.BASIC;
    }

    public CombatRoom.Enemy createMonster(float x, float y, int kind, RunPlan plan) {
        return createMonster(x, y, kind, plan, false);
    }

    private CombatRoom.Enemy createMonster(float x, float y, int kind, RunPlan plan, boolean algorithm) {
        float hp = 42 + plan.stage() * 5;
        float sp = 37;
        float speed = 80;
        float damage = 7;
        // Keep each monster's base stats together, before applying loop scaling.
        switch (kind) {
            case CombatRoom.BINDER:
                hp = 58;
                sp = 48;
                speed = 68;
                break;
            case CombatRoom.WARPER:
                hp = 56;
                sp = 42;
                speed = 92;
                damage = 10;
                break;
            case CombatRoom.WAVE:
                hp = 48;
                sp = 43;
                speed = 65;
                damage = 9;
                break;
            case CombatRoom.RANGED:
                hp = 46;
                sp = 36;
                break;
            case CombatRoom.SHIELDED:
                hp = 52;
                speed = 62;
                break;
            default:
                break;
        }
        float spawnX = MathUtils.clamp(x, GameSpace.LEFT + 17, GameSpace.RIGHT - 17);
        float spawnY = MathUtils.clamp(y, GameSpace.BOTTOM + 17, GameSpace.WALK_TOP - 17);
        CombatRoom.Enemy enemy = algorithm
            ? new AlgoMonster(spawnX, spawnY, 17, hp * plan.healthScale(), sp * plan.spScale(),
                speed * plan.speedScale(), damage * plan.damageScale(), kind)
            : new NetworkMonster(spawnX, spawnY, 17, hp * plan.healthScale(), sp * plan.spScale(),
                speed * plan.speedScale(), damage * plan.damageScale(), kind);
        if (kind == CombatRoom.SHIELDED) {
            enemy.maxArmor = (36 + plan.stage() * 7) * plan.healthScale();
            enemy.armor = enemy.maxArmor;
        }
        return enemy;
    }

    private CombatRoom.Enemy createBoss(RunPlan plan) {
        int id = plan.bossId();
        int family = id % 4;
        CombatRoom.Enemy boss = new CombatRoom.Enemy(
            GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            (190 + plan.stage() * 22 + (id % 3) * 12) * plan.healthScale(),
            72 * plan.spScale(), (90 + (id % 4) * 9) * plan.speedScale(),
            (12 + (id % 3) * 2) * plan.damageScale(), id);
        if (id == RunPlan.KEN_ID) boss = new KenBoss(plan, random);
        else if (id == RunPlan.M_ID) boss = new MBoss(plan);
        else if (id == RunPlan.SATI_ID) boss = new SatiBoss(plan, random);
        else if (id == RunPlan.AE_ID) boss = new AeBoss(plan);
        boss.reflector = family == 0 || family == 3;
        boss.warper = family == 1;
        boss.artillery = family == 2;
        boss.waveCaster = family == 3;
        if (boss instanceof KenBoss) boss.reflector = false;
        boss.warpCooldown = plan.loop() == 1 ? 4f : 2.8f;
        if (id == RunPlan.AE_ID) boss.warpCooldown = 6f;
        if (id == RunPlan.SATI_ID) boss.satiCooldown = 3.4f;
        if (id == RunPlan.KEN_ID) boss.talgoCooldown = 3.4f;
        if (plan.loop() >= 3) {
            if (random.nextBoolean()) boss.reflector = true;
            else boss.warper = true;
        }
        if (plan.loop() >= 5 && random.nextBoolean()) boss.waveCaster = true;
        if (id == RunPlan.DOME_ID) {
            boss.warper = false;
            boss.waveCaster = false;
        }
        if (boss instanceof KenBoss || boss instanceof SatiBoss) {
            boss.summoner = true;
            boss.summonTimer = plan.loop() == 1 ? 5.2f : 3.8f;
        }
        if (boss instanceof KenBoss) {
            boss.warper = false;
            boss.talgoCooldown = 0;
        }
        if (boss.reflector) {
            boss.maxArmor = ((plan.loop() == 1 ? 75 : 110)
                + plan.stage() * 22) * plan.healthScale();
            boss.armor = boss.maxArmor;
        }
        return boss;
    }

    private CombatRoom.Enemy createEx(CombatRoom combat, RunPlan plan, int exId) {
        CombatRoom.Enemy ex = new CombatRoom.Enemy(
            GameSpace.RIGHT - 75, GameSpace.WALK_TOP - 75, 34,
            380 * plan.healthScale(), 88 * plan.spScale(),
            120 * plan.speedScale(), 14 * plan.damageScale(), CombatRoom.LIQUID_CAT + exId);
        if (exId == 0) ex = new LiquidCatBoss(plan);
        ex.skillTimer = 1.8f;
        if (ex.kind == CombatRoom.LIQUID_CAT) ex.exStateTime = 3.5f;
        else if (ex.kind == CombatRoom.ALLERGIC_VACUUM) {
            ex.exObjectTimer = 1.2f;
            for (int i = 0; i < 6; i++) ExEnvironment.spawnDust(combat, ex);
        } else {
            ex.maxArmor = 150 * plan.healthScale();
            ex.armor = ex.maxArmor;
            ex.exObjectTimer = 4f;
            for (int i = 0; i < 3; i++) ExEnvironment.spawnNote(combat);
        }
        return ex;
    }

    private CombatRoom.Enemy createFinalExamBoss(int id, int slot, RunPlan plan) {
        CombatRoom.Enemy boss = new CombatRoom.Enemy(
            GameSpace.serverBossX(slot), GameSpace.SERVER_BOSS_Y, 36,
            170 * plan.healthScale(), 72 * plan.spScale(), 0,
            10 * plan.damageScale(), id);
        boss.anchored = true;
        boss.artillery = id == RunPlan.FINAL_EXAM_FAR_ID;
        boss.skillTimer = .65f + slot * .55f;
        boss.sp = boss.maxSp;
        return boss;
    }
}
