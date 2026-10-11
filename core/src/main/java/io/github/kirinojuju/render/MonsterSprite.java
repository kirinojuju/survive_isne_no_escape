package io.github.kirinojuju.render;

// File role: Loads and draws Network, Programming and slime monster artwork.

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.kirinojuju.combat.CombatRoom.Enemy;
import io.github.kirinojuju.enemy.NetworkMonster;
import io.github.kirinojuju.enemy.ProgramMonster;
import io.github.kirinojuju.enemy.SlimeMonster;

/** Exported Network and Programming monster animation frames. */
public final class MonsterSprite {
    private static final float DRAW_SIZE = 92f;
    private static final float FRAME_TIME = .12f;
    private final Texture[] network = OptionalAssetTexture.loadNumberedFrames(
        "characters/monsters/monster_network/", "gigiNetwork", 15);
    private final Texture[] program = OptionalAssetTexture.loadNumberedFrames(
        "characters/monsters/monster_program/", "gigiProgram", 8);
    private final Texture[] slime = {
        Gdx.files.internal("gigiSlime.png").exists() ? OptionalAssetTexture.load("gigiSlime.png") : null
    };

    // Select Network, Programming or slime artwork from the monster subclass.
    private Texture[] frames(Enemy monster) {
        if (monster instanceof NetworkMonster) return network;
        if (monster instanceof ProgramMonster) return program;
        if (monster instanceof SlimeMonster) return slime;
        return null;
    }

    // Check whether an image is available before replacing the monster's shape placeholder.
    public boolean hasBody(Enemy monster) {
        Texture[] frames = frames(monster);
        return frames != null && OptionalAssetTexture.frame(frames, 0) != null;
    }

    // Draw the current monster frame below its health bar using the shared draw size.
    public void draw(SpriteBatch batch, Enemy monster) {
        Texture[] frames = frames(monster);
        if (frames == null) return;
        Texture frame = OptionalAssetTexture.frame(frames,
            (int)(monster.animationTime / FRAME_TIME) % frames.length);
        if (frame == null) return;
        batch.draw(frame, monster.x - DRAW_SIZE / 2, monster.y + monster.radius - DRAW_SIZE,
            DRAW_SIZE, DRAW_SIZE);
    }

    // Release textures and drawing resources owned by this sprite.
    public void dispose() {
        OptionalAssetTexture.disposeFrames(network);
        OptionalAssetTexture.disposeFrames(program);
        OptionalAssetTexture.disposeFrames(slime);
    }
}
