package com.example.domain.qr;

/**
 * Domain exception for QR code generation failures
 */
public class QRCodeGenerationException extends Exception {
    
    public QRCodeGenerationException(String message) {
        super(message);
    }
    
    public QRCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static QRCodeGenerationException invalidData(String details) {
        return new QRCodeGenerationException("Invalid QR code data: " + details);
    }
    
    public static QRCodeGenerationException generationFailed(Throwable cause) {
        return new QRCodeGenerationException("QR code generation failed", cause);
    }
}
