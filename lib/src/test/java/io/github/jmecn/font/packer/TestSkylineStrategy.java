package io.github.jmecn.font.packer;

import io.github.jmecn.font.packer.strategy.SkylineStrategy;

public class TestSkylineStrategy {

    public static void main(String[] args) {
        new TestPackStrategy(new SkylineStrategy()).run();
    }
}
