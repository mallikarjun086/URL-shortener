package com.urlshortener.algorithm;

/**
 * Base62 Encoder / Decoder.
 * Converts 64-bit positive long integer IDs to/from compact 62-character strings [0-9][a-z][A-Z].
 */
public class Base62Encoder {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARS.length();

    public static String encode(long number) {
        if (number == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        long num = Math.abs(number);

        while (num > 0) {
            int remainder = (int) (num % BASE);
            sb.append(BASE62_CHARS.charAt(remainder));
            num /= BASE;
        }

        return sb.reverse().toString();
    }

    public static long decode(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("String to decode cannot be null or empty");
        }

        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int digit = BASE62_CHARS.indexOf(c);
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            result = result * BASE + digit;
        }

        return result;
    }
}
