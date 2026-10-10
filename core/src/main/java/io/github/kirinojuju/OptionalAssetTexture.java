package io.github.kirinojuju;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/** Uses local art when available and a small code-drawn stand-in in fresh clones. */
final class OptionalAssetTexture {
    private OptionalAssetTexture() { }

    static Texture load(String path) {
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
        else drawSati(pixels, path);
        return pixels;
    }

    private static void drawScene(Pixmap pixels, String path) {
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
}
