package io.github.kirinojuju.flow;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.player.Upgrade;
import io.github.kirinojuju.world.EntranceScene;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/** Menu, room transitions, loop completion and EX rewards. */
public final class RunDirector {
    public enum Phase { START_SELECT, ENTRANCE, CAR_PROMPT, CAR_ENDING,
        FIGHT, UPGRADE, TRANSITION, CUTSCENE, GAME_OVER }
    public enum Room { MOBS, BOSS, EX, FINAL_EXAM }
    private static final float SCENE_FADE_DURATION = .4f;
    public static final int MONSTER_CLEAR_HEAL = 12;
    public static final int BOSS_CLEAR_HEAL = 35;

    public final RunPlan plan;
    public Phase phase = Phase.START_SELECT;
    public Room room = Room.MOBS;
    public Room nextRoom = Room.MOBS;
    public int exId, clearedRooms;
    public String transitionTitle = "", transitionDetail = "";
    public float sceneFade;
    public boolean exTest;
    public final Upgrade[] offeredUpgrades = new Upgrade[3];
    private boolean nextLoopOnContinue;
    private boolean finalExamQueued, finalExamCleared;
    private Room roomAfterFinalExam = Room.MOBS;
    private boolean nextLoopAfterFinalExam;
    private Phase fadeDestination;
    private final Random random;
    private int upgradeRerollsRemaining;
    private GameAudio audio;

    public RunDirector(Random random) {
        this.random = random;
        plan = new RunPlan(random);
    }
    public void setAudio(GameAudio audio) { this.audio = audio; }

    /** Desktop test entry: skip progression and keep the real cutscene and fight. */
    public void startFinalExamTest(PlayerCharacter player) {
        plan.recordBossDefeat(RunPlan.AE_ID);
        plan.recordBossDefeat(RunPlan.DOME_ID);
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        finalExamQueued = true;
        nextRoom = Room.FINAL_EXAM;
        phase = Phase.CUTSCENE;
    }

    /** Desktop test entry: start directly in a real monster room with tables. */
    public void startObstaclesTest(PlayerCharacter player, CombatRoom combat) {
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        beginRoom(Room.MOBS, player, combat);
    }

    /** Desktop test entry: fight one EX immediately with both weapons and EX techniques. */
    public void startExTest(PlayerCharacter player, CombatRoom combat, int selectedExId) {
        if (selectedExId < 0 || selectedExId >= RunPlan.EX_MONSTERS.length)
            throw new IllegalArgumentException("Unknown EX id: " + selectedExId);
        exTest = true;
        exId = selectedExId;
        player.acquire(PlayerCharacter.Weapon.PHYSICAL);
        player.acquire(PlayerCharacter.Weapon.MAGICAL);
        beginRoom(Room.EX, player, combat);
        sceneFade = 0;
    }

    /** Returns true when the player requests a fresh run. */
    public boolean update(float dt, PlayerCharacter player, CombatRoom combat,
                   boolean touchSkill, boolean mouseAttack) {
        return update(dt, player, combat, touchSkill, false, false, mouseAttack);
    }

    public boolean update(float dt, PlayerCharacter player, CombatRoom combat,
                          boolean touchSkill, boolean touchIceWall, boolean mouseAttack) {
        return update(dt, player, combat, touchSkill, touchIceWall, false, mouseAttack);
    }

    public boolean update(float dt, PlayerCharacter player, CombatRoom combat,
                          boolean touchSkill, boolean touchIceWall,
                          boolean touchLanWave, boolean mouseAttack) {
        if (advanceSceneFade(dt, player, combat)) return false;
        switch (phase) {
            case GAME_OVER:
                return Gdx.input.isKeyJustPressed(Input.Keys.R);
            case CAR_ENDING:
                return Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(Input.Keys.R);
            case START_SELECT:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) startRun(player, combat);
                break;
            case ENTRANCE:
                updateEntrance(dt, player);
                break;
            case CAR_PROMPT:
                updateCarPrompt();
                break;
            case UPGRADE:
                if (Gdx.input.isKeyJustPressed(Input.Keys.R)) rerollUpgrades(player);
                else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) chooseUpgrade(0, player, combat);
                else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) chooseUpgrade(1, player, combat);
                else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) chooseUpgrade(2, player, combat);
                break;
            case TRANSITION:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) continueAfterRoom(player, combat);
                break;
            case CUTSCENE:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) continueAfterCutscene(player, combat);
                break;
            case FIGHT:
                updateFight(dt, player, combat, touchSkill, touchIceWall, touchLanWave, mouseAttack);
                break;
            default:
                break;
        }
        return false;
    }

    private void updateEntrance(float dt, PlayerCharacter player) {
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)
            || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx--;
        if (Gdx.input.isKeyPressed(Input.Keys.D)
            || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx++;
        if (Gdx.input.isKeyPressed(Input.Keys.S)
            || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy--;
        if (Gdx.input.isKeyPressed(Input.Keys.W)
            || Gdx.input.isKeyPressed(Input.Keys.UP)) dy++;
        EntranceScene.move(player, dt, dx, dy);
        if (EntranceScene.atDoor(player)) requestDoor(player);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) openCarPrompt(player);
    }

    private void updateCarPrompt() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) chooseCar(true);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.N)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)
            || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) chooseCar(false);
    }

    private void updateFight(float dt, PlayerCharacter player, CombatRoom combat,
                             boolean touchSkill, boolean touchIceWall,
                             boolean touchLanWave, boolean mouseAttack) {
        player.update(dt, combat, touchSkill, touchIceWall, touchLanWave, mouseAttack);
        boolean cleared = combat.update(dt, player, plan);
        if (player.hp <= 0) phase = Phase.GAME_OVER;
        else if (cleared) finishRoom(player);
    }

    public void startRun(PlayerCharacter player, CombatRoom combat) {
        if (phase != Phase.START_SELECT) return;
        if (audio != null) audio.click();
        fadeDestination = Phase.ENTRANCE;
    }

    public void requestDoor(PlayerCharacter player) {
        if (phase != Phase.ENTRANCE || sceneFade > 0 || fadeDestination != null
            || !EntranceScene.atDoor(player)) return;
        fadeDestination = Phase.FIGHT;
    }

    public void openCarPrompt(PlayerCharacter player) {
        if (phase != Phase.ENTRANCE || sceneFade > 0 || fadeDestination != null
            || !EntranceScene.atCar(player)) return;
        player.moving = false;
        phase = Phase.CAR_PROMPT;
        if (audio != null) audio.click();
    }

    public void chooseCar(boolean goHome) {
        if (phase != Phase.CAR_PROMPT || fadeDestination != null) return;
        if (audio != null) audio.click();
        if (goHome) fadeDestination = Phase.CAR_ENDING;
        else phase = Phase.ENTRANCE;
    }

    public boolean advanceSceneFade(float dt, PlayerCharacter player, CombatRoom combat) {
        if (fadeDestination != null) {
            sceneFade = Math.min(1, sceneFade + dt / SCENE_FADE_DURATION);
            if (sceneFade == 1) {
                Phase destination = fadeDestination;
                fadeDestination = null;
                if (destination == Phase.ENTRANCE) {
                    EntranceScene.placePlayer(player);
                    phase = Phase.ENTRANCE;
                } else if (destination == Phase.FIGHT) {
                    player.acquire(PlayerCharacter.Weapon.MAGICAL);
                    player.acquire(PlayerCharacter.Weapon.PHYSICAL);
                    if (audio != null) audio.nextStage();
                    beginRoom(Room.MOBS, player, combat);
                } else phase = destination;
            }
            return true;
        }
        if (sceneFade > 0) {
            sceneFade = Math.max(0, sceneFade - dt / SCENE_FADE_DURATION);
            return true;
        }
        return false;
    }

    public void chooseUpgrade(int index, PlayerCharacter player, CombatRoom combat) {
        if (phase != Phase.UPGRADE || index < 0 || index >= offeredUpgrades.length) return;
        Upgrade choice = offeredUpgrades[index];
        if (choice == null || !choice.available(player)) return;
        choice.apply(player);
        if (audio != null) audio.click();
        phase = Phase.TRANSITION;
        continueAfterRoom(player, combat);
    }

    public void continueAfterRoom(PlayerCharacter player, CombatRoom combat) {
        if (phase != Phase.TRANSITION) return;
        if (nextRoom == Room.FINAL_EXAM) {
            phase = Phase.CUTSCENE;
            return;
        }
        if (nextLoopOnContinue) {
            plan.nextLoop();
            nextLoopOnContinue = false;
        }
        if (audio != null) audio.nextStage();
        beginRoom(nextRoom, player, combat);
    }

    public void continueAfterCutscene(PlayerCharacter player, CombatRoom combat) {
        if (phase != Phase.CUTSCENE) return;
        if (audio != null) audio.nextStage();
        beginRoom(Room.FINAL_EXAM, player, combat);
    }

    private void beginRoom(Room next, PlayerCharacter player, CombatRoom combat) {
        room = next;
        phase = Phase.FIGHT;
        player.startRoom(next == Room.FINAL_EXAM);
        player.setExTechniques(next == Room.EX);
        combat.begin(room, plan, exId);
        player.setObstacles(combat.obstacles);
        player.setIceWall(combat.iceWall);
        sceneFade = 1;
    }

    public void finishRoom(PlayerCharacter player) {
        clearedRooms++;
        phase = Phase.TRANSITION;
        switch (room) {
            case MOBS:
                player.heal(MONSTER_CLEAR_HEAL);
                transitionTitle = "MONSTER ROOM CLEARED";
                transitionDetail = "+" + MONSTER_CLEAR_HEAL + " HP  |  The Ajarn is waiting in the next room.";
                nextRoom = Room.BOSS;
                break;
            case BOSS:
                finishBossRoom(player);
                break;
            case EX:
                finishExRoom(player);
                break;
            case FINAL_EXAM:
                finalExamQueued = false;
                finalExamCleared = true;
                transitionTitle = "FINAL EXAM CLEARED";
                transitionDetail = "Ae and Dome defeated together.";
                nextRoom = roomAfterFinalExam;
                nextLoopOnContinue = nextLoopAfterFinalExam;
                break;
        }
    }

    private void finishBossRoom(PlayerCharacter player) {
        String defeated = plan.bossName();
        plan.recordBossDefeat(plan.bossId());
        player.heal(BOSS_CLEAR_HEAL);
        if (!plan.completeStage()) {
            transitionTitle = defeated + " DEFEATED";
            transitionDetail = "Next stage: " + plan.stageName() + "  |  +" + BOSS_CLEAR_HEAL + " HP";
            nextRoom = Room.MOBS;
        } else if (plan.hasExAfterYear()) {
            exId = plan.randomExId();
            transitionTitle = "YEAR FOUR COMPLETE - LOOP " + plan.loop();
            transitionDetail = "EX: " + RunPlan.EX_MONSTERS[exId]
                + "  |  Tether + Heat Beam active only in this fight";
            nextRoom = Room.EX;
        } else {
            transitionTitle = "YEAR FOUR COMPLETE - LOOP " + plan.loop();
            transitionDetail = "A harder school year begins.  |  +" + BOSS_CLEAR_HEAL + " HP";
            nextRoom = Room.MOBS;
            nextLoopOnContinue = true;
        }

        offerUpgrades(player);
        phase = Phase.UPGRADE;
        if (plan.finalExamReady() && !finalExamQueued && !finalExamCleared) {
            finalExamQueued = true;
            // Let the player reach the introductory EX before the Ae-Dome exam.
            if (plan.loop() > 1 && nextRoom != Room.EX) queueFinalExam();
        }
    }

    private void finishExRoom(PlayerCharacter player) {
        player.setExTechniques(false);
        player.applyExBuff(exId);
        transitionTitle = RunPlan.EX_MONSTERS[exId].toUpperCase() + " DEFEATED";
        if (exId == 0) transitionDetail = "Liquid Cat buff: +25 max HP, +45 HP.";
        else if (exId == 1) transitionDetail = "Allergic Vacuum buff: +30 speed, +20 HP.";
        else transitionDetail = "Insecure Sword buff: +8 physical / +6 magic damage, +20 HP.";
        nextRoom = Room.MOBS;
        nextLoopOnContinue = true;
        if (finalExamQueued) queueFinalExam();
    }

    private void queueFinalExam() {
        roomAfterFinalExam = nextRoom;
        nextLoopAfterFinalExam = nextLoopOnContinue;
        nextRoom = Room.FINAL_EXAM;
        nextLoopOnContinue = false;
    }

    /** One reroll per upgrade screen, preferring choices the player has not just seen. */
    public boolean rerollUpgrades(PlayerCharacter player) {
        if (phase != Phase.UPGRADE || upgradeRerollsRemaining <= 0) return false;

        ArrayList<Upgrade> available = new ArrayList<Upgrade>();
        ArrayList<Upgrade> unseen = new ArrayList<Upgrade>();
        ArrayList<Upgrade> previous = new ArrayList<Upgrade>();
        Collections.addAll(previous, offeredUpgrades);
        for (Upgrade upgrade : Upgrade.values()) {
            if (!upgrade.available(player)) continue;
            available.add(upgrade);
            if (!previous.contains(upgrade)) unseen.add(upgrade);
        }
        if (unseen.isEmpty()) return false;

        Collections.shuffle(unseen, random);
        Collections.shuffle(previous, random);
        ArrayList<Upgrade> rerolled = new ArrayList<Upgrade>();
        for (Upgrade upgrade : unseen) {
            if (rerolled.size() == offeredUpgrades.length) break;
            rerolled.add(upgrade);
        }
        for (Upgrade upgrade : previous) {
            if (rerolled.size() == offeredUpgrades.length) break;
            if (available.contains(upgrade) && !rerolled.contains(upgrade))
                rerolled.add(upgrade);
        }
        for (int i = 0; i < offeredUpgrades.length; i++)
            offeredUpgrades[i] = rerolled.get(i);
        upgradeRerollsRemaining--;
        return true;
    }

    public int upgradeRerollsRemaining() { return upgradeRerollsRemaining; }

    private void offerUpgrades(PlayerCharacter player) {
        upgradeRerollsRemaining = 1;
        ArrayList<Upgrade> choices = new ArrayList<Upgrade>();
        for (Upgrade upgrade : Upgrade.values())
            if (upgrade.available(player)) choices.add(upgrade);
        Collections.shuffle(choices, random);
        for (int i = 0; i < offeredUpgrades.length; i++)
            offeredUpgrades[i] = choices.get(i);
    }
}
