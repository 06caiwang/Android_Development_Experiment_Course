package com.example.daily_new;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 登录页：单用户校验，账号 zyh，密码 123456。
 */
public class LoginActivity extends AppCompatActivity {

    private static final String USER_NAME = "zyh";
    private static final String USER_PASSWORD = "123456";

    private EditText etAccount;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        TextView btnLogin = findViewById(R.id.btn_login);

        // 提示文案由常量拼接，保证展示的演示账号与校验逻辑完全一致
        TextView tip = findViewById(R.id.tv_login_tip);
        tip.setText("演示账号：" + USER_NAME + "　密码：" + USER_PASSWORD);

        btnLogin.setOnClickListener(v -> login());

        // 键盘上的“完成”键也可以直接登录
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login();
                return true;
            }
            return false;
        });
    }

    private void login() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.login_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (USER_NAME.equals(account) && USER_PASSWORD.equals(password)) {
            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
            etPassword.setText("");
            etPassword.requestFocus();
        }
    }
}
