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
        images.sort(comparator.reversed());
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
            Row bestRow = null;
            int len = page.rows();
            // Fit in any row before the last.
            for (int ii = 0, nn = len - 1; ii < nn; ii++) {
                Row row = page.get(ii);
                if (row.x + rectWidth >= pageWidth || row.y + rectHeight >= pageHeight || rectHeight > row.height) {
                    continue;
                }
                if (bestRow == null || row.height < bestRow.height) {
                    bestRow = row;
                }
            }
            if (bestRow == null) {
                // Fit in last row, increasing height.
                Row row = page.getLast();
                if (row.y + rectHeight >= pageHeight) continue;
                if (row.x + rectWidth < pageWidth) {
                    row.height = Math.max(row.height, rectHeight);
                    bestRow = row;
                } else if (row.y + row.height + rectHeight < pageHeight) {
                    // Fit in new row.
                    bestRow = new Row(padding, row.y + row.height, rectHeight);
                    page.add(bestRow);
                }
            }
            if (bestRow != null) {
                bestRow.add(image, padding);
                return page;
            }
        }
        // Fit in new page.
        SkylinePage page = new SkylinePage(packer);
        packer.addPage(page);

        Row row = new Row(padding, padding, rectHeight);
        page.add(row);
        row.add(image, padding);
        return page;
    }
}
