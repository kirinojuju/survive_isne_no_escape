package io.github.kirinojuju.render.sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.render.OptionalAssetTexture;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Shared drawing contract and ownership of textures used by one enemy renderer. */
public abstract class EnemySprite implements Disposable {
    private final int kind;
    private final List<Texture> textures = new ArrayList<Texture>();

    protected EnemySprite(int kind) { this.kind = kind; }

    public boolean supports(Enemy enemy) { return enemy.kind == kind; }
    public boolean hasBody() { return true; }
    public abstract void draw(SpriteBatch batch, Enemy enemy, float playerX);

    protected Texture load(String path) {
        Texture texture = OptionalAssetTexture.load(path);
        textures.add(texture);
        return texture;
    }

    protected Texture optional(String path) {
        return Gdx.files.internal(path).exists() ? load(path) : null;
    }

    protected Texture[] frames(String pattern, int first, int count, boolean required) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++) {
            String path = String.format(Locale.ROOT, pattern, first + i);
            frames[i] = required ? load(path) : optional(path);
        }
        return frames;
    }

    protected void drawBody(SpriteBatch batch, Enemy enemy, Texture frame, float height, boolean flip) {
        if (frame == null) return;
        float width = height * frame.getWidth() / frame.getHeight();
        enemy.bodyAspectRatio = width / height;
        OptionalAssetTexture.applyBodyBounds(enemy, frame, flip);
        batch.draw(frame, enemy.x + (flip ? width / 2 : -width / 2),
            enemy.y + enemy.radius - height, flip ? -width : width, height);
    }

    @Override public void dispose() {
        for (Texture texture : textures) texture.dispose();
        textures.clear();
    }
}
