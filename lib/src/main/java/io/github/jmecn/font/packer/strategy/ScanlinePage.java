package io.github.jmecn.font.packer.strategy;

import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.PackerPage;

import java.util.LinkedList;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class ScanlinePage extends PackerPage {
    protected LinkedList<Scanline> lines;
    public ScanlinePage(Packer packer) {
        super(packer);
        this.lines = new LinkedList<>();
    }
}
