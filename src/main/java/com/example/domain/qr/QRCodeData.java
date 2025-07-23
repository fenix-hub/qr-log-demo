package com.example.domain.qr;

import java.net.URI;

/**
 * Value object representing QR code data
 * Encapsulates QR code generation parameters and validation
 */
public final class QRCodeData {
    private final URI targetUri;
    private final int size;
    private final String format;

    private QRCodeData(URI targetUri, int size, String format) {
        this.targetUri = validateUri(targetUri);
        this.size = validateSize(size);
        this.format = validateFormat(format);
    }

    public static QRCodeData of(URI targetUri) {
        return new QRCodeData(targetUri, 200, "PNG");
    }

    public static QRCodeData of(URI targetUri, int size, String format) {
        return new QRCodeData(targetUri, size, format);
    }

    private URI validateUri(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Target URI cannot be null");
        }
        if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Only HTTP and HTTPS URIs are allowed");
        }
        return uri;
    }

    private int validateSize(int size) {
        if (size < 50 || size > 1000) {
            throw new IllegalArgumentException("QR code size must be between 50 and 1000 pixels");
        }
        return size;
    }

    private String validateFormat(String format) {
        if (format == null || (!format.equalsIgnoreCase("PNG") && !format.equalsIgnoreCase("JPEG"))) {
            throw new IllegalArgumentException("Only PNG and JPEG formats are supported");
        }
        return format.toUpperCase();
    }

    public URI getTargetUri() { return targetUri; }
    public int getSize() { return size; }
    public String getFormat() { return format; }

    @Override
    public String toString() {
        return "QRCodeData{" +
                "targetUri=" + targetUri +
                ", size=" + size +
                ", format='" + format + '\'' +
                '}';
    }
}
