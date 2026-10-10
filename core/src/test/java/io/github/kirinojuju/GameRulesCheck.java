package io.github.kirinojuju;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Dependency-free regression check for the loop and weapon rules. */
public final class GameRulesCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        RunPlan order = new RunPlan(new Random(4));
        Set<Integer> stages = new HashSet<Integer>();
        Set<Integer> bosses = new HashSet<Integer>();
        Set<Integer> bossFamilies = new HashSet<Integer>();
        for (int i = 0; i < 4; i++) {
            stages.add(order.stageId());
            bosses.add(order.bossId());
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
        player.switchTo(PlayerCharacter.Weapon.MAGICAL);
        check(player.activeWeapon() == PlayerCharacter.Weapon.MAGICAL,
            "The notebook must be selectable in loop one");
        player.switchTo(PlayerCharacter.Weapon.PHYSICAL);

        combat.enemies.clear();
        Enemy target = new Enemy(player.x + 150, player.y,
            17, 100, 37, 0, 0, -1);
        Enemy backup = new Enemy(player.x + 350, player.y,
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
        target.hp = 0;
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
        check(run.plan.loop() == 2 && player.hasBothWeapons(),
            "Loop two must start after four boss upgrades");
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
        checkUpgrades();
        checkTetherAndHeatBeam();
        checkExMechanics();
        checkSatiSkill();
        checkPowerBallKnockback();
        checkTalgoCast();
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
        check(run.phase == RunDirector.Phase.FIGHT,
            "Choosing an upgrade must advance to the next room");
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
        Enemy first = new Enemy(300, 300, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        Enemy second = new Enemy(300, 300, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        first.sp = second.sp = 0;
        combat.enemies.add(first);
        combat.enemies.add(second);
        combat.update(.01f, player, plan);
        float dx = first.x - second.x, dy = first.y - second.y;
        check(dx * dx + dy * dy >= 45 * 45,
            "Ordinary enemies should separate instead of overlapping");

        combat.enemies.clear();
        Enemy warper = new Enemy(300, 300, 17, 58, 42, 0, 0,
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
        Enemy strikingWarper = new Enemy(300, 300, 17, 58, 42, 0, 0,
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
        Enemy boss = new Enemy(850, 400, 36, 200, 72, 0, 12, 2);
        boss.summoner = boss.reflector = true;
        boss.armor = boss.maxArmor = 110;
        boss.summonTimer = .01f;
        combat.enemies.add(boss);
        combat.damageNearby(boss.x, boss.y, 1, 27);
        check(boss.hp == 200 && boss.armor == 83 && combat.shots.size == 1
            && combat.shots.first().reflected,
            "Armored boss should absorb damage and reflect an attack");
        combat.update(.02f, player, plan);
        check(combat.enemies.size == 2 && combat.enemies.get(1).kind != CombatRoom.BASIC
            && combat.enemies.get(1).armor > 0,
            "A loop-one boss should summon a skilled, shielded minion");
    }

    private static void checkNewCombat() {
        PlayerCharacter player = new PlayerCharacter();
        player.moveBy(0, 1000);
        check(player.y == GameSpace.WALK_TOP - 64,
            "Walking or dashing must stop before the window wall");
        player.moveBy(0, -76);
        RunPlan plan = new RunPlan(new Random(11));
        CombatRoom combat = new CombatRoom(new Random(12));

        Enemy front = new Enemy(player.x + 115, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        Enemy diagonal = new Enemy(player.x + 90, player.y + 60,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        Enemy behind = new Enemy(player.x - 100, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        Enemy beyond = new Enemy(player.x + 150, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        combat.enemies.add(front);
        combat.enemies.add(diagonal);
        combat.enemies.add(behind);
        combat.enemies.add(beyond);
        combat.damageArc(player.x, player.y, 1, 0, PlayerCharacter.LAN_REACH, 65, 27);
        check(front.hp == 73 && diagonal.hp == 73 && behind.hp == 100
            && beyond.hp == 100,
            "LAN cable should sweep a shorter forward arc without striking behind");
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        player.lockAttackTarget(combat, behind.x, behind.y);
        check(player.attackTarget() == behind && player.faceX < 0,
            "Clicking an enemy should lock and face that enemy");
        player.attack(combat);
        check(behind.hp == 73 && front.hp == 73
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
        Enemy impact = new Enemy(780, 360, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        Enemy splash = new Enemy(875, 360, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        Enemy outside = new Enemy(1100, 360, 17, 100, 999, 0, 0,
            CombatRoom.BASIC);
        impact.sp = splash.sp = outside.sp = 0;
        combat.enemies.add(impact);
        combat.enemies.add(splash);
        combat.enemies.add(outside);
        combat.firePowerBall(780, 360, 0, 0, 42, impact);
        combat.update(.01f, player, plan);
        check(impact.hp < 100 && splash.hp < 100 && outside.hp == 100,
            "PowerBall should damage multiple enemies in its blast radius");
        check(impact.stunTime > 0 && splash.slowTime > 0 && combat.blastTime > 0,
            "PowerBall should shock enemies and show its explosion");

        combat.enemies.clear();
        combat.shots.clear();
        Enemy shooter = new Enemy(500, 360, 17, 100, 999, 80, 0,
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
        Enemy waveEnemy = new Enemy(300, 360, 17, 100, 43, 0, 9,
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
        boolean wave = false;
        for (Enemy enemy : combat.enemies)
            if (enemy.kind == CombatRoom.WAVE) wave = true;
        check(wave, "The wave monster should appear in the second stage");
        plan.completeStage();
        combat.begin(RunDirector.Room.MOBS, plan, 0);
        Enemy shield = null;
        for (Enemy enemy : combat.enemies)
            if (enemy.kind == CombatRoom.SHIELDED && enemy.armor > 0) shield = enemy;
        check(shield != null, "Shielded monsters should appear later in loop one");
        float shieldHp = shield.hp, shieldArmor = shield.armor;
        combat.damageNearby(shield.x, shield.y, 1, 20);
        check(shield.hp == shieldHp && shield.armor == shieldArmor - 20,
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
            if (family == 0)
                check(boss.summoner && boss.reflector && boss.armor > 0,
                    "Summon and shield boss missing its traits");
            else if (family == 1) {
                check(boss.summoner && boss.warper,
                    "Summon and warp boss missing its traits");
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
            else if (family == 2) {
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

        Enemy lanTarget = new Enemy(player.x + 150, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        combat.enemies.add(lanTarget);
        player.attack(combat);
        check(lanTarget.hp == 66 && lanTarget.slowTime > 0
            && player.attackTimer < PlayerCharacter.LAN_ATTACK_INTERVAL,
            "A wide LAN sweep must reach farther, hit harder, and slow its target");

        combat.enemies.clear();
        Enemy notebookTarget = new Enemy(760, player.y,
            17, 100, 999, 0, 0, CombatRoom.BASIC);
        Enemy nearby = new Enemy(810, player.y,
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
        check(notebookTarget.hp < 100 && nearby.hp < 100
            && notebookTarget.poisonTime > 0 && nearby.poisonTime > 0
            && combat.splashTime > 0,
            "Notebook splash must damage and poison nearby monsters");
        float hpAfterHit = notebookTarget.hp;
        combat.update(1f, player, plan);
        check(notebookTarget.hp < hpAfterHit,
            "Poison must keep damaging monsters after the notebook hit");
        player.attackSpeedBonus = 1.8f;
        player.speedBonus = 160;
        int remaining = 0;
        for (Upgrade upgrade : Upgrade.values())
            if (upgrade.available(player)) remaining++;
        check(remaining >= 3,
            "Late loops must still offer three upgrades after unlocks and speed caps");
    }

    private static void checkTetherAndHeatBeam() {
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(40));
        RunPlan plan = new RunPlan(new Random(41));
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        player.setExTechniques(true);
        check(player.lanTether && player.notebookHeatBeam,
            "Both special weapon techniques should activate for EX combat");
        Enemy first = new Enemy(player.x + 110, player.y,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy second = new Enemy(player.x + 130, player.y + 30,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy ajarn = new Enemy(player.x + 100, player.y - 65,
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
        Enemy marked = new Enemy(760, 360,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy plain = new Enemy(640, 360,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        marked.tetherTime = 1.7f;
        marked.sp = plain.sp = 0;
        combat.enemies.add(marked);
        combat.enemies.add(plain);
        combat.firePowerBall(700, 360, 0, 0, 40, null);
        combat.shots.first().life = 0;
        combat.update(.01f, player, plan);
        check(marked.hp < plain.hp,
            "PowerBall should deal bonus damage to a tethered target at equal distance");

        combat.enemies.clear();
        combat.shots.clear();
        Upgrade.NOTEBOOK_POISON.apply(player);
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        Enemy beamNear = new Enemy(800, 360,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy beamFar = new Enemy(950, 360,
            17, 200, 999, 0, 0, CombatRoom.BASIC);
        Enemy outside = new Enemy(850, 480,
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
        check(beamNear.hp < 200 && beamFar.hp < 200 && outside.hp == 200
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
        LiquidCatBoss cat = (LiquidCatBoss) combat.enemies.first();
        cat.skillTimer = 100;
        combat.update(3.6f, player, plan);
        check(cat.exState == 1 && cat.radius == 16 && combat.exObjects.size > 0,
            "Liquid Cat should melt into a smaller puddle and leave ooze");
        CombatRoom.ExObject slime = combat.exObjects.first();
        player.x = slime.x;
        player.y = slime.y;
        combat.update(.01f, player, plan);
        check(player.slowTime > 0, "Liquid Cat ooze should slow the player");
        player.x = cat.x - 180;
        player.y = cat.y;
        cat.skillTimer = 0;
        cat.catTransformTime = 0;
        combat.update(.01f, player, plan);
        check(cat.catDivideTime > 0 && combat.shots.size > 0
            && combat.shots.first().liquidCatBullet,
            "Liquid Cat's compact form should use the divide and bullet attack assets");
        combat.update(2.5f, player, plan);
        check(cat.exState == 0 && cat.radius == 34,
            "Liquid Cat should return to its full hitbox");
        check(cat.catTransformTime > 0,
            "Changing forms should start Liquid Cat's transformation frames");

        player.x = GameSpace.WIDTH / 2;
        player.y = GameSpace.HEIGHT / 2;
        combat.begin(RunDirector.Room.EX, plan, 1);
        Enemy vacuum = combat.enemies.first();
        vacuum.exMeter = 100;
        vacuum.exStoredDust = 4;
        combat.update(.01f, player, plan);
        check(vacuum.exState == 1,
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
        check(vacuum.exState == 2 && vacuum.exMeter == 0 && scatteredDust >= 4,
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
        check(combat.carriedNotes == 0 && sword.exConfidence == 25
            && sword.armor < armor,
            "Bringing a note to the sword should raise confidence and weaken armor");
        sword.exConfidence = 100;
        sword.exState = 0;
        sword.skillTimer = 0;
        combat.update(.01f, player, plan);
        check(sword.exState == 1 && sword.exAimX == player.x,
            "A confident sword should warn before its heavy strike");
        float hpBeforeDodge = player.hp;
        player.x = 500;
        combat.update(.66f, player, plan);
        check(sword.exState == 2 && player.hp == hpBeforeDodge,
            "Moving away from the sword's marked point should dodge the slash");
    }

    private static void checkSatiSkill() {
        RunPlan plan = new RunPlan(new Random(21));
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(22));

        SatiBoss sati = new SatiBoss(plan, new Random(23));
        sati.x = 640;
        sati.y = 520;
        sati.satiCooldown = .01f;
        combat.enemies.add(sati);
        combat.update(.02f, player, plan);
        check(sati.satiCharge == SatiBoss.SATI_CHARGE_DURATION
            && sati.satiAimX == player.x && sati.satiAimY == player.y,
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
        Enemy monster = new Enemy(740, 360, 17,
            200, 999, 0, 0, CombatRoom.BASIC);
        Enemy boss = new Enemy(770, 420, 36,
            200, 50, 0, 0, RunPlan.TALGO_ID);
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

    private static void checkTalgoCast() {
        RunPlan plan = new RunPlan(new Random(62));
        PlayerCharacter player = new PlayerCharacter();
        CombatRoom combat = new CombatRoom(new Random(63));
        Enemy talgo = new Enemy(1100, 360, 36,
            500, 72, 0, 12, RunPlan.TALGO_ID);
        talgo.talgoCooldown = .01f;
        talgo.skillTimer = 100;
        talgo.sp = 0;
        combat.enemies.add(talgo);

        combat.update(.02f, player, plan);
        check(talgo.talgoCharge == CombatRoom.TALGO_CAST_DURATION,
            "Talgo should mark a safe circle before casting the full-room AOE");
        float reachX = talgo.talgoSafeX - player.x;
        float reachY = talgo.talgoSafeY - player.y;
        check(reachX * reachX + reachY * reachY <= 180 * 180,
            "The marked circle should be reachable during the warning");
        player.x = talgo.talgoSafeX;
        player.y = talgo.talgoSafeY;
        player.invulnerable = 0;
        float hp = player.hp;
        combat.update(CombatRoom.TALGO_CAST_DURATION + .01f, player, plan);
        check(player.hp == hp, "Standing in Talgo's safe circle should avoid the AOE");

        talgo.talgoCooldown = 0;
        combat.update(.01f, player, plan);
        player.x = talgo.talgoSafeX > GameSpace.WIDTH / 2
            ? GameSpace.LEFT + 50 : GameSpace.RIGHT - 50;
        player.y = GameSpace.HEIGHT / 2;
        player.invulnerable = 0;
        combat.update(CombatRoom.TALGO_CAST_DURATION + .01f, player, plan);
        check(player.hp < hp, "The AOE should damage a player outside the safe circle");

        talgo.talgoCooldown = 0;
        combat.update(.01f, player, plan);
        player.x = talgo.talgoSafeX > GameSpace.WIDTH / 2
            ? GameSpace.LEFT + 50 : GameSpace.RIGHT - 50;
        player.y = GameSpace.HEIGHT / 2;
        player.invulnerable = 0;
        player.dashTimer = 0;
        player.sp = 100;
        hp = player.hp;
        check(player.dash(0, 1), "A ready dash should activate during Talgo's cast");
        talgo.talgoCharge = .01f;
        combat.update(.02f, player, plan);
        check(player.hp == hp,
            "Dashing at the impact should protect the player outside the safe circle");
    }
}
