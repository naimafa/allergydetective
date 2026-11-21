import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Dark start screen background from the LIGHT leaf picture: same branch, same place, but on forest green
 * with vivid green leaves like the user's dark picture.
 * - leafness: how far a pixel is from the cream (the light picture's leaves are greener / darker than the cream)
 * - shade: brightness inside the leaf (light picture: darker = deeper green, lighter = tips and veins)
 * - sharpness: soft, out-of-focus leaves (few edges around them) stay dimmer, like in the dark picture
 * - strength: leaves that are pale in the light picture stay pale here too (user feedback 21.11.: the leaf
 *   beside the progress card stood out); the green halo around the branch is kept faint
 * Args: lightOnCream.png out.jpg height fade quality [softAlpha softMix saturation brightness]
 * Used for home_background_dark.jpg (variant A of 21.11.2026, chosen by the user):
 *   BgRecolor full.png home_background_dark.jpg 1100 200 0.86 0.08 0.2 0.85 0.92
 * (full.png = the light source picture rendered on #FBF4EA at 1024 x 1536, e.g. with headless Edge)
 */
public class BgRecolor {
    static final int BG = 0x062115, GLOW = 0x0F3A25;
    static final int[] BASE = {0x1F5A2F, 0x4E8F57, 0xADCF96};
    static final int[] STOPS = BASE.clone();

    static int tone(int c, double sat, double bright) {
        double r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255, l = 0.299 * r + 0.587 * g + 0.114 * b;
        r = (l + (r - l) * sat) * bright; g = (l + (g - l) * sat) * bright; b = (l + (b - l) * sat) * bright;
        return ((int) Math.round(r) << 16) | ((int) Math.round(g) << 8) | (int) Math.round(b);
    }

    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[0]));
        int w = src.getWidth(), h = Integer.parseInt(a[2]), fade = Integer.parseInt(a[3]);
        // Optional: how strong the soft leaves are, and saturation / brightness of the leaf greens.
        double softAlpha = a.length > 5 ? Double.parseDouble(a[5]) : 0.45, softMix = a.length > 6 ? Double.parseDouble(a[6]) : 0.55;
        double sat = a.length > 7 ? Double.parseDouble(a[7]) : 1, bright = a.length > 8 ? Double.parseDouble(a[8]) : 1;
        for (int i = 0; i < 3; i++) STOPS[i] = tone(BASE[i], sat, bright);
        double[][] lum = new double[h][w], leaf = new double[h][w], strength = new double[h][w];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int p = src.getRGB(x, y), r = p >> 16 & 255, g = p >> 8 & 255, b = p & 255;
                double d = Math.sqrt(sq(r - 0xFB) + sq(g - 0xF4) + sq(b - 0xEA));
                leaf[y][x] = clamp((d - 9) / 38.0);
                // How strong the leaf is in the light picture: pale leaves stay pale in the dark copy too.
                strength[y][x] = clamp((d - 9) / 70.0);
                lum[y][x] = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
            }
        // Close small gaps (light veins) so they count as leaf.
        double[][] m = box(filter(filter(leaf, 3, true), 3, false), 2);
        // Edge density: crisp leaves have strong edges nearby, blurred ones don't.
        double[][] edge = new double[h][w];
        for (int y = 1; y < h - 1; y++)
            for (int x = 1; x < w - 1; x++)
                edge[y][x] = Math.abs(leaf[y][x + 1] - leaf[y][x - 1]) + Math.abs(leaf[y + 1][x] - leaf[y - 1][x]) > 0.3 ? 1 : 0;
        edge = box(box(edge, 40), 20);
        double[][] glow = box(m, 50);
        double[][] strength2 = filter(filter(strength, 3, true), 3, false);

        BufferedImage o = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            double t = y < h - fade ? 0 : (double) (y - (h - fade)) / fade;
            t = t * t * (3 - 2 * t);
            for (int x = 0; x < w; x++) {
                double sharp = clamp(edge[y][x] / 0.025);
                double alpha = m[y][x] * (softAlpha + (1 - softAlpha) * sharp) * Math.pow(strength2[y][x], 1.6) * (1 - t);
                // Soft leaves get an even mid shade (no light rim); crisp leaves keep their light and dark parts.
                double f = clamp((lum[y][x] - 0.76) / 0.18) * sharp + 0.38 * (1 - sharp);
                int leafColor = ramp(f);
                int base = mix(BG, GLOW, clamp(glow[y][x] * 0.35) * (1 - t));
                // Soft leaves also lose some brightness, like the out-of-focus leaves in the dark picture.
                int dimmed = mix(mix(BG, leafColor, softMix), leafColor, sharp);
                o.setRGB(x, y, mix(base, dimmed, alpha));
            }
        }
        ImageWriter wr = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam p = wr.getDefaultWriteParam();
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        p.setCompressionQuality(Float.parseFloat(a[4]));
        try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(a[1]))) {
            wr.setOutput(out);
            wr.write(null, new IIOImage(o, null, null), p);
        }
    }

    static int ramp(double f) {
        return f < 0.5 ? mix(STOPS[0], STOPS[1], f / 0.5) : mix(STOPS[1], STOPS[2], (f - 0.5) / 0.5);
    }

    static double sq(double v) { return v * v; }

    static double clamp(double v) { return Math.max(0, Math.min(1, v)); }

    static int mix(int a, int b, double t) {
        int r = (int) Math.round((a >> 16 & 255) + ((b >> 16 & 255) - (a >> 16 & 255)) * t);
        int g = (int) Math.round((a >> 8 & 255) + ((b >> 8 & 255) - (a >> 8 & 255)) * t);
        int bl = (int) Math.round((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    static double[][] filter(double[][] a, int r, boolean max) {
        int h = a.length, w = a[0].length;
        double[][] t = new double[h][w], o = new double[h][w];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double v = a[y][x];
                for (int k = Math.max(0, x - r); k <= Math.min(w - 1, x + r); k++) v = max ? Math.max(v, a[y][k]) : Math.min(v, a[y][k]);
                t[y][x] = v;
            }
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double v = t[y][x];
                for (int k = Math.max(0, y - r); k <= Math.min(h - 1, y + r); k++) v = max ? Math.max(v, t[k][x]) : Math.min(v, t[k][x]);
                o[y][x] = v;
            }
        return o;
    }

    /** Box blur with radius r via running sums. */
    static double[][] box(double[][] a, int r) {
        int h = a.length, w = a[0].length;
        double[][] t = new double[h][w], o = new double[h][w];
        for (int y = 0; y < h; y++) {
            double s = 0; int n = 0;
            for (int x = -r; x < w; x++) {
                int add = x + r, rem = x - r - 1;
                if (add < w) { s += a[y][add]; n++; }
                if (rem >= 0) { s -= a[y][rem]; n--; }
                if (x >= 0) t[y][x] = s / n;
            }
        }
        for (int x = 0; x < w; x++) {
            double s = 0; int n = 0;
            for (int y = -r; y < h; y++) {
                int add = y + r, rem = y - r - 1;
                if (add < h) { s += t[add][x]; n++; }
                if (rem >= 0) { s -= t[rem][x]; n--; }
                if (y >= 0) o[y][x] = s / n;
            }
        }
        return o;
    }
}
