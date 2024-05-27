package io.github.jmecn.font.utils;

import io.github.jmecn.font.exception.FtRuntimeException;

import java.io.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class FileUtils {
    private FileUtils() {
    }

    public static byte[] readAllBytes(InputStream inputStream) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(65536);
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[4096];
        int len;
        try {
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new FtRuntimeException("Read font data failed", e);
        }

        return bos.toByteArray();
    }
}
