package com.example;

import io.nayuki.qrcodegen.QrCode;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@ApplicationScoped
public class Service {

    private final Queue<String> channelQueue = new ConcurrentLinkedQueue<String>();
    private final Random random = new Random();

    private BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
        Objects.requireNonNull(qr);
        if (scale <= 0 || border < 0)
            throw new IllegalArgumentException("Value out of range");
        if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
            throw new IllegalArgumentException("Scale or border too large");

        BufferedImage result = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                boolean color = qr.getModule(x / scale - border, y / scale - border);
                result.setRGB(x, y, color ? darkColor : lightColor);
            }
        }
        return result;
    }


    private BufferedImage generateQRCodeImage(String barcodeText) {
        QrCode qr = QrCode.encodeBinary(barcodeText.getBytes(), QrCode.Ecc.MEDIUM);
        return toImage(qr, 5, 2, 0xFFFFFF, 0x000000);
    }

    // Get QR code as byte array writing from BufferedImage
    private byte[] getQRBytes(BufferedImage image) throws Exception {
        try {
            var baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Get QR code as Base64 encoded string
    private String getQRString(BufferedImage image) throws Exception {
        return Base64.getEncoder().encodeToString(
                getQRBytes(image)
        );
    }

    private String getRandomString(int byteSize) {
        byte[] bytes = new byte[byteSize];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    private String getNewChannel() {
        String channel = getRandomString(32);
        channelQueue.add(channel);
        return channel;
    }

    public Multi<String> getChannels() {
        return Multi.createFrom().items(channelQueue.stream());
    }

    public Map<String, String> getData() throws Exception {
        String channel = getNewChannel();
        return Map.of(
                "qr", getQRString(generateQRCodeImage(channel)),
                "channel", channel
        );
    }

    public void removeChannel(String channel) {
        channelQueue.remove(channel);
    }
}
