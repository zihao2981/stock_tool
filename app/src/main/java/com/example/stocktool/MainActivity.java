package com.example.stocktool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private CardView averagingCostCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图组件
        initializeViews();
        
        // 设置卡片点击事件
        setupClickListeners();
    }

    private void initializeViews() {
        // 获取卡片视图
        averagingCostCard = findViewById(R.id.averagingCostCard);
    }

    private void setupClickListeners() {
        // 设置补仓计算器卡片点击事件
        averagingCostCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到补仓成本计算器页面
                Intent intent = new Intent(MainActivity.this, AveragingCostActivity.class);
                startActivity(intent);
            }
        });
    }
}