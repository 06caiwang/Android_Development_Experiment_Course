package com.example.daily_new;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 新闻详情页：标题、来源信息、配图与正文。
 */
public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_SOURCE = "extra_source";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_COMMENT = "extra_comment";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_IMAGE = "extra_image";
    public static final String EXTRA_VIDEO = "extra_video";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String source = getIntent().getStringExtra(EXTRA_SOURCE);
        String time = getIntent().getStringExtra(EXTRA_TIME);
        String comment = getIntent().getStringExtra(EXTRA_COMMENT);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);
        int imageRes = getIntent().getIntExtra(EXTRA_IMAGE, R.drawable.news_01);
        boolean video = getIntent().getBooleanExtra(EXTRA_VIDEO, false);

        ((TextView) findViewById(R.id.tv_detail_title)).setText(title);
        ((TextView) findViewById(R.id.tv_detail_source)).setText(source);
        ((TextView) findViewById(R.id.tv_detail_time)).setText(time);
        ((TextView) findViewById(R.id.tv_detail_comment)).setText(comment);
        ((TextView) findViewById(R.id.tv_detail_content)).setText(content);
        ((ImageView) findViewById(R.id.iv_detail_image)).setImageResource(imageRes);

        // 视频类新闻在详情配图上叠加播放按钮
        findViewById(R.id.iv_detail_play).setVisibility(video ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}
