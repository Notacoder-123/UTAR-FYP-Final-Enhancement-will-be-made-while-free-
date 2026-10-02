package com.example.final_yp;

import java.io.Serializable;
import java.util.Date;

public class ProgressModel implements Serializable {
    private String progressId;
    private String userId;
    private Date date;
    private float weight;
    private float bodyFat; // Optional
    private int caloriesConsumed;
    private int caloriesBurned;
    private int workoutsCompleted;
    private String notes;
    private MeasurementsModel measurements;

    // Required empty constructor for Firebase
    public ProgressModel() {
    }

    public ProgressModel(String progressId, String userId, Date date, float weight) {
        this.progressId = progressId;
        this.userId = userId;
        this.date = date;
        this.weight = weight;
        this.workoutsCompleted = 0;
        this.caloriesConsumed = 0;
        this.caloriesBurned = 0;
    }

    // Inner class for body measurements
    public static class MeasurementsModel implements Serializable {
        private float chest;
        private float waist;
        private float hips;
        private float biceps;
        private float thighs;
        private float neck;

        public MeasurementsModel() {
        }

        // Getters and Setters
        public float getChest() { return chest; }
        public void setChest(float chest) { this.chest = chest; }

        public float getWaist() { return waist; }
        public void setWaist(float waist) { this.waist = waist; }

        public float getHips() { return hips; }
        public void setHips(float hips) { this.hips = hips; }

        public float getBiceps() { return biceps; }
        public void setBiceps(float biceps) { this.biceps = biceps; }

        public float getThighs() { return thighs; }
        public void setThighs(float thighs) { this.thighs = thighs; }

        public float getNeck() { return neck; }
        public void setNeck(float neck) { this.neck = neck; }
    }

    // Calculate progress compared to another entry
    public float calculateWeightChange(ProgressModel previousEntry) {
        if (previousEntry != null) {
            return this.weight - previousEntry.getWeight();
        }
        return 0;
    }

    // Getters and Setters
    public String getProgressId() { return progressId; }
    public void setProgressId(String progressId) { this.progressId = progressId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public float getBodyFat() { return bodyFat; }
    public void setBodyFat(float bodyFat) { this.bodyFat = bodyFat; }

    public int getCaloriesConsumed() { return caloriesConsumed; }
    public void setCaloriesConsumed(int caloriesConsumed) { this.caloriesConsumed = caloriesConsumed; }

    public int getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public int getWorkoutsCompleted() { return workoutsCompleted; }
    public void setWorkoutsCompleted(int workoutsCompleted) { this.workoutsCompleted = workoutsCompleted; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public MeasurementsModel getMeasurements() { return measurements; }
    public void setMeasurements(MeasurementsModel measurements) { this.measurements = measurements; }
}