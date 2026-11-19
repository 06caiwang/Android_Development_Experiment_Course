package com.example.game;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 游戏主界面（演示用的“假”界面）。
 * 本页面只负责展示，不包含真实游戏逻辑。
 */
public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        findViewById(R.id.btnBattle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GameActivity.this, "坦克已就位，战斗即将开始…", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
