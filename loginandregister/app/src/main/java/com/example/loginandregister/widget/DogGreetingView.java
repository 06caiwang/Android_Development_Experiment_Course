package com.example.loginandregister.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.loginandregister.R;
import com.example.loginandregister.util.AnimUtils;

/**
 * 趴在登录卡片顶边的小狗：会轻轻起伏呼吸、偶尔眨眼，
 * 头顶的气泡每隔几秒换一句招呼。
 * 视觉上小狗的下半身压在卡片边框上，形成"趴在卡片上"的效果。
 */
public class DogGreetingView extends View {

    private static final String[] DEFAULT_GREETINGS = {
            "嗨，欢迎回来～",
            "今天也要元气满满！",
            "想你了，快登录吧",
            "汪！又见面啦",
            "密码没忘吧？",
            "慢慢来，不着急"
    };

    private final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint earPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint darkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tempRect = new RectF();
    private final RectF bubbleRect = new RectF();

    private int bodyColor;
    private int pawColor;
    private int earColor;
    private int darkColor;

    private String[] greetings = DEFAULT_GREETINGS;
    private String currentText = DEFAULT_GREETINGS[0];
    private int greetingIndex;
    private float textAlpha = 1f;
    private boolean blink;
    private boolean running;
    private long startTime;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable cycleRunnable = new Runnable() {
        @Override
        public void run() {
            fadeToNextGreeting();
            handler.postDelayed(this, 3800);
        }
    };

    private final Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            blink = true;
            invalidate();
            handler.postDelayed(() -> {
                blink = false;
                invalidate();
            }, 130);
            handler.postDelayed(this, 3600);
        }
    };

    public DogGreetingView(Context context) {
        this(context, null);
    }

    public DogGreetingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        bodyColor = ContextCompat.getColor(context, R.color.dog_body);
        pawColor = ContextCompat.getColor(context, R.color.dog_paw);
        earColor = ContextCompat.getColor(context, R.color.dog_ear);
        darkColor = ContextCompat.getColor(context, R.color.dog_dark);

        bodyPaint.setColor(bodyColor);
        earPaint.setColor(earColor);
        darkPaint.setColor(darkColor);
        blushPaint.setColor(ContextCompat.getColor(context, R.color.dog_blush));
        blushPaint.setAlpha(90);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(darkColor);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        bubblePaint.setColor(0xF2FFFFFF);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(ContextCompat.getColor(context, R.color.dog_dark));
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.5f,
                getResources().getDisplayMetrics()));
    }

    public void setGreetings(String[] greetings) {
        if (greetings != null && greetings.length > 0) {
            this.greetings = greetings;
            this.greetingIndex = 0;
            this.currentText = greetings[0];
            invalidate();
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        running = AnimUtils.isAnimationEnabled(getContext());
        if (running) {
            startTime = 0L;
            handler.postDelayed(cycleRunnable, 3800);
            handler.postDelayed(blinkRunnable, 2200);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        running = false;
        handler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }

    private void fadeToNextGreeting() {
        ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
        fadeOut.setDuration(160);
        fadeOut.addUpdateListener(a -> {
            textAlpha = (float) a.getAnimatedValue();
            invalidate();
        });
        fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                greetingIndex = (greetingIndex + 1) % greetings.length;
                currentText = greetings[greetingIndex];
                ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f);
                fadeIn.setDuration(220);
                fadeIn.addUpdateListener(a -> {
                    textAlpha = (float) a.getAnimatedValue();
                    invalidate();
                });
                fadeIn.start();
            }
        });
        fadeOut.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }

        long now = SystemClock.uptimeMillis();
        if (startTime == 0L) {
            startTime = now;
        }
        float t = (now - startTime) / 1000f;
        float bob = running ? (float) Math.sin(t * 2.1f) * dp(1.5f) : 0f;

        float cx = w / 2f;
        float bottom = h;

        float headR = dp(24);
        float headCy = bottom - dp(52) + bob;
        float bodyCy = bottom - dp(26) + bob;

        // 落地投影：让小狗"贴"在卡片上，而不是浮在空中
        shadowPaint.setColor(0x38000000);
        tempRect.set(cx - dp(52), bodyCy + dp(4), cx + dp(52), bodyCy + dp(24));
        canvas.drawOval(tempRect, shadowPaint);

        // 尾巴（画在身体之后，露出右边缘）
        bodyPaint.setColor(bodyColor);
        canvas.save();
        canvas.rotate(-32f, cx + dp(54), bottom - dp(32));
        tempRect.set(cx + dp(46), bottom - dp(40), cx + dp(64), bottom - dp(26));
        canvas.drawOval(tempRect, bodyPaint);
        canvas.restore();

        // 身体
        tempRect.set(cx - dp(52), bodyCy - dp(18), cx + dp(52), bodyCy + dp(18));
        canvas.drawOval(tempRect, bodyPaint);

        // 耳朵（先画，被头部压住一半，形成垂耳）
        drawEar(canvas, cx - dp(21), headCy - dp(3), -26f);
        drawEar(canvas, cx + dp(21), headCy - dp(3), 26f);

        // 头
        bodyPaint.setColor(bodyColor);
        canvas.drawCircle(cx, headCy, headR, bodyPaint);

        // 眼睛
        float eyeDx = dp(8.5f);
        float eyeY = headCy - dp(3);
        float eyeR = dp(2.9f);
        float eyeRy = blink ? dp(0.4f) : eyeR;
        tempRect.set(cx - eyeDx - eyeR, eyeY - eyeRy, cx - eyeDx + eyeR, eyeY + eyeRy);
        canvas.drawOval(tempRect, darkPaint);
        tempRect.set(cx + eyeDx - eyeR, eyeY - eyeRy, cx + eyeDx + eyeR, eyeY + eyeRy);
        canvas.drawOval(tempRect, darkPaint);

        // 鼻子
        tempRect.set(cx - dp(4.2f), headCy + dp(3.6f), cx + dp(4.2f), headCy + dp(10));
        canvas.drawOval(tempRect, darkPaint);

        // 嘴
        strokePaint.setStrokeWidth(dp(1.5f));
        canvas.drawArc(cx - dp(5), headCy + dp(7), cx, headCy + dp(13),
                0f, 160f, false, strokePaint);
        canvas.drawArc(cx, headCy + dp(7), cx + dp(5), headCy + dp(13),
                20f, 160f, false, strokePaint);

        // 腮红
        tempRect.set(cx - dp(20), headCy + dp(4), cx - dp(11), headCy + dp(10));
        canvas.drawOval(tempRect, blushPaint);
        tempRect.set(cx + dp(11), headCy + dp(4), cx + dp(20), headCy + dp(10));
        canvas.drawOval(tempRect, blushPaint);

        // 前爪：颜色略深一档，压在卡片上
        float pawTop = bottom - dp(30);
        float pawBottom = bottom - dp(2);
        pawPaint.setColor(pawColor);
        tempRect.set(cx - dp(34), pawTop, cx - dp(11), pawBottom);
        canvas.drawRoundRect(tempRect, dp(11), dp(11), pawPaint);
        tempRect.set(cx + dp(11), pawTop, cx + dp(34), pawBottom);
        canvas.drawRoundRect(tempRect, dp(11), dp(11), pawPaint);

        // 爪缝
        strokePaint.setStrokeWidth(dp(1.2f));
        strokePaint.setColor(0x2E000000);
        canvas.drawLine(cx - dp(22.5f), pawBottom - dp(10), cx - dp(22.5f), pawBottom - dp(5),
                strokePaint);
        canvas.drawLine(cx + dp(22.5f), pawBottom - dp(10), cx + dp(22.5f), pawBottom - dp(5),
                strokePaint);
        strokePaint.setColor(darkColor);

        // 气泡
        drawBubble(canvas, cx, headCy - headR - dp(3));

        if (running && isAttachedToWindow()) {
            postInvalidateOnAnimation();
        }
    }

    private void drawEar(Canvas canvas, float x, float y, float rotation) {
        canvas.save();
        canvas.rotate(rotation, x, y);
        earPaint.setColor(earColor);
        tempRect.set(x - dp(9), y - dp(8), x + dp(9), y + dp(15));
        canvas.drawOval(tempRect, earPaint);
        canvas.restore();
    }

    private void drawBubble(Canvas canvas, float cx, float bubbleBottom) {
        float padding = dp(13);
        float textWidth = textPaint.measureText(currentText);
        float bubbleH = dp(29);
        float bubbleW = Math.min(textWidth + padding * 2, getWidth() - dp(14));
        float top = bubbleBottom - bubbleH;

        bubbleRect.set(cx - bubbleW / 2f, top, cx + bubbleW / 2f, bubbleBottom);
        canvas.drawRoundRect(bubbleRect, bubbleH / 2f, bubbleH / 2f, bubblePaint);

        // 指向小脑袋的小尖角
        android.graphics.Path pointer = new android.graphics.Path();
        pointer.moveTo(cx - dp(5.5f), bubbleBottom - dp(1));
        pointer.lineTo(cx + dp(5.5f), bubbleBottom - dp(1));
        pointer.lineTo(cx, bubbleBottom + dp(7));
        pointer.close();
        canvas.drawPath(pointer, bubblePaint);

        // 文字垂直居中
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float baseline = top + bubbleH / 2f - (fm.ascent + fm.descent) / 2f;
        int alpha = (int) (255 * textAlpha);
        textPaint.setAlpha(alpha);
        canvas.drawText(currentText, cx, baseline, textPaint);
        textPaint.setAlpha(255);
    }
}
