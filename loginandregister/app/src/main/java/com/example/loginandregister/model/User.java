package com.example.loginandregister.model;

/**
 * 用户实体。三种登录方式共用一张表，用 accountType 区分。
 */
public class User {

    public static final String TYPE_PHONE = "phone";
    public static final String TYPE_EMAIL = "email";
    public static final String TYPE_QQ = "qq";

    private long id;
    private String account;
    private String accountType;
    private String nickname;
    private String passwordHash;
    private String salt;
    private long createdAt;
    private long lastLoginAt;

    public User() {
    }

    public User(String account, String accountType, String nickname,
                String passwordHash, String salt) {
        this.account = account;
        this.accountType = accountType;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /** 头像上显示的首字母 / 首字符。 */
    public String getInitial() {
        if (nickname != null && !nickname.isEmpty()) {
            return nickname.substring(0, 1).toUpperCase();
        }
        if (account != null && !account.isEmpty()) {
            return account.substring(0, 1).toUpperCase();
        }
        return "?";
    }

    /** 展示用昵称。 */
    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        return account == null ? "" : account;
    }
}
