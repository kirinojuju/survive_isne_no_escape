package io.github.kirinojuju.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.kirinojuju.enemy.boss.AeBoss;
import io.github.kirinojuju.enemy.boss.AeBossBehavior;
import io.github.kirinojuju.world.GameSpace;

/** Ae's supplied portrait and optional vertical laser art. */
public final class AeSprite {
    private static final String ROOT = "characters/boss/Tnetwork(Ae)/";
    private static final float DRAW_HEIGHT = 138f;
    private final Texture body = loadBody();
    private final Texture laser = loadOptional("aeLaser.png");

    private static Texture loadBody() {
        Texture png = loadOptional("aeBody.png");
        return png != null ? png : loadOptional("aeTest.jpg");
    }

    private static Texture loadOptional(String name) {
        if (!Gdx.files.internal(ROOT + name).exists()) return null;
        Texture texture = new Texture(Gdx.files.internal(ROOT + name));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    public void draw(SpriteBatch batch, AeBoss boss) {
        if (body == null) return;
        float width = DRAW_HEIGHT * body.getWidth() / body.getHeight();
        batch.draw(body, boss.x - width / 2, boss.y - DRAW_HEIGHT / 2, width, DRAW_HEIGHT);
    }

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

    public void drawLaser(SpriteBatch batch, AeBoss boss) {
        if (laser == null || boss.hp <= 0 || boss.laserPhase != AeBoss.LaserPhase.SWEEP) return;
        batch.draw(laser, boss.laserX - AeBossBehavior.LASER_HALF_WIDTH, GameSpace.BOTTOM,
            AeBossBehavior.LASER_HALF_WIDTH * 2, GameSpace.WALK_TOP - GameSpace.BOTTOM);
    }

    public boolean hasBody() { return body != null; }

    public void dispose() {
        if (body != null) body.dispose();
        if (laser != null) laser.dispose();
    }
}
