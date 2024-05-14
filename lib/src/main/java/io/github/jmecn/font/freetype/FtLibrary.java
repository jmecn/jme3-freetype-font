package io.github.jmecn.font.freetype;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.freetype.FreeType.*;
import static io.github.jmecn.font.freetype.FtErrors.ok;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtLibrary implements AutoCloseable {

    static Logger logger = LoggerFactory.getLogger(FtLibrary.class);

    public static final int MIN_SPREAD = 2;
    public static final int MAX_SPREAD = 32;
    public static final int SDF_UNIT_ONE = 256;

    private long address;
    private final String version;
    private boolean isClosed;

    public FtLibrary() {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer ptr = stack.mallocPointer(1);
            ok(FT_Init_FreeType(ptr));
            address = ptr.get(0);

            IntBuffer major = stack.mallocInt(1);
            IntBuffer minor = stack.mallocInt(1);
            IntBuffer patch = stack.mallocInt(1);

            FT_Library_Version(address, major, minor, patch);
            version = String.format("%d.%d.%d", major.get(0), minor.get(0), patch.get(0));
            logger.info("Loaded FreeType {}", version);

            this.isClosed = false;
        }
    }

    /**
     * convert int to 26.6 fixed-point
     * @param x int
     * @return 26.6 fixed-point value
     */
    public static int int26D6(int x) {
        return x << 6;
    }

    /**
     * convert int to 16.16 fixed-point
     * @param x int
     * @return 16.16 fixed-point value
     */
    public static int int16D16(int x) {
        return x << 16;
    }


    public static int from26D6(long value) {
        return (int) (value >> 6);
    }

    public static int from16D16(long value) {
        return (int) (value >> 16);
    }

    public String getVersion() {
        return version;
    }

    public long address() {
        return address;
    }

    @Override
    public void close() {
        if (!isClosed) {
            FT_Done_FreeType(address);
            address = 0L;
            isClosed = true;
        }
    }

    public FtFace newFace(String filePath) {
        return newFace(filePath, 0);
    }

    public FtFace newFace(String filePath, long faceIndex) {
        return newFace(new File(filePath), faceIndex);
    }

    public FtFace newFace(File file, long faceIndex) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getPath());
        }

        try (MemoryStack stack = stackPush()) {
            PointerBuffer ptr = stack.mallocPointer(1);
            ok(FT_New_Face(address, file.getPath(), faceIndex, ptr));
            return new FtFace(ptr.get(0));
        }
    }

    public FtFace newFace(InputStream inputStream) throws IOException {
        return newFace(inputStream, 0);
    }

    public FtFace newFace(InputStream inputStream, long faceIndex) throws IOException {
        // Read all data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(65536);
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[4096];
        int len;
        while((len = bis.read(buffer) ) != -1) {
            bos.write(buffer, 0, len);
        }

        byte[] data = bos.toByteArray();
        if (data.length == 0) {
            throw new IllegalArgumentException("input stream is empty");
        }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length);
        byteBuffer.put(data);
        byteBuffer.flip();
        logger.info("load input data:{}", data.length);

        return newMemoryFace(byteBuffer, faceIndex);
    }

    public FtFace newMemoryFace(ByteBuffer buffer) {
        return newMemoryFace(buffer, 0);
    }

    public FtFace newMemoryFace(ByteBuffer buffer, long faceIndex) {
        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("Only support DirectByteBuffer");
        }
        try (MemoryStack stack = stackPush()) {
            PointerBuffer ptr = stack.mallocPointer(1);
            ok(FT_New_Memory_Face(address, buffer, faceIndex, ptr));
            return new FtFace(ptr.get(0));
        }
    }

    public FtStroker newStroker() {
        try (MemoryStack stack = stackPush()){
            PointerBuffer ptr = stack.mallocPointer(1);
            ok(FT_Stroker_New(address, ptr));
            return new FtStroker(ptr.get(0));
        }
    }

    public void getProperty(String module, String name, ByteBuffer buffer) {
        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("Only support DirectByteBuffer");
        }
        if (buffer.isReadOnly()) {
            throw new IllegalArgumentException("Only support writable ByteBuffer");
        }
        ok(FT_Property_Get(address, module, name, buffer));
    }

    public void setProperty(String module, String name, ByteBuffer buffer) {
        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("Only support DirectByteBuffer");
        }
        ok(FT_Property_Set(address, module, name, buffer));
    }

    public int getSdfSpread() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        getProperty("sdf", "spread", buffer);
        int spread = buffer.asIntBuffer().get(0);
        return from16D16(spread) >> 8;
    }

    public void setSdfSpread(int spread) {
        if (spread < MIN_SPREAD || spread > MAX_SPREAD) {
            throw new IllegalArgumentException("spread must be between " + MIN_SPREAD + " and " + MAX_SPREAD);
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        buffer.asIntBuffer().put(int16D16(spread << 8));
        setProperty("sdf", "spread", buffer);
    }

    public void setSdfFlipY(boolean flipY) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        buffer.put(0, (byte) (flipY ? 1 : 0));
        setProperty("sdf", "flip_y", buffer);
    }

    public boolean getSdfFlipY() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        getProperty("sdf", "flip_y", buffer);
        return buffer.get(0) != 0;
    }

    public void setSdfFlipSign(boolean flipSign) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        buffer.put(0, (byte) (flipSign ? 1 : 0));
        setProperty("sdf", "flip_sign", buffer);
    }

    public boolean getSdfFlipSign() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        getProperty("sdf", "flip_sign", buffer);
        return buffer.get(0) != 0;
    }

}