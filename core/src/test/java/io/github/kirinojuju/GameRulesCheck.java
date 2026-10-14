package io.github.kirinojuju;

import io.github.kirinojuju.weapon.WeaponAttack;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.boss.AeBoss;
import io.github.kirinojuju.enemy.boss.Boss;
import io.github.kirinojuju.enemy.boss.DomeBoss;
import io.github.kirinojuju.enemy.boss.KenBoss;
import io.github.kirinojuju.enemy.boss.MBoss;
import io.github.kirinojuju.enemy.boss.SatiBoss;
import io.github.kirinojuju.enemy.ex.AllergicVacuumBoss;
import io.github.kirinojuju.enemy.ex.ExBoss;
import io.github.kirinojuju.enemy.ex.InsecureSwordBoss;
import io.github.kirinojuju.enemy.ex.LiquidCatBoss;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.player.Upgrade;
import io.github.kirinojuju.weapon.WeaponAbility;
import io.github.kirinojuju.weapon.WeaponAttack.WeaponAttackSpec;
import io.github.kirinojuju.world.EntranceScene;
import io.github.kirinojuju.world.GameSpace;
import io.github.kirinojuju.world.IceWall;
import io.github.kirinojuju.world.RoomObstacles;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Dependency-free regression check for the loop and weapon rules. */
public final class GameRulesCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        checkInjectedWeapons();
        RunPlan order = new RunPlan(new Random(4));
        Set<Integer> stages = new HashSet<Integer>();
        Set<Integer> bosses = new HashSet<Integer>();
        Set<Integer> bossFamilies = new HashSet<Integer>();
        for (int i = 0; i < 4; i++) {
            stages.add(order.stageId());
            bosses.add(order.bossId());
            check(RunPlan.isBossId(order.bossId()), "Every chosen boss must have an active identity");
            bossFamilies.add(order.bossId() % 4);
            order.completeStage();
        }
        check(stages.size() == 4, "A loop must use each stage once");
        check(bosses.size() == 4, "A loop must choose four distinct Ajarns");
        check(bossFamilies.size() == 4, "A loop must include four different boss styles");

        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(5));
        RunDirector run = new RunDirector(new Random(6));
        run.startRun(player, combat);
        run.advanceSceneFade(.41f, player, combat);
        check(run.phase == RunDirector.Phase.ENTRANCE && combat.enemies.size == 0
            && !player.hasBothWeapons(),
            "Starting the game should open Building 30 with walking only");
        run.advanceSceneFade(.41f, player, combat);
        run.requestDoor(player);
        check(run.phase == RunDirector.Phase.ENTRANCE && run.sceneFade == 0,
            "The core game must stay closed until the student reaches the left door");
        EntranceScene.move(player, .5f, -1, 0);
        check(player.x < 640 && player.moving,
            "The student should walk across the entrance scene");
        player.x = EntranceScene.DOOR_X;
        player.y = EntranceScene.DOOR_Y;
        run.requestDoor(player);
        run.advanceSceneFade(.2f, player, combat);
        check(run.phase == RunDirector.Phase.ENTRANCE && run.sceneFade > 0,
            "The door should fade out before entering combat");
        run.advanceSceneFade(.21f, player, combat);
        check(run.phase == RunDirector.Phase.FIGHT && combat.enemies.size > 0,
            "The left door should start the first monster room");
        check(player.hasBothWeapons() && player.activeWeapon() == PlayerCharacter.Weapon.PHYSICAL,
            "Both weapons must be available when entering the core game");
        checkCarEnding();
        checkCombatFrameOrder();
        player.switchTo(PlayerCharacter.Weapon.MAGICAL);
        check(player.activeWeapon() == PlayerCharacter.Weapon.MAGICAL,
            "The notebook must be selectable in loop one");
        player.switchTo(PlayerCharacter.Weapon.PHYSICAL);

        combat.enemies.clear();
        Enemy target = GameRulesCheck.fixture(player.x + 150, player.y,
            17, 100, 37, 0, 0, -1);
        Enemy backup = GameRulesCheck.fixture(player.x + 350, player.y,
            17, 100, 37, 0, 0, -1);
        combat.enemies.add(target);
        combat.enemies.add(backup);
        float castX = player.x, castY = player.y;
        check(player.tryStartSkill(combat), "E / touch skill should lock a target");
        check(player.skillPlaying && !player.moving && player.sp == 70,
            "Casting should stop walking and consume 30 SP");
        check(player.facingRight && player.faceX > 0,
            "The student should face the locked target at cast start");
        target.x = player.x - 150;
        player.advanceSkill(.39f, combat);
        check(!player.facingRight && player.faceX < 0,
            "The student should turn when the locked target crosses behind");
        check(combat.shots.size == 0, "PowerBall should wait for the release frame");
        player.advanceSkill(.02f, combat);
        check(combat.shots.size == 1 && combat.shots.first().powerBall
            && combat.shots.first().target == target,
            "Casting should release the PowerBall sprite projectile");
        target.x = player.x + 150;
        combat.update(.01f, player, run.plan);
        check(combat.shots.first().vx > 0,
            "PowerBall should turn to follow its moving target");
        target.setHp(0);
        combat.update(.01f, player, run.plan);
        check(combat.shots.first().target == backup,
            "PowerBall should relock another living enemy if its target falls");
        player.advanceSkill(.4f, combat);
        check(!player.skillPlaying && combat.shots.size == 1,
            "One cast should release exactly one PowerBall");
        check(player.x == castX && player.y == castY,
            "The player should remain in place through the cast");
        check(!player.tryStartSkill(combat), "The cooldown should prevent immediate recasting");

        for (int i = 0; i < 4; i++) {
            clearBossAndChoose(run, player, combat);
        }
        check(run.room == RunDirector.Room.EX && run.plan.loop() == 1
            && player.lanTether && player.notebookHeatBeam,
            "The first EX must open after four bosses in loop one");
        run.finishRoom(player);
        run.continueAfterRoom(player, combat);
        clearFinalExamIfPending(run, player, combat);
        check(run.plan.loop() == 2 && player.hasBothWeapons(),
            "Loop two must start after the first EX reward");
        player.switchTo(PlayerCharacter.Weapon.PHYSICAL);
        check(player.activeWeapon() == PlayerCharacter.Weapon.PHYSICAL,
            "The player must be able to switch back during combat");

        for (int i = 0; i < 4; i++) {
            clearBossAndChoose(run, player, combat);
        }
        check(run.room == RunDirector.Room.EX && run.plan.loop() == 2
            && player.lanTether && player.notebookHeatBeam,
            "EX must happen before loop 3 starts");
        run.finishRoom(player);
        check(!player.lanTether && !player.notebookHeatBeam,
            "EX-only weapon techniques must expire after the encounter");
        run.continueAfterRoom(player, combat);
        clearFinalExamIfPending(run, player, combat);
        check(run.plan.loop() == 3 && run.room == RunDirector.Room.MOBS,
            "Loop 3 must begin after the EX reward");
        for (int i = 0; i < 4; i++) {
            clearBossAndChoose(run, player, combat);
        }
        check(run.plan.loop() == 4 && run.room == RunDirector.Room.MOBS,
            "Odd loops after the first must skip EX and begin the next loop");
        for (int i = 0; i < 4; i++) {
            clearBossAndChoose(run, player, combat);
        }
        check(run.room == RunDirector.Room.EX
            && player.lanTether && player.notebookHeatBeam,
            "Every even loop, including loop 4, must end with EX");
        check(run.plan.healthScale() > 1 && run.plan.spScale() > 1
            && run.plan.speedScale() > 1 && run.plan.damageScale() > 1,
            "Enemy stats must increase with the loop");
        checkEnemyMechanics();
        checkNewCombat();
        checkAeBalance();
        checkBossMinions();
        checkFirstExProgression();
        checkUpgrades();
        checkUpgradeReroll();
        checkWeaponAbilityExtension();
        checkLanWave();
        checkNotebookImpacts();
        checkRoomObstacles();
        checkIceWall();
        checkPotionInventory();
        checkRandomTables();
        checkBossShotsAcrossTables();
        checkTetherAndHeatBeam();
        checkExMechanics();
        checkExTestEntry();
        checkSatiSkill();
        checkPowerBallKnockback();
        checkDomeSkills();
        checkFinalExamMechanics();
        System.out.println("Game rules verified");
    }

    private static void clearBossAndChoose(RunDirector run, PlayerCharacter player,
                                            CombatRoom combat) {
        run.room = RunDirector.Room.BOSS;
        run.finishRoom(player);
        check(run.phase == RunDirector.Phase.UPGRADE,
            "Defeating an Ajarn must offer an upgrade");
        check(run.offeredUpgrades[0] != null && run.offeredUpgrades[1] != null
            && run.offeredUpgrades[2] != null
            && run.offeredUpgrades[0] != run.offeredUpgrades[1]
            && run.offeredUpgrades[0] != run.offeredUpgrades[2]
            && run.offeredUpgrades[1] != run.offeredUpgrades[2],
            "The upgrade screen must have three different choices");
        run.chooseUpgrade(0, player, combat);
        clearFinalExamIfPending(run, player, combat);
        check(run.phase == RunDirector.Phase.FIGHT,
            "Choosing an upgrade must advance to the next room");
    }

    private static void clearFinalExamIfPending(RunDirector run, PlayerCharacter player,
                                                 CombatRoom combat) {
        if (run.phase != RunDirector.Phase.CUTSCENE) return;
        run.continueAfterCutscene(player, combat);
        check(run.room == RunDirector.Room.FINAL_EXAM && combat.enemies.size == 2,
            "The cutscene must lead to the two-boss server room");
        combat.enemies.clear();
        run.finishRoom(player);
        run.continueAfterRoom(player, combat);
    }

    private static void checkFinalExamMechanics() {
        RunPlan identities = new RunPlan(new Random(91));
        identities.recordBossDefeat(RunPlan.AE_ID);
        identities.recordBossDefeat(RunPlan.M_ID);
        check(!identities.finalExamReady(), "Defeating M must not count as defeating Dome");
        identities.recordBossDefeat(RunPlan.DOME_ID);
        check(identities.finalExamReady(), "Ae and Dome must unlock the final exam");
        checkFinalExamMechanics(1);
        checkFinalExamMechanics(2);
    }

    private static void checkIceWall() {
        RunPlan plan = new RunPlan(new Random(140));
        CombatRoom combat = new CombatRoom(new Random(141));
        PlayerCharacter player = new PlayerCharacter();
        player.x = 600;
        player.y = 360;
        player.sp = PlayerCharacter.ICE_WALL_SP_COST - 1;
        check(!player.tryStartIceWall(combat) && combat.iceWall.time == 0,
            "Ice Wall must require enough SP");
        player.sp = 100;
        Enemy nearby = GameRulesCheck.fixture(692, 360, 17, 100, 0, 0, 0, CombatRoom.BASIC);
        combat.enemies.add(nearby);
        check(player.tryStartIceWall(combat) && player.sp == 75
            && player.iceWallCooldown == 7 && nearby.getHp() == 76 && nearby.stunTime == .8f,
            "Ice Wall must spend SP, start cooldown, damage and stun nearby enemies");
        check(!player.tryStartIceWall(combat) && player.sp == 75,
            "Cooldown must prevent a second cast without spending SP");
        nearby.setHp(0);
        player.moveBy(250, 0);
        check(player.x < combat.iceWall.x - IceWall.HALF_DEPTH - 17,
            "Walking must not cross Ice Wall");
        float previousY = player.y;
        player.moveBy(0, 35);
        check(player.y == previousY + 35, "The player must be able to move along the wall");
        player.x = 600;
        player.y = 360;
        check(player.dash(1, 0) && player.x < combat.iceWall.x,
            "Dash must stop on the player's side of Ice Wall");

        for (float angle : new float[] {0, 45, 90, 135}) {
            combat.enemies.clear();
            combat.shots.clear();
            float normalX = com.badlogic.gdx.math.MathUtils.cosDeg(angle);
            float normalY = com.badlogic.gdx.math.MathUtils.sinDeg(angle);
            combat.iceWall.raise(700 - normalX * 92, 360 - normalY * 92, normalX, normalY);
            player.x = 700 - normalX * 80;
            player.y = 360 - normalY * 80;
            player.invulnerable = 0;
            player.hp = 100;
            CombatRoom.Shot hostile = new CombatRoom.Shot(700 + normalX * 80, 360 + normalY * 80,
                -normalX * 1600, -normalY * 1600, 7, 20, 1, false);
            hostile.ignoresTables = true;
            combat.shots.add(hostile);
            combat.update(.1f, player, plan);
            check(combat.shots.size == 0 && player.hp == 100,
                "Ice Wall must stop fast boss projectiles at every tested angle");

            Enemy target = GameRulesCheck.fixture(player.x, player.y, 17,
                100, 0, 0, 0, CombatRoom.BASIC);
            target.stunTime = 10;
            combat.enemies.add(target);
            combat.shots.add(new CombatRoom.Shot(700 + normalX * 80, 360 + normalY * 80,
                -normalX * 1600, -normalY * 1600, 7, 20, 1, true));
            combat.update(.1f, player, plan);
            check(target.getHp() == 80, "Friendly projectiles must pass through Ice Wall");
        }

        combat.enemies.clear();
        combat.iceWall.raise(600, 360, 1, 0);
        Enemy blocked = GameRulesCheck.fixture(692, 360, 17, 100, 0, 0, 0, CombatRoom.BASIC);
        combat.enemies.add(blocked);
        combat.update(.01f, player, plan);
        check(combat.iceWall.hitFraction(blocked.x, blocked.y, blocked.x, blocked.y, blocked.radius) < 0,
            "An enemy must be separated from the active wall");

        combat.iceWall.update(IceWall.DURATION);
        check(combat.iceWall.time == 0
            && combat.iceWall.hitFraction(600, 360, 800, 360, 17) < 0,
            "Expired Ice Wall must stop blocking movement and shots");
        player.x = 600;
        player.y = 360;
        player.moveBy(200, 0);
        check(player.x == 800, "The former wall position must be walkable after expiry");

        RunDirector run = new RunDirector(new Random(142));
        run.startExTest(player, combat, 2);
        check(player.iceWallCooldown == 0 && combat.iceWall.time == 0,
            "A new room must clear Ice Wall and its cooldown");
        combat.begin(RunDirector.Room.FINAL_EXAM, plan, 0);
        Enemy anchored = combat.enemies.first();
        float originalX = anchored.x, originalY = anchored.y;
        combat.iceWall.x = originalX;
        combat.iceWall.y = originalY;
        combat.iceWall.time = IceWall.DURATION;
        combat.iceWall.resolveEnemy(anchored);
        check(anchored.x == originalX && anchored.y == originalY,
            "Ice Wall must not move the anchored Final Exam bosses");

        RoomObstacles tables = new RoomObstacles();
        tables.tables.add(new RoomObstacles.Table(0, 500, 360));
        player.startRoom(false);
        player.x = 450;
        player.y = 360;
        player.setObstacles(tables);
        player.setIceWall(combat.iceWall);
        combat.iceWall.raise(700, 360, 1, 0);
        player.moveBy(500, 0);
        check(player.x < 500, "Adding Ice Wall must preserve collision with tables");

        PlayerCharacter caster = new PlayerCharacter();
        CombatRoom fixed = new CombatRoom(new Random(143));
        fixed.enemies.add(GameRulesCheck.fixture(caster.x - 150, caster.y, 17,
            100, 0, 0, 0, CombatRoom.BASIC));
        check(caster.tryStartIceWall(fixed) && fixed.iceWall.x == caster.x + IceWall.CAST_DISTANCE
            && fixed.iceWall.y == caster.y,
            "Ice Wall must follow the caster's facing, not auto-turn toward another enemy");
        float wallX = fixed.iceWall.x, wallY = fixed.iceWall.y;
        caster.x += 30;
        fixed.enemies.first().y += 50;
        fixed.update(.1f, caster, plan);
        check(fixed.iceWall.x == wallX && fixed.iceWall.y == wallY,
            "An active Ice Wall must keep its world position when actors move");
        caster.iceWallCooldown = 0;
        caster.sp = 100;
        caster.x = GameSpace.RIGHT - 30;
        check(!caster.tryStartIceWall(fixed) && caster.sp == 100 && caster.iceWallCooldown == 0
            && fixed.iceWall.x == wallX && fixed.iceWall.y == wallY,
            "Outward casts at the arena edge must fail without moving the wall or spending SP");
        check(fixed.iceWall.moveX(wallX, wallY, 40, 17) > wallX,
            "An actor pushed into the wall center must be able to escape");
        Enemy fast = GameRulesCheck.fixture(wallX - 80, wallY, 17, 100, 0, 0, 0, CombatRoom.BASIC);
        fast.x = wallX + 80;
        fixed.iceWall.stopEnemy(fast, wallX - 80, wallY);
        check(fast.x < wallX, "Swept enemy movement must not tunnel through Ice Wall");
        fixed.iceWall.raise(GameSpace.LEFT + 107, 300, -1, 0);
        fast.x = fixed.iceWall.x;
        fast.y = fixed.iceWall.y;
        fixed.iceWall.resolveEnemy(fast);
        check(fixed.iceWall.hitFraction(fast.x, fast.y, fast.x, fast.y, fast.radius) < 0,
            "Wall separation near an arena edge must choose an exit that is inside the room");
    }

    private static void checkPotionInventory() {
        PlayerCharacter player = new PlayerCharacter();
        check(!player.potions.useHp(player) && !player.potions.useSp(player)
            && player.potions.hpCount() == 2 && player.potions.spCount() == 2,
            "Full HP/SP must not waste a potion");
        player.hp = 30;
        player.sp = 80;
        check(player.potions.useHp(player) && player.hp == 70 && player.potions.hpCount() == 1,
            "HP potion must heal 40 and consume exactly one bottle");
        check(player.potions.useSp(player) && player.sp == 100 && player.potions.spCount() == 1,
            "SP potion must recover 35 with a cap of 100");
        player.startRoom(false);
        check(player.potions.hpCount() == 1 && player.potions.spCount() == 1,
            "Potion inventory must persist across rooms");
        check(player.potions.useHp(player) && player.hp == 100 && !player.potions.useHp(player),
            "Healing must clamp at max HP and an empty stack must not be usable");
        player.hp = 0;
        player.sp = 0;
        check(!player.potions.useSp(player) && player.potions.spCount() == 1,
            "Potion use must not revive a defeated player");
        check(new PlayerCharacter().potions.hpCount() == 2,
            "A new run must create a fresh inventory");
    }

    private static void checkFinalExamMechanics(int loop) {
        RunPlan plan = new RunPlan(new Random(90));
        plan.recordBossDefeat(RunPlan.AE_ID);
        check(!plan.finalExamReady(), "Ae alone must not unlock the final exam");
        plan.recordBossDefeat(RunPlan.DOME_ID);
        check(plan.finalExamReady(), "Ae and Dome must unlock the final exam");

        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(91));
        RunDirector run = null;
        for (int seed = 92; seed < 192; seed++) {
            RunDirector candidate = new RunDirector(new Random(seed));
            if (loop == 2) candidate.plan.nextLoop();
            while (!candidate.plan.isFinalStage()) candidate.plan.completeStage();
            if (candidate.plan.bossId() == RunPlan.DOME_ID) {
                run = candidate;
                break;
            }
        }
        check(run != null, "The test needs Dome as the final boss of an EX loop");
        run.plan.recordBossDefeat(RunPlan.AE_ID);
        check(!run.plan.finalExamReady(), "Dome must be the second required victory");
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        run.room = RunDirector.Room.BOSS;
        run.finishRoom(player);
        check(run.nextRoom == RunDirector.Room.EX,
            "The first-loop and even-loop EX must both happen before a queued final exam");
        check(run.plan.finalExamReady(),
            "Defeating Dome after Ae must unlock the final exam");
        run.chooseUpgrade(0, player, combat);
        check(run.room == RunDirector.Room.EX && run.phase == RunDirector.Phase.FIGHT,
            "The original EX monster fight must remain playable");
        combat.enemies.clear();
        run.finishRoom(player);
        check(run.nextRoom == RunDirector.Room.FINAL_EXAM,
            "After EX, the two-boss fight must be queued");
        run.continueAfterRoom(player, combat);
        check(run.phase == RunDirector.Phase.CUTSCENE,
            "The black cutscene must appear before the final exam");
        run.continueAfterCutscene(player, combat);
        check(run.room == RunDirector.Room.FINAL_EXAM && combat.enemies.size == 2,
            "The final exam must spawn exactly two bosses");
        int[] kinds = {RunPlan.AE_ID, RunPlan.DOME_ID};
        for (int slot = 0; slot < kinds.length; slot++) {
            Enemy boss = combat.enemies.get(slot);
            check(boss.kind == kinds[slot] && boss.anchored
                && Math.abs(boss.x - GameSpace.serverBossX(slot * 2)) < .001f
                && boss.y == GameSpace.SERVER_BOSS_Y,
                "Each boss must stay aligned with its server cabinet");
        }
        float playerY = player.y, playerX = player.x;
        player.moveBy(0, 200);
        check(player.y == playerY && player.x == playerX,
            "The server fight must ignore vertical player movement");
        player.dash(0, 1);
        check(player.y == playerY && player.x != playerX,
            "Dash must remain horizontal in the server fight");
        Enemy anchored = combat.enemies.first();
        float bossX = anchored.x, bossY = anchored.y, bossHp = anchored.getHp();
        combat.firePowerBall(bossX, bossY, 0, 0, 10, anchored);
        combat.update(.01f, player, run.plan);
        check(anchored.getHp() < bossHp && anchored.x == bossX && anchored.y == bossY,
            "PowerBall must hurt a cabinet boss without knocking it off its cabinet");
        combat.enemies.get(0).setHp(0);
        check(!combat.update(.01f, player, run.plan),
            "The final exam must continue while one of its two bosses is alive");
        combat.enemies.first().setHp(0);
        check(combat.update(.01f, player, run.plan),
            "Defeating both bosses must clear the final exam");
        run.finishRoom(player);
        run.continueAfterRoom(player, combat);
        check(run.room == RunDirector.Room.MOBS && run.plan.loop() == loop + 1
            && player.y == GameSpace.HEIGHT / 2,
            "The next loop must resume normal movement after the final exam");
    }

    private static void checkCarEnding() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(70));
        RunDirector run = new RunDirector(new Random(71));
        run.startRun(player, combat);
        run.advanceSceneFade(.41f, player, combat);
        run.advanceSceneFade(.41f, player, combat);
        player.x = EntranceScene.CAR_X - 60;
        player.y = EntranceScene.CAR_Y;
        run.openCarPrompt(player);
        check(run.phase == RunDirector.Phase.CAR_PROMPT,
            "The nearby car should open a choice rather than end the game immediately");
        run.chooseCar(false);
        check(run.phase == RunDirector.Phase.ENTRANCE,
            "Declining the ride should return to free walking");
        run.openCarPrompt(player);
        run.chooseCar(true);
        run.advanceSceneFade(.41f, player, combat);
        check(run.phase == RunDirector.Phase.CAR_ENDING
            && combat.enemies.size == 0 && !player.hasBothWeapons(),
            "Going home should reach the joke ending without starting combat");
    }

    private static void checkEnemyMechanics() {
        RunPlan plan = new RunPlan(new Random(7));
        PlayerCharacter player = new PlayerCharacter();
        player.bind(3.5f);
        check(player.isBound(), "Binder should stop movement");
        player.escapePress(-1);
        player.escapePress(-1);
        check(player.bindProgress == 1, "Repeating one direction must not escape");
        player.escapePress(1);
        player.escapePress(-1);
        player.escapePress(1);
        player.escapePress(-1);
        check(player.isBound(), "The bind should need six alternating presses");
        player.escapePress(1);
        check(!player.isBound() && player.bindImmunity > 0,
            "Six alternating A/D presses should release the player");

        CombatRoom combat = new CombatRoom(new Random(8));
        Enemy first = GameRulesCheck.fixture(300, 300, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        Enemy second = GameRulesCheck.fixture(300, 300, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        first.sp = second.sp = 0;
        combat.enemies.add(first);
        combat.enemies.add(second);
        combat.update(.01f, player, plan);
        float dx = first.x - second.x, dy = first.y - second.y;
        check(dx * dx + dy * dy >= 45 * 45,
            "Ordinary enemies should separate instead of overlapping");

        combat.enemies.clear();
        Enemy warper = GameRulesCheck.fixture(300, 300, 17, 58, 42, 0, 0,
            CombatRoom.WARPER);
        warper.sp = warper.maxSp;
        combat.enemies.add(warper);
        player.x = 640;
        player.y = 360;
        player.invulnerable = 0;
        combat.update(.01f, player, plan);
        check(warper.warpWindup > 0 && warper.warpX == 640,
            "Warper should mark the player's position before striking");
        player.x = 800;
        combat.update(.6f, player, plan);
        check(player.hp == 100 && Math.abs(warper.x - 640) < 1,
            "Leaving the marked spot should dodge the warp attack");

        combat.enemies.clear();
        Enemy strikingWarper = GameRulesCheck.fixture(300, 300, 17, 58, 42, 0, 0,
            CombatRoom.WARPER);
        strikingWarper.sp = strikingWarper.maxSp;
        combat.enemies.add(strikingWarper);
        player.x = 640;
        player.y = 360;
        player.invulnerable = 0;
        combat.update(.01f, player, plan);
        combat.update(.6f, player, plan);
        check(player.hp == 75, "Staying on the warp marker should take heavy damage");

        combat.enemies.clear();
        combat.shots.clear();
        PlayerCharacter bindVictim = new PlayerCharacter();
        CombatRoom.Shot bindShot = new CombatRoom.Shot(bindVictim.x, bindVictim.y,
            0, 0, 9, 3, 1, false);
        bindShot.binding = true;
        combat.shots.add(bindShot);
        combat.update(.01f, bindVictim, plan);
        check(bindVictim.isBound() && bindVictim.hp == 97,
            "A binder projectile should damage and immobilize the player");

        combat.enemies.clear();
        combat.shots.clear();
        Enemy boss = GameRulesCheck.fixture(850, 400, 36, 200, 72, 0, 12, 2);
        boss.reflector = true;
        boss.armor = boss.maxArmor = 110;
        combat.enemies.add(boss);
        combat.damageNearby(boss.x, boss.y, 1, 27);
        check(boss.getHp() == 200 && boss.armor == 83 && combat.shots.size == 1
            && combat.shots.first().reflected,
            "Armored boss should absorb damage and reflect an attack");
        combat.update(.02f, player, plan);
        check(combat.enemies.size == 1, "Boss behavior must not summon extra monsters");
    }

    private static void checkNewCombat() {
        PlayerCharacter player = new PlayerCharacter();
        player.moveBy(0, 1000);
        check(player.y == GameSpace.WALK_TOP - 64,
            "Walking or dashing must stop before the window wall");
        player.moveBy(0, -76);
        RunPlan plan = new RunPlan(new Random(11));
        CombatRoom combat = new CombatRoom(new Random(12));

        Enemy front = GameRulesCheck.fixture(player.x + 115, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        Enemy diagonal = GameRulesCheck.fixture(player.x + 90, player.y + 60,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        Enemy behind = GameRulesCheck.fixture(player.x - 100, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        Enemy beyond = GameRulesCheck.fixture(player.x + 150, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        combat.enemies.add(front);
        combat.enemies.add(diagonal);
        combat.enemies.add(behind);
        combat.enemies.add(beyond);
        combat.damageArc(player.x, player.y, 1, 0, PlayerCharacter.LAN_REACH, 65, 27);
        check(front.getHp() == 73 && diagonal.getHp() == 73 && behind.getHp() == 100
            && beyond.getHp() == 100,
            "LAN cable should sweep a shorter forward arc without striking behind");
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        player.lockAttackTarget(combat, behind.x, behind.y);
        check(player.attackTarget() == behind && player.faceX < 0,
            "Clicking an enemy should lock and face that enemy");
        player.attack(combat);
        check(behind.getHp() == 73 && front.getHp() == 73
            && player.attackTimer == PlayerCharacter.LAN_ATTACK_INTERVAL,
            "A locked LAN attack should strike its target at the slower attack rate");
        player.lockAttackTarget(combat, front.x, front.y);
        check(player.attackTarget() == front && player.faceX > 0,
            "Clicking another enemy should switch the attack lock");
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        player.attack(combat);
        CombatRoom.Shot notebookShot = combat.shots.first();
        check(notebookShot.target == front && notebookShot.friendly
            && player.attackTimer == PlayerCharacter.NOTEBOOK_ATTACK_INTERVAL,
            "Notebook attack should carry the locked target and updated timing");
        front.x = player.x - 115;
        combat.update(.01f, player, plan);
        check(notebookShot.vx < 0,
            "Notebook shot should track a locked target that moves behind the player");

        combat.enemies.clear();
        combat.shots.clear();
        Enemy impact = GameRulesCheck.fixture(780, 360, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        Enemy splash = GameRulesCheck.fixture(875, 360, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        Enemy outside = GameRulesCheck.fixture(1100, 360, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        impact.sp = splash.sp = outside.sp = 0;
        combat.enemies.add(impact);
        combat.enemies.add(splash);
        combat.enemies.add(outside);
        combat.firePowerBall(780, 360, 0, 0, 42, impact);
        combat.update(.01f, player, plan);
        check(impact.getHp() < 100 && splash.getHp() < 100 && outside.getHp() == 100,
            "PowerBall should damage multiple enemies in its blast radius");
        check(impact.stunTime > 0 && splash.slowTime > 0 && combat.blastTime > 0,
            "PowerBall should shock enemies and show its explosion");

        combat.enemies.clear();
        combat.shots.clear();
        Enemy shooter = GameRulesCheck.fixture(500, 360, 17, 100, 999, 80, 0,
            CombatRoom.RANGED);
        shooter.sp = 0;
        combat.enemies.add(shooter);
        combat.update(.5f, player, plan);
        check(shooter.x < 500, "Ranged monster should retreat when the player is close");
        shooter.x = 100;
        combat.update(.5f, player, plan);
        check(shooter.x > 100, "Ranged monster should approach only until it can fire");

        combat.enemies.clear();
        combat.shots.clear();
        Enemy waveEnemy = GameRulesCheck.fixture(300, 360, 17, 100, 43, 0, 9,
            CombatRoom.WAVE);
        waveEnemy.sp = waveEnemy.maxSp;
        combat.enemies.add(waveEnemy);
        combat.update(.02f, player, plan);
        check(combat.shots.size == 1 && combat.shots.first().wave
            && combat.shots.first().radius > 20,
            "Wave monster should fire an expanding energy wave");

        combat.begin(RunDirector.Room.MOBS, plan, 0);
        int ranged = 0, advanced = 0;
        for (Enemy enemy : combat.enemies) {
            check(enemy.y <= GameSpace.WALK_TOP - enemy.radius,
                "Monsters must spawn below the windows");
            if (enemy.kind == CombatRoom.RANGED) ranged++;
            if (enemy.kind != CombatRoom.BASIC && enemy.kind != CombatRoom.RANGED) advanced++;
        }
        check(ranged == 1 && advanced == 0,
            "The first room of loop one should introduce only the ranged special");
        player.y = GameSpace.TOP;
        combat.update(1f, player, plan);
        for (Enemy enemy : combat.enemies)
            check(enemy.y <= GameSpace.WALK_TOP - enemy.radius,
                "Monsters must stay below the windows while chasing");
        player.y = GameSpace.HEIGHT / 2;
        plan.completeStage();
        combat.begin(RunDirector.Room.MOBS, plan, 0);
        boolean waveIntroduced = false;
        for (Enemy enemy : combat.enemies)
            if (enemy.kind == CombatRoom.WAVE) waveIntroduced = true;
        check(waveIntroduced, "The wave monster should appear in the second stage");
        plan.completeStage();
        combat.begin(RunDirector.Room.MOBS, plan, 0);
        Enemy shield = null;
        for (Enemy enemy : combat.enemies)
            if (enemy.kind == CombatRoom.SHIELDED && enemy.armor > 0) shield = enemy;
        check(shield != null, "Shielded monsters should appear later in loop one");
        float shieldHp = shield.getHp(), shieldArmor = shield.armor;
        combat.damageNearby(shield.x, shield.y, 1, 20);
        check(shield.getHp() == shieldHp && shield.armor == shieldArmor - 20,
            "Shielded monster should lose armor before health");

        RunPlan bossPlan = new RunPlan(new Random(13));
        for (int stage = 0; stage < 4; stage++) {
            combat.begin(RunDirector.Room.BOSS, bossPlan, 0);
            Enemy boss = combat.enemies.first();
            check(boss.x >= GameSpace.RIGHT - 80
                && boss.y <= GameSpace.WALK_TOP - boss.radius,
                "Boss should spawn at the far right below the windows");
            float homeX = boss.x, homeY = boss.y;
            int family = boss.kind % 4;
            if (boss.kind == RunPlan.DOME_ID)
                check(boss instanceof DomeBoss && ((DomeBoss) boss).dome() != null
                    && boss.reflector && boss.armor > 0 && !boss.waveCaster,
                    "Dome should use lightning and laser with his reflecting armor");
            else if (boss instanceof KenBoss) {
                check(boss.summoner && !boss.warper && !boss.reflector,
                    "Ken must retain his original bomb/summon traits");
                boss.skillTimer = 0;
                combat.update(.01f, player, bossPlan);
                check(((KenBoss) boss).bombs.size >= 3 ,
                    "Ken must use bombs rather than a room-wide grid");
            } else if (family == 0)
                check(boss.reflector && boss.armor > 0,
                    "Shield boss missing its reflecting armor");
            else if (boss.kind == RunPlan.AE_ID)
                check(boss.warper && boss.warpCooldown == 6f,
                    "Ae should wait before his first warned warp");
            else if (family == 1) {
                check(boss.warper,
                    "Warp boss missing its teleport trait");
                boss.warpCooldown = 0;
                combat.update(.01f, player, bossPlan);
                check(boss.warpReturnTime > 0 && boss.warpWindup == 0
                    && boss.x != homeX && player.isBound(),
                    "Warp boss should appear instantly and bind the player");
                combat.update(.4f, player, bossPlan);
                check(boss.x == homeX && boss.y == homeY,
                    "Warp boss should return to its home position");
                float hpBeforeStrike = player.hp;
                boss.warpCooldown = 0;
                combat.update(.01f, player, bossPlan);
                check(player.hp < hpBeforeStrike,
                    "The next sudden boss warp should strike immediately");
                combat.update(.4f, player, bossPlan);
            }
            else if (boss instanceof MBoss) {
                boss.skillTimer = 0;
                combat.update(.01f, player, bossPlan);
                boolean mShot = false;
                for (CombatRoom.Shot shot : combat.shots)
                    if (shot.radius == 10 && shot.damage == 7 && shot.ignoresTables) mShot = true;
                check(mShot, "M should retain its aimed projectile instead of the generic artillery shot");
            } else if (family == 2) {
                check(boss.artillery, "Artillery boss missing its huge projectile trait");
                boss.sp = boss.maxSp;
                combat.update(.01f, player, bossPlan);
                boolean giant = false;
                for (CombatRoom.Shot shot : combat.shots)
                    if (shot.giant && shot.radius >= 38) giant = true;
                check(giant, "Artillery boss should fire a huge projectile");
            } else
                check(boss.waveCaster && boss.reflector,
                    "Wave and reflection boss missing its traits");
            combat.update(.1f, player, bossPlan);
            check(boss.x == homeX && boss.y == homeY,
                "Boss should hold its position between attacks");
            bossPlan.completeStage();
        }
    }

    private static void checkAeBalance() {
        CombatRoom combat = new CombatRoom(new Random(104));
        RunPlan plan = new RunPlan(new Random(105));
        PlayerCharacter player = new PlayerCharacter();
        Enemy ae = GameRulesCheck.fixture(1100, 400, 36, 200, 100, 0, 12, RunPlan.AE_ID);
        ae.warper = true;
        ae.skillTimer = 100;
        combat.enemies.add(ae);
        combat.update(.01f, player, plan);
        check(ae.warpWindup == .8f && ae.x == ae.homeX && ae.y == ae.homeY
            && player.hp == 100 && !player.isBound() && combat.shots.size == 0,
            "Ae must warn before teleporting, binding, damaging or shooting");
        float markedX = ae.warpX, markedY = ae.warpY;
        player.moveBy(150, 0);
        combat.update(.81f, player, plan);
        check(ae.x == markedX && ae.y == markedY && player.hp == 100 && !player.isBound(),
            "Leaving Ae's fixed warning circle must dodge the warp instead of being tracked");
        combat.update(.4f, player, plan);
        check(ae.x == ae.homeX && ae.y == ae.homeY && ae.warpCooldown == 7f,
            "Ae must return home and allow a longer pause before another warp");
        combat.update(4f, player, plan);
        check(ae.warpWindup == 0 && ae.warpCooldown > 0,
            "Ae must not resume the old rapid warp cadence");

        player.x = markedX;
        player.y = markedY;
        ae.warpCooldown = 0;
        combat.update(.01f, player, plan);
        combat.update(.4f, player, plan);
        check(player.hp == 100 && !player.isBound(), "Ae must respect the entire warning duration");
        combat.update(.41f, player, plan);
        check(player.hp == 86 && !player.isBound(),
            "Staying in Ae's second warp must take the reduced 14 damage");
        combat.update(.4f, player, plan);
        player.invulnerable = 0;
        ae.warpCooldown = 0;
        combat.update(.01f, player, plan);
        combat.update(.81f, player, plan);
        check(player.boundTime == 1f && player.hp == 86,
            "Ae's alternating bind must last only one second without extra damage");
        combat.update(.4f, player, plan);

        // Even a dash stopped by furniture can avoid the impact while still inside the circle.
        player.setObstacles(combat.obstacles);
        combat.obstacles.tables.add(new RoomObstacles.Table(0, markedX + 75, markedY));
        for (int strike = 0; strike < 2; strike++) {
            player.x = markedX;
            player.y = markedY;
            player.boundTime = player.bindImmunity = player.invulnerable = player.dashTimer = 0;
            ae.warpStrikes = strike;
            ae.warpCooldown = 0;
            combat.update(.01f, player, plan);
            combat.update(.7f, player, plan);
            check(player.dash(1, 0) && Math.abs(player.x - markedX) < 75,
                "The cover fixture must stop the dash inside Ae's marked area");
            combat.update(.11f, player, plan);
            check(player.hp == 86 && !player.isBound(),
                "Dash invulnerability must protect against both Ae's hit and bind");
            combat.update(.4f, player, plan);
        }

        combat.obstacles.tables.clear();
        combat.shots.clear();
        ae.warper = false;
        ae.sp = ae.maxSp;
        ae.skillTimer = 0;
        player.x = 640;
        player.y = 360;
        combat.update(.01f, player, plan);
        check(combat.shots.size == 3 && ae.skillTimer == 1.1f,
            "Ae should retain his three-shot spread with a slower firing interval");
        for (CombatRoom.Shot shot : combat.shots) {
            float speed = (float)Math.sqrt(shot.vx * shot.vx + shot.vy * shot.vy);
            check(Math.abs(speed - 220) < .01f && shot.damage == 8 && shot.ignoresTables,
                "Ae's shots must be slower and weaker while still passing above tables");
        }
        combat.update(.7f, player, plan);
        check(combat.shots.size == 3, "Ae must leave a gap after his spread");
        combat.update(.41f, player, plan);
        check(combat.shots.size == 6, "Ae must resume firing after the new gap");

        combat.begin(RunDirector.Room.FINAL_EXAM, plan, 0);
        Enemy anchoredAe = combat.enemies.first();
        anchoredAe.warper = true;
        anchoredAe.warpCooldown = 0;
        anchoredAe.skillTimer = 0;
        combat.update(.01f, player, plan);
        check(anchoredAe.x == anchoredAe.homeX && anchoredAe.y == anchoredAe.homeY
            && anchoredAe.warpWindup == 0 && anchoredAe.skillTimer == 1.1f,
            "Final Exam Ae must use the easier firing cadence and remain on his cabinet");
    }

    private static void checkBossMinions() {
        CombatRoom combat = new CombatRoom(new Random(90));
        int[] minionCounts = {2, 3, 5};
        Set<Integer> bossKinds = new HashSet<Integer>();
        Set<Integer> monsterKinds = new HashSet<Integer>();
        for (int seed = 0; seed < 16; seed++) {
            RunPlan plan = new RunPlan(new Random(seed));
            for (int loop = 0; loop < 3; loop++) {
                for (int stage = 0; stage < 4; stage++) {
                    combat.begin(RunDirector.Room.BOSS, plan, 0);
                    check(combat.enemies.size == 1 + minionCounts[loop]
                        && combat.enemies.first().kind == plan.bossId(),
                        "Boss rooms must increase helpers gradually: two, three, then five");
                    bossKinds.add(plan.bossId());
                    if (plan.bossId() == RunPlan.AE_ID)
                        check(combat.enemies.first().warpCooldown == 6f,
                            "Ae's room entry must leave six seconds before warning, including later loops");
                    Set<Integer> kinds = new HashSet<Integer>();
                    for (Enemy enemy : combat.enemies) {
                        check(combat.obstacles.hitFraction(enemy.x, enemy.y, enemy.x, enemy.y, enemy.radius) < 0,
                            "Boss room enemies must spawn clear of table collision");
                        if (enemy.kind >= 0) continue;
                        if (loop == 0 && stage == 0)
                            check(enemy.kind == CombatRoom.BASIC || enemy.kind == CombatRoom.RANGED,
                                "The first boss must use only the two introduced monster types");
                        if (loop == 0)
                            check(enemy.kind != CombatRoom.WARPER,
                                "First-loop bosses must leave time to learn ordinary warpers separately");
                        kinds.add(enemy.kind);
                        monsterKinds.add(enemy.kind);
                        float dx = enemy.x - GameSpace.WIDTH / 2, dy = enemy.y - GameSpace.HEIGHT / 2;
                        check(dx * dx + dy * dy > 150 * 150,
                            "Minions should give the entering player room to react");
                        check(enemy.kind == CombatRoom.SHIELDED || enemy.armor == 0,
                            "Boss minions must retain their existing monster stats without extra summon armor");
                    }
                    check(kinds.size() == minionCounts[loop], "Initial boss helpers must use different monster types");
                    plan.completeStage();
                }
                plan.nextLoop();
            }
        }
        for (int bossId : RunPlan.bossIds())
            check(bossKinds.contains(bossId), "Every active boss must be covered by the shared room rule");
        check(bossKinds.size() == RunPlan.bossIds().length && monsterKinds.size() == 6,
            "The shared room rule must cover every boss and all six monster types");

        RunPlan plan = new RunPlan(new Random(91));
        PlayerCharacter player = new PlayerCharacter();
        player.invulnerable = 100;
        combat.begin(RunDirector.Room.BOSS, plan, 0);
        boolean summoningBoss = combat.enemies.first().summoner;
        for (Enemy enemy : combat.enemies)
            if (enemy.kind < 0) enemy.setHp(0);
        for (int frame = 0; frame < 100; frame++)
            check(!combat.update(.1f, player, plan)
                && (summoningBoss ? combat.enemies.size <= 5 : combat.enemies.size == 1),
                "Only the user's summoning bosses may replenish helpers, with a four-minion cap");

        combat.begin(RunDirector.Room.BOSS, plan, 0);
        combat.enemies.first().setHp(0);
        check(!combat.update(.01f, player, plan) && combat.enemies.size == 2,
            "Defeating only the boss must leave its existing minions to clear");
        for (Enemy enemy : combat.enemies) enemy.setHp(0);
        check(combat.update(.01f, player, plan), "The boss room clears after the boss and all minions fall");
        combat.begin(RunDirector.Room.FINAL_EXAM, plan, 0);
        check(combat.enemies.size == 2, "Final Exam retains its two anchored bosses without extra minions");
        combat.begin(RunDirector.Room.EX, plan, 0);
        check(combat.enemies.size == 1, "EX retains its single special enemy without extra minions");
    }

    private static void checkFirstExProgression() {
        for (int seed = 0; seed < 16; seed++) {
            PlayerCharacter player = new PlayerCharacter();
            CombatRoom combat = new CombatRoom(new Random(seed + 120));
            RunDirector run = new RunDirector(new Random(seed));
            run.startObstaclesTest(player, combat);
            for (int stage = 0; stage < 4; stage++) {
                check(run.room == RunDirector.Room.MOBS && run.plan.loop() == 1
                    && combat.enemies.size == 4 + stage,
                    "The first route to EX must use lighter monster rooms");
                player.hp = 40;
                for (Enemy enemy : combat.enemies) enemy.setHp(0);
                check(combat.update(.01f, player, run.plan), "The defeated monster room must clear");
                run.finishRoom(player);
                check(player.hp == 52 && run.nextRoom == RunDirector.Room.BOSS,
                    "Clearing monsters must restore 12 HP before the boss");
                run.continueAfterRoom(player, combat);
                check(run.room == RunDirector.Room.BOSS && combat.enemies.size == 3,
                    "Each first-loop boss must start with only two helpers");
                player.hp = 40;
                for (Enemy enemy : combat.enemies) enemy.setHp(0);
                check(combat.update(.01f, player, run.plan), "The defeated boss room must clear");
                run.finishRoom(player);
                check(player.hp == 75 && run.phase == RunDirector.Phase.UPGRADE,
                    "A boss victory must restore 35 HP and offer an upgrade");
                run.chooseUpgrade(0, player, combat);
                check(run.phase == RunDirector.Phase.FIGHT && run.room != RunDirector.Room.FINAL_EXAM,
                    "A queued final exam must not interrupt the route to the first EX");
            }
            check(run.room == RunDirector.Room.EX && run.plan.loop() == 1
                && combat.enemies.size == 1 && player.lanTether && player.notebookHeatBeam,
                "Four ordinary stages must reach the first EX with both EX techniques enabled");
            for (Enemy enemy : combat.enemies) enemy.setHp(0);
            if (run.exId == 0) {
                check(!combat.update(.01f, player, run.plan), "The first Cat phase must not end the EX route");
                combat.enemies.first().setHp(0);
            }
            check(combat.update(.01f, player, run.plan), "The first EX must be clearable");
            run.finishRoom(player);
            check(player.hp <= player.maxHp && !player.lanTether && !player.notebookHeatBeam,
                "The first EX reward must respect max HP and end EX-only techniques");
            run.continueAfterRoom(player, combat);
            clearFinalExamIfPending(run, player, combat);
            check(run.room == RunDirector.Room.MOBS && run.plan.loop() == 2,
                "The first EX must resume the second loop without an extra year or duplicate EX");
        }
        PlayerCharacter healthy = new PlayerCharacter();
        RunDirector run = new RunDirector(new Random(140));
        run.room = RunDirector.Room.MOBS;
        run.finishRoom(healthy);
        run.room = RunDirector.Room.BOSS;
        run.finishRoom(healthy);
        check(healthy.hp == healthy.maxHp, "Room healing must never exceed maximum HP");

        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(142));
        RunDirector earlyExam = new RunDirector(new Random(143));
        earlyExam.plan.completeStage();
        earlyExam.plan.completeStage();
        earlyExam.plan.recordBossDefeat(RunPlan.AE_ID);
        earlyExam.plan.recordBossDefeat(RunPlan.DOME_ID);
        earlyExam.room = RunDirector.Room.BOSS;
        earlyExam.finishRoom(player);
        earlyExam.chooseUpgrade(0, player, combat);
        check(earlyExam.room == RunDirector.Room.MOBS && earlyExam.phase == RunDirector.Phase.FIGHT,
            "Qualifying for Final Exam at stage three must still allow the fourth ordinary stage");
        earlyExam.room = RunDirector.Room.BOSS;
        earlyExam.finishRoom(player);
        earlyExam.chooseUpgrade(0, player, combat);
        check(earlyExam.room == RunDirector.Room.EX && earlyExam.plan.loop() == 1,
            "A deferred early Final Exam must leave the first EX accessible after four bosses");
        earlyExam.finishRoom(player);
        check(earlyExam.nextRoom == RunDirector.Room.FINAL_EXAM,
            "The deferred two-boss exam must remain queued after the first EX");
        earlyExam.continueAfterRoom(player, combat);
        check(earlyExam.phase == RunDirector.Phase.CUTSCENE,
            "The deferred exam must retain its black cutscene");
        clearFinalExamIfPending(earlyExam, player, combat);
        check(earlyExam.plan.loop() == 2 && earlyExam.room == RunDirector.Room.MOBS,
            "Finishing the deferred exam must advance to loop two exactly once");
    }

    private static void checkUpgrades() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(30));
        RunPlan plan = new RunPlan(new Random(31));
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        Upgrade.LAN_DAMAGE.apply(player);
        Upgrade.NOTEBOOK_DAMAGE.apply(player);
        Upgrade.ATTACK_SPEED.apply(player);
        Upgrade.MOVE_SPEED.apply(player);
        Upgrade.POWER_BALL_DAMAGE.apply(player);
        Upgrade.LAN_SLOW.apply(player);
        Upgrade.LAN_AOE.apply(player);
        Upgrade.NOTEBOOK_POISON.apply(player);
        Upgrade.NOTEBOOK_SPLASH.apply(player);
        check(!Upgrade.LAN_SLOW.available(player) && !Upgrade.NOTEBOOK_POISON.available(player),
            "One-time weapon effects must leave the upgrade pool after selection");
        check(player.physicalBonus == 7 && player.notebookBonus == 6
            && player.magicalBonus == 0
            && player.speedBonus == 20 && player.attackSpeedBonus > 0
            && player.skillDamageBonus == 10,
            "Stat upgrades must be applied to the student");

        Enemy lanTarget = GameRulesCheck.fixture(player.x + 150, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        combat.enemies.add(lanTarget);
        player.attack(combat);
        check(lanTarget.getHp() == 66 && lanTarget.slowTime > 0
            && player.attackTimer < PlayerCharacter.LAN_ATTACK_INTERVAL,
            "A wide LAN sweep must reach farther, hit harder, and slow its target");

        combat.enemies.clear();
        Enemy notebookTarget = GameRulesCheck.fixture(760, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        Enemy nearby = GameRulesCheck.fixture(810, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        notebookTarget.sp = nearby.sp = 0;
        combat.enemies.add(notebookTarget);
        combat.enemies.add(nearby);
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        player.lockAttackTarget(combat, notebookTarget.x, notebookTarget.y);
        player.attack(combat);
        check(combat.shots.first().poisonous && combat.shots.first().splash,
            "Notebook upgrades must attach to the thrown shot");
        combat.update(.23f, player, plan);
        check(notebookTarget.getHp() < 100 && nearby.getHp() < 100
            && notebookTarget.poisonTime > 0 && nearby.poisonTime > 0
            && combat.splashTime > 0,
            "Notebook splash must damage and poison nearby monsters");
        float hpAfterHit = notebookTarget.getHp();
        combat.update(1f, player, plan);
        check(notebookTarget.getHp() < hpAfterHit,
            "Poison must keep damaging monsters after the notebook hit");
        player.attackSpeedBonus = 1.8f;
        player.speedBonus = 160;
        int remaining = 0;
        for (Upgrade upgrade : Upgrade.values())
            if (upgrade.available(player)) remaining++;
        check(remaining >= 3,
            "Late loops must still offer three upgrades after unlocks and speed caps");
    }

    private static void checkUpgradeReroll() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(160));
        RunDirector run = new RunDirector(new Random(161));
        check(!run.rerollUpgrades(player), "Reroll must be unavailable before an upgrade screen");
        run.room = RunDirector.Room.BOSS;
        run.finishRoom(player);
        Set<Upgrade> original = new HashSet<Upgrade>();
        for (Upgrade upgrade : run.offeredUpgrades) original.add(upgrade);
        check(run.upgradeRerollsRemaining() == 1 && run.rerollUpgrades(player),
            "Each boss reward must grant one reroll");
        Set<Upgrade> rerolled = new HashSet<Upgrade>();
        for (Upgrade upgrade : run.offeredUpgrades) {
            check(upgrade.available(player) && !original.contains(upgrade),
                "Reroll must prefer available upgrades outside the previous choices");
            rerolled.add(upgrade);
        }
        check(rerolled.size() == 3 && run.upgradeRerollsRemaining() == 0
            && !run.rerollUpgrades(player), "Repeated rerolls must not replace the choices again");
        for (Upgrade upgrade : run.offeredUpgrades)
            check(rerolled.contains(upgrade), "An exhausted reroll must preserve the offered rewards");
        run.chooseUpgrade(0, player, combat);
        check(!run.rerollUpgrades(player), "Reroll must be unavailable during combat");
        run.room = RunDirector.Room.BOSS;
        run.finishRoom(player);
        check(run.upgradeRerollsRemaining() == 1, "The next boss must restore one reroll");
        player.lanSlow = player.lanAoE = player.notebookPoison = player.notebookSplash = true;
        player.attackSpeedBonus = 1.8f;
        player.speedBonus = 160;
        run.offeredUpgrades[0] = Upgrade.LAN_DAMAGE;
        run.offeredUpgrades[1] = Upgrade.NOTEBOOK_DAMAGE;
        run.offeredUpgrades[2] = Upgrade.POWER_BALL_DAMAGE;
        check(!run.rerollUpgrades(player) && run.upgradeRerollsRemaining() == 1,
            "With only three available upgrades, reroll must not consume a charge or repeat rewards");
    }

    private static void checkRoomObstacles() {
        checkRoomObstacles(RunDirector.Room.MOBS);
        checkRoomObstacles(RunDirector.Room.BOSS);
    }

    private static void checkRoomObstacles(RunDirector.Room room) {
        RoomObstacles obstacles = new RoomObstacles();
        // Fixed geometry isolates collision checks from the room's random layout.
        obstacles.tables.add(new RoomObstacles.Table(0, 365, 300));
        obstacles.tables.add(new RoomObstacles.Table(1, 640, 435));
        obstacles.tables.add(new RoomObstacles.Table(3, 940, 290));
        check(obstacles.tables.size == 3, room + " should have three separate tables");
        RoomObstacles.Table table = obstacles.tables.first();
        float left = table.solid.x - 17;
        PlayerCharacter player = new PlayerCharacter();
        player.startRoom(false);
        player.setObstacles(obstacles);
        player.x = left - 40;
        player.y = table.y;
        player.moveBy(150, 0);
        check(player.x < left && player.x > left - 1,
            "Walking must stop at the table instead of crossing it");
        player.moveBy(0, 80);
        check(player.y == table.y + 80, "The player should slide along a table edge");
        player.x = left - 35;
        player.y = table.y;
        check(player.dash(1, 0) && player.x < left && player.invulnerable > 0,
            "Dash must stop before the table while retaining its invulnerability");
        player.dashTimer = 0;
        player.x = left - 20;
        player.y = table.solid.y - 17 - 20;
        check(player.dash(.7071f, .7071f)
            && obstacles.hitFraction(player.x, player.y, player.x, player.y, 17) < 0,
            "A diagonal dash must not cut through a table corner");

        Enemy walker = GameRulesCheck.fixture(270, table.y, 17,
            100, 999, 80, 0, CombatRoom.BASIC);
        for (int i = 0; i < 300; i++) {
            obstacles.moveToward(walker, 460, table.y, 2, 0);
            check(obstacles.hitFraction(walker.x, walker.y, walker.x, walker.y, walker.radius) < 0,
                "A walking monster must never enter the table while taking a detour");
        }
        check(Math.abs(walker.x - 460) < 3 && Math.abs(walker.y - table.y) < 3,
            "A monster should reach the other side of a table without getting stuck");
        for (int i = 0; i < 900; i++) obstacles.moveToward(walker, 1050, 435, 2, 0);
        check(Math.abs(walker.x - 1050) < 3 && Math.abs(walker.y - 435) < 3,
            "Routing should also work across multiple tables");
        walker.x = table.x;
        walker.y = table.y;
        obstacles.resolveEnemy(walker);
        check(obstacles.hitFraction(walker.x, walker.y, walker.x, walker.y, walker.radius) < 0,
            "Spawns and knockback inside furniture must be moved to a free position");
        RoomObstacles edge = new RoomObstacles();
        edge.tables.add(new RoomObstacles.Table(1, 640, 445));
        for (float radius : new float[]{17, 36}) {
            Enemy trapped = GameRulesCheck.fixture(640, GameSpace.WALK_TOP - radius,
                radius, 100, 999, 0, 0, CombatRoom.BASIC);
            edge.resolveEnemy(trapped);
            check(edge.hitFraction(trapped.x, trapped.y, trapped.x, trapped.y, radius) < 0,
                "A table near the top wall must eject monsters and bosses into the room, not into the wall");
        }

        CombatRoom combat = new CombatRoom(new Random(86));
        RunPlan plan = new RunPlan(new Random(87));
        combat.begin(room, plan, 0);
        combat.obstacles.tables.clear();
        combat.obstacles.tables.addAll(obstacles.tables);
        combat.enemies.clear();
        Enemy target = GameRulesCheck.fixture(460, table.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        target.sp = 0;
        combat.enemies.add(target);
        combat.fireFriendly(250, table.y, 1000, 0, 20, target).notebook = true;
        combat.update(.3f, player, plan);
        check(target.getHp() == 100 && combat.shots.size == 0 && combat.notebookImpacts.size == 1,
            "A fast notebook must break against cover without hitting the enemy behind it");
        player.x = 460;
        player.y = table.y;
        player.invulnerable = 0;
        float hp = player.hp;
        combat.shots.add(new CombatRoom.Shot(250, table.y, 1000, 0, 9, 15, 2, false));
        combat.update(.3f, player, plan);
        check(player.hp == hp && combat.shots.size == 0,
            "The same table must also protect the player from hostile projectiles");
        combat.firePowerBall(250, table.y, 1000, 0, 50, target);
        combat.update(.3f, player, plan);
        check(combat.shots.size == 0 && combat.blastTime > 0 && combat.blastX < table.solid.x,
            "PowerBall should explode at the front of cover instead of crossing it");
        target.x = table.solid.x - target.radius - 1;
        target.setHp(100);
        combat.fireFriendly(target.x - 30, table.y, 300, 0, 20, target);
        combat.update(.1f, player, plan);
        check(target.getHp() == 80, "An enemy standing in front of cover must still take the hit");
        combat.firePowerBall(250, table.y, 1000, 0, 30, target);
        combat.update(.3f, player, plan);
        check(target.getHp() > 0 && target.x < table.solid.x - target.radius,
            "PowerBall knockback must stop an enemy before the table instead of pushing through");
        combat.begin(RunDirector.Room.FINAL_EXAM, plan, 0);
        check(combat.obstacles.tables.size == 0,
            "Final Exam must keep its existing unobstructed horizontal lane");
        player.startRoom(true);
        player.moveBy(100, 100);
        check(player.y == GameSpace.SERVER_PLAYER_Y,
            "Leaving a table room must retain the Final Exam movement rule");
    }

    private static void checkRandomTables() {
        Set<String> layouts = new HashSet<String>();
        Set<Integer> art = new HashSet<Integer>();
        boolean upper = false, lower = false;
        for (RunDirector.Room room : new RunDirector.Room[]{RunDirector.Room.MOBS, RunDirector.Room.BOSS}) {
            for (int seed = 0; seed < 50; seed++) {
                CombatRoom combat = new CombatRoom(new Random(seed));
                CombatRoom repeat = new CombatRoom(new Random(seed));
                RunPlan plan = new RunPlan(new Random(101));
                combat.begin(room, plan, 0);
                repeat.begin(room, plan, 0);
                RoomObstacles obstacles = combat.obstacles;
                check(obstacles.tables.size == 3, "Random rooms must keep three tables");
                check(obstacles.hitFraction(640, 360, 640, 360, 30) < 0,
                    "Every random layout must leave the player's room entry clear");
                StringBuilder layout = new StringBuilder();
                for (int i = 0; i < obstacles.tables.size; i++) {
                    RoomObstacles.Table table = obstacles.tables.get(i);
                    RoomObstacles.Table same = repeat.obstacles.tables.get(i);
                    check(table.art == same.art && table.x == same.x && table.y == same.y,
                        "Repeating a room seed must reproduce its layout");
                    check(table.x - table.width / 2 > GameSpace.LEFT
                        && table.x + table.width / 2 < GameSpace.RIGHT
                        && table.y - table.height / 2 > GameSpace.BOTTOM
                        && table.y + table.height / 2 < GameSpace.WALK_TOP,
                        "Random furniture must stay inside the floor");
                    if (i > 0) {
                        RoomObstacles.Table previous = obstacles.tables.get(i - 1);
                        check(table.solid.x - previous.solid.x - previous.solid.width > 70,
                            "Random table columns must leave a wide passage");
                    }
                    art.add(table.art);
                    layout.append(table.art).append(':').append(table.x).append(',').append(table.y).append(';');
                }
                layouts.add(layout.toString());
                upper |= obstacles.tables.get(1).y > 360;
                lower |= obstacles.tables.get(1).y < 360;
                for (Enemy enemy : combat.enemies)
                    check(obstacles.hitFraction(enemy.x, enemy.y, enemy.x, enemy.y, enemy.radius) < 0,
                        "Enemies must enter each random room outside furniture");

                Enemy walker = GameRulesCheck.fixture(120, 360, 17,
                    100, 999, 80, 0, CombatRoom.BASIC);
                for (float destination : new float[]{1160, 640}) {
                    for (int step = 0; step < 800; step++) {
                        obstacles.moveToward(walker, destination, 360, 2, 0);
                        check(obstacles.hitFraction(walker.x, walker.y, walker.x, walker.y, 17) < 0,
                            "A random layout's detour must stay outside every table");
                    }
                    check(Math.abs(walker.x - destination) < 3 && Math.abs(walker.y - 360) < 3,
                        "Random furniture must allow crossing the room and returning to its center");
                }
                combat.begin(RunDirector.Room.EX, plan, 0);
                check(obstacles.tables.size == 0, "EX must clear the previous room's random tables");
                combat.begin(RunDirector.Room.FINAL_EXAM, plan, 0);
                check(obstacles.tables.size == 0, "Final Exam must retain its clear horizontal lane");
            }
        }
        check(layouts.size() > 45 && art.size() == 3 && upper && lower,
            "Room seeds must vary table positions, finished art and the middle table's side");
    }

    private static void checkBossShotsAcrossTables() {
        for (int pattern = 0; pattern < 6; pattern++) {
            CombatRoom combat = new CombatRoom(new Random(102));
            RunPlan plan = new RunPlan(new Random(103));
            PlayerCharacter player = new PlayerCharacter();
            player.x = 500;
            player.y = 300;
            RoomObstacles.Table table = new RoomObstacles.Table(0, 365, 300);
            combat.obstacles.tables.add(table);
            int kind = pattern == 1 ? 1 : pattern == 2 ? 2 : pattern == 3 ? 7 : 0;
            Enemy boss = GameRulesCheck.fixture(250, 300, 36, 200, 100, 0, 12, kind);
            boss.sp = 100;
            if (boss instanceof SatiBoss) ((SatiBoss) boss).satiCooldown = 100;
            boss.artillery = pattern == 2;
            boss.waveCaster = pattern == 3;
            combat.enemies.add(boss);
            if (pattern == 4) {
                ((SatiBoss) boss).satiCharge = .01f;
                ((SatiBoss) boss).satiAimX = player.x;
                ((SatiBoss) boss).satiAimY = player.y;
            }
            if (pattern == 5) {
                boss.armor = 100;
                boss.reflector = true;
                combat.damageNearby(player.x, player.y, 300, 1);
            } else {
                boss.updateBehavior(combat, .01f, player, plan);
            }
            check(combat.shots.size > 0, "Each boss projectile pattern must actually fire");
            for (CombatRoom.Shot shot : combat.shots)
                check(shot.ignoresTables, "Every projectile from a boss must travel over furniture");
            boss.skillTimer = 100;
            boss.sp = 0;
            float hp = player.hp;
            boolean crossed = false;
            for (int frame = 0; frame < 200 && player.hp == hp; frame++) {
                combat.update(.01f, player, plan);
                for (CombatRoom.Shot shot : combat.shots)
                    crossed |= shot.x > table.solid.x + table.solid.width + shot.radius;
            }
            check(crossed && player.hp < hp && boss.x == boss.homeX && boss.y == boss.homeY,
                "A stationary boss must hit beyond cover: pattern=" + pattern
                    + ", crossed=" + crossed + ", hp=" + player.hp + "/" + hp
                    + ", boss=" + boss.x + "," + boss.y);
        }
    }

    /** These order-dependent rules must survive changes to the combat components. */
    private static void checkCombatFrameOrder() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(180));
        RunPlan plan = new RunPlan(new Random(181));

        Enemy shield = GameRulesCheck.fixture(800, 400, 17,
            100, 100, 0, 20, RunPlan.SATI_ID);
        shield.armor = 10;
        shield.reflector = true;
        shield.stunTime = 10;
        combat.enemies.add(shield);
        combat.fireFriendly(shield.x, shield.y, 0, 0, 25, null);
        combat.update(.1f, player, plan);
        check(shield.armor == 0 && shield.getHp() == 85 && combat.shots.size == 1,
            "A reflected hit must spill excess armor damage into HP and consume the incoming shot");
        CombatRoom.Shot reflected = combat.shots.first();
        check(reflected.reflected && reflected.ignoresTables && reflected.animationTime == 0
            && reflected.x == shield.x && reflected.y == shield.y,
            "Reflection created during shot collision must wait until the next frame to move");
        combat.update(.1f, player, plan);
        check(reflected.animationTime > 0 && reflected.life < 2.5f,
            "The reflected projectile must advance on the next frame");

        combat.enemies.clear();
        combat.shots.clear();
        Enemy poisoned = GameRulesCheck.fixture(900, 400, 17,
            1, 100, 0, 20, CombatRoom.RANGED);
        poisoned.sp = 100;
        poisoned.poisonTime = .1f;
        poisoned.poisonDps = 20;
        combat.enemies.add(poisoned);
        player.sp = 0;
        check(combat.update(.1f, player, plan) && combat.shots.size == 0 && player.sp == 5,
            "Poison death must prevent a final enemy attack and grant SP exactly once");
        combat.update(.1f, player, plan);
        check(player.sp == 5, "A removed enemy must not grant SP again");

        Enemy sword = GameRulesCheck.fixture(player.x + 65, player.y,
            34, 100, 100, 0, 20, CombatRoom.INSECURE_SWORD);
        sword.armor = 150;
        sword.skillTimer = ((ExBoss) sword).exObjectTimer = 100;
        combat.enemies.add(sword);
        combat.exObjects.add(new CombatRoom.ExObject(player.x, player.y, 11, .005f, CombatRoom.NOTE));
        combat.exObjects.add(new CombatRoom.ExObject(1000, 200, 8, .005f, CombatRoom.DUST));
        combat.update(.01f, player, plan);
        check(combat.exObjects.size == 0 && combat.carriedNotes == 0
            && ((InsecureSwordBoss) sword).exConfidence == 25 && sword.armor == 95 && ((ExBoss) sword).exState == 2,
            "A touching note must be collected before expiry and applied to the sword in the same frame");

        int[] kinds = {CombatRoom.BASIC, CombatRoom.LIQUID_CAT,
            CombatRoom.LIQUID_CAT, CombatRoom.INSECURE_SWORD};
        int[] states = {0, 0, 1, 2};
        float[] expectedDamage = {20, 20, 11, 30};
        for (int i = 0; i < kinds.length; i++) {
            combat.enemies.clear();
            Enemy target = GameRulesCheck.fixture(900, 400, 17,
                100, 100, 0, 0, kinds[i]);
            if (target instanceof ExBoss) ((ExBoss) target).exState = states[i];
            combat.enemies.add(target);
            combat.damageNearby(target.x, target.y, 1, 20);
            check(Math.abs(target.getHp() - (100 - expectedDamage[i])) < .001f,
                "Ordinary enemies, both cat forms and the exposed sword must retain their damage rules");
        }
    }

    private static void checkNotebookImpacts() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(82));
        RunPlan plan = new RunPlan(new Random(83));
        Enemy target = GameRulesCheck.fixture(760, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        Enemy nearby = GameRulesCheck.fixture(810, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        target.sp = nearby.sp = 0;
        combat.enemies.add(target);
        combat.enemies.add(nearby);
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        player.lockAttackTarget(combat, target.x, target.y);
        player.attack(combat);
        CombatRoom.Shot notebook = combat.shots.first();
        check(notebook.notebook && !notebook.powerBall,
            "Notebook attacks must select the notebook art without affecting PowerBall");
        combat.update(.02f, player, plan);
        check(notebook.animationTime > 0 && combat.notebookImpacts.size == 0,
            "A flying notebook should animate without creating an early impact");
        combat.update(.20f, player, plan);
        check(combat.shots.size == 0 && combat.notebookImpacts.size == 1
            && target.getHp() == 80 && nearby.getHp() == 100,
            "Notebook impact art should accompany one hit without adding splash damage");
        CombatRoom.NotebookImpact firstImpact = combat.notebookImpacts.first();
        combat.update(.10f, player, plan);
        player.attack(combat);
        combat.update(.20f, player, plan);
        check(combat.notebookImpacts.size == 2 && firstImpact.time > 0
            && combat.notebookImpacts.peek().time == 0 && target.getHp() == 60,
            "Rapid notebook hits should retain independent impact animations");
        combat.update(.10f, player, plan);
        check(combat.notebookImpacts.size == 1 && target.getHp() == 60,
            "An expired impact should disappear without damaging the enemy again");
        combat.update(CombatRoom.NOTEBOOK_IMPACT_DURATION, player, plan);
        check(combat.notebookImpacts.size == 0, "All notebook impacts should expire");
        player.attack(combat);
        combat.update(.20f, player, plan);
        check(combat.notebookImpacts.size == 1, "Another hit should create a fresh impact");
        combat.begin(RunDirector.Room.MOBS, plan, 0);
        check(combat.notebookImpacts.size == 0 && combat.shots.size == 0,
            "Changing rooms must clear notebook projectiles and impact art");
    }

    private static void checkWeaponAbilityExtension() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(55));
        final int[] calls = {0, 0};
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        player.addWeaponAbility(PlayerCharacter.Weapon.MAGICAL, new WeaponAbility() {
            @Override
            public void modify(PlayerCharacter owner, WeaponAttackSpec attack) {
                attack.damage += 11;
                calls[0]++;
            }

            @Override
            public void afterAttack(PlayerCharacter owner, CombatRoom room) {
                calls[1]++;
            }
        });
        player.attack(combat);
        check(combat.shots.size == 1 && combat.shots.first().damage == 31
            && calls[0] == 1 && calls[1] == 1,
            "A notebook ability should modify its attack and run after firing");
        player.sp = 0;
        player.attack(combat);
        check(combat.shots.size == 1 && calls[1] == 1,
            "An ability must not run after an attack that cannot pay its SP cost");
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        Enemy nearby = GameRulesCheck.fixture(player.x + 100, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        combat.enemies.add(nearby);
        player.attack(combat);
        check(nearby.getHp() == 73 && calls[0] == 2 && calls[1] == 1,
            "A notebook ability must not change the LAN attack");
    }

    private static void checkLanWave() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(150));
        RunPlan plan = new RunPlan(new Random(151));
        player.x = 300; player.y = 300;
        check(!player.tryLanWave(combat), "An unarmed student must not cast LAN Wave");
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        check(!player.tryLanWave(combat), "Notebook must not cast the LAN weapon skill");
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        player.physicalBonus = 12;
        player.sp = 0;
        Enemy target = GameRulesCheck.fixture(430, 300, 17, 100, 0, 0, 0, CombatRoom.BASIC);
        target.skillTimer = 100;
        combat.enemies.add(target);
        check(player.tryLanWave(combat) && player.sp == 0 && combat.shots.size == 1,
            "LAN Wave must be available without SP and launch one projectile");
        check(!player.tryLanWave(combat) && combat.shots.size == 1,
            "Repeated requests during cooldown must not launch extra waves");
        combat.update(.15f, player, plan);
        check(target.getHp() == 50 && combat.shots.size == 0,
            "A traveling LAN Wave must hit once with the physical upgrade bonus");

        target.x = 500;
        combat.obstacles.tables.add(new RoomObstacles.Table(0, 400, 300));
        player.lanWaveCooldown = 0;
        player.tryLanWave(combat);
        combat.update(.3f, player, plan);
        check(target.getHp() == 50 && combat.shots.size == 0,
            "Tables must stop LAN Wave before it damages an enemy behind cover");
        combat.obstacles.tables.clear();
        combat.iceWall.raise(player.x, player.y, 1, 0);
        target.x = 430;
        player.lanWaveCooldown = 0;
        player.tryLanWave(combat);
        combat.update(.15f, player, plan);
        check(target.getHp() == 0 && combat.shots.size == 0,
            "Friendly LAN Wave must pass through Ice Wall and damage an enemy beyond it");

        player.lanWaveCooldown = 0;
        player.skillPlaying = true;
        check(!player.tryLanWave(combat), "PowerBall casting must lock the LAN weapon skill too");
        player.skillPlaying = false;
        player.hp = 0;
        check(!player.tryLanWave(combat), "A defeated student must not launch LAN Wave");
        player.lanWaveCooldown = 1;
        player.startRoom(false);
        check(player.lanWaveCooldown == 0, "Entering a room must reset LAN Wave cooldown");
    }

    private static void checkTetherAndHeatBeam() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(40));
        RunPlan plan = new RunPlan(new Random(41));
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        player.setExTechniques(true);
        check(player.lanTether && player.notebookHeatBeam,
            "Both special weapon techniques should activate for EX combat");
        Enemy first = GameRulesCheck.fixture(player.x + 110, player.y,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy second = GameRulesCheck.fixture(player.x + 130, player.y + 30,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy ajarn = GameRulesCheck.fixture(player.x + 100, player.y + 65,
            36, 250, 999, 0, 0, 0);
        combat.enemies.add(first);
        combat.enemies.add(second);
        combat.enemies.add(ajarn);
        float firstX = first.x, secondX = second.x, ajarnX = ajarn.x;
        player.attack(combat);
        check(first.x < firstX && second.x < secondX
            && first.tetherTime > 0 && second.tetherTime > 0
            && first.stunTime > 0 && second.stunTime > 0,
            "The LAN tether should gather and briefly hold ordinary monsters");
        check(ajarn.x == ajarnX && ajarn.tetherTime > 0,
            "Bosses should be marked for the combo without being dragged");

        combat.enemies.clear();
        Enemy marked = GameRulesCheck.fixture(760, 360,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy plain = GameRulesCheck.fixture(640, 360,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        marked.tetherTime = 1.7f;
        marked.sp = plain.sp = 0;
        combat.enemies.add(marked);
        combat.enemies.add(plain);
        combat.firePowerBall(700, 360, 0, 0, 40, null);
        combat.shots.first().life = 0;
        combat.update(.01f, player, plan);
        check(marked.getHp() < plain.getHp(),
            "PowerBall should deal bonus damage to a tethered target at equal distance");

        combat.enemies.clear();
        combat.shots.clear();
        Upgrade.NOTEBOOK_POISON.apply(player);
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        Enemy beamNear = GameRulesCheck.fixture(800, 360,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy beamFar = GameRulesCheck.fixture(950, 360,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy outside = GameRulesCheck.fixture(850, 480,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        combat.enemies.add(beamNear);
        combat.enemies.add(beamFar);
        combat.enemies.add(outside);
        player.lockAttackTarget(combat, beamNear.x, beamNear.y);
        for (int i = 0; i < 4; i++) player.attack(combat);
        check(player.notebookHeat == 100 && combat.beamTime == 0,
            "Four ordinary notebook throws should charge but not fire the beam");
        combat.shots.clear();
        player.attack(combat);
        check(player.notebookHeat == 0 && combat.beamTime > 0
            && combat.shots.size == 0,
            "The next notebook attack should spend full heat on a beam");
        check(beamNear.getHp() < 200 && beamFar.getHp() < 200 && outside.getHp() == 200
            && beamNear.poisonTime > 0 && beamFar.poisonTime > 0,
            "The beam should pierce targets on its path and inherit poison");
        player.setExTechniques(false);
        check(!player.lanTether && !player.notebookHeatBeam && player.notebookHeat == 0,
            "EX techniques and stored heat must reset after the encounter");
    }

    private static void checkExMechanics() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(50));
        RunPlan plan = new RunPlan(new Random(51));

        combat.begin(RunDirector.Room.EX, plan, 0);
        Enemy cat = combat.enemies.first();
        cat.skillTimer = 100;
        float idleX = cat.x, idleY = cat.y;
        combat.update(3.6f, player, plan);
        check(((ExBoss) cat).exState == 0 && cat.x == idleX && cat.y == idleY && !((LiquidCatBoss) cat).catMoving,
            "Phase one must remain idle instead of walking or transforming on a timer");
        player.sp = 0;
        combat.damageNearby(cat.x, cat.y, 1, cat.getHp() + 1000);
        check(!combat.update(.01f, player, plan) && player.sp == 0 && cat.getHp() == cat.getMaxHp(),
            "Lethal phase-one damage must refill phase two without clearing the room or rewarding a kill");
        check(((ExBoss) cat).exState == 1 && cat.radius == 16 && combat.exObjects.size == 0
            && ((LiquidCatBoss) cat).catTransformTime > 0,
            "The first defeat must start the compact phase's transformation");
        float transformX = cat.x, transformY = cat.y;
        float transformHp = cat.getHp();
        combat.fireNotebookBeam(cat.x - 100, cat.y, 1, 0, 500, true);
        combat.damageArc(cat.x - 10, cat.y, 1, 0, 100, 90, 500, true, true);
        combat.firePowerBall(cat.x, cat.y, 0, 0, 500, cat);
        combat.update(.01f, player, plan);
        check(cat.getHp() == transformHp && cat.poisonTime == 0 && cat.stunTime == 0 && cat.slowTime == 0,
            "Transformation must reject damage, poison, stun, slow and tether attacks");
        combat.update(((LiquidCatBoss) cat).catTransformTime, player, plan);
        check(cat.x == transformX && cat.y == transformY && combat.shots.size == 0,
            "Liquid Cat must not move or attack during its transformation");
        combat.update(.01f, player, plan);
        CombatRoom.ExObject slime = combat.exObjects.first();
        player.x = slime.x;
        player.y = slime.y;
        combat.update(.01f, player, plan);
        check(player.slowTime > 0, "Liquid Cat ooze should slow the player");
        player.x = cat.x - 180;
        player.y = cat.y;
        cat.skillTimer = 100;
        ((ExBoss) cat).exStateTime = 0;
        combat.update(.01f, player, plan);
        check(((LiquidCatBoss) cat).catDivideTime > 0 && combat.enemies.size == 1,
            "Divide must play its animation before spawning copies");
        combat.update(.36f, player, plan);
        check(((LiquidCatBoss) cat).catDivideTime > 0 && combat.enemies.size == 3 && combat.shots.size == 0,
            "The original must split into two actual enemies, not just fire a bullet");
        Enemy copy = combat.enemies.get(1);
        check(LiquidCatBoss.isCopy(copy) && copy.getMaxHp() < cat.getMaxHp(),
            "Copies must have their own smaller health bars");
        player.x = 200;
        combat.update(.30f, player, plan);
        check(combat.enemies.size == 3, "A divide animation must spawn its copies only once");
        ((ExBoss) copy).exStateTime = 0;
        combat.update(.01f, player, plan);
        combat.update(.36f, player, plan);
        check(combat.enemies.size == 3 && combat.shots.size == 1 && combat.shots.first().liquidCatBullet,
            "Copies may shoot but must never create more copies");
        copy.setHp(0);
        combat.update(.01f, player, plan);
        check(combat.enemies.size == 2, "A defeated copy must disappear without transforming");
        ((ExBoss) cat).exStateTime = 0;
        combat.update(.01f, player, plan);
        combat.update(.36f, player, plan);
        check(combat.enemies.size == 3, "Divide must replace only the missing copy");
        ((LiquidCatBoss) cat).catDivideTime = .31f;
        combat.update(.02f, player, plan);
        check(combat.enemies.size == 3 && ((ExBoss) cat).exState == 1,
            "The second phase must stay compact and enforce its two-copy cap");
        cat.setHp(0);
        check(combat.update(.01f, player, plan) && combat.enemies.size == 0,
            "The second defeat must remove the original and its copies in the same frame");

        combat.begin(RunDirector.Room.EX, plan, 0);
        cat = combat.enemies.first();
        cat.setHp(1);
        cat.poisonTime = .1f;
        cat.poisonDps = 100;
        check(!combat.update(.02f, player, plan) && ((ExBoss) cat).exState == 1 && cat.getHp() == cat.getMaxHp()
            && cat.poisonTime == 0,
            "Poison must trigger phase two once and must not carry damage through the transformation");

        player.x = GameSpace.WIDTH / 2;
        player.y = GameSpace.HEIGHT / 2;
        combat.begin(RunDirector.Room.EX, plan, 1);
        Enemy vacuum = combat.enemies.first();
        ((ExBoss) vacuum).exMeter = 100;
        ((AllergicVacuumBoss) vacuum).exStoredDust = 4;
        combat.update(.01f, player, plan);
        check(((ExBoss) vacuum).exState == 1,
            "A full allergy meter should start the sneeze warning");
        player.x = vacuum.x - 100;
        player.y = vacuum.y;
        float playerX = player.x;
        combat.update(.1f, player, plan);
        check(player.x == playerX,
            "Vacuum suction must stop while the sneeze is charging");
        combat.update(.8f, player, plan);
        int scatteredDust = 0;
        for (CombatRoom.ExObject object : combat.exObjects)
            if (object.kind == CombatRoom.DUST && object.ejectionTime > 0)
                scatteredDust++;
        check(((ExBoss) vacuum).exState == 2 && ((ExBoss) vacuum).exMeter == 0 && scatteredDust >= 4,
            "The sneeze should recoil and scatter the stored dust");

        player.x = GameSpace.WIDTH / 2;
        player.y = GameSpace.HEIGHT / 2;
        combat.begin(RunDirector.Room.EX, plan, 2);
        Enemy sword = combat.enemies.first();
        CombatRoom.ExObject note = combat.exObjects.first();
        player.x = note.x;
        player.y = note.y;
        combat.update(.01f, player, plan);
        check(combat.carriedNotes == 1,
            "The player should be able to collect a Post-it note");
        float armor = sword.armor;
        player.x = sword.x - 70;
        player.y = sword.y;
        combat.update(.01f, player, plan);
        check(combat.carriedNotes == 0 && ((InsecureSwordBoss) sword).exConfidence == 25
            && sword.armor < armor && ((InsecureSwordBoss) sword).swordAttackTime == 0,
            "Bringing a note to the sword should raise confidence and weaken armor");
        ((InsecureSwordBoss) sword).exConfidence = 100;
        ((ExBoss) sword).exState = 0;
        sword.skillTimer = 0;
        combat.update(.01f, player, plan);
        check(((ExBoss) sword).exState == 1 && ((ExBoss) sword).exAimX == player.x && ((InsecureSwordBoss) sword).swordAttackTime == 0,
            "A confident sword should warn before its heavy strike");
        float hpBeforeDodge = player.hp;
        player.x = 500;
        combat.update(.66f, player, plan);
        check(((ExBoss) sword).exState == 2 && player.hp == hpBeforeDodge && ((InsecureSwordBoss) sword).swordAttackTime > 0,
            "Moving away from the sword's marked point should dodge the slash");
        combat.update(.51f, player, plan);
        check(((ExBoss) sword).exState == 2 && ((InsecureSwordBoss) sword).swordAttackTime == 0,
            "The slash animation must finish while the sword remains open to attacks");

        ((ExBoss) sword).exState = 0;
        ((InsecureSwordBoss) sword).exConfidence = 0;
        sword.skillTimer = 0;
        player.x = sword.x - 70;
        // Deterministically exercise a whiff, independent of the preceding pickup RNG.
        combat.random.setSeed(0);
        combat.update(.01f, player, plan);
        check(((ExBoss) sword).exState == 3 && ((InsecureSwordBoss) sword).swordAttackTime > 0 && player.hp == hpBeforeDodge,
            "A low-confidence whiff must animate without damaging the nearby player");
        combat.carriedNotes = 1;
        combat.update(.01f, player, plan);
        check(((ExBoss) sword).exState == 2 && ((InsecureSwordBoss) sword).swordAttackTime == 0,
            "Applying a Post-it must interrupt the whiff animation");
    }

    private static void checkExTestEntry() {
        for (int exId = 0; exId < RunPlan.EX_MONSTERS.length; exId++) {
            PlayerCharacter player = new PlayerCharacter();
            CombatRoom combat = new CombatRoom(new Random(190 + exId));
            RunDirector run = new RunDirector(new Random(195 + exId));
            run.startExTest(player, combat, exId);
            check(run.exTest && run.phase == RunDirector.Phase.FIGHT
                && run.room == RunDirector.Room.EX && run.exId == exId && run.sceneFade == 0,
                "EX test must enter the selected fight immediately");
            check(combat.enemies.size == 1 && combat.enemies.first().kind == CombatRoom.LIQUID_CAT + exId,
                "EX test must spawn only the selected boss");
            check(player.hasBothWeapons() && player.activeWeapon() == PlayerCharacter.Weapon.MAGICAL
                && player.lanTether && player.notebookHeatBeam && player.hp == 100 && player.sp == 100,
                "EX test must prepare both weapons, full resources and EX-only techniques");
            check(combat.exObjects.size == (exId == 1 ? 6 : exId == 2 ? 3 : 0),
                "EX test must include the boss's actual dust or Post-it objects");
            combat.enemies.first().setHp(0);
            if (exId == 0) {
                check(!combat.update(.01f, player, run.plan), "The first Cat defeat must not finish EX");
                combat.enemies.first().setHp(0);
            }
            check(combat.update(.01f, player, run.plan), "EX test boss must be defeatable");
            run.finishRoom(player);
            check(run.phase == RunDirector.Phase.TRANSITION && !player.lanTether && !player.notebookHeatBeam,
                "EX test must use the real room-clear rules");
        }
        RunDirector invalid = new RunDirector(new Random(200));
        try {
            invalid.startExTest(new PlayerCharacter(), new CombatRoom(new Random(201)), 3);
            throw new AssertionError("An invalid EX id must be rejected");
        } catch (IllegalArgumentException expected) {
            check(!invalid.exTest && invalid.phase == RunDirector.Phase.START_SELECT,
                "Invalid EX selection must not change run state");
        }
    }

    private static void checkSatiSkill() {
        RunPlan plan = new RunPlan(new Random(21));
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(22));
        Enemy sati = GameRulesCheck.fixture(640, 520, 36,
            200, 72, 90, 12, 0);
        ((SatiBoss) sati).satiCooldown = .01f;
        combat.enemies.add(sati);
        combat.update(.02f, player, plan);
        check(((SatiBoss) sati).satiCharge == SatiBoss.CHARGE_DURATION
            && ((SatiBoss) sati).satiAimX == player.x && ((SatiBoss) sati).satiAimY == player.y,
            "Sati should lock a warning point before transforming the C++ element");
        float chargedX = sati.x, chargedY = sati.y;
        player.x = 850;
        combat.update(1.06f, player, plan);
        check(sati.x == chargedX && sati.y == chargedY,
            "Sati should hold position during the spear transformation");
        check(combat.shots.size > 0 && combat.shots.first().satiSpear
            && combat.shots.first().vx < 0,
            "Sati's spear should fire toward the warned position after charging");
    }

    private static void checkPowerBallKnockback() {
        RunPlan plan = new RunPlan(new Random(60));
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(61));
        Enemy monster = GameRulesCheck.fixture(740, 360, 17,
            200, 999, 0, 0, CombatRoom.BASIC);
        Enemy boss = GameRulesCheck.fixture(770, 420, 36,
            200, 50, 0, 0, RunPlan.KEN_ID);
        combat.enemies.add(monster);
        combat.enemies.add(boss);
        combat.firePowerBall(700, 360, 0, 0, 42, null);
        combat.shots.first().life = 0;
        combat.update(.01f, player, plan);
        check(monster.x > 740 && boss.x > 770,
            "PowerBall should knock both monsters and resistant bosses away from its blast");
        check(monster.x - 740 > boss.x - 770,
            "Ordinary monsters should be pushed farther than bosses");
    }

    private static void checkDomeSkills() {
        RunPlan plan = new RunPlan(new Random(80));
        CombatRoom combat = new CombatRoom(new Random(81));
        PlayerCharacter player = new PlayerCharacter();
        player.x = 600;
        player.y = 360;
        player.invulnerable = 0;
        Enemy boss = GameRulesCheck.fixture(1100, 425, 36,
            500, 72, 0, 12, RunPlan.DOME_ID);
        boss.sp = boss.maxSp;
        ((DomeBoss) boss).dome().remaining = 0;
        combat.enemies.add(boss);

        float hp = player.hp;
        combat.update(.01f, player, plan);
        check(((DomeBoss) boss).dome().phase == DomeBoss.Phase.LIGHTNING_CHARGE
            && player.hp == hp && combat.shots.size == 0,
            "Dome should warn before lightning without firing generic projectiles");
        float targetX = player.x, targetY = player.y;
        player.x += DomeBoss.LIGHTNING_RADIUS + 40;
        combat.update(DomeBoss.LIGHTNING_CHARGE + .01f, player, plan);
        check(((DomeBoss) boss).dome().aimX == targetX && ((DomeBoss) boss).dome().aimY == targetY
            && player.hp == hp,
            "Lightning should keep its marked point so moving away avoids impact");
        combat.update(DomeBoss.LIGHTNING_STRIKE + .01f, player, plan);
        combat.update(DomeBoss.RECOVERY + .01f, player, plan);
        player.x = targetX;
        combat.update(2.5f, player, plan);
        check(((DomeBoss) boss).dome().phase == DomeBoss.Phase.LASER_CHARGE
            && player.hp == hp,
            "Dome should alternate to a charged laser after lightning and recovery");
        player.y = 150;
        combat.update(DomeBoss.LASER_CHARGE + .01f, player, plan);
        check(player.hp == hp && ((DomeBoss) boss).dome().aimY == targetY,
            "The normal laser should aim at the marked position and allow a dodge");
        player.y = targetY;
        combat.update(.01f, player, plan);
        check(player.hp < hp,
            "Entering the active aimed laser should hurt the player");
        hp = player.hp;
        combat.update(.01f, player, plan);
        check(player.hp == hp, "An active laser should respect hit invulnerability");
        combat.update(DomeBoss.LASER_FIRE, player, plan);
        combat.update(DomeBoss.RECOVERY + .01f, player, plan);
        combat.update(2.5f, player, plan);
        check(((DomeBoss) boss).dome().phase == DomeBoss.Phase.LIGHTNING_CHARGE,
            "Dome should return to lightning after the laser");
        player.invulnerable = 0;
        combat.update(DomeBoss.LIGHTNING_CHARGE + .01f, player, plan);
        check(player.hp < hp, "Remaining inside the lightning marker should take damage");

        combat.update(DomeBoss.LIGHTNING_STRIKE + .01f, player, plan);
        combat.update(DomeBoss.RECOVERY + .01f, player, plan);
        ((DomeBoss) boss).dome().nextLaser = false;
        ((DomeBoss) boss).dome().remaining = 0;
        combat.update(.01f, player, plan);
        hp = player.hp;
        player.x = ((DomeBoss) boss).dome().aimX - 115;
        player.invulnerable = player.dashTimer = 0;
        player.sp = 100;
        check(player.dash(1, 0) && player.x == ((DomeBoss) boss).dome().aimX,
            "The test dash should land inside the lightning marker");
        ((DomeBoss) boss).dome().remaining = .01f;
        combat.update(.02f, player, plan);
        check(player.hp == hp, "Dash invulnerability should protect against lightning");

        Enemy other = GameRulesCheck.fixture(900, 425, 36,
            500, 72, 0, 12, RunPlan.DOME_ID);
        check(((DomeBoss) other).dome() != ((DomeBoss) boss).dome()
            && ((DomeBoss) other).dome().phase == DomeBoss.Phase.IDLE,
            "Dome instances must own independent strategies and skill states");

        combat.begin(RunDirector.Room.FINAL_EXAM, plan, 0);
        Enemy serverDome = null;
        for (Enemy enemy : combat.enemies)
            if (enemy.kind == RunPlan.DOME_ID) serverDome = enemy;
        check(serverDome != null && serverDome.anchored,
            "The Final Exam must contain an anchored Dome");
        combat.enemies.clear();
        combat.enemies.add(serverDome);
        player.startRoom(true);
        player.invulnerable = 0;
        ((DomeBoss) serverDome).dome().nextLaser = true;
        ((DomeBoss) serverDome).dome().remaining = 0;
        hp = player.hp;
        combat.update(.01f, player, plan);
        check(((DomeBoss) serverDome).dome().downward
            && ((DomeBoss) serverDome).dome().originX == ((DomeBoss) serverDome).dome().endX
            && Math.abs(((DomeBoss) serverDome).dome().endY - GameSpace.BOTTOM) < .01f
            && player.hp == hp,
            "The cabinet laser must warn vertically down to the player lane");
        player.x = ((DomeBoss) serverDome).dome().originX;
        combat.update(DomeBoss.LASER_CHARGE + .01f, player, plan);
        check(player.hp < hp, "The downward laser must hit a player directly below Dome");
        player.x += 100;
        player.invulnerable = 0;
        hp = player.hp;
        combat.update(.01f, player, plan);
        check(player.hp == hp && player.y == GameSpace.SERVER_PLAYER_Y,
            "Moving sideways in the Final Exam lane must avoid the laser");
        check(serverDome.x == serverDome.homeX && serverDome.y == serverDome.homeY,
            "Dome must remain on his cabinet while using skills");

        // Measure the actual cast cadence, including SP regeneration and all phases.
        combat.begin(RunDirector.Room.BOSS, plan, 0);
        combat.enemies.clear();
        Enemy rapidDome = GameRulesCheck.fixture(1100, 425, 36,
            500, 72, 0, 12, RunPlan.DOME_ID);
        combat.enemies.add(rapidDome);
        player.startRoom(false);
        player.invulnerable = 100;
        int casts = 0;
        for (int i = 0; i < 1200; i++) {
            DomeBoss.Phase previous = ((DomeBoss) rapidDome).dome().phase;
            combat.update(.01f, player, plan);
            if (previous == DomeBoss.Phase.IDLE
                && ((DomeBoss) rapidDome).dome().phase != DomeBoss.Phase.IDLE) casts++;
        }
        check(casts >= 5, "Dome should start at least five casts in twelve seconds in loop one");
    }

    /** Build the same explicit-stat fixture using the concrete owner of each skill state. */
    static Enemy fixture(float x, float y, float radius, float hp, float maxSp,
                         float speed, float damage, int kind) {
        if (kind == CombatRoom.WARPER) return new io.github.kirinojuju.enemy.WarperMonster(x, y, radius, hp, maxSp, speed, damage, kind);
        if (kind < 0) return new io.github.kirinojuju.enemy.Monster(x, y, radius, hp, maxSp, speed, damage, kind);
        switch (kind) {
            case RunPlan.AE_ID: return new AeBoss(x, y, radius, hp, maxSp, speed, damage, kind);
            case RunPlan.SATI_ID: return new SatiBoss(x, y, radius, hp, maxSp, speed, damage, kind);
            case RunPlan.DOME_ID: return new DomeBoss(x, y, radius, hp, maxSp, speed, damage, kind);
            case CombatRoom.LIQUID_CAT: return new LiquidCatBoss(x, y, radius, hp, maxSp, speed, damage, kind);
            case CombatRoom.ALLERGIC_VACUUM: return new AllergicVacuumBoss(x, y, radius, hp, maxSp, speed, damage, kind);
            case CombatRoom.INSECURE_SWORD: return new InsecureSwordBoss(x, y, radius, hp, maxSp, speed, damage, kind);
            default: return new FixtureBoss(x, y, radius, hp, maxSp, speed, damage, kind);
        }
    }
    private static final class FixtureBoss extends Boss {
        FixtureBoss(float x, float y, float radius, float hp, float maxSp,
                    float speed, float damage, int kind) {
            super(x, y, radius, hp, maxSp, speed, damage, kind);
        }
    }

    private static final class ProbeAttack implements WeaponAttack {
        int calls;
        @Override public boolean attack(PlayerCharacter player, CombatRoom room,
                                        io.github.kirinojuju.audio.GameAudio audio) {
            calls++;
            return true;
        }
        @Override public void addAbility(io.github.kirinojuju.weapon.WeaponAbility ability) { }
    }

    private static void checkInjectedWeapons() {
        ProbeAttack physical = new ProbeAttack(), magical = new ProbeAttack();
        PlayerCharacter player = new PlayerCharacter(physical, magical);
        CombatRoom room = new CombatRoom(new Random(200));
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        player.attack(room);
        check(physical.calls == 1 && magical.calls == 0,
            "Physical attacks must use the injected implementation");
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        player.attack(room);
        check(physical.calls == 1 && magical.calls == 1,
            "Switching weapons must dispatch to the other injected implementation");
    }
}
