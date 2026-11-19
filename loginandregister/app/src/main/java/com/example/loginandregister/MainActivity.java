package com.example.loginandregister;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loginandregister.db.UserDbHelper;
import com.example.loginandregister.model.User;
import com.example.loginandregister.util.AnimUtils;
import com.example.loginandregister.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 主页：展示当前登录用户信息，并负责登录态路由。
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_UID = "extra_uid";

    private UserDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = UserDbHelper.get(this);

        long uid = getIntent().getLongExtra(EXTRA_UID, -1L);
        if (uid == -1L) {
            uid = SessionManager.getUid(this);
        }
        User user = uid > 0 ? db.findById(uid) : null;

        if (user == null) {
            SessionManager.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        bind(user);
        playEnterAnimation();
    }

    private void bind(User user) {
        TextView tvAvatar = findViewById(R.id.tv_avatar);
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvBadge = findViewById(R.id.tv_badge);
        TextView tvAccount = findViewById(R.id.tv_account);
        TextView tvType = findViewById(R.id.tv_type);
        TextView tvCreated = findViewById(R.id.tv_created);
        TextView tvLastLogin = findViewById(R.id.tv_last_login);

        tvAvatar.setText(user.getInitial());
        tvName.setText(user.getDisplayName());
        tvBadge.setText("已登录");
        tvAccount.setText(maskAccount(user));
        tvType.setText(typeLabel(user.getAccountType()));
        tvCreated.setText(formatTime(user.getCreatedAt()));
        tvLastLogin.setText(formatTime(user.getLastLoginAt()));

        View.OnClickListener logout = v -> confirmLogout();
        findViewById(R.id.btn_logout).setOnClickListener(logout);
        findViewById(R.id.btn_logout_top).setOnClickListener(logout);
    }

    /** 账号中间打码，主页属于"公开"场景，避免完整账号暴露。 */
    private String maskAccount(User user) {
        String account = user.getAccount();
        if (account == null || account.length() < 5) {
            return account == null ? "" : account;
        }
        if (User.TYPE_EMAIL.equals(user.getAccountType()) && account.contains("@")) {
            String name = account.substring(0, account.indexOf('@'));
            String domain = account.substring(account.indexOf('@'));
            if (name.length() <= 2) {
                return name.charAt(0) + "***" + domain;
            }
            return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
        }
        return account.substring(0, 3) + "****" + account.substring(account.length() - 2);
    }

    private String typeLabel(String type) {
        if (User.TYPE_EMAIL.equals(type)) {
            return getString(R.string.type_email);
        }
        if (User.TYPE_QQ.equals(type)) {
            return getString(R.string.type_qq);
        }
        return getString(R.string.type_phone);
    }

    private String formatTime(long millis) {
        if (millis <= 0) {
            return "--";
        }
        return new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.CHINA).format(new Date(millis));
    }

    private void playEnterAnimation() {
        AnimUtils.pop(findViewById(R.id.avatar_wrap), 80);
        AnimUtils.stagger(new View[]{
                findViewById(R.id.tv_name),
                findViewById(R.id.tv_badge),
                findViewById(R.id.tv_account),
                findViewById(R.id.card_info),
                findViewById(R.id.card_security),
                findViewById(R.id.btn_logout)
        }, 160, 70);
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.action_logout)
                .setMessage("退出后需要重新登录，本地账号不会被删除。")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> doLogout())
                .show();
    }

    private void doLogout() {
        SessionManager.logout(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
