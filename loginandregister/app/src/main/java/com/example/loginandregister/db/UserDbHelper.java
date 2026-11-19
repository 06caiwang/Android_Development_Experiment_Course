package com.example.loginandregister.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.loginandregister.model.User;

/**
 * 本地用户库。三种登录方式共用 users 表，account 唯一。
 */
public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "account.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE = "users";
    private static final String C_ID = "_id";
    private static final String C_ACCOUNT = "account";
    private static final String C_TYPE = "account_type";
    private static final String C_NICKNAME = "nickname";
    private static final String C_HASH = "password_hash";
    private static final String C_SALT = "salt";
    private static final String C_CREATED = "created_at";
    private static final String C_LAST_LOGIN = "last_login_at";

    private static volatile UserDbHelper instance;

    private UserDbHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static UserDbHelper get(Context context) {
        if (instance == null) {
            synchronized (UserDbHelper.class) {
                if (instance == null) {
                    instance = new UserDbHelper(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_ACCOUNT + " TEXT NOT NULL UNIQUE, "
                + C_TYPE + " TEXT NOT NULL, "
                + C_NICKNAME + " TEXT, "
                + C_HASH + " TEXT, "
                + C_SALT + " TEXT, "
                + C_CREATED + " INTEGER NOT NULL, "
                + C_LAST_LOGIN + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    /** 注册新用户，返回自增 id，失败返回 -1。 */
    public long insert(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        long now = System.currentTimeMillis();
        values.put(C_ACCOUNT, user.getAccount());
        values.put(C_TYPE, user.getAccountType());
        values.put(C_NICKNAME, user.getNickname());
        values.put(C_HASH, user.getPasswordHash());
        values.put(C_SALT, user.getSalt());
        values.put(C_CREATED, now);
        values.put(C_LAST_LOGIN, now);
        long id = db.insert(TABLE, null, values);
        if (id > 0) {
            user.setId(id);
            user.setCreatedAt(now);
            user.setLastLoginAt(now);
        }
        return id;
    }

    public boolean exists(String account) {
        return findByAccount(account) != null;
    }

    public User findByAccount(String account) {
        if (account == null) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, C_ACCOUNT + "=?",
                new String[]{account.trim()}, null, null, null, "1");
        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = read(cursor);
            }
            cursor.close();
        }
        return user;
    }

    public User findById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, C_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, "1");
        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = read(cursor);
            }
            cursor.close();
        }
        return user;
    }

    public void touchLastLogin(long id) {
        ContentValues values = new ContentValues();
        values.put(C_LAST_LOGIN, System.currentTimeMillis());
        getWritableDatabase().update(TABLE, values, C_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void updatePassword(long id, String hash, String salt) {
        ContentValues values = new ContentValues();
        values.put(C_HASH, hash);
        values.put(C_SALT, salt);
        getWritableDatabase().update(TABLE, values, C_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int count() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE, null);
        int total = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
            cursor.close();
        }
        return total;
    }

    private User read(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(C_ID)));
        user.setAccount(cursor.getString(cursor.getColumnIndexOrThrow(C_ACCOUNT)));
        user.setAccountType(cursor.getString(cursor.getColumnIndexOrThrow(C_TYPE)));
        user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow(C_NICKNAME)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(C_HASH)));
        user.setSalt(cursor.getString(cursor.getColumnIndexOrThrow(C_SALT)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(C_CREATED)));
        user.setLastLoginAt(cursor.getLong(cursor.getColumnIndexOrThrow(C_LAST_LOGIN)));
        return user;
    }
}
