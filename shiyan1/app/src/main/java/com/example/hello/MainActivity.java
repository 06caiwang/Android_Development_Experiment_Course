package com.example.hello;

import android.os.Bundle;
import android.widget.TextView; // 需要导入这个包
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 放在这里：在布局加载完毕之后
        TextView textView = findViewById(R.id.textView);
        textView.setText("你好");
    }
}