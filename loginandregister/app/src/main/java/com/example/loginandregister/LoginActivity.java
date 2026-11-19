package com.example.loginandregister;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loginandregister.db.UserDbHelper;
import com.example.loginandregister.model.User;
import com.example.loginandregister.util.AnimUtils;
import com.example.loginandregister.util.PasswordUtils;
import com.example.loginandregister.util.SessionManager;
import com.example.loginandregister.util.Validator;
import com.example.loginandregister.widget.SegmentedTabs;
import com.example.loginandregister.widget.ShineButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 登录页：手机号 / 邮箱 / QQ 三种方式，顶部标签切换。
 */
public class LoginActivity extends AppCompatActivity implements SegmentedTabs.OnTabSelectedListener {

    private static final int REQ_REGISTER = 1001;
    private static final int TAB_PHONE = 0;
    private static final int TAB_EMAIL = 1;
    private static final int TAB_QQ = 2;

    private SegmentedTabs tabs;
    private View formGroup;
    private View qqGroup;
    private View pulseRing;
    private View card;
    private View footerRegister;
    private View footerSecurity;

    private TextInputLayout tilAccount;
    private TextInputLayout tilPassword;
    private TextInputEditText etAccount;
    private TextInputEditText etPassword;
    private CheckBox cbRemember;
    private ShineButton btnLogin;
    private ShineButton btnQq;
    private TextView tvForgot;
    private TextView tvRegister;

    private UserDbHelper db;
    private int currentTab = TAB_PHONE;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = UserDbHelper.get(this);

        // 已登录且勾选了「记住登录状态」，直接进入主页
        if (SessionManager.isLoggedIn(this)) {
            openHome(SessionManager.getUid(this));
            return;
        }

        setContentView(R.layout.activity_login);
        bindViews();
        setupTabs();
        setupInputs();
        setupActions();
        playEnterAnimation();
    }

    private void bindViews() {
        tabs = findViewById(R.id.tabs);
        formGroup = findViewById(R.id.form_group);
        qqGroup = findViewById(R.id.qq_group);
        pulseRing = findViewById(R.id.pulse_ring);
        card = findViewById(R.id.card);
        footerRegister = findViewById(R.id.footer_register);
        footerSecurity = findViewById(R.id.footer_security);

        tilAccount = findViewById(R.id.til_account);
        tilPassword = findViewById(R.id.til_password);
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        btnLogin = findViewById(R.id.btn_login);
        btnQq = findViewById(R.id.btn_qq);
        tvForgot = findViewById(R.id.tv_forgot);
        tvRegister = findViewById(R.id.tv_register);
    }

    private void setupTabs() {
        tabs.setLabels(getString(R.string.tab_phone),
                getString(R.string.tab_email),
                getString(R.string.tab_qq));
        tabs.setOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(int index) {
        currentTab = index;
        if (index == TAB_QQ) {
            formGroup.setVisibility(View.GONE);
            qqGroup.setVisibility(View.VISIBLE);
            AnimUtils.popIn(qqGroup);
            return;
        }
        if (qqGroup.getVisibility() == View.VISIBLE) {
            qqGroup.setVisibility(View.GONE);
            formGroup.setVisibility(View.VISIBLE);
            AnimUtils.popIn(formGroup);
        }
        applyAccountMode(index);
    }

    /** 手机号 / 邮箱切换时，输入框的提示、图标与键盘类型一起变。 */
    private void applyAccountMode(int index) {
        tilAccount.setError(null);
        etAccount.setText("");
        if (index == TAB_PHONE) {
            tilAccount.setHint(getString(R.string.hint_phone));
            tilAccount.setStartIconDrawable(R.drawable.ic_phone);
            etAccount.setInputType(InputType.TYPE_CLASS_PHONE);
            etAccount.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        } else {
            tilAccount.setHint(getString(R.string.hint_email));
            tilAccount.setStartIconDrawable(R.drawable.ic_email);
            etAccount.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            etAccount.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }
    }

    private void setupInputs() {
        tinPasswordToggle();

        etAccount.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilAccount.setError(null);
            }
        });
        etPassword.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilPassword.setError(null);
            }
        });
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            attemptLogin();
            return true;
        });
    }

    /** 密码显隐：切换 inputType 并同步图标，图标带一次缩放缓动。 */
    private void tinPasswordToggle() {
        tilPassword.setEndIconOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            int selection = etPassword.getSelectionEnd();
            if (passwordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                tilPassword.setEndIconDrawable(R.drawable.ic_eye_off);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                tilPassword.setEndIconDrawable(R.drawable.ic_eye);
            }
            if (selection >= 0) {
                etPassword.setSelection(Math.min(selection, etPassword.length()));
            }
            // 图标切换时给输入框一次轻微的重绘强调
            etPassword.setAlpha(0.55f);
            etPassword.animate().alpha(1f).setDuration(200).start();
        });
    }

    private void setupActions() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnQq.setOnClickListener(v -> showQqDialog());
        tvForgot.setOnClickListener(v -> showForgotDialog());
        tvRegister.setOnClickListener(v -> openRegister());

        AnimUtils.pulse(pulseRing);
    }

    private void playEnterAnimation() {
        AnimUtils.stagger(new View[]{card, footerRegister, footerSecurity}, 90, 85);
    }

    // ---------------------------------------------------------------- 登录

    private void attemptLogin() {
        String account = textOf(etAccount);
        String password = textOf(etPassword);
        boolean valid = true;

        if (currentTab == TAB_PHONE) {
            if (!Validator.isPhone(account)) {
                tilAccount.setError("请输入正确的 11 位手机号");
                valid = false;
            }
        } else {
            if (!Validator.isEmail(account)) {
                tilAccount.setError("邮箱格式不正确");
                valid = false;
            }
        }

        if (password.isEmpty()) {
            tilPassword.setError("请输入密码");
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("密码至少 6 位");
            valid = false;
        }

        if (!valid) {
            AnimUtils.shake(btnLogin);
            return;
        }

        User user = db.findByAccount(account);
        if (user == null) {
            tilAccount.setError("该账号尚未注册，请先注册");
            AnimUtils.shake(btnLogin);
            return;
        }
        if (!PasswordUtils.verify(password, user)) {
            tilPassword.setError("密码不正确");
            AnimUtils.shake(btnLogin);
            return;
        }

        db.touchLastLogin(user.getId());
        SessionManager.login(this, user.getId(), cbRemember.isChecked());
        enterHome(user.getId());
    }

    private void showQqDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.qq_dialog_title)
                .setMessage(R.string.qq_dialog_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_agree, (dialog, which) -> doQqLogin())
                .show();
    }

    /** 本地模拟 QQ 授权：固定 openid，首次授权自动建号，之后直接登录。 */
    private void doQqLogin() {
        final String openId = "qq_88213366";
        User user = db.findByAccount(openId);
        if (user == null) {
            User created = new User(openId, User.TYPE_QQ, "企鹅用户", null, null);
            long id = db.insert(created);
            if (id < 0) {
                Toast.makeText(this, "授权失败，请重试", Toast.LENGTH_SHORT).show();
                return;
            }
            user = db.findByAccount(openId);
        }
        if (user == null) {
            return;
        }
        db.touchLastLogin(user.getId());
        SessionManager.login(this, user.getId(), true);
        enterHome(user.getId());
    }

    private void enterHome(final long uid) {
        AnimUtils.revealAndRun(this, btnLogin,
                getResources().getColor(R.color.bg_deep, getTheme()),
                () -> {
                    openHome(uid);
                    overridePendingTransition(0, 0);
                });
    }

    private void openHome(long uid) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_UID, uid);
        startActivity(intent);
        finish();
    }

    private void openRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra(RegisterActivity.EXTRA_ACCOUNT, textOf(etAccount));
        intent.putExtra(RegisterActivity.EXTRA_TYPE,
                currentTab == TAB_EMAIL ? User.TYPE_EMAIL : User.TYPE_PHONE);
        startActivityForResult(intent, REQ_REGISTER);
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_REGISTER || resultCode != RESULT_OK || data == null) {
            return;
        }
        String account = data.getStringExtra(RegisterActivity.RESULT_ACCOUNT);
        String type = data.getStringExtra(RegisterActivity.RESULT_TYPE);
        if (account == null) {
            return;
        }
        int target = User.TYPE_EMAIL.equals(type) ? TAB_EMAIL : TAB_PHONE;
        if (target != currentTab) {
            tabs.select(target);
        }
        etAccount.setText(account);
        etAccount.setSelection(account.length());
        etPassword.setText("");
        tilPassword.setError(null);
        AnimUtils.fadeUp(card, 0);

        Toast.makeText(this, "注册成功，请用新密码登录", Toast.LENGTH_SHORT).show();
    }

    // ------------------------------------------------------------ 忘记密码

    private void showForgotDialog() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_forgot, null);
        final TextInputLayout tilForgotAccount = content.findViewById(R.id.til_forgot_account);
        final TextInputLayout tilForgotPwd = content.findViewById(R.id.til_forgot_pwd);
        final EditText etForgotAccount = content.findViewById(R.id.et_forgot_account);
        final EditText etForgotPwd = content.findViewById(R.id.et_forgot_pwd);
        if (currentTab == TAB_EMAIL) {
            tilForgotAccount.setHint(getString(R.string.hint_email));
            tilForgotAccount.setStartIconDrawable(R.drawable.ic_email);
        }

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.tip_forgot_title)
                .setView(content)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String account = textOf(etForgotAccount);
                    String newPwd = textOf(etForgotPwd);
                    User user = db.findByAccount(account);
                    if (user == null) {
                        tilForgotAccount.setError("该账号不存在");
                        return;
                    }
                    if (!Validator.isPassword(newPwd)) {
                        tilForgotPwd.setError("需 6-20 位，且包含字母和数字");
                        return;
                    }
                    String salt = PasswordUtils.newSalt();
                    db.updatePassword(user.getId(), PasswordUtils.hash(newPwd, salt), salt);
                    dialog.dismiss();
                    Toast.makeText(this, R.string.tip_reset_ok, Toast.LENGTH_SHORT).show();
                }));
        dialog.show();
    }

    private static String textOf(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    /** 只关心 afterTextChanged 的简化 Watcher。 */
    private abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
