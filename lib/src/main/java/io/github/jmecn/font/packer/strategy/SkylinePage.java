package io.github.jmecn.font.packer.strategy;

import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Page;

import java.util.LinkedList;

public class SkylinePage extends Page {
    protected LinkedList<Row> lines;
    public SkylinePage(Packer packer) {
        super(packer);
        this.lines = new LinkedList<>();
    }
}
