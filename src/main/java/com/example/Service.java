package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Queue<Token> tokenQueue = new ConcurrentLinkedQueue<Token>();

    public record Token(String token) {
    }

    private Random random = new Random();

    private BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
        System.out.println("generateQRCodeImage> " + barcodeText);
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

    public byte[] getQR(String token) {
        try {
            return getQRBytes(generateQRCodeImage(token));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getTokenQR() {
        String token = getTokenString();
        Token tokenObj = new Token(token);
        tokenQueue.add(tokenObj);
        try {
            return getQRBytes(generateQRCodeImage(objectMapper.writeValueAsString(tokenObj)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getTokenString() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    private Token newToken() {
        String token = getTokenString();
        Token tokenObj = new Token(token);
        tokenQueue.add(tokenObj);
        return tokenObj;
    }

    public Multi<Token> getTokens() {
        return Multi.createFrom().items(tokenQueue.stream());
    }


    public Map<String, String> getData() throws Exception {
        Token token = newToken();
        return Map.of(
                "qr", getQRString(generateQRCodeImage(token.token)),
                "channel", "ws://localhost:8080/" + token.token
        );
    }

    public void removeToken(Token token) {
        tokenQueue.remove(token);
    }
}
