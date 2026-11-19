package com.example.daily_new;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页：顶部品牌栏 + 频道栏 + 新闻列表。
 * 全部为本地死数据，仅做界面展示。
 */
public class MainActivity extends AppCompatActivity {

    private static final String[] TABS = {"推荐", "视频", "图文", "热点", "娱乐"};

    private final List<View> tabViews = new ArrayList<>();
    private List<News> allNews;
    private NewsAdapter adapter;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allNews = NewsRepository.getAll();
        ListView lvNews = findViewById(R.id.lv_news);
        adapter = new NewsAdapter(this, allNews);
        lvNews.setAdapter(adapter);

        // 点击列表项进入详情页，把当前新闻的数据带过去
        lvNews.setOnItemClickListener((parent, view, position, id) -> {
            News news = adapter.getItem(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_TITLE, news.getTitle());
            intent.putExtra(DetailActivity.EXTRA_SOURCE, news.getSource());
            intent.putExtra(DetailActivity.EXTRA_TIME, news.getTime());
            intent.putExtra(DetailActivity.EXTRA_COMMENT, news.getComment());
            intent.putExtra(DetailActivity.EXTRA_CONTENT, news.getContent());
            intent.putExtra(DetailActivity.EXTRA_IMAGE, news.getImageResId());
            intent.putExtra(DetailActivity.EXTRA_VIDEO, news.isVideo());
            startActivity(intent);
        });

        setupTabs();
        selectTab(0);
    }

    /** 用 item_tab 布局动态生成频道栏 */
    private void setupTabs() {
        LinearLayout container = findViewById(R.id.tab_container);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < TABS.length; i++) {
            View tab = inflater.inflate(R.layout.item_tab, container, false);
            TextView title = tab.findViewById(R.id.tv_tab);
            title.setText(TABS[i]);

            final int index = i;
            tab.setOnClickListener(v -> selectTab(index));

            container.addView(tab);
            tabViews.add(tab);
        }
    }

    /** 切换频道：更新文字颜色与下划线，并按分类过滤列表 */
    private void selectTab(int index) {
        currentTab = index;

        for (int i = 0; i < tabViews.size(); i++) {
            TextView title = tabViews.get(i).findViewById(R.id.tv_tab);
            View indicator = tabViews.get(i).findViewById(R.id.v_indicator);
            boolean selected = (i == index);

            title.setTextColor(getColor(selected ? R.color.brand_red : R.color.text_regular));
            title.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            indicator.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        updateList();
    }

    private void updateList() {
        String tab = TABS[currentTab];
        if (currentTab == 0) {
            adapter.setData(allNews);
            return;
        }

        List<News> filtered = new ArrayList<>();
        for (News news : allNews) {
            if (tab.equals(news.getCategory())) {
                filtered.add(news);
            }
        }
        adapter.setData(filtered);
    }
}
