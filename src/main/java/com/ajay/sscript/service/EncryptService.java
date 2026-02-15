package com.ajay.sscript.service;

import com.ajay.sscript.util.CryptoUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EncryptService {

    public String encrypt(String input) {

        if (input == null || input.isEmpty()) {
            return "";
        }

        validateInput(input);

        // Step 1: Normal encryption
        String baseEncrypted = basicEncrypt(input);

        // Step 2: Apply time-based layer
        return applyTimeLayer(baseEncrypted);
    }

    // 🔹 Basic Encryption (your existing logic)
    private String basicEncrypt(String input) {

        String processed = input.replace(" ", "^");
        int length = processed.length();

        StringBuilder encrypted = new StringBuilder();
        int previousValue = length;

        for (int i = 0; i < length; i++) {

            char current = processed.charAt(i);
            int currentIndex = CryptoUtil.indexOf(current);

            if (currentIndex == -1) {
                throw new IllegalArgumentException("Invalid character: " + current);
            }

            int dynamicKey = CryptoUtil.generateDynamicKey(length, i, previousValue);

            int newIndex;

            if (i % 2 == 0) {
                newIndex = (currentIndex + dynamicKey) % CryptoUtil.CHAR_SET.length;
            } else {
                newIndex = currentIndex - dynamicKey;
                if (newIndex < 0) {
                    newIndex += CryptoUtil.CHAR_SET.length;
                }
            }

            encrypted.append(CryptoUtil.CHAR_SET[newIndex]);
            previousValue = newIndex;
        }

        return encrypted.toString();
    }

    // 🔹 Time-Based Layer (SECOND ENCRYPTION)
    private String applyTimeLayer(String text) {

        String timeSeed = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        int[] parts = extractTimeParts(timeSeed);

        int length = text.length();
        StringBuilder result = new StringBuilder();
        int previousValue = length;

        for (int i = 0; i < length; i++) {

            char current = text.charAt(i);
            int currentIndex = CryptoUtil.indexOf(current);

            int dynamicKey = generateTimeDynamicKey(length, i, previousValue, parts);

            int newIndex;

            if (i % 2 == 0) {
                newIndex = (currentIndex + dynamicKey) % CryptoUtil.CHAR_SET.length;
            } else {
                newIndex = currentIndex - dynamicKey;
                if (newIndex < 0) {
                    newIndex += CryptoUtil.CHAR_SET.length;
                }
            }

            result.append(CryptoUtil.CHAR_SET[newIndex]);
            previousValue = newIndex;
        }

        // Attach timeSeed so we can decrypt later
        return result.toString() + "|" + timeSeed;
    }

    private int[] extractTimeParts(String timeSeed) {

        String yyyy = timeSeed.substring(0, 4);
        String MM   = timeSeed.substring(4, 6);
        String dd   = timeSeed.substring(6, 8);
        String HH   = timeSeed.substring(8, 10);
        String mm   = timeSeed.substring(10, 12);
        String ss   = timeSeed.substring(12, 14);

        String yy1 = yyyy.substring(0, 2);
        String yy2 = yyyy.substring(2, 4);

        yy1 = new StringBuilder(yy1).reverse().toString();
        yy2 = new StringBuilder(yy2).reverse().toString();
        MM  = new StringBuilder(MM).reverse().toString();
        dd  = new StringBuilder(dd).reverse().toString();
        HH  = new StringBuilder(HH).reverse().toString();
        mm  = new StringBuilder(mm).reverse().toString();
        ss  = new StringBuilder(ss).reverse().toString();

        return new int[] {
                Integer.parseInt(yy1),
                Integer.parseInt(yy2),
                Integer.parseInt(MM),
                Integer.parseInt(dd),
                Integer.parseInt(HH),
                Integer.parseInt(mm),
                Integer.parseInt(ss)
        };
    }

    private int generateTimeDynamicKey(int length, int index, int previousValue, int[] parts) {

        int yySum = parts[0] + parts[1];
        int MM    = parts[2];
        int dd    = parts[3];
        int HH    = parts[4];
        int mm    = parts[5];
        int ss    = parts[6];

        int dynamicKey = (
                (length * (yySum + MM + HH + ss))
              + (index * (yySum + dd + mm))
              + previousValue
        ) % CryptoUtil.CHAR_SET.length;

        if (dynamicKey < 0)
            dynamicKey += CryptoUtil.CHAR_SET.length;

        return dynamicKey;
    }

    private void validateInput(String input) {
        for (char c : input.toCharArray()) {
            if (c == ' ') continue;

            if (!Character.isLowerCase(c) && !Character.isDigit(c)) {
                throw new IllegalArgumentException("Invalid character: " + c);
            }
        }
    }
}
