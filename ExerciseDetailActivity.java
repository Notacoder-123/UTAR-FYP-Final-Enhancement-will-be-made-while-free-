package com.example.final_yp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ExerciseDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView exerciseImageView;
    private TextView exerciseNameTextView, exerciseDescriptionTextView;
    private TextView muscleGroupTextView, setsRepsTextView, instructionsTextView;

    private DatabaseReference mDatabase;
    private String exerciseId;
    private ExerciseModel exercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://final-fyp1-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDatabase = database.getReference();

        // Get exercise ID from intent
        exerciseId = getIntent().getStringExtra("exercise_id");
        if (exerciseId == null) {
            Toast.makeText(this, "Exercise not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.exercise_details);

        exerciseImageView = findViewById(R.id.exercise_image);
        exerciseNameTextView = findViewById(R.id.exercise_name);
        exerciseDescriptionTextView = findViewById(R.id.exercise_description);
        muscleGroupTextView = findViewById(R.id.exercise_muscle_group);
        setsRepsTextView = findViewById(R.id.exercise_sets_reps);
        instructionsTextView = findViewById(R.id.exercise_instructions);

        // Load exercise data
        loadExerciseData();
    }

    private void loadExerciseData() {
        mDatabase.child("exercises").child(exerciseId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                exercise = dataSnapshot.getValue(ExerciseModel.class);

                if (exercise != null) {
                    // Update UI with exercise data
                    updateUI();
                } else {
                    Toast.makeText(ExerciseDetailActivity.this, "Exercise not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ExerciseDetailActivity.this,
                        "Failed to load exercise: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI() {
        // Set exercise details
        exerciseNameTextView.setText(exercise.getName());
        exerciseDescriptionTextView.setText(exercise.getDescription());
        muscleGroupTextView.setText(exercise.getMuscleGroup());

        String setsReps = getString(R.string.sets) + ": " + exercise.getDefaultSets() + " | " +
                getString(R.string.reps) + ": " + exercise.getDefaultReps();
        setsRepsTextView.setText(setsReps);

        instructionsTextView.setText(exercise.getInstructions());

        // Load image if URL is available
        if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(exercise.getImageUrl())
                    .placeholder(R.drawable.ic_workout)
                    .error(R.drawable.ic_workout)
                    .centerCrop()
                    .into(exerciseImageView);
        } else {
            exerciseImageView.setImageResource(R.drawable.ic_workout);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}