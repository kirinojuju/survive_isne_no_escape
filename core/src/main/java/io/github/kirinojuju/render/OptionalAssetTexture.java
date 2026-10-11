package io.github.kirinojuju.render;

// File role: Loads artwork and creates placeholders; optional numbered frames can stay missing.

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/** Uses local art when available and a small code-drawn stand-in in fresh clones. */
public final class OptionalAssetTexture {
    // Prevent construction of this utility class; use its static helpers.
    private OptionalAssetTexture() { }

    /** Exported frames numbered 1..count; absent frames never become unrelated placeholder art. */
    public static Texture[] loadNumberedFrames(String root, String prefix, int count) {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++) {
            String path = root + prefix + (i + 1) + ".png";
            if (Gdx.files.internal(path).exists()) frames[i] = load(path);
        }
        return frames;
    }

    // Choose a bounded frame index and fall back to the first available frame if needed.
    public static Texture frame(Texture[] frames, int index) {
        Texture selected = frames[Math.max(0, Math.min(index, frames.length - 1))];
        if (selected != null) return selected;
        for (Texture frame : frames) if (frame != null) return frame;
        return null;
    }

    // Release each available texture in an animation array.
    public static void disposeFrames(Texture[] frames) {
        for (Texture frame : frames) if (frame != null) frame.dispose();
    }

    // Load the requested image or create the matching placeholder when it is missing.
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

    // Choose and create placeholder artwork based on the requested asset path.
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
        else drawSati(pixels, path);
        return pixels;
    }

    // Paint fallback Notebook or Notebook effect pixels.
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

    // Paint fallback scene pixels for the requested background.
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

    // Paint fallback player pixels for the requested animation or direction.
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

    // Paint fallback Liquid Cat pixels.
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

    // Paint fallback Sword EX pixels.
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

    // Paint fallback Sati or C++ element pixels.
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

    // Paint fallback Dome or Dome effect pixels.
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
