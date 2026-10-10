package io.github.kirinojuju.weapon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.audio.GameAudio;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.render.OptionalAssetTexture;
import io.github.kirinojuju.weapon.WeaponAttack.WeaponAttackSpec;

/** Thrown notebook and EX heat beam, separate from input and movement. */
public final class NotebookWeaponAttack extends AbstractWeaponAttack {
    @Override
    public boolean attack(PlayerCharacter player, CombatRoom combat, GameAudio audio) {
        boolean beamReady = player.notebookHeatBeam && player.notebookHeat >= 100;
        WeaponAttackSpec spec = new WeaponAttackSpec(
            (beamReady ? 62 : 20) + player.magicalBonus + player.notebookBonus,
            beamReady ? PlayerCharacter.NOTEBOOK_BEAM_INTERVAL
                : PlayerCharacter.NOTEBOOK_ATTACK_INTERVAL,
            10);
        spec.projectileSpeed = 540;
        spec.poison = player.notebookPoison;
        spec.splash = player.notebookSplash;
        spec.beam = beamReady;
        modify(player, spec);
        if (player.sp < spec.spCost) return false;
        player.sp -= spec.spCost;
        player.attackTimer = spec.interval / (1 + player.attackSpeedBonus);
        CombatRoom.Enemy target = player.attackTarget();
        float dx = target == null ? player.faceX : target.x - player.x;
        float dy = target == null ? player.faceY : target.y - player.y;
        float length = Math.max(.001f, (float)Math.sqrt(dx * dx + dy * dy));
        if (spec.beam) {
            player.notebookHeat = 0;
            combat.fireNotebookBeam(player.x, player.y, dx / length, dy / length,
                spec.damage, spec.poison);
            if (audio != null) audio.skillImpact();
        } else {
            if (player.notebookHeatBeam)
                player.notebookHeat = Math.min(100, player.notebookHeat + 25);
            CombatRoom.Shot shot = combat.fireFriendly(player.x, player.y,
                dx / length * spec.projectileSpeed,
                dy / length * spec.projectileSpeed,
                spec.damage, target, spec.poison, spec.splash);
            shot.notebook = true;
            if (audio != null) audio.notebookAttack();
        }
        afterAttack(player, combat);
        return true;
    }

    /** Notebook art lives with its weapon system; combat owns animation timing. */
    public static final class NotebookSprite {
        private static final String ROOT = "weapons/notebook/";
        private static final float FLYING_FRAME_TIME = .06f;
        private static final float PROJECTILE_SIZE = 48;
        private static final float IMPACT_SIZE = 64;
        private final Texture icon = OptionalAssetTexture.load(ROOT + "notebook.png");
        private final Texture[] flying = loadFrames("notebook_flying", 9);
        private final Texture[] explode = loadFrames("notebook_explode", 6);

        private static Texture[] loadFrames(String prefix, int count) {
            Texture[] frames = new Texture[count];
            for (int i = 0; i < count; i++)
                frames[i] = OptionalAssetTexture.load(ROOT + prefix + (i + 1) + ".png");
            return frames;
        }

        public void drawShot(SpriteBatch batch, CombatRoom.Shot shot) {
            int index = (int)(shot.animationTime / FLYING_FRAME_TIME) % flying.length;
            batch.draw(flying[index], shot.x - PROJECTILE_SIZE / 2,
                shot.y - PROJECTILE_SIZE / 2, PROJECTILE_SIZE, PROJECTILE_SIZE);
        }

        public void drawImpact(SpriteBatch batch, CombatRoom.NotebookImpact impact) {
            int index = MathUtils.clamp((int)(impact.time / CombatRoom.NOTEBOOK_IMPACT_DURATION
                * explode.length), 0, explode.length - 1);
            batch.draw(explode[index], impact.x - IMPACT_SIZE / 2,
                impact.y - IMPACT_SIZE / 2, IMPACT_SIZE, IMPACT_SIZE);
        }

        public void drawIcon(SpriteBatch batch, float x, float y) {
            batch.draw(icon, x, y, 32, 32);
        }

        public void dispose() {
            icon.dispose();
            for (Texture frame : flying) frame.dispose();
            for (Texture frame : explode) frame.dispose();
        }
    }
}
