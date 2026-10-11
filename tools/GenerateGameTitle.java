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

/** Run from the repository root after changing schoolLife.GAME_TITLE (JDK 11+). */
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
    }
}
