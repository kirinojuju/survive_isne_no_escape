package io.github.kirinojuju.render.sprite.boss;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.boss.BossBody;
import io.github.kirinojuju.enemy.boss.SatiBoss;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.render.OptionalAssetTexture;
import io.github.kirinojuju.render.sprite.EnemySprite;

/** Sati's C++ element, transformation animation, and thrown spear art. */
public class SatiSprite extends EnemySprite {
    private static final String ROOT = "characters/boss/Tprogram(Sati)/skill/cpp_element/";
    private static final float ELEMENT_SIZE = 92;
    private static final float DRAW_SIZE = BossBody.SATI_DRAW_SIZE;
    private final Texture[] body = frames(ROOT + "satiBody%d.png", 1, 7, false);
    private final Texture[] attack = frames(ROOT + "satiActack%d.png", 1, 10, false);
    private static final float SPEAR_WIDTH = 192;
    private static final float SPEAR_LENGTH = 192;
    private final Texture[] idle = frames(ROOT + "cpp_idle%02d.png", 0, 6, true);
    private final Texture[] transform = frames(ROOT + "cpp_transform%02d.png", 0, 16, true);
    private final Texture spear = load(ROOT + "cpp_spear00.png");
    private final TextureRegion spearRegion = new TextureRegion(spear);

    public SatiSprite() { super(RunPlan.SATI_ID); }

    @Override public void draw(SpriteBatch batch, Enemy enemy, float playerX) {
        SatiBoss boss = (SatiBoss) enemy;
        drawBody(batch, boss);
        drawElement(batch, boss);
    }

    public void drawElement(SpriteBatch batch, SatiBoss boss) {
        Texture frame;
        if (boss.satiCharge > 0) {
            float progress = 1 - boss.satiCharge / SatiBoss.CHARGE_DURATION;
            int index = MathUtils.clamp((int)(progress * transform.length),
                0, transform.length - 1);
            frame = transform[index];
        } else {
            frame = idle[(int)(boss.animationTime / .12f) % idle.length];
        }
        float x = SatiBoss.elementX(boss);
        float y = SatiBoss.elementY(boss);
        batch.draw(frame, x - ELEMENT_SIZE / 2, y - ELEMENT_SIZE / 2,
            ELEMENT_SIZE, ELEMENT_SIZE);
    }

    public boolean hasBody() { return OptionalAssetTexture.frame(body, 0) != null; }

    public void drawBody(SpriteBatch batch, SatiBoss boss) {
        Texture frame;
        if (boss.satiCharge > 0) {
            float progress = 1 - boss.satiCharge / SatiBoss.CHARGE_DURATION;
            frame = OptionalAssetTexture.frame(attack, (int)(progress * attack.length));
        } else {
            frame = OptionalAssetTexture.frame(body, (int)(boss.animationTime / .12f) % body.length);
        }
        if (frame == null) frame = OptionalAssetTexture.frame(body, 0);
        if (frame == null) return;
        drawBody(batch, boss, frame, DRAW_SIZE, false);
    }

    public void drawSpear(SpriteBatch batch, CombatRoom.Shot shot) {
        float rotation = MathUtils.atan2(shot.vy, shot.vx)
            * MathUtils.radiansToDegrees + 90;
        batch.draw(spearRegion, shot.x - SPEAR_WIDTH / 2,
            shot.y - SPEAR_LENGTH / 2, SPEAR_WIDTH / 2, SPEAR_LENGTH / 2,
            SPEAR_WIDTH, SPEAR_LENGTH, 1, 1, rotation);
    }

}
