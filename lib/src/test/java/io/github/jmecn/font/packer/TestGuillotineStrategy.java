package io.github.jmecn.font.packer;

import io.github.jmecn.font.packer.strategy.GuillotineStrategy;

public class TestGuillotineStrategy {
    public static void main(String[] args) {
        new TestPackStrategy(new GuillotineStrategy()).run();
    }
}
