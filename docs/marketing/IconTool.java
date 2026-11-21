// App icon from the flat artwork (feedback 20.11.). Run with any JDK 11+ from the repo root:
//   java docs/marketing/IconTool.java docs/marketing/app-icon-source.jpg <outDir>
// then copy icon-1024.png to iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png,
// icon-512.png to docs/marketing/app-icon-512.png and ic_launcher_foreground.png to
// androidApp/src/main/res/drawable-nodpi/; the printed background colour goes into
// androidApp/src/main/res/drawable/ic_launcher_background.xml. The themed (monochrome) icon stays the
// vector from docs/marketing/app-icon.svg.
//
// The motif is found by its difference to the background, centred and scaled so that its farthest
// pixel stays inside the area the system masks never cut: 47 % of the size for iOS and the Play Store,
// 34 dp of 108 dp for the Android adaptive icon.

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class IconTool {
    static final int BG_DISTANCE = 60;

    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[0]));
        File out = new File(a[1]);
        out.mkdirs();
        int w = src.getWidth(), h = src.getHeight();

        int bg = average(src, new int[][]{{20, 20}, {w - 21, 20}, {20, h - 21}, {w - 21, h - 21}, {w / 2, 20}, {20, h / 2}});
        System.out.printf("background #%06X%n", bg & 0xFFFFFF);

        // Motif = pixels clearly different from the background.
        int minX = w, minY = h, maxX = 0, maxY = 0;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            if (distance(src.getRGB(x, y), bg) > BG_DISTANCE) {
                minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                minY = Math.min(minY, y); maxY = Math.max(maxY, y);
            }
        }
        double cx = (minX + maxX) / 2.0, cy = (minY + maxY) / 2.0;
        double radius = 0;
        for (int y = minY; y <= maxY; y++) for (int x = minX; x <= maxX; x++) {
            if (distance(src.getRGB(x, y), bg) > BG_DISTANCE) radius = Math.max(radius, Math.hypot(x - cx, y - cy));
        }
        System.out.printf("motif %d..%d x %d..%d, centre %.0f/%.0f, radius %.0f%n", minX, maxX, minY, maxY, cx, cy, radius);

        write(compose(src, bg, cx, cy, radius, 1024, 0.47 * 1024, false), BufferedImage.TYPE_INT_RGB, new File(out, "icon-1024.png"));
        write(compose(src, bg, cx, cy, radius, 512, 0.47 * 512, false), BufferedImage.TYPE_INT_RGB, new File(out, "icon-512.png"));
        // 432 px = 108 dp at xxxhdpi; transparent outside the motif, the background layer has the cream colour.
        write(compose(src, bg, cx, cy, radius, 432, 34.0 / 108 * 432, true), BufferedImage.TYPE_INT_ARGB, new File(out, "ic_launcher_foreground.png"));
    }

    /** The motif centred on a [size] square, its farthest pixel at [target] px from the centre. */
    static BufferedImage compose(BufferedImage src, int bg, double cx, double cy, double radius, int size, double target, boolean transparent) {
        double scale = target / radius;
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        if (!transparent) {
            g.setColor(new Color(bg));
            g.fillRect(0, 0, size, size);
        }
        // Inverse mapping with bilinear sampling of an area average: each output pixel averages the
        // source pixels it covers, which keeps edges smooth when shrinking 2000 px to 432 px.
        int step = Math.max(1, (int) Math.floor(1 / scale));
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
            double sx = cx + (x + 0.5 - size / 2.0) / scale, sy = cy + (y + 0.5 - size / 2.0) / scale;
            long r = 0, gr = 0, b = 0, n = 0;
            for (int dy = 0; dy < step; dy++) for (int dx = 0; dx < step; dx++) {
                int px = (int) Math.round(sx - step / 2.0 + dx), py = (int) Math.round(sy - step / 2.0 + dy);
                int rgb = (px >= 0 && py >= 0 && px < src.getWidth() && py < src.getHeight()) ? src.getRGB(px, py) : bg;
                r += rgb >> 16 & 255; gr += rgb >> 8 & 255; b += rgb & 255; n++;
            }
            int rgb = (int) (r / n) << 16 | (int) (gr / n) << 8 | (int) (b / n);
            int alpha = 255;
            if (transparent) {
                // Background becomes transparent, anti-aliased edges keep a partial alpha.
                double d = distance(rgb, bg);
                alpha = (int) Math.max(0, Math.min(255, (d - 12) * 255 / 48));
                if (alpha == 0) continue;
            }
            out.setRGB(x, y, alpha << 24 | rgb);
        }
        g.dispose();
        return out;
    }

    static void write(BufferedImage img, int type, File file) throws Exception {
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), type);
        Graphics2D g = out.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        ImageIO.write(out, "png", file);
    }

    static int average(BufferedImage s, int[][] points) {
        long r = 0, g = 0, b = 0, n = 0;
        for (int[] p : points) for (int dy = -8; dy <= 8; dy++) for (int dx = -8; dx <= 8; dx++) {
            int rgb = s.getRGB(p[0] + dx, p[1] + dy);
            r += rgb >> 16 & 255; g += rgb >> 8 & 255; b += rgb & 255; n++;
        }
        return (int) (r / n) << 16 | (int) (g / n) << 8 | (int) (b / n);
    }

    static double distance(int a, int b) {
        int dr = (a >> 16 & 255) - (b >> 16 & 255), dg = (a >> 8 & 255) - (b >> 8 & 255), db = (a & 255) - (b & 255);
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}
