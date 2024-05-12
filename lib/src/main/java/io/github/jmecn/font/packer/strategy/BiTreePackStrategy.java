package io.github.jmecn.font.packer.strategy;


import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.PackerPage;
import io.github.jmecn.font.packer.Rectangle;

import java.util.Comparator;
import java.util.List;

public class BiTreePackStrategy implements PackStrategy {
	Comparator<Rectangle> comparator;

	public void sort(List<Rectangle> images) {
		if (comparator == null) {
			comparator = Comparator.comparingInt(o -> Math.max(o.getWidth(), o.getHeight()));
		}
		images.sort(comparator);
	}

	public PackerPage pack(Packer packer, String name, Rectangle image) {
		BiTreePage page;
		if (packer.isEmpty()) {
			// Add a page if empty.
			page = new BiTreePage(packer);
			packer.addPage(page);
		} else {
			// Always try to pack into the last page.
			page = (BiTreePage) packer.peek();
		}

		int padding = packer.getPadding();
		image.addPadding(padding);

		Node node = page.insert(image);
		if (node == null) {
			// Didn't fit, pack into a new page.
			page = new BiTreePage(packer);
			packer.addPage(page);
			node = page.insert(image);
		}
		node.occupied = true;
		image.set(node.rect.getX(), node.rect.getY(), node.rect.getWidth() - padding, node.rect.getHeight() - padding);
		return page;
	}
}