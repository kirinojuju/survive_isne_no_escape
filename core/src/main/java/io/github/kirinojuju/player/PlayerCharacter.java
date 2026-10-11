package io.github.kirinojuju.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.weapon.LanWeaponAttack;
import io.github.kirinojuju.weapon.NotebookWeaponAttack;
import io.github.kirinojuju.weapon.WeaponAbility;
import io.github.kirinojuju.weapon.WeaponAttack;
import io.github.kirinojuju.world.GameSpace;
import io.github.kirinojuju.world.IceWall;
import io.github.kirinojuju.world.RoomObstacles;

/** Player movement, resources, weapon ownership and attacks. */
public final class PlayerCharacter {
    public static final float SKILL_ANIMATION_DURATION = .72f;
    public static final float SKILL_SP_COST = 30f;
    public static final float ICE_WALL_SP_COST = 25f;
    public static final float ICE_WALL_COOLDOWN = 7f;
    public static final float LAN_SWING_DURATION = .25f;
    public static final float LAN_REACH = 125f;
    public static final float LAN_ATTACK_INTERVAL = .44f;
    public static final float LAN_WAVE_COOLDOWN = .1f;
    public static final float NOTEBOOK_ATTACK_INTERVAL = .28f;
    public static final float NOTEBOOK_BEAM_INTERVAL = .65f;
    private static final float SKILL_RELEASE_TIME = .4f;
    private static final float SKILL_COOLDOWN = 2f;
    private static final float EDGE_X = 30, EDGE_Y = 64;
    public enum Weapon { PHYSICAL, MAGICAL }

    public float x = GameSpace.WIDTH / 2, y = GameSpace.HEIGHT / 2;
    public float faceX = 1, faceY = 0;
    public float swingFaceX = 1, swingFaceY = 0;
    public float hp = 100, maxHp = 100, sp = 100;
    public final PotionInventory potions = new PotionInventory();
    public float physicalBonus, magicalBonus, notebookBonus, speedBonus;
    public float attackSpeedBonus, skillDamageBonus;
    public boolean lanSlow, lanAoE, lanTether;
    public boolean notebookPoison, notebookSplash, notebookHeatBeam;
    public float notebookHeat;
    public float attackTimer, dashTimer, invulnerable, swingTimer;
    public float lanWaveCooldown;
    public float slowTime;
    public float walkTime, skillTime, skillCooldown;
    public float iceWallCooldown;
    public float boundTime, bindImmunity;
    public int bindProgress, lastBindDirection;
    public boolean moving, facingRight = true, skillPlaying, skillReleased;
    private CombatRoom.Enemy skillTarget;
    private CombatRoom.Enemy attackTarget;
    private GameAudio audio;
    private final boolean[] owned = new boolean[Weapon.values().length];
    private final WeaponAttack[] attacks = {new LanWeaponAttack(), new NotebookWeaponAttack()};
    private Weapon active;
    private boolean horizontalOnly;
    private RoomObstacles obstacles;
    private IceWall iceWall;

    public Weapon activeWeapon() { return active; }
    public boolean owns(Weapon weapon) { return owned[weapon.ordinal()]; }
    public boolean hasBothWeapons() { return owns(Weapon.PHYSICAL) && owns(Weapon.MAGICAL); }
    public CombatRoom.Enemy attackTarget() {
        return attackTarget != null && attackTarget.hp > 0 ? attackTarget : null;
    }
    public void setAudio(GameAudio audio) { this.audio = audio; }
    public void setObstacles(RoomObstacles obstacles) { this.obstacles = obstacles; }
    public void setIceWall(IceWall iceWall) { this.iceWall = iceWall; }

    /** Teammates can attach an ability to one weapon without changing input handling. */
    public void addWeaponAbility(Weapon weapon, WeaponAbility ability) {
        attacks[weapon.ordinal()].addAbility(ability);
    }

    public void acquire(Weapon weapon) {
        owned[weapon.ordinal()] = true;
        active = weapon;
    }

    public void switchTo(Weapon weapon) {
        if (owns(weapon) && active != weapon) {
            active = weapon;
            if (audio != null) audio.weaponSwitch();
        }
    }

    public void startRoom(boolean horizontalOnly) {
        obstacles = null;
        iceWall = null;
        this.horizontalOnly = horizontalOnly;
        x = GameSpace.WIDTH / 2;
        y = horizontalOnly ? GameSpace.SERVER_PLAYER_Y : GameSpace.HEIGHT / 2;
        sp = Math.min(100, sp + 30);
        invulnerable = .9f;
        attackTimer = 0;
        walkTime = skillTime = skillCooldown = iceWallCooldown = lanWaveCooldown = 0;
        boundTime = bindImmunity = 0;
        slowTime = 0;
        bindProgress = lastBindDirection = 0;
        moving = skillPlaying = skillReleased = false;
        skillTarget = attackTarget = null;
    }

    public void setExTechniques(boolean enabled) {
        lanTether = enabled;
        notebookHeatBeam = enabled;
        notebookHeat = 0;
    }

    public void update(float dt, CombatRoom combat, boolean touchSkill, boolean mouseAttack) {
        update(dt, combat, touchSkill, false, false, mouseAttack);
    }

    public void update(float dt, CombatRoom combat, boolean touchSkill,
                       boolean touchIceWall, boolean mouseAttack) {
        update(dt, combat, touchSkill, touchIceWall, false, mouseAttack);
    }

    public void update(float dt, CombatRoom combat, boolean touchSkill,
                       boolean touchIceWall, boolean touchLanWave, boolean mouseAttack) {
        attackTimer = Math.max(0, attackTimer - dt);
        dashTimer = Math.max(0, dashTimer - dt);
        invulnerable = Math.max(0, invulnerable - dt);
        swingTimer = Math.max(0, swingTimer - dt);
        lanWaveCooldown = Math.max(0, lanWaveCooldown - dt);
        skillCooldown = Math.max(0, skillCooldown - dt);
        iceWallCooldown = Math.max(0, iceWallCooldown - dt);
        slowTime = Math.max(0, slowTime - dt);
        bindImmunity = Math.max(0, bindImmunity - dt);
        if (boundTime > 0) {
            boundTime = Math.max(0, boundTime - dt);
            if (boundTime == 0) releaseBind();
        }
        sp = Math.min(100, sp + 14 * dt);
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) potions.useHp(this);
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) potions.useSp(this);
        if (skillPlaying) {
            advanceSkill(dt, combat);
            return; // Casting locks movement, dash, weapon switching, and normal attacks.
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.E) || touchSkill)
            && tryStartSkill(combat)) return;
        if ((Gdx.input.isKeyJustPressed(Input.Keys.F) || touchIceWall)
            && tryStartIceWall(combat)) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) || touchLanWave)
            tryLanWave(combat);

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) switchTo(Weapon.PHYSICAL);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) switchTo(Weapon.MAGICAL);
        if ((Gdx.input.isKeyJustPressed(Input.Keys.TAB)
            || Gdx.input.isKeyJustPressed(Input.Keys.Q)) && hasBothWeapons())
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
        if (horizontalOnly) dy = 0;
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

    public boolean dash(float directionX, float directionY) {
        if (dashTimer > 0 || sp < 25) return false;
        if (horizontalOnly) {
            directionX = Math.abs(directionX) > .001f
                ? Math.signum(directionX) : facingRight ? 1 : -1;
            directionY = 0;
        }
        float dx = directionX * 115, dy = directionY * 115;
        float hit = iceWall == null ? -1 : iceWall.hitFraction(x, y, x + dx, y + dy, 17);
        if (obstacles != null) {
            float tableHit = obstacles.hitFraction(x, y, x + dx, y + dy, 17);
            if (tableHit >= 0 && (hit < 0 || tableHit < hit)) hit = tableHit;
        }
        if (hit >= 0) {
            float fraction = Math.max(0, hit - .001f);
            dx *= fraction;
            dy *= fraction;
        }
        moveBy(dx, dy);
        sp -= 25;
        dashTimer = .75f;
        invulnerable = Math.max(invulnerable, .22f);
        return true;
    }

    public void moveBy(float dx, float dy) {
        float nextX = MathUtils.clamp(x + dx,
            GameSpace.LEFT + EDGE_X, GameSpace.RIGHT - EDGE_X);
        if (obstacles != null) nextX = obstacles.moveX(x, y, nextX - x, 17);
        if (iceWall != null) nextX = iceWall.moveX(x, y, nextX - x, 17);
        x = nextX;
        float nextY = horizontalOnly ? GameSpace.SERVER_PLAYER_Y
            : MathUtils.clamp(y + dy,
                GameSpace.BOTTOM + EDGE_Y, GameSpace.WALK_TOP - EDGE_Y);
        if (obstacles != null) nextY = obstacles.moveY(x, y, nextY - y, 17);
        if (iceWall != null) nextY = iceWall.moveY(x, y, nextY - y, 17);
        y = nextY;
    }

    public void lockAttackTarget(CombatRoom combat, float aimX, float aimY) {
        attackTarget = null;
        CombatRoom.Enemy pointed = combat.closestEnemy(aimX, aimY);
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

    public void attack(CombatRoom combat) {
        if (active != null) attacks[active.ordinal()].attack(this, combat, audio);
    }

    public boolean tryLanWave(CombatRoom combat) {
        if (active != Weapon.PHYSICAL || lanWaveCooldown > 0) return false;
        CombatRoom.Enemy target = attackTarget();
        float dx = target == null ? faceX : target.x - x;
        float dy = target == null ? faceY : target.y - y;
        float length = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        dx /= length;
        dy /= length;
        faceX = dx;
        faceY = dy;
        swingFaceX = dx;
        swingFaceY = dy;
        combat.fireLanWave(x + dx * 24, y + dy * 24, dx, dy, 38 + physicalBonus);
        lanWaveCooldown = LAN_WAVE_COOLDOWN;
        if (audio != null) audio.lanAttack();
        return true;
    }

    public boolean tryStartIceWall(CombatRoom combat) {
        if (skillPlaying || iceWallCooldown > 0 || sp < ICE_WALL_SP_COST) return false;
        iceWall = combat.iceWall;
        if (!combat.raiseIceWall(x, y, faceX, faceY)) return false;
        sp -= ICE_WALL_SP_COST;
        iceWallCooldown = ICE_WALL_COOLDOWN;
        if (audio != null) audio.skillCast();
        return true;
    }

    public boolean tryStartSkill(CombatRoom combat) {
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

    public void advanceSkill(float dt, CombatRoom combat) {
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

    public void hurt(float damage) {
        if (invulnerable > 0) return;
        hp = Math.max(0, hp - damage);
        if (audio != null) audio.hit(false);
        invulnerable = .65f;
    }

    public boolean isBound() { return boundTime > 0; }

    public void bind(float duration) {
        if (boundTime > 0 || bindImmunity > 0) return;
        boundTime = duration;
        bindProgress = 0;
        lastBindDirection = 0;
        moving = false;
    }

    public void escapePress(int direction) {
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

    public void heal(float amount) { hp = Math.min(maxHp, hp + amount); }
    public void recoverSp(float amount) { sp = Math.min(100, sp + amount); }

    public void applyExBuff(int exId) {
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
