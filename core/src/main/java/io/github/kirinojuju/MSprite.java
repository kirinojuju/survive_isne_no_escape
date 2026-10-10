package io.github.kirinojuju;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

final class MSprite {
    private static final float DRAW_SIZE = 92f;

    private final Texture image =
        OptionalAssetTexture.load("characters/boss/Toop(M)/mTest.jpg");

    void draw(SpriteBatch batch, Enemy boss) {
        batch.draw(image,
            boss.x - DRAW_SIZE / 2,
            boss.y - DRAW_SIZE / 2,
            DRAW_SIZE, DRAW_SIZE);
    }

    void dispose() {
        image.dispose();
    }
}
