package com.example.final_yp;


import android.content.Intent;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Bundle;

public class MealPlannerActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private TabLayout mealTabLayout;
    private TextView dateTextView, caloriesRemainingTextView;
    private TextView totalCaloriesTextView, totalProteinTextView, totalCarbsTextView, totalFatTextView;
    private RecyclerView foodRecyclerView;
    private Button addFoodButton;
    private FloatingActionButton calculatorFab;
    private CardView nutritionSummaryCard;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private UserModel currentUser;
    private FoodLogModel todaysFoodLog;  // Changed from FoodModel to FoodLogModel
    private List<FoodLogModel.FoodEntry> currentMealEntries;
    private FoodEntryAdapter foodEntryAdapter;
    private String currentMeal = "breakfast"; // Default tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planner);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if user is logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            // Not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUserId = firebaseUser.getUid();

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.meal_planner);

        mealTabLayout = findViewById(R.id.meal_tab_layout);
        dateTextView = findViewById(R.id.date_text_view);
        caloriesRemainingTextView = findViewById(R.id.calories_remaining_text_view);
        totalCaloriesTextView = findViewById(R.id.total_calories_text_view);
        totalProteinTextView = findViewById(R.id.total_protein_text_view);
        totalCarbsTextView = findViewById(R.id.total_carbs_text_view);
        totalFatTextView = findViewById(R.id.total_fat_text_view);
        foodRecyclerView = findViewById(R.id.food_recycler_view);
        addFoodButton = findViewById(R.id.add_food_button);
        calculatorFab = findViewById(R.id.calculator_fab);
        nutritionSummaryCard = findViewById(R.id.nutrition_summary_card);

        // Set today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(new Date()));

        // Set up RecyclerView
        currentMealEntries = new ArrayList<>();
        foodEntryAdapter = new FoodEntryAdapter(this, currentMealEntries);
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodRecyclerView.setAdapter(foodEntryAdapter);

        // Delete food entry listener
        foodEntryAdapter.setOnDeleteClickListener(new FoodEntryAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                removeFoodEntry(position);
            }
        });

        // Add food button click listener
        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MealPlannerActivity.this, FoodSearchActivity.class);
                intent.putExtra("meal", currentMeal);
                startActivity(intent);
            }
        });

        // Calculator FAB click listener
        calculatorFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MealPlannerActivity.this, CaloriesCalculatorActivity.class));
            }
        });

        // Set up meal tab listener
        setupMealTabs();

        // Load user data and food log
        loadUserData();
    }

    private void setupMealTabs() {
        // Add meal tabs
        mealTabLayout.addTab(mealTabLayout.newTab().setText(R.string.breakfast));
        mealTabLayout.addTab(mealTabLayout.newTab().setText(R.string.lunch));
        mealTabLayout.addTab(mealTabLayout.newTab().setText(R.string.dinner));
        mealTabLayout.addTab(mealTabLayout.newTab().setText(R.string.snacks));

        // Set tab listener
        mealTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentMeal = "breakfast";
                        break;
                    case 1:
                        currentMeal = "lunch";
                        break;
                    case 2:
                        currentMeal = "dinner";
                        break;
                    case 3:
                        currentMeal = "snacks";
                        break;
                }
                updateMealDisplay();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void loadUserData() {
        mDatabase.child("users").child(currentUserId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(UserModel.class);
                        if (currentUser != null) {
                            // Update calories remaining
                            updateCaloriesRemaining();

                            // Load today's food log
                            loadTodaysFoodLog();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MealPlannerActivity.this, "Failed to load user data: " +
                                databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTodaysFoodLog() {
        // Get today's date in format YYYY-MM-DD for the log ID
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final String today = dateFormat.format(new Date());
        final String logId = currentUserId + "_" + today;

        mDatabase.child("food_logs").child(logId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            todaysFoodLog = dataSnapshot.getValue(FoodLogModel.class);
                        } else {
                            // Create a new food log for today
                            todaysFoodLog = new FoodLogModel(logId, currentUserId, new Date());
                            // Save the new log to Firebase
                            mDatabase.child("food_logs").child(logId).setValue(todaysFoodLog);
                        }

                        // Update UI with food log data
                        updateMealDisplay();
                        updateNutritionSummary();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MealPlannerActivity.this, "Failed to load food log: " +
                                databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateMealDisplay() {
        // Clear current entries
        currentMealEntries.clear();

        if (todaysFoodLog != null && todaysFoodLog.getMealEntries() != null
                && todaysFoodLog.getMealEntries().get(currentMeal) != null) {
            // Add food entries for current meal
            currentMealEntries.addAll(todaysFoodLog.getMealEntries().get(currentMeal));
        }

        // Update adapter
        foodEntryAdapter.notifyDataSetChanged();

        // Show empty view if needed
        if (currentMealEntries.isEmpty()) {
            foodRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        } else {
            foodRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.empty_view).setVisibility(View.GONE);
        }
    }

    private void updateNutritionSummary() {
        if (todaysFoodLog != null) {
            // Calculate total nutrition for the day
            NutritionModel dailyNutrition = todaysFoodLog.calculateDailyNutrition();

            // Update UI
            totalCaloriesTextView.setText(String.valueOf(dailyNutrition.getCalories()));
            totalProteinTextView.setText(String.format("%.1f g", dailyNutrition.getProtein()));
            totalCarbsTextView.setText(String.format("%.1f g", dailyNutrition.getCarbs()));
            totalFatTextView.setText(String.format("%.1f g", dailyNutrition.getFat()));

            // Update calories remaining
            updateCaloriesRemaining();
        }
    }

    private void updateCaloriesRemaining() {
        if (currentUser != null && todaysFoodLog != null) {
            // Calculate daily calorie target from user profile
            int calorieTarget = currentUser.calculateCalorieTarget();

            // Calculate calories consumed today
            int caloriesConsumed = todaysFoodLog.calculateDailyNutrition().getCalories();

            // Calculate calories remaining
            int caloriesRemaining = calorieTarget - caloriesConsumed;

            // Update UI
            caloriesRemainingTextView.setText(String.valueOf(caloriesRemaining));
        }
    }

    private void removeFoodEntry(int position) {
        if (todaysFoodLog != null && position >= 0 && position < currentMealEntries.size()) {
            // Get the entry to remove
            FoodLogModel.FoodEntry entryToRemove = currentMealEntries.get(position);

            // Remove from list
            currentMealEntries.remove(position);

            // Update the food log
            todaysFoodLog.getMealEntries().get(currentMeal).remove(entryToRemove);

            // Save changes to Firebase
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            final String today = dateFormat.format(new Date());
            final String logId = currentUserId + "_" + today;

            mDatabase.child("food_logs").child(logId).setValue(todaysFoodLog);

            // Update UI
            foodEntryAdapter.notifyDataSetChanged();
            updateNutritionSummary();

            // Show empty view if needed
            if (currentMealEntries.isEmpty()) {
                foodRecyclerView.setVisibility(View.GONE);
                findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh food log data when returning to the activity
        loadTodaysFoodLog();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}