package com.example.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

/**
 * 游戏条款确认页（核心页面）。
 * 两个勾选项全部勾选后，“同意并开始”按钮才可点击，点击后跳转到游戏主界面。
 */
public class TermsActivity extends AppCompatActivity {

    private MaterialCheckBox cbAgree;
    private MaterialCheckBox cbAge;
    private MaterialButton btnAgree;
    private TextView tvHint;

    /** 两个勾选框共用同一个监听器 */
    private final CompoundButton.OnCheckedChangeListener checkListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    refreshConfirmState();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        cbAgree = findViewById(R.id.cbAgree);
        cbAge = findViewById(R.id.cbAge);
        btnAgree = findViewById(R.id.btnAgree);
        tvHint = findViewById(R.id.tvHint);

        cbAgree.setOnCheckedChangeListener(checkListener);
        cbAge.setOnCheckedChangeListener(checkListener);

        // 返回上一页
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 拒绝并退出：不做任何同意记录，直接回到开始界面
        findViewById(R.id.btnReject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TermsActivity.this, "您未同意协议，无法进入游戏", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // 同意并开始：进入游戏主界面
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TermsActivity.this, GameActivity.class));
                finish();
            }
        });

        refreshConfirmState();
    }

    /** 根据两个勾选框的状态刷新按钮可用性与提示文案 */
    private void refreshConfirmState() {
        boolean confirmed = cbAgree.isChecked() && cbAge.isChecked();

        btnAgree.setEnabled(confirmed);
        if (confirmed) {
            tvHint.setText(R.string.terms_hint_ready);
            tvHint.setTextColor(ContextCompat.getColor(this, R.color.cyan));
        } else {
            tvHint.setText(R.string.terms_hint_wait);
            tvHint.setTextColor(ContextCompat.getColor(this, R.color.amber));
        }
    }
}
