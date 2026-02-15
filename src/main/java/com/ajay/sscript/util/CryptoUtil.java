package com.ajay.sscript.util;

public class CryptoUtil {

    public static final char[] CHAR_SET = {
        'a','b','c','d','e','f','g','h','i','j','k','l','m',
        'n','o','p','q','r','s','t','u','v','w','x','y','z',
        '0','1','2','3','4','5','6','7','8','9',
        '#','@','!','$','%','*','&','+','=','-','?','/','>','.', '^','<'
    };

    public static int indexOf(char c) {
        for (int i = 0; i < CHAR_SET.length; i++) {
            if (CHAR_SET[i] == c) {
                return i;
            }
        }
        return -1;
    }

    public static int generateDynamicKey(int length, int index, int previousValue) {

        int dynamicKey = (length * 3 + index * 7 + previousValue) % CHAR_SET.length;

        if (dynamicKey == 0) {
            dynamicKey = index + 1;
        }

        return dynamicKey;
    }
}
