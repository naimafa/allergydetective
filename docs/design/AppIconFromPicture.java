import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Builds the adaptive launcher icon layers from the icon picture (docs/design/app-icon-simple.png, made with
 * IconRebuild.java from app-icon-magnifier.png: the user's picture of 21.11.2026 without frame, arcs and bars,
 * in the start screen colours, with a bigger magnifier).
 * Adaptive icons are 108 dp; launchers show about the middle 66–72 dp. The picture is placed at 64 dp in
 * the middle (phone test 21.11.: at 76 dp the Xiaomi launcher cut the bars, the leaf and the handle);
 * the cream background fills the rest.
 *  - foreground: the picture, 432 px (108 dp at 4x), transparent outside the picture
 *  - monochrome (themed icons, Android 13+): the dark shapes (magnifier, fork, bolt, bars, leaf) as alpha
 * Args: picture.png foreground.png monochrome.png
 */
public class AppIconFromPicture {
    public static void main(String[] a) throws Exception {
        BufferedImage pic = ImageIO.read(new File(a[0]));
        int size = 432, inner = 256, off = (size - inner) / 2;
        BufferedImage scaled = scale(pic, inner);

        BufferedImage fg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fg.createGraphics();
        g.drawImage(scaled, off, off, null);
        g.dispose();
        ImageIO.write(fg, "png", new File(a[1]));

        BufferedImage mono = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < inner; y++) for (int x = 0; x < inner; x++) {
            int p = scaled.getRGB(x, y);
            double l = 0.299 * (p >> 16 & 255) + 0.587 * (p >> 8 & 255) + 0.114 * (p & 255);
            // Dark and saturated shapes count, the cream and the light wave do not.
            double alpha = Math.max(0, Math.min(1, (175 - l) / 60.0));
            // The orange bolt is lighter than the greens: strong colour counts fully as well.
            int mx = Math.max(p >> 16 & 255, Math.max(p >> 8 & 255, p & 255)), mn = Math.min(p >> 16 & 255, Math.min(p >> 8 & 255, p & 255));
            alpha = Math.max(alpha, Math.max(0, Math.min(1, ((mx - mn) / 255.0 - 0.25) / 0.2)));
            mono.setRGB(x + off, y + off, ((int) Math.round(alpha * 255) << 24) | 0xFFFFFF);
        }
        ImageIO.write(mono, "png", new File(a[2]));
    }

    /** Scales down in halving steps for a smooth result. */
    static BufferedImage scale(BufferedImage s, int n) {
        BufferedImage cur = s;
        while (cur.getWidth() / 2 >= n) cur = draw(cur, cur.getWidth() / 2);
        return cur.getWidth() == n ? cur : draw(cur, n);
    }

    static BufferedImage draw(BufferedImage s, int n) {
        BufferedImage o = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = o.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(s, 0, 0, n, n, null);
        g.dispose();
        return o;
    }
}
