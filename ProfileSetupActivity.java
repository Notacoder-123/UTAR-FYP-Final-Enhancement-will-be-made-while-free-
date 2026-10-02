package com.example.final_yp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText ageEditText, weightEditText, heightEditText;
    private RadioGroup genderRadioGroup;
    private Spinner activityLevelSpinner, goalSpinner, dietaryRestrictionSpinner;
    private Button saveButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private UserModel currentUser; // Added the missing semicolon here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        ageEditText = findViewById(R.id.age_edit_text);
        weightEditText = findViewById(R.id.weight_edit_text);
        heightEditText = findViewById(R.id.height_edit_text);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        activityLevelSpinner = findViewById(R.id.activity_level_spinner);
        goalSpinner = findViewById(R.id.goal_spinner);
        dietaryRestrictionSpinner = findViewById(R.id.dietary_restriction_spinner);
        saveButton = findViewById(R.id.save_button);
        progressBar = findViewById(R.id.progress_bar);

        // Set up spinners
        setupSpinners();

        // Check if user is logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            // Not logged in, go to login screen
            startActivity(new Intent(ProfileSetupActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Load existing user data if available
        loadUserData(firebaseUser.getUid());

        // Save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void setupSpinners() {
        // Activity Level Spinner
        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(this,
                R.array.activity_levels, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityLevelSpinner.setAdapter(activityAdapter);

        // Goal Spinner
        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(this,
                R.array.goals, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalSpinner.setAdapter(goalAdapter);

        // Dietary Restriction Spinner
        ArrayAdapter<CharSequence> dietaryAdapter = ArrayAdapter.createFromResource(this,
                R.array.dietary_restrictions, android.R.layout.simple_spinner_item);
        dietaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietaryRestrictionSpinner.setAdapter(dietaryAdapter);
    }

    private void loadUserData(String userId) {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.GONE);

                        if (dataSnapshot.exists()) {
                            currentUser = dataSnapshot.getValue(UserModel.class);

                            // Populate the form with existing data
                            if (currentUser != null) {
                                if (currentUser.getAge() > 0) {
                                    ageEditText.setText(String.valueOf(currentUser.getAge()));
                                }

                                if (currentUser.getWeight() > 0) {
                                    weightEditText.setText(String.valueOf(currentUser.getWeight()));
                                }

                                if (currentUser.getHeight() > 0) {
                                    heightEditText.setText(String.valueOf(currentUser.getHeight()));
                                }

                                // Set gender radio button
                                if (currentUser.getGender() != null) {
                                    if (currentUser.getGender().equalsIgnoreCase("male")) {
                                        ((RadioButton) findViewById(R.id.male_radio_button)).setChecked(true);
                                    } else if (currentUser.getGender().equalsIgnoreCase("female")) {
                                        ((RadioButton) findViewById(R.id.female_radio_button)).setChecked(true);
                                    }
                                }

                                // Set spinners
                                setSpinnerSelection(activityLevelSpinner, R.array.activity_levels, currentUser.getActivityLevel());
                                setSpinnerSelection(goalSpinner, R.array.goals, currentUser.getGoal());
                                setSpinnerSelection(dietaryRestrictionSpinner, R.array.dietary_restrictions, currentUser.getDietaryRestrictions());
                            }
                        } else {
                            currentUser = new UserModel();
                            currentUser.setUserId(userId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileSetupActivity.this, "Failed to load user data: " +
                                databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setSpinnerSelection(Spinner spinner, int arrayResourceId, String value) {
        if (value != null && !value.isEmpty()) {
            String[] items = getResources().getStringArray(arrayResourceId);
            for (int i = 0; i < items.length; i++) {
                if (items[i].equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveUserProfile() {
        // Validate form data
        String ageStr = ageEditText.getText().toString().trim();
        String weightStr = weightEditText.getText().toString().trim();
        String heightStr = heightEditText.getText().toString().trim();

        if (TextUtils.isEmpty(ageStr)) {
            ageEditText.setError("Age is required");
            ageEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(weightStr)) {
            weightEditText.setError("Weight is required");
            weightEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(heightStr)) {
            heightEditText.setError("Height is required");
            heightEditText.requestFocus();
            return;
        }

        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get data from form
        int age = Integer.parseInt(ageStr);
        float weight = Float.parseFloat(weightStr);
        float height = Float.parseFloat(heightStr);

        String gender = ((RadioButton) findViewById(selectedGenderId)).getText().toString();
        String activityLevel = activityLevelSpinner.getSelectedItem().toString();
        String goal = goalSpinner.getSelectedItem().toString();
        String dietaryRestrictions = dietaryRestrictionSpinner.getSelectedItem().toString();

        // Update user model
        currentUser.setAge(age);
        currentUser.setWeight(weight);
        currentUser.setHeight(height);
        currentUser.setGender(gender);
        currentUser.setActivityLevel(activityLevel);
        currentUser.setGoal(goal);
        currentUser.setDietaryRestrictions(dietaryRestrictions);

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Save to Firebase
        mDatabase.child("users").child(currentUser.getUserId()).setValue(currentUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileSetupActivity.this, "Profile saved successfully",
                                    Toast.LENGTH_SHORT).show();
                            // Navigate to MainActivity
                            Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ProfileSetupActivity.this, "Failed to save profile: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}