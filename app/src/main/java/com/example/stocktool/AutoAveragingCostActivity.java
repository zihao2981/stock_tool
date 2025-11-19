package com.example.stocktool;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AutoAveragingCostActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AutoAveragingCostPrefs";
    private static final String KEY_OLD_PRICE = "old_price";
    private static final String KEY_OLD_QUANTITY = "old_quantity";
    private static final String KEY_START_QUANTITY = "start_quantity";
    private static final String KEY_END_QUANTITY = "end_quantity";
    private static final String KEY_INTERVAL = "interval";
    
    private SharedPreferences sharedPreferences;

    private TextInputEditText oldPriceInput, oldQuantityInput;
    private android.widget.LinearLayout pricesContainer;
    private android.widget.Button addPriceButton;
    private java.util.List<EditText> newPrices = new java.util.ArrayList<>();
    private TextInputEditText startQuantityInput, endQuantityInput, intervalInput;
    private Button calculateButton;
    private Button btnHighlightWithoutCommission, btnHighlightWithCommission;
    private TableLayout resultTableCombined;
    
    // 用于跟踪高亮状态的变量
    private boolean isWithoutCommissionHighlighted = false;
    private boolean isWithCommissionHighlighted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_averaging_cost);
        
        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // 启用ActionBar返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("自动补仓计算器");
        }

        initViews();
        setupClickListener();
        
        // 加载保存的数据
        loadSavedData();
    }

    private void initViews() {
        oldPriceInput = findViewById(R.id.oldPriceInput);
        oldQuantityInput = findViewById(R.id.oldQuantityInput);
        
        // 初始化动态价格列表组件
        pricesContainer = findViewById(R.id.pricesContainer);
        addPriceButton = findViewById(R.id.addPriceButton);
        
        // 初始化数量范围输入框
        startQuantityInput = findViewById(R.id.startQuantityInput);
        endQuantityInput = findViewById(R.id.endQuantityInput);
        intervalInput = findViewById(R.id.intervalInput);
        
        calculateButton = findViewById(R.id.calculateButton);
        btnHighlightWithoutCommission = findViewById(R.id.btnHighlightWithoutCommission);
        btnHighlightWithCommission = findViewById(R.id.btnHighlightWithCommission);
        resultTableCombined = findViewById(R.id.resultTableCombined);
        
        // 添加初始的价格输入框
        addPriceInput();
    }

    private void setupClickListener() {
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateCosts();
            }
        });
        
        btnHighlightWithoutCommission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleWithoutCommissionHighlight();
            }
        });
        
        btnHighlightWithCommission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleWithCommissionHighlight();
            }
        });
        
        addPriceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPriceInput();
            }
        });
    }
    
    private void loadSavedData() {
        // 从 SharedPreferences 中获取保存的数据
        String savedOldPrice = sharedPreferences.getString(KEY_OLD_PRICE, "");
        String savedOldQuantity = sharedPreferences.getString(KEY_OLD_QUANTITY, "");
        String savedStartQuantity = sharedPreferences.getString(KEY_START_QUANTITY, "");
        String savedEndQuantity = sharedPreferences.getString(KEY_END_QUANTITY, "");
        String savedInterval = sharedPreferences.getString(KEY_INTERVAL, "");
        
        // 将数据填充到对应的输入框中
        if (!savedOldPrice.isEmpty()) {
            oldPriceInput.setText(savedOldPrice);
        }
        
        if (!savedOldQuantity.isEmpty()) {
            oldQuantityInput.setText(savedOldQuantity);
        }
        
        if (!savedStartQuantity.isEmpty()) {
            startQuantityInput.setText(savedStartQuantity);
        }
        
        if (!savedEndQuantity.isEmpty()) {
            endQuantityInput.setText(savedEndQuantity);
        }
        
        if (!savedInterval.isEmpty()) {
            intervalInput.setText(savedInterval);
        }
    }
    
    // 处理ActionBar返回按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 关闭当前Activity，返回到主页面
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private List<Integer> generateQuantityList(int start, int end, int interval) {
        List<Integer> quantities = new ArrayList<>();
        for (int i = start; i <= end; i += interval) {
            quantities.add(i);
        }
        return quantities;
    }
    
    private void calculateCosts() {
        String oldPriceStr = oldPriceInput.getText().toString().trim();
        String oldQuantityStr = oldQuantityInput.getText().toString().trim();
        String startQuantityStr = startQuantityInput.getText().toString().trim();
        String endQuantityStr = endQuantityInput.getText().toString().trim();
        String intervalStr = intervalInput.getText().toString().trim();
        
        // 验证必要信息
        if (oldPriceStr.isEmpty() || oldQuantityStr.isEmpty() || 
            startQuantityStr.isEmpty() || endQuantityStr.isEmpty()) {
            Toast.makeText(this, "请输入原有持仓信息和数量范围", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            double oldPrice = Double.parseDouble(oldPriceStr);
            int oldQuantity = Integer.parseInt(oldQuantityStr);
            int startQuantity = Integer.parseInt(startQuantityStr);
            int endQuantity = Integer.parseInt(endQuantityStr);
            int interval = intervalStr.isEmpty() ? 100 : Integer.parseInt(intervalStr);
            
            // 验证输入值必须为正数
            if (oldPrice <= 0 || oldQuantity <= 0 || startQuantity <= 0 || endQuantity <= 0 || interval <= 0) {
                Toast.makeText(this, "所有输入值必须为正数", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 验证起始值小于等于结束值
            if (startQuantity > endQuantity) {
                Toast.makeText(this, "起始数量不能大于结束数量", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 收集所有有效的新购价格
            List<Double> validPrices = new ArrayList<>();
            for (int i = 0; i < newPrices.size(); i++) {
                String priceStr = newPrices.get(i).getText().toString().trim();
                if (!priceStr.isEmpty()) {
                    double price = Double.parseDouble(priceStr);
                    if (price > 0) {
                        validPrices.add(price);
                    }
                }
            }
            
            // 检查是否有有效价格
            if (validPrices.isEmpty()) {
                Toast.makeText(this, "请至少输入一个有效的新购价格", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 清空之前的结果表格
            resultTableCombined.removeAllViews();
            
            // 生成数量列表
            List<Integer> quantities = generateQuantityList(startQuantity, endQuantity, interval);
            
            // 显示生成的信息
            Toast.makeText(this, "生成 " + validPrices.size() + " 个价格 × " + quantities.size() + " 个数量 的组合", Toast.LENGTH_SHORT).show();
            
            // 创建合并后的笛卡尔积表头
            createCombinedCartesianTableHeader(validPrices);

            // 计算原有持仓总价值
            double oldTotalValue = oldPrice * oldQuantity;
            int totalQuantity = oldQuantity;
            
            // 逐行生成笛卡尔积表格数据
            for (int quantity : quantities) {
                // 创建合并后的表格行
                TableRow row = new TableRow(this);
                
                // 移除TableRow的边框
                // row.setBackgroundColor(ContextCompat.getColor(this, R.color.table_border));
                
                // 设置TableRow的布局参数
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
                // 移除外边距以去除边框效果
                // rowParams.setMargins(2, 2, 2, 2);
                row.setLayoutParams(rowParams);
                
                // 添加数量列
                TextView quantityText = new TextView(this);
                quantityText.setText(String.valueOf(quantity));
                quantityText.setPadding(8, 8, 8, 8);
                // 设置最小宽度确保可见
                quantityText.setMinimumWidth(80);
                // 设置字体颜色为白色
                quantityText.setTextColor(ContextCompat.getColor(this, R.color.text_white));
                // 添加内边距以增强边框效果
                android.graphics.drawable.GradientDrawable quantityBackground = new android.graphics.drawable.GradientDrawable();
                quantityBackground.setColor(ContextCompat.getColor(this, R.color.table_cell_background));
                // quantityBackground.setStroke(2, ContextCompat.getColor(this, R.color.table_border)); // 移除边框宽度和颜色
                quantityBackground.setCornerRadius(4f); // 保留圆角
                quantityText.setBackground(quantityBackground);
                row.addView(quantityText);
                
                // 计算每个价格对应的平均成本
                for (double price : validPrices) {
                    // 计算新增价值
                    double newTotalValue = price * quantity;
                    
                    // 更新总数量和总价值
                    int currentTotalQuantity = totalQuantity + quantity;
                    double currentTotalValue = oldTotalValue + newTotalValue;
                    
                    // 计算不含佣金的平均成本
                    double avgCostWithoutCommission = currentTotalValue / currentTotalQuantity;
                    if(currentTotalValue > 50000){
                        currentTotalValue += currentTotalValue * 1.0005 ;
                    }else {
                        currentTotalValue += 5;
                    }
                    double avgCostWithCommission = currentTotalValue / currentTotalQuantity;
                    
                    // 创建包含两个结果的垂直线性布局
                    android.widget.LinearLayout cellLayout = new android.widget.LinearLayout(this);
                    cellLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
                    cellLayout.setPadding(8, 8, 8, 8);
                    // 为单元格添加背景（移除边框）
                    android.graphics.drawable.GradientDrawable cellBackground = new android.graphics.drawable.GradientDrawable();
                    // 检查计算结果是否有效
                    if (Double.isNaN(avgCostWithoutCommission) || Double.isInfinite(avgCostWithoutCommission) ||
                        Double.isNaN(avgCostWithCommission) || Double.isInfinite(avgCostWithCommission)) {
                        // 如果结果无效，则使用透明背景
                        cellBackground.setColor(ContextCompat.getColor(this, R.color.transparent));
                    } else {
                        // 否则使用默认背景
                        cellBackground.setColor(ContextCompat.getColor(this, R.color.table_cell_background));
                    }
                    // cellBackground.setStroke(2, ContextCompat.getColor(this, R.color.table_border)); // 移除边框宽度和颜色
                    cellBackground.setCornerRadius(4f); // 保留圆角
                    cellLayout.setBackground(cellBackground);
                    
                    // 上半部分：不含佣金结果
                    TextView costTextWithout = new TextView(this);
                    costTextWithout.setText(String.format("%.3f", avgCostWithoutCommission));
                    costTextWithout.setPadding(0, 0, 0, 4); // 底部留一些间距
                    costTextWithout.setGravity(android.view.Gravity.CENTER);
                    // 设置字体颜色为橙色
                    costTextWithout.setTextColor(ContextCompat.getColor(this, R.color.tax_exclusive_text));
                    cellLayout.addView(costTextWithout);
                    
                    // 下半部分：含佣金结果
                    TextView costTextWith = new TextView(this);
                    costTextWith.setText(String.format("%.3f", avgCostWithCommission));
                    costTextWith.setPadding(0, 4, 0, 0); // 顶部留一些间距
                    costTextWith.setGravity(android.view.Gravity.CENTER);
                    // 设置字体颜色为橙色
                    costTextWith.setTextColor(ContextCompat.getColor(this, R.color.tax_exclusive_text));
                    cellLayout.addView(costTextWith);
                    
                    // 将垂直布局添加到表格行中
                    row.addView(cellLayout);
                }
                
                // 添加行到表格
                resultTableCombined.addView(row);
            }
            
            // 更新表格高亮状态
            updateTableHighlight();
            
            // 保存数据到 SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_OLD_PRICE, oldPriceStr);
            editor.putString(KEY_OLD_QUANTITY, oldQuantityStr);
            editor.putString(KEY_START_QUANTITY, startQuantityStr);
            editor.putString(KEY_END_QUANTITY, endQuantityStr);
            editor.putString(KEY_INTERVAL, intervalStr);
            editor.apply();
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void toggleWithoutCommissionHighlight() {
        isWithoutCommissionHighlighted = !isWithoutCommissionHighlighted;
        
        // 更新按钮文本
        if (isWithoutCommissionHighlighted) {
            btnHighlightWithoutCommission.setText("凸显不含佣价格");
        } else {
            btnHighlightWithoutCommission.setText("凸显不含佣价格");
        }
        
        // 如果启用了不含佣金高亮，则禁用含佣金高亮
        if (isWithoutCommissionHighlighted && isWithCommissionHighlighted) {
            isWithCommissionHighlighted = false;
            btnHighlightWithCommission.setText("凸显含佣价格");
        }
        
        // 更新表格显示
        updateTableHighlight();
    }
    
    private void toggleWithCommissionHighlight() {
        isWithCommissionHighlighted = !isWithCommissionHighlighted;
        
        // 更新按钮文本
        if (isWithCommissionHighlighted) {
            btnHighlightWithCommission.setText("凸显含佣价格");
        } else {
            btnHighlightWithCommission.setText("凸显含佣价格");
        }
        
        // 如果启用了含佣金高亮，则禁用不含佣金高亮
        if (isWithCommissionHighlighted && isWithoutCommissionHighlighted) {
            isWithoutCommissionHighlighted = false;
            btnHighlightWithoutCommission.setText("凸显不含佣价格");
        }
        
        // 更新表格显示
        updateTableHighlight();
    }
    
    private void updateTableHighlight() {
        // 遍历表格的所有行（跳过标题行）
        for (int i = 2; i < resultTableCombined.getChildCount(); i++) {
            View view = resultTableCombined.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                
                // 遍历行中的所有单元格（跳过数量列）
                for (int j = 1; j < row.getChildCount(); j++) {
                    View cellView = row.getChildAt(j);
                    if (cellView instanceof android.widget.LinearLayout) {
                        android.widget.LinearLayout cellLayout = (android.widget.LinearLayout) cellView;
                        
                        // 获取不含佣金和含佣金的TextView
                        if (cellLayout.getChildCount() >= 2) {
                            TextView withoutCommissionText = (TextView) cellLayout.getChildAt(0);
                            TextView withCommissionText = (TextView) cellLayout.getChildAt(1);
                            
                            // 根据高亮状态设置颜色
                            if (isWithoutCommissionHighlighted) {
                                // 高亮不含佣金价格（设为黄色）
                                withoutCommissionText.setTextColor(ContextCompat.getColor(this, R.color.tax_exclusive_text));
                                // 恢复含佣金价格颜色
                                withCommissionText.setTextColor(ContextCompat.getColor(this, R.color.text_white));
                            } else if (isWithCommissionHighlighted) {
                                // 恢复不含佣金价格颜色
                                withoutCommissionText.setTextColor(ContextCompat.getColor(this, R.color.text_white));
                                // 高亮含佣金价格（设为黄色）
                                withCommissionText.setTextColor(ContextCompat.getColor(this, R.color.tax_exclusive_text));
                            } else {
                                // 恢复默认颜色
                                withoutCommissionText.setTextColor(ContextCompat.getColor(this, R.color.text_white));
                                withCommissionText.setTextColor(ContextCompat.getColor(this, R.color.text_white));
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void addPriceInput() {
        // 创建水平线性布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setPadding(0, 8, 0, 8);
        
        // 直接创建EditText而不是TextInputLayout
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(0, 0, 16, 0);
        editText.setLayoutParams(params);
        editText.setTextColor(ContextCompat.getColor(this, R.color.text_white));
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.text_white));
        editText.setHint("新购价格 " + (newPrices.size() + 1));
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        // 将EditText添加到水平线性布局
        layout.addView(editText);
        
        // 将水平线性布局添加到pricesContainer
        pricesContainer.addView(layout);
        
        // 将新的输入框添加到列表中
        newPrices.add(editText);
    }
    
    private void createCombinedCartesianTableHeader(List<Double> prices) {
        // 添加标题行
        TableRow titleRow = new TableRow(this);
        
        // 移除TableRow的边框
        // titleRow.setBackgroundColor(ContextCompat.getColor(this, R.color.table_border));
        
        // 设置TableRow的布局参数
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT);
        // 移除外边距以去除边框效果
        // rowParams.setMargins(2, 2, 2, 2);
        titleRow.setLayoutParams(rowParams);
        
        TextView titleText = new TextView(this);
        titleText.setText("计算结果");
        titleText.setTextSize(18);
        titleText.setTextColor(ContextCompat.getColor(this, R.color.text_white));
        titleText.setPadding(16, 16, 16, 16);
        // 移除标题文本的边框效果
        android.graphics.drawable.GradientDrawable titleBackground = new android.graphics.drawable.GradientDrawable();
        titleBackground.setColor(ContextCompat.getColor(this, R.color.table_cell_background));
        // titleBackground.setStroke(2, ContextCompat.getColor(this, R.color.table_border)); // 移除边框宽度和颜色
        titleBackground.setCornerRadius(4f); // 保留圆角
        titleText.setBackground(titleBackground);
        titleRow.addView(titleText);
        
        // 占满整行
        TableRow.LayoutParams params = (TableRow.LayoutParams) titleText.getLayoutParams();
        params.span = prices.size() + 3; // 数量列 + 所有价格列
        titleText.setLayoutParams(params);
        
        resultTableCombined.addView(titleRow);
        
        // 添加表头行
        TableRow headerRow = new TableRow(this);
        // 移除TableRow的边框
        // headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.table_border));
        
        // 设置TableRow的布局参数
        TableRow.LayoutParams headerRowParams = new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT);
        // 移除外边距以去除边框效果
        // headerRowParams.setMargins(2, 2, 2, 2);
        headerRow.setLayoutParams(headerRowParams);
        
        // 第一列是数量列
        TextView quantityHeader = new TextView(this);
        quantityHeader.setText("数量\\价格");
        quantityHeader.setTextSize(14);
        quantityHeader.setPadding(8, 8, 8, 8);
        // 设置最小宽度确保可见
        quantityHeader.setMinimumWidth(80);
        // 设置文本加粗
        quantityHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        // 设置字体颜色为白色
        quantityHeader.setTextColor(ContextCompat.getColor(this, R.color.text_white));
        // 为数量列标题添加背景以创建边框效果
        android.graphics.drawable.GradientDrawable quantityHeaderBackground = new android.graphics.drawable.GradientDrawable();
        quantityHeaderBackground.setColor(ContextCompat.getColor(this, R.color.table_cell_background));
        // quantityHeaderBackground.setStroke(2, ContextCompat.getColor(this, R.color.table_border)); // 移除边框宽度和颜色
        quantityHeaderBackground.setCornerRadius(4f); // 保留圆角
        quantityHeader.setBackground(quantityHeaderBackground);
        headerRow.addView(quantityHeader);
        
        // 为每个价格添加列标题
        for (double price : prices) {
            TextView priceHeader = new TextView(this);
            priceHeader.setText(String.format("%.2f", price));
            priceHeader.setTextSize(14);
            priceHeader.setPadding(8, 8, 8, 8);
            // 设置最小宽度确保可见
            priceHeader.setMinimumWidth(100);
            // 设置文本居中和加粗
            priceHeader.setGravity(android.view.Gravity.CENTER);
            priceHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            // 设置字体颜色为白色
            priceHeader.setTextColor(ContextCompat.getColor(this, R.color.text_white));
            // 为价格列标题添加背景以创建边框效果
            android.graphics.drawable.GradientDrawable priceHeaderBackground = new android.graphics.drawable.GradientDrawable();
            priceHeaderBackground.setColor(ContextCompat.getColor(this, R.color.table_cell_background));
            // priceHeaderBackground.setStroke(2, ContextCompat.getColor(this, R.color.table_border)); // 移除边框宽度和颜色
            priceHeaderBackground.setCornerRadius(4f); // 保留圆角
            priceHeader.setBackground(priceHeaderBackground);
            headerRow.addView(priceHeader);
        }
        
        resultTableCombined.addView(headerRow);
    }
}
