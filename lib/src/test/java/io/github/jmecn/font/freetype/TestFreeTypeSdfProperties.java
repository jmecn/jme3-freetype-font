package io.github.jmecn.font.freetype;

import io.github.jmecn.font.exception.FtRuntimeException;
import io.github.jmecn.font.utils.DebugPrintUtils;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestFreeTypeSdfProperties {

    @Test void testGetDefaultSpread() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buf = stack.malloc(4);
            library.getProperty("sdf", "spread", buf);
            int spread = buf.getInt();
            assertEquals(FtLibrary.DEFAULT_SPREAD, spread);
        }
    }

    @Test void testGetDefaultSpread2() {
        try (FtLibrary library = new FtLibrary()) {
            assertEquals(FtLibrary.DEFAULT_SPREAD, library.getSdfSpread());
        }
    }

    @Test void testSetMaxSpread() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            buffer.putInt(FtLibrary.MAX_SPREAD);
            buffer.flip();
            assertDoesNotThrow(() -> library.setProperty("sdf", "spread", buffer));
            assertEquals(FtLibrary.MAX_SPREAD, library.getSdfSpread());
        }
    }

    @Test void testSetMinSpread() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            buffer.putInt(FtLibrary.MIN_SPREAD);
            buffer.flip();
            assertDoesNotThrow(() -> library.setProperty("sdf", "spread", buffer));
            assertEquals(FtLibrary.MIN_SPREAD, library.getSdfSpread());
        }
    }

    @Test void testSetSpread() {
        try (FtLibrary library = new FtLibrary()) {
            assertDoesNotThrow(() -> library.setSdfSpread(4));
            assertEquals(4, library.getSdfSpread());
        }
    }

    @Test void testIllegalSpread() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            buffer.putInt(FtLibrary.MAX_SPREAD + 1);
            buffer.flip();
            assertThrows(FtRuntimeException.class, () -> library.setProperty("sdf", "spread", buffer));
            assertEquals(FtLibrary.DEFAULT_SPREAD, library.getSdfSpread());
        }
    }

    @Test void testIllegalSpread2() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            buffer.putInt(FtLibrary.MIN_SPREAD - 1);
            buffer.flip();
            assertThrows(FtRuntimeException.class, () -> library.setProperty("sdf", "spread", buffer));
            assertEquals(FtLibrary.DEFAULT_SPREAD, library.getSdfSpread());
        }
    }

    @Test void testGetFlipY() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            library.getProperty("sdf", "flip_y", buffer);
            int flip = buffer.getInt();
            assertEquals(0, flip);
            assertFalse(library.getSdfFlipY());
        }
    }

    @Test void testSetFlipY() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            buffer.putInt(1).flip();
            assertDoesNotThrow(() -> library.setProperty("sdf", "flip_y", buffer));
            assertTrue(library.getSdfFlipY());
        }
    }

    @Test void testSetNegativeFlipY() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            buffer.putInt(-1).flip();
            assertDoesNotThrow(() -> library.setProperty("sdf", "flip_y", buffer));
            assertTrue(library.getSdfFlipY());
        }
    }

    @Test void testGetFlipSign() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            library.getProperty("sdf", "flip_sign", buffer);
            int flip = buffer.getInt();
            assertEquals(0, flip);
        }
    }

    @Test void testSetFlipSign() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            buffer.putInt(1).flip();
            assertDoesNotThrow(() -> library.setProperty("sdf", "flip_sign", buffer));
            assertTrue(library.getSdfFlipSign());
        }
    }
    @Test void testSetFlipSignTrue() {
        try (FtLibrary library = new FtLibrary()) {
            assertDoesNotThrow(() -> library.setSdfFlipSign(true));
            assertTrue(library.getSdfFlipSign());
        }
    }

    @Test void testSetFlipSignFalse() {
        try (FtLibrary library = new FtLibrary()) {
            assertDoesNotThrow(() -> library.setSdfFlipSign(false));
            assertFalse(library.getSdfFlipSign());
        }
    }

    @Test void testSetNegativeFlipSign() {
        try (FtLibrary library = new FtLibrary(); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            buffer.putInt(-1).flip();
            assertDoesNotThrow(() -> library.setProperty("sdf", "flip_sign", buffer));
            assertTrue(library.getSdfFlipSign());
        }
    }


}