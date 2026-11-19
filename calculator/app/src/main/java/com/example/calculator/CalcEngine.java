package com.example.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 计算器状态机。
 *
 * <p>维护一条用户可见的表达式字符串，每次按键后重新计算一次"预览结果"，
 * 因此按下 1、+、2 之后界面就已经显示 {@code =3}，与参考设计一致。</p>
 *
 * <p>表达式内部统一使用 ASCII 运算符 {@code + - * /}，展示时再映射成 × ÷ −。</p>
 */
public class CalcEngine {

    /** 出错时大字号区域显示的文案。 */
    public static final String ERROR_TEXT = "错误";

    /** 整条表达式最大长度，防止无限输入。 */
    private static final int MAX_EXPRESSION_LENGTH = 32;

    /** 单个数字最大长度（不含小数点）。 */
    private static final int MAX_NUMBER_LENGTH = 14;

    private final StringBuilder expression = new StringBuilder();

    /** 是否刚刚按过等号：此时再输入数字应开始新算式。 */
    private boolean justEvaluated = false;

    private boolean error = false;

    private String resultText = "0";

    // ============================ 对外输入 ============================

    /** 输入数字，{@code digits} 为 "0"~"9" 或 "00"。 */
    public void inputDigit(String digits) {
        if (digits == null || digits.isEmpty()) {
            return;
        }
        if (justEvaluated) {
            reset();
        }
        int start = currentNumberStart();
        String current = expression.substring(start);
        // 前导 0 直接替换，避免出现 007
        if ("0".equals(current)) {
            if (digits.startsWith("0")) {
                return;
            }
            expression.setLength(start);
            current = "";
        }
        if (current.length() + digits.length() > MAX_NUMBER_LENGTH
                || expression.length() + digits.length() > MAX_EXPRESSION_LENGTH) {
            return;
        }
        expression.append(digits);
        refresh();
    }

    /** 输入小数点，同一个数字内只允许一个。 */
    public void inputDot() {
        if (justEvaluated) {
            reset();
        }
        int start = currentNumberStart();
        String current = expression.substring(start);
        if (current.contains(".")) {
            return;
        }
        if (current.isEmpty()) {
            expression.append('0');
        }
        if (expression.length() >= MAX_EXPRESSION_LENGTH) {
            return;
        }
        expression.append('.');
        refresh();
    }

    /** 输入运算符：{@code + - * /}。 */
    public void inputOperator(char operator) {
        if (operator != '+' && operator != '-' && operator != '*' && operator != '/') {
            return;
        }
        if (error) {
            reset();
        }
        justEvaluated = false;
        int length = expression.length();
        if (length == 0) {
            // 只允许以减号开头表示负数
            if (operator == '-') {
                expression.append('-');
            }
            // 可能刚清掉了错误态，这里必须刷新一次，让结果回到 0
            refresh();
            return;
        }
        if (expression.charAt(length - 1) == '.') {
            expression.append('0');
            length++;
        }
        if (isOperator(expression.charAt(length - 1))) {
            char lastOperator = expression.charAt(length - 1);
            if (length == 1 && lastOperator == '-') {
                return;
            }
            if (operator == '-' && (lastOperator == '*' || lastOperator == '/')) {
                // 允许 5×−3 这种写法：负号跟在乘除号后面时是追加而不是替换
                if (length >= MAX_EXPRESSION_LENGTH) {
                    return;
                }
                expression.append('-');
            } else {
                // 末尾已是运算符：直接替换，避免出现 5+×
                expression.setCharAt(length - 1, operator);
            }
        } else {
            if (length >= MAX_EXPRESSION_LENGTH) {
                return;
            }
            expression.append(operator);
        }
        refresh();
    }

    /** 百分号：把当前数除以 100 后回写表达式，例如 50 → 0.5。 */
    public void inputPercent() {
        collapseToResult();
        int start = currentNumberStart();
        String current = expression.substring(start);
        if (current.isEmpty() || ".".equals(current)) {
            return;
        }
        try {
            BigDecimal value = new BigDecimal(current)
                    .divide(new BigDecimal(100), MAX_NUMBER_LENGTH, RoundingMode.HALF_UP);
            String formatted = format(value);
            expression.setLength(start);
            expression.append(formatted);
            refresh();
        } catch (NumberFormatException ignored) {
            // 当前不是合法数字（例如 "1."），忽略本次输入
        }
    }

    /** 退格：删掉最后一个字符；刚按过等号则视为清空。 */
    public void backspace() {
        if (justEvaluated) {
            clear();
            return;
        }
        if (expression.length() > 0) {
            expression.deleteCharAt(expression.length() - 1);
            refresh();
        }
    }

    /** 全部清空。 */
    public void clear() {
        reset();
        refresh();
    }

    /** 等号：表达式保持不变，只标记"已结算"，结果继续用实时预览值。 */
    public void equals() {
        if (expression.length() == 0) {
            return;
        }
        justEvaluated = true;
        refresh();
    }

    // ============================ 对外查询 ============================

    /** 顶部小字：带 × ÷ − 的算式。 */
    public String getExpressionText() {
        return display(expression.toString());
    }

    /** 大字号结果（不含 "=" 前缀）。 */
    public String getResultText() {
        return resultText;
    }

    public boolean isError() {
        return error;
    }

    public boolean isJustEvaluated() {
        return justEvaluated;
    }

    // ============================ 状态保存 ============================

    /** 转屏时用于保存原始表达式。 */
    public String getRawExpression() {
        return expression.toString();
    }

    /** 转屏后恢复现场。 */
    public void restore(String rawExpression, boolean evaluated) {
        expression.setLength(0);
        if (rawExpression != null) {
            expression.append(rawExpression);
        }
        justEvaluated = evaluated;
        refresh();
    }

    // ============================ 内部实现 ============================

    private void reset() {
        expression.setLength(0);
        justEvaluated = false;
        error = false;
    }

    /** 把表达式收敛成上一次的结果，让 "%" 之类的操作作用在结果上。 */
    private void collapseToResult() {
        if (!justEvaluated) {
            return;
        }
        String value = error ? "" : resultText;
        expression.setLength(0);
        expression.append(value);
        justEvaluated = false;
    }

    /** 当前正在输入的数字在表达式中的起始下标。 */
    private int currentNumberStart() {
        int index = expression.length();
        while (index > 0) {
            char c = expression.charAt(index - 1);
            if (Character.isDigit(c) || c == '.') {
                index--;
            } else {
                break;
            }
        }
        return index;
    }

    private void refresh() {
        String target = evaluableExpression();
        if (target.isEmpty()) {
            resultText = "0";
            error = false;
            return;
        }
        try {
            resultText = format(ExpressionEvaluator.evaluate(target));
            error = false;
        } catch (RuntimeException e) {
            resultText = ERROR_TEXT;
            error = true;
        }
    }

    /** 去掉末尾悬空的运算符和小数点，才能拿去求值，例如 "1+2×" → "1+2"。 */
    private String evaluableExpression() {
        int end = expression.length();
        while (end > 0) {
            char c = expression.charAt(end - 1);
            if (c == '.' || isOperator(c)) {
                end--;
            } else {
                break;
            }
        }
        return expression.substring(0, end);
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    /** 内部运算符 → 界面展示字符。 */
    private static String display(String raw) {
        StringBuilder builder = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '*':
                    builder.append('×');
                    break;
                case '/':
                    builder.append('÷');
                    break;
                case '-':
                    builder.append('−');
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    /** BigDecimal → 界面文本：去掉多余的 0，超长时按有效数字四舍五入。 */
    static String format(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0);
        }
        String text = normalized.toPlainString();
        if (countSignificant(text) > MAX_NUMBER_LENGTH) {
            normalized = value.round(new MathContext(10, RoundingMode.HALF_UP)).stripTrailingZeros();
            if (normalized.scale() < 0) {
                normalized = normalized.setScale(0);
            }
            text = normalized.toPlainString();
        }
        return text;
    }

    private static int countSignificant(String text) {
        int count = 0;
        int start = text.startsWith("-") ? 1 : 0;
        boolean leadingZero = true;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isDigit(c)) {
                continue;
            }
            if (leadingZero && c == '0') {
                continue;
            }
            leadingZero = false;
            count++;
        }
        return count;
    }
}
