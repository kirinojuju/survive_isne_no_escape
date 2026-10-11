package io.github.kirinojuju.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.kirinojuju.schoolLife;
import java.util.Arrays;
import java.util.List;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        List<String> options = Arrays.asList(args);
        schoolLife.TestMode testMode = schoolLife.TestMode.NONE;
        if (options.contains("--test-final-exam")) testMode = schoolLife.TestMode.FINAL_EXAM;
        else if (options.contains("--test-obstacles")) testMode = schoolLife.TestMode.OBSTACLES;
        else if (options.contains("--test-allergic-vacuum"))
            testMode = schoolLife.TestMode.EX_ALLERGIC_VACUUM;
        else if (options.contains("--test-insecure-sword"))
            testMode = schoolLife.TestMode.EX_INSECURE_SWORD;
        else if (options.contains("--test-ex") || options.contains("--test-liquid-cat"))
            testMode = schoolLife.TestMode.EX_LIQUID_CAT;
        createApplication(testMode);
    }

    private static Lwjgl3Application createApplication(schoolLife.TestMode testMode) {
        return new Lwjgl3Application(new schoolLife(testMode), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(schoolLife.GAME_TITLE);
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        configuration.setWindowedMode(1280, 720);
        // The desktop prototype must also open in a fresh clone without local artwork.

        //// This could improve compatibility with Windows machines with buggy OpenGL drivers, Macs
        //// with Apple Silicon that have to emulate compatibility with OpenGL anyway, and more.
        //// This uses the dependency `com.badlogicgames.gdx:gdx-lwjgl3-angle` to function.
        //// You would need to add this line to lwjgl3/build.gradle , below the dependency on `gdx-backend-lwjgl3`:
        ////     implementation "com.badlogicgames.gdx:gdx-lwjgl3-angle:$gdxVersion"
        //// You can choose to add the following line and the mentioned dependency if you want; they
        //// are not intended for games that use GL30 (which is compatibility with OpenGL ES 3.0).
        //// Know that it might not work well in some cases.
//        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        return configuration;
    }
}
