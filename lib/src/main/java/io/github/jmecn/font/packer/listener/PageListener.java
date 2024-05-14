package io.github.jmecn.font.packer.listener;

import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Page;

/**
 * @author yanmaoyuan
 */
public interface PageListener {
    void onPageAdded(Packer packer, PackStrategy strategy, Page page);
}