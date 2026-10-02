package com.example.final_yp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CaloriesCalculatorActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText ageEditText, weightEditText, heightEditText;
    private RadioGroup genderRadioGroup;
    private Spinner activityLevelSpinner, goalSpinner;
    private Button calculateButton;
    private TextView caloriesTextView, proteinTextView, carbsTextView, fatTextView;
    private CardView resultCardView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private UserModel currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calories_calculator);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.calorie_calculator);

        ageEditText = findViewById(R.id.age_edit_text);
        weightEditText = findViewById(R.id.weight_edit_text);
        heightEditText = findViewById(R.id.height_edit_text);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        activityLevelSpinner = findViewById(R.id.activity_level_spinner);
        goalSpinner = findViewById(R.id.goal_spinner);
        calculateButton = findViewById(R.id.calculate_button);
        caloriesTextView = findViewById(R.id.calories_value);
        proteinTextView = findViewById(R.id.protein_value);
        carbsTextView = findViewById(R.id.carbs_value);
        fatTextView = findViewById(R.id.fat_value);
        resultCardView = findViewById(R.id.result_card_view);

        // Set result card initially invisible
        resultCardView.setVisibility(View.GONE);

        // Load user data if logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            loadUserData(firebaseUser.getUid());
        }

        // Calculate button click listener
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateNutrition();
            }
        });
    }

    private void loadUserData(String userId) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(UserModel.class);

                        if (currentUser != null) {
                            // Populate form with user's data
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
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(CaloriesCalculatorActivity.this, "Failed to load user data: " +
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

    private void calculateNutrition() {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }

        // Get values from input fields
        int age = Integer.parseInt(ageEditText.getText().toString().trim());
        float weight = Float.parseFloat(weightEditText.getText().toString().trim());
        float height = Float.parseFloat(heightEditText.getText().toString().trim());

        // Get gender
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        String gender = ((RadioButton) findViewById(selectedGenderId)).getText().toString();

        // Get activity level and goal
        String activityLevel = activityLevelSpinner.getSelectedItem().toString();
        String goal = goalSpinner.getSelectedItem().toString();

        // Create a temporary user model with the input values
        UserModel tempUser = new UserModel();
        tempUser.setAge(age);
        tempUser.setWeight(weight);
        tempUser.setHeight(height);
        tempUser.setGender(gender);
        tempUser.setActivityLevel(activityLevel);
        tempUser.setGoal(goal);

        // Calculate nutrition values
        int calories = tempUser.calculateCalorieTarget();
        UserModel.MacroNutrients macros = tempUser.calculateMacros();

        // Display results
        caloriesTextView.setText(String.valueOf(calories));
        proteinTextView.setText(String.valueOf(macros.getProteinGrams()) + " g");
        carbsTextView.setText(String.valueOf(macros.getCarbGrams()) + " g");
        fatTextView.setText(String.valueOf(macros.getFatGrams()) + " g");

        // Show result card
        resultCardView.setVisibility(View.VISIBLE);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate age
        String ageStr = ageEditText.getText().toString().trim();
        if (ageStr.isEmpty()) {
            ageEditText.setError("Age is required");
            ageEditText.requestFocus();
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 15 || age > 80) {
                    ageEditText.setError("Age must be between 15 and 80");
                    ageEditText.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                ageEditText.setError("Invalid age");
                ageEditText.requestFocus();
                isValid = false;
            }
        }

        // Validate weight
        String weightStr = weightEditText.getText().toString().trim();
        if (weightStr.isEmpty()) {
            weightEditText.setError("Weight is required");
            weightEditText.requestFocus();
            isValid = false;
        } else {
            try {
                float weight = Float.parseFloat(weightStr);
                if (weight < 30 || weight > 300) {
                    weightEditText.setError("Weight must be between 30 and 300 kg");
                    weightEditText.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                weightEditText.setError("Invalid weight");
                weightEditText.requestFocus();
                isValid = false;
            }
        }

        // Validate height
        String heightStr = heightEditText.getText().toString().trim();
        if (heightStr.isEmpty()) {
            heightEditText.setError("Height is required");
            heightEditText.requestFocus();
            isValid = false;
        } else {
            try {
                float height = Float.parseFloat(heightStr);
                if (height < 100 || height > 250) {
                    heightEditText.setError("Height must be between 100 and 250 cm");
                    heightEditText.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                heightEditText.setError("Invalid height");
                heightEditText.requestFocus();
                isValid = false;
            }
        }

        // Validate gender selection
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}