package io.github.jmecn.font.packer;

import java.util.List;

/**
 * @author yanmaoyuan
 */
public interface PackStrategy {
    void sort(List<Rectangle> images);

    PackerPage pack(Packer packer, String name, Rectangle image);
}
