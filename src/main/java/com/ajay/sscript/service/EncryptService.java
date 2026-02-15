package com.ajay.sscript.service;

import org.springframework.stereotype.Service;

@Service
public class EncryptService {

    private static final int BLOCK_SIZE = 13;

    // 🔐 Character set (52 chars)
    private static final char[] CHAR_SET = {
        // a-z
        'a','b','c','d','e','f','g','h','i','j','k','l','m',
        'n','o','p','q','r','s','t','u','v','w','x','y','z',

        // 0-9
        '0','1','2','3','4','5','6','7','8','9',

        // symbols
        '#','@','!','$','%','*','&','+','=','-','?','/','>','.', '^','<'
    };

    // 🔐 PUBLIC METHOD
    public String encrypt(String input) {

        validateInput(input);

        // Replace space with ^
        String processed = input.replace(" ", "^");

        StringBuilder finalEncrypted = new StringBuilder();

        int logicalIndex = 0; // counts only real characters
        int blockIndex = 0;

        for (int start = 0; start < processed.length(); start += BLOCK_SIZE) {

            int end = Math.min(start + BLOCK_SIZE, processed.length());
            String block = processed.substring(start, end);

            int rawKey = BLOCK_SIZE + blockIndex;
            int key = normalizeKey(rawKey);

            finalEncrypted.append(
                encryptBlock(block, key, logicalIndex)
            );

            // update logicalIndex
            for (char c : block.toCharArray()) {
                // if (c != '^') logicalIndex++;
                logicalIndex++;
            }

            blockIndex++;
        }

        return finalEncrypted.toString();
    }

    // 🔐 Encrypt a single block
    private String encryptBlock(String block, int key, int logicalStartIndex) {
        StringBuilder encrypted = new StringBuilder();
        int logicalIndex = logicalStartIndex;

        for (char current : block.toCharArray()) {

            int currentIndex = indexOf(current);
            if (currentIndex == -1) {
                throw new IllegalArgumentException(
                    "Character not found in set: " + current
                );
            }

            int newIndex;

            if (logicalIndex % 2 == 0) {
                newIndex = (currentIndex + key) % CHAR_SET.length;
            } else {
                newIndex = currentIndex - key;
                if (newIndex < 0) newIndex += CHAR_SET.length;
            }
            encrypted.append(CHAR_SET[newIndex]);

            // if (current != '^') logicalIndex++;
            logicalIndex++;
        }

        return encrypted.toString();
    }

    // 🔐 Normalize key to avoid 0 or 52
    private int normalizeKey(int key) {
        key = key % CHAR_SET.length;
        return (key == 0) ? 1 : key;
    }

    // 🔎 Find index in CHAR_SET
    private int indexOf(char c) {
        for (int i = 0; i < CHAR_SET.length; i++) {
            if (CHAR_SET[i] == c) return i;
        }
        return -1;
    }

    // ✅ Input validation
    private void validateInput(String input) {
        for (char c : input.toCharArray()) {
            if (c == ' ') continue;
            if (!Character.isLowerCase(c) && !Character.isDigit(c)) {
                throw new IllegalArgumentException(
                    "Invalid character: " + c
                );
            }
        }
    }
}
