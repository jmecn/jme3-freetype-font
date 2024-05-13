package io.github.jmecn.font.packer.strategy;

import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Page;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SkylinePage extends Page {
    protected List<Row> lines;
    public SkylinePage(Packer packer) {
        super(packer);
        this.lines = new ArrayList<>();
    }

    public int rows() {
        return lines.size();
    }

    public Row get(int i) {
        return lines.get(i);
    }

    public Row getLast() {
        if (lines.isEmpty()) {
            return null;
        }
        return lines.get(lines.size() - 1);
    }

    public void add(Row row) {
        lines.add(row);
    }
}
