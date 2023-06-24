package com.example;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

@ApplicationScoped
public class Service {

    private final Queue<String> channelQueue = new ConcurrentLinkedQueue<String>();
    private final Random random = new Random();

    private BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
                barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
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
