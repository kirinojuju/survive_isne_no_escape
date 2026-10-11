package io.github.kirinojuju.render;

import io.github.kirinojuju.enemy.boss.BossBody;

// File role: Loads and draws Far's body artwork when it is available.

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.kirinojuju.enemy.boss.FarBoss;

/** Optional local Far artwork; the renderer keeps its existing stand-in when absent. */
public final class FarSprite {
    private static final String ROOT = "characters/boss/Tnetwork(Far)/";
    private static final float DRAW_SIZE = BossBody.FAR_DRAW_SIZE;
    private final Texture body = loadBody();

    // Load Far's body PNG, returning null when it has not been supplied yet.
    private static Texture loadBody() {
        if (!Gdx.files.internal(ROOT + "farBody.png").exists()) return null;
        Texture texture = OptionalAssetTexture.load(ROOT + "farBody.png");
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    // Check whether body artwork is available before replacing the shape placeholder.
    public boolean hasBody() { return body != null; }

    // Draw Far artwork using the current animation and combat state.
    public void draw(SpriteBatch batch, FarBoss boss) {
        if (body == null) return;
        float width = DRAW_SIZE * body.getWidth() / body.getHeight();
        boss.bodyAspectRatio = width / DRAW_SIZE;
        OptionalAssetTexture.applyBodyBounds(boss, body, false);
        batch.draw(body, boss.x - width / 2, boss.y + boss.radius - DRAW_SIZE,
            width, DRAW_SIZE);
    }

    // Release textures and drawing resources owned by this sprite.
    public void dispose() {
        if (body != null) body.dispose();
    }
}
