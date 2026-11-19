package com.example.loginandregister.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.loginandregister.R;

/**
 * 顶部分段切换标签：文字 + 底部滑动指示器，切换时指示器平滑位移、文字颜色渐变。
 */
public class SegmentedTabs extends FrameLayout {

    public interface OnTabSelectedListener {
        void onTabSelected(int index);
    }

    private final LinearLayout row = new LinearLayout(getContext());
    private final View indicator = new View(getContext());
    private final int colorActive;
    private final int colorIdle;
    private final Typeface medium = Typeface.create("sans-serif-medium", Typeface.NORMAL);

    private String[] labels = new String[0];
    private int selected = 0;
    private int tabWidth = 0;
    private OnTabSelectedListener listener;
    private ValueAnimator colorAnimator;

    public SegmentedTabs(Context context) {
        this(context, null);
    }

    public SegmentedTabs(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        colorActive = ContextCompat.getColor(context, R.color.accent);
        colorIdle = ContextCompat.getColor(context, R.color.text_tertiary);

        row.setOrientation(LinearLayout.HORIZONTAL);
        addView(row, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        indicator.setBackgroundResource(R.drawable.bg_tab_indicator);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                0, (int) dp(2.5f), Gravity.BOTTOM | Gravity.START);
        addView(indicator, lp);
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    public void setLabels(String... items) {
        labels = items == null ? new String[0] : items;
        row.removeAllViews();
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            TextView tab = new TextView(getContext());
            tab.setText(labels[i]);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tab.setGravity(Gravity.CENTER);
            tab.setTextColor(i == selected ? colorActive : colorIdle);
            tab.setTypeface(i == selected ? medium : Typeface.DEFAULT);
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    select(index);
                }
            });
            row.addView(tab, new LinearLayout.LayoutParams(
                    0, LayoutParams.MATCH_PARENT, 1f));
        }
        tabWidth = 0;
        requestLayout();
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public int getSelectedIndex() {
        return selected;
    }

    public String getSelectedLabel() {
        return selected < labels.length ? labels[selected] : "";
    }

    public void select(int index) {
        if (index < 0 || index >= labels.length || index == selected) {
            return;
        }
        animateLabelColor(selected, index);
        selected = index;

        if (tabWidth > 0) {
            indicator.animate()
                    .translationX((float) index * tabWidth)
                    .setDuration(280)
                    .setInterpolator(new DecelerateInterpolator(1.8f))
                    .start();
        }
        if (listener != null) {
            listener.onTabSelected(index);
        }
    }

    private void animateLabelColor(int from, int to) {
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
        if (from < 0 || from >= row.getChildCount() || to < 0 || to >= row.getChildCount()) {
            return;
        }
        final TextView oldTab = (TextView) row.getChildAt(from);
        final TextView newTab = (TextView) row.getChildAt(to);
        newTab.setTypeface(medium);
        oldTab.setTypeface(Typeface.DEFAULT);

        final int startOld = colorActive;
        final int endOld = colorIdle;
        final int startNew = colorIdle;
        final int endNew = colorActive;

        colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(240);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                oldTab.setTextColor(blend(startOld, endOld, f));
                newTab.setTextColor(blend(startNew, endNew, f));
            }
        });
        colorAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (labels.length == 0) {
            return;
        }
        tabWidth = w / labels.length;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) indicator.getLayoutParams();
        lp.width = tabWidth;
        indicator.setLayoutParams(lp);
        indicator.setTranslationX((float) selected * tabWidth);
    }

    private static int blend(int from, int to, float fraction) {
        float f = Math.max(0f, Math.min(1f, fraction));
        int a = (int) (android.graphics.Color.alpha(from)
                + (android.graphics.Color.alpha(to) - android.graphics.Color.alpha(from)) * f);
        int r = (int) (android.graphics.Color.red(from)
                + (android.graphics.Color.red(to) - android.graphics.Color.red(from)) * f);
        int g = (int) (android.graphics.Color.green(from)
                + (android.graphics.Color.green(to) - android.graphics.Color.green(from)) * f);
        int b = (int) (android.graphics.Color.blue(from)
                + (android.graphics.Color.blue(to) - android.graphics.Color.blue(from)) * f);
        return android.graphics.Color.argb(a, r, g, b);
    }
}
