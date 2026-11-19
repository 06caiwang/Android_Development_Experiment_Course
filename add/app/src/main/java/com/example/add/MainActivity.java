package com.example.add;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 整数加法：读取两个整数输入，求和后把结果显示在界面的"屏幕"区域。
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_RESULT = "state_result";
    private static final String STATE_EXPRESSION = "state_expression";

    private EditText etFirst;
    private EditText etSecond;
    private TextView tvResult;
    private TextView tvExpression;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etFirst = findViewById(R.id.etFirst);
        etSecond = findViewById(R.id.etSecond);
        tvResult = findViewById(R.id.tvResult);
        tvExpression = findViewById(R.id.tvExpression);

        findViewById(R.id.btnCalculate).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            calculate();
        });
        findViewById(R.id.btnClear).setOnClickListener(v -> reset());

        // 键盘上的"下一项 / 完成"直接串起输入与计算
        etFirst.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etSecond.requestFocus();
                return true;
            }
            return false;
        });
        etSecond.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                calculate();
                return true;
            }
            return false;
        });

        // 长按结果即可复制
        tvResult.setOnLongClickListener(v -> {
            copyResult();
            return true;
        });

        if (savedInstanceState != null) {
            tvResult.setText(savedInstanceState.getString(STATE_RESULT, getString(R.string.result_idle)));
            tvExpression.setText(savedInstanceState.getString(STATE_EXPRESSION, getString(R.string.expression_idle)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_RESULT, tvResult.getText().toString());
        outState.putString(STATE_EXPRESSION, tvExpression.getText().toString());
    }

    /** 读取输入：空输入按 0 处理，非法内容返回 null。 */
    private Long readInteger(EditText field) {
        String raw = field.getText().toString().trim();
        if (raw.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 计算并刷新界面。 */
    private void calculate() {
        Long a = readInteger(etFirst);
        if (a == null) {
            reportInvalid(etFirst);
            return;
        }
        Long b = readInteger(etSecond);
        if (b == null) {
            reportInvalid(etSecond);
            return;
        }

        long sum;
        try {
            sum = Math.addExact(a, b);
        } catch (ArithmeticException overflow) {
            Toast.makeText(this, R.string.toast_overflow, Toast.LENGTH_SHORT).show();
            shake(tvResult);
            return;
        }

        // 结果写回界面
        tvResult.setText(String.valueOf(sum));
        tvExpression.setText(buildExpression(a, b) + " =");
        playResultAnimation();
        hideKeyboard();
    }

    /** 拼出 "12 + 30"、"12 − 30" 这种可读的算式（减号而非负号 + 号）。 */
    private String buildExpression(long a, long b) {
        if (b < 0 && b != Long.MIN_VALUE) {
            return a + " − " + (-b);
        }
        if (b < 0) {
            return a + " + (" + b + ")";
        }
        return a + " + " + b;
    }

    /** 结果数字的弹入动画，让数值变化更明显。 */
    private void playResultAnimation() {
        tvResult.animate().cancel();
        tvResult.setScaleX(0.86f);
        tvResult.setScaleY(0.86f);
        tvResult.setAlpha(0f);
        tvResult.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(360)
                .setInterpolator(new OvershootInterpolator(1.6f))
                .start();
    }

    /** 清空输入与结果，回到初始状态。 */
    private void reset() {
        etFirst.setText("");
        etSecond.setText("");
        tvResult.setText(R.string.result_idle);
        tvExpression.setText(R.string.expression_idle);
        tvResult.setAlpha(1f);
        tvResult.setScaleX(1f);
        tvResult.setScaleY(1f);
        hideKeyboard();
    }

    private void reportInvalid(EditText field) {
        Toast.makeText(this, R.string.toast_invalid, Toast.LENGTH_SHORT).show();
        field.requestFocus();
        shake(field);
    }

    /** 左右抖动，作为输入错误的即时反馈。 */
    private void shake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X,
                0f, -16f, 16f, -11f, 11f, -5f, 0f);
        animator.setDuration(440);
        animator.start();
    }

    private void copyResult() {
        CharSequence text = tvResult.getText();
        if (text == null || text.length() == 0) {
            return;
        }
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) {
            return;
        }
        manager.setPrimaryClip(ClipData.newPlainText("result", text));
        Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }
}
