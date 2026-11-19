package com.example.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 四则运算求值器：词法分析 + 递归下降，全程使用 BigDecimal 保证十进制精度。
 *
 * <pre>
 * expr   := term (('+' | '-') term)*
 * term   := factor (('*' | '/') factor)*
 * factor := ('+' | '-')* number
 * </pre>
 *
 * 表达式内部统一使用 ASCII 运算符 {@code + - * /}，展示时才映射成 × ÷ −。
 */
class ExpressionEvaluator {

    /** 除法的中间精度：足够让 1/3 之类的循环小数不丢有效位。 */
    private static final int DIVIDE_SCALE = 19;

    private final List<String> tokens;
    private int pos;

    private ExpressionEvaluator(List<String> tokens) {
        this.tokens = tokens;
    }

    /**
     * 计算表达式的结果。
     *
     * @throws ArithmeticException      除数为 0
     * @throws IllegalArgumentException 表达式不完整或含非法字符
     */
    static BigDecimal evaluate(String expression) {
        List<String> tokens = tokenize(expression);
        if (tokens.isEmpty()) {
            return BigDecimal.ZERO;
        }
        ExpressionEvaluator evaluator = new ExpressionEvaluator(tokens);
        BigDecimal value = evaluator.parseExpression();
        if (evaluator.pos < tokens.size()) {
            throw new IllegalArgumentException("多余的操作数: " + tokens.get(evaluator.pos));
        }
        return value;
    }

    private static List<String> tokenize(String expression) {
        List<String> result = new ArrayList<>();
        int index = 0;
        int length = expression.length();
        while (index < length) {
            char c = expression.charAt(index);
            if (Character.isDigit(c) || c == '.') {
                int start = index;
                while (index < length) {
                    char n = expression.charAt(index);
                    if (!Character.isDigit(n) && n != '.') {
                        break;
                    }
                    index++;
                }
                result.add(expression.substring(start, index));
            } else if (isOperator(c)) {
                result.add(String.valueOf(c));
                index++;
            } else {
                throw new IllegalArgumentException("非法字符: " + c);
            }
        }
        return result;
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private BigDecimal parseExpression() {
        BigDecimal value = parseTerm();
        while (true) {
            String token = peek();
            if ("+".equals(token)) {
                pos++;
                value = value.add(parseTerm());
            } else if ("-".equals(token)) {
                pos++;
                value = value.subtract(parseTerm());
            } else {
                return value;
            }
        }
    }

    private BigDecimal parseTerm() {
        BigDecimal value = parseFactor();
        while (true) {
            String token = peek();
            if ("*".equals(token)) {
                pos++;
                value = value.multiply(parseFactor());
            } else if ("/".equals(token)) {
                pos++;
                BigDecimal divisor = parseFactor();
                if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("除数不能为 0");
                }
                value = value.divide(divisor, DIVIDE_SCALE, RoundingMode.HALF_UP);
            } else {
                return value;
            }
        }
    }

    private BigDecimal parseFactor() {
        String token = peek();
        if (token == null) {
            throw new IllegalArgumentException("表达式不完整");
        }
        if ("-".equals(token)) {
            pos++;
            return parseFactor().negate();
        }
        if ("+".equals(token)) {
            pos++;
            return parseFactor();
        }
        if (isOperator(token.charAt(0))) {
            throw new IllegalArgumentException("运算符位置非法");
        }
        pos++;
        return new BigDecimal(token);
    }

    private String peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }
}
