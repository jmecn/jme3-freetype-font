package io.github.jmecn.font.packer.strategy;

import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.PackerPage;
import io.github.jmecn.font.packer.Rectangle;

class BiTreePage extends PackerPage {
	Node root;

	public BiTreePage(Packer packer) {
		super(packer);
		int x = packer.getPadding();
		int y = packer.getPadding();
		int width = packer.getPageWidth() - packer.getPadding() * 2;
		int height = packer.getPageHeight() - packer.getPadding() * 2;
		root = new Node(x, y, width, height);
	}

	public Node insert(Rectangle rect) {
		return root.insert(rect);
	}
}