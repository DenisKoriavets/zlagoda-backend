package ua.edu.ukma.zlagodabackend.util;

import java.util.concurrent.ThreadLocalRandom;

public final class CheckNumberGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 10;

    private CheckNumberGenerator() {
        throw new UnsupportedOperationException();
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARACTERS.charAt(ThreadLocalRandom.current().nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
