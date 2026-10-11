package io.github.kirinojuju;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.enemy.AlgoMonster;
import io.github.kirinojuju.enemy.boss.KenBoss;
import io.github.kirinojuju.enemy.boss.KenBossBehavior;
import io.github.kirinojuju.enemy.boss.AeBoss;
import io.github.kirinojuju.enemy.boss.AeBossBehavior;
import io.github.kirinojuju.world.GameSpace;
import io.github.kirinojuju.enemy.boss.MBoss;
import io.github.kirinojuju.enemy.boss.MBossBehavior;
import io.github.kirinojuju.enemy.boss.SatiBoss;
import io.github.kirinojuju.enemy.ex.LiquidCatBoss;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import java.util.Random;

/** Regression checks for the user's enemy classes in the cleanup architecture. */
public final class CustomBossCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        checkFactory();
        checkKen();
        checkAe();
        checkM();
        checkSati();
        checkCat();
        System.out.println("Custom bosses verified");
    }

    private static void checkFactory() {
        boolean ken = false, m = false, sati = false;
        CombatRoom room = new CombatRoom(new Random(20));
        for (int seed = 0; seed < 40; seed++) {
            RunPlan plan = new RunPlan(new Random(seed * 7919L));
            for (int stage = 0; stage < 4; stage++) {
                room.begin(RunDirector.Room.BOSS, plan, 0);
                Enemy boss = room.enemies.first();
                check(boss.kind == plan.bossId(), "Factory must preserve the chosen boss identity");
                if (plan.bossId() == RunPlan.KEN_ID) {
                    check(boss instanceof KenBoss && boss.summoner && !boss.warper,
                        "Ken must retain its bomb/summon class without replacing Ae");
                    ken = true;
                } else if (plan.bossId() == RunPlan.M_ID) {
                    check(boss instanceof MBoss, "M's own identity must create its split class");
                    m = true;
                } else if (plan.bossId() == RunPlan.SATI_ID) {
                    check(boss instanceof SatiBoss, "Sati must retain the user's class");
                    sati = true;
                }
                plan.completeStage();
            }
        }
        check(ken && m && sati, "All custom bosses must remain reachable");
        RunPlan plan = new RunPlan(new Random(21));
        room.begin(RunDirector.Room.MOBS, plan, 0);
        check(room.enemies.first() instanceof AlgoMonster, "Algorithm monster must keep its sprite/projectile class");
        room.begin(RunDirector.Room.FINAL_EXAM, plan, 0);
        check(room.enemies.size == 3, "Cleanup final exam must retain all three anchored bosses");
        for (Enemy enemy : room.enemies) {
            check(enemy.anchored, "Final exam bosses must stay on their cabinets");
            check(!(enemy instanceof AeBoss), "Final exam Ae must retain its original behavior");
            check(!(enemy.behavior instanceof MBossBehavior),
                "Final exam must retain its anchored attacks rather than M's split behavior");
        }
        room.begin(RunDirector.Room.EX, plan, 0);
        check(room.enemies.first() instanceof LiquidCatBoss, "EX factory must retain the user's Liquid Cat class");
    }

    private static void checkKen() {
        RunPlan plan = new RunPlan(new Random(1));
        CombatRoom room = new CombatRoom(new Random(2));
        KenBoss ken = new KenBoss(plan, new Random(3));
        check(ken.behavior instanceof KenBossBehavior, "Ken must use its dedicated behavior");
        PlayerCharacter player = new PlayerCharacter();
        room.enemies.add(ken);
        ken.skillTimer = 0;
        room.update(.01f, player, plan);
        check(ken.bombs.size >= 3 && ken.bombAttackTime > 0 && ken.talgoCharge == 0,
            "Ken must use bombs rather than accidentally triggering the cleanup Talgo grid");
        KenBoss.Bomb bomb = ken.bombs.first();
        player.x = bomb.x;
        player.y = bomb.y;
        player.invulnerable = 0;
        float hp = player.hp;
        KenBossBehavior.updateBombSkill(ken, 3.01f, player, plan, false);
        check(bomb.exploded && player.hp == hp - KenBoss.BOMB_DAMAGE,
            "Ken's warned bomb must keep its original damage");
        ken.summoner = true;
        ken.summonTimer = 0;
        room.enemies.clear();
        room.enemies.add(ken);
        room.update(.01f, player, plan);
        check(room.enemies.size == 2 && room.enemies.peek().armor > 0,
            "Ken's original armored summon must coexist with the room's spawn queue");
    }

    private static void checkAe() {
        RunPlan plan = new RunPlan(new Random(11));
        CombatRoom room = new CombatRoom(new Random(12));
        AeBoss ae = new AeBoss(plan);
        PlayerCharacter player = new PlayerCharacter();
        player.x = (GameSpace.LEFT + GameSpace.RIGHT) / 2;
        player.y = (GameSpace.BOTTOM + GameSpace.WALK_TOP) / 2;
        ae.warper = false;
        ae.sp = ae.maxSp;
        float hp = player.hp;
        ae.updateBehavior(room, 2f, player, plan);
        check(ae.behavior instanceof AeBossBehavior && ae.laserPhase == AeBoss.LaserPhase.WARNING,
            "Ae must warn before starting its sweeping laser");
        check(player.hp == hp && room.shots.size == 3,
            "Laser warning must not damage the player and Ae must fire aimed bullet volleys");
        ae.updateBehavior(room, AeBossBehavior.LASER_WARNING, player, plan);
        check(ae.laserPhase == AeBoss.LaserPhase.SWEEP && ae.laserX == GameSpace.LEFT,
            "Laser must start at the left edge");
        ae.updateBehavior(room, AeBossBehavior.LASER_DURATION / 2, player, plan);
        check(player.hp < hp && Math.abs(ae.laserX - player.x) < .01f,
            "Sweeping laser must move right and hit a player in its path, even on long frames");
        float beforeStun = ae.laserX;
        ae.stunTime = 1;
        ae.updateBehavior(room, .5f, player, plan);
        check(ae.laserX == beforeStun, "Stun must pause Ae's sweep");
        ae.stunTime = 0;
        player.invulnerable = 1;
        float hpAfterHit = player.hp;
        ae.updateBehavior(room, AeBossBehavior.LASER_DURATION / 2, player, plan);
        check(ae.laserPhase == AeBoss.LaserPhase.IDLE && ae.laserX == GameSpace.RIGHT,
            "Laser must finish at the right edge and return to cooldown");
        check(player.hp == hpAfterHit, "Invulnerability must protect against the laser");
        AeBoss independent = new AeBoss(plan);
        check(independent.laserPhase == AeBoss.LaserPhase.IDLE && independent.laserX == GameSpace.LEFT,
            "Each Ae must retain its own laser state");
    }

    private static void checkM() {
        RunPlan plan = new RunPlan(new Random(4));
        CombatRoom room = new CombatRoom(new Random(5));
        MBoss m = new MBoss(plan);
        check(m.behavior instanceof MBossBehavior, "M must dispatch its dedicated behavior");
        PlayerCharacter player = new PlayerCharacter();
        room.enemies.add(m);
        m.hp = m.maxHp / 2;
        room.update(.01f, player, plan);
        check(room.enemies.size == 2 && room.enemies.peek() instanceof MBoss,
            "M must split once at half health without changing the array during iteration");
        room.update(.01f, player, plan);
        check(room.enemies.size == 2, "M and its copy must not repeatedly split");
        MBoss second = new MBoss(plan);
        room.enemies.add(second);
        second.hp = second.maxHp / 2;
        room.update(.01f, player, plan);
        check(room.enemies.size == 4, "Each M must retain its own split state");
        m.skillTimer = 0;
        room.update(.01f, player, plan);
        check(room.shots.size > 0 && room.shots.first().ignoresTables,
            "M's own projectile must use cleanup's boss-shot collision rules");
    }

    private static void checkSati() {
        RunPlan plan = new RunPlan(new Random(6));
        SatiBoss sati = new SatiBoss(plan, new Random(7));
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom room = new CombatRoom(new Random(8));
        room.enemies.add(sati);
        sati.summoner = false;
        sati.satiCooldown = 0;
        sati.sp = 0;
        room.update(.01f, player, plan);
        check(sati.satiCharge > 0, "Sati's original cast must be dispatched through the new behavior");
        room.update(1.06f, player, plan);
        CombatRoom.Shot spear = null;
        for (CombatRoom.Shot shot : room.shots) if (shot.satiSpear) spear = shot;
        check(spear != null && spear.radius == 36 && spear.ignoresTables,
            "Sati must retain the user's spear size and boss collision rules");
        float speed = (float) Math.sqrt(spear.vx * spear.vx + spear.vy * spear.vy);
        check(Math.abs(speed - 500) < .01f, "Sati must retain the user's spear speed");
    }

    private static void checkCat() {
        RunPlan plan = new RunPlan(new Random(9));
        CombatRoom room = new CombatRoom(new Random(10));
        PlayerCharacter player = new PlayerCharacter();
        room.begin(RunDirector.Room.EX, plan, 0);
        LiquidCatBoss cat = (LiquidCatBoss) room.enemies.first();
        room.update(3.6f, player, plan);
        check(cat.exState == 1 && cat.catTransformTime == LiquidCatBoss.TRANSFORM_DURATION,
            "Liquid Cat must retain its transformation without double-decrementing timers");
        room.update(LiquidCatBoss.TRANSFORM_DURATION, player, plan);
        room.update(.01f, player, plan);
        CombatRoom.ExObject slime = room.exObjects.first();
        slime.life = 2;
        player.x = slime.x;
        player.y = slime.y;
        room.update(.01f, player, plan);
        check(player.slowTime > 0, "Custom Liquid Cat ooze must use cleanup's slime collision handling");

        room.begin(RunDirector.Room.EX, plan, 0);
        cat = (LiquidCatBoss) room.enemies.first();
        cat.x = 800; cat.y = 360;
        cat.exStateTime = .02f;
        cat.skillTimer = 0;
        player.x = 640; player.y = 360; player.hp = 100; player.invulnerable = 0;
        room.update(.01f, player, plan);
        check(cat.exMeter > 0 && cat.exAimX == 640 && cat.exAimY == 360,
            "Orbital leap must lock the warning point before dealing damage");
        player.x = 400;
        room.update(.5f, player, plan);
        check(cat.exState == 0 && cat.exMeter == 0 && cat.x == 640 && player.hp == 100,
            "A pending leap must finish before changing form, and the player can dodge its locked marker");
        room.update(.31f, player, plan);
        check(cat.exState == 1 && cat.catTransformTime > 0,
            "Liquid Cat must resume its form cycle after landing recovery");
        cat.stunTime = 1;
        float remaining = cat.catTransformTime;
        room.update(.1f, player, plan);
        check(cat.catTransformTime == remaining,
            "A stunned Cat must retain its pending transformation animation");
    }
}
