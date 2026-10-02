package com.example.final_yp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView workoutImageView;
    private TextView workoutNameTextView, workoutDescriptionTextView;
    private TextView levelTextView, categoryTextView, timeTextView;
    private RecyclerView exercisesRecyclerView;
    private Button startWorkoutButton;

    private DatabaseReference mDatabase;
    private String workoutId;
    private WorkoutModel workout;
    private ExerciseAdapter exerciseAdapter;
    private List<ExerciseModel> exerciseList;
    private static final String TAG = "WorkoutDashboard";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, "Debug: Checking for workouts...", Snackbar.LENGTH_LONG).show();

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://final-fyp1-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDatabase = database.getReference();

        // Get workout ID from intent
        workoutId = getIntent().getStringExtra("workout_id");
        if (workoutId == null) {
            Toast.makeText(this, "Workout not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Workout Details");

        workoutImageView = findViewById(R.id.workout_image);
        workoutNameTextView = findViewById(R.id.workout_name);
        workoutDescriptionTextView = findViewById(R.id.workout_description);
        levelTextView = findViewById(R.id.workout_level);
        categoryTextView = findViewById(R.id.workout_category);
        timeTextView = findViewById(R.id.workout_time);
        exercisesRecyclerView = findViewById(R.id.exercises_recycler_view);
        startWorkoutButton = findViewById(R.id.start_workout_button);

        // Set up RecyclerView
        exerciseList = new ArrayList<>();
        exerciseAdapter = new ExerciseAdapter(this, exerciseList);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(exerciseAdapter);

        // Set item click listener
        exerciseAdapter.setOnItemClickListener(new ExerciseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExerciseModel exercise) {
                // Open exercise details
                Intent intent = new Intent(WorkoutDetailActivity.this, ExerciseDetailActivity.class);
                intent.putExtra("exercise_id", exercise.getExerciseId());
                startActivity(intent);
            }
        });

        // Set start workout button listener
        startWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For Phase 1, just show a toast message
                // In future phases, this would navigate to workout tracker
                Toast.makeText(WorkoutDetailActivity.this,
                        "Workout tracking will be available in the next update",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Load workout data
        loadWorkoutData();
    }

    private void loadWorkoutData() {
        mDatabase.child("workouts").child(workoutId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                workout = dataSnapshot.getValue(WorkoutModel.class);

                if (workout != null) {
                    // Update UI with workout data
                    updateUI();

                    // Load exercises for this workout
                    loadExercisesForWorkout();
                } else {
                    Toast.makeText(WorkoutDetailActivity.this, "Workout not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(WorkoutDetailActivity.this,
                        "Failed to load workout: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI() {
        // Set workout details
        workoutNameTextView.setText(workout.getName());
        workoutDescriptionTextView.setText(workout.getDescription());
        levelTextView.setText(workout.getLevel());
        categoryTextView.setText(workout.getCategory());
        timeTextView.setText(workout.getEstimatedTimeMinutes() + " " + getString(R.string.minutes));

        // Load image if URL is available
        if (workout.getImageUrl() != null && !workout.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(workout.getImageUrl())
                    .placeholder(R.drawable.ic_workout)
                    .error(R.drawable.ic_workout)
                    .centerCrop()
                    .into(workoutImageView);
        } else {
            workoutImageView.setImageResource(R.drawable.ic_workout);
        }
    }

    private void loadExercisesForWorkout() {
        // In a real app, exercises would be linked to workouts in the database
        // For Phase 1, we'll use a simplified approach and load all exercises
        // In future phases, this would be replaced with a proper query

        mDatabase.child("exercises").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                exerciseList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ExerciseModel exercise = snapshot.getValue(ExerciseModel.class);
                    if (exercise != null) {
                        exerciseList.add(exercise);
                    }
                }

                exerciseAdapter.notifyDataSetChanged();

                // Add sample exercises if none exist (for testing)
                if (exerciseList.isEmpty()) {
                    addSampleExercisesToFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Method to add sample data to Firebase (for testing)
    private void addSampleExercisesToFirebase() {
        // Sample exercises
        ExerciseModel exercise1 = new ExerciseModel(
                "e1",
                "Push-ups",
                "Basic upper body exercise",
                "chest",
                "https://www.google.com/search?q=workout&sca_esv=b5b85f1110a5fb0d&source=hp&biw=1280&bih=551&ei=c8QbaNzzIL7vseMPzvq1sA0&iflsig=ACkRmUkAAAAAaBvSg-ilft-g8xeqIj4fLCcgETroiNfv&ved=0ahUKEwic-e-Sm5KNAxW-d2wGHU59DdYQ4dUDCBc&uact=5&oq=workout&gs_lp=EgNpbWciB3dvcmtvdXQyCBAAGIAEGLEDMgUQABiABDIFEAAYgAQyBRAAGIAEMgUQABiABDIFEAAYgAQyBRAAGIAEMgUQABiABDIFEAAYgAQyBRAAGIAESPIPUO8FWMwOcAF4AJABAJgBuwGgAdsGqgEDMy40uAEDyAEA-AEBigILZ3dzLXdpei1pbWeYAgegAvkGqAIAwgILEAAYgAQYsQMYgwHCAg4QABiABBixAxiDARiKBZgDA5IHAzEuNqAH_CCyBwMxLja4B_kG&sclient=img&udm=2#vhid=z-ELbwKhDmrJRM&vssid=mosaic.jpg",
                "",
                "1. Start in a plank position with hands slightly wider than shoulders\n" +
                        "2. Lower your body until your chest nearly touches the floor\n" +
                        "3. Push back up to the starting position\n" +
                        "4. Repeat",
                3,
                10,
                60,
                true
        );

        ExerciseModel exercise2 = new ExerciseModel(
                "e2",
                "Squats",
                "Fundamental lower body movement",
                "legs",
                "https://example.com/squat.jpg",
                "",
                "1. Stand with feet shoulder-width apart\n" +
                        "2. Bend knees and lower your hips as if sitting in a chair\n" +
                        "3. Keep chest up and back straight\n" +
                        "4. Return to standing position\n" +
                        "5. Repeat",
                3,
                15,
                45,
                true
        );

        ExerciseModel exercise3 = new ExerciseModel(
                "e3",
                "Plank",
                "Core stabilization exercise",
                "core",
                "https://example.com/plank.jpg",
                "",
                "1. Start in push-up position but with forearms on the ground\n" +
                        "2. Keep body in straight line from head to heels\n" +
                        "3. Engage core and hold position\n" +
                        "4. Breathe normally during hold",
                3,
                1,
                30,
                true
        );

        // Add exercises to Firebase
        mDatabase.child("exercises").child(exercise1.getExerciseId()).setValue(exercise1);
        mDatabase.child("exercises").child(exercise2.getExerciseId()).setValue(exercise2);
        mDatabase.child("exercises").child(exercise3.getExerciseId()).setValue(exercise3);
    }
}