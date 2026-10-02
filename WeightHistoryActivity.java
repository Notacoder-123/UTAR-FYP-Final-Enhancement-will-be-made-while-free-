package com.example.final_yp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WeightHistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView historyRecyclerView;
    private TextView emptyView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private WeightHistoryAdapter adapter;
    private List<ProgressModel> progressList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_history);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        historyRecyclerView = findViewById(R.id.history_recycler_view);
        emptyView = findViewById(R.id.empty_view);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Weight History");

        // Setup RecyclerView
        progressList = new ArrayList<>();
        adapter = new WeightHistoryAdapter(this, progressList);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);

        // Load data
        loadWeightHistory();
    }

    private void loadWeightHistory() {
        String userId = mAuth.getCurrentUser().getUid();

        Query query = mDatabase.child("progress").child(userId)
                .orderByChild("date");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ProgressModel progress = snapshot.getValue(ProgressModel.class);
                    if (progress != null) {
                        progressList.add(progress);
                    }
                }

                // Sort by date descending (newest first)
                Collections.sort(progressList, (a, b) -> b.getDate().compareTo(a.getDate()));

                adapter.notifyDataSetChanged();

                // Show/hide empty view
                if (progressList.isEmpty()) {
                    historyRecyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    historyRecyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Inner adapter class
    public static class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.ViewHolder> {

        private List<ProgressModel> progressList;
        private SimpleDateFormat dateFormat;

        public WeightHistoryAdapter(WeightHistoryActivity context, List<ProgressModel> progressList) {
            this.progressList = progressList;
            this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_weight_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProgressModel progress = progressList.get(position);

            holder.dateTextView.setText(dateFormat.format(progress.getDate()));
            holder.weightTextView.setText(String.format("%.1f kg", progress.getWeight()));

            // Show weight change if not the last item
            if (position < progressList.size() - 1) {
                ProgressModel previousProgress = progressList.get(position + 1);
                float change = progress.getWeight() - previousProgress.getWeight();

                if (change > 0) {
                    holder.changeTextView.setText(String.format("+%.1f kg", change));
                    holder.changeTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.error));
                } else if (change < 0) {
                    holder.changeTextView.setText(String.format("%.1f kg", change));
                    holder.changeTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.success));
                } else {
                    holder.changeTextView.setText("No change");
                    holder.changeTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.textSecondary));
                }
                holder.changeTextView.setVisibility(View.VISIBLE);
            } else {
                holder.changeTextView.setVisibility(View.GONE);
            }

            // Show body fat if available
            if (progress.getBodyFat() > 0) {
                holder.bodyFatTextView.setText(String.format("Body Fat: %.1f%%", progress.getBodyFat()));
                holder.bodyFatTextView.setVisibility(View.VISIBLE);
            } else {
                holder.bodyFatTextView.setVisibility(View.GONE);
            }

            // Show notes if available
            if (progress.getNotes() != null && !progress.getNotes().isEmpty()) {
                holder.notesTextView.setText(progress.getNotes());
                holder.notesTextView.setVisibility(View.VISIBLE);
            } else {
                holder.notesTextView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return progressList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView dateTextView, weightTextView, changeTextView, bodyFatTextView, notesTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                dateTextView = itemView.findViewById(R.id.date_text_view);
                weightTextView = itemView.findViewById(R.id.weight_text_view);
                changeTextView = itemView.findViewById(R.id.change_text_view);
                bodyFatTextView = itemView.findViewById(R.id.body_fat_text_view);
                notesTextView = itemView.findViewById(R.id.notes_text_view);
            }
        }
    }
}