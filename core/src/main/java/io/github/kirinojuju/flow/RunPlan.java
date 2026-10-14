package io.github.kirinojuju.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** The four stages and four distinct Ajarns chosen for one school-year loop. */
public final class RunPlan {
    public static final int SATI_ID = 0, AE_ID = 1, M_ID = 2, DOME_ID = 3;
    public static final int KEN_ID = 4;
    public static final String[] STAGES = {
        "Programming Lab", "Circuit Workshop", "Network Center", "Capstone Studio"
    };
    private static final int[] ACTIVE_BOSSES = {SATI_ID, AE_ID, M_ID, DOME_ID, KEN_ID};

    public static int[] bossIds() { return ACTIVE_BOSSES.clone(); }

    public static boolean isBossId(int id) {
        for (int boss : ACTIVE_BOSSES) if (boss == id) return true;
        return false;
    }

    public static String bossName(int id) {
        switch (id) {
            case SATI_ID: return "Ajarn Sati";
            case AE_ID: return "Ajarn Ae";
            case M_ID: return "Ajarn M";
            case DOME_ID: return "Ajarn Dome";
            case KEN_ID: return "Ajarn Ken";
            default: return "Unknown Boss";
        }
    }
    public static final String[] EX_MONSTERS = {
        "Liquid Cat", "Allergic Vacuum", "Insecure Sword"
    };

    private final Random random;
    private final int[] stageOrder = new int[4];
    private final int[] bossOrder = new int[4];
    private int loop = 1;
    private int stage = 0;
    private boolean aeDefeated, domeDefeated;

    public RunPlan(Random random) {
        this.random = random;
        shuffleYear();
    }

    private void shuffleYear() {
        List<Integer> stages = new ArrayList<Integer>();
        List<Integer> bosses = new ArrayList<Integer>();
        for (int i = 0; i < STAGES.length; i++) stages.add(i);
        // One boss from each combat family keeps every loop varied.
        bosses.add(random.nextBoolean() ? KEN_ID : SATI_ID);
        bosses.add(AE_ID);
        bosses.add(M_ID);
        bosses.add(DOME_ID);
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
    public String bossName() { return bossName(bossId()); }
    public boolean isFinalStage() { return stage == 3; }
    public boolean hasExAfterYear() { return loop == 1 || loop % 2 == 0; }
    public int randomExId() { return random.nextInt(EX_MONSTERS.length); }

    public void recordBossDefeat(int bossId) {
        if (bossId == AE_ID) aeDefeated = true;
        else if (bossId == DOME_ID) domeDefeated = true;
    }

    public boolean finalExamReady() { return aeDefeated && domeDefeated; }

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
