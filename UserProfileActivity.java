

        package com.example.final_yp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private Toolbar toolbar;
    private EditText nameEditText, ageEditText, weightEditText, heightEditText;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton, femaleRadioButton;
    private Spinner activityLevelSpinner, goalSpinner, dietaryRestrictionsSpinner;
    private Button saveButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private UserModel currentUser;
    private boolean isEditMode = false;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        initializeViews();

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.profile);

        // Setup spinners
        setupSpinners();

        // Check if we're in edit mode
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);

        // Set up save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });

        // Load user data
        loadUserData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        nameEditText = findViewById(R.id.name_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        weightEditText = findViewById(R.id.weight_edit_text);
        heightEditText = findViewById(R.id.height_edit_text);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        maleRadioButton = findViewById(R.id.male_radio_button);
        femaleRadioButton = findViewById(R.id.female_radio_button);
        activityLevelSpinner = findViewById(R.id.activity_level_spinner);
        goalSpinner = findViewById(R.id.goal_spinner);
        dietaryRestrictionsSpinner = findViewById(R.id.dietary_restrictions_spinner);
        saveButton = findViewById(R.id.save_button);
        progressBar = findViewById(R.id.progress_bar);
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

        // Dietary Restrictions Spinner
        ArrayAdapter<CharSequence> dietaryAdapter = ArrayAdapter.createFromResource(this,
                R.array.dietary_restrictions, android.R.layout.simple_spinner_item);
        dietaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietaryRestrictionsSpinner.setAdapter(dietaryAdapter);
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = firebaseUser.getUid();
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);

                        if (dataSnapshot.exists()) {
                            currentUser = dataSnapshot.getValue(UserModel.class);
                            if (currentUser != null) {
                                currentUser.setUserId(userId);
                                currentUser.setEmail(firebaseUser.getEmail());
                                // Fill form with user data
                                populateForm();
                                isDataLoaded = true;

                                // If not in edit mode, disable editing
                                if (!isEditMode) {
                                    disableEditing();
                                }
                            }
                        } else {
                            // Create new user profile
                            currentUser = new UserModel();
                            currentUser.setUserId(userId);
                            currentUser.setEmail(firebaseUser.getEmail());
                            isDataLoaded = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);
                        Toast.makeText(UserProfileActivity.this, "Failed to load user data: " +
                                databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateForm() {
        // Set values in the form
        if (currentUser.getName() != null) {
            nameEditText.setText(currentUser.getName());
        }

        if (currentUser.getAge() > 0) {
            ageEditText.setText(String.valueOf(currentUser.getAge()));
        }

        if (currentUser.getWeight() > 0) {
            weightEditText.setText(String.valueOf(currentUser.getWeight()));
        }

        if (currentUser.getHeight() > 0) {
            heightEditText.setText(String.valueOf(currentUser.getHeight()));
        }

        // Set gender
        if (currentUser.getGender() != null) {
            if (currentUser.getGender().equalsIgnoreCase("male")) {
                maleRadioButton.setChecked(true);
            } else if (currentUser.getGender().equalsIgnoreCase("female")) {
                femaleRadioButton.setChecked(true);
            }
        }

        // Set spinners
        setSpinnerSelection(activityLevelSpinner, currentUser.getActivityLevel());
        setSpinnerSelection(goalSpinner, currentUser.getGoal());
        setSpinnerSelection(dietaryRestrictionsSpinner, currentUser.getDietaryRestrictions());
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value != null) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void disableEditing() {
        nameEditText.setEnabled(false);
        ageEditText.setEnabled(false);
        weightEditText.setEnabled(false);
        heightEditText.setEnabled(false);
        genderRadioGroup.setEnabled(false);
        maleRadioButton.setEnabled(false);
        femaleRadioButton.setEnabled(false);
        activityLevelSpinner.setEnabled(false);
        goalSpinner.setEnabled(false);
        dietaryRestrictionsSpinner.setEnabled(false);
        saveButton.setVisibility(View.GONE);
    }

    private void saveUserProfile() {
        if (!isDataLoaded) {
            Toast.makeText(this, "Please wait for data to load", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate form
        if (!validateForm()) {
            return;
        }

        // Disable button to prevent multiple clicks
        saveButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Get user values
        String userId = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();
        String name = nameEditText.getText().toString().trim();
        int age = Integer.parseInt(ageEditText.getText().toString().trim());
        float weight = Float.parseFloat(weightEditText.getText().toString().trim());
        float height = Float.parseFloat(heightEditText.getText().toString().trim());
        String gender = maleRadioButton.isChecked() ? "male" : "female";
        String activityLevel = activityLevelSpinner.getSelectedItem().toString();
        String goal = goalSpinner.getSelectedItem().toString();
        String dietaryRestrictions = dietaryRestrictionsSpinner.getSelectedItem().toString();

        // Create user model
        UserModel updatedUser = new UserModel(
                userId, name, email, age, gender, weight, height,
                activityLevel, goal, dietaryRestrictions);

        // Save to Firebase using updateChildren for better reliability
        Map<String, Object> userValues = new HashMap<>();
        userValues.put("userId", userId);
        userValues.put("name", name);
        userValues.put("email", email);
        userValues.put("age", age);
        userValues.put("gender", gender);
        userValues.put("weight", weight);
        userValues.put("height", height);
        userValues.put("activityLevel", activityLevel);
        userValues.put("goal", goal);
        userValues.put("dietaryRestrictions", dietaryRestrictions);

        mDatabase.child("users").child(userId).updateChildren(userValues)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(UserProfileActivity.this, "Profile saved successfully",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Profile saved successfully for user: " + userId);

                            // Update local user object
                            currentUser = updatedUser;

                            // If this was first time setup, go to main activity
                            if (!isEditMode) {
                                finish();
                            }
                        } else {
                            Log.e(TAG, "Failed to save profile", task.getException());
                            Toast.makeText(UserProfileActivity.this, "Failed to save profile: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);
                        Log.e(TAG, "Failed to save profile", e);
                        Toast.makeText(UserProfileActivity.this,
                                "Failed to save profile: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        // Check name
        if (nameEditText.getText().toString().trim().isEmpty()) {
            nameEditText.setError("Required");
            nameEditText.requestFocus();
            valid = false;
        } else {
            nameEditText.setError(null);
        }

        // Check age
        String ageStr = ageEditText.getText().toString().trim();
        if (ageStr.isEmpty()) {
            ageEditText.setError("Required");
            if (valid) ageEditText.requestFocus();
            valid = false;
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 15 || age > 100) {
                    ageEditText.setError("Age must be between 15 and 100");
                    if (valid) ageEditText.requestFocus();
                    valid = false;
                } else {
                    ageEditText.setError(null);
                }
            } catch (NumberFormatException e) {
                ageEditText.setError("Must be a number");
                if (valid) ageEditText.requestFocus();
                valid = false;
            }
        }

        // Check weight
        String weightStr = weightEditText.getText().toString().trim();
        if (weightStr.isEmpty()) {
            weightEditText.setError("Required");
            if (valid) weightEditText.requestFocus();
            valid = false;
        } else {
            try {
                float weight = Float.parseFloat(weightStr);
                if (weight < 30 || weight > 300) {
                    weightEditText.setError("Weight must be between 30 and 300 kg");
                    if (valid) weightEditText.requestFocus();
                    valid = false;
                } else {
                    weightEditText.setError(null);
                }
            } catch (NumberFormatException e) {
                weightEditText.setError("Must be a number");
                if (valid) weightEditText.requestFocus();
                valid = false;
            }
        }

        // Check height
        String heightStr = heightEditText.getText().toString().trim();
        if (heightStr.isEmpty()) {
            heightEditText.setError("Required");
            if (valid) heightEditText.requestFocus();
            valid = false;
        } else {
            try {
                float height = Float.parseFloat(heightStr);
                if (height < 100 || height > 250) {
                    heightEditText.setError("Height must be between 100 and 250 cm");
                    if (valid) heightEditText.requestFocus();
                    valid = false;
                } else {
                    heightEditText.setError(null);
                }
            } catch (NumberFormatException e) {
                heightEditText.setError("Must be a number");
                if (valid) heightEditText.requestFocus();
                valid = false;
            }
        }

        // Check gender
        if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}