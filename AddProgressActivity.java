package com.example.final_yp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddProgressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView dateTextView;
    private EditText weightEditText, bodyFatEditText, notesEditText;
    private TextView caloriesConsumedTextView, workoutsCompletedTextView;
    private Button saveButton, selectDateButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private Date selectedDate;
    private SimpleDateFormat dateFormat;
    private int caloriesConsumed = 0;
    private int workoutsCompleted = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_progress);

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
        dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());

        // Initialize views
        initializeViews();

        // Set default date to today
        selectedDate = new Date();
        dateTextView.setText(dateFormat.format(selectedDate));

        // Load today's data from other sources
        loadTodaysData();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Progress");

        dateTextView = findViewById(R.id.date_text_view);
        weightEditText = findViewById(R.id.weight_edit_text);
        bodyFatEditText = findViewById(R.id.body_fat_edit_text);
        notesEditText = findViewById(R.id.notes_edit_text);
        caloriesConsumedTextView = findViewById(R.id.calories_consumed_text);
        workoutsCompletedTextView = findViewById(R.id.workouts_completed_text);
        saveButton = findViewById(R.id.save_button);
        selectDateButton = findViewById(R.id.select_date_button);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProgress();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);
                        selectedDate = selected.getTime();
                        dateTextView.setText(dateFormat.format(selectedDate));

                        // Reload data for the selected date
                        loadTodaysData();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Don't allow future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void loadTodaysData() {
        // Load calories consumed from food log
        loadCaloriesFromFoodLog();

        // Load workouts completed (this would need to be implemented when workout tracking is added)
        // For now, we'll leave it at 0 or allow manual entry
    }

    private void loadCaloriesFromFoodLog() {
        SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateKey = logDateFormat.format(selectedDate);
        String logId = currentUserId + "_" + dateKey;

        mDatabase.child("food_logs").child(logId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            FoodLogModel foodLog = dataSnapshot.getValue(FoodLogModel.class);
                            if (foodLog != null) {
                                NutritionModel dailyNutrition = foodLog.calculateDailyNutrition();
                                caloriesConsumed = dailyNutrition.getCalories();
                                caloriesConsumedTextView.setText(String.valueOf(caloriesConsumed));
                            }
                        } else {
                            caloriesConsumed = 0;
                            caloriesConsumedTextView.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });

        // Also check if there's already a progress entry for this date
        checkExistingProgress();
    }

    private void checkExistingProgress() {
        SimpleDateFormat idFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String progressId = currentUserId + "_" + idFormat.format(selectedDate);

        mDatabase.child("progress").child(currentUserId).child(progressId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            ProgressModel existingProgress = dataSnapshot.getValue(ProgressModel.class);
                            if (existingProgress != null) {
                                // Pre-fill the form with existing data
                                weightEditText.setText(String.valueOf(existingProgress.getWeight()));
                                if (existingProgress.getBodyFat() > 0) {
                                    bodyFatEditText.setText(String.valueOf(existingProgress.getBodyFat()));
                                }
                                if (existingProgress.getNotes() != null) {
                                    notesEditText.setText(existingProgress.getNotes());
                                }
                                workoutsCompleted = existingProgress.getWorkoutsCompleted();
                                workoutsCompletedTextView.setText(String.valueOf(workoutsCompleted));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void saveProgress() {
        // Validate weight input
        String weightStr = weightEditText.getText().toString().trim();
        if (TextUtils.isEmpty(weightStr)) {
            weightEditText.setError("Weight is required");
            weightEditText.requestFocus();
            return;
        }

        float weight;
        try {
            weight = Float.parseFloat(weightStr);
            if (weight < 30 || weight > 300) {
                weightEditText.setError("Weight must be between 30 and 300 kg");
                weightEditText.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            weightEditText.setError("Invalid weight");
            weightEditText.requestFocus();
            return;
        }

        // Body fat is optional
        float bodyFat = 0;
        String bodyFatStr = bodyFatEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(bodyFatStr)) {
            try {
                bodyFat = Float.parseFloat(bodyFatStr);
                if (bodyFat < 0 || bodyFat > 70) {
                    bodyFatEditText.setError("Body fat must be between 0 and 70%");
                    bodyFatEditText.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                bodyFatEditText.setError("Invalid body fat percentage");
                bodyFatEditText.requestFocus();
                return;
            }
        }

        // Get notes
        String notes = notesEditText.getText().toString().trim();

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Create progress entry
        SimpleDateFormat idFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String progressId = currentUserId + "_" + idFormat.format(selectedDate);

        ProgressModel progress = new ProgressModel(progressId, currentUserId, selectedDate, weight);
        progress.setBodyFat(bodyFat);
        progress.setNotes(notes);
        progress.setCaloriesConsumed(caloriesConsumed);
        progress.setWorkoutsCompleted(workoutsCompleted);

        // Save to Firebase
        mDatabase.child("progress").child(currentUserId).child(progressId).setValue(progress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Also update the user's current weight
                            updateUserCurrentWeight(weight);

                            Toast.makeText(AddProgressActivity.this,
                                    "Progress saved successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddProgressActivity.this,
                                    "Failed to save progress", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserCurrentWeight(float weight) {
        mDatabase.child("users").child(currentUserId).child("weight").setValue(weight);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}