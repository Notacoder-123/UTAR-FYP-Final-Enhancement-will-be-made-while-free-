package com.example.final_yp;

import java.io.Serializable;
public class FoodModel implements Serializable{

    private String foodId;
    private String name;
    private String category; // proteins, carbs, fats, fruits, vegetables, etc.
    private String imageUrl;
    private int servingSize; // in grams
    private String servingSizeUnit; // g, oz, cup, etc.
    private NutritionModel nutritionPerServing;
    private boolean isFavorite;

    // Required empty constructor for Firebase
    public FoodModel() {
    }

    public FoodModel(String foodId, String name, String category, String imageUrl,
                     int servingSize, String servingSizeUnit, NutritionModel nutritionPerServing) {
        this.foodId = foodId;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.servingSize = servingSize;
        this.servingSizeUnit = servingSizeUnit;
        this.nutritionPerServing = nutritionPerServing;
        this.isFavorite = false;
    }

    // Calculate nutrition for a specific portion
    public NutritionModel getNutritionForPortion(float portions) {
        int calories = (int) (nutritionPerServing.getCalories() * portions);
        float protein = nutritionPerServing.getProtein() * portions;
        float carbs = nutritionPerServing.getCarbs() * portions;
        float fat = nutritionPerServing.getFat() * portions;
        float fiber = nutritionPerServing.getFiber() * portions;
        float sugar = nutritionPerServing.getSugar() * portions;

        return new NutritionModel(calories, protein, carbs, fat, fiber, sugar);
    }

    // Getters and Setters
    public String getFoodId() { return foodId; }
    public void setFoodId(String foodId) { this.foodId = foodId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getServingSize() { return servingSize; }
    public void setServingSize(int servingSize) { this.servingSize = servingSize; }

    public String getServingSizeUnit() { return servingSizeUnit; }
    public void setServingSizeUnit(String servingSizeUnit) { this.servingSizeUnit = servingSizeUnit; }

    public NutritionModel getNutritionPerServing() { return nutritionPerServing; }
    public void setNutritionPerServing(NutritionModel nutritionPerServing) { this.nutritionPerServing = nutritionPerServing; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
