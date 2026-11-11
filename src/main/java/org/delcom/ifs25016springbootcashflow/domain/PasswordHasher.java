package org.delcom.ifs25016springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash dan verifikasi password dengan SHA-256 (hex), sesuai ketentuan praktikum. */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 tidak tersedia", ex);
        }
    }

    public static boolean matches(String raw, String hash) {
        return raw != null && hash != null && MessageDigest.isEqual(
                hash(raw).getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
    }
}
