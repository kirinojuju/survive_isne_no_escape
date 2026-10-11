package io.github.kirinojuju.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import io.github.kirinojuju.combat.CombatRoom.Enemy;

public class MSprite {
    private static final float DRAW_SIZE = 92f;

    private final Texture image =
        OptionalAssetTexture.load("characters/boss/Toop(M)/mTest.jpg");

    public void draw(SpriteBatch batch, Enemy boss) {
        batch.draw(image,
            boss.x - DRAW_SIZE / 2,
            boss.y - DRAW_SIZE / 2,
            DRAW_SIZE, DRAW_SIZE);
    }

    public void dispose() {
        image.dispose();
    }
}
