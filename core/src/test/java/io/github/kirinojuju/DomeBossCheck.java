package io.github.kirinojuju;

import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.flow.RunDirector;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.world.GameSpace;
import io.github.kirinojuju.enemy.boss.DomeBossBehavior;
import io.github.kirinojuju.render.DomeSprite;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.lang.reflect.Proxy;
import java.util.Random;

/** Checks the imported attacks, spawning, and missing-art handling without a GL window. */
public final class DomeBossCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        checkSpawning();
        checkLightning(true);
        checkLightning(false);
        checkLaser();
        checkMissingArt();
        System.out.println("Dome boss verified");
    }

    private static Enemy dome(RunPlan plan) {
        return new Enemy(GameSpace.RIGHT - 60, GameSpace.WALK_TOP - 75, 36,
            190 * plan.healthScale(), 72 * plan.spScale(), 0, 12, RunPlan.DOME_ID);
    }

    private static void checkSpawning() {
        boolean found = false;
        for (int seed = 0; seed < 40; seed++) {
            RunPlan plan = new RunPlan(new Random(seed));
            CombatRoom room = new CombatRoom(new Random(seed));
            for (int stage = 0; stage < 4; stage++) {
                room.begin(RunDirector.Room.BOSS, plan, 0);
                Enemy boss = room.enemies.first();
                check(boss.kind == plan.bossId(), "Room must spawn the planned boss, rather than always Ken");
                if (plan.bossId() == RunPlan.DOME_ID) {
                    found = true;
                    check(boss.dome != null && !boss.warper && !boss.waveCaster,
                        "Dome must use its own attack behavior");
                }
                plan.completeStage();
            }
        }
        check(found, "Dome must be reachable through normal stage selection");
    }

    private static void checkLightning(boolean stayInWarning) {
        RunPlan plan = new RunPlan(new Random(1));
        CombatRoom room = new CombatRoom(new Random(2));
        Enemy boss = dome(plan);
        room.enemies.add(boss);
        boss.sp = boss.maxSp;
        PlayerCharacter player = new PlayerCharacter();
        player.x = 600;
        player.y = 300;
        player.invulnerable = 0;
        room.update(.91f, player, plan);
        check(boss.dome.phase == DomeBossBehavior.Phase.LIGHTNING_CHARGE,
            "Dome must warn before striking");
        float hp = player.hp;
        if (!stayInWarning) player.x += 150;
        room.update(.86f, player, plan);
        check(boss.dome.aimX == 600 && boss.dome.aimY == 300,
            "Lightning must keep the warned location so the player can dodge");
        check(stayInWarning ? player.hp == hp - 22 : player.hp == hp,
            "Lightning must hit inside the warning and miss outside it");
        room.update(.41f, player, plan);
        room.update(.31f, player, plan);
        room.update(.91f, player, plan);
        check(boss.dome.phase == DomeBossBehavior.Phase.LASER_CHARGE,
            "Dome must alternate lightning with laser");
        check(room.shots.size == 0, "Dome must not also fire generic enemy shots");
    }

    private static void checkLaser() {
        RunPlan plan = new RunPlan(new Random(3));
        Enemy boss = dome(plan);
        boss.sp = boss.maxSp;
        boss.dome.nextLaser = true;
        PlayerCharacter player = new PlayerCharacter();
        player.x = 600;
        player.y = boss.y - 12;
        player.invulnerable = 0;
        boss.updateBehavior(new CombatRoom(new Random(5)), .91f, player, plan);
        DomeBossBehavior.State state = boss.dome;
        check(state.phase == DomeBossBehavior.Phase.LASER_CHARGE
            && Math.abs(state.endX - GameSpace.LEFT) < .01f,
            "Laser must warn and stop at the arena edge");
        check(DomeBossBehavior.onBeam(state, player.x, player.y, 17), "Player on beam must be hittable");
        check(!DomeBossBehavior.onBeam(state, player.x, player.y + 60, 17),
            "Player outside beam width must be safe");
        check(!DomeBossBehavior.onBeam(state, state.originX + 10, state.originY, 17),
            "Laser must not hit behind its origin");
        float hp = player.hp;
        boss.updateBehavior(new CombatRoom(new Random(5)), 1.01f, player, plan);
        check(state.phase == DomeBossBehavior.Phase.LASER_FIRE && player.hp == hp - 18,
            "Laser must damage only after charging");
        Enemy other = dome(plan);
        check(other.dome.phase == DomeBossBehavior.Phase.IDLE, "Bosses must not share cast state");
        boss.stunTime = 1;
        float remaining = state.remaining;
        boss.updateBehavior(new CombatRoom(new Random(5)), .2f, player, plan);
        check(state.remaining == remaining, "Stun must pause Dome's cast");
    }

    private static void checkMissingArt() {
        Files previous = Gdx.files;
        try {
            Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
                new Class<?>[] {Files.class}, (proxy, method, args) -> {
                    if (method.getName().equals("internal"))
                        return new FileHandle("build/nonexistent-dome-test-assets/" + args[0]);
                    throw new UnsupportedOperationException(method.getName());
                });
            DomeSprite sprite = new DomeSprite();
            Enemy boss = dome(new RunPlan(new Random(4)));
            sprite.draw(null, boss);
            boss.dome.phase = DomeBossBehavior.Phase.LIGHTNING_STRIKE;
            sprite.drawEffect(null, boss);
            boss.dome.phase = DomeBossBehavior.Phase.LASER_FIRE;
            sprite.drawEffect(null, boss);
            sprite.dispose();
        } finally {
            Gdx.files = previous;
        }
    }
}
