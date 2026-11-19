package com.example.loginandregister;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.loginandregister.db.UserDbHelper;
import com.example.loginandregister.model.User;
import com.example.loginandregister.util.AnimUtils;
import com.example.loginandregister.util.PasswordUtils;
import com.example.loginandregister.util.Validator;
import com.example.loginandregister.widget.CheckView;
import com.example.loginandregister.widget.SegmentedTabs;
import com.example.loginandregister.widget.ShineButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.Random;

/**
 * 注册页：手机号 / 邮箱两种方式，含本地模拟验证码与密码强度反馈。
 */
public class RegisterActivity extends AppCompatActivity implements SegmentedTabs.OnTabSelectedListener {

    public static final String EXTRA_ACCOUNT = "extra_account";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String RESULT_ACCOUNT = "result_account";
    public static final String RESULT_TYPE = "result_type";

    private static final int TAB_PHONE = 0;
    private static final int TAB_EMAIL = 1;
    private static final long CODE_TTL = 5 * 60 * 1000L;

    private SegmentedTabs tabs;
    private View btnBack;
    private View tvTitle;
    private View tvSubtitle;
    private View card;
    private View footerLogin;
    private View strengthRow;
    private View seg1;
    private View seg2;
    private View seg3;
    private TextView tvStrength;
    private TextView btnSendCode;
    private TextView tvAgree;
    private TextView tvGoLogin;

    private TextInputLayout tilAccount;
    private TextInputLayout tilCode;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirm;
    private TextInputEditText etAccount;
    private TextInputEditText etCode;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirm;
    private CheckBox cbAgree;
    private ShineButton btnRegister;

    private UserDbHelper db;
    private int currentTab = TAB_PHONE;
    private String pendingCode;
    private long codeSentAt;
    private CountDownTimer countdown;

    private int colorWeak;
    private int colorMid;
    private int colorStrong;
    private int colorIdle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = UserDbHelper.get(this);
        colorWeak = ContextCompat.getColor(this, R.color.error);
        colorMid = ContextCompat.getColor(this, R.color.warning);
        colorStrong = ContextCompat.getColor(this, R.color.success);
        colorIdle = 0x33FFFFFF;

        bindViews();
        setupTabs();
        setupInputs();
        setupAgreement();
        setupActions();
        applyAccountMode(currentTab);
        prefill();
        playEnterAnimation();
    }

    private void bindViews() {
        tabs = findViewById(R.id.tabs);
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        card = findViewById(R.id.card);
        footerLogin = findViewById(R.id.footer_login);
        strengthRow = findViewById(R.id.strength_row);
        seg1 = findViewById(R.id.seg_1);
        seg2 = findViewById(R.id.seg_2);
        seg3 = findViewById(R.id.seg_3);
        tvStrength = findViewById(R.id.tv_strength);
        btnSendCode = findViewById(R.id.btn_send_code);
        tvAgree = findViewById(R.id.tv_agree);
        tvGoLogin = findViewById(R.id.tv_go_login);

        tilAccount = findViewById(R.id.til_account);
        tilCode = findViewById(R.id.til_code);
        tilPassword = findViewById(R.id.til_password);
        tilConfirm = findViewById(R.id.til_confirm);
        etAccount = findViewById(R.id.et_account);
        etCode = findViewById(R.id.et_code);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);
        cbAgree = findViewById(R.id.cb_agree);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupTabs() {
        tabs.setLabels(getString(R.string.tab_phone), getString(R.string.tab_email));
        tabs.setOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(int index) {
        currentTab = index;
        applyAccountMode(index);
        AnimUtils.popIn(card);
    }

    private void applyAccountMode(int index) {
        tilAccount.setError(null);
        etAccount.setText("");
        if (index == TAB_PHONE) {
            tilAccount.setHint(getString(R.string.hint_phone));
            tilAccount.setStartIconDrawable(R.drawable.ic_phone);
            etAccount.setInputType(InputType.TYPE_CLASS_PHONE);
        } else {
            tilAccount.setHint(getString(R.string.hint_email));
            tilAccount.setStartIconDrawable(R.drawable.ic_email);
            etAccount.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
    }

    private void prefill() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String account = intent.getStringExtra(EXTRA_ACCOUNT);
        String type = intent.getStringExtra(EXTRA_TYPE);
        if (User.TYPE_EMAIL.equals(type) && currentTab != TAB_EMAIL) {
            tabs.select(TAB_EMAIL);
        }
        if (account != null && !account.isEmpty()) {
            etAccount.setText(account);
            etAccount.setSelection(account.length());
        }
    }

    private void setupInputs() {
        bindPasswordToggle(tilPassword, etPassword);
        bindPasswordToggle(tilConfirm, etConfirm);

        etAccount.addTextChangedListener(new Watcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilAccount.setError(null);
            }
        });
        etCode.addTextChangedListener(new Watcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilCode.setError(null);
            }
        });
        etPassword.addTextChangedListener(new Watcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilPassword.setError(null);
                updateStrength(s.toString());
            }
        });
        etConfirm.addTextChangedListener(new Watcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilConfirm.setError(null);
            }
        });
    }

    private void bindPasswordToggle(final TextInputLayout layout, final EditText editText) {
        layout.setEndIconOnClickListener(v -> {
            boolean visible = layout.getTag() != null && (Boolean) layout.getTag();
            int selection = editText.getSelectionEnd();
            if (visible) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                layout.setEndIconDrawable(R.drawable.ic_eye);
                layout.setTag(false);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                layout.setEndIconDrawable(R.drawable.ic_eye_off);
                layout.setTag(true);
            }
            if (selection >= 0) {
                editText.setSelection(Math.min(selection, editText.length()));
            }
        });
    }

    /** 密码强度：三段式进度条 + 文字，颜色随强度变化。 */
    private void updateStrength(String password) {
        if (password == null || password.isEmpty()) {
            strengthRow.setVisibility(View.INVISIBLE);
            return;
        }
        strengthRow.setVisibility(View.VISIBLE);
        int level = Validator.strength(password);
        int color = level == 0 ? colorWeak : (level == 1 ? colorMid : colorStrong);
        int filled = level + 1;

        paintSegment(seg1, filled >= 1, color);
        paintSegment(seg2, filled >= 2, color);
        paintSegment(seg3, filled >= 3, color);

        if (level == 0) {
            tvStrength.setText(R.string.strength_weak);
        } else if (level == 1) {
            tvStrength.setText(R.string.strength_mid);
        } else {
            tvStrength.setText(R.string.strength_strong);
        }
        tvStrength.setTextColor(color);
    }

    private void paintSegment(View segment, boolean active, int color) {
        segment.setBackgroundTintList(ColorStateList.valueOf(active ? color : colorIdle));
        if (active) {
            segment.setScaleX(0.4f);
            segment.animate().scaleX(1f).setDuration(240).start();
        }
    }

    private void setupAgreement() {
        String user = getString(R.string.agree_user);
        String privacy = getString(R.string.agree_privacy);
        String full = getString(R.string.agree_prefix) + " " + user + " "
                + getString(R.string.agree_and) + " " + privacy;

        SpannableString span = new SpannableString(full);
        int accent = ContextCompat.getColor(this, R.color.accent);
        int startUser = full.indexOf(user);
        int startPrivacy = full.indexOf(privacy);
        if (startUser >= 0) {
            span.setSpan(new ForegroundColorSpan(accent), startUser,
                    startUser + user.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (startPrivacy >= 0) {
            span.setSpan(new ForegroundColorSpan(accent), startPrivacy,
                    startPrivacy + privacy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvAgree.setText(span);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right);
        });
        tvGoLogin.setOnClickListener(v -> btnBack.performClick());
        btnSendCode.setOnClickListener(v -> sendCode());
        btnRegister.setOnClickListener(v -> attemptRegister());
        cbAgree.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateRegisterButtonState());
        updateRegisterButtonState();
    }

    private void updateRegisterButtonState() {
        boolean enabled = cbAgree.isChecked();
        btnRegister.setEnabled(enabled);
        btnRegister.setAlpha(enabled ? 1f : 0.55f);
        btnRegister.setShining(enabled);
    }

    private void playEnterAnimation() {
        AnimUtils.stagger(new View[]{btnBack, tvTitle, tvSubtitle, card, footerLogin}, 40, 75);
    }

    // ------------------------------------------------------------ 验证码

    private void sendCode() {
        String account = textOf(etAccount);
        if (currentTab == TAB_PHONE && !Validator.isPhone(account)) {
            tilAccount.setError("请输入正确的 11 位手机号");
            AnimUtils.shake(tilAccount);
            return;
        }
        if (currentTab == TAB_EMAIL && !Validator.isEmail(account)) {
            tilAccount.setError("邮箱格式不正确");
            AnimUtils.shake(tilAccount);
            return;
        }
        if (db.exists(account)) {
            tilAccount.setError("该账号已注册，请直接登录");
            AnimUtils.shake(tilAccount);
            return;
        }

        pendingCode = String.format(Locale.CHINA, "%06d", new Random().nextInt(1000000));
        codeSentAt = System.currentTimeMillis();
        tilCode.setHelperText("本地模拟验证码：" + pendingCode);
        tilCode.setHelperTextColor(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.accent)));
        etCode.requestFocus();
        Toast.makeText(this, getString(R.string.tip_code_sent, pendingCode),
                Toast.LENGTH_SHORT).show();
        startCountdown();
    }

    private void startCountdown() {
        if (countdown != null) {
            countdown.cancel();
        }
        btnSendCode.setEnabled(false);
        btnSendCode.setAlpha(0.55f);
        countdown = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnSendCode.setText((millisUntilFinished / 1000) + "s 后重发");
            }

            @Override
            public void onFinish() {
                btnSendCode.setEnabled(true);
                btnSendCode.setAlpha(1f);
                btnSendCode.setText(R.string.action_send_code);
            }
        }.start();
    }

    // -------------------------------------------------------------- 注册

    private void attemptRegister() {
        String account = textOf(etAccount);
        String code = textOf(etCode);
        String password = textOf(etPassword);
        String confirm = textOf(etConfirm);
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

        if (pendingCode == null) {
            tilCode.setError(getString(R.string.tip_code_first));
            valid = false;
        } else if (System.currentTimeMillis() - codeSentAt > CODE_TTL) {
            tilCode.setError("验证码已过期，请重新获取");
            valid = false;
        } else if (!Validator.isCode(code)) {
            tilCode.setError("请输入 6 位数字验证码");
            valid = false;
        } else if (!pendingCode.equals(code)) {
            tilCode.setError(getString(R.string.tip_code_wrong));
            valid = false;
        }

        if (!Validator.isPassword(password)) {
            tilPassword.setError("需 6-20 位，且同时包含字母和数字");
            valid = false;
        } else if (!password.equals(confirm)) {
            tilConfirm.setError("两次输入的密码不一致");
            valid = false;
        }

        if (!cbAgree.isChecked()) {
            Toast.makeText(this, R.string.tip_agree_first, Toast.LENGTH_SHORT).show();
            AnimUtils.shake(cbAgree);
            valid = false;
        }

        if (!valid) {
            AnimUtils.shake(btnRegister);
            return;
        }

        if (db.exists(account)) {
            tilAccount.setError("该账号已注册，请直接登录");
            AnimUtils.shake(tilAccount);
            return;
        }

        String salt = PasswordUtils.newSalt();
        String type = currentTab == TAB_EMAIL ? User.TYPE_EMAIL : User.TYPE_PHONE;
        String nickname = buildNickname(account, type);
        User user = new User(account, type, nickname,
                PasswordUtils.hash(password, salt), salt);
        long id = db.insert(user);
        if (id < 0) {
            Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        showSuccessDialog(account, type);
    }

    private String buildNickname(String account, String type) {
        if (User.TYPE_EMAIL.equals(type) && account.contains("@")) {
            return account.substring(0, account.indexOf('@'));
        }
        if (account.length() >= 4) {
            return "用户" + account.substring(account.length() - 4);
        }
        return "新用户";
    }

    private void showSuccessDialog(final String account, final String type) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        final CheckView checkView = content.findViewById(R.id.check);

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(content)
                .setCancelable(false)
                .create();
        dialog.show();
        checkView.play();

        content.postDelayed(dialog::dismiss, 1600);
        content.postDelayed(() -> finishWithResult(account, type), 1750);
    }

    private void finishWithResult(String account, String type) {
        Intent data = new Intent();
        data.putExtra(RESULT_ACCOUNT, account);
        data.putExtra(RESULT_TYPE, type);
        setResult(RESULT_OK, data);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        super.onDestroy();
    }

    private static String textOf(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private abstract static class Watcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
