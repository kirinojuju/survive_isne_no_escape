package io.github.kirinojuju.flow;

// File role: Stores shuffled stages and bosses, current loop and difficulty scaling.

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/** The four stages and four distinct Ajarns chosen for one school-year loop. */
public final class RunPlan {
    // Boss identity, independent of combat family:
    // Sati = C++, M = OOP, Ae Far and Dome = Network, Ken = Algorithm.
    public static final int SATI_ID = 0, AE_ID = 1, M_ID = 2, DOME_ID = 3;
    public static final int KEN_ID = 4, FAR_ID = 6;
    // Preserve cleanup's original Final Exam slot and unlock condition (numeric ID 2).
    public static final int FINAL_EXAM_FAR_ID = 2;
    public static final String[] STAGES = {
        "Programming Lab", "Circuit Workshop", "Network Center", "Capstone Studio"
    };
    // IDs need not be consecutive; removing a boss must not shift another boss's name.
    public static final Map<Integer, String> BOSSES;
    static {
        Map<Integer, String> names = new LinkedHashMap<Integer, String>();
        names.put(SATI_ID, "Ajarn Sati");
        names.put(AE_ID, "Ajarn Ae");
        names.put(M_ID, "Ajarn M");
        names.put(DOME_ID, "Ajarn Dome");
        names.put(KEN_ID, "Ajarn Ken");
        names.put(FAR_ID, "Ajarn Far");
        BOSSES = Collections.unmodifiableMap(names);
    }
    public static final String[] EX_MONSTERS = {
        "Liquid Cat", "Allergic Vacuum", "Insecure Sword"
    };

    private final Random random;
    private final int[] stageOrder = new int[4];
    private final int[] bossOrder = new int[4];
    private int loop = 1;
    private int stage = 0;
    private int finalExamDefeats;

    // Create the first loop and shuffle its stage and boss order.
    public RunPlan(Random random) {
        this.random = random;
        shuffleYear();
    }

    // Choose the year's boss variants and shuffle the stage and boss orders.
    private void shuffleYear() {
        List<Integer> stages = new ArrayList<Integer>();
        List<Integer> bosses = new ArrayList<Integer>();
        for (int i = 0; i < STAGES.length; i++) stages.add(i);
        // One boss from each combat family keeps every loop varied.
        bosses.add(random.nextBoolean() ? KEN_ID : SATI_ID);
        bosses.add(AE_ID);
        bosses.add(random.nextBoolean() ? FAR_ID : M_ID);
        bosses.add(DOME_ID);
        Collections.shuffle(stages, random);
        Collections.shuffle(bosses, random);
        for (int i = 0; i < 4; i++) {
            stageOrder[i] = stages.get(i);
            bossOrder[i] = bosses.get(i);
        }
    }

    // Return the current difficulty loop, starting at one.
    public int loop() { return loop; }
    // Return the current position in the four-stage sequence, starting at zero.
    public int stage() { return stage; }
    // Return the stage identity at the current shuffled position.
    public int stageId() { return stageOrder[stage]; }
    // Return the boss identity assigned to the current stage.
    public int bossId() { return bossOrder[stage]; }
    // Look up the current stage's display name.
    public String stageName() { return STAGES[stageId()]; }
    // Look up the current boss's display name by its ID.
    public String bossName() { return BOSSES.get(bossId()); }
    // Check whether the player is at the fourth stage of this loop.
    public boolean isFinalStage() { return stage == 3; }
    // Check whether this loop ends with an EX encounter.
    public boolean hasExAfterYear() { return loop == 1 || loop % 2 == 0; }
    // Choose one EX enemy ID from the available encounters.
    public int randomExId() { return random.nextInt(EX_MONSTERS.length); }

    // Record a qualifying boss defeat toward unlocking Final Exam.
    public void recordBossDefeat(int bossId) {
        if (bossId == AE_ID) finalExamDefeats |= 1;
        else if (bossId == FINAL_EXAM_FAR_ID) finalExamDefeats |= 2;
        else if (bossId == DOME_ID) finalExamDefeats |= 4;
    }

    // Check whether all three required Final Exam defeat flags are set.
    public boolean finalExamReady() { return finalExamDefeats == 7; }

    /** Call only after the current boss is defeated. Returns true after stage four. */
    public boolean completeStage() {
        if (stage < 3) {
            stage++;
            return false;
        }
        return true;
    }

    // Increase difficulty, reset the stage position and shuffle a new year.
    public void nextLoop() {
        loop++;
        stage = 0;
        shuffleYear();
    }

    // Return the health multiplier for the current loop.
    public float healthScale() { return 1f + (loop - 1) * 0.15f; }
    // Return the enemy SP multiplier for the current loop.
    public float spScale() { return 1f + (loop - 1) * 0.10f; }
    // Return the movement and attack-speed multiplier for the current loop.
    public float speedScale() { return 1f + (loop - 1) * 0.04f; }
    // Return the enemy damage multiplier for the current loop.
    public float damageScale() { return 1f + (loop - 1) * 0.10f; }
}
