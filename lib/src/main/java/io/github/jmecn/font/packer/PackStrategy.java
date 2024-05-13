package io.github.jmecn.font.packer;

import java.util.List;

/**
 * @author yanmaoyuan
 */
public interface PackStrategy {
    void sort(List<Rectangle> images);

    Page pack(Packer packer, String name, Rectangle image);
}
