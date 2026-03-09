package com.ajay.sscript.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.ajay.sscript.util.CryptoUtil;

@Service
public class DecryptService {

        public String decrypt(String encryptedText) {

                if (encryptedText == null || encryptedText.isEmpty()) {
                        return "";
                }

                // STEP 1 → remove time blocks
                System.out.println("hiii " + encryptedText);
                ExtractResult extracted = removeBlocksCenterExpand(encryptedText);
                

                // STEP 2 → reverse time encryption
                String timeDecrypted = decryptWithTimeKey(
                                extracted.text,
                                convertBlocksToKeys(extracted.blocks));

                // STEP 3 → reverse base encryption
                return basicDecrypt(timeDecrypted);
        }

        // =====================================================
        // BASIC DECRYPT (reverse of basicEncrypt)
        // =====================================================

        private String basicDecrypt(String encryptedText) {

                StringBuilder decrypted = new StringBuilder();

                int length = encryptedText.length();
                int previousValue = length;

                for (int i = 0; i < length; i++) {

                        char current = encryptedText.charAt(i);
                        int encryptedIndex = CryptoUtil.indexOf(current);

                        int dynamicKey = CryptoUtil.generateDynamicKey(
                                        length, i, previousValue);

                        int originalIndex;

                        if (i % 2 == 0) {
                                originalIndex = encryptedIndex - dynamicKey;
                        } else {
                                originalIndex = encryptedIndex + dynamicKey;
                        }

                        int setLen = CryptoUtil.CHAR_SET.length;

                        originalIndex = (originalIndex % setLen + setLen)
                                        % setLen;

                        decrypted.append(
                                        CryptoUtil.CHAR_SET[originalIndex]);

                        previousValue = encryptedIndex;
                }

                return decrypted.toString().replace("^", " ");
        }

        // =====================================================
        // TIME LAYER DECRYPT (reverse encryptWithTimeKey)
        // =====================================================

        private String decryptWithTimeKey(
                        String text,
                        int[] keys) {

                StringBuilder result = new StringBuilder();

                int previous = text.length();
                int keyIndex = 0;

                for (int i = 0; i < text.length(); i++) {

                        int encryptedIndex = CryptoUtil.indexOfMixed(
                                        text.charAt(i));

                        int dynamic = keys[keyIndex] + previous;

                        int originalIndex;

                        // REVERSE OPERATION
                        if (i % 2 == 0) {
                                originalIndex = encryptedIndex + dynamic;
                        } else {
                                originalIndex = encryptedIndex - dynamic;
                        }

                        int setLength = CryptoUtil.CHAR_SET_MIXED.length;

                        originalIndex = (originalIndex % setLength
                                        + setLength)
                                        % setLength;

                        result.append(
                                        CryptoUtil.CHAR_SET_MIXED[originalIndex]);

                        previous = encryptedIndex;
                        keyIndex = (keyIndex + 1) % keys.length;
                }

                return result.toString();
        }

        // =====================================================
        // REMOVE ATTACHED BLOCKS
        // =====================================================

        private ExtractResult removeBlocksCenterExpand(String encoded) {

                int n = encoded.length() - 14;
                int N = 7;
                int blockSize = 2;

                StringBuilder core = new StringBuilder();
                String[] blocks = new String[N];

                // ================= >= 9 =================
                if (n >= 9) {

                        // ---------- ODD ----------
                        if (n % 2 == 1) {

                                int mid = n / 2;

                                int idx = 0;

                                // first = blocks[2] + text[0] + blocks[3]
                                blocks[2] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                char firstChar = encoded.charAt(idx++);
                                core.append(firstChar);

                                blocks[3] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                // text.substring(1, mid)
                                String left = encoded.substring(idx, idx + (mid - 1));
                                core.append(left);
                                idx += (mid - 1);

                                // center = blocks[0] + text[mid] + blocks[1]
                                blocks[0] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                char midChar = encoded.charAt(idx++);
                                core.append(midChar);

                                blocks[1] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                // text.substring(mid+1 , n-1)
                                int rightLen = (n - 1) - (mid + 1);
                                String right = encoded.substring(idx, idx + rightLen);
                                core.append(right);
                                idx += rightLen;

                                // last = blocks[4] + text[n-1] + blocks[5]
                                blocks[4] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                char lastChar = encoded.charAt(idx++);
                                core.append(lastChar);

                                blocks[5] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                // remain
                                blocks[6] = encoded.substring(idx, idx + blockSize);
                        }

                        // ---------- EVEN ----------
                        else {

                                int mid = n / 2;

                                int idx = 0;

                                // first = blocks[4] + text[0] + blocks[3]
                                blocks[4] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                char firstChar = encoded.charAt(idx++);
                                core.append(firstChar);

                                blocks[3] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                // text.substring(1 , mid-1)
                                String left = encoded.substring(idx, idx + (mid - 2));
                                core.append(left);
                                idx += (mid - 2);

                                // center = blocks[1] + e + blocks[0] + f + blocks[2]
                                blocks[1] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                char e = encoded.charAt(idx++);
                                core.append(e);

                                blocks[0] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                char f = encoded.charAt(idx++);
                                core.append(f);

                                blocks[2] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                // text.substring(mid+1 , n-1)
                                int rightLen = (n - 1) - (mid + 1);
                                String right = encoded.substring(idx, idx + rightLen);
                                core.append(right);
                                idx += rightLen;

                                // last = blocks[5] + text[n-1]
                                blocks[5] = encoded.substring(idx, idx + blockSize);
                                idx += blockSize;

                                char lastChar = encoded.charAt(idx++);
                                core.append(lastChar);

                                // remain
                                blocks[6] = encoded.substring(idx, idx + blockSize);
                        }
                } else {

                        int start = (N + 2 - n) / 2;

                        Set<Integer> letterPositions = new HashSet<>();
                        for (int i = start; i < start + 2 * n; i += 2) {
                                letterPositions.add(i);
                        }

                        // recreate numSeq
                        List<Integer> numSeq = new ArrayList<>();

                        if (n % 2 == 1) {
                                for (int i = N; i > 0; i -= 2) {
                                        numSeq.add(i);
                                }
                                for (int i = 2; i < N; i += 2) {
                                        numSeq.add(i);
                                }
                        } else {
                                for (int i = N - 1; i > 0; i -= 2) {
                                        numSeq.add(i);
                                }
                                for (int i = 1; i <= N; i += 2) {
                                        numSeq.add(i);
                                }
                        }

                        int pos = 0;
                        int numIdx = 0;

                        for (int i = 0; i < n + N; i++) {

                                if (letterPositions.contains(i)) {

                                        core.append(encoded.charAt(pos));
                                        pos += 1;

                                } else {

                                        int blockIndex = numSeq.get(numIdx) - 1;
                                        blocks[blockIndex] = encoded.substring(pos, pos + blockSize);
                                        pos += blockSize;
                                        numIdx += 1;

                                }
                        }
                }

                return new ExtractResult(core.toString(), blocks);
        }
        // =====================================================
        // BLOCK → KEY
        // =====================================================

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

        // =====================================================

        private static class ExtractResult {
                String text;
                String[] blocks;

                ExtractResult(String t, String[] b) {
                        text = t;
                        blocks = b;
                }
        }
}