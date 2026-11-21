import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;

/**
 * Removes the dark green frame with rounded corners around an icon picture: flood fill from the image
 * edges over the frame colour (plus its antialiased seam), then fills those pixels from their inner
 * neighbours (cream at the top, the light wave at the bottom), so the picture runs edge to edge.
 * Args: in out.png frameHex tolerance
 */
public class IconUnframe {
    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[0]));
        int w = src.getWidth(), h = src.getHeight(), frame = Integer.parseInt(a[2], 16);
        double tol = Double.parseDouble(a[3]);
        int[] px = src.getRGB(0, 0, w, h, null, 0, w);
        boolean[] mask = new boolean[w * h];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int x = 0; x < w; x++) { q.add(x); q.add((h - 1) * w + x); }
        for (int y = 0; y < h; y++) { q.add(y * w); q.add(y * w + w - 1); }
        while (!q.isEmpty()) {
            int i = q.poll();
            if (mask[i] || dist(px[i], frame) > tol) continue;
            mask[i] = true;
            int x = i % w, y = i / w;
            if (x > 0) q.add(i - 1); if (x < w - 1) q.add(i + 1); if (y > 0) q.add(i - w); if (y < h - 1) q.add(i + w);
        }
        // Grow the mask by a few pixels to take the blended seam between frame and picture too.
        for (int r = 0; r < 4; r++) {
            boolean[] m2 = mask.clone();
            for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                int i = y * w + x;
                if (!mask[i] && (mask[i - 1] || mask[i + 1] || mask[i - w] || mask[i + w])) m2[i] = true;
            }
            mask = m2;
        }
        // Fill inwards-out: each pass sets masked pixels next to known ones to the average of those.
        boolean changed = true;
        while (changed) {
            changed = false;
            boolean[] m2 = mask.clone();
            int[] p2 = px.clone();
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                int i = y * w + x;
                if (!mask[i]) continue;
                long r = 0, g = 0, b = 0; int n = 0;
                for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                    int xx = x + dx, yy = y + dy;
                    if (xx < 0 || yy < 0 || xx >= w || yy >= h) continue;
                    int j = yy * w + xx;
                    if (mask[j]) continue;
                    r += px[j] >> 16 & 255; g += px[j] >> 8 & 255; b += px[j] & 255; n++;
                }
                if (n > 0) { p2[i] = 0xFF000000 | (int) (r / n) << 16 | (int) (g / n) << 8 | (int) (b / n); m2[i] = false; changed = true; }
            }
            mask = m2; px = p2;
        }
        BufferedImage o = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        o.setRGB(0, 0, w, h, px, 0, w);
        ImageIO.write(o, "png", new File(a[1]));
    }

    static double dist(int p, int c) {
        int dr = (p >> 16 & 255) - (c >> 16 & 255), dg = (p >> 8 & 255) - (c >> 8 & 255), db = (p & 255) - (c & 255);
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}
