package com.example.loginandregister.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.loginandregister.R;
import com.example.loginandregister.util.AnimUtils;

/**
 * 打勾动画：外圈弹性出现，对勾用 PathMeasure 一笔画出。
 */
public class CheckView extends View {

    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path checkPath = new Path();
    private final Path partialPath = new Path();
    private final PathMeasure pathMeasure = new PathMeasure();
    private final RectF ringRect = new RectF();

    private float ringProgress;
    private float scale = 0.6f;
    private float checkProgress;
    private float stroke;
    private int color;

    public CheckView(Context context) {
        this(context, null);
    }

    public CheckView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        color = ContextCompat.getColor(context, R.color.success);
        stroke = AnimUtils.dp(context, 2f);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(stroke);
        ringPaint.setColor(color);

        checkPaint.setStyle(Paint.Style.STROKE);
        checkPaint.setStrokeWidth(AnimUtils.dp(context, 2.6f));
        checkPaint.setColor(color);
        checkPaint.setStrokeCap(Paint.Cap.ROUND);
        checkPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float inset = stroke + AnimUtils.dp(getContext(), 1f);
        ringRect.set(inset, inset, w - inset, h - inset);

        checkPath.reset();
        checkPath.moveTo(w * 0.30f, h * 0.52f);
        checkPath.lineTo(w * 0.44f, h * 0.66f);
        checkPath.lineTo(w * 0.71f, h * 0.36f);
        pathMeasure.setPath(checkPath, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        canvas.save();
        canvas.scale(scale, scale, cx, cy);

        ringPaint.setAlpha((int) (255 * ringProgress));
        canvas.drawArc(ringRect, -90f, 360f * ringProgress, false, ringPaint);

        if (checkProgress > 0f) {
            partialPath.reset();
            float length = pathMeasure.getLength();
            pathMeasure.getSegment(0f, length * checkProgress, partialPath, true);
            canvas.drawPath(partialPath, checkPaint);
        }

        canvas.restore();
    }

    /** 播放一次入场动画。 */
    public void play() {
        if (!AnimUtils.isAnimationEnabled(getContext())) {
            ringProgress = 1f;
            checkProgress = 1f;
            scale = 1f;
            invalidate();
            return;
        }

        ValueAnimator ring = ValueAnimator.ofFloat(0f, 1f);
        ring.setDuration(320);
        ring.setInterpolator(new DecelerateInterpolator());
        ring.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                ringProgress = Math.min(1f, f * 1.6f);
                scale = 0.6f + 0.4f * f;
                invalidate();
            }
        });
        ring.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scale = 1f;
                ValueAnimator check = ValueAnimator.ofFloat(0f, 1f);
                check.setDuration(420);
                check.setInterpolator(new OvershootInterpolator(0.9f));
                check.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        checkProgress = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });
                check.start();
            }
        });
        ring.start();
    }
}
