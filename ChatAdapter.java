package com.example.final_yp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private Context context;
    private List<AIAssistantActivity.ChatMessage> messages;
    private SimpleDateFormat timeFormat;

    public ChatAdapter(Context context, List<AIAssistantActivity.ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_ai, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        AIAssistantActivity.ChatMessage message = messages.get(position);

        holder.messageTextView.setText(message.getMessage());
        holder.timeTextView.setText(timeFormat.format(new Date(message.getTimestamp())));

        // Handle save button for AI messages
        if (!message.isUser() && message.isShowSaveButton() && holder.saveButton != null) {
            holder.saveButton.setVisibility(View.VISIBLE);
            holder.saveButton.setOnClickListener(v -> {
                saveContent(message.getContentToSave());
                holder.saveButton.setEnabled(false);
                holder.saveButton.setText("Saved!");
            });
        } else if (holder.saveButton != null) {
            holder.saveButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void saveContent(String content) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        String userId = mAuth.getCurrentUser().getUid();
        String contentId = UUID.randomUUID().toString();

        // Save to a new "ai_generated" node in Firebase
        mDatabase.child("ai_generated").child(userId).child(contentId).setValue(content);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timeTextView;
        Button saveButton;
        CardView messageCard;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            saveButton = itemView.findViewById(R.id.save_button);
            messageCard = itemView.findViewById(R.id.message_card);
        }
    }
}