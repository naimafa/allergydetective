// App icon from the artwork (feedback 20.11.). Run with any JDK 11+ from the repo root:
//   java docs/marketing/IconTool.java make docs/marketing/app-icon-source.jpg <outDir> 100 55 1760 971 837 62
// then copy icon-1024.png to iosApp/.../AppIcon.appiconset/app-icon-1024.png, icon-512.png to docs/marketing/app-icon-512.png,
// ic_launcher_foreground.png to androidApp/src/main/res/drawable-nodpi/. The monochrome (themed) icon stays the vector
// from docs/marketing/app-icon.svg.

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * probe <src>                         prints colours along the middle row/column and at some points
 * make <src> <outDir> x y size cx cy  crops the square (x, y, size) and writes the icons;
 *                                     (cx, cy) = motif centre inside the crop
 */
public class IconTool {
    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[1]));
        if (a[0].equals("probe")) probe(src); else make(src, a);
    }

    static String c(int rgb) { return String.format("#%06X", rgb & 0xFFFFFF); }

    static int lum(int rgb) { return ((rgb >> 16 & 255) * 3 + (rgb >> 8 & 255) * 6 + (rgb & 255)) / 10; }

    static void probe(BufferedImage s) {
        int w = s.getWidth(), h = s.getHeight();
        System.out.println("size " + w + "x" + h);
        StringBuilder row = new StringBuilder("row y=1000: ");
        for (int x = 0; x < 260; x += 10) row.append(x).append(':').append(lum(s.getRGB(x, 1000))).append(' ');
        for (int x = w - 260; x < w; x += 10) row.append(x).append(':').append(lum(s.getRGB(x, 1000))).append(' ');
        System.out.println(row);
        StringBuilder col = new StringBuilder("col x=300: ");
        for (int y = 0; y < 200; y += 10) col.append(y).append(':').append(lum(s.getRGB(300, y))).append(' ');
        for (int y = h - 260; y < h; y += 10) col.append(y).append(':').append(lum(s.getRGB(300, y))).append(' ');
        System.out.println(col);
        int[][] pts = {{250, 1000}, {1000, 1700}, {250, 250}, {1750, 250}, {250, 1750}, {1000, 100}, {1850, 1000}};
        for (int[] p : pts) System.out.println("at " + p[0] + "," + p[1] + " " + c(s.getRGB(p[0], p[1])));
    }

    static BufferedImage scale(BufferedImage img, int size, int type) {
        BufferedImage out = new BufferedImage(size, size, type);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // Step down in halves for a smooth result.
        BufferedImage cur = img;
        int w = img.getWidth();
        while (w / 2 >= size) {
            w /= 2;
            BufferedImage half = new BufferedImage(w, w, BufferedImage.TYPE_INT_ARGB);
            Graphics2D hg = half.createGraphics();
            hg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            hg.drawImage(cur, 0, 0, w, w, null);
            hg.dispose();
            cur = half;
        }
        g.drawImage(cur, 0, 0, size, size, null);
        g.dispose();
        return out;
    }

    static void make(BufferedImage s, String[] a) throws Exception {
        File out = new File(a[2]);
        out.mkdirs();
        int x = Integer.parseInt(a[3]), y = Integer.parseInt(a[4]), size = Integer.parseInt(a[5]);
        int cx = Integer.parseInt(a[6]), cy = Integer.parseInt(a[7]);
        BufferedImage crop = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = crop.createGraphics();
        g.drawImage(s, -x, -y, null);
        g.dispose();

        // Full-bleed square for iOS and the Play Store: the system mask cuts the corners.
        ImageIO.write(scale(crop, 1024, BufferedImage.TYPE_INT_RGB), "png", new File(out, "icon-1024.png"));
        ImageIO.write(scale(crop, 512, BufferedImage.TYPE_INT_RGB), "png", new File(out, "icon-512.png"));

        // Background colour = average of the crop border (flat cream).
        long r = 0, gg = 0, b = 0, n = 0;
        for (int i = 0; i < size; i += 4) {
            for (int[] p : new int[][]{{i, 8}, {i, size - 9}, {8, i}, {size - 9, i}}) {
                int rgb = crop.getRGB(p[0], p[1]);
                r += rgb >> 16 & 255; gg += rgb >> 8 & 255; b += rgb & 255; n++;
            }
        }
        System.out.printf("background #%02X%02X%02X%n", r / n, gg / n, b / n);

        // Android adaptive foreground (432 px = 108 dp at xxxhdpi): the crop scaled so that the
        // motif fits the safe zone, centred on the motif, edges faded out into the background.
        double dp = Double.parseDouble(a[8]); // size of the crop in dp
        int fg = 432;
        int drawn = (int) Math.round(dp * 4);
        BufferedImage scaled = scale(crop, drawn, BufferedImage.TYPE_INT_ARGB);
        double k = drawn / (double) size;
        int fade = Math.max(1, (int) Math.round(drawn * 0.04));
        for (int py = 0; py < drawn; py++) {
            for (int px = 0; px < drawn; px++) {
                int edge = Math.min(Math.min(px, py), Math.min(drawn - 1 - px, drawn - 1 - py));
                if (edge < fade) {
                    int rgb = scaled.getRGB(px, py);
                    int alpha = (int) Math.round(255.0 * edge / fade);
                    scaled.setRGB(px, py, (alpha << 24) | (rgb & 0xFFFFFF));
                }
            }
        }
        BufferedImage fgImg = new BufferedImage(fg, fg, BufferedImage.TYPE_INT_ARGB);
        Graphics2D fgG = fgImg.createGraphics();
        fgG.drawImage(scaled, (int) Math.round(fg / 2.0 - cx * k), (int) Math.round(fg / 2.0 - cy * k), null);
        fgG.dispose();
        ImageIO.write(fgImg, "png", new File(out, "ic_launcher_foreground.png"));
    }
}
