import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Simplified app icon (user's wishes of 21.11.2026): keep the light green area in the bottom corner, drop the
 * other light green arcs and the bars, make the magnifier bigger.
 * Rebuilt in layers from app-icon-magnifier.png (1138 px, start screen colours):
 *  1. cream #FBF4EA
 *  2. the bottom corner as a clean shape with the measured greens (lighter at the top edge, deeper at the bottom)
 *  3. the leaf: only its dark green pixels, so the light arcs next to it disappear
 *  4. the magnifier (lens content, ring, handle) cut out and scaled by [scale] around the lens centre
 * Args: in.png out.png [scale] [green top edge at the left and right, as part of the height]
 */
public class IconRebuild {
    static final int CREAM = 0xFBF4EA;
    // Measured in app-icon-magnifier.png.
    static final double CX = 523, CY = 457, R_OUT = 322, R_IN = 252;

    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[0]));
        int n = src.getWidth();
        double scale = a.length > 2 ? Double.parseDouble(a[2]) : 1.15;
        // New lens centre a little up and left, so the bigger handle stays inside the visible area.
        double ncx = CX - 50, ncy = CY - 45;

        BufferedImage out = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(new Color(CREAM));
        g.fillRect(0, 0, n, n);

        // 2. Bottom corner, larger on the user's wish: green up to the lens ring: the edge meets the ring where the handle starts.
        Path2D wave = new Path2D.Double();
        double left = a.length > 3 ? Double.parseDouble(a[3]) : 0.862, right = a.length > 4 ? Double.parseDouble(a[4]) : 0.342;
        wave.moveTo(0, n * left);
        wave.curveTo(n * 0.36, n * (left - (left - right) * 0.25), n * 0.72, n * (left - (left - right) * 0.5), n, n * right);
        wave.lineTo(n, n);
        wave.lineTo(0, n);
        wave.closePath();
        g.setPaint(new LinearGradientPaint(new Point2D.Double(n * 0.5, n * 0.5), new Point2D.Double(n * 0.75, n),
                new float[]{0f, 0.35f, 1f}, new Color[]{new Color(0xDCE5CD), new Color(0xC2D2B2), new Color(0xA8C29F)}));
        g.fill(wave);

        // 3. Leaf: dark pixels left of the lens, outside the ring.
        BufferedImage leaf = layer(src, (x, y) -> x > 12 && x < 560 && y > 470 && y < 980 && Math.hypot(x - CX, y - CY) > R_OUT + 8
                && !(x >= 395 && y >= 815 && y <= 905 && lum(src.getRGB(x, y)) > 105)); // not the swoosh under the lens
        g.drawImage(leaf, 0, 0, null);

        // 4. Magnifier: everything inside the lens, the ring and the handle; then scaled.
        BufferedImage mag = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) {
            double d = Math.hypot(x - CX, y - CY);
            int p = src.getRGB(x, y);
            double m;
            if (d <= R_IN) {
                m = 1; // lens: cream with the light rim of the ring
                // Fork (352,321 - 490,639), divider (x 527-547) and bolt (571,321 - 741,639) are cleared and drawn
                // again below: fork and heart the same height, divider as long as both (user's wish of 21.11.2026).
                if (x >= 340 && x <= 755 && y >= 305 && y <= 655 && d < R_IN - 18) p = CREAM;
            }
            else if (d <= R_OUT + 6) m = keep(p); // ring
            else if (x >= 690 && y >= 630 && x < 1065 && y < 975) m = keep(p); // handle
            else m = 0;
            if (m > 0) mag.setRGB(x, y, ((int) Math.round(m * 255) << 24) | (p & 0xFFFFFF));
        }
        // Heart with pulse line in place of the bolt (user's wish of 21.11.2026): the same shape and orange as the
        // symptom icon on the start screen (HeartPulseIcon in HomeArt.kt, 100 x 100 units).
        Graphics2D gm = mag.createGraphics();
        gm.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gm.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        gm.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        // Row in the middle of the lens: fork | gap | heart, both ROW_H high around ROW_Y. No divider, and the fork
        // 1.5 times as wide as in the picture, so it weighs as much as the heart (user's wish of 21.11.2026).
        // The fork is drawn again as a clean shape (stretching the picture made it squat): three round tines, a
        // bowl and a round handle, 0.6 times as wide as high, in the greens of the picture's fork.
        // Phone test 21.11.: as large as the lens allows (the row just fits inside the ring at its top edge).
        final double ROW_H = 245 / scale, ROW_Y = CY, HEART_W = 245 / scale * 100 / 88.0, GAP = 32 / scale;
        double forkW = ROW_H * 0.52;
        double rowW = forkW + GAP + HEART_W, rowX = CX - rowW / 2;
        Graphics2D gf = (Graphics2D) gm.create();
        gf.translate(rowX, ROW_Y - ROW_H / 2);
        gf.scale(forkW / 60.0 * 1.0, ROW_H / 100.0);
        Area forkShape = new Area();
        double tine = 16, gapT = (60 - 3 * tine) / 2;
        for (int k = 0; k < 3; k++)
            forkShape.add(new Area(new RoundRectangle2D.Double(k * (tine + gapT), 0, tine, 46, tine, tine)));
        Path2D bowl = new Path2D.Double();
        bowl.moveTo(0, 34);
        bowl.lineTo(60, 34);
        bowl.lineTo(60, 40);
        bowl.curveTo(60, 52, 48, 59, 37, 61);
        bowl.lineTo(23, 61);
        bowl.curveTo(12, 59, 0, 52, 0, 40);
        bowl.closePath();
        forkShape.add(new Area(bowl));
        forkShape.add(new Area(new RoundRectangle2D.Double(20, 52, 20, 48, 20, 14)));
        gf.setPaint(new GradientPaint(0, 0, new Color(0x2A6A47), 0, 100, new Color(0x184F34)));
        gf.fill(forkShape);
        gf.dispose();
        double heartX = rowX + forkW + GAP;
        double hs = ROW_H / 88.0; // heart is 88 units high (4..92)
        double hx = heartX + HEART_W / 2 - 50 * hs;
        double hy = ROW_Y - ROW_H / 2 - 4 * hs;
        gm.translate(hx, hy);
        gm.scale(hs, hs);
        Path2D heart = new Path2D.Double();
        heart.moveTo(50, 92);
        heart.curveTo(12, 66, -2, 42, 8, 24);
        heart.curveTo(18, 6, 42, 4, 50, 24);
        heart.curveTo(58, 4, 82, 6, 92, 24);
        heart.curveTo(102, 42, 88, 66, 50, 92);
        heart.closePath();
        gm.setPaint(new GradientPaint(0, 4, new Color(0xDC8C58), 0, 92, new Color(0xD47636)));
        gm.fill(heart);
        Path2D pulse = new Path2D.Double();
        pulse.moveTo(10, 48);
        pulse.lineTo(34, 48);
        pulse.lineTo(42, 32);
        pulse.lineTo(53, 66);
        pulse.lineTo(61, 42);
        pulse.lineTo(67, 48);
        pulse.lineTo(90, 48);
        gm.setColor(new Color(CREAM));
        gm.setStroke(new BasicStroke(6.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gm.draw(pulse);
        gm.dispose();
        AffineTransform t = new AffineTransform();
        t.translate(ncx, ncy);
        t.scale(scale, scale);
        t.translate(-CX, -CY);
        g.drawImage(mag, t, null);
        g.dispose();

        BufferedImage rgb = new BufferedImage(n, n, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = rgb.createGraphics();
        g2.drawImage(out, 0, 0, null);
        g2.dispose();
        ImageIO.write(rgb, "png", new File(a[1]));
    }

    interface Region { boolean in(int x, int y); }

    static BufferedImage layer(BufferedImage src, Region r) {
        int n = src.getWidth();
        BufferedImage o = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) {
            if (!r.in(x, y)) continue;
            int p = src.getRGB(x, y);
            double m = keep(p);
            if (m > 0) o.setRGB(x, y, ((int) Math.round(m * 255) << 24) | (p & 0xFFFFFF));
        }
        return o;
    }

    static double lum(int p) { return 0.299 * (p >> 16 & 255) + 0.587 * (p >> 8 & 255) + 0.114 * (p & 255); }

    /** Keeps dark and saturated pixels (shapes), drops cream and the light green arcs (luminance above ~195). */
    static double keep(int p) {
        int r = p >> 16 & 255, g = p >> 8 & 255, b = p & 255;
        double l = 0.299 * r + 0.587 * g + 0.114 * b;
        double dark = Math.max(0, Math.min(1, (198 - l) / 40.0));
        double sat = (Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b))) / 255.0;
        double orange = r > g + 35 ? Math.max(0, Math.min(1, (sat - 0.2) / 0.15)) : 0;
        return Math.max(dark, orange);
    }
}
