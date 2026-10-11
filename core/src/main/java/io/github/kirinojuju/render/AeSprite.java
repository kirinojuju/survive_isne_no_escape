package io.github.kirinojuju.render;

import io.github.kirinojuju.enemy.boss.BossBody;

// File role: Loads Ae's body and attack frames and draws its vertical laser.

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.kirinojuju.enemy.boss.AeBoss;
import io.github.kirinojuju.enemy.boss.AeBossBehavior;
import io.github.kirinojuju.world.GameSpace;

/** Ae's body animation and optional vertical laser art. */
public final class AeSprite {
    private static final String ROOT = "characters/boss/Tnetwork(Ae)/";
    private static final float DRAW_SIZE = BossBody.AE_DRAW_SIZE;
    private final Texture body = loadBody();
    private final Texture[] bodyFrames = OptionalAssetTexture.loadNumberedFrames(ROOT, "aeBody", 7);
    private final Texture[] attackFrames = OptionalAssetTexture.loadNumberedFrames(ROOT, "aeActack", 8);
    private final Texture laser = loadOptional("aeLaser.png");

    // Load Ae's standalone body PNG when available; animated body frames take priority.
    private static Texture loadBody() {
        return loadOptional("aeBody.png");
    }

    // Load an available Ae texture, returning null when no file exists.
    private static Texture loadOptional(String name) {
        if (!Gdx.files.internal(ROOT + name).exists()) return null;
        Texture texture = new Texture(Gdx.files.internal(ROOT + name));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    // Draw Ae artwork using the current animation and combat state.
    public void draw(SpriteBatch batch, AeBoss boss) {
        Texture frame;
        if (boss.attackAnimationTime > 0) {
            int index = (int)((1 - boss.attackAnimationTime / .4f) * attackFrames.length);
            frame = OptionalAssetTexture.frame(attackFrames, index);
        } else if (boss.laserPhase != AeBoss.LaserPhase.IDLE || boss.warpWindup > 0) {
            frame = OptionalAssetTexture.frame(attackFrames,
                (int)(boss.animationTime / .1f) % attackFrames.length);
        } else {
            frame = OptionalAssetTexture.frame(bodyFrames,
                (int)(boss.animationTime / .12f) % bodyFrames.length);
        }
        if (frame == null) frame = body;
        if (frame == null) return;
        float width = DRAW_SIZE * frame.getWidth() / frame.getHeight();
        boss.bodyAspectRatio = width / DRAW_SIZE;
        batch.draw(frame, boss.x - width / 2, boss.y + boss.radius - DRAW_SIZE, width, DRAW_SIZE);
    }

    // Draw the laser warning and a shape-based active beam when laser artwork is missing.
    public void drawLaserShapes(ShapeRenderer shapes, AeBoss boss) {
        if (boss.hp <= 0 || boss.laserPhase == AeBoss.LaserPhase.IDLE) return;
        float height = GameSpace.WALK_TOP - GameSpace.BOTTOM;
        if (boss.laserPhase == AeBoss.LaserPhase.WARNING) {
            shapes.setColor(Color.YELLOW);
            shapes.rect(boss.laserX - 3, GameSpace.BOTTOM, 6, height);
            // Mark the direction and show the width that will become dangerous.
            shapes.triangle(boss.laserX, GameSpace.BOTTOM + 30,
                boss.laserX + 32, GameSpace.BOTTOM + 42,
                boss.laserX, GameSpace.BOTTOM + 54);
        } else if (laser == null) {
            shapes.setColor(Color.CYAN);
            shapes.rect(boss.laserX - AeBossBehavior.LASER_HALF_WIDTH, GameSpace.BOTTOM,
                AeBossBehavior.LASER_HALF_WIDTH * 2, height);
            shapes.setColor(Color.WHITE);
            shapes.rect(boss.laserX - 5, GameSpace.BOTTOM, 10, height);
        }
    }

    // Draw optional laser artwork at the current sweep position.
    public void drawLaser(SpriteBatch batch, AeBoss boss) {
        if (laser == null || boss.hp <= 0 || boss.laserPhase != AeBoss.LaserPhase.SWEEP) return;
        batch.draw(laser, boss.laserX - AeBossBehavior.LASER_HALF_WIDTH, GameSpace.BOTTOM,
            AeBossBehavior.LASER_HALF_WIDTH * 2, GameSpace.WALK_TOP - GameSpace.BOTTOM);
    }

    // Check whether body artwork is available before replacing the shape placeholder.
    public boolean hasBody() { return body != null || OptionalAssetTexture.frame(bodyFrames, 0) != null; }

    // Release textures and drawing resources owned by this sprite.
    public void dispose() {
        if (body != null) body.dispose();
        OptionalAssetTexture.disposeFrames(bodyFrames);
        OptionalAssetTexture.disposeFrames(attackFrames);
        if (laser != null) laser.dispose();
    }
}
