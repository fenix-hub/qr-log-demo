package com.example.infrastructure.qr;

import com.example.domain.qr.QRCodeData;
import com.example.domain.qr.QRCodeGenerationException;
import com.example.domain.qr.QRCodeGenerator;
import io.nayuki.qrcodegen.QrCode;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

/**
 * Implementation of QRCodeGenerator using the nayuki QR code library
 * This is an adapter implementing the port defined in the domain
 */
@ApplicationScoped
public class NayukiQRCodeGenerator implements QRCodeGenerator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NayukiQRCodeGenerator.class);
    private static final int SCALE = 5;
    private static final int BORDER = 2;
    private static final int LIGHT_COLOR = 0xFFFFFF; // White
    private static final int DARK_COLOR = 0x000000;  // Black

    @Override
    public BufferedImage generateImage(QRCodeData qrCodeData) throws QRCodeGenerationException {
        Objects.requireNonNull(qrCodeData, "QR code data cannot be null");
        
        try {
            LOGGER.debug("Generating QR code image for URI: {}", qrCodeData.getTargetUri());
            
            String uriString = qrCodeData.getTargetUri().toString();
            QrCode qr = QrCode.encodeBinary(uriString.getBytes(), QrCode.Ecc.MEDIUM);
            
            return toImage(qr, SCALE, BORDER, LIGHT_COLOR, DARK_COLOR);
            
        } catch (Exception e) {
            LOGGER.error("Failed to generate QR code image", e);
            throw QRCodeGenerationException.generationFailed(e);
        }
    }

    @Override
    public String generateBase64(QRCodeData qrCodeData) throws QRCodeGenerationException {
        try {
            BufferedImage image = generateImage(qrCodeData);
            byte[] bytes = imageToBytes(image, qrCodeData.getFormat());
            return Base64.getEncoder().encodeToString(bytes);
            
        } catch (QRCodeGenerationException e) {
            throw e; // Re-throw QR generation exceptions
        } catch (Exception e) {
            LOGGER.error("Failed to generate QR code as Base64", e);
            throw QRCodeGenerationException.generationFailed(e);
        }
    }

    @Override
    public byte[] generateBytes(QRCodeData qrCodeData) throws QRCodeGenerationException {
        try {
            BufferedImage image = generateImage(qrCodeData);
            return imageToBytes(image, qrCodeData.getFormat());
            
        } catch (QRCodeGenerationException e) {
            throw e; // Re-throw QR generation exceptions
        } catch (Exception e) {
            LOGGER.error("Failed to generate QR code as bytes", e);
            throw QRCodeGenerationException.generationFailed(e);
        }
    }
    
    private BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
        Objects.requireNonNull(qr, "QR code cannot be null");
        
        if (scale <= 0 || border < 0) {
            throw new IllegalArgumentException("Scale must be positive and border must be non-negative");
        }
        if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale) {
            throw new IllegalArgumentException("Scale or border too large");
        }

        int imageSize = (qr.size + border * 2) * scale;
        BufferedImage result = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                boolean color = qr.getModule(x / scale - border, y / scale - border);
                result.setRGB(x, y, color ? darkColor : lightColor);
            }
        }
        
        return result;
    }
    
    private byte[] imageToBytes(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format.toLowerCase(), baos);
            return baos.toByteArray();
        }
    }
}
