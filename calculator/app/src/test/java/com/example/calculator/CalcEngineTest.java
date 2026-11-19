package com.example.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 运算引擎单元测试：只依赖纯 Java，{@code ./gradlew test} 即可跑。
 */
public class CalcEngineTest {

    private static CalcEngine type(String keys) {
        CalcEngine engine = new CalcEngine();
        for (int i = 0; i < keys.length(); i++) {
            char c = keys.charAt(i);
            switch (c) {
                case 'C':
                    engine.clear();
                    break;
                case 'D':
                    engine.backspace();
                    break;
                case '=':
                    engine.equals();
                    break;
                case '%':
                    engine.inputPercent();
                    break;
                case '.':
                    engine.inputDot();
                    break;
                case '+':
                case '-':
                case '*':
                case '/':
                    engine.inputOperator(c);
                    break;
                default:
                    engine.inputDigit(String.valueOf(c));
                    break;
            }
        }
        return engine;
    }

    /** 在已有引擎上继续按键。 */
    private static void type(CalcEngine engine, String keys) {
        for (int i = 0; i < keys.length(); i++) {
            char c = keys.charAt(i);
            switch (c) {
                case 'C':
                    engine.clear();
                    break;
                case 'D':
                    engine.backspace();
                    break;
                case '=':
                    engine.equals();
                    break;
                case '%':
                    engine.inputPercent();
                    break;
                case '.':
                    engine.inputDot();
                    break;
                case '+':
                case '-':
                case '*':
                case '/':
                    engine.inputOperator(c);
                    break;
                default:
                    engine.inputDigit(String.valueOf(c));
                    break;
            }
        }
    }

    @Test
    public void addShowsResultBeforeEquals() {
        CalcEngine engine = type("1+2");
        assertEquals("1+2", engine.getExpressionText());
        assertEquals("3", engine.getResultText());
    }

    @Test
    public void multiplyHasHigherPriorityThanAdd() {
        assertEquals("14", type("2+3*4").getResultText());
    }

    @Test
    public void decimalIsExact() {
        assertEquals("0.3", type("0.1+0.2").getResultText());
        assertEquals("2.5", type("10/4").getResultText());
    }

    @Test
    public void longDivisionIsTruncated() {
        assertEquals("0.3333333333", type("1/3").getResultText());
    }

    @Test
    public void divideByZeroShowsErrorAndRecovers() {
        CalcEngine engine = type("5/0");
        assertTrue(engine.isError());
        assertEquals(CalcEngine.ERROR_TEXT, engine.getResultText());

        type(engine, "C");
        assertFalse(engine.isError());
        assertEquals("0", engine.getResultText());
    }

    @Test
    public void leadingZeroIsReplaced() {
        CalcEngine engine = type("00");
        assertEquals("0", engine.getResultText());
        type(engine, "5");
        assertEquals("5", engine.getExpressionText());
    }

    @Test
    public void trailingOperatorIsReplaced() {
        CalcEngine engine = type("5+");
        assertEquals("5", engine.getResultText());
        type(engine, "*");
        assertEquals("5×", engine.getExpressionText());
    }

    @Test
    public void negativeNumberIsSupported() {
        assertEquals("-3", type("-5+2").getResultText());
        assertEquals("-10", type("2+3*-4").getResultText());
    }

    @Test
    public void percentDividesCurrentNumberByHundred() {
        assertEquals("0.5", type("50%").getResultText());
        assertEquals("20", type("200*10%").getResultText());
    }

    @Test
    public void digitAfterEqualsStartsNewExpression() {
        CalcEngine engine = type("1+2=");
        assertEquals("1+2", engine.getExpressionText());
        assertEquals("3", engine.getResultText());

        type(engine, "9");
        assertEquals("9", engine.getExpressionText());
    }

    @Test
    public void operatorAfterEqualsContinuesCalculation() {
        assertEquals("7", type("1+2=+4").getResultText());
    }

    @Test
    public void restoreKeepsState() {
        CalcEngine engine = new CalcEngine();
        engine.restore("12*3", false);
        assertEquals("36", engine.getResultText());
    }
}
