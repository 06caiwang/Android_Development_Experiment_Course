package com.example.loginandregister.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 登录态管理。
 * 勾选「记住登录状态」时才写入持久化存储，否则仅保留在内存中，
 * 进程结束后需要重新登录。
 */
public final class SessionManager {

    private static final String PREF = "app_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_PERSIST = "persist";

    private static long memoryUid = -1L;

    private SessionManager() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static void login(Context context, long uid, boolean persist) {
        memoryUid = uid;
        prefs(context).edit()
                .putLong(KEY_UID, uid)
                .putBoolean(KEY_PERSIST, persist)
                .apply();
    }

    public static boolean isLoggedIn(Context context) {
        return getUid(context) != -1L;
    }

    public static long getUid(Context context) {
        if (memoryUid != -1L) {
            return memoryUid;
        }
        SharedPreferences p = prefs(context);
        if (!p.getBoolean(KEY_PERSIST, false)) {
            return -1L;
        }
        return p.getLong(KEY_UID, -1L);
    }

    public static void logout(Context context) {
        memoryUid = -1L;
        prefs(context).edit().clear().apply();
    }
}
