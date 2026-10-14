package io.github.kirinojuju.render.sprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.render.OptionalAssetTexture;

/** Numbered idle/attack frames for bosses whose body has no special drawing rules. */
public class AnimatedEnemySprite extends EnemySprite {
    private final Texture[] idle, attack;
    private final float height;

    public AnimatedEnemySprite(int kind, float height, String idlePattern, int idleCount,
                               String attackPattern, int attackCount) {
        super(kind);
        this.height = height;
        idle = frames(idlePattern, 1, idleCount, false);
        attack = frames(attackPattern, 1, attackCount, false);
    }

    @Override public boolean supports(Enemy enemy) {
        return super.supports(enemy) && !enemy.anchored;
    }

    @Override public boolean hasBody() { return OptionalAssetTexture.frame(idle, 0) != null; }

    @Override public void draw(SpriteBatch batch, Enemy enemy, float playerX) {
        Texture frame = null;
        if (enemy.attackAnimationTime > 0 && attack.length > 0)
            frame = OptionalAssetTexture.frame(attack,
                (int)((1 - enemy.attackAnimationTime / .4f) * attack.length));
        if (frame == null)
            frame = OptionalAssetTexture.frame(idle, (int)(enemy.animationTime / .12f) % idle.length);
        drawBody(batch, enemy, frame, height, false);
    }
}
