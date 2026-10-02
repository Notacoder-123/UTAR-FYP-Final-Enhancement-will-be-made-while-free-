package com.example.final_yp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodLogModel implements Serializable{
    private String logId;
    private String userId;
    private Date date;
    private Map<String, List<FoodEntry>> mealEntries; // breakfast, lunch, dinner, snacks

    // Required empty constructor for Firebase
    public FoodLogModel() {
        mealEntries = new HashMap<>();
        initializeMeals();
    }

    public FoodLogModel(String logId, String userId, Date date) {
        this.logId = logId;
        this.userId = userId;
        this.date = date;
        mealEntries = new HashMap<>();
        initializeMeals();
    }

    // Initialize meal categories
    private void initializeMeals() {
        mealEntries.put("breakfast", new ArrayList<>());
        mealEntries.put("lunch", new ArrayList<>());
        mealEntries.put("dinner", new ArrayList<>());
        mealEntries.put("snacks", new ArrayList<>());
    }

    // Add a food entry to a specific meal
    public void addFoodEntry(String meal, FoodEntry entry) {
        if (!mealEntries.containsKey(meal)) {
            mealEntries.put(meal, new ArrayList<>());
        }
        mealEntries.get(meal).add(entry);
    }

    // Remove a food entry from a specific meal
    public void removeFoodEntry(String meal, FoodEntry entry) {
        if (mealEntries.containsKey(meal)) {
            mealEntries.get(meal).remove(entry);
        }
    }

    // Calculate total nutrition for the day
    public NutritionModel calculateDailyNutrition() {
        NutritionModel totalNutrition = new NutritionModel(0, 0, 0, 0, 0, 0);

        for (Map.Entry<String, List<FoodEntry>> meal : mealEntries.entrySet()) {
            for (FoodEntry entry : meal.getValue()) {
                NutritionModel entryNutrition = entry.getFood().getNutritionForPortion(entry.getServings());
                totalNutrition = NutritionModel.combine(totalNutrition, entryNutrition);
            }
        }

        return totalNutrition;
    }

    // Calculate nutrition for a specific meal
    public NutritionModel calculateMealNutrition(String meal) {
        NutritionModel mealNutrition = new NutritionModel(0, 0, 0, 0, 0, 0);

        if (mealEntries.containsKey(meal)) {
            for (FoodEntry entry : mealEntries.get(meal)) {
                NutritionModel entryNutrition = entry.getFood().getNutritionForPortion(entry.getServings());
                mealNutrition = NutritionModel.combine(mealNutrition, entryNutrition);
            }
        }

        return mealNutrition;
    }

    // Inner class to represent a food entry in the log
    public static class FoodEntry implements Serializable {
        private String entryId;
        private FoodModel food;
        private float servings;
        private Date timeAdded;

        // Required empty constructor for Firebase
        public FoodEntry() {
        }

        public FoodEntry(String entryId, FoodModel food, float servings) {
            this.entryId = entryId;
            this.food = food;
            this.servings = servings;
            this.timeAdded = new Date();
        }

        // Getters and Setters
        public String getEntryId() { return entryId; }
        public void setEntryId(String entryId) { this.entryId = entryId; }

        public FoodModel getFood() { return food; }
        public void setFood(FoodModel food) { this.food = food; }

        public float getServings() { return servings; }
        public void setServings(float servings) { this.servings = servings; }

        public Date getTimeAdded() { return timeAdded; }
        public void setTimeAdded(Date timeAdded) { this.timeAdded = timeAdded; }
    }

    // Getters and Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Map<String, List<FoodEntry>> getMealEntries() { return mealEntries; }
    public void setMealEntries(Map<String, List<FoodEntry>> mealEntries) { this.mealEntries = mealEntries; }
}
