package com.example.final_yp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class WorkoutModel implements Serializable{

    private String workoutId;
    private String name;
    private String description;
    private String category; // full body, upper body, lower body
    private String level; // beginner, intermediate, advanced
    private String imageUrl;
    private int estimatedTimeMinutes;
    private List<ExerciseModel> exercises;

    // Required empty constructor for Firebase
    public WorkoutModel() {
        exercises = new ArrayList<>();
    }

    public WorkoutModel(String workoutId, String name, String description, String category,
                        String level, String imageUrl, int estimatedTimeMinutes) {
        this.workoutId = workoutId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.level = level;
        this.imageUrl = imageUrl;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.exercises = new ArrayList<>();
    }

    // Method to add exercise to workout
    public void addExercise(ExerciseModel exercise) {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        exercises.add(exercise);
    }

    // Getters and Setters
    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }

    public List<ExerciseModel> getExercises() { return exercises; }
    public void setExercises(List<ExerciseModel> exercises) { this.exercises = exercises; }
}
