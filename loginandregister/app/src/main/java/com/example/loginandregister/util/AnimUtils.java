package com.example.loginandregister.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.provider.Settings;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * 动效工具：入场 stagger、圆形扩散转场、抖动、呼吸、弹入。
 * 所有动效都遵守系统的「移除动画」无障碍设置。
 */
public final class AnimUtils {

    private AnimUtils() {
    }

    public static float dp(Context context, float value) {
        return value * context.getResources().getDisplayMetrics().density;
    }

    /** 系统是否开启动画。关闭时所有动效直接跳到终态。 */
    public static boolean isAnimationEnabled(Context context) {
        try {
            float scale = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
            return scale != 0f;
        } catch (Exception e) {
            return true;
        }
    }

    /** 单元素淡入上浮。 */
    public static void fadeUp(View view, long delay) {
        if (view == null) {
            return;
        }
        if (!isAnimationEnabled(view.getContext())) {
            view.setAlpha(1f);
            view.setTranslationY(0f);
            return;
        }
        view.setAlpha(0f);
        view.setTranslationY(dp(view.getContext(), 26));
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(520)
                .setInterpolator(new DecelerateInterpolator(1.6f))
                .start();
    }

    /**
     * 逐元素入场：每个元素依次淡入上浮。
     *
     * @param views      参与动效的控件
     * @param startDelay 整体起始延迟
     * @param gap        相邻元素间隔
     */
    public static void stagger(final View[] views, long startDelay, long gap) {
        if (views == null) {
            return;
        }
        for (int i = 0; i < views.length; i++) {
            if (views[i] != null) {
                fadeUp(views[i], startDelay + i * gap);
            }
        }
    }

    /** tab 切换时内容区的轻微弹入。 */
    public static void popIn(View view) {
        if (view == null) {
            return;
        }
        if (!isAnimationEnabled(view.getContext())) {
            view.setAlpha(1f);
            view.setScaleX(1f);
            view.setScaleY(1f);
            return;
        }
        view.animate().cancel();
        view.setAlpha(0f);
        view.setScaleX(0.965f);
        view.setScaleY(0.965f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator(1.4f))
                .start();
    }

    /** 校验失败时的横向抖动。 */
    public static void shake(View view) {
        if (view == null || !isAnimationEnabled(view.getContext())) {
            return;
        }
        float d = dp(view.getContext(), 9);
        Keyframe[] frames = new Keyframe[]{
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(0.16f, -d),
                Keyframe.ofFloat(0.32f, d),
                Keyframe.ofFloat(0.50f, -d * 0.7f),
                Keyframe.ofFloat(0.68f, d * 0.45f),
                Keyframe.ofFloat(0.84f, -d * 0.2f),
                Keyframe.ofFloat(1f, 0f)
        };
        PropertyValuesHolder holder =
                PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X, frames);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, holder);
        animator.setDuration(430);
        animator.start();
    }

    /** 按钮外圈无限呼吸光环。 */
    public static void pulse(final View ring) {
        if (ring == null) {
            return;
        }
        if (!isAnimationEnabled(ring.getContext())) {
            ring.setAlpha(0f);
            return;
        }
        PropertyValuesHolder sx = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.16f);
        PropertyValuesHolder sy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.16f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.5f, 0f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(ring, sx, sy, alpha);
        animator.setDuration(2400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

    /** 头像 / 标志的弹性入场。 */
    public static void pop(View view, long delay) {
        if (view == null) {
            return;
        }
        if (!isAnimationEnabled(view.getContext())) {
            view.setScaleX(1f);
            view.setScaleY(1f);
            view.setAlpha(1f);
            return;
        }
        view.setScaleX(0.6f);
        view.setScaleY(0.6f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setStartDelay(delay)
                .setDuration(560)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
    }

    /**
     * 圆形扩散转场：以 origin 的中心为圆心，用遮罩色扩散覆盖当前页，扩散结束时执行 action。
     * 常用于「登录 → 主页」这种带仪式感的跳转。
     */
    public static void revealAndRun(final Activity activity, final View origin,
                                    final int color, final Runnable action) {
        if (activity == null || origin == null || !isAnimationEnabled(activity)) {
            action.run();
            return;
        }
        final ViewGroup root = activity.findViewById(android.R.id.content);
        if (root == null) {
            action.run();
            return;
        }

        final View overlay = new View(activity);
        overlay.setBackgroundColor(color);
        overlay.setClickable(true);
        root.addView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final int[] originPos = new int[2];
        origin.getLocationInWindow(originPos);
        final int[] rootPos = new int[2];
        root.getLocationInWindow(rootPos);
        final int cx = originPos[0] - rootPos[0] + origin.getWidth() / 2;
        final int cy = originPos[1] - rootPos[1] + origin.getHeight() / 2;

        overlay.post(new Runnable() {
            @Override
            public void run() {
                float radius = (float) Math.hypot(
                        Math.max(cx, root.getWidth() - cx),
                        Math.max(cy, root.getHeight() - cy));
                Animator animator = ViewAnimationUtils.createCircularReveal(
                        overlay, cx, cy, 0f, radius);
                animator.setDuration(430);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (activity.isFinishing()) {
                            return;
                        }
                        action.run();
                        // 若跳转后当前页仍存活（例如返回），延时移除遮罩，避免残留
                        overlay.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ViewGroup parent = (ViewGroup) overlay.getParent();
                                if (parent != null) {
                                    parent.removeView(overlay);
                                }
                            }
                        }, 800);
                    }
                });
                animator.start();
            }
        });
    }

    /** 把颜色按比例混合，用于 tab 文字颜色过渡。 */
    public static int blend(int from, int to, float fraction) {
        float f = Math.max(0f, Math.min(1f, fraction));
        int a = (int) (Color.alpha(from) + (Color.alpha(to) - Color.alpha(from)) * f);
        int r = (int) (Color.red(from) + (Color.red(to) - Color.red(from)) * f);
        int g = (int) (Color.green(from) + (Color.green(to) - Color.green(from)) * f);
        int b = (int) (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * f);
        return Color.argb(a, r, g, b);
    }
}
