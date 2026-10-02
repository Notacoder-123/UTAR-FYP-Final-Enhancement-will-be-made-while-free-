package com.example.final_yp;

import java.io.Serializable;

public class UserModel implements Serializable {

    private String userId;
    private String name;
    private String email;
    private int age;
    private String gender;
    private float weight; // in kg
    private float height; // in cm
    private String activityLevel; // sedentary, moderate, active, very active
    private String goal; // weight loss, maintenance, muscle gain
    private String dietaryRestrictions; // vegetarian, vegan, gluten-free, etc.

    // Required empty constructor for Firebase
    public UserModel() {
    }

    public UserModel(String userId, String name, String email, int age, String gender, float weight, float height,
                     String activityLevel, String goal, String dietaryRestrictions) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.activityLevel = activityLevel;
        this.goal = goal;
        this.dietaryRestrictions = dietaryRestrictions;
    }

    // Calculate BMR (Basal Metabolic Rate) using Harris-Benedict Equation
    public double calculateBMR() {
        if (gender == null || gender.isEmpty()) {
            // Default to an average of male and female formulas if gender is not set
            double maleBMR = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
            double femaleBMR = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
            return (maleBMR + femaleBMR) / 2;
        }

        if (gender.equalsIgnoreCase("male")) {
            return 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else {
            return 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        }
    }

    // Calculate TDEE (Total Daily Energy Expenditure)
    public double calculateTDEE() {
        double bmr = calculateBMR();
        double activityMultiplier = 1.2; // Default: Sedentary

        if (activityLevel != null && !activityLevel.isEmpty()) {
            switch (activityLevel.toLowerCase()) {
                case "sedentary":
                    activityMultiplier = 1.2;
                    break;
                case "moderate":
                    activityMultiplier = 1.375;
                    break;
                case "active":
                    activityMultiplier = 1.55;
                    break;
                case "very active":
                    activityMultiplier = 1.725;
                    break;
            }
        }

        return bmr * activityMultiplier;
    }


    // Calculate daily calorie target based on goal
    public int calculateCalorieTarget() {
        double tdee = calculateTDEE();

        if (goal != null && !goal.isEmpty()) {
            switch (goal.toLowerCase()) {
                case "weight loss":
                    return (int) (tdee - 500); // 500 calorie deficit
                case "muscle gain":
                    return (int) (tdee + 300); // 300 calorie surplus
                default:
                    return (int) tdee; // maintenance
            }
        }
        return (int) tdee; // Default to maintenance if goal is not set
    }

    // Calculate macronutrient ratios based on goal
    public MacroNutrients calculateMacros() {
        int calorieTarget = calculateCalorieTarget();
        double proteinRatio, carbRatio, fatRatio;

        switch (goal.toLowerCase()) {
            case "weight loss":
                proteinRatio = 0.40; // 40% protein
                fatRatio = 0.35;     // 35% fat
                carbRatio = 0.25;    // 25% carbs
                break;
            case "muscle gain":
                proteinRatio = 0.30; // 30% protein
                fatRatio = 0.25;     // 25% fat
                carbRatio = 0.45;    // 45% carbs
                break;
            default: // maintenance
                proteinRatio = 0.30; // 30% protein
                fatRatio = 0.30;     // 30% fat
                carbRatio = 0.40;    // 40% carbs
        }

        int proteinGrams = (int) ((calorieTarget * proteinRatio) / 4); // 4 calories per gram
        int carbGrams = (int) ((calorieTarget * carbRatio) / 4);       // 4 calories per gram
        int fatGrams = (int) ((calorieTarget * fatRatio) / 9);         // 9 calories per gram

        return new MacroNutrients(proteinGrams, carbGrams, fatGrams);
    }

    // Nested class to hold macronutrient information
    public static class MacroNutrients {
        private int proteinGrams;
        private int carbGrams;
        private int fatGrams;

        public MacroNutrients(int proteinGrams, int carbGrams, int fatGrams) {
            this.proteinGrams = proteinGrams;
            this.carbGrams = carbGrams;
            this.fatGrams = fatGrams;
        }

        public int getProteinGrams() { return proteinGrams; }
        public int getCarbGrams() { return carbGrams; }
        public int getFatGrams() { return fatGrams; }
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getDietaryRestrictions() { return dietaryRestrictions; }
    public void setDietaryRestrictions(String dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }
}
