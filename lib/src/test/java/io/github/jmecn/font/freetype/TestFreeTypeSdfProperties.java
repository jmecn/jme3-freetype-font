package io.github.jmecn.font.freetype;

import io.github.jmecn.font.exception.FtRuntimeException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestFreeTypeSdfProperties {

    static final int DEFAULT_SPREAD = 8;
    static final int MIN_SPREAD = 2;
    static final int MAX_SPREAD = 32;

    static final int UNIT_ONE = 256;

    @Test void testGetDefaultSpread() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer spreadBuf = ByteBuffer.allocateDirect(4);
            library.getProperty("sdf", "spread", spreadBuf);
            int spread = FtLibrary.from16D16(spreadBuf.asIntBuffer().get(0)) / UNIT_ONE;
            assertEquals(DEFAULT_SPREAD, spread);
        }
    }

    @Test void testSetMaxSpread() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer spreadBuf = ByteBuffer.allocateDirect(4);
            spreadBuf.asIntBuffer().put(FtLibrary.int16D16(MAX_SPREAD * UNIT_ONE));
            assertDoesNotThrow(() -> library.setProperty("sdf", "spread", spreadBuf));
        }
    }

    @Test void testSetMinSpread() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer spreadBuf = ByteBuffer.allocateDirect(4);
            spreadBuf.asIntBuffer().put(FtLibrary.int16D16(MIN_SPREAD * UNIT_ONE));
            assertDoesNotThrow(() -> library.setProperty("sdf", "spread", spreadBuf));
        }
    }

    @Test void testIllegalSpread() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer spreadBuf = ByteBuffer.allocateDirect(4);
            spreadBuf.asIntBuffer().put(FtLibrary.int16D16(33 * UNIT_ONE));
            assertThrows(FtRuntimeException.class, () -> library.setProperty("sdf", "spread", spreadBuf));
        }
    }

    @Test void testIllegalSpread2() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer spreadBuf = ByteBuffer.allocateDirect(4);
            spreadBuf.asIntBuffer().put(FtLibrary.int16D16(UNIT_ONE));
            assertThrows(FtRuntimeException.class, () -> library.setProperty("sdf", "spread", spreadBuf));
        }
    }

    @Test void testGetFlipY() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer flipBuf = ByteBuffer.allocateDirect(4);
            library.getProperty("sdf", "flip_y", flipBuf);
            int flip = flipBuf.asIntBuffer().get(0);
            assertEquals(0, flip);
        }
    }

    @Test void testSetFlipY() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer flipBuf = ByteBuffer.allocateDirect(4);
            flipBuf.asIntBuffer().put(1);
            assertDoesNotThrow(() -> library.setProperty("sdf", "flip_y", flipBuf));
        }
    }

    @Test void testSetNegativeFlipY() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer flipBuf = ByteBuffer.allocateDirect(4);
            flipBuf.asIntBuffer().put(-1);
            assertDoesNotThrow(() -> library.setProperty("sdf", "flip_y", flipBuf));
        }
    }

    @Test void testGetFlipSign() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer flipBuf = ByteBuffer.allocateDirect(4);
            library.getProperty("sdf", "flip_sign", flipBuf);
            int flip = flipBuf.asIntBuffer().get(0);
            assertEquals(0, flip);
        }
    }

    @Test void testSetFlipSign() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer flipBuf = ByteBuffer.allocateDirect(4);
            flipBuf.asIntBuffer().put(1);
            assertDoesNotThrow(() -> library.setProperty("sdf", "flip_sign", flipBuf));
        }
    }

    @Test void testSetNegativeFlipSign() {
        try (FtLibrary library = new FtLibrary()) {
            ByteBuffer flipBuf = ByteBuffer.allocateDirect(4);
            flipBuf.asIntBuffer().put(-1);
            assertDoesNotThrow(() -> library.setProperty("sdf", "flip_sign", flipBuf));
        }
    }
}