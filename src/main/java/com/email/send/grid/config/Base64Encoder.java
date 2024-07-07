package com.email.send.grid.config;

import java.util.Base64;

public class Base64Encoder {
    public static String encode(String plainText) {
        return Base64.getEncoder().encodeToString(plainText.getBytes());
    }

    public static String decode(String encodedText) {
        return new String(Base64.getDecoder().decode(encodedText));
    }

    public static void main(String[] args) {
        String htmlBody = "<h1>This is a test email.</h1>";
        String encodedHtmlBody = encode(htmlBody);
        System.out.println("Encoded HTML Body: " + encodedHtmlBody);

        // Decoding example
        String decodedHtmlBody = decode(encodedHtmlBody);
        System.out.println("Decoded HTML Body: " + decodedHtmlBody);
    }
}
