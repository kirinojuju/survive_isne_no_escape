package io.github.kirinojuju.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/** Uses local art when available and a small code-drawn stand-in in fresh clones. */
public final class OptionalAssetTexture {
    private OptionalAssetTexture() { }

    public static Texture load(String path) {
        FileHandle file = Gdx.files.internal(path);
        Texture texture;
        if (file.exists()) texture = new Texture(file);
        else {
            Pixmap pixels = placeholder(path);
            try { texture = new Texture(pixels); }
            finally { pixels.dispose(); }
        }
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    private static Pixmap placeholder(String path) {
        Pixmap pixels = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixels.setColor(0, 0, 0, 0);
        pixels.fill();
        if (path.startsWith("scene/")) drawScene(pixels, path);
        else if (path.contains("student_player/")) drawStudent(pixels, path);
        else if (path.contains("liquid-cat/")) drawCat(pixels, path);
        else if (path.contains("insecure-sword/")) drawSword(pixels);
        else if (path.contains("Tnetwork(Dome)/")) drawDome(pixels, path);
        else if (path.startsWith("weapons/notebook/")) drawNotebook(pixels, path);
        else if (path.startsWith("weapons/icewall/")) drawIceWall(pixels);
        else if (path.startsWith("weapons/potion/")) drawPotion(pixels, path.contains("_hp"));
        else if (path.contains("monsters/")) drawSlime(pixels);
        else drawSati(pixels, path);
        return pixels;
    }

    private static void drawIceWall(Pixmap pixels) {
        pixels.setColor(.24f, .70f, .92f, 1);
        pixels.fillRectangle(4, 22, 56, 28);
        pixels.setColor(.62f, .94f, 1f, 1);
        for (int x = 7; x < 58; x += 10) {
            pixels.fillRectangle(x, 18, 7, 30);
            pixels.fillTriangle(x, 18, x + 7, 18, x + 3, 8);
        }
        pixels.setColor(Color.WHITE);
        pixels.drawLine(5, 23, 59, 23);
    }

    private static void drawSlime(Pixmap pixels) {
        pixels.setColor(.43f, .87f, .57f, 1);
        pixels.fillCircle(32, 43, 16);
        pixels.fillRectangle(16, 43, 33, 14);
        pixels.setColor(Color.WHITE);
        pixels.fillRectangle(24, 40, 5, 5);
        pixels.fillRectangle(36, 40, 5, 5);
    }

    private static void drawPotion(Pixmap pixels, boolean health) {
        pixels.setColor(Color.LIGHT_GRAY);
        pixels.fillRectangle(26, 12, 12, 12);
        pixels.fillRectangle(21, 24, 22, 29);
        pixels.setColor(health ? Color.CORAL : Color.CYAN);
        pixels.fillRectangle(24, 31, 16, 19);
    }

    private static void drawNotebook(Pixmap pixels, String path) {
        pixels.setColor(Color.GRAY);
        pixels.fillRectangle(18, 17, 28, 20);
        pixels.setColor(Color.DARK_GRAY);
        pixels.fillRectangle(20, 19, 24, 16);
        pixels.setColor(Color.LIGHT_GRAY);
        pixels.fillRectangle(14, 39, 36, 10);
        if (path.contains("explode")) {
            pixels.setColor(Color.WHITE);
            pixels.fillRectangle(10, 14, 4, 4);
            pixels.fillRectangle(49, 29, 5, 5);
            pixels.fillRectangle(22, 53, 4, 4);
        }
    }

    private static void drawScene(Pixmap pixels, String path) {
        if (path.contains("/table")) {
            pixels.setColor(Color.LIGHT_GRAY);
            pixels.fillRectangle(10, 10, 44, 35);
            pixels.setColor(Color.DARK_GRAY);
            pixels.drawRectangle(10, 10, 44, 35);
            pixels.fillRectangle(12, 45, 5, 12);
            pixels.fillRectangle(47, 45, 5, 12);
            return;
        }
        if (path.contains("Car_object")) {
            pixels.setColor(Color.DARK_GRAY);
            pixels.fillRectangle(32, 37, 25, 8);
            pixels.setColor(Color.CYAN);
            pixels.fillRectangle(38, 32, 12, 6);
            pixels.setColor(Color.BLACK);
            pixels.fillCircle(37, 46, 3);
            pixels.fillCircle(52, 46, 3);
            return;
        }
        if (path.contains("entrance")) {
            pixels.setColor(.58f, .76f, .88f, 1);
            pixels.fill();
            pixels.setColor(.73f, .70f, .57f, 1);
            pixels.fillRectangle(5, 10, 54, 42);
            pixels.setColor(.22f, .36f, .47f, 1);
            for (int x = 11; x < 54; x += 13)
                for (int y = 16; y < 40; y += 12)
                    pixels.fillRectangle(x, y, 7, 7);
            pixels.setColor(.28f, .38f, .34f, 1);
            pixels.fillRectangle(0, 52, 64, 12);
            pixels.setColor(Color.DARK_GRAY);
            pixels.fillRectangle(6, 38, 9, 14);
            return;
        }
        pixels.setColor(path.contains("boss") ? .29f : .20f,
            .27f, .33f, 1);
        pixels.fill();
        pixels.setColor(.13f, .17f, .23f, 1);
        for (int x = 0; x < 64; x += 16) pixels.drawLine(x, 0, x, 63);
        for (int y = 0; y < 64; y += 16) pixels.drawLine(0, y, 63, y);
        pixels.setColor(.48f, .63f, .67f, 1);
        pixels.fillRectangle(8, 4, 20, 9);
        pixels.fillRectangle(38, 4, 18, 9);
    }

    private static void drawStudent(Pixmap pixels, String path) {
        if (path.contains("power_ball")) {
            pixels.setColor(.08f, .52f, .93f, .7f);
            pixels.fillCircle(32, 32, 26);
            pixels.setColor(.45f, .93f, 1f, 1);
            pixels.fillCircle(32, 32, 17);
            return;
        }
        pixels.setColor(Color.BLACK);
        pixels.fillRectangle(19, 11, 26, 34);
        pixels.setColor(.95f, .80f, .73f, 1);
        pixels.fillRectangle(21, 22, 22, 19);
        pixels.setColor(.90f, .91f, .94f, 1);
        pixels.fillRectangle(21, 42, 22, 17);
        pixels.setColor(Color.BLACK);
        pixels.fillRectangle(24, 28, 4, 5);
        pixels.fillRectangle(37, 28, 4, 5);
        pixels.fillRectangle(23, 58, 7, 5);
        pixels.fillRectangle(35, 58, 7, 5);
        if (path.contains("skill_case")) {
            pixels.setColor(Color.CYAN);
            pixels.fillCircle(50, 41, 7);
        }
    }

    private static void drawCat(Pixmap pixels, String path) {
        pixels.setColor(.25f, .82f, .27f, 1);
        pixels.fillCircle(32, 37, path.contains("bullet") ? 13 : 20);
        if (path.contains("bullet")) return;
        pixels.fillRectangle(17, 15, 8, 17);
        pixels.fillRectangle(39, 15, 8, 17);
        pixels.setColor(.73f, 1f, .44f, 1);
        pixels.fillRectangle(23, 34, 6, 4);
        pixels.fillRectangle(36, 34, 6, 4);
    }

    private static void drawSword(Pixmap pixels) {
        pixels.setColor(Color.CORAL);
        pixels.fillRectangle(29, 7, 6, 13);
        pixels.fillRectangle(24, 19, 16, 4);
        pixels.setColor(Color.LIGHT_GRAY);
        pixels.fillRectangle(28, 23, 8, 27);
        pixels.fillTriangle(28, 50, 36, 50, 32, 57);
        pixels.setColor(Color.WHITE);
        pixels.drawLine(29, 24, 29, 49);
    }

    private static void drawSati(Pixmap pixels, String path) {
        pixels.setColor(.60f, .45f, .91f, 1);
        if (path.contains("spear")) {
            pixels.fillRectangle(29, 11, 6, 45);
            pixels.fillTriangle(22, 22, 42, 22, 32, 5);
        } else {
            pixels.fillRectangle(16, 16, 32, 32);
            pixels.setColor(Color.WHITE);
            pixels.fillRectangle(23, 26, 5, 5);
            pixels.fillRectangle(36, 26, 5, 5);
        }
    }

    private static void drawDome(Pixmap pixels, String path) {
        if (path.contains("Elements/")) {
            pixels.setColor(path.contains("Laser") ? Color.CYAN : Color.YELLOW);
            pixels.fillRectangle(25, 0, 14, 64);
            return;
        }
        pixels.setColor(.22f, .45f, .40f, 1);
        pixels.fillRectangle(23, 18, 18, 22);
        pixels.setColor(.95f, .80f, .73f, 1);
        pixels.fillRectangle(25, 29, 14, 12);
        pixels.setColor(Color.WHITE);
        pixels.fillRectangle(23, 41, 18, 19);
    }
}
