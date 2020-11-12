package com.frame.modules.untils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 伽马加深图片亮度
 *
 * @author xhl
 * @date 2020-11-06
 */
public class GammaImage {
    public int destW, destH;
    private int[] lut;
    private double gamma;

    public GammaImage(double gamma) {
        this.lut = new int[256];
        this.gamma = gamma;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        destW = width;
        destH = height;
        if (dst == null)
            dst = createCompatibleDestImage(src, null);

        // Gamma correction
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        getRGB(src, 0, 0, width, height, inPixels);
        setupGammaLut();
        int index = 0;
        for (int row = 0; row < height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for (int col = 0; col < width; col++) {
                index = row * width + col;
                ta = (inPixels[index] >> 24) & 0xff;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
                outPixels[index] = (ta << 24) | (lut[tr] << 16) | (lut[tg] << 8) | lut[tb];
            }
        }

        // 返回结果
        setRGB(dst, 0, 0, width, height, outPixels);
        return dst;
    }

    /**
     * 读取像素数据
     *
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     * @param pixels
     * @return
     */
    public int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
            return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
        } else {
            return image.getRGB(x, y, width, height, pixels, 0, width);
        }
    }

    /**
     * 写入像素数据
     *
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     * @param pixels
     */
    public void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
            image.getRaster().setDataElements(x, y, width, height, pixels);
        } else {
            image.setRGB(x, y, width, height, pixels, 0, width);
        }
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null)
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(destW, destH), dstCM.isAlphaPremultiplied(), null);
    }

    private void setupGammaLut() {
        for (int i = 0; i < 256; i++) {
            lut[i] = (int) (Math.exp(Math.log(i / 255.0) * gamma) * 255.0);
        }
    }


    /**
     * @param srcImage 输入的图片路径
     * @param depth    亮度
     * @param outImage 输出的图片地址
     * @return 输出的图片路径
     */
    public void convert(String srcImage, int depth, String outImage) {
        //String srcImage="C:\\cutpdf\\cutImage\\b37c13963f5e43f8bcd15ea69780e605\\0_result.png";
        BufferedImage src = ImgUtil.read(new File(srcImage));
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        GammaImage gammaTest = new GammaImage(depth);
        BufferedImage image = gammaTest.filter(src, dst);
        File file = new File(outImage);
        if (!file.exists()) {
            FileUtil.touch(file);
        }
        ImgUtil.write(image, FileUtil.file(outImage));

    }

}
