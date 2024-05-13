package io.github.jmecn.font.harfbuzz;

import static org.lwjgl.util.harfbuzz.HarfBuzz.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2024/5/13
 */
public class HbBlob implements AutoCloseable{
    private long address;

    public HbBlob(String file) {
        address = hb_blob_create_from_file(file);
    }

    @Override
    public void close() throws Exception {
        hb_blob_destroy(address);
    }

    public HbFace createFace(int faceIndex) {
        long face = hb_face_create(address, faceIndex);
        return new HbFace(face);
    }

}
