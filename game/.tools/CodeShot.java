import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 把代码文本渲染成编辑器风格的截图（深色主题 + 行号 + 简易高亮）。
 * 西文用 Consolas，中文自动回退到微软雅黑，避免出现方块。
 * 用法: java CodeShot.java <in.txt> <out.png> <title>
 */
public class CodeShot {

    static final Color BG = new Color(0x1E, 0x1E, 0x1E);
    static final Color TAB = new Color(0x25, 0x25, 0x26);
    static final Color GUTTER = new Color(0x6E, 0x76, 0x81);
    static final Color LINE = new Color(0x2A, 0x2A, 0x2B);
    static final Color TEXT = new Color(0xD4, 0xD4, 0xD4);
    static final Color COMMENT = new Color(0x6A, 0x99, 0x55);
    static final Color STRING = new Color(0xCE, 0x91, 0x78);
    static final Color TAG = new Color(0x56, 0x9C, 0xD6);
    static final Color ATTR = new Color(0x9C, 0xDC, 0xFE);
    static final Color KEYWORD = new Color(0x56, 0x9C, 0xD6);
    static final Color CONTROL = new Color(0xC5, 0x86, 0xC0);
    static final Color TYPE = new Color(0x4E, 0xC9, 0xB0);
    static final Color ANNO = new Color(0xDC, 0xDC, 0xAA);

    static Font mono, cjk;
    static FontMetrics fmMono, fmCjk;

    static final String[] KEYWORDS = {
            "public", "private", "protected", "class", "extends", "implements", "interface",
            "static", "final", "void", "int", "boolean", "float", "double", "long", "char",
            "import", "package", "this", "super", "true", "false", "null", "String",
            "new", "return", "if", "else", "for", "while", "switch", "case", "break"
    };

    public static void main(String[] args) throws Exception {
        List<String> lines = Files.readAllLines(new File(args[0]).toPath(), StandardCharsets.UTF_8);
        String title = args.length > 2 ? args[2] : "";

        mono = new Font("Consolas", Font.PLAIN, 17);
        cjk = new Font("Microsoft YaHei", Font.PLAIN, 15);

        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        pg.setFont(mono);
        fmMono = pg.getFontMetrics(mono);
        pg.setFont(cjk);
        fmCjk = pg.getFontMetrics(cjk);

        int padX = 22, padTop = 16, lineH = 27;
        int gutterW = fmMono.stringWidth("000") + 18;
        int maxW = 0;
        for (String l : lines) {
            maxW = Math.max(maxW, measure(l));
        }
        int headerH = title.isEmpty() ? 0 : 38;
        int w = gutterW + maxW + padX * 2;
        int h = headerH + padTop * 2 + lines.size() * lineH;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(BG);
        g.fillRect(0, 0, w, h);

        if (headerH > 0) {
            g.setColor(TAB);
            g.fillRect(0, 0, w, headerH);
            g.setColor(new Color(0x33, 0x33, 0x33));
            g.fillRect(0, headerH - 1, w, 1);
            g.setFont(new Font("Consolas", Font.PLAIN, 15));
            g.setColor(new Color(0xCC, 0xCC, 0xCC));
            g.drawString(title, padX, headerH - 12);
        }

        g.setColor(LINE);
        g.fillRect(gutterW - 1, headerH, 1, h - headerH);

        int y = headerH + padTop + fmMono.getAscent();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String no = String.valueOf(i + 1);
            g.setFont(mono);
            g.setColor(GUTTER);
            g.drawString(no, gutterW - 12 - fmMono.stringWidth(no), y);
            drawTokens(g, line, gutterW + padX, y);
            y += lineH;
        }
        g.dispose();
        ImageIO.write(img, "png", new File(args[1]));
        System.out.println("saved " + args[1] + " " + w + "x" + h);
    }

    static boolean isCJK(char c) {
        return c >= 0x2E80;
    }

    static int measure(String s) {
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            w += isCJK(c) ? fmCjk.charWidth(c) : fmMono.charWidth(c);
        }
        return w;
    }

    static int draw(Graphics2D g, String s, int x, int y, Color color) {
        g.setColor(color);
        int cx = x;
        int i = 0, n = s.length();
        while (i < n) {
            boolean cjkRun = isCJK(s.charAt(i));
            int j = i;
            while (j < n && isCJK(s.charAt(j)) == cjkRun) j++;
            String seg = s.substring(i, j);
            g.setFont(cjkRun ? cjk : mono);
            g.drawString(seg, cx, y);
            cx += (cjkRun ? fmCjk : fmMono).stringWidth(seg);
            i = j;
        }
        return cx;
    }

    static boolean inBlockComment = false;

    static void drawTokens(Graphics2D g, String line, int x, int y) {
        int i = 0, n = line.length();
        int cx = x;
        g.setFont(mono);
        while (i < n) {
            char c = line.charAt(i);

            if (inBlockComment) {
                int end = line.indexOf("-->", i);
                int end2 = line.indexOf("*/", i);
                int stop = -1;
                if (end >= 0) stop = end + 3;
                if (end2 >= 0 && (stop < 0 || end2 + 2 < stop)) stop = end2 + 2;
                String s = stop < 0 ? line.substring(i) : line.substring(i, stop);
                cx = draw(g, s, cx, y, COMMENT);
                if (stop < 0) { i = n; } else { i = stop; inBlockComment = false; }
                continue;
            }
            if (line.startsWith("<!--", i) || line.startsWith("/*", i)) {
                inBlockComment = true;
                continue;
            }
            if (line.startsWith("//", i)) {
                cx = draw(g, line.substring(i), cx, y, COMMENT);
                i = n;
                continue;
            }
            if (c == '"') {
                int j = i + 1;
                while (j < n && line.charAt(j) != '"') {
                    if (line.charAt(j) == '\\') j++;
                    j++;
                }
                j = Math.min(j + 1, n);
                cx = draw(g, line.substring(i, j), cx, y, STRING);
                i = j;
                continue;
            }
            if (c == '<' && i + 1 < n && (Character.isLetter(line.charAt(i + 1)) || line.charAt(i + 1) == '/')) {
                int j = i + 1;
                if (line.charAt(j) == '/') j++;
                while (j < n && (Character.isLetterOrDigit(line.charAt(j)) || ".:_-".indexOf(line.charAt(j)) >= 0)) j++;
                cx = draw(g, line.substring(i, j), cx, y, TAG);
                i = j;
                continue;
            }
            if (c == '@') {
                int j = i + 1;
                while (j < n && Character.isLetterOrDigit(line.charAt(j))) j++;
                cx = draw(g, line.substring(i, j), cx, y, ANNO);
                i = j;
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                int j = i;
                while (j < n && (Character.isLetterOrDigit(line.charAt(j)) || line.charAt(j) == '_')) j++;
                String word = line.substring(i, j);
                int k = j;
                while (k < n && line.charAt(k) == ' ') k++;
                boolean isAttr = (k < n && line.charAt(k) == '=');
                Color col = TEXT;
                if (isAttr) col = ATTR;
                else if (isKeyword(word)) col = keywordColor(word);
                else if (Character.isUpperCase(word.charAt(0))) col = TYPE;
                cx = draw(g, word, cx, y, col);
                i = j;
                continue;
            }
            cx = draw(g, String.valueOf(c), cx, y, TEXT);
            i++;
        }
    }

    static boolean isKeyword(String w) {
        for (String k : KEYWORDS) if (k.equals(w)) return true;
        return false;
    }

    static Color keywordColor(String w) {
        switch (w) {
            case "new": case "return": case "if": case "else": case "for": case "while":
            case "switch": case "case": case "break": case "true": case "false": case "null":
                return CONTROL;
            default:
                return KEYWORD;
        }
    }
}
