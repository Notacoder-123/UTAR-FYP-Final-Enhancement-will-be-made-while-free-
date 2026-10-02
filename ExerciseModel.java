package com.example.final_yp;

import java.io.Serializable;


public class ExerciseModel implements Serializable{

    private String exerciseId;
    private String name;
    private String description;
    private String muscleGroup; // chest, back, legs, etc.
    private String imageUrl;
    private String videoUrl;
    private String instructions;
    private int defaultSets;
    private int defaultReps;
    private int restTimeSeconds;
    private boolean isBodyweightExercise;

    // Required empty constructor for Firebase
    public ExerciseModel() {
    }

    public ExerciseModel(String exerciseId, String name, String description, String muscleGroup,
                         String imageUrl, String videoUrl, String instructions,
                         int defaultSets, int defaultReps, int restTimeSeconds, boolean isBodyweightExercise) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.instructions = instructions;
        this.defaultSets = defaultSets;
        this.defaultReps = defaultReps;
        this.restTimeSeconds = restTimeSeconds;
        this.isBodyweightExercise = isBodyweightExercise;
    }

    // Getters and Setters
    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public int getDefaultSets() { return defaultSets; }
    public void setDefaultSets(int defaultSets) { this.defaultSets = defaultSets; }

    public int getDefaultReps() { return defaultReps; }
    public void setDefaultReps(int defaultReps) { this.defaultReps = defaultReps; }

    public int getRestTimeSeconds() { return restTimeSeconds; }
    public void setRestTimeSeconds(int restTimeSeconds) { this.restTimeSeconds = restTimeSeconds; }

    public boolean isBodyweightExercise() { return isBodyweightExercise; }
    public void setBodyweightExercise(boolean bodyweightExercise) { isBodyweightExercise = bodyweightExercise; }
}

