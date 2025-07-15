package com.teamproject.sellog.domain.auth.model.jwt;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

    private static final int ITERATIONS = 10000; // 반복 횟수
    private static final int KEY_LENGTH = 256; // 키 길이 (비트)
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256"; // 해싱 알고리즘

    // Salt 생성
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16바이트 salt 생성
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // 비밀번호 해싱
    public static String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing password", e);
        }
    }

    // 비밀번호 검증
    public static boolean verifyPassword(String rawPassword, String storedHash, String storedSalt) {
        String hashedRawPassword = hashPassword(rawPassword, storedSalt);
        return hashedRawPassword.equals(storedHash);
    }
}
