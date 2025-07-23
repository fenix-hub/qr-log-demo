package com.example.domain.qr;

import java.awt.image.BufferedImage;

/**
 * Port (interface) for QR code generation
 * Following hexagonal architecture - this will be implemented by an adapter
 */
public interface QRCodeGenerator {
    
    /**
     * Generates a QR code image from the provided data
     * @param qrCodeData the data to encode in the QR code
     * @return BufferedImage containing the QR code
     * @throws QRCodeGenerationException if generation fails
     */
    BufferedImage generateImage(QRCodeData qrCodeData) throws QRCodeGenerationException;
    
    /**
     * Generates a QR code as a Base64-encoded string
     * @param qrCodeData the data to encode in the QR code
     * @return Base64-encoded string of the QR code image
     * @throws QRCodeGenerationException if generation fails
     */
    String generateBase64(QRCodeData qrCodeData) throws QRCodeGenerationException;
    
    /**
     * Generates a QR code as byte array
     * @param qrCodeData the data to encode in the QR code
     * @return byte array of the QR code image
     * @throws QRCodeGenerationException if generation fails
     */
    byte[] generateBytes(QRCodeData qrCodeData) throws QRCodeGenerationException;
}
