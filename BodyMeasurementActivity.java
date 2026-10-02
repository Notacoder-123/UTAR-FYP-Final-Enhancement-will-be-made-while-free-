package com.example.final_yp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BodyMeasurementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText chestEditText, waistEditText, hipsEditText;
    private EditText bicepsEditText, thighsEditText, neckEditText;
    private TextView lastUpdatedTextView;
    private Button saveButton;
    private ProgressBar progressBar;
    private CardView currentMeasurementsCard, previousMeasurementsCard;

    // Previous measurement views
    private TextView prevChestText, prevWaistText, prevHipsText;
    private TextView prevBicepsText, prevThighsText, prevNeckText;
    private TextView prevDateText;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private ProgressModel.MeasurementsModel currentMeasurements;
    private ProgressModel.MeasurementsModel previousMeasurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_measurement);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Initialize views
        initializeViews();

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Body Measurements");

        // Load existing measurements
        loadMeasurements();

        // Save button listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMeasurements();
            }
        });
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);

        // Input fields
        chestEditText = findViewById(R.id.chest_edit_text);
        waistEditText = findViewById(R.id.waist_edit_text);
        hipsEditText = findViewById(R.id.hips_edit_text);
        bicepsEditText = findViewById(R.id.biceps_edit_text);
        thighsEditText = findViewById(R.id.thighs_edit_text);
        neckEditText = findViewById(R.id.neck_edit_text);

        // Previous measurement views
        prevChestText = findViewById(R.id.prev_chest_text);
        prevWaistText = findViewById(R.id.prev_waist_text);
        prevHipsText = findViewById(R.id.prev_hips_text);
        prevBicepsText = findViewById(R.id.prev_biceps_text);
        prevThighsText = findViewById(R.id.prev_thighs_text);
        prevNeckText = findViewById(R.id.prev_neck_text);
        prevDateText = findViewById(R.id.prev_date_text);

        lastUpdatedTextView = findViewById(R.id.last_updated_text);
        saveButton = findViewById(R.id.save_button);
        progressBar = findViewById(R.id.progress_bar);
        currentMeasurementsCard = findViewById(R.id.current_measurements_card);
        previousMeasurementsCard = findViewById(R.id.previous_measurements_card);
    }

    private void loadMeasurements() {
        progressBar.setVisibility(View.VISIBLE);

        // Load latest measurements
        mDatabase.child("measurements").child(currentUserId).child("latest")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            currentMeasurements = dataSnapshot.getValue(ProgressModel.MeasurementsModel.class);
                            if (currentMeasurements != null) {
                                displayCurrentMeasurements();
                            }

                            // Get last updated timestamp
                            Long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                            if (timestamp != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("Last updated: MMM dd, yyyy", Locale.getDefault());
                                lastUpdatedTextView.setText(sdf.format(new Date(timestamp)));
                                lastUpdatedTextView.setVisibility(View.VISIBLE);
                            }
                        }

                        // Load previous measurements
                        loadPreviousMeasurements();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(BodyMeasurementActivity.this,
                                "Failed to load measurements", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPreviousMeasurements() {
        mDatabase.child("measurements").child(currentUserId).child("previous")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.GONE);

                        if (dataSnapshot.exists()) {
                            previousMeasurements = dataSnapshot.getValue(ProgressModel.MeasurementsModel.class);
                            Long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);

                            if (previousMeasurements != null) {
                                displayPreviousMeasurements(timestamp);
                                previousMeasurementsCard.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void displayCurrentMeasurements() {
        if (currentMeasurements.getChest() > 0)
            chestEditText.setText(String.valueOf(currentMeasurements.getChest()));
        if (currentMeasurements.getWaist() > 0)
            waistEditText.setText(String.valueOf(currentMeasurements.getWaist()));
        if (currentMeasurements.getHips() > 0)
            hipsEditText.setText(String.valueOf(currentMeasurements.getHips()));
        if (currentMeasurements.getBiceps() > 0)
            bicepsEditText.setText(String.valueOf(currentMeasurements.getBiceps()));
        if (currentMeasurements.getThighs() > 0)
            thighsEditText.setText(String.valueOf(currentMeasurements.getThighs()));
        if (currentMeasurements.getNeck() > 0)
            neckEditText.setText(String.valueOf(currentMeasurements.getNeck()));
    }

    private void displayPreviousMeasurements(Long timestamp) {
        prevChestText.setText(String.format("%.1f cm", previousMeasurements.getChest()));
        prevWaistText.setText(String.format("%.1f cm", previousMeasurements.getWaist()));
        prevHipsText.setText(String.format("%.1f cm", previousMeasurements.getHips()));
        prevBicepsText.setText(String.format("%.1f cm", previousMeasurements.getBiceps()));
        prevThighsText.setText(String.format("%.1f cm", previousMeasurements.getThighs()));
        prevNeckText.setText(String.format("%.1f cm", previousMeasurements.getNeck()));

        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            prevDateText.setText("Measured on: " + sdf.format(new Date(timestamp)));
        }
    }

    private void saveMeasurements() {
        // Create new measurements object
        ProgressModel.MeasurementsModel newMeasurements = new ProgressModel.MeasurementsModel();

        // Get values from input fields
        String chestStr = chestEditText.getText().toString().trim();
        String waistStr = waistEditText.getText().toString().trim();
        String hipsStr = hipsEditText.getText().toString().trim();
        String bicepsStr = bicepsEditText.getText().toString().trim();
        String thighsStr = thighsEditText.getText().toString().trim();
        String neckStr = neckEditText.getText().toString().trim();

        // Validate at least one measurement is entered
        if (TextUtils.isEmpty(chestStr) && TextUtils.isEmpty(waistStr) &&
                TextUtils.isEmpty(hipsStr) && TextUtils.isEmpty(bicepsStr) &&
                TextUtils.isEmpty(thighsStr) && TextUtils.isEmpty(neckStr)) {
            Toast.makeText(this, "Please enter at least one measurement", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse values
        try {
            if (!TextUtils.isEmpty(chestStr)) newMeasurements.setChest(Float.parseFloat(chestStr));
            if (!TextUtils.isEmpty(waistStr)) newMeasurements.setWaist(Float.parseFloat(waistStr));
            if (!TextUtils.isEmpty(hipsStr)) newMeasurements.setHips(Float.parseFloat(hipsStr));
            if (!TextUtils.isEmpty(bicepsStr)) newMeasurements.setBiceps(Float.parseFloat(bicepsStr));
            if (!TextUtils.isEmpty(thighsStr)) newMeasurements.setThighs(Float.parseFloat(thighsStr));
            if (!TextUtils.isEmpty(neckStr)) newMeasurements.setNeck(Float.parseFloat(neckStr));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid measurement value", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // If there's a current measurement, move it to previous
        if (currentMeasurements != null) {
            mDatabase.child("measurements").child(currentUserId).child("previous")
                    .setValue(currentMeasurements);
            mDatabase.child("measurements").child(currentUserId).child("previous")
                    .child("timestamp").setValue(System.currentTimeMillis());
        }

        // Save new measurements as latest
        mDatabase.child("measurements").child(currentUserId).child("latest")
                .setValue(newMeasurements)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Add timestamp
                            mDatabase.child("measurements").child(currentUserId).child("latest")
                                    .child("timestamp").setValue(System.currentTimeMillis());

                            Toast.makeText(BodyMeasurementActivity.this,
                                    "Measurements saved successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(BodyMeasurementActivity.this,
                                    "Failed to save measurements", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}