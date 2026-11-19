package com.example.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 游戏开始界面（启动页）。
 * 点击“开始游戏”先跳转到条款确认页 {@link TermsActivity}，
 * 只有用户阅读并勾选确认条款后，才会真正进入游戏。
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        playEnterAnimation();

        // 开始游戏 -> 先进入条款确认页
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TermsActivity.class));
            }
        });

        // 关于我们
        findViewById(R.id.btnAbout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "《坦克大战》v1.0.0\n课程演示版 · 仅用于界面练习",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 退出游戏
        findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
    }

    /** 启动时的入场动画：整体淡入并向上轻移 */
    private void playEnterAnimation() {
        View content = findViewById(R.id.splashContent);
        float offset = 48 * getResources().getDisplayMetrics().density;
        content.setAlpha(0f);
        content.setTranslationY(offset);
        content.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
