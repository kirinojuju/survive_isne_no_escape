package io.github.kirinojuju;

import com.badlogic.gdx.math.MathUtils;

class Enemy {
    float x, y, radius, hp, maxHp, sp, maxSp, speed, damage;
    float contactTimer, skillTimer, armor, maxArmor, reflectTimer, summonTimer;
    float warpCooldown, warpWindup, warpX, warpY, warpReturnTime;
    final float homeX, homeY;
    int warpStrikes;
    float animationTime;
    float talgoCooldown, talgoCharge, talgoSafeX, talgoSafeY, talgoFlashTime;
    float stunTime, slowTime, poisonTime, poisonDps;
    float tetherTime, tetherCooldown;
    float exStateTime, exMeter, exAimX, exAimY, exFlashTime, exObjectTimer;
    int exState, exStoredDust, exConfidence;
    boolean summoner, reflector, warper, artillery, waveCaster;
    int kind;

    public Enemy(float x, float y, float radius, float hp, float maxSp,
                 float speed, float damage, int kind) {
        this.x = x;
        this.y = y;
        this.homeX = x;
        this.homeY = y;
        this.radius = radius;
        this.hp = hp;
        this.maxHp = hp;
        this.maxSp = maxSp;
        this.sp = maxSp * .35f;
        this.speed = speed;
        this.damage = damage;
        this.kind = kind;
        this.summonTimer = 3.8f;
    }
}
