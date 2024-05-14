package io.github.jmecn.font.packer;

import com.jme3.texture.Image;
import com.jme3.util.IntMap;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TestPackStrategy {

    static final int PAGE_SIZE = 1024;
    static final int RECT_COUNT = 5000;
    static final int CHAR_SIZE = 32;

    private final PackStrategy packStrategy;

    TestPackStrategy(PackStrategy packStrategy) {
        this.packStrategy = packStrategy;
    }

    /**
     * 随机生成一些矩形，用来模拟英文、汉字字符的大小。
     * <p>英文符号一般是 half width, 中文符号一般是 full width。标点符号一般是 half width & half height。</p>
     * <p>生成矩形大小随机，避免出现矩形大小相同的情况。</p>
     * @param count
     * @param size
     * @return
     */
    List<Rectangle> randomRectangles(int count, int size) {
        List<Rectangle> rectangles = new ArrayList<>();

        Random rand = new Random();
        int halfSize = size / 2;
        int quarterSize = halfSize / 2;
        for (int i = 0; i < count; i++) {
            int type = (int) (Math.random() * 3);
            int width = 0;
            int height = 0;
            switch (type) {
                case 0: // english character, width = random halfSize +/- random quarterSize, height = size +/- random quarterSize
                    width = halfSize + (int) ((rand.nextDouble() * 2 - 1.0) * quarterSize);
                    height = size + (int) ((rand.nextDouble() * 2 - 1.0) * quarterSize);
                    break;
                case 1: {// chinese character, width = size +/- random quarterSize, height = size +/- random quarterSize
                    width = size + (int) ((rand.nextDouble() * 2 - 1.0) * quarterSize);
                    height = size + (int) ((rand.nextDouble() * 2 - 1.0) * quarterSize);
                    break;
                }
                case 2: {// symbol, width = random halfSize +/- random quarterSize, height = random halfSize +/- random quarterSize
                    width = halfSize + (int) ((rand.nextDouble() * 2 - 1.0) * quarterSize);
                    height = halfSize + (int) ((rand.nextDouble() * 2 - 1.0) * quarterSize);
                    break;
                }
            }
            rectangles.add(new Rectangle(width, height));
        }

        return rectangles;
    }

    void showResult(String strategy, List<Rectangle> list, long time) {
        IntMap<BufferedImage> images = new IntMap<>();
        IntMap<Graphics2D> g2ds = new IntMap<>();
        for (Rectangle rect : list) {
            int page = rect.getPage();
            if (!images.containsKey(page)) {
                BufferedImage image = new BufferedImage(PAGE_SIZE, PAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = image.createGraphics();
                g2d.clearRect(0, 0, PAGE_SIZE, PAGE_SIZE);
                images.put(page, image);
                g2ds.put(page, g2d);
            }
        }

        for (Rectangle rect : list) {
            Graphics2D g2d = g2ds.get(rect.getPage());
            // random color
            int r = (int) (Math.random() * 255);
            int g = (int) (Math.random() * 255);
            int b = (int) (Math.random() * 255);
            int a = 255;
            int color = (a << 24) | (r << 16) | (g << 8) | b;

            int x = rect.getX();
            int y = rect.getY();
            int width = rect.getWidth();
            int height = rect.getHeight();

            g2d.setColor(new Color(color));
            g2d.fillRect(x, y, width, height);
            //g2d.drawRect(x, y, width, height);
        }

        g2ds.forEach(e -> e.getValue().dispose());

        // show images
        JFrame frame = new JFrame();
        frame.setTitle(strategy + ", resolution:" + PAGE_SIZE + ", characters:" + list.size() + ", pages:" + images.size() + ", cost " + time + "ms");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JTabbedPane panel = new JTabbedPane();
        images.forEach(e -> {
            panel.add("page " + e.getKey(), new JLabel(new ImageIcon(e.getValue())));
        });
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public void run() {
        List<Rectangle> list = randomRectangles(RECT_COUNT, CHAR_SIZE);

        long start = System.currentTimeMillis();

        Packer packer = new Packer(Image.Format.RGBA8, PAGE_SIZE, PAGE_SIZE, 1, true);
        packStrategy.sort(list);
        for (Rectangle rect : list) {
            Page page = packStrategy.pack(packer, null, rect);
            rect.setPage(page.index);
            System.out.printf("%s, page:%d\n", rect, page.index);
        }

        long time = System.currentTimeMillis() - start;

        showResult(packStrategy.getClass().getSimpleName(), list, time);
    }
}
