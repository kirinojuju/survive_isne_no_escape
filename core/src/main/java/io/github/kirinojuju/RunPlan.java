package io.github.kirinojuju;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** The four stages and four distinct Ajarns chosen for one school-year loop. */
public final class RunPlan {
    public static final int TALGO_ID = 4;
    public static final String[] STAGES = {
        "Programming Lab", "Circuit Workshop", "Network Center", "Capstone Studio"
    };
    public static final String[] BOSSES = {
        "Ajarn Sati", "Ajarn Ohm", "Ajarn Packet", "Ajarn Deadline",
        "Ajarn Talgo (Ken)", "Ajarn Voltage", "Ajarn Firewall", "Ajarn Thesis"
    };
    public static final String[] EX_MONSTERS = {
        "Liquid Cat", "Allergic Vacuum", "Insecure Sword"
    };

    private final Random random;
    private final int[] stageOrder = new int[4];
    private final int[] bossOrder = new int[4];
    private int loop = 1;
    private int stage = 0;

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
    public boolean hasExAfterYear() { return loop % 2 == 0; }
    public int randomExId() { return random.nextInt(EX_MONSTERS.length); }

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

    public float healthScale() { return 1f + (loop - 1) * 0.25f; }
    public float spScale() { return 1f + (loop - 1) * 0.18f; }
    public float speedScale() { return 1f + (loop - 1) * 0.07f; }
    public float damageScale() { return 1f + (loop - 1) * 0.18f; }
}
