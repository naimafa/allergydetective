import javax.imageio.*; import javax.imageio.stream.*; import java.awt.image.BufferedImage; import java.io.File;
/**
 * Dark version of the leaf background: takes the picture composited on cream, finds the leaves by how much
 * greener than the cream a pixel is, and paints the same leaves, darkened, on the dark page colour.
 * Crops and fades the bottom like BgCut. Args: in.png out.jpg height fade quality pageHex leafScale strength
 */
public class BgDark { public static void main(String[] a) throws Exception {
  BufferedImage src = ImageIO.read(new File(a[0])); int h = Integer.parseInt(a[2]), fade = Integer.parseInt(a[3]);
  int page = Integer.parseInt(a[5], 16); double scale = Double.parseDouble(a[6]), strength = Double.parseDouble(a[7]);
  int pr = page>>16&255, pg = page>>8&255, pb = page&255;
  int W = src.getWidth();
  // Leafness map, then a closing (max then min over 11 px) so the thin light veins count as leaf.
  double[][] m = new double[h][W];
  for (int y = 0; y < h; y++) for (int x = 0; x < W; x++) { int p = src.getRGB(x, y); m[y][x] = Math.max(0, Math.min(1, ((p>>8&255) - (p>>16&255) + 6) / 34.0)); }
  m = filter(filter(m, 5, true), 5, false);
  BufferedImage o = new BufferedImage(W, h, BufferedImage.TYPE_INT_RGB);
  for (int y = 0; y < h; y++) { double t = y < h - fade ? 0 : (double)(y - (h - fade)) / fade; t = t*t*(3-2*t);
    for (int x = 0; x < src.getWidth(); x++) { int p = src.getRGB(x, y); int r=p>>16&255, g=p>>8&255, b=p&255;
      // Cream is red-ish (R > G); leaves are green (G > R). Leafness 0..1 from that difference.
      double leaf = m[y][x] * strength * (1 - t);
      // Leaf colour darkened, keeping its hue; light veins stay a bit lighter.
      double lr = r * scale, lg = g * scale, lb = b * scale;
      int R = (int)Math.round(pr + (lr - pr) * leaf), G = (int)Math.round(pg + (lg - pg) * leaf), B = (int)Math.round(pb + (lb - pb) * leaf);
      o.setRGB(x, y, (c(R)<<16)|(c(G)<<8)|c(B)); } }
  ImageWriter w = ImageIO.getImageWritersByFormatName("jpg").next(); ImageWriteParam pw = w.getDefaultWriteParam(); pw.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); pw.setCompressionQuality(Float.parseFloat(a[4]));
  try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(a[1]))) { w.setOutput(out); w.write(null, new IIOImage(o, null, null), pw); } }
  static double[][] filter(double[][] a, int r, boolean max) { int h = a.length, w = a[0].length; double[][] t = new double[h][w], o = new double[h][w];
    for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) { double v = a[y][x]; for (int k = Math.max(0, x - r); k <= Math.min(w - 1, x + r); k++) v = max ? Math.max(v, a[y][k]) : Math.min(v, a[y][k]); t[y][x] = v; }
    for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) { double v = t[y][x]; for (int k = Math.max(0, y - r); k <= Math.min(h - 1, y + r); k++) v = max ? Math.max(v, t[k][x]) : Math.min(v, t[k][x]); o[y][x] = v; }
    return o; }
  static int c(double v) { return (int)Math.max(0, Math.min(255, Math.round(v))); } }
