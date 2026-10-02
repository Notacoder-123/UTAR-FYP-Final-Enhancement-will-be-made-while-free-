package com.example.final_yp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProgressChartActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner timeRangeSpinner;
    private TextView weightChangeText, avgWeightText, minWeightText, maxWeightText;
    private TextView avgCaloriesText, totalWorkoutsText;

    private List<ProgressModel> progressList;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_charts);

        // Get progress list from intent
        progressList = (List<ProgressModel>) getIntent().getSerializableExtra("progress_list");
        if (progressList == null) {
            progressList = new ArrayList<>();
        }

        dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

        // Initialize views
        initializeViews();
        setupSpinner();

        // Set default time range to 30 days
        filterAndDisplayData(30);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Progress Charts");

        timeRangeSpinner = findViewById(R.id.time_range_spinner);

        // Weight stats
        weightChangeText = findViewById(R.id.weight_change_text);
        avgWeightText = findViewById(R.id.avg_weight_text);
        minWeightText = findViewById(R.id.min_weight_text);
        maxWeightText = findViewById(R.id.max_weight_text);

        // Fitness stats
        avgCaloriesText = findViewById(R.id.avg_calories_text);
        totalWorkoutsText = findViewById(R.id.total_workouts_text);

        // Hide the chart views since we're not using the MPAndroidChart library
        View weightChart = findViewById(R.id.weight_chart);
        View caloriesChart = findViewById(R.id.calories_chart);
        if (weightChart != null) weightChart.setVisibility(View.GONE);
        if (caloriesChart != null) caloriesChart.setVisibility(View.GONE);
    }

    private void setupSpinner() {
        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int days = 0;
                switch (position) {
                    case 0: days = 7; break;   // 1 week
                    case 1: days = 30; break;  // 1 month
                    case 2: days = 90; break;  // 3 months
                    case 3: days = 365; break; // 1 year
                }
                filterAndDisplayData(days);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void filterAndDisplayData(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        long startTime = calendar.getTimeInMillis();

        List<ProgressModel> filteredList = new ArrayList<>();
        for (ProgressModel progress : progressList) {
            if (progress.getDate().getTime() >= startTime) {
                filteredList.add(progress);
            }
        }

        // Sort by date
        Collections.sort(filteredList, (a, b) -> a.getDate().compareTo(b.getDate()));

        // Update statistics
        updateStatistics(filteredList);
    }

    private void updateStatistics(List<ProgressModel> data) {
        if (data.isEmpty()) {
            weightChangeText.setText("No data");
            avgWeightText.setText("--");
            minWeightText.setText("--");
            maxWeightText.setText("--");
            avgCaloriesText.setText("--");
            totalWorkoutsText.setText("0");
            return;
        }

        // Weight statistics
        float startWeight = data.get(0).getWeight();
        float endWeight = data.get(data.size() - 1).getWeight();
        float weightChange = endWeight - startWeight;

        float totalWeight = 0;
        float minWeight = Float.MAX_VALUE;
        float maxWeight = Float.MIN_VALUE;

        for (ProgressModel progress : data) {
            float weight = progress.getWeight();
            totalWeight += weight;
            minWeight = Math.min(minWeight, weight);
            maxWeight = Math.max(maxWeight, weight);
        }

        float avgWeight = totalWeight / data.size();

        // Display weight stats
        if (weightChange > 0) {
            weightChangeText.setText(String.format("+%.1f kg", weightChange));
            weightChangeText.setTextColor(getResources().getColor(R.color.error));
        } else if (weightChange < 0) {
            weightChangeText.setText(String.format("%.1f kg", weightChange));
            weightChangeText.setTextColor(getResources().getColor(R.color.success));
        } else {
            weightChangeText.setText("No change");
            weightChangeText.setTextColor(getResources().getColor(R.color.textSecondary));
        }

        avgWeightText.setText(String.format("%.1f kg", avgWeight));
        minWeightText.setText(String.format("%.1f kg", minWeight));
        maxWeightText.setText(String.format("%.1f kg", maxWeight));

        // Calories and workout statistics
        int totalCalories = 0;
        int totalWorkouts = 0;
        int daysWithCalories = 0;

        for (ProgressModel progress : data) {
            if (progress.getCaloriesConsumed() > 0) {
                totalCalories += progress.getCaloriesConsumed();
                daysWithCalories++;
            }
            totalWorkouts += progress.getWorkoutsCompleted();
        }

        int avgCalories = daysWithCalories > 0 ? totalCalories / daysWithCalories : 0;

        avgCaloriesText.setText(avgCalories > 0 ? String.valueOf(avgCalories) : "--");
        totalWorkoutsText.setText(String.valueOf(totalWorkouts));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}