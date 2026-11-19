package com.example.loginandregister.util;

import android.util.Base64;

import com.example.loginandregister.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密码工具：永不明文存储。
 * 入库格式为 SHA-256(salt + password) 的十六进制串，salt 每个用户独立随机。
 */
public final class PasswordUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtils() {
    }

    /** 生成 16 字节随机盐（Base64 编码）。 */
    public static String newSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    /** 计算哈希。 */
    public static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] result = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 在所有 Android 版本上都存在，理论上不会走到这里
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    /** 校验密码是否匹配。 */
    public static boolean verify(String password, User user) {
        if (password == null || user == null || user.getSalt() == null) {
            return false;
        }
        return hash(password, user.getSalt()).equals(user.getPasswordHash());
    }
}
