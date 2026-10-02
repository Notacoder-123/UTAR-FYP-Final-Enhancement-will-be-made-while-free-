package com.example.final_yp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressTrackingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView currentWeightTextView, weightChangeTextView, weeklyAverageTextView;
    private TextView totalWorkoutsTextView, avgCaloriesTextView, streakTextView;
    private CardView weightProgressCard, fitnessStatsCard, viewChartsCard, bodyMeasurementsCard;
    private FloatingActionButton addProgressFab;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private List<ProgressModel> progressList;
    private UserModel currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_tracking);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if user is logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            finish();
            return;
        }

        currentUserId = firebaseUser.getUid();
        progressList = new ArrayList<>();

        // Initialize UI components
        initializeViews();
        setupClickListeners();

        // Load user data and progress
        loadUserData();
        loadProgressData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Progress Tracking");

        // Weight progress views
        currentWeightTextView = findViewById(R.id.current_weight_text);
        weightChangeTextView = findViewById(R.id.weight_change_text);
        weeklyAverageTextView = findViewById(R.id.weekly_average_text);

        // Fitness stats views
        totalWorkoutsTextView = findViewById(R.id.total_workouts_text);
        avgCaloriesTextView = findViewById(R.id.avg_calories_text);
        streakTextView = findViewById(R.id.streak_text);

        // Cards
        weightProgressCard = findViewById(R.id.weight_progress_card);
        fitnessStatsCard = findViewById(R.id.fitness_stats_card);
        viewChartsCard = findViewById(R.id.view_charts_card);
        bodyMeasurementsCard = findViewById(R.id.body_measurements_card);

        addProgressFab = findViewById(R.id.add_progress_fab);
    }

    private void setupClickListeners() {
        addProgressFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProgressTrackingActivity.this, AddProgressActivity.class));
            }
        });

        viewChartsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProgressTrackingActivity.this, ProgressChartActivity.class);
                intent.putExtra("progress_list", new ArrayList<>(progressList));
                startActivity(intent);
            }
        });

        bodyMeasurementsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProgressTrackingActivity.this, BodyMeasurementActivity.class));
            }
        });

        weightProgressCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProgressTrackingActivity.this, WeightHistoryActivity.class);
                intent.putExtra("progress_list", new ArrayList<>(progressList));
                startActivity(intent);
            }
        });
    }

    private void loadUserData() {
        mDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(UserModel.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ProgressTrackingActivity.this,
                                "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProgressData() {
        // Load progress entries for the last 30 days
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        long thirtyDaysAgo = calendar.getTimeInMillis();

        Query query = mDatabase.child("progress").child(currentUserId)
                .orderByChild("date")
                .startAt(thirtyDaysAgo);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ProgressModel progress = snapshot.getValue(ProgressModel.class);
                    if (progress != null) {
                        progressList.add(progress);
                    }
                }

                // Update UI with progress data
                updateProgressStats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProgressTrackingActivity.this,
                        "Failed to load progress data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProgressStats() {
        if (progressList.isEmpty()) {
            // Show empty state
            currentWeightTextView.setText("--");
            weightChangeTextView.setText("No data");
            weeklyAverageTextView.setText("--");
            totalWorkoutsTextView.setText("0");
            avgCaloriesTextView.setText("--");
            streakTextView.setText("0 days");
            return;
        }

        // Get latest entry
        ProgressModel latestEntry = progressList.get(progressList.size() - 1);

        // Update current weight
        currentWeightTextView.setText(String.format("%.1f kg", latestEntry.getWeight()));

        // Calculate weight change (last 7 days)
        float weightChange = calculateWeightChange();
        if (weightChange > 0) {
            weightChangeTextView.setText(String.format("+%.1f kg", weightChange));
            weightChangeTextView.setTextColor(getResources().getColor(
                    currentUser != null && "Weight Loss".equals(currentUser.getGoal())
                            ? R.color.error : R.color.success));
        } else if (weightChange < 0) {
            weightChangeTextView.setText(String.format("%.1f kg", weightChange));
            weightChangeTextView.setTextColor(getResources().getColor(
                    currentUser != null && "Weight Loss".equals(currentUser.getGoal())
                            ? R.color.success : R.color.error));
        } else {
            weightChangeTextView.setText("No change");
            weightChangeTextView.setTextColor(getResources().getColor(R.color.textSecondary));
        }

        // Calculate weekly average weight
        float weeklyAvg = calculateWeeklyAverageWeight();
        weeklyAverageTextView.setText(String.format("%.1f kg", weeklyAvg));

        // Calculate fitness stats
        int totalWorkouts = calculateTotalWorkouts();
        totalWorkoutsTextView.setText(String.valueOf(totalWorkouts));

        int avgCalories = calculateAverageCalories();
        avgCaloriesTextView.setText(avgCalories > 0 ? String.valueOf(avgCalories) : "--");

        int streak = calculateStreak();
        streakTextView.setText(streak + " days");
    }

    private float calculateWeightChange() {
        if (progressList.size() < 2) return 0;

        // Get entries from 7 days ago and today
        Calendar sevenDaysAgo = Calendar.getInstance();
        sevenDaysAgo.add(Calendar.DAY_OF_MONTH, -7);

        ProgressModel oldestInRange = null;
        ProgressModel newest = progressList.get(progressList.size() - 1);

        for (ProgressModel progress : progressList) {
            if (progress.getDate().getTime() >= sevenDaysAgo.getTimeInMillis()) {
                if (oldestInRange == null) {
                    oldestInRange = progress;
                }
                break;
            }
        }

        if (oldestInRange != null && !oldestInRange.equals(newest)) {
            return newest.getWeight() - oldestInRange.getWeight();
        }

        return 0;
    }

    private float calculateWeeklyAverageWeight() {
        Calendar sevenDaysAgo = Calendar.getInstance();
        sevenDaysAgo.add(Calendar.DAY_OF_MONTH, -7);

        float totalWeight = 0;
        int count = 0;

        for (ProgressModel progress : progressList) {
            if (progress.getDate().getTime() >= sevenDaysAgo.getTimeInMillis()) {
                totalWeight += progress.getWeight();
                count++;
            }
        }

        return count > 0 ? totalWeight / count : 0;
    }

    private int calculateTotalWorkouts() {
        int total = 0;
        for (ProgressModel progress : progressList) {
            total += progress.getWorkoutsCompleted();
        }
        return total;
    }

    private int calculateAverageCalories() {
        int totalCalories = 0;
        int count = 0;

        for (ProgressModel progress : progressList) {
            if (progress.getCaloriesConsumed() > 0) {
                totalCalories += progress.getCaloriesConsumed();
                count++;
            }
        }

        return count > 0 ? totalCalories / count : 0;
    }

    private int calculateStreak() {
        if (progressList.isEmpty()) return 0;

        // Sort by date descending
        List<ProgressModel> sortedList = new ArrayList<>(progressList);
        sortedList.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        int streak = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < sortedList.size(); i++) {
            Date progressDate = sortedList.get(i).getDate();
            Calendar progressCal = Calendar.getInstance();
            progressCal.setTime(progressDate);
            progressCal.set(Calendar.HOUR_OF_DAY, 0);
            progressCal.set(Calendar.MINUTE, 0);
            progressCal.set(Calendar.SECOND, 0);
            progressCal.set(Calendar.MILLISECOND, 0);

            if (i == 0) {
                // Check if the latest entry is today or yesterday
                long diffInMillis = calendar.getTimeInMillis() - progressCal.getTimeInMillis();
                long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                if (diffInDays > 1) {
                    // Streak is broken
                    break;
                }
                streak = 1;
                calendar.setTime(progressDate);
            } else {
                // Check if this entry is the day before the previous one
                calendar.add(Calendar.DAY_OF_MONTH, -1);

                if (progressCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        progressCal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) {
                    streak++;
                    calendar.setTime(progressDate);
                } else {
                    // Streak is broken
                    break;
                }
            }
        }

        return streak;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity
        loadProgressData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}