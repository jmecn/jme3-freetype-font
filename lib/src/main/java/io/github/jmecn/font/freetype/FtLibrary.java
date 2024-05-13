package io.github.jmecn.font.freetype;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
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

    private long library;
    private final String version;
    private boolean isClosed;

    public FtLibrary() {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer ptr = stack.mallocPointer(1);
            ok(FT_Init_FreeType(ptr));
            library = ptr.get(0);

            IntBuffer major = stack.mallocInt(1);
            IntBuffer minor = stack.mallocInt(1);
            IntBuffer patch = stack.mallocInt(1);

            FT_Library_Version(library, major, minor, patch);
            version = String.format("%d.%d.%d", major.get(0), minor.get(0), patch.get(0));
            logger.info("Loaded FreeType {}", version);

            this.isClosed = false;
        }
    }

    public static int int26D6(int x) {
        return x << 6;
    }

    public static int from26D6ToInt(long value) {
        return (int) (((value + 63) & -64) >> 6);
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void close() {
        if (!isClosed) {
            FT_Done_FreeType(library);
            library = 0L;
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
            ok(FT_New_Face(library, file.getPath(), faceIndex, ptr));
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
            ok(FT_New_Memory_Face(library, buffer, faceIndex, ptr));
            return new FtFace(ptr.get(0));
        }
    }

    public FtStroker newStroker() {
        try (MemoryStack stack = stackPush()){
            PointerBuffer ptr = stack.mallocPointer(1);
            ok(FT_Stroker_New(library, ptr));
            return new FtStroker(ptr.get(0));
        }
    }
}
