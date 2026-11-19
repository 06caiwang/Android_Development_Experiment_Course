package com.example.list;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);

        List<ListItem> data = new ArrayList<>();
        // 已替换为你实验要求的 6 个列表项及对应系统图标
        data.add(new ListItem(android.R.drawable.ic_menu_add, "添加"));
        data.add(new ListItem(android.R.drawable.ic_menu_delete, "删除"));
        data.add(new ListItem(android.R.drawable.ic_menu_edit, "编辑"));
        data.add(new ListItem(android.R.drawable.ic_menu_search, "搜索"));
        data.add(new ListItem(android.R.drawable.ic_menu_info_details, "详情"));
        data.add(new ListItem(android.R.drawable.ic_menu_share, "分享"));

        IconListAdapter adapter = new IconListAdapter(this, data);
        listView.setAdapter(adapter);
    }
}