package io.github.kirinojuju;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.enemy.AlgoMonster;
import io.github.kirinojuju.enemy.NetworkMonster;
import io.github.kirinojuju.enemy.ProgramMonster;
import io.github.kirinojuju.enemy.SlimeMonster;
import io.github.kirinojuju.enemy.WarperMonsterBehavior;
import io.github.kirinojuju.enemy.boss.KenBoss;
import io.github.kirinojuju.enemy.boss.BossBody;
import io.github.kirinojuju.enemy.boss.KenBossBehavior;
import io.github.kirinojuju.enemy.boss.AeBoss;
import io.github.kirinojuju.enemy.boss.AeBossBehavior;
import io.github.kirinojuju.enemy.boss.FarBoss;
import io.github.kirinojuju.enemy.boss.FarBossBehavior;
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
        checkMonsterArtworkTypes();
        checkKen();
        checkKenBodyHitbox();
        checkBossBodyHitboxes();
        checkTransparentBodyPadding();
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
                } else if (plan.bossId() == RunPlan.FAR_ID) {
                    check(boss instanceof FarBoss && boss.behavior instanceof FarBossBehavior,
                        "Far must own the full-room blast behavior");
                }
                plan.completeStage();
            }
        }
        check(ken && m && sati, "All custom bosses must remain reachable");
        RunPlan plan = new RunPlan(new Random(21));
        room.begin(RunDirector.Room.MOBS, plan, 0);
        check(room.factory.createMonster(640, 300, CombatRoom.BASIC, plan) instanceof AlgoMonster,
            "Ordinary monsters must use Algorithm artwork");
        room.begin(RunDirector.Room.FINAL_EXAM, plan, 0);
        check(room.enemies.size == 3, "Cleanup final exam must retain all three anchored bosses");
        for (Enemy enemy : room.enemies) {
            check(enemy.anchored, "Final exam bosses must stay on their cabinets");
            check(!(enemy instanceof AeBoss), "Final exam Ae must retain its original behavior");
            check(!(enemy.behavior instanceof FarBossBehavior),
                "Final exam Far must retain its original anchored artillery behavior");
            check(!(enemy.behavior instanceof MBossBehavior),
                "Final exam must retain its anchored attacks rather than M's split behavior");
        }
        room.begin(RunDirector.Room.EX, plan, 0);
        check(room.enemies.first() instanceof LiquidCatBoss, "EX factory must retain the user's Liquid Cat class");
    }

    private static void checkMonsterArtworkTypes() {
        RunPlan plan = new RunPlan(new Random(13));
        CombatRoom room = new CombatRoom(new Random(14));
        boolean program = false, network = false, algo = false, slime = false;
        for (int stage = 0; stage < 4; stage++) {
            room.begin(RunDirector.Room.MOBS, plan, 0);
            for (int i = 0; i < room.enemies.size; i++) {
                Enemy enemy = room.enemies.get(i);
                check(enemy.kind == CombatRoom.BASIC || enemy.kind == CombatRoom.SHIELDED
                    || enemy.kind == CombatRoom.WARPER || enemy.kind == CombatRoom.RANGED
                    || enemy.kind == CombatRoom.WAVE || enemy.kind == CombatRoom.BINDER,
                    "All six supported monster kinds may spawn");
                if (enemy.kind == CombatRoom.WARPER) {
                    check(enemy instanceof ProgramMonster && enemy.behavior instanceof WarperMonsterBehavior,
                        "Programming monsters must retain their teleport attacks");
                    program = true;
                } else if (enemy.kind == CombatRoom.SHIELDED) {
                    check(enemy instanceof NetworkMonster && enemy.armor > 0,
                        "Network monsters must have a shield");
                    network = true;
                } else if (enemy.kind == CombatRoom.BASIC) {
                    check(enemy instanceof AlgoMonster, "Basic monsters must use Algorithm artwork");
                    algo = true;
                } else {
                    check(enemy instanceof SlimeMonster,
                        "Ranged, wave and binding monsters must retain the slime artwork fallback");
                    slime = true;
                }
                check(enemy.kind < 0 && enemy.hp > 0,
                    "Artwork selection must preserve the monster's combat identity and statistics");
            }
            plan.completeStage();
        }
        check(program && network && algo && slime, "All four monster artwork types must be reachable");
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
        check(ken.bombs.size >= 7 && ken.bombs.size <= 9 && ken.bombAttackTime > 0 && ken.farCharge == 0,
            "Ken must use bombs rather than accidentally triggering the cleanup Far grid");
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

    private static void checkKenBodyHitbox() {
        RunPlan plan = new RunPlan(new Random(15));
        PlayerCharacter player = new PlayerCharacter();
        float[][] points = {{0, -10}, {0, -KenBoss.DRAW_SIZE / 2},
            {0, -KenBoss.DRAW_SIZE + 10}, {-55, -100}, {60, -100}};
        for (float[] point : points) {
            CombatRoom room = new CombatRoom(new Random(16));
            KenBoss ken = new KenBoss(plan, new Random(17));
            ken.x = 800;
            ken.y = 450;
            BossBody.applyBounds(ken, new float[] {15f / 64, 1f / 64, 49f / 64, 63f / 64}, false);
            room.enemies.add(ken);
            float hitX = ken.x + point[0], hitY = ken.y + ken.radius + point[1];
            float hp = ken.hp;
            room.damageNearby(hitX, hitY, 1, 10);
            check(ken.hp == hp - 10, "Area attacks must hit Ken's head, torso, feet and sides");
            ken.hp = hp;
            room.damageArc(hitX - 20, hitY, 1, 0, 25, 60, 10);
            check(ken.hp == hp - 10, "Melee attacks must reach Ken's visible body");
            ken.hp = hp;
            room.fireFriendly(hitX, hitY, 0, 0, 10, null);
            room.update(.01f, player, plan);
            check(ken.hp == hp - 10, "Friendly projectiles must hit every part of Ken's body");
            ken.hp = hp;
            room.firePowerBall(hitX, hitY, 0, 0, 20, null);
            room.update(.01f, player, plan);
            check(ken.hp == hp - 20, "Power-ball impacts must hit Ken's body");
            ken.hp = hp;
            room.damageNearby(ken.x, ken.y + ken.radius - KenBoss.DRAW_SIZE - 30, 1, 10);
            check(ken.hp == hp, "Attacks below Ken's body must miss");
        }
        CombatRoom room = new CombatRoom(new Random(18));
        KenBoss ken = new KenBoss(plan, new Random(19));
        ken.x = 800;
        ken.y = 450;
        BossBody.applyBounds(ken, new float[] {15f / 64, 1f / 64, 49f / 64, 63f / 64}, false);
        room.enemies.add(ken);
        float hp = ken.hp;
        room.fireNotebookBeam(ken.x - 200, ken.y + ken.radius - KenBoss.DRAW_SIZE + 10,
            1, 0, 10, false);
        check(ken.hp == hp - 10, "Notebook beam must hit Ken's feet without crossing his head");
        for (int seed = 0; seed < 30; seed++) {
            KenBoss caster = new KenBoss(plan, new Random(seed));
            caster.skillTimer = 0;
            KenBossBehavior.updateBombSkill(caster, .01f, player, plan, true);
            check(caster.bombs.size >= 7 && caster.bombs.size <= 9,
                "Every Ken bomb cast must create 7 to 9 bombs");
        }
    }

    // Exercise actual attacks at head, torso, feet and sides of every supported boss body.
    private static void checkBossBodyHitboxes() {
        RunPlan plan = new RunPlan(new Random(50));
        PlayerCharacter player = new PlayerCharacter();
        int[] kinds = {RunPlan.KEN_ID, RunPlan.AE_ID, RunPlan.FAR_ID, RunPlan.M_ID,
            RunPlan.SATI_ID, RunPlan.DOME_ID, CombatRoom.LIQUID_CAT, CombatRoom.INSECURE_SWORD};
        for (int kind : kinds) {
            Enemy boss = new Enemy(700, 460, 36, 500, 72, 0, 0, kind);
            BossBody.applyBounds(boss, new float[] {.3f, .35f, .7f, .9f}, false);
            boss.stunTime = 100;
            // Non-square Ae/Far art must retain the same horizontal extent as its hit area.
            if (kind == RunPlan.AE_ID || kind == RunPlan.FAR_ID) boss.bodyAspectRatio = 1.4f;
            CombatRoom room = new CombatRoom(new Random(51));
            room.enemies.add(boss);
            float height = BossBody.top(boss) - BossBody.bottom(boss);
            float width = BossBody.right(boss) - BossBody.left(boss);
            float top = BossBody.top(boss);
            float[][] points = {{boss.x, top - 2}, {boss.x, top - height / 2},
                {boss.x, top - height + 2}, {boss.x - width / 2 + 2, top - height / 2},
                {boss.x + width / 2 - 2, top - height / 2}};
            for (float[] point : points) {
                // PowerBall pushes the boss; reset its position before testing the next body part.
                boss.x = 700;
                boss.y = 460;
                boss.hp = 500;
                room.damageNearby(point[0], point[1], 1, 10);
                check(boss.hp == 490, "Area hit must reach every body part of boss " + kind);
                boss.hp = 500;
                room.damageArc(point[0] - 20, point[1], 1, 0, 25, 60, 10);
                check(boss.hp == 490, "Melee hit must reach every body part of boss " + kind);
                boss.hp = 500;
                room.fireFriendly(point[0], point[1], 0, 0, 10, null);
                room.update(.01f, player, plan);
                check(boss.hp == 490, "Projectile must reach every body part of boss " + kind);
                boss.hp = 500;
                room.firePowerBall(point[0], point[1], 0, 0, 20, null);
                room.update(.01f, player, plan);
                check(boss.hp == 480, "PowerBall must reach every body part of boss " + kind);
            }
            boss.x = 700;
            boss.y = 460;
            boss.hp = 500;
            room.damageNearby(boss.x, top - height - 20, 1, 10);
            room.damageNearby(boss.x + width / 2 + 20, top - height / 2, 1, 10);
            check(boss.hp == 500, "Hits outside the boss body must miss: " + kind);
            room.fireNotebookBeam(boss.x - width / 2 - 100, top - height + 2, 1, 0, 10, false);
            check(boss.hp == 490, "Beam must hit feet without crossing the head: " + kind);
            check(Math.abs(boss.aimY() - (top - height / 2)) < .01f,
                "Player attacks must aim at the boss body center: " + kind);
            check(!boss.overlapsBeam(boss.x - width / 2 - 100, top - height - 20, 1, 0, 400, 1),
                "A beam below the body must miss: " + kind);
        }
        Enemy cat = new Enemy(700, 460, 16, 500, 72, 0, 0, CombatRoom.LIQUID_CAT);
        cat.exState = 1;
        check(BossBody.height(cat) == BossBody.SMALL_CAT_DRAW_SIZE,
            "Small cat hit area must shrink with its rendered form");
        cat.catTransformTime = .2f;
        check(BossBody.height(cat) == BossBody.CAT_DRAW_SIZE,
            "Transforming cat keeps the full-size animation's hit area");
        Enemy anchored = new Enemy(700, 460, 36, 500, 72, 0, 0, RunPlan.DOME_ID);
        anchored.anchored = true;
        check(anchored.damageRadius() == 36 && anchored.damageY(0) == anchored.y,
            "Final Exam must retain its original anchored hit geometry");
        Enemy monster = new Enemy(700, 460, 17, 50, 37, 80, 7, CombatRoom.BASIC);
        check(monster.damageRadius() == 17 && monster.aimY() == monster.y,
            "Ordinary monster geometry must remain unchanged");
    }

    // Transparent canvas margins must not count as the boss's body for any incoming attack.
    private static void checkTransparentBodyPadding() {
        int[] rgba = new int[64 * 64];
        for (int y = 8; y <= 36; y++)
            for (int x = 19; x <= 47; x++) rgba[y * 64 + x] = 0xffffffff;
        rgba[0] = 0xffffff1f; // A very faint pixel outside the body must not expand the hit area.
        float[] bounds = BossBody.visibleBounds(64, 64, rgba);
        check(bounds[0] == 19f / 64 && bounds[1] == 27f / 64
            && bounds[2] == 48f / 64 && bounds[3] == 56f / 64,
            "Visible bounds must exclude transparent and very faint image margins");
        check(BossBody.visibleBounds(64, 64, new int[64 * 64]) == null,
            "A fully transparent frame must not create an invisible body rectangle");
        RunPlan plan = new RunPlan(new Random(70));
        Enemy boss = new Enemy(700, 460, 36, 500, 72, 0, 0, RunPlan.M_ID);
        boss.stunTime = 100;
        BossBody.applyBounds(boss, bounds, false);
        CombatRoom room = new CombatRoom(new Random(71));
        room.enemies.add(boss);
        PlayerCharacter player = new PlayerCharacter();
        float paddingX = boss.x, paddingY = boss.y + boss.radius - BossBody.M_DRAW_SIZE + 10;
        room.damageNearby(paddingX, paddingY, 1, 10);
        room.damageArc(paddingX - 20, paddingY, 1, 0, 25, 60, 10);
        room.fireFriendly(paddingX, paddingY, 0, 0, 10, null);
        room.update(.01f, player, plan);
        room.fireNotebookBeam(boss.x - 200, paddingY, 1, 0, 10, false);
        check(boss.hp == 500, "Area, melee, projectile and beam hits inside empty PNG padding must miss");
        room.shots.clear();
        float centerX = (BossBody.left(boss) + BossBody.right(boss)) / 2;
        float centerY = (BossBody.bottom(boss) + BossBody.top(boss)) / 2;
        check(boss.aimX() == centerX && boss.aimY() == centerY,
            "Targeting must aim at the visible body, not the empty canvas center");
        room.damageNearby(centerX, centerY, 1, 10);
        check(boss.hp == 490, "The visible torso must still take damage");
        BossBody.applyBounds(boss, bounds, true);
        check(boss.bodyLeft == 1 - 48f / 64 && boss.bodyRight == 1 - 19f / 64,
            "Flipped artwork must mirror the visible hit bounds");
        BossBody.applyBounds(boss, null, false);
        check(boss.damageRadius() == boss.radius && boss.aimY() == boss.y,
            "Missing artwork must keep a small fallback hit area");
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
        CombatRoom.ExObject slime = room.exObjects.first();
        slime.life = 2;
        player.x = slime.x;
        player.y = slime.y;
        room.update(.01f, player, plan);
        check(player.slowTime > 0, "Custom Liquid Cat ooze must use cleanup's slime collision handling");
    }
}
