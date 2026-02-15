package com.ajay.sscript.service;

import com.ajay.sscript.util.CryptoUtil;
import org.springframework.stereotype.Service;

@Service
public class DecryptService {

    public String decrypt(String encryptedText) {

        if (encryptedText == null || encryptedText.isEmpty()) {
            return "";
        }
        StringBuilder decrypted = new StringBuilder();

        int length = encryptedText.length();
        int previousValue = length;

        for (int i = 0; i < length; i++) {

            char current = encryptedText.charAt(i);
            int encryptedIndex = CryptoUtil.indexOf(current);

            if (encryptedIndex == -1) {
                throw new IllegalArgumentException("Invalid character: " + current);
            }

            int dynamicKey = CryptoUtil.generateDynamicKey(length, i, previousValue);

            int originalIndex;

            if (i % 2 == 0) {
                originalIndex = encryptedIndex - dynamicKey;
                if (originalIndex < 0) {
                    originalIndex += CryptoUtil.CHAR_SET.length;
                }
            } else {
                originalIndex = (encryptedIndex + dynamicKey) % CryptoUtil.CHAR_SET.length;
            }

            decrypted.append(CryptoUtil.CHAR_SET[originalIndex]);

            previousValue = encryptedIndex;
        }

        return decrypted.toString().replace("^", " ");
    }
}
