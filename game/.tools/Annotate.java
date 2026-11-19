import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * 在截图上画圆角矩形标注 + 序号角标。
 * 用法: java Annotate.java <in.png> <out.png> "x1,y1,x2,y2,编号" ...
 */
public class Annotate {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: Annotate in.png out.png x1,y1,x2,y2,label ...");
            return;
        }
        BufferedImage src = ImageIO.read(new File(args[0]));
        BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Color red = new Color(0xFF, 0x2D, 0x55);

        for (int i = 2; i < args.length; i++) {
            String[] p = args[i].split(",");
            float x1 = Float.parseFloat(p[0].trim());
            float y1 = Float.parseFloat(p[1].trim());
            float x2 = Float.parseFloat(p[2].trim());
            float y2 = Float.parseFloat(p[3].trim());
            String label = p.length > 4 ? p[4].trim() : "";

            RoundRectangle2D r = new RoundRectangle2D.Float(x1, y1, x2 - x1, y2 - y1, 26, 26);

            // 白色描边打底，保证在深色/浅色背景上都清晰
            g.setColor(new Color(255, 255, 255, 235));
            g.setStroke(new BasicStroke(11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(r);
            // 红色主描边
            g.setColor(red);
            g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(r);

            if (!label.isEmpty()) {
                float cx = Math.max(30f, x1);
                float cy = Math.max(30f, y1);
                float rad = 27f;
                g.setColor(new Color(255, 255, 255, 240));
                g.fill(new Ellipse2D.Float(cx - rad - 4, cy - rad - 4, (rad + 4) * 2, (rad + 4) * 2));
                g.setColor(red);
                g.fill(new Ellipse2D.Float(cx - rad, cy - rad, rad * 2, rad * 2));

                g.setColor(Color.WHITE);
                g.setFont(new Font("Microsoft YaHei", Font.BOLD, 30));
                FontMetrics fm = g.getFontMetrics();
                int tw = fm.stringWidth(label);
                g.drawString(label, cx - tw / 2f, cy + (fm.getAscent() - fm.getDescent()) / 2f);
            }
        }

        g.dispose();
        ImageIO.write(img, "png", new File(args[1]));
        System.out.println("saved " + args[1] + " " + img.getWidth() + "x" + img.getHeight());
    }
}
