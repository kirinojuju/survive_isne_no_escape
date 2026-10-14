import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/** Run from the repository root after editing the game title or bilingual intro (JDK 11+). */
public final class GenerateGameTitle {
    public static void main(String[] args) throws Exception {
        Path source = Paths.get("core/src/main/java/io/github/kirinojuju/schoolLife.java");
        String code = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        Matcher match = Pattern.compile("GAME_TITLE\\s*=\\s*\"([^\"]+)\"").matcher(code);
        if (!match.find()) throw new IllegalStateException("Missing GAME_TITLE");
        String title = match.group(1);
        Font font = null;
        for (String family : new String[] {"Tahoma", "Noto Sans Thai", "Thonburi", "Dialog"}) {
            Font candidate = new Font(family, Font.BOLD, 54);
            if (candidate.canDisplayUpTo(title) == -1) { font = candidate; break; }
        }
        if (font == null) throw new IllegalStateException("Install a Thai font to regenerate the title");
        TextLayout layout = new TextLayout(title, font, new FontRenderContext(null, true, true));
        Rectangle2D bounds = layout.getBounds();
        int padding = 16;
        BufferedImage image = new BufferedImage((int)Math.ceil(bounds.getWidth()) + padding * 2,
            (int)Math.ceil(bounds.getHeight()) + padding * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setColor(new Color(232, 240, 232));
            layout.draw(graphics, padding - (float)bounds.getX(), padding - (float)bounds.getY());
        } finally { graphics.dispose(); }
        Path output = Paths.get("core/src/main/resources/ui/game-title.png");
        Files.createDirectories(output.getParent());
        ImageIO.write(image, "png", output.toFile());
        System.out.println("Thai title generated: " + image.getWidth() + "x" + image.getHeight());
        generateIntro(font.deriveFont(Font.PLAIN, 64f));
    }

    /** AWT TextLayout shapes Thai vowels/tone marks once, without shipping an OS font with the game. */
    private static void generateIntro(Font font) throws Exception {
        Path source = Paths.get("core/src/main/java/io/github/kirinojuju/flow/RunDirector.java");
        String code = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        Matcher lines = Pattern.compile("\\{\\\"[^\\\"]+\\\",\\s*\\\"([^\\\"]*[\\u0E00-\\u0E7F][^\\\"]*)\\\"\\}").matcher(code);
        java.util.List<String> subtitles = new java.util.ArrayList<String>();
        while (lines.find()) subtitles.add(lines.group(1));
        if (subtitles.isEmpty()) throw new IllegalStateException("Missing Thai intro lines");
        BufferedImage image = new BufferedImage(1600, subtitles.size() * 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setColor(Color.WHITE);
            for (int i = 0; i < subtitles.size(); i++) {
                String text = subtitles.get(i);
                if (font.canDisplayUpTo(text) != -1) throw new IllegalStateException("Thai intro font missing characters");
                TextLayout layout = new TextLayout(text, font, graphics.getFontRenderContext());
                Rectangle2D bounds = layout.getBounds();
                if (bounds.getWidth() > 1560) throw new IllegalStateException("Intro line is too wide: " + text);
                layout.draw(graphics, (1600 - (float)bounds.getWidth()) / 2 - (float)bounds.getX(),
                    i * 128 + (128 - (float)bounds.getHeight()) / 2 - (float)bounds.getY());
            }
        } finally { graphics.dispose(); }
        Path output = Paths.get("core/src/main/resources/ui/intro-story-th.png");
        Files.createDirectories(output.getParent());
        ImageIO.write(image, "png", output.toFile());
        System.out.println("Thai intro generated: " + subtitles.size() + " shaped lines");
    }
}
