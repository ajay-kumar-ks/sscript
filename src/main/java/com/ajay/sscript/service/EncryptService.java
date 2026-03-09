package com.ajay.sscript.service;

import com.ajay.sscript.util.CryptoUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

        LocalDateTime now = LocalDateTime.now();

        String dd = reverse(String.format("%02d", now.getDayOfMonth()));
        String MM = reverse(String.format("%02d", now.getMonthValue()));
        String yyyy = reverse(String.valueOf(now.getYear()));
        String HH = reverse(String.format("%02d", now.getHour()));
        String mm = reverse(String.format("%02d", now.getMinute()));
        String ss = reverse(String.format("%02d", now.getSecond()));

        String yy1 = yyyy.substring(0, 2);
        String yy2 = yyyy.substring(2, 4);

        // 🔥 Direct mixed order (no left/right)
        String[] blocks = { dd, HH, MM, mm, yy1, ss, yy2 };
        // String[] blocks = { "71", "90", "21", "45", "02", "03", "42" };
        // String[] blocks = { "11", "22", "33", "44", "55", "66", "77" };

        int[] keys = convertBlocksToKeys(blocks);

        String encrypted = encryptWithTimeKey(text, keys);
        // int sum = 0;
        // for (int i = 0; i < blocks.length; i++) {
        //     sum += Integer.parseInt(blocks[i]);
        // }
        // sum = sum%10;
        // fullTimeEncrypt(encrypted,sum);

        return attachBlocksCenterExpand(encrypted, blocks);
    }
    // private String fullTimeEncrypt(String encrypted,int sum){

    // }

    private int[] convertBlocksToKeys(String[] blocks) {

        int[] keys = new int[blocks.length];

        for (int i = 0; i < blocks.length; i++) {

            int sum = 0;
            for (char c : blocks[i].toCharArray()) {
                sum += Character.getNumericValue(c);
            }

            keys[i] = sum;
        }

        return keys;
    }

    private String encryptWithTimeKey(String text, int[] keys) {

        StringBuilder result = new StringBuilder();

        int previous = text.length();
        int keyIndex = 0;

        for (int i = 0; i < text.length(); i++) {

            int currentIndex =
                    CryptoUtil.indexOfMixed(text.charAt(i));

            if (currentIndex == -1) {
                throw new IllegalArgumentException(
                        "Invalid character in time layer: "
                                + text.charAt(i));
            }

            int dynamic = keys[keyIndex] + previous;

            int newIndex;

            // alternate direction encryption
            if (i % 2 == 0) {
                newIndex = currentIndex - dynamic;
            } else {
                newIndex = currentIndex + dynamic;
            }

            int setLength = CryptoUtil.CHAR_SET_MIXED.length;

            newIndex =
                    (newIndex % setLength + setLength) % setLength;

            result.append(
                    CryptoUtil.CHAR_SET_MIXED[newIndex]
            );

            previous = newIndex;
            keyIndex = (keyIndex + 1) % keys.length;
        }

        return result.toString();
    }
    private String attachBlocksCenterExpand(String text, String[] blocks) {

        int n = text.length();
        int N = blocks.length;
        int total = n + N;

        StringBuilder result = new StringBuilder();

        // ================= >= 9 =================
        if (n >= 9) {

            // ---------- ODD ----------
            if (n % 2 == 1) {

                int mid = n / 2;

                String first =
                        blocks[2] + text.charAt(0) + blocks[3];

                String center =
                        blocks[0] + text.charAt(mid) + blocks[1];

                String last =
                        blocks[4] + text.charAt(n - 1) + blocks[5];

                String remain = blocks[6];

                result.append(first);
                result.append(text.substring(1, mid));
                result.append(center);
                result.append(text.substring(mid + 1, n - 1));
                result.append(last);
                result.append(remain);

                return result.toString();
            }

            // ---------- EVEN ----------
            else {

                int mid = n / 2;

                char e = text.charAt(mid - 1);
                char f = text.charAt(mid);

                String first =
                        blocks[4] + text.charAt(0) + blocks[3];

                String center =
                        blocks[1] + e + blocks[0] + f + blocks[2];

                String last =
                        blocks[5] + text.charAt(n - 1);

                String remain = blocks[6];

                result.append(first);
                result.append(text.substring(1, mid - 1));
                result.append(center);
                result.append(text.substring(mid + 1, n - 1));
                result.append(last);
                result.append(remain);

                return result.toString();
            }
        }

        // ================= < 9 =================
        else {

            int start = (N + 2 - n) / 2;

            java.util.Set<Integer> letterPositions =
                    new java.util.HashSet<>();

            for (int i = start; i < start + 2 * n; i += 2)
                letterPositions.add(i);

            java.util.List<Integer> numSeq =
                    new java.util.ArrayList<>();

            if (n % 2 == 1) {
                for (int i = N; i >= 1; i -= 2)
                    numSeq.add(i);
                for (int i = 2; i < N; i += 2)
                    numSeq.add(i);
            } else {
                for (int i = N - 1; i >= 1; i -= 2)
                    numSeq.add(i);
                for (int i = 1; i <= N; i += 2)
                    numSeq.add(i);
            }

            int numIdx = 0;
            int letIdx = 0;

            for (int i = 0; i < total; i++) {

                if (letterPositions.contains(i)) {
                    result.append(text.charAt(letIdx++));
                } else {
                    result.append(blocks[numSeq.get(numIdx++) - 1]);
                }
            }
        }

        return result.toString();
    }
    private String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
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
