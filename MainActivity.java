package com.example.final_yp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.widget.Button;
import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView userNameTextView, caloriesRemainingTextView;
    private CardView workoutCardView, mealPlannerCardView, calorieCalculatorCardView, profileCardView, progressCardView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private UserModel currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://final-fyp1-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDatabase = database.getReference();

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        userNameTextView = findViewById(R.id.user_name_text_view);
        caloriesRemainingTextView = findViewById(R.id.calories_remaining_text_view);
        workoutCardView = findViewById(R.id.workout_card_view);
        mealPlannerCardView = findViewById(R.id.meal_planner_card_view);
        calorieCalculatorCardView = findViewById(R.id.calorie_calculator_card_view);
        profileCardView = findViewById(R.id.profile_card_view);
        progressCardView = findViewById(R.id.progress_card_view);

        // Set up navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Set up card view click listeners
        workoutCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WorkoutDashboardActivity.class));
            }
        });

        mealPlannerCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MealPlannerActivity.class));
            }
        });

        calorieCalculatorCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CaloriesCalculatorActivity.class));
            }
        });

        profileCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
            }
        });

        progressCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProgressTrackingActivity.class));
            }


        });

        // Check if user is logged in
        checkUserAuthentication();
        // Add this line to populate workout data
        populateFirebaseWithWorkoutData();

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("Firebase", "Connected to Firebase");
                } else {
                    Log.d("Firebase", "Not connected to Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Connection check failed: " + error);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data whenever coming back to MainActivity
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        }
    }

    private void checkUserAuthentication() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("Auth", "User is logged in: " + user.getEmail());
        } else {
            Log.d("Auth", "User is not logged in");
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, redirect to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            // User is logged in, load user data
            loadUserData();
        }
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            currentUser = dataSnapshot.getValue(UserModel.class);

                            if (currentUser != null) {
                                // Update UI with user data
                                updateUI();
                            } else {
                                // Profile incomplete, redirect to profile setup
                                startActivity(new Intent(MainActivity.this, ProfileSetupActivity.class));
                            }
                        } else {
                            // Profile doesn't exist, redirect to profile setup
                            startActivity(new Intent(MainActivity.this, ProfileSetupActivity.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Failed to load user data: " +
                                databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (currentUser != null) {
            // Update welcome message
            userNameTextView.setText(getString(R.string.welcome) + " " + currentUser.getName());

            // Calculate and display calories remaining
            int calorieTarget = currentUser.calculateCalorieTarget();
            caloriesRemainingTextView.setText(String.valueOf(calorieTarget));

            // Update navigation drawer header with user info
            View headerView = navigationView.getHeaderView(0);
            TextView navUserName = headerView.findViewById(R.id.nav_header_name);
            TextView navUserEmail = headerView.findViewById(R.id.nav_header_email);

            if (navUserName != null && navUserEmail != null) {
                navUserName.setText(currentUser.getName());
                navUserEmail.setText(currentUser.getEmail());
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item clicks
        int id = item.getItemId();

        if (id == R.id.nav_workouts) {
            startActivity(new Intent(MainActivity.this, WorkoutDashboardActivity.class));
        } else if (id == R.id.nav_meal_planner) {
            startActivity(new Intent(MainActivity.this, MealPlannerActivity.class));
        } else if (id == R.id.nav_progress) {
            startActivity(new Intent(MainActivity.this, ProgressTrackingActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
        } else if (id == R.id.nav_ai_assistant) {
            startActivity(new Intent(MainActivity.this, AIAssistantActivity.class));
        } else if (id == R.id.nav_settings) {
            // TODO: Implement Settings activity in Phase 2
            Toast.makeText(this, "Settings will be available in the next update", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {
        // Sign out from Firebase
        mAuth.signOut();

        // Redirect to LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Close drawer if open, otherwise handle back as usual
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void populateFirebaseWithWorkoutData() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Check if data already exists
        database.child("workouts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    // No workouts exist, populate the database
                    addAllWorkoutsAndExercises();
                    Toast.makeText(MainActivity.this, "Populating workout data...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Workout data already exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error checking database: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAllWorkoutsAndExercises() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // First, add all exercises
        addAllExercises(database);

        // Then add all workouts with their exercises
        addBeginnerWorkouts(database);
        addIntermediateWorkouts(database);
        addAdvancedWorkouts(database);

        Toast.makeText(this, "Workout data added successfully!", Toast.LENGTH_LONG).show();
    }

    private void addAllExercises(DatabaseReference database) {
        // Basic Push-up variations
        database.child("exercises").child("e1").setValue(new ExerciseModel("e1", "Standard Push-ups",
                "Classic upper body exercise", "chest", "", "",
                "1. Start in plank position\n2. Lower chest to floor\n3. Push back up\n4. Keep core tight",
                3, 10, 60, true));

        database.child("exercises").child("e2").setValue(new ExerciseModel("e2", "Knee Push-ups",
                "Beginner-friendly push-up variation", "chest", "", "",
                "1. Start on knees\n2. Lower chest to floor\n3. Push back up\n4. Keep back straight",
                3, 12, 45, true));

        database.child("exercises").child("e3").setValue(new ExerciseModel("e3", "Diamond Push-ups",
                "Advanced tricep-focused push-ups", "triceps", "", "",
                "1. Form diamond with hands\n2. Lower chest to hands\n3. Push back up\n4. Focus on triceps",
                3, 8, 75, true));

        // Squat variations
        database.child("exercises").child("e4").setValue(new ExerciseModel("e4", "Bodyweight Squats",
                "Fundamental lower body exercise", "legs", "", "",
                "1. Feet shoulder-width apart\n2. Lower hips back and down\n3. Go to parallel\n4. Stand back up",
                3, 15, 45, true));

        database.child("exercises").child("e5").setValue(new ExerciseModel("e5", "Jump Squats",
                "Explosive power exercise", "legs", "", "",
                "1. Perform regular squat\n2. Explode up into jump\n3. Land softly\n4. Immediately repeat",
                3, 10, 60, true));

        // Core exercises
        database.child("exercises").child("e6").setValue(new ExerciseModel("e6", "Plank",
                "Core stabilization", "core", "", "",
                "1. Forearms on ground\n2. Body straight line\n3. Hold position\n4. Breathe normally",
                3, 30, 30, true));

        database.child("exercises").child("e7").setValue(new ExerciseModel("e7", "Mountain Climbers",
                "Dynamic core and cardio", "core", "", "",
                "1. Start in plank\n2. Drive knees to chest\n3. Alternate quickly\n4. Keep hips level",
                3, 20, 45, true));

        database.child("exercises").child("e8").setValue(new ExerciseModel("e8", "Bicycle Crunches",
                "Oblique-focused ab exercise", "core", "", "",
                "1. Lie on back\n2. Bring elbow to opposite knee\n3. Alternate sides\n4. Keep lower back pressed",
                3, 20, 45, true));

        // Lunge variations
        database.child("exercises").child("e9").setValue(new ExerciseModel("e9", "Forward Lunges",
                "Basic lunge pattern", "legs", "", "",
                "1. Step forward\n2. Lower back knee\n3. Push back to start\n4. Alternate legs",
                3, 12, 60, true));

        database.child("exercises").child("e10").setValue(new ExerciseModel("e10", "Reverse Lunges",
                "Backward lunge variation", "legs", "", "",
                "1. Step backward\n2. Lower back knee\n3. Push forward to start\n4. Alternate legs",
                3, 12, 60, true));

        // Add more exercises as needed...
    }

    private void addBeginnerWorkouts(DatabaseReference database) {
        // Workout 1: Full Body Basics
        WorkoutModel workout1 = new WorkoutModel("w1", "Full Body Basics",
                "A comprehensive workout targeting all major muscle groups. Perfect for beginners.",
                "full body", "beginner", "", 30);

        // Add exercises to workout1
        workout1.addExercise(new ExerciseModel("e2", "Knee Push-ups", "Modified push-ups", "chest",
                "", "", "", 3, 10, 60, true));
        workout1.addExercise(new ExerciseModel("e4", "Bodyweight Squats", "Basic squats", "legs",
                "", "", "", 3, 12, 60, true));
        workout1.addExercise(new ExerciseModel("e6", "Plank", "Core hold", "core",
                "", "", "", 3, 20, 45, true));
        workout1.addExercise(new ExerciseModel("e9", "Forward Lunges", "Alternating lunges", "legs",
                "", "", "", 3, 10, 60, true));

        database.child("workouts").child("w1").setValue(workout1);

        // Workout 2: Upper Body Strength
        WorkoutModel workout2 = new WorkoutModel("w2", "Upper Body Strength",
                "Focus on building strength in your chest, shoulders, and arms.",
                "upper body", "beginner", "", 25);

        workout2.addExercise(new ExerciseModel("e2", "Knee Push-ups", "Chest exercise", "chest",
                "", "", "", 3, 12, 60, true));
        workout2.addExercise(new ExerciseModel("e1", "Standard Push-ups", "If able", "chest",
                "", "", "", 2, 8, 75, true));

        database.child("workouts").child("w2").setValue(workout2);

        // Workout 3: Lower Body Power
        WorkoutModel workout3 = new WorkoutModel("w3", "Lower Body Power",
                "Build strong legs and glutes with this beginner-friendly routine.",
                "lower body", "beginner", "", 20);

        workout3.addExercise(new ExerciseModel("e4", "Bodyweight Squats", "Foundation movement", "legs",
                "", "", "", 3, 15, 60, true));
        workout3.addExercise(new ExerciseModel("e9", "Forward Lunges", "Alternating legs", "legs",
                "", "", "", 3, 10, 60, true));
        workout3.addExercise(new ExerciseModel("e10", "Reverse Lunges", "Backward lunges", "legs",
                "", "", "", 3, 10, 60, true));

        database.child("workouts").child("w3").setValue(workout3);

        // Workout 4: Core Essentials
        WorkoutModel workout4 = new WorkoutModel("w4", "Core Essentials",
                "Develop core strength and stability with simple, effective exercises.",
                "core", "beginner", "", 15);

        workout4.addExercise(new ExerciseModel("e6", "Plank", "30 second holds", "core",
                "", "", "", 3, 30, 45, true));
        workout4.addExercise(new ExerciseModel("e8", "Bicycle Crunches", "Controlled movement", "core",
                "", "", "", 2, 15, 45, true));
        workout4.addExercise(new ExerciseModel("e7", "Mountain Climbers", "Slow pace", "core",
                "", "", "", 2, 15, 60, true));

        database.child("workouts").child("w4").setValue(workout4);

        // Workout 5: Cardio Starter
        WorkoutModel workout5 = new WorkoutModel("w5", "Cardio Starter",
                "A gentle introduction to cardio training to build endurance.",
                "cardio", "beginner", "", 20);

        workout5.addExercise(new ExerciseModel("e7", "Mountain Climbers", "Moderate pace", "core",
                "", "", "", 3, 20, 60, true));
        workout5.addExercise(new ExerciseModel("e5", "Jump Squats", "Low intensity", "legs",
                "", "", "", 2, 8, 75, true));

        database.child("workouts").child("w5").setValue(workout5);
    }

    private void addIntermediateWorkouts(DatabaseReference database) {
        // Workout 6: Intermediate Full Body
        WorkoutModel workout6 = new WorkoutModel("w6", "Intermediate Full Body",
                "Step up your game with this challenging full body workout.",
                "full body", "intermediate", "", 45);

        workout6.addExercise(new ExerciseModel("e1", "Standard Push-ups", "Full range", "chest",
                "", "", "", 4, 15, 60, true));
        workout6.addExercise(new ExerciseModel("e5", "Jump Squats", "Explosive movement", "legs",
                "", "", "", 4, 12, 75, true));
        workout6.addExercise(new ExerciseModel("e6", "Plank", "45 second holds", "core",
                "", "", "", 3, 45, 60, true));
        workout6.addExercise(new ExerciseModel("e10", "Reverse Lunges", "Alternating", "legs",
                "", "", "", 3, 12, 60, true));

        database.child("workouts").child("w6").setValue(workout6);

        // Add more intermediate workouts...
    }

    private void addAdvancedWorkouts(DatabaseReference database) {
        // Workout 11: Elite Full Body
        WorkoutModel workout11 = new WorkoutModel("w11", "Elite Full Body",
                "Maximum intensity workout for experienced athletes.",
                "full body", "advanced", "", 60);

        workout11.addExercise(new ExerciseModel("e3", "Diamond Push-ups", "To failure", "triceps",
                "", "", "", 4, 15, 75, true));
        workout11.addExercise(new ExerciseModel("e5", "Jump Squats", "Maximum height", "legs",
                "", "", "", 5, 15, 90, true));
        workout11.addExercise(new ExerciseModel("e1", "Standard Push-ups", "To failure", "chest",
                "", "", "", 4, 20, 60, true));

        database.child("workouts").child("w11").setValue(workout11);

        // Add more advanced workouts...
    }

}