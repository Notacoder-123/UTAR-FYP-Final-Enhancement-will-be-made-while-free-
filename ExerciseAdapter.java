package com.example.final_yp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private Context context;
    private List<ExerciseModel> exerciseList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ExerciseModel exercise);
    }

    public ExerciseAdapter(Context context, List<ExerciseModel> exerciseList) {
        this.context = context;
        this.exerciseList = exerciseList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseModel exercise = exerciseList.get(position);

        holder.nameTextView.setText(exercise.getName());
        holder.muscleGroupTextView.setText(exercise.getMuscleGroup());

        String setsReps = context.getString(R.string.sets) + ": " + exercise.getDefaultSets() + " | " +
                context.getString(R.string.reps) + ": " + exercise.getDefaultReps();
        holder.setsRepsTextView.setText(setsReps);

        // Load image with Glide if URL is available
        if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(exercise.getImageUrl())
                    .placeholder(R.drawable.ic_workout)
                    .error(R.drawable.ic_workout)
                    .centerCrop()
                    .into(holder.exerciseImageView);
        } else {
            holder.exerciseImageView.setImageResource(R.drawable.ic_workout);
        }

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(exercise);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView exerciseImageView;
        TextView nameTextView, muscleGroupTextView, setsRepsTextView;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseImageView = itemView.findViewById(R.id.exercise_image);
            nameTextView = itemView.findViewById(R.id.exercise_name);
            muscleGroupTextView = itemView.findViewById(R.id.exercise_muscle_group);
            setsRepsTextView = itemView.findViewById(R.id.exercise_sets_reps);
        }
    }
}