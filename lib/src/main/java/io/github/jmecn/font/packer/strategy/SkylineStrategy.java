package io.github.jmecn.font.packer.strategy;

import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Page;
import io.github.jmecn.font.packer.Rectangle;

import java.util.Comparator;
import java.util.List;

public class SkylineStrategy implements PackStrategy {
    private Comparator<Rectangle> comparator;

    @Override
    public void sort(List<Rectangle> images) {
        if (comparator == null) {
            comparator = Comparator.comparingInt(Rectangle::getHeight);
        }
        images.sort(comparator);
    }

    @Override
    public Page pack(Packer packer, String name, Rectangle image) {
        int padding = packer.getPadding();
        int pageWidth = packer.getPageWidth() - padding * 2;
        int pageHeight = packer.getPageHeight() - padding * 2;

        int rectWidth = image.getWidth() + padding;
        int rectHeight = image.getHeight() + padding;
        for (int i = 0, n = packer.getPages().size(); i < n; i++) {
            SkylinePage page = (SkylinePage) packer.getPages().get(i);
            Row bestLine = null;
            // Fit in any line before the last.
            for (int ii = 0, nn = page.lines.size() - 1; ii < nn; ii++) {
                Row line = page.lines.get(ii);
                if (line.x + rectWidth >= pageWidth || line.y + rectHeight >= pageHeight || rectHeight > line.height) {
                    continue;
                }
                if (bestLine == null || line.height < bestLine.height) {
                    bestLine = line;
                }
            }
            if (bestLine == null) {
                // Fit in last line, increasing height.
                Row line = page.lines.peek();
                if (line.y + rectHeight >= pageHeight) continue;
                if (line.x + rectWidth < pageWidth) {
                    line.height = Math.max(line.height, rectHeight);
                    bestLine = line;
                } else if (line.y + line.height + rectHeight < pageHeight) {
                    // Fit in new line.
                    bestLine = new Row(padding, line.y + line.height, rectHeight);
                    page.lines.add(bestLine);
                }
            }
            if (bestLine != null) {
                bestLine.add(image);
                return page;
            }
        }
        // Fit in new page.
        SkylinePage page = new SkylinePage(packer);
        packer.addPage(page);

        Row line = new Row(padding, padding, rectHeight);
        page.lines.add(line);
        line.add(image);
        return page;
    }
}
