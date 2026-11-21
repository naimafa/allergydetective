import javax.imageio.*; import javax.imageio.stream.*; import java.awt.image.BufferedImage; import java.io.File;
/** Crops the leaf background to its upper part, fades the bottom into the page cream, writes a JPEG. */
public class BgCut { public static void main(String[] a) throws Exception {
  BufferedImage src = ImageIO.read(new File(a[0])); int h = Integer.parseInt(a[2]), fade = Integer.parseInt(a[3]); int page = 0xFBF4EA;
  BufferedImage o = new BufferedImage(src.getWidth(), h, BufferedImage.TYPE_INT_RGB);
  for (int y = 0; y < h; y++) { double t = y < h - fade ? 0 : (double)(y - (h - fade)) / fade; t = t * t * (3 - 2 * t);
    for (int x = 0; x < src.getWidth(); x++) { int p = src.getRGB(x, y); int r = mix((p>>16)&255, page>>16&255, t), g = mix((p>>8)&255, page>>8&255, t), b = mix(p&255, page&255, t); o.setRGB(x, y, (r<<16)|(g<<8)|b); } }
  ImageWriter w = ImageIO.getImageWritersByFormatName("jpg").next(); ImageWriteParam pr = w.getDefaultWriteParam(); pr.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); pr.setCompressionQuality(Float.parseFloat(a[4]));
  try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(a[1]))) { w.setOutput(out); w.write(null, new IIOImage(o, null, null), pr); } }
  static int mix(int a, int b, double t) { return (int)Math.round(a + (b - a) * t); } }
