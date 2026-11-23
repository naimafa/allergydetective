import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Recolours the icon with the start screen colours (Theme.kt, HomeColors light): every pixel keeps its
 * brightness, but its colour comes from the start screen ramp – greens from #053923 over #184F34,
 * #478661, sage #C2D2B2 / #EDEEDC to cream #FBF4EA; orange pixels (the bolt) from the heart/moon orange
 * #D47636. Shading and gradients of the picture stay.
 * Args: in out.png
 */
public class IconPalette {
    static final int[] GREEN = {0x053923, 0x184F34, 0x478661, 0xC2D2B2, 0xEDEEDC, 0xFBF4EA};
    static final int[] ORANGE = {0x9A4A33, 0xD47636, 0xF0A27A, 0xFBF4EA};

    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[0]));
        int w = src.getWidth(), h = src.getHeight();
        double gLo = lum(GREEN[0]), gHi = lum(GREEN[GREEN.length - 1]);
        BufferedImage o = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        // Source luminance range of the greens, so the darkest source green maps to #053923 and cream to cream.
        double sLo = 1e9, sHi = 0;
        for (int y = 0; y < h; y += 2) for (int x = 0; x < w; x += 2) {
            int p = src.getRGB(x, y);
            if (isOrange(p)) continue;
            double l = lum(p); sLo = Math.min(sLo, l); sHi = Math.max(sHi, l);
        }
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int p = src.getRGB(x, y);
            double l = lum(p);
            int c;
            if (isOrange(p)) {
                // Bolt: its mid tone becomes #D47636.
                double t = clamp((l - 110) / 150.0);
                c = ramp(ORANGE, t);
            } else {
                // Slightly more contrast: dark greens reach the deep start screen greens, cream stays cream.
                double t = Math.pow(clamp((l - sLo) / (sHi - 14 - sLo)), 1.35);
                c = rampByLum(GREEN, gLo + t * (gHi - gLo));
            }
            o.setRGB(x, y, c);
        }
        ImageIO.write(o, "png", new File(a[1]));
    }

    static boolean isOrange(int p) {
        int r = p >> 16 & 255, g = p >> 8 & 255, b = p & 255;
        return r > g + 35 && r > b + 60;
    }

    /** Picks the colour on the ramp whose luminance matches [target], interpolating between stops. */
    static int rampByLum(int[] stops, double target) {
        for (int i = 0; i < stops.length - 1; i++) {
            double l0 = lum(stops[i]), l1 = lum(stops[i + 1]);
            if (target <= l1 || i == stops.length - 2) return mix(stops[i], stops[i + 1], clamp((target - l0) / (l1 - l0)));
        }
        return stops[stops.length - 1];
    }

    static int ramp(int[] stops, double t) {
        double f = t * (stops.length - 1);
        int i = Math.min(stops.length - 2, (int) f);
        return mix(stops[i], stops[i + 1], f - i);
    }

    static double lum(int p) { return 0.299 * (p >> 16 & 255) + 0.587 * (p >> 8 & 255) + 0.114 * (p & 255); }

    static double clamp(double v) { return Math.max(0, Math.min(1, v)); }

    static int mix(int a, int b, double t) {
        int r = (int) Math.round((a >> 16 & 255) + ((b >> 16 & 255) - (a >> 16 & 255)) * t);
        int g = (int) Math.round((a >> 8 & 255) + ((b >> 8 & 255) - (a >> 8 & 255)) * t);
        int bl = (int) Math.round((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
