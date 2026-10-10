package io.github.kirinojuju;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;

/** Player movement, resources, weapon ownership and attacks. */
final class PlayerCharacter {
    static final float SKILL_ANIMATION_DURATION = .72f;
    static final float SKILL_SP_COST = 30f;
    static final float LAN_SWING_DURATION = .25f;
    static final float LAN_REACH = 125f;
    static final float LAN_ATTACK_INTERVAL = .44f;
    static final float NOTEBOOK_ATTACK_INTERVAL = .28f;
    static final float NOTEBOOK_BEAM_INTERVAL = .65f;
    private static final float SKILL_RELEASE_TIME = .4f;
    private static final float SKILL_COOLDOWN = 2f;
    private static final float EDGE_X = 30, EDGE_Y = 64;
    enum Weapon { PHYSICAL, MAGICAL }

    float x = GameSpace.WIDTH / 2, y = GameSpace.HEIGHT / 2;
    float faceX = 1, faceY = 0;
    float swingFaceX = 1, swingFaceY = 0;
    float hp = 100, maxHp = 100, sp = 100;
    float physicalBonus, magicalBonus, notebookBonus, speedBonus;
    float attackSpeedBonus, skillDamageBonus;
    boolean lanSlow, lanAoE, lanTether;
    boolean notebookPoison, notebookSplash, notebookHeatBeam;
    float notebookHeat;
    float attackTimer, dashTimer, invulnerable, swingTimer;
    float slowTime;
    float walkTime, skillTime, skillCooldown;
    float boundTime, bindImmunity;
    int bindProgress, lastBindDirection;
    boolean moving, facingRight = true, skillPlaying, skillReleased;
    private Enemy skillTarget;
    private Enemy attackTarget;
    private GameAudio audio;
    private final boolean[] owned = new boolean[Weapon.values().length];
    private Weapon active;

    Weapon activeWeapon() { return active; }
    boolean owns(Weapon weapon) { return owned[weapon.ordinal()]; }
    boolean hasBothWeapons() { return owns(Weapon.PHYSICAL) && owns(Weapon.MAGICAL); }
    Enemy attackTarget() {
        return attackTarget != null && attackTarget.hp > 0 ? attackTarget : null;
    }
    void setAudio(GameAudio audio) { this.audio = audio; }

    void acquire(Weapon weapon) {
        owned[weapon.ordinal()] = true;
        active = weapon;
    }

    void switchTo(Weapon weapon) {
        if (owns(weapon) && active != weapon) {
            active = weapon;
            if (audio != null) audio.weaponSwitch();
        }
    }

    void startRoom() {
        x = GameSpace.WIDTH / 2;
        y = GameSpace.HEIGHT / 2;
        sp = Math.min(100, sp + 30);
        invulnerable = .9f;
        attackTimer = 0;
        walkTime = skillTime = skillCooldown = 0;
        boundTime = bindImmunity = 0;
        slowTime = 0;
        bindProgress = lastBindDirection = 0;
        moving = skillPlaying = skillReleased = false;
        skillTarget = attackTarget = null;
    }

    void setExTechniques(boolean enabled) {
        lanTether = enabled;
        notebookHeatBeam = enabled;
        notebookHeat = 0;
    }

    void update(float dt, CombatRoom combat, boolean touchSkill, boolean mouseAttack) {
        attackTimer = Math.max(0, attackTimer - dt);
        dashTimer = Math.max(0, dashTimer - dt);
        invulnerable = Math.max(0, invulnerable - dt);
        swingTimer = Math.max(0, swingTimer - dt);
        skillCooldown = Math.max(0, skillCooldown - dt);
        slowTime = Math.max(0, slowTime - dt);
        bindImmunity = Math.max(0, bindImmunity - dt);
        if (boundTime > 0) {
            boundTime = Math.max(0, boundTime - dt);
            if (boundTime == 0) releaseBind();
        }
        sp = Math.min(100, sp + 14 * dt);
        if (skillPlaying) {
            advanceSkill(dt, combat);
            return; // Casting locks movement, dash, weapon switching, and normal attacks.
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.E) || touchSkill)
            && tryStartSkill(combat)) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) switchTo(Weapon.PHYSICAL);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) switchTo(Weapon.MAGICAL);
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && hasBothWeapons())
            switchTo(active == Weapon.PHYSICAL ? Weapon.MAGICAL : Weapon.PHYSICAL);

        boolean attacking = mouseAttack || Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if (boundTime > 0) {
            boolean left = Gdx.input.isKeyJustPressed(Input.Keys.A)
                || Gdx.input.isKeyJustPressed(Input.Keys.LEFT);
            boolean right = Gdx.input.isKeyJustPressed(Input.Keys.D)
                || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT);
            if (left != right) escapePress(left ? -1 : 1);
            moving = false;
            walkTime = 0;
            followAttackTarget(combat, attacking);
            if (attacking && attackTimer <= 0) attack(combat);
            return;
        }

        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx--;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx++;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy--;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) dy++;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        moving = length > 0;
        if (length > 0) {
            dx /= length;
            dy /= length;
            faceX = dx;
            faceY = dy;
            if (dx != 0) facingRight = dx > 0;
            walkTime += dt;
            float moveSpeed = (225 + speedBonus) * (slowTime > 0 ? .62f : 1f);
            moveBy(dx * moveSpeed * dt, dy * moveSpeed * dt);
        }
        else walkTime = 0;

        followAttackTarget(combat, attacking);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT))
            dash(length > 0 ? dx : faceX, length > 0 ? dy : faceY);
        if (attacking && attackTimer <= 0) attack(combat);
    }

    boolean dash(float directionX, float directionY) {
        if (dashTimer > 0 || sp < 25) return false;
        moveBy(directionX * 115, directionY * 115);
        sp -= 25;
        dashTimer = .75f;
        invulnerable = Math.max(invulnerable, .22f);
        return true;
    }

    void moveBy(float dx, float dy) {
        x = MathUtils.clamp(x + dx,
            GameSpace.LEFT + EDGE_X, GameSpace.RIGHT - EDGE_X);
        y = MathUtils.clamp(y + dy,
            GameSpace.BOTTOM + EDGE_Y, GameSpace.WALK_TOP - EDGE_Y);
    }

    void lockAttackTarget(CombatRoom combat, float aimX, float aimY) {
        attackTarget = null;
        Enemy pointed = combat.closestEnemy(aimX, aimY);
        if (pointed != null) {
            float dx = pointed.x - aimX, dy = pointed.y - aimY;
            float pickRadius = pointed.radius + 40;
            if (dx * dx + dy * dy <= pickRadius * pickRadius) attackTarget = pointed;
        }
        if (attackTarget == null) attackTarget = combat.closestEnemy(x, y);
        faceAttackTarget();
    }

    private void followAttackTarget(CombatRoom combat, boolean attacking) {
        if (!attacking) {
            attackTarget = null;
            return;
        }
        if (attackTarget == null || attackTarget.hp <= 0)
            attackTarget = combat.closestEnemy(x, y);
        faceAttackTarget();
    }

    private void faceAttackTarget() {
        if (attackTarget == null || attackTarget.hp <= 0) return;
        float dx = attackTarget.x - x, dy = attackTarget.y - y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        if (distance < .001f) return;
        faceX = dx / distance;
        faceY = dy / distance;
        if (Math.abs(dx) > .001f) facingRight = dx > 0;
    }

    void attack(CombatRoom combat) {
        if (active == Weapon.PHYSICAL) {
            attackTimer = LAN_ATTACK_INTERVAL / (1 + attackSpeedBonus);
            swingTimer = LAN_SWING_DURATION;
            swingFaceX = faceX;
            swingFaceY = faceY;
            if (audio != null) audio.lanAttack();
            combat.damageArc(x, y, swingFaceX, swingFaceY,
                LAN_REACH + (lanAoE ? 35 : 0), lanAoE ? 85 : 65,
                27 + physicalBonus, lanSlow, lanTether);
        } else if (active == Weapon.MAGICAL && sp >= 10) {
            sp -= 10;
            Enemy target = attackTarget();
            float dx = target == null ? faceX : target.x - x;
            float dy = target == null ? faceY : target.y - y;
            float length = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
            if (notebookHeatBeam && notebookHeat >= 100) {
                notebookHeat = 0;
                attackTimer = NOTEBOOK_BEAM_INTERVAL / (1 + attackSpeedBonus);
                combat.fireNotebookBeam(x, y, dx / length, dy / length,
                    62 + magicalBonus + notebookBonus, notebookPoison);
                if (audio != null) audio.skillImpact();
            } else {
                attackTimer = NOTEBOOK_ATTACK_INTERVAL / (1 + attackSpeedBonus);
                if (notebookHeatBeam) notebookHeat = Math.min(100, notebookHeat + 25);
                combat.fireFriendly(x, y, dx / length * 540, dy / length * 540,
                    20 + magicalBonus + notebookBonus, target,
                    notebookPoison, notebookSplash);
                if (audio != null) audio.notebookAttack();
            }
        }
    }

    boolean tryStartSkill(CombatRoom combat) {
        if (skillPlaying || skillCooldown > 0 || sp < SKILL_SP_COST) return false;
        skillTarget = combat.closestEnemy(x, y);
        if (skillTarget == null) return false;
        sp -= SKILL_SP_COST;
        skillPlaying = true;
        skillReleased = false;
        skillTime = 0;
        skillCooldown = SKILL_COOLDOWN;
        if (audio != null) audio.skillCast();
        moving = false;
        walkTime = 0;
        swingTimer = 0;
        faceSkillTarget(combat);
        return true;
    }

    void advanceSkill(float dt, CombatRoom combat) {
        if (!skillPlaying) return;
        faceSkillTarget(combat);
        skillTime += dt;
        if (!skillReleased && skillTime >= SKILL_RELEASE_TIME) {
            combat.firePowerBall(x + faceX * 26, y + faceY * 26,
                faceX * 420, faceY * 420,
                42 + magicalBonus + skillDamageBonus, skillTarget);
            skillReleased = true;
        }
        if (skillTime >= SKILL_ANIMATION_DURATION) {
            skillPlaying = false;
            skillTarget = null;
        }
    }

    private void faceSkillTarget(CombatRoom combat) {
        if (skillTarget == null || skillTarget.hp <= 0)
            skillTarget = combat.closestEnemy(x, y);
        if (skillTarget == null) return;
        float dx = skillTarget.x - x, dy = skillTarget.y - y;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length < .001f) return;
        faceX = dx / length;
        faceY = dy / length;
        if (Math.abs(dx) > .001f) facingRight = dx > 0;
    }

    void hurt(float damage) {
        if (invulnerable > 0) return;
        hp = Math.max(0, hp - damage);
        if (audio != null) audio.hit(false);
        invulnerable = .65f;
    }

    boolean isBound() { return boundTime > 0; }

    void bind(float duration) {
        if (boundTime > 0 || bindImmunity > 0) return;
        boundTime = duration;
        bindProgress = 0;
        lastBindDirection = 0;
        moving = false;
    }

    void escapePress(int direction) {
        if (boundTime <= 0 || (direction != -1 && direction != 1)) return;
        if (direction != lastBindDirection) {
            bindProgress++;
            lastBindDirection = direction;
            if (bindProgress >= 6) releaseBind();
        }
    }

    private void releaseBind() {
        boundTime = 0;
        bindProgress = 0;
        lastBindDirection = 0;
        bindImmunity = 1.2f;
    }

    void heal(float amount) { hp = Math.min(maxHp, hp + amount); }
    void recoverSp(float amount) { sp = Math.min(100, sp + amount); }

    void applyExBuff(int exId) {
        if (exId == 0) {
            maxHp += 25;
            heal(45);
        } else if (exId == 1) {
            speedBonus += 30;
            heal(20);
        } else {
            physicalBonus += 8;
            magicalBonus += 6;
            heal(20);
        }
    }
}
