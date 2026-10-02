package com.example.final_yp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView workoutRecyclerView;
    private TextView emptyView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private WorkoutAdapter workoutAdapter;
    private List<WorkoutModel> workoutList;
    private String currentLevel = "beginner"; // Default filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Workouts");

        tabLayout = findViewById(R.id.tab_layout);
        workoutRecyclerView = findViewById(R.id.workout_recycler_view);
        emptyView = findViewById(R.id.empty_view);

        // Set up RecyclerView
        workoutList = new ArrayList<>();
        workoutAdapter = new WorkoutAdapter(this, workoutList);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutRecyclerView.setAdapter(workoutAdapter);

        // Set up item click listener
        workoutAdapter.setOnItemClickListener(new WorkoutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WorkoutModel workout) {
                // Navigate to workout details screen
                Intent intent = new Intent(WorkoutDashboardActivity.this, WorkoutDetailActivity.class);
                intent.putExtra("workout_id", workout.getWorkoutId());
                startActivity(intent);
            }
        });

        // Set up tab listener for filtering workouts by level
        setupTabLayout();

        // Check and add sample data if database is empty
        checkAndAddSampleData();

        // Load workouts from Firebase
        loadWorkouts();
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.beginner));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.intermediate));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.advanced));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentLevel = "beginner";
                        break;
                    case 1:
                        currentLevel = "intermediate";
                        break;
                    case 2:
                        currentLevel = "advanced";
                        break;
                }
                loadWorkouts(); // Reload workouts with new filter
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

    private void checkAndAddSampleData() {
        mDatabase.child("workouts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    // Database is empty, add simple sample data
                    Toast.makeText(WorkoutDashboardActivity.this,
                            "Adding sample workouts...", Toast.LENGTH_LONG).show();
                    addSimpleWorkouts();
                }
                else {
                    // Data already exists, just log it
                    Log.d("WorkoutDebug", "Workout data already exists: " + snapshot.getChildrenCount() + " workouts");
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("WorkoutDebug", "Error checking database: " + error.getMessage());
            }
        });
    }

    private void loadWorkouts() {
        Log.d("WorkoutDebug", "Loading workouts for level: " + currentLevel);

        mDatabase.child("workouts").orderByChild("level").equalTo(currentLevel)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("WorkoutDebug", "Found " + dataSnapshot.getChildrenCount() + " workouts");
                        workoutList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                            WorkoutModel workout = snapshot.getValue(WorkoutModel.class);
                            if (workout != null) {
                                Log.d("WorkoutDebug", "Adding: " + workout.getName());
                                workoutList.add(workout);
                            }
                            }catch (Exception e){
                                Log.e("WorkoutDebug", "Error parsing workout: " + e.getMessage());
                            }
                        }

                        workoutAdapter.notifyDataSetChanged();

                        // Show empty view if no workouts found
                        if (workoutList.isEmpty()) {
                            workoutRecyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            workoutRecyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("WorkoutDebug", "Error: " + databaseError.getMessage());
                        //Toast.makeText(WorkoutDashboardActivity.this,
                                //"Error loading workouts: " + databaseError.getMessage(),
                                //Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Simple method to add basic workouts for school project
    private void addSimpleWorkouts() {
        // Add 3 beginner workouts
        WorkoutModel w1 = new WorkoutModel("w1", "Easy Start",
                "Perfect for beginners", "full body", "beginner", "", 15);
        mDatabase.child("workouts").child("w1").setValue(w1);

        WorkoutModel w2 = new WorkoutModel("w2", "Morning Stretch",
                "Light morning exercises", "stretching", "beginner", "", 20);
        mDatabase.child("workouts").child("w2").setValue(w2);

        WorkoutModel w3 = new WorkoutModel("w3", "Basic Strength",
                "Build foundation", "strength", "beginner", "", 25);
        mDatabase.child("workouts").child("w3").setValue(w3);

        // Add 3 intermediate workouts
        WorkoutModel w4 = new WorkoutModel("w4", "Cardio Blast",
                "Get your heart pumping", "cardio", "intermediate", "", 30);
        mDatabase.child("workouts").child("w4").setValue(w4);

        WorkoutModel w5 = new WorkoutModel("w5", "Core Power",
                "Strengthen your core", "core", "intermediate", "", 25);
        mDatabase.child("workouts").child("w5").setValue(w5);

        WorkoutModel w6 = new WorkoutModel("w6", "HIIT Session",
                "High intensity training", "hiit", "intermediate", "", 35);
        mDatabase.child("workouts").child("w6").setValue(w6);

        // Add 3 advanced workouts
        WorkoutModel w7 = new WorkoutModel("w7", "Beast Mode",
                "Maximum intensity", "full body", "advanced", "", 45);
        mDatabase.child("workouts").child("w7").setValue(w7);

        WorkoutModel w8 = new WorkoutModel("w8", "Power Training",
                "Build explosive power", "power", "advanced", "", 50);
        mDatabase.child("workouts").child("w8").setValue(w8);

        WorkoutModel w9 = new WorkoutModel("w9", "Endurance Challenge",
                "Test your limits", "endurance", "advanced", "", 60);
        mDatabase.child("workouts").child("w9").setValue(w9);

        // Add basic exercises
        addBasicExercises();
    }

    private void addBasicExercises() {
        ExerciseModel e1 = new ExerciseModel("e1", "Push-ups", "Basic push-ups",
                "chest", "", "", "1. Start in plank\n2. Lower down\n3. Push up",
                3, 10, 60, true);
        mDatabase.child("exercises").child("e1").setValue(e1);

        ExerciseModel e2 = new ExerciseModel("e2", "Squats", "Basic squats",
                "legs", "", "", "1. Stand straight\n2. Squat down\n3. Stand up",
                3, 15, 60, true);
        mDatabase.child("exercises").child("e2").setValue(e2);

        ExerciseModel e3 = new ExerciseModel("e3", "Plank", "Core hold",
                "core", "", "", "1. Get in position\n2. Hold steady\n3. Keep breathing",
                3, 30, 45, true);
        mDatabase.child("exercises").child("e3").setValue(e3);

        ExerciseModel e4 = new ExerciseModel("e4", "Lunges", "Basic lunges",
                "legs", "", "", "1. Step forward\n2. Lower down\n3. Push back",
                3, 10, 60, true);
        mDatabase.child("exercises").child("e4").setValue(e4);

        ExerciseModel e5 = new ExerciseModel("e5", "Jumping Jacks", "Cardio exercise",
                "cardio", "", "", "1. Start standing\n2. Jump and spread\n3. Jump back",
                3, 20, 45, true);
        mDatabase.child("exercises").child("e5").setValue(e5);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}