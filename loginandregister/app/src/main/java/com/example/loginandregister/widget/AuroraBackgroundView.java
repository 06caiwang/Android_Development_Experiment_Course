package com.example.loginandregister.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.loginandregister.util.AnimUtils;

import java.util.Random;

/**
 * 极光背景：多层径向渐变光斑缓慢漂移 + 细小微粒上升 + 四周压暗。
 * 全部用 Canvas 自绘，零额外依赖；画笔与 Shader 均复用，避免 onDraw 内分配对象。
 */
public class AuroraBackgroundView extends View {

    private static final int BLOB_COUNT = 5;
    private static final int DOT_COUNT = 26;

    private static final int[] BLOB_COLORS = {
            0x8CB79CFF, // 亮紫
            0x78FF7AC6, // 亮粉
            0x6E6FA8FF, // 亮蓝
            0x5A4FE0B0, // 薄荷
            0x808B5CF6  // 深紫
    };

    private final Paint bgPaint = new Paint();
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();

    private Shader[] blobShaders = new Shader[BLOB_COUNT];
    private LinearGradient bgShader;
    private RadialGradient veilShader;

    /** 光斑基准位置（相对宽高）、半径（相对宽度）、漂移速度与相位。 */
    private final float[] bx = {0.14f, 0.88f, 0.48f, 0.06f, 0.72f};
    private final float[] by = {0.12f, 0.26f, 0.78f, 0.86f, 0.96f};
    private final float[] br = {0.95f, 0.80f, 1.05f, 0.60f, 0.70f};
    private final float[] bSpeed = {0.17f, 0.13f, 0.10f, 0.21f, 0.15f};
    private final float[] bPhase = {0f, 1.7f, 3.1f, 4.4f, 2.2f};
    private final float[] bAmp = {0.10f, 0.08f, 0.12f, 0.07f, 0.09f};

    private final float[] dx = new float[DOT_COUNT];
    private final float[] dy = new float[DOT_COUNT];
    private final float[] dr = new float[DOT_COUNT];
    private final float[] ds = new float[DOT_COUNT];
    private final float[] dp = new float[DOT_COUNT];

    private long startTime;
    private boolean animate = true;

    public AuroraBackgroundView(Context context) {
        this(context, null);
    }

    public AuroraBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        animate = AnimUtils.isAnimationEnabled(context);
        dotPaint.setColor(0xFFFFFFFF);

        Random random = new Random(7L);
        for (int i = 0; i < DOT_COUNT; i++) {
            dx[i] = random.nextFloat();
            dy[i] = random.nextFloat();
            dr[i] = 0.9f + random.nextFloat() * 1.6f;
            ds[i] = 0.012f + random.nextFloat() * 0.03f;
            dp[i] = random.nextFloat() * 6.28f;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) {
            return;
        }
        bgShader = new LinearGradient(0f, 0f, w * 0.35f, h,
                new int[]{0xFF33205C, 0xFF1A1030, 0xFF261845},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP);
        bgPaint.setShader(bgShader);

        for (int i = 0; i < BLOB_COUNT; i++) {
            int color = BLOB_COLORS[i];
            int transparent = color & 0x00FFFFFF;
            blobShaders[i] = new RadialGradient(0f, 0f, 1f,
                    new int[]{color, transparent},
                    new float[]{0f, 1f}, Shader.TileMode.CLAMP);
        }

        veilShader = new RadialGradient(w * 0.5f, h * 0.42f, Math.max(w, h) * 0.82f,
                new int[]{0x00000000, 0x33000000, 0x80000000},
                new float[]{0f, 0.66f, 1f}, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0 || bgShader == null) {
            return;
        }

        long now = SystemClock.uptimeMillis();
        if (startTime == 0L) {
            startTime = now;
        }
        float t = animate ? (now - startTime) / 1000f : 0f;

        canvas.drawRect(0f, 0f, w, h, bgPaint);

        for (int i = 0; i < BLOB_COUNT; i++) {
            float cx = w * bx[i] + (float) Math.sin(t * bSpeed[i] + bPhase[i]) * w * bAmp[i];
            float cy = h * by[i] + (float) Math.cos(t * bSpeed[i] * 0.82f + bPhase[i]) * h * bAmp[i];
            float radius = w * br[i];
            matrix.setTranslate(cx, cy);
            matrix.preScale(radius, radius);
            blobShaders[i].setLocalMatrix(matrix);
            blobPaint.setShader(blobShaders[i]);
            canvas.drawCircle(cx, cy, radius, blobPaint);
        }

        for (int i = 0; i < DOT_COUNT; i++) {
            float progress = (dy[i] - t * ds[i]) % 1f;
            if (progress < 0f) {
                progress += 1f;
            }
            float y = progress * h;
            float x = dx[i] * w + (float) Math.sin(t * 0.6f + dp[i]) * w * 0.012f;
            float twinkle = 0.35f + 0.65f * (0.5f + 0.5f * (float) Math.sin(t * 1.4f + dp[i]));
            dotPaint.setAlpha((int) (60 * twinkle));
            canvas.drawCircle(x, y, dr[i], dotPaint);
        }

        bgPaint.setShader(veilShader);
        canvas.drawRect(0f, 0f, w, h, bgPaint);
        bgPaint.setShader(bgShader);

        if (animate && isAttachedToWindow()) {
            postInvalidateOnAnimation();
        }
    }

    /** 外部可临时关闭动画（例如页面不可见时）。 */
    public void setAnimate(boolean animate) {
        this.animate = animate && AnimUtils.isAnimationEnabled(getContext());
        invalidate();
    }
}
