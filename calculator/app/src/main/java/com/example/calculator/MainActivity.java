package com.example.calculator;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * 计算器主界面：只负责"找控件 → 分发按键 → 刷新显示"，运算逻辑全在 {@link CalcEngine}。
 *
 * <p>键盘由 TableLayout 的 5 个 TableRow 组成，每个按键用 {@code android:tag}
 * 标注语义（"7"、"+"、"C"、"DEL" …），因此 20 个按键共用同一个点击监听。</p>
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_EXPRESSION = "state_expression";
    private static final String STATE_EVALUATED = "state_evaluated";

    /** 键盘全部按键，顺序与布局一致：5 行 × 4 列。 */
    private static final int[] KEY_IDS = {
            R.id.key_c, R.id.key_del, R.id.key_percent, R.id.key_div,
            R.id.key_7, R.id.key_8, R.id.key_9, R.id.key_mul,
            R.id.key_4, R.id.key_5, R.id.key_6, R.id.key_sub,
            R.id.key_1, R.id.key_2, R.id.key_3, R.id.key_add,
            R.id.key_00, R.id.key_0, R.id.key_dot, R.id.key_eq
    };

    private final CalcEngine engine = new CalcEngine();

    private TextView expressionView;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expressionView = findViewById(R.id.tv_expression);
        resultView = findViewById(R.id.tv_result);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                dispatchKey(view);
                render();
            }
        };
        for (int id : KEY_IDS) {
            findViewById(id).setOnClickListener(listener);
        }

        if (savedInstanceState != null) {
            engine.restore(savedInstanceState.getString(STATE_EXPRESSION),
                    savedInstanceState.getBoolean(STATE_EVALUATED));
        }
        render();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPRESSION, engine.getRawExpression());
        outState.putBoolean(STATE_EVALUATED, engine.isJustEvaluated());
    }

    /** 根据按键的 tag 分发到引擎。 */
    private void dispatchKey(View view) {
        Object tag = view.getTag();
        if (!(tag instanceof String)) {
            return;
        }
        String key = (String) tag;
        switch (key) {
            case "C":
                engine.clear();
                break;
            case "DEL":
                engine.backspace();
                break;
            case "%":
                engine.inputPercent();
                break;
            case "=":
                engine.equals();
                break;
            case ".":
                engine.inputDot();
                break;
            case "+":
            case "-":
            case "*":
            case "/":
                engine.inputOperator(key.charAt(0));
                break;
            default:
                engine.inputDigit(key);
                break;
        }
    }

    /** 把引擎状态渲染到显示区。 */
    private void render() {
        expressionView.setText(engine.getExpressionText());
        resultView.setText(engine.getResultText());
        resultView.setTextColor(ContextCompat.getColor(this,
                engine.isError() ? R.color.text_error : R.color.text_primary));
    }
}
