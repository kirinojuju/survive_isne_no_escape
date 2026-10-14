package io.github.kirinojuju.render.sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.kirinojuju.combat.CombatRoom;
import io.github.kirinojuju.enemy.Enemy;
import io.github.kirinojuju.enemy.Monster;
import io.github.kirinojuju.render.OptionalAssetTexture;

/** Dedicated monster artwork with Slime/Blob fallbacks for the six combat roles. */
public class MonsterSprite extends EnemySprite {
    private static final String ROOT = "characters/monsters/";
    private static final float DRAW_SIZE = 92f;
    private static final float FRAME_TIME = .12f;
    private final Texture[] network = frames(ROOT + "monster_network/Network_Kiki%d.png", 1, 15, false);
    private final Texture[] program = frames(ROOT + "monster_program/Program_Kiki%d.png", 1, 8, false);
    private final Texture[] algorithm = frames(ROOT + "monster_algo/gigiAlgo%02d.png", 0, 2, false);
    private final Texture algoBall = load(ROOT + "monster_algo/ballgigiAlgo00.png");
    private final Texture[] slime = {optional("gigiSlime.png")};
    private final Frames slimeIdle = Frames.individual("slime-idle-", 4);
    private final Frames slimeMove = Frames.individual("slime-move-", 4);
    private final Frames slimeAttack = Frames.individual("slime-attack-", 5);
    private final Frames slimeHurt = Frames.individual("slime-hurt-", 4);
    private final Frames blueIdle = Frames.sheet("Blue", "Idle");
    private final Frames blueRun = Frames.sheet("Blue", "Run");
    private final Frames greenIdle = Frames.sheet("Green", "Idle");
    private final Frames greenRun = Frames.sheet("Green", "Run");

    public MonsterSprite() { super(CombatRoom.BASIC); }
    @Override public boolean supports(Enemy enemy) { return enemy.kind < 0; }

    @Override public void draw(SpriteBatch batch, Enemy monster, float playerX) {
        Texture[] dedicated = dedicatedFrames(monster);
        if (dedicated != null) {
            float interval = monster.kind == CombatRoom.BASIC ? .2f : FRAME_TIME;
            Texture frame = OptionalAssetTexture.frame(dedicated,
                (int)(monster.animationTime / interval) % dedicated.length);
            if (frame != null) {
                batch.draw(frame, monster.x - DRAW_SIZE / 2, monster.y - DRAW_SIZE / 2,
                    DRAW_SIZE, DRAW_SIZE);
                return;
            }
        }
        Frames animation;
        float frameTime = .12f, elapsed = monster.animationTime;
        switch (monster.kind) {
            case CombatRoom.BINDER:
            case CombatRoom.WAVE:
                animation = monster.moving ? blueRun : blueIdle;
                break;
            case CombatRoom.WARPER:
            case CombatRoom.RANGED:
                animation = monster.moving ? greenRun : greenIdle;
                break;
            default:
                animation = monster.moving ? slimeMove : slimeIdle;
                if (monster.stunTime > 0) animation = slimeHurt;
                else if (monster.contactTimer > .25f) {
                    animation = slimeAttack;
                    elapsed = .75f - monster.contactTimer;
                    frameTime = .1f;
                }
                break;
        }
        TextureRegion frame = animation.frames[(int)(Math.max(0, elapsed) / frameTime)
            % animation.frames.length];
        float scale = 2.5f * monster.radius / 17 * 32 / frame.getRegionWidth();
        float width = frame.getRegionWidth() * scale, height = frame.getRegionHeight() * scale;
        boolean flip = playerX < monster.x;
        batch.setColor(Color.WHITE);
        batch.draw(frame, monster.x + (flip ? width / 2 : -width / 2),
            monster.y - animation.originY * scale, flip ? -width : width, height);
    }

    private Texture[] dedicatedFrames(Enemy enemy) {
        if (!(enemy instanceof Monster)) return null;
        switch (((Monster)enemy).appearance()) {
            case ALGORITHM: return algorithm;
            case NETWORK: return network;
            case PROGRAM: return program;
            case SLIME: return slime;
            default: return null;
        }
    }

    public void drawBall(SpriteBatch batch, CombatRoom.Shot shot) {
        float size = shot.radius * 4;
        batch.draw(algoBall, shot.x - size / 2, shot.y - size / 2, size, size);
    }

    @Override public void dispose() {
        super.dispose();
        for (Frames animation : new Frames[] {slimeIdle, slimeMove, slimeAttack, slimeHurt,
            blueIdle, blueRun, greenIdle, greenRun}) animation.dispose();
    }

    private static final class Frames {
        final Texture[] textures;
        final TextureRegion[] frames;
        final float originY;

        Frames(Texture[] textures, TextureRegion[] frames, float originY) {
            this.textures = textures;
            this.frames = frames;
            this.originY = originY;
        }

        static Frames individual(String prefix, int count) {
            Texture[] textures = new Texture[count];
            TextureRegion[] frames = new TextureRegion[count];
            for (int i = 0; i < count; i++) {
                textures[i] = OptionalAssetTexture.load(ROOT + "Individual Sprites/" + prefix + i + ".png");
                frames[i] = new TextureRegion(textures[i]);
            }
            return new Frames(textures, frames, textures[0].getHeight() == 25 ? 8 : 24);
        }

        static Frames sheet(String color, String action) {
            Texture texture = OptionalAssetTexture.load(ROOT + color + "-Blob-sheets/"
                + color + "-Blob-" + action + ".png");
            if (texture.getHeight() != 32)
                return new Frames(new Texture[] {texture}, new TextureRegion[] {new TextureRegion(texture)}, 24);
            int count = Math.max(1, texture.getWidth() / 64);
            TextureRegion[] frames = new TextureRegion[count];
            // Every cell is 64x32. Crop transparent side padding, never join adjacent frames.
            for (int i = 0; i < count; i++)
                frames[i] = new TextureRegion(texture, i * 64 + (color.equals("Blue") ? 14 : 16),
                    0, 32, 32);
            return new Frames(new Texture[] {texture}, frames, 12);
        }

        void dispose() { for (Texture texture : textures) texture.dispose(); }
    }
}
