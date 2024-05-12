package io.github.jmecn.font.packer.strategy;


import io.github.jmecn.font.packer.Rectangle;

/**
 * desc:
 *
 * @see <a href="https://blackpawn.com/texts/lightmaps/default.html">Packing Lightmaps</a>
 * @author yanmaoyuan
 */
public class Node {
    Rectangle rect;
    Node left;
    Node right;
    boolean occupied;
    public Node(int x, int y, int width, int height) {
        rect = new Rectangle(x, y, width, height);
        left = null;
        right = null;
        occupied = false;
    }

    boolean isLeaf() {
        return left == null && right == null;
    }

    public Node insert(Rectangle image) {
        if (!isLeaf()) {
            Node newNode = left.insert(image);
            if (newNode != null) {
                return newNode;
            }
            return right.insert(image);
        } else {
            if (occupied) {
                return null;
            }

            // if we're too small
            if (image.getWidth() > rect.getWidth() || image.getHeight() > rect.getHeight()) {
                return null;
            }

            // if we're just fit
            if (image.getWidth() == rect.getWidth() && image.getHeight() == rect.getHeight()) {
                return this;
            }

            int dw = rect.getWidth() - image.getWidth();
            int dh = rect.getHeight() - image.getHeight();

            if (dw > dh) {
                left = new Node(rect.getX(), rect.getY(), image.getWidth(), rect.getHeight());
                right = new Node(rect.getX() + image.getWidth(), rect.getY(), rect.getWidth() - image.getWidth(), rect.getHeight());
            } else {
                left = new Node(rect.getX(), rect.getY(), rect.getWidth(), image.getHeight());
                right = new Node(rect.getX(), rect.getY() + image.getHeight(), rect.getWidth(), rect.getHeight() - image.getHeight());
            }

            return left.insert(image);
        }
    }
}
