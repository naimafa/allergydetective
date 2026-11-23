import javax.imageio.ImageIO; import java.awt.*; import java.awt.image.BufferedImage; import java.io.File;
/** Rebuilds the alpha from renders on black and magenta, crops to the leaves and scales to a square PNG. */
public class LogoCut { public static void main(String[] a) throws Exception {
  BufferedImage k = ImageIO.read(new File(a[0])), m = ImageIO.read(new File(a[1])); int W = k.getWidth(), H = k.getHeight(), size = Integer.parseInt(a[3]);
  BufferedImage rgba = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB); int x0=W,y0=H,x1=0,y1=0;
  for (int y=0;y<H;y++) for (int x=0;x<W;x++) { int pk=k.getRGB(x,y), pm=m.getRGB(x,y);
    double al = 1 - (((pm>>16)&255) - ((pk>>16)&255)) / 255.0; al = Math.max(0, Math.min(1, al));
    int A=(int)Math.round(al*255), r=0,g=0,b=0; if (al>0.004) { r=c(((pk>>16)&255)/al); g=c(((pk>>8)&255)/al); b=c((pk&255)/al); }
    rgba.setRGB(x,y,(A<<24)|(r<<16)|(g<<8)|b); if (A>8) { x0=Math.min(x0,x); y0=Math.min(y0,y); x1=Math.max(x1,x); y1=Math.max(y1,y);} }
  int bw=x1-x0+1, bh=y1-y0+1, side=Math.max(bw,bh); int pad=side/40; side+=2*pad;
  BufferedImage sq = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB); Graphics2D g=sq.createGraphics();
  g.drawImage(rgba.getSubimage(x0,y0,bw,bh), (side-bw)/2, (side-bh)/2, null); g.dispose();
  // Scale down in halving steps for a smooth result.
  BufferedImage cur = sq; while (cur.getWidth()/2 >= size) { cur = scale(cur, cur.getWidth()/2); } if (cur.getWidth()!=size) cur = scale(cur, size);
  ImageIO.write(cur, "png", new File(a[2])); System.out.println("box "+bw+"x"+bh+" -> "+size); }
  static int c(double v){ return (int)Math.max(0,Math.min(255,Math.round(v))); }
  static BufferedImage scale(BufferedImage s, int n){ BufferedImage o=new BufferedImage(n,n,BufferedImage.TYPE_INT_ARGB); Graphics2D g=o.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC); g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(s,0,0,n,n,null); g.dispose(); return o; } }
