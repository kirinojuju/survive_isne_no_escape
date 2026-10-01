package io.github.kirinojuju;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/** Menu, room transitions, loop completion and EX rewards. */
final class RunDirector {
    enum Phase { START_SELECT, ENTRANCE, CAR_PROMPT, CAR_ENDING,
        FIGHT, UPGRADE, TRANSITION, GAME_OVER }
    enum Room { MOBS, BOSS, EX }
    private static final float SCENE_FADE_DURATION = .4f;

    final RunPlan plan;
    Phase phase = Phase.START_SELECT;
    Room room = Room.MOBS;
    Room nextRoom = Room.MOBS;
    int exId, clearedRooms;
    String transitionTitle = "", transitionDetail = "";
    float sceneFade;
    final Upgrade[] offeredUpgrades = new Upgrade[3];
    private boolean nextLoopOnContinue;
    private Phase fadeDestination;
    private final Random random;
    private GameAudio audio;

    RunDirector(Random random) {
        this.random = random;
        plan = new RunPlan(random);
    }
    void setAudio(GameAudio audio) { this.audio = audio; }

    /** Returns true when the player requests a fresh run. */
    boolean update(float dt, PlayerCharacter player, CombatRoom combat,
                   boolean touchSkill, boolean mouseAttack) {
        if (advanceSceneFade(dt, player, combat)) return false;
        if (phase == Phase.GAME_OVER)
            return Gdx.input.isKeyJustPressed(Input.Keys.R);

        if (phase == Phase.CAR_ENDING)
            return Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.R);

        if (phase == Phase.START_SELECT) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) startRun(player, combat);
            return false;
        }

        if (phase == Phase.ENTRANCE) {
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
            return false;
        }

        if (phase == Phase.CAR_PROMPT) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Y)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) chooseCar(true);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.N)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) chooseCar(false);
            return false;
        }

        if (phase == Phase.UPGRADE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))
                chooseUpgrade(0, player, combat);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))
                chooseUpgrade(1, player, combat);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3))
                chooseUpgrade(2, player, combat);
            return false;
        }

        if (phase == Phase.TRANSITION) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) continueAfterRoom(player, combat);
            return false;
        }

        player.update(dt, combat, touchSkill, mouseAttack);
        boolean cleared = combat.update(dt, player, plan);
        if (player.hp <= 0) phase = Phase.GAME_OVER;
        else if (cleared) finishRoom(player);
        return false;
    }

    void startRun(PlayerCharacter player, CombatRoom combat) {
        if (phase != Phase.START_SELECT) return;
        if (audio != null) audio.click();
        fadeDestination = Phase.ENTRANCE;
    }

    void requestDoor(PlayerCharacter player) {
        if (phase != Phase.ENTRANCE || sceneFade > 0 || fadeDestination != null
            || !EntranceScene.atDoor(player)) return;
        fadeDestination = Phase.FIGHT;
    }

    void openCarPrompt(PlayerCharacter player) {
        if (phase != Phase.ENTRANCE || sceneFade > 0 || fadeDestination != null
            || !EntranceScene.atCar(player)) return;
        player.moving = false;
        phase = Phase.CAR_PROMPT;
        if (audio != null) audio.click();
    }

    void chooseCar(boolean goHome) {
        if (phase != Phase.CAR_PROMPT || fadeDestination != null) return;
        if (audio != null) audio.click();
        if (goHome) fadeDestination = Phase.CAR_ENDING;
        else phase = Phase.ENTRANCE;
    }

    boolean advanceSceneFade(float dt, PlayerCharacter player, CombatRoom combat) {
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

    void chooseUpgrade(int index, PlayerCharacter player, CombatRoom combat) {
        if (phase != Phase.UPGRADE || index < 0 || index >= offeredUpgrades.length) return;
        Upgrade choice = offeredUpgrades[index];
        if (choice == null || !choice.available(player)) return;
        choice.apply(player);
        if (audio != null) audio.click();
        phase = Phase.TRANSITION;
        continueAfterRoom(player, combat);
    }

    void continueAfterRoom(PlayerCharacter player, CombatRoom combat) {
        if (phase != Phase.TRANSITION) return;
        if (nextLoopOnContinue) {
            plan.nextLoop();
            nextLoopOnContinue = false;
        }
        if (audio != null) audio.nextStage();
        beginRoom(nextRoom, player, combat);
    }

    private void beginRoom(Room next, PlayerCharacter player, CombatRoom combat) {
        room = next;
        phase = Phase.FIGHT;
        player.startRoom();
        player.setExTechniques(next == Room.EX);
        combat.begin(room, plan, exId);
        sceneFade = 1;
    }

    void finishRoom(PlayerCharacter player) {
        clearedRooms++;
        phase = Phase.TRANSITION;
        if (room == Room.MOBS) {
            transitionTitle = "MONSTER ROOM CLEARED";
            transitionDetail = "The Ajarn is waiting in the next room.";
            nextRoom = Room.BOSS;
            return;
        }

        if (room == Room.BOSS) {
            String defeated = plan.bossName();
            player.heal(24);
            if (!plan.completeStage()) {
                transitionTitle = defeated + " DEFEATED";
                transitionDetail = "Next stage: " + plan.stageName() + "  |  +24 HP";
                nextRoom = Room.MOBS;
            } else if (plan.hasExAfterYear()) {
                exId = plan.randomExId();
                transitionTitle = "YEAR FOUR COMPLETE - LOOP " + plan.loop();
                transitionDetail = "EX: " + RunPlan.EX_MONSTERS[exId]
                    + "  |  Tether + Heat Beam active only in this fight";
                nextRoom = Room.EX;
            } else {
                transitionTitle = "YEAR FOUR COMPLETE - LOOP " + plan.loop();
                nextRoom = Room.MOBS;
                transitionDetail = "A harder school year begins.  |  +24 HP";
                nextLoopOnContinue = true;
            }
            offerUpgrades(player);
            phase = Phase.UPGRADE;
            return;
        }

        player.setExTechniques(false);
        player.applyExBuff(exId);
        transitionTitle = RunPlan.EX_MONSTERS[exId].toUpperCase() + " DEFEATED";
        if (exId == 0) transitionDetail = "Liquid Cat buff: +25 max HP, +45 HP.";
        else if (exId == 1) transitionDetail = "Allergic Vacuum buff: +30 speed, +20 HP.";
        else transitionDetail = "Insecure Sword buff: +8 physical / +6 magic damage, +20 HP.";
        nextRoom = Room.MOBS;
        nextLoopOnContinue = true;
    }

    private void offerUpgrades(PlayerCharacter player) {
        ArrayList<Upgrade> choices = new ArrayList<Upgrade>();
        for (Upgrade upgrade : Upgrade.values())
            if (upgrade.available(player)) choices.add(upgrade);
        Collections.shuffle(choices, random);
        for (int i = 0; i < offeredUpgrades.length; i++)
            offeredUpgrades[i] = choices.get(i);
    }
}
