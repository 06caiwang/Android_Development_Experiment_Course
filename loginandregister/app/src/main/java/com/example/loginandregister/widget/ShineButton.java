package com.example.loginandregister.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.example.loginandregister.util.AnimUtils;

/**
 * 带流光描边的按钮：一圈高光沿边框环绕。
 * 用 SweepGradient + 矩阵旋转实现，不依赖任何动画库。
 */
public class ShineButton extends AppCompatButton {

    private final Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private final Matrix matrix = new Matrix();

    private SweepGradient shader;
    private float angle;
    private float cornerRadius;
    private float strokeWidth;
    private boolean shining = true;

    public ShineButton(Context context) {
        this(context, null);
    }

    public ShineButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public ShineButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strokeWidth = AnimUtils.dp(context, 1.4f);
        cornerRadius = AnimUtils.dp(context, 27f);
        shinePaint.setStyle(Paint.Style.STROKE);
        shinePaint.setStrokeWidth(strokeWidth);
        shining = AnimUtils.isAnimationEnabled(context);
    }

    /** 关闭/开启动效（例如按钮置灰时调用）。 */
    public void setShining(boolean shining) {
        this.shining = shining && AnimUtils.isAnimationEnabled(getContext());
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) {
            return;
        }
        float inset = strokeWidth / 2f + AnimUtils.dp(getContext(), 0.6f);
        bounds.set(inset, inset, w - inset, h - inset);
        shader = new SweepGradient(w / 2f, h / 2f,
                new int[]{0x00FFFFFF, 0x33FFFFFF, 0xFFFFFFFF, 0x33FFFFFF, 0x00FFFFFF},
                new float[]{0f, 0.16f, 0.5f, 0.84f, 1f});
        shinePaint.setShader(shader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!shining || shader == null || !isEnabled()) {
            return;
        }
        matrix.setRotate(angle, getWidth() / 2f, getHeight() / 2f);
        shader.setLocalMatrix(matrix);
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, shinePaint);
        angle = (angle + 1.35f) % 360f;
        if (isAttachedToWindow()) {
            postInvalidateOnAnimation();
        }
    }
}
