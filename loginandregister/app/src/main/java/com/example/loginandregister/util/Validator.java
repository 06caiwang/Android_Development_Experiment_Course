package com.example.loginandregister.util;

import java.util.regex.Pattern;

/**
 * 表单校验规则：登录、注册两侧共用同一套规则。
 */
public final class Validator {

    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern HAS_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");

    private Validator() {
    }

    public static boolean isPhone(String value) {
        return value != null && PHONE.matcher(value.trim()).matches();
    }

    public static boolean isEmail(String value) {
        return value != null && EMAIL.matcher(value.trim()).matches();
    }

    /**
     * 密码规则：6-20 位，必须同时包含字母和数字。
     */
    public static boolean isPassword(String value) {
        if (value == null) {
            return false;
        }
        int len = value.length();
        return len >= 6 && len <= 20
                && HAS_LETTER.matcher(value).matches()
                && HAS_DIGIT.matcher(value).matches();
    }

    /**
     * 强度：0 弱、1 中、2 强。
     */
    public static int strength(String value) {
        if (value == null || value.isEmpty()) {
            return -1;
        }
        int score = 0;
        if (value.length() >= 8) {
            score++;
        }
        if (value.length() >= 12) {
            score++;
        }
        boolean letter = HAS_LETTER.matcher(value).matches();
        boolean digit = HAS_DIGIT.matcher(value).matches();
        boolean symbol = value.matches(".*[^A-Za-z0-9].*");
        if (letter && digit) {
            score++;
        }
        if (symbol) {
            score++;
        }
        if (score <= 1) {
            return 0;
        }
        if (score <= 2) {
            return 1;
        }
        return 2;
    }

    /** 6 位纯数字验证码。 */
    public static boolean isCode(String value) {
        return value != null && value.matches("^\\d{6}$");
    }
}
