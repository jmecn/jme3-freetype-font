package io.github.jmecn.font.utils;

import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;

import java.nio.ByteBuffer;

public final class ImageUtils {
    private ImageUtils() {}

    public static Image newImage(Image.Format format, int width, int height) {
        int capacity = format.getBitsPerPixel() * width * height / 8;
        return new Image(format, width, height, ByteBuffer.allocateDirect(capacity), ColorSpace.Linear);
    }

    public static void drawImage(Image dest, Image source, int x, int y) {
        int destWidth = dest.getWidth();
        int destHeight = dest.getHeight();
        int destSize = destWidth * destHeight * dest.getFormat().getBitsPerPixel() / 8;
        byte[] image = new byte[destSize];

        ByteBuffer sourceData = source.getData(0);
        int height = source.getHeight();
        int width = source.getWidth();
        for (int yPos = 0; yPos < height; yPos++) {
            for (int xPos = 0; xPos < width; xPos++) {
                int i = ((xPos + x) + (yPos + y) * destWidth) * 4;
                if (source.getFormat() == Image.Format.ABGR8) {
                    int j = (xPos + yPos * width) * 4;
                    image[i] = sourceData.get(j); //a
                    image[i + 1] = sourceData.get(j + 1); //b
                    image[i + 2] = sourceData.get(j + 2); //g
                    image[i + 3] = sourceData.get(j + 3); //r
                } else if (source.getFormat() == Image.Format.BGR8) {
                    int j = (xPos + yPos * width) * 3;
                    image[i] = 1; //a
                    image[i + 1] = sourceData.get(j); //b
                    image[i + 2] = sourceData.get(j + 1); //g
                    image[i + 3] = sourceData.get(j + 2); //r
                } else if (source.getFormat() == Image.Format.RGB8) {
                    int j = (xPos + yPos * width) * 3;
                    image[i] = 1; //a
                    image[i + 1] = sourceData.get(j + 2); //b
                    image[i + 2] = sourceData.get(j + 1); //g
                    image[i + 3] = sourceData.get(j); //r
                } else if (source.getFormat() == Image.Format.RGBA8) {
                    int j = (xPos + yPos * width) * 4;
                    image[i] = sourceData.get(j + 3); //a
                    image[i + 1] = sourceData.get(j + 2); //b
                    image[i + 2] = sourceData.get(j + 1); //g
                    image[i + 3] = sourceData.get(j); //r
                } else if (source.getFormat() == Image.Format.Luminance8) {
                    int j = (xPos + yPos * width) * 1;
                    image[i] = 1; //a
                    image[i + 1] = sourceData.get(j); //b
                    image[i + 2] = sourceData.get(j); //g
                    image[i + 3] = sourceData.get(j); //r
                } else if (source.getFormat() == Image.Format.Luminance8Alpha8) {
                    int j = (xPos + yPos * width) * 2;
                    image[i] = sourceData.get(j + 1); //a
                    image[i + 1] = sourceData.get(j); //b
                    image[i + 2] = sourceData.get(j); //g
                    image[i + 3] = sourceData.get(j); //r
                } else {
                    throw new UnsupportedOperationException("Cannot draw textures with format " + source.getFormat());
                }
            }
        }

        ByteBuffer destData = dest.getData(0);
        destData.put(image);
        destData.flip();
        dest.setUpdateNeeded();
    }
}
