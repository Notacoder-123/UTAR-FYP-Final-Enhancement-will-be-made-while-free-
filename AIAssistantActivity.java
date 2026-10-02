package com.example.final_yp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIAssistantActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private ProgressBar progressBar;
    private ChipGroup quickActionsChipGroup;
    private RadioGroup modeRadioGroup;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private UserModel currentUser;
    private String currentMode = "workout"; // workout or recipe
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // Setup UI
        setupToolbar();
        setupChatRecyclerView();
        setupQuickActions();
        setupClickListeners();

        // Load user data
        loadUserData();

        // Show welcome message
        addWelcomeMessage();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        progressBar = findViewById(R.id.progress_bar);
        quickActionsChipGroup = findViewById(R.id.quick_actions_chip_group);
        modeRadioGroup = findViewById(R.id.mode_radio_group);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("AI Fitness Assistant");
    }

    private void setupChatRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupQuickActions() {
        // Add quick action chips based on mode
        modeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            quickActionsChipGroup.removeAllViews();

            if (checkedId == R.id.workout_mode_radio) {
                currentMode = "workout";
                addWorkoutQuickActions();
            } else if (checkedId == R.id.recipe_mode_radio) {
                currentMode = "recipe";
                addRecipeQuickActions();
            }
        });

        // Default to workout mode
        modeRadioGroup.check(R.id.workout_mode_radio);
    }

    private void addWorkoutQuickActions() {
        String[] workoutActions = {
                "Create beginner full-body workout",
                "Design HIIT routine",
                "Build muscle gain program",
                "Create home workout plan",
                "Design weight loss program"
        };

        for (String action : workoutActions) {
            Chip chip = new Chip(this);
            chip.setText(action);
            chip.setClickable(true);
            chip.setOnClickListener(v -> {
                messageEditText.setText(action);
                sendMessage();
            });
            quickActionsChipGroup.addView(chip);
        }
    }

    private void addRecipeQuickActions() {
        String[] recipeActions = {
                "High-protein breakfast ideas",
                "Low-calorie lunch recipes",
                "Post-workout meal suggestions",
                "Vegetarian dinner recipes",
                "Healthy snack ideas"
        };

        for (String action : recipeActions) {
            Chip chip = new Chip(this);
            chip.setText(action);
            chip.setClickable(true);
            chip.setOnClickListener(v -> {
                messageEditText.setText(action);
                sendMessage();
            });
            quickActionsChipGroup.addView(chip);
        }
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            currentUser = dataSnapshot.getValue(UserModel.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(AIAssistantActivity.this,
                                    "Failed to load user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void addWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
                "Hello! I'm your AI Fitness Assistant. I can help you create personalized workouts and healthy recipes based on your goals. What would you like help with today?",
                false,
                System.currentTimeMillis()
        );
        chatMessages.add(welcomeMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        // Add user message to chat
        ChatMessage userMessage = new ChatMessage(message, true, System.currentTimeMillis());
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        // Clear input
        messageEditText.setText("");

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);

        // Simulate AI processing with a delay
        handler.postDelayed(() -> {
            generateAIResponse(message);
        }, 1500); // 1.5 second delay to simulate processing
    }

    private void generateAIResponse(String userMessage) {
        String aiResponse = generateLocalResponse(userMessage);

        // Hide progress
        progressBar.setVisibility(View.GONE);
        sendButton.setEnabled(true);

        // Add AI response to chat
        ChatMessage aiMessage = new ChatMessage(aiResponse, false, System.currentTimeMillis());
        chatMessages.add(aiMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        // Show save option for generated content
        if (aiResponse.contains("Workout") || aiResponse.contains("Recipe") ||
                aiResponse.contains("Ingredients") || aiResponse.contains("sets")) {
            showSaveOption(aiResponse);
        }
    }

    private String generateLocalResponse(String userMessage) {
        String lowercaseMessage = userMessage.toLowerCase();

        if (currentMode.equals("workout")) {
            return generateWorkoutResponse(lowercaseMessage);
        } else {
            return generateRecipeResponse(lowercaseMessage);
        }
    }

    private String generateWorkoutResponse(String message) {
        // Beginner workouts
        if (message.contains("beginner") || message.contains("full-body") || message.contains("full body")) {
            return "🏋️ BEGINNER FULL-BODY WORKOUT\n\n" +
                    "⏱️ Duration: 30-40 minutes\n" +
                    "📊 Difficulty: Beginner\n\n" +
                    "WARM-UP (5 minutes):\n" +
                    "• Jumping jacks - 30 seconds\n" +
                    "• Arm circles - 30 seconds each direction\n" +
                    "• Leg swings - 10 each leg\n" +
                    "• Torso twists - 20 reps\n\n" +
                    "MAIN WORKOUT:\n" +
                    "1. Push-ups (or knee push-ups)\n" +
                    "   Sets: 3 | Reps: 8-12 | Rest: 60s\n\n" +
                    "2. Bodyweight Squats\n" +
                    "   Sets: 3 | Reps: 12-15 | Rest: 60s\n\n" +
                    "3. Plank\n" +
                    "   Sets: 3 | Time: 20-30s | Rest: 45s\n\n" +
                    "4. Walking Lunges\n" +
                    "   Sets: 3 | Reps: 10 each leg | Rest: 60s\n\n" +
                    "5. Glute Bridges\n" +
                    "   Sets: 3 | Reps: 15 | Rest: 45s\n\n" +
                    "6. Mountain Climbers\n" +
                    "   Sets: 3 | Reps: 20 total | Rest: 60s\n\n" +
                    "COOL-DOWN (5 minutes):\n" +
                    "• Static stretching for all major muscle groups\n\n" +
                    "💡 Tips: Focus on form over speed. Increase reps gradually as you get stronger!";
        }

        // HIIT workouts
        else if (message.contains("hiit") || message.contains("high intensity")) {
            return "🔥 HIIT WORKOUT ROUTINE\n\n" +
                    "⏱️ Duration: 20-25 minutes\n" +
                    "📊 Difficulty: Intermediate\n\n" +
                    "FORMAT: 45 seconds work / 15 seconds rest\n\n" +
                    "ROUND 1 (4 minutes):\n" +
                    "1. Burpees\n" +
                    "2. Jump Squats\n" +
                    "3. Mountain Climbers\n" +
                    "4. High Knees\n\n" +
                    "REST: 1 minute\n\n" +
                    "ROUND 2 (4 minutes):\n" +
                    "1. Push-up to T\n" +
                    "2. Jumping Lunges\n" +
                    "3. Plank Jacks\n" +
                    "4. Bicycle Crunches\n\n" +
                    "REST: 1 minute\n\n" +
                    "ROUND 3 (4 minutes):\n" +
                    "1. Star Jumps\n" +
                    "2. Squat Thrusters\n" +
                    "3. Russian Twists\n" +
                    "4. Sprint in Place\n\n" +
                    "FINISHER:\n" +
                    "• 1 minute plank hold\n" +
                    "• 30 burpees (as fast as possible)\n\n" +
                    "💡 Tip: Maintain high intensity during work periods. Modify exercises if needed!";
        }

        // Muscle gain
        else if (message.contains("muscle") || message.contains("gain") || message.contains("strength")) {
            return "💪 MUSCLE BUILDING PROGRAM\n\n" +
                    "📅 4-Week Progressive Plan\n\n" +
                    "WEEK 1-2: Foundation\n" +
                    "Monday/Thursday: Upper Body\n" +
                    "• Push-ups: 4x8-10\n" +
                    "• Pike Push-ups: 3x8\n" +
                    "• Diamond Push-ups: 3x6-8\n" +
                    "• Tricep Dips: 3x10\n" +
                    "• Plank to Downward Dog: 3x10\n\n" +
                    "Tuesday/Friday: Lower Body\n" +
                    "• Squats: 4x12-15\n" +
                    "• Bulgarian Split Squats: 3x10 each\n" +
                    "• Single-Leg Deadlifts: 3x10 each\n" +
                    "• Calf Raises: 4x20\n" +
                    "• Wall Sits: 3x30-45s\n\n" +
                    "WEEK 3-4: Progressive Overload\n" +
                    "• Increase reps by 2-3\n" +
                    "• Add 1 set to main exercises\n" +
                    "• Decrease rest by 15s\n\n" +
                    "NUTRITION TIPS:\n" +
                    "• Protein: 1.6-2.2g per kg body weight\n" +
                    "• Eat in slight caloric surplus (200-300 calories)\n" +
                    "• Stay hydrated\n" +
                    "• Get 7-9 hours sleep";
        }

        // Home workout
        else if (message.contains("home") || message.contains("no equipment")) {
            return "🏠 HOME WORKOUT PLAN\n\n" +
                    "No Equipment Needed!\n\n" +
                    "CIRCUIT 1 (3 rounds):\n" +
                    "• Jumping Jacks - 30s\n" +
                    "• Push-ups - 10 reps\n" +
                    "• Squats - 15 reps\n" +
                    "• Plank - 30s\n" +
                    "Rest 60s between rounds\n\n" +
                    "CIRCUIT 2 (3 rounds):\n" +
                    "• Burpees - 8 reps\n" +
                    "• Lunges - 10 each leg\n" +
                    "• Mountain Climbers - 20 total\n" +
                    "• Side Plank - 20s each side\n" +
                    "Rest 60s between rounds\n\n" +
                    "CIRCUIT 3 (2 rounds):\n" +
                    "• High Knees - 30s\n" +
                    "• Tricep Dips (use chair) - 12 reps\n" +
                    "• Glute Bridges - 15 reps\n" +
                    "• Bicycle Crunches - 20 total\n\n" +
                    "Total Time: 25-30 minutes\n\n" +
                    "💡 Perfect for small spaces!";
        }

        // Weight loss
        else if (message.contains("weight loss") || message.contains("fat") || message.contains("burn")) {
            return "🔥 WEIGHT LOSS PROGRAM\n\n" +
                    "4-Week Fat Burning Plan\n\n" +
                    "WORKOUT SCHEDULE:\n" +
                    "• Monday: HIIT Cardio (20 min)\n" +
                    "• Tuesday: Full Body Strength\n" +
                    "• Wednesday: Active Recovery (walk/yoga)\n" +
                    "• Thursday: HIIT Cardio (20 min)\n" +
                    "• Friday: Full Body Strength\n" +
                    "• Saturday: Long Cardio (30-45 min)\n" +
                    "• Sunday: Rest\n\n" +
                    "SAMPLE HIIT SESSION:\n" +
                    "30s work / 30s rest x 10 rounds:\n" +
                    "1. Burpees\n" +
                    "2. Jump Squats\n" +
                    "3. Mountain Climbers\n" +
                    "4. High Knees\n" +
                    "5. Jumping Jacks\n\n" +
                    "NUTRITION GUIDELINES:\n" +
                    "• Create 300-500 calorie deficit\n" +
                    "• Protein: 1.8-2g per kg body weight\n" +
                    "• Stay hydrated (3L water daily)\n" +
                    "• Track your calories\n\n" +
                    "Expected Results: 0.5-1kg loss per week";
        }

        // Default workout response
        else {
            String userGoal = currentUser != null ? currentUser.getGoal() : "general fitness";
            return "💪 PERSONALIZED WORKOUT RECOMMENDATION\n\n" +
                    "Based on your profile" + (currentUser != null ? " (Goal: " + userGoal + ")" : "") + ":\n\n" +
                    "TODAY'S WORKOUT:\n" +
                    "1. Dynamic Warm-up - 5 minutes\n\n" +
                    "2. Main Exercises:\n" +
                    "   • Compound Movement: 4 sets x 8-10 reps\n" +
                    "   • Secondary Exercise: 3 sets x 12 reps\n" +
                    "   • Isolation Exercise: 3 sets x 15 reps\n" +
                    "   • Core Work: 3 sets x 30 seconds\n\n" +
                    "3. Cardio Finisher:\n" +
                    "   • 10 minutes moderate intensity\n\n" +
                    "4. Cool-down & Stretch - 5 minutes\n\n" +
                    "Would you like me to create a more specific workout? Try asking for:\n" +
                    "• 'Beginner workout'\n" +
                    "• 'HIIT routine'\n" +
                    "• 'Home workout'\n" +
                    "• 'Weight loss program'";
        }
    }

    private String generateRecipeResponse(String message) {
        // High protein breakfast
        if (message.contains("protein") && message.contains("breakfast")) {
            return "🍳 HIGH-PROTEIN BREAKFAST BOWL\n\n" +
                    "Calories: 450 | Protein: 35g | Carbs: 28g | Fat: 22g\n\n" +
                    "INGREDIENTS:\n" +
                    "• 2 large eggs\n" +
                    "• 1/2 cup Greek yogurt (non-fat)\n" +
                    "• 1/4 cup granola (low sugar)\n" +
                    "• 1/2 avocado, sliced\n" +
                    "• 1 cup spinach\n" +
                    "• 1 tbsp chia seeds\n" +
                    "• Salt & pepper to taste\n" +
                    "• Cooking spray\n\n" +
                    "INSTRUCTIONS:\n" +
                    "1. Heat a pan with cooking spray over medium heat\n" +
                    "2. Sauté spinach until wilted (2 minutes)\n" +
                    "3. Scramble eggs with spinach, season with salt & pepper\n" +
                    "4. In a bowl, add Greek yogurt as base\n" +
                    "5. Top with scrambled eggs\n" +
                    "6. Add sliced avocado on the side\n" +
                    "7. Sprinkle granola and chia seeds on top\n\n" +
                    "Prep Time: 10 minutes\n" +
                    "Perfect for muscle recovery!";
        }

        // Low calorie lunch
        else if (message.contains("lunch") || message.contains("low-calorie") || message.contains("low calorie")) {
            return "🥗 LOW-CALORIE POWER LUNCH\n\n" +
                    "Calories: 320 | Protein: 38g | Carbs: 24g | Fat: 8g\n\n" +
                    "GRILLED CHICKEN SALAD:\n\n" +
                    "INGREDIENTS:\n" +
                    "• 150g grilled chicken breast\n" +
                    "• 2 cups mixed greens\n" +
                    "• 1/2 cucumber, diced\n" +
                    "• 10 cherry tomatoes, halved\n" +
                    "• 1/4 red onion, sliced\n" +
                    "• 1 tbsp feta cheese\n" +
                    "• 2 tbsp balsamic vinegar\n" +
                    "• 1 tsp olive oil\n" +
                    "• Lemon juice\n" +
                    "• Herbs: oregano, basil\n\n" +
                    "INSTRUCTIONS:\n" +
                    "1. Season and grill chicken (15 minutes)\n" +
                    "2. Let chicken rest, then slice\n" +
                    "3. Mix all vegetables in a large bowl\n" +
                    "4. Add sliced chicken on top\n" +
                    "5. Sprinkle feta cheese\n" +
                    "6. Drizzle with balsamic, olive oil, and lemon\n" +
                    "7. Season with herbs\n\n" +
                    "💡 Meal prep tip: Make 5 portions on Sunday!";
        }

        // Post workout meal
        else if (message.contains("post-workout") || message.contains("post workout") || message.contains("recovery")) {
            return "💪 POST-WORKOUT RECOVERY MEAL\n\n" +
                    "Calories: 520 | Protein: 42g | Carbs: 65g | Fat: 12g\n\n" +
                    "PROTEIN POWER BOWL:\n\n" +
                    "INGREDIENTS:\n" +
                    "• 150g grilled salmon or chicken\n" +
                    "• 1 cup cooked brown rice\n" +
                    "• 1 cup steamed broccoli\n" +
                    "• 1/2 cup black beans\n" +
                    "• 1/4 avocado\n" +
                    "• 1 tbsp tahini sauce\n" +
                    "• Sesame seeds\n\n" +
                    "QUICK SHAKE OPTION:\n" +
                    "• 1 banana\n" +
                    "• 1 cup milk\n" +
                    "• 1 scoop protein powder\n" +
                    "• 1 tbsp peanut butter\n" +
                    "• 1/2 cup oats\n" +
                    "• Ice cubes\n\n" +
                    "TIMING:\n" +
                    "Consume within 30-45 minutes post-workout for optimal recovery!\n\n" +
                    "💡 The 3:1 carb to protein ratio maximizes recovery";
        }

        // Vegetarian dinner
        else if (message.contains("vegetarian") || message.contains("dinner")) {
            return "🌱 VEGETARIAN DINNER\n\n" +
                    "Calories: 420 | Protein: 22g | Carbs: 48g | Fat: 16g\n\n" +
                    "QUINOA BUDDHA BOWL:\n\n" +
                    "INGREDIENTS:\n" +
                    "• 1 cup cooked quinoa\n" +
                    "• 1/2 cup chickpeas (roasted)\n" +
                    "• 1/2 cup roasted sweet potato\n" +
                    "• 1 cup kale (massaged)\n" +
                    "• 1/4 cup red cabbage (shredded)\n" +
                    "• 2 tbsp hummus\n" +
                    "• 1 tbsp pumpkin seeds\n" +
                    "• Tahini dressing\n\n" +
                    "TAHINI DRESSING:\n" +
                    "• 2 tbsp tahini\n" +
                    "• 1 tbsp lemon juice\n" +
                    "• 1 tsp maple syrup\n" +
                    "• Water to thin\n\n" +
                    "INSTRUCTIONS:\n" +
                    "1. Roast sweet potato & chickpeas (25 min at 400°F)\n" +
                    "2. Massage kale with lemon juice\n" +
                    "3. Assemble bowl with quinoa base\n" +
                    "4. Arrange toppings\n" +
                    "5. Drizzle with tahini dressing\n\n" +
                    "Rich in fiber and plant protein!";
        }

        // Healthy snacks
        else if (message.contains("snack")) {
            return "🥜 HEALTHY SNACK IDEAS\n\n" +
                    "5 QUICK OPTIONS (100-200 calories each):\n\n" +
                    "1. PROTEIN BALLS\n" +
                    "• 1 cup dates\n" +
                    "• 1/2 cup almonds\n" +
                    "• 2 tbsp cocoa powder\n" +
                    "• Roll into balls, refrigerate\n\n" +
                    "2. GREEK YOGURT PARFAIT\n" +
                    "• 1/2 cup Greek yogurt\n" +
                    "• 1/4 cup berries\n" +
                    "• 1 tbsp honey\n\n" +
                    "3. VEGGIE STICKS & HUMMUS\n" +
                    "• Carrots, celery, peppers\n" +
                    "• 3 tbsp hummus\n\n" +
                    "4. APPLE SLICES WITH ALMOND BUTTER\n" +
                    "• 1 medium apple\n" +
                    "• 1 tbsp almond butter\n" +
                    "• Cinnamon sprinkle\n\n" +
                    "5. MIXED NUTS\n" +
                    "• 1/4 cup mixed nuts\n" +
                    "• No added salt or sugar\n\n" +
                    "💡 Prep these on Sunday for the whole week!";
        }

        // Default recipe response
        else {
            int targetCalories = currentUser != null ? currentUser.calculateCalorieTarget() : 2000;
            return "🍽️ PERSONALIZED MEAL SUGGESTION\n\n" +
                    "Based on your profile (Daily target: " + targetCalories + " calories):\n\n" +
                    "BALANCED MEAL TEMPLATE:\n\n" +
                    "• Protein: 30-40g (palm-sized portion)\n" +
                    "• Complex Carbs: 40-50g (fist-sized portion)\n" +
                    "• Vegetables: 2-3 cups (unlimited non-starchy)\n" +
                    "• Healthy Fats: 15-20g (thumb-sized portion)\n\n" +
                    "SAMPLE COMBINATIONS:\n" +
                    "1. Grilled chicken + Brown rice + Steamed vegetables\n" +
                    "2. Baked fish + Sweet potato + Salad\n" +
                    "3. Tofu stir-fry + Quinoa + Mixed vegetables\n" +
                    "4. Lean beef + Whole grain pasta + Tomato sauce\n\n" +
                    "Would you like a specific recipe? Try asking for:\n" +
                    "• 'High-protein breakfast'\n" +
                    "• 'Low-calorie lunch'\n" +
                    "• 'Post-workout meal'\n" +
                    "• 'Vegetarian dinner'\n" +
                    "• 'Healthy snacks'";
        }
    }

    private void showSaveOption(String content) {
        // Add a save button as a system message
        ChatMessage saveMessage = new ChatMessage(
                "Would you like to save this " + currentMode + " to your profile?",
                false,
                System.currentTimeMillis()
        );
        saveMessage.setShowSaveButton(true);
        saveMessage.setContentToSave(content);

        chatMessages.add(saveMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Inner class for chat messages
    public static class ChatMessage {
        private String message;
        private boolean isUser;
        private long timestamp;
        private boolean showSaveButton = false;
        private String contentToSave;

        public ChatMessage(String message, boolean isUser, long timestamp) {
            this.message = message;
            this.isUser = isUser;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public String getMessage() { return message; }
        public boolean isUser() { return isUser; }
        public long getTimestamp() { return timestamp; }
        public boolean isShowSaveButton() { return showSaveButton; }
        public void setShowSaveButton(boolean show) { this.showSaveButton = show; }
        public String getContentToSave() { return contentToSave; }
        public void setContentToSave(String content) { this.contentToSave = content; }
    }
}