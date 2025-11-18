package com.example.stocktool;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    // 输入组件
    private TextInputEditText oldNumberInput;
    private TextInputEditText oldPriceInput;
    private TextInputEditText newPriceInput;
    private TextInputEditText newNumberInput;
    
    // 结果显示组件
    private TextView taxExclusiveResult;
    private TextView taxInclusiveResult;
    private TextView newTotalResult;
    private View resultLayout;
    
    private Button calculateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图组件
        initializeViews();
        
        // 设置计算按钮点击事件
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAveragingCost();
            }
        });
    }

    private void initializeViews() {
        // 输入组件
        oldNumberInput = findViewById(R.id.oldNumberInput);
        oldPriceInput = findViewById(R.id.oldPriceInput);
        newNumberInput = findViewById(R.id.newNumberInput);
        newPriceInput = findViewById(R.id.newPriceInput);
        
        // 结果显示组件
        taxExclusiveResult = findViewById(R.id.taxExclusiveResult);
        taxInclusiveResult = findViewById(R.id.taxInclusiveResult);
        newTotalResult = findViewById(R.id.newTotalResult);
        resultLayout = findViewById(R.id.resultLayout);
        
        // 按钮
        calculateButton = findViewById(R.id.calculateButton);
    }

    private void calculateAveragingCost() {
        try {
            // 获取输入数据
            String oldNumberStr = oldNumberInput.getText().toString().trim();
            String oldPriceStr = oldPriceInput.getText().toString().trim();
            String newPriceStr = newPriceInput.getText().toString().trim();
            String newNumberStr = newNumberInput.getText().toString().trim();

            // 检查输入是否为空
            if (oldNumberStr.isEmpty() || oldPriceStr.isEmpty() || 
                newPriceStr.isEmpty() || newNumberStr.isEmpty()) {
                Toast.makeText(this, "请填写所有输入项", Toast.LENGTH_SHORT).show();
                return;
            }

            // 转换为数字
            int oldNumber = Integer.parseInt(oldNumberStr);
            double oldPrice = Double.parseDouble(oldPriceStr);
            double newPrice = Double.parseDouble(newPriceStr);
            int newNumber = Integer.parseInt(newNumberStr);

            // 检查输入是否为正数
            if (oldNumber <= 0 || oldPrice <= 0 || newPrice <= 0 || newNumber <= 0) {
                Toast.makeText(this, "所有数值必须为正数", Toast.LENGTH_SHORT).show();
                return;
            }

            // 计算补仓成本
            double oldTotal = oldNumber * oldPrice;
            double newTotal = newNumber * newPrice;
            int totalShares = oldNumber + newNumber;
            double totalCost = oldTotal + newTotal;
            double totalTaxCost = oldTotal + newTotal ;
            if(newTotal > 50000){
                totalTaxCost += newTotal * 1.0005 ;
            }else {
                totalTaxCost += 5;
            }
            // 不含税成本价
            double taxExclusivePrice = totalCost / totalShares;
            double taxInclusivePrice = totalTaxCost / totalShares;

            // 显示结果
            taxExclusiveResult.setText(String.format("¥%.2f", taxExclusivePrice));
            taxInclusiveResult.setText(String.format("¥%.2f", taxInclusivePrice));
            newTotalResult.setText(String.format("新增总价(不含税): ¥%.2f", newTotal));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
        }
    }
}