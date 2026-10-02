package com.example.final_yp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FoodSearchActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText searchEditText;
    private RecyclerView foodRecyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FoodAdapter foodAdapter;
    private List<FoodModel> foodList;
    private String currentMeal;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_search);

        // Get current meal from intent
        currentMeal = getIntent().getStringExtra("meal");
        if (currentMeal == null) {
            currentMeal = "breakfast"; // Default to breakfast if not specified
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.search_food);

        searchEditText = findViewById(R.id.search_edit_text);
        foodRecyclerView = findViewById(R.id.food_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progress_bar);

        // Set up RecyclerView
        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList);
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodRecyclerView.setAdapter(foodAdapter);

        // Set food item click listener
        foodAdapter.setOnFoodClickListener(new FoodAdapter.OnFoodClickListener() {
            @Override
            public void onFoodClick(FoodModel food) {
                // Add food to current meal
                addFoodToMeal(food);
            }
        });

        // Set search text change listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Search food as user types
                searchFood(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Load initial food data
        loadFoods();
    }

    private void loadFoods() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("foods").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                foodList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FoodModel food = snapshot.getValue(FoodModel.class);
                    if (food != null) {
                        foodList.add(food);
                    }
                }

                foodAdapter.notifyDataSetChanged();

                // Show empty view if no foods
                if (foodList.isEmpty()) {
                    foodRecyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("No foods found. Try adding some common foods.");

                    // Add sample foods if none exist (for testing)
                    addSampleFoodsToFirebase();
                } else {
                    foodRecyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FoodSearchActivity.this, "Failed to load foods: " +
                        databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchFood(String query) {
        if (query.isEmpty()) {
            // If search is empty, load all foods
            loadFoods();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Convert query to lowercase for case-insensitive search
        final String lowercaseQuery = query.toLowerCase();

        Query searchQuery = mDatabase.child("foods").orderByChild("name")
                .startAt(lowercaseQuery)
                .endAt(lowercaseQuery + "\uf8ff"); // Unicode character to match end of string

        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                foodList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FoodModel food = snapshot.getValue(FoodModel.class);
                    if (food != null) {
                        foodList.add(food);
                    }
                }

                foodAdapter.notifyDataSetChanged();

                // Show empty view if no foods match search
                if (foodList.isEmpty()) {
                    foodRecyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("No foods found matching \"" + query + "\"");
                } else {
                    foodRecyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FoodSearchActivity.this, "Failed to search foods: " +
                        databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFoodToMeal(FoodModel food) {
        // Get today's date in format YYYY-MM-DD for the log ID
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final String today = dateFormat.format(new Date());
        final String logId = currentUserId + "_" + today;

        // First, check if there's already a food log for today
        mDatabase.child("food_logs").child(logId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        FoodLogModel foodLog;

                        if (dataSnapshot.exists()) {
                            // Food log exists, use it
                            foodLog = dataSnapshot.getValue(FoodLogModel.class);
                        } else {
                            // Create a new food log for today
                            foodLog = new FoodLogModel(logId, currentUserId, new Date());
                        }

                        // Create a new food entry with 1 serving as default
                        String entryId = UUID.randomUUID().toString();
                        FoodLogModel.FoodEntry entry = new FoodLogModel.FoodEntry(entryId, food, 1);

                        // Add the entry to the current meal
                        foodLog.addFoodEntry(currentMeal, entry);

                        // Save the food log to Firebase
                        mDatabase.child("food_logs").child(logId).setValue(foodLog)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(FoodSearchActivity.this,
                                                food.getName() + " added to " + currentMeal,
                                                Toast.LENGTH_SHORT).show();
                                        // Go back to meal planner
                                        finish();
                                    } else {
                                        Toast.makeText(FoodSearchActivity.this,
                                                "Failed to add food: " + task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(FoodSearchActivity.this,
                                "Failed to add food: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addSampleFoodsToFirebase() {
        // Sample basic foods for testing
        FoodModel food1 = new FoodModel(
                "f1",
                "Chicken Breast",
                "proteins",
                "https://example.com/chicken.jpg",
                100,
                "g",
                new NutritionModel(165, 31, 0, 3.6f, 0, 0)
        );

        FoodModel food2 = new FoodModel(
                "f2",
                "Brown Rice",
                "carbs",
                "https://example.com/rice.jpg",
                100,
                "g",
                new NutritionModel(112, 2.6f, 23, 0.9f, 1.8f, 0.4f)
        );

        FoodModel food3 = new FoodModel(
                "f3",
                "Avocado",
                "fats",
                "https://example.com/avocado.jpg",
                100,
                "g",
                new NutritionModel(160, 2, 8.5f, 14.7f, 6.7f, 0.7f)
        );

        FoodModel food4 = new FoodModel(
                "f4",
                "Egg",
                "proteins",
                "https://example.com/egg.jpg",
                50,
                "g",
                new NutritionModel(78, 6.3f, 0.6f, 5.3f, 0, 0.6f)
        );

        FoodModel food5 = new FoodModel(
                "f5",
                "Apple",
                "fruits",
                "https://example.com/apple.jpg",
                100,
                "g",
                new NutritionModel(52, 0.3f, 14, 0.2f, 2.4f, 10.3f)
        );

        // Add foods to Firebase
        mDatabase.child("foods").child(food1.getFoodId()).setValue(food1);
        mDatabase.child("foods").child(food2.getFoodId()).setValue(food2);
        mDatabase.child("foods").child(food3.getFoodId()).setValue(food3);
        mDatabase.child("foods").child(food4.getFoodId()).setValue(food4);
        mDatabase.child("foods").child(food5.getFoodId()).setValue(food5);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}