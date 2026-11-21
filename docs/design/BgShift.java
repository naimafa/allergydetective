import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Places an opaque background picture shifted by (dx, dy) on its own background colour, crops it and
 * writes a JPEG. The cut edges at the top and left fade softly into the background colour.
 * Args: in.png out.jpg width height dx dy bgHex quality
 */
public class BgShift {
    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[0]));
        int w = Integer.parseInt(a[2]), h = Integer.parseInt(a[3]), dx = Integer.parseInt(a[4]), dy = Integer.parseInt(a[5]);
        int bg = Integer.parseInt(a[6], 16);
        int feather = 120;
        BufferedImage o = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int sx = x - dx, sy = y - dy;
                int px = bg;
                double t = 0;
                if (sx >= 0 && sy >= 0 && sx < src.getWidth() && sy < src.getHeight()) {
                    px = src.getRGB(sx, sy) & 0xFFFFFF;
                    t = smooth(Math.min(1.0, sy / (double) feather)) * (dx > 0 ? smooth(Math.min(1.0, sx / (double) feather)) : 1);
                }
                o.setRGB(x, y, mix(bg, px, t));
            }
        }
        ImageWriter wr = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam p = wr.getDefaultWriteParam();
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        p.setCompressionQuality(Float.parseFloat(a[7]));
        try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(a[1]))) {
            wr.setOutput(out);
            wr.write(null, new IIOImage(o, null, null), p);
        }
    }

    static double smooth(double t) { return t * t * (3 - 2 * t); }

    static int mix(int a, int b, double t) {
        int r = (int) Math.round((a >> 16 & 255) + ((b >> 16 & 255) - (a >> 16 & 255)) * t);
        int g = (int) Math.round((a >> 8 & 255) + ((b >> 8 & 255) - (a >> 8 & 255)) * t);
        int bl = (int) Math.round((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
