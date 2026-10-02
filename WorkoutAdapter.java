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
public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>{

    private Context context;
    private List<WorkoutModel> workoutList;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(WorkoutModel workout);
    }

    public WorkoutAdapter(Context context, List<WorkoutModel> workoutList) {
        this.context = context;
        this.workoutList = workoutList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutModel workout = workoutList.get(position);


        holder.workoutDescriptionTextView.setText(workout.getDescription());
        holder.workoutCategoryTextView.setText(workout.getCategory());
        holder.workoutTimeTextView.setText(workout.getEstimatedTimeMinutes() + " " + context.getString(R.string.minutes));


        // Load image with Glide if URL is available
        if (workout.getImageUrl() != null && !workout.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(workout.getImageUrl())
                    .placeholder(R.drawable.ic_workout)
                    .error(R.drawable.ic_workout)
                    .centerCrop()
                    .into(holder.workoutImageView);
        } else {
            holder.workoutImageView.setImageResource(R.drawable.ic_workout);
        }

        if (workout.getName() != null) {
            holder.workoutNameTextView.setText(workout.getName());
        } else {
            holder.workoutNameTextView.setText("Unnamed Workout");
        }

        if (workout.getDescription() != null) {
            holder.workoutDescriptionTextView.setText(workout.getDescription());
        } else {
            holder.workoutDescriptionTextView.setText("No description");
        }

        if (workout.getCategory() != null) {
            holder.workoutCategoryTextView.setText(workout.getCategory());
        } else {
            holder.workoutCategoryTextView.setText("");

            // Set time
            holder.workoutTimeTextView.setText(workout.getEstimatedTimeMinutes() + " minutes");

        }

        // For numeric values, they can't be null but check the object first
        if (workout != null) {
            holder.workoutTimeTextView.setText(workout.getEstimatedTimeMinutes() + " minutes");
        } else {
            holder.workoutTimeTextView.setText("0 minutes");
        }


        // Set image (keep it small)
        holder.workoutImageView.setImageResource(R.drawable.ic_workout);
        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(workout);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        ImageView workoutImageView;
        TextView workoutNameTextView, workoutDescriptionTextView, workoutCategoryTextView, workoutTimeTextView;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutImageView = itemView.findViewById(R.id.workout_image);
            workoutNameTextView = itemView.findViewById(R.id.workout_name);
            workoutDescriptionTextView = itemView.findViewById(R.id.workout_description);
            workoutCategoryTextView = itemView.findViewById(R.id.workout_category);
            workoutTimeTextView = itemView.findViewById(R.id.workout_time);


        }
    }
}
