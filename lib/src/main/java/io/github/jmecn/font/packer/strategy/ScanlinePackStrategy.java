package io.github.jmecn.font.packer.strategy;

import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.PackerPage;
import io.github.jmecn.font.packer.Rectangle;

import java.util.Comparator;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class ScanlinePackStrategy implements PackStrategy {
    private Comparator<Rectangle> comparator;

    @Override
    public void sort(List<Rectangle> images) {
        if (comparator == null) {
            comparator = Comparator.comparingInt(Rectangle::getHeight);
        }
        images.sort(comparator);
    }

    @Override
    public PackerPage pack(Packer packer, String name, Rectangle image) {
        int padding = packer.getPadding();
        int pageWidth = packer.getPageWidth() - padding * 2;
        int pageHeight = packer.getPageHeight() - padding * 2;

        int rectWidth = image.getWidth() + padding;
        int rectHeight = image.getHeight() + padding;
        for (int i = 0, n = packer.getPages().size(); i < n; i++) {
            ScanlinePage page = (ScanlinePage) packer.getPages().get(i);
            Scanline bestLine = null;
            // Fit in any line before the last.
            for (int ii = 0, nn = page.lines.size() - 1; ii < nn; ii++) {
                Scanline line = page.lines.get(ii);
                if (line.x + rectWidth >= pageWidth || line.y + rectHeight >= pageHeight || rectHeight > line.height) {
                    continue;
                }
                if (bestLine == null || line.height < bestLine.height) {
                    bestLine = line;
                }
            }
            if (bestLine == null) {
                // Fit in last line, increasing height.
                Scanline line = page.lines.peek();
                if (line.y + rectHeight >= pageHeight) continue;
                if (line.x + rectWidth < pageWidth) {
                    line.height = Math.max(line.height, rectHeight);
                    bestLine = line;
                } else if (line.y + line.height + rectHeight < pageHeight) {
                    // Fit in new line.
                    bestLine = new Scanline(padding, line.y + line.height, rectHeight);
                    page.lines.add(bestLine);
                }
            }
            if (bestLine != null) {
                bestLine.add(image);
                return page;
            }
        }
        // Fit in new page.
        ScanlinePage page = new ScanlinePage(packer);
        packer.addPage(page);

        Scanline line = new Scanline(padding, padding, rectHeight);
        page.lines.add(line);
        line.add(image);
        return page;
    }
}
