package io.github.kirinojuju.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** The four stages and four distinct Ajarns chosen for one school-year loop. */
public final class RunPlan {
    // Boss identity, independent of combat family: Sati = C++, M = OOP;
    // Ae, Far and Dome = Network; Ken = Algorithm.
    public static final int SATI_ID = 0, AE_ID = 1, M_ID = 2, DOME_ID = 3;
    public static final int KEN_ID = 4, VOLTAGE_ID = 5, FAR_ID = 6, THESIS_ID = 7;
    public static final String[] STAGES = {
        "Programming Lab", "Circuit Workshop", "Network Center", "Capstone Studio"
    };
    public static final String[] BOSSES = {
        "Ajarn Sati", "Ajarn Ae", "Ajarn M", "Ajarn Dome",
        "Ajarn Ken", "Ajarn Voltage", "Ajarn Far", "Ajarn Thesis"
    };
    public static final String[] EX_MONSTERS = {
        "Liquid Cat", "Allergic Vacuum", "Insecure Sword"
    };

    private final Random random;
    private final int[] stageOrder = new int[4];
    private final int[] bossOrder = new int[4];
    private int loop = 1;
    private int stage = 0;
    private int finalExamDefeats;

    public RunPlan(Random random) {
        this.random = random;
        shuffleYear();
    }

    private void shuffleYear() {
        List<Integer> stages = new ArrayList<Integer>();
        List<Integer> bosses = new ArrayList<Integer>();
        for (int i = 0; i < STAGES.length; i++) stages.add(i);
        // One boss from each combat family keeps every loop varied.
        for (int family = 0; family < 4; family++)
            bosses.add(family + (random.nextBoolean() ? 4 : 0));
        Collections.shuffle(stages, random);
        Collections.shuffle(bosses, random);
        for (int i = 0; i < 4; i++) {
            stageOrder[i] = stages.get(i);
            bossOrder[i] = bosses.get(i);
        }
    }

    public int loop() { return loop; }
    public int stage() { return stage; }
    public int stageId() { return stageOrder[stage]; }
    public int bossId() { return bossOrder[stage]; }
    public String stageName() { return STAGES[stageId()]; }
    public String bossName() { return BOSSES[bossId()]; }
    public boolean isFinalStage() { return stage == 3; }
    public boolean hasExAfterYear() { return loop == 1 || loop % 2 == 0; }
    public int randomExId() { return random.nextInt(EX_MONSTERS.length); }

    public void recordBossDefeat(int bossId) {
        if (bossId == AE_ID) finalExamDefeats |= 1;
        else if (bossId == FAR_ID) finalExamDefeats |= 2;
        else if (bossId == DOME_ID) finalExamDefeats |= 4;
    }

    public boolean finalExamReady() { return finalExamDefeats == 7; }

    /** Call only after the current boss is defeated. Returns true after stage four. */
    public boolean completeStage() {
        if (stage < 3) {
            stage++;
            return false;
        }
        return true;
    }

    public void nextLoop() {
        loop++;
        stage = 0;
        shuffleYear();
    }

    public float healthScale() { return 1f + (loop - 1) * 0.15f; }
    public float spScale() { return 1f + (loop - 1) * 0.10f; }
    public float speedScale() { return 1f + (loop - 1) * 0.04f; }
    public float damageScale() { return 1f + (loop - 1) * 0.10f; }
}
