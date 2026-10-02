package com.example.final_yp;

import java.io.Serializable;
public class NutritionModel implements Serializable{

    private int calories;
    private float protein; // grams
    private float carbs;   // grams
    private float fat;     // grams
    private float fiber;   // grams
    private float sugar;   // grams

    // Required empty constructor for Firebase
    public NutritionModel() {
    }

    public NutritionModel(int calories, float protein, float carbs, float fat, float fiber, float sugar) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.fiber = fiber;
        this.sugar = sugar;
    }

    // Combines two nutrition objects, used for daily tracking
    public static NutritionModel combine(NutritionModel n1, NutritionModel n2) {
        int calories = n1.getCalories() + n2.getCalories();
        float protein = n1.getProtein() + n2.getProtein();
        float carbs = n1.getCarbs() + n2.getCarbs();
        float fat = n1.getFat() + n2.getFat();
        float fiber = n1.getFiber() + n2.getFiber();
        float sugar = n1.getSugar() + n2.getSugar();

        return new NutritionModel(calories, protein, carbs, fat, fiber, sugar);
    }

    // Getters and Setters
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public float getProtein() { return protein; }
    public void setProtein(float protein) { this.protein = protein; }

    public float getCarbs() { return carbs; }
    public void setCarbs(float carbs) { this.carbs = carbs; }

    public float getFat() { return fat; }
    public void setFat(float fat) { this.fat = fat; }

    public float getFiber() { return fiber; }
    public void setFiber(float fiber) { this.fiber = fiber; }

    public float getSugar() { return sugar; }
    public void setSugar(float sugar) { this.sugar = sugar; }
}
