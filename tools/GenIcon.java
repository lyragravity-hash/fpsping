import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** One-off generator for the mod icon (signal bars motif). */
public class GenIcon {
	public static void main(String[] args) throws Exception {
		int s = 256; // square; Modrinth icons should be 1:1 and reasonably hi-res
		BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		float k = s / 128f; // scale factor from the original 128x128 design

		// Rounded dark background
		g.setColor(new Color(0x1D1D24));
		g.fill(new RoundRectangle2D.Float(4 * k, 4 * k, s - 8 * k, s - 8 * k, 48 * k, 48 * k));

		// Signal bars: two green, one tall red (a "ping" spike)
		g.setColor(new Color(0x55FF55));
		g.fill(new RoundRectangle2D.Float(22 * k, 72 * k, 20 * k, 32 * k, 12 * k, 12 * k));
		g.fill(new RoundRectangle2D.Float(54 * k, 52 * k, 20 * k, 52 * k, 12 * k, 12 * k));
		g.setColor(new Color(0xFF5555));
		g.fill(new RoundRectangle2D.Float(86 * k, 24 * k, 20 * k, 80 * k, 12 * k, 12 * k));

		g.dispose();
		File out = new File("src/main/resources/assets/fpsping/icon.png");
		out.getParentFile().mkdirs();
		ImageIO.write(img, "png", out);
		System.out.println("Wrote " + out.getAbsolutePath());
	}
}
