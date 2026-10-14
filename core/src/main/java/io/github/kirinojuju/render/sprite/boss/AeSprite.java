package io.github.kirinojuju.render.sprite.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.boss.AeBoss;
import io.github.kirinojuju.enemy.boss.BossBody;
import io.github.kirinojuju.flow.RunPlan;
import io.github.kirinojuju.render.OptionalAssetTexture;
import io.github.kirinojuju.render.sprite.EnemySprite;
import io.github.kirinojuju.world.GameSpace;

/** Ae's body animation and optional vertical laser art. */
public class AeSprite extends EnemySprite {
    private static final String ROOT = "characters/boss/Tnetwork(Ae)/";
    private static final float DRAW_SIZE = BossBody.AE_DRAW_SIZE;
    private final Texture body = optional(ROOT + "aeBody.png");
    private final Texture[] bodyFrames = frames(ROOT + "aeBody%d.png", 1, 7, false);
    private final Texture[] attackFrames = frames(ROOT + "aeActack%d.png", 1, 8, false);
    private final Texture laser = optional(ROOT + "aeLaser.png");

    public AeSprite() { super(RunPlan.AE_ID); }
    @Override public boolean supports(Enemy enemy) { return enemy instanceof AeBoss; }

    @Override public void draw(SpriteBatch batch, Enemy enemy, float playerX) {
        AeBoss boss = (AeBoss)enemy;
        Texture frame;
        if (boss.attackAnimationTime > 0) {
            int index = (int)((1 - boss.attackAnimationTime / .4f) * attackFrames.length);
            frame = OptionalAssetTexture.frame(attackFrames, index);
        } else if (boss.laserPhase() != AeBoss.LaserPhase.IDLE || boss.warpWindup > 0) {
            frame = OptionalAssetTexture.frame(attackFrames,
                (int)(boss.animationTime / .1f) % attackFrames.length);
        } else {
            frame = OptionalAssetTexture.frame(bodyFrames,
                (int)(boss.animationTime / .12f) % bodyFrames.length);
        }
        if (frame == null) frame = body;
        drawBody(batch, boss, frame, DRAW_SIZE, false);
    }

    public void drawLaserShapes(ShapeRenderer shapes, AeBoss boss) {
        if (boss.getHp() <= 0 || boss.laserPhase() == AeBoss.LaserPhase.IDLE) return;
        float height = GameSpace.WALK_TOP - GameSpace.BOTTOM;
        if (boss.laserPhase() == AeBoss.LaserPhase.WARNING) {
            shapes.setColor(Color.YELLOW);
            shapes.rect(boss.laserX() - 3, GameSpace.BOTTOM, 6, height);
            // Mark the direction and show the width that will become dangerous.
            shapes.triangle(boss.laserX(), GameSpace.BOTTOM + 30,
                boss.laserX() + 32, GameSpace.BOTTOM + 42,
                boss.laserX(), GameSpace.BOTTOM + 54);
        } else if (laser == null) {
            shapes.setColor(Color.CYAN);
            shapes.rect(boss.laserX() - AeBoss.LASER_HALF_WIDTH, GameSpace.BOTTOM,
                AeBoss.LASER_HALF_WIDTH * 2, height);
            shapes.setColor(Color.WHITE);
            shapes.rect(boss.laserX() - 5, GameSpace.BOTTOM, 10, height);
        }
    }

    public void drawLaser(SpriteBatch batch, AeBoss boss) {
        if (laser == null || boss.getHp() <= 0 || boss.laserPhase() != AeBoss.LaserPhase.SWEEP) return;
        batch.draw(laser, boss.laserX() - AeBoss.LASER_HALF_WIDTH, GameSpace.BOTTOM,
            AeBoss.LASER_HALF_WIDTH * 2, GameSpace.WALK_TOP - GameSpace.BOTTOM);
    }

    public boolean hasBody() { return body != null || OptionalAssetTexture.frame(bodyFrames, 0) != null; }

}
