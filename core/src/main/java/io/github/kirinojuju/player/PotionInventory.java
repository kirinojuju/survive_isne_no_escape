package io.github.kirinojuju.player;

/** Consumables persist between rooms; a new player starts with two of each. */
public final class PotionInventory {
    public static final float HP_RESTORE = 40, SP_RESTORE = 35;
    private int hpCount = 2, spCount = 2;

    public int hpCount() { return hpCount; }
    public int spCount() { return spCount; }

    public boolean useHp(PlayerCharacter player) {
        if (hpCount == 0 || player.hp <= 0 || player.hp >= player.maxHp) return false;
        player.heal(HP_RESTORE);
        hpCount--;
        return true;
    }

    public boolean useSp(PlayerCharacter player) {
        if (spCount == 0 || player.hp <= 0 || player.sp >= 100) return false;
        player.recoverSp(SP_RESTORE);
        spCount--;
        return true;
    }
}
