package pro.sketchware.activities.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import pro.sketchware.R;
import pro.sketchware.activities.auth.AuthManager;
import pro.sketchware.activities.auth.CategoryManager;

public class PublishFreelanceActivity extends BaseAppCompatActivity {

    private TextInputLayout tilTitle;
    private TextInputLayout tilShortDesc;
    private TextInputLayout tilLongDesc;
    private EditText titleInput;
    private EditText shortDescInput;
    private EditText longDescInput;
    private ChipGroup chipGroupSkills;
    private Button publishButton;
    private FirebaseAnalytics analytics;
    private DatabaseReference db;

    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d[\\s-\\.]*){7,}");
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;
    private static final Pattern URL_PATTERN = Patterns.WEB_URL;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_freelance);

        tilTitle = findViewById(R.id.til_title);
        tilShortDesc = findViewById(R.id.til_short_desc);
        tilLongDesc = findViewById(R.id.til_long_desc);
        titleInput = findViewById(R.id.input_title);
        shortDescInput = findViewById(R.id.input_short_description);
        longDescInput = findViewById(R.id.input_long_description);
        chipGroupSkills = findViewById(R.id.chipgroup_skills);
        publishButton = findViewById(R.id.button_publish);
        analytics = FirebaseAnalytics.getInstance(this);
        db = FirebaseDatabase.getInstance().getReference();

        // Enforce 200 chars on short description
        shortDescInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});

        attachValidationWatchers();
        populateSkills();

        publishButton.setOnClickListener(v -> {
            if (!validateAll()) return;

            String title = titleInput.getText().toString().trim();
            String shortDesc = shortDescInput.getText().toString().trim();
            String longDesc = longDescInput.getText().toString().trim();
            List<String> selectedSkillIds = getSelectedSkillIds();

            if (selectedSkillIds.isEmpty()) {
                Toast.makeText(this, "Select at least 1 skill", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthManager auth = AuthManager.getInstance();
            if (auth == null || auth.getCurrentUser() == null) {
                Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = auth.getCurrentUser().getUid();
            String email = auth.getCurrentUser().getEmail();
            String phone = auth.getCurrentUser().getPhoneNumber();

            DatabaseReference postsRef = db.child("freelance_posts").push();
            String postId = postsRef.getKey();

            Map<String, Object> post = new HashMap<>();
            post.put("id", postId);
            post.put("title", title);
            post.put("short_description", shortDesc);
            post.put("long_description", longDesc);
            post.put("skills", selectedSkillIds);
            post.put("views", 0);
            Map<String, Object> owner = new HashMap<>();
            owner.put("uid", uid);
            owner.put("email", email);
            owner.put("phone", phone);
            post.put("owner", owner);
            post.put("created_at", System.currentTimeMillis());

            // Salvar e notificar
            postsRef.setValue(post).addOnSuccessListener(aVoid -> {
                // Analytics
                Bundle params = new Bundle();
                params.putString("post_id", postId);
                params.putString("title_len", String.valueOf(title.length()));
                params.putInt("skills_count", selectedSkillIds.size());
                analytics.logEvent("freelance_post_published", params);

                // Enfileira notificação para todos os usuários (processada por backend/Cloud Function)
                Map<String, Object> notif = new HashMap<>();
                notif.put("type", "freelance_post");
                notif.put("title", "Novo anúncio de freela");
                notif.put("body", title);
                notif.put("post_id", postId);
                notif.put("created_at", System.currentTimeMillis());
                db.child("notifications").child("broadcasts").push().setValue(notif);

                Toast.makeText(this, "Ad published", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to publish: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        });
    }

    private void attachValidationWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateAll(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        titleInput.addTextChangedListener(watcher);
        shortDescInput.addTextChangedListener(watcher);
        longDescInput.addTextChangedListener(watcher);
    }

    private boolean validateAll() {
        boolean ok = true;

        ok &= validateFieldNoContacts(tilTitle, titleInput, true);
        ok &= validateFieldNoContacts(tilShortDesc, shortDescInput, true);
        ok &= validateFieldNoContacts(tilLongDesc, longDescInput, true);

        return ok;
    }

    private boolean validateFieldNoContacts(TextInputLayout til, EditText edit, boolean required) {
        String text = edit.getText() == null ? "" : edit.getText().toString().trim();
        if (required && TextUtils.isEmpty(text)) {
                            til.setError("Required field");
            return false;
        }
        if (!TextUtils.isEmpty(text)) {
            if (EMAIL_PATTERN.matcher(text).find()) {
                til.setError("Email not allowed");
                return false;
            }
            if (URL_PATTERN.matcher(text).find()) {
                til.setError("Links not allowed");
                return false;
            }
            if (PHONE_PATTERN.matcher(text).find()) {
                til.setError("Phone not allowed");
                return false;
            }
        }
        til.setError(null);
        return true;
    }

    

    private void populateSkills() {
        chipGroupSkills.removeAllViews();
        List<CategoryManager.Category> categories = CategoryManager.getInstance().getAllCategories();
        for (CategoryManager.Category category : categories) {
            for (CategoryManager.SubCategory sub : category.getSubCategories()) {
                Chip chip = new Chip(this);
                chip.setText(sub.getTitle());
                chip.setCheckable(true);
                chip.setTag(sub.getId());
                chipGroupSkills.addView(chip);
            }
        }
    }

    private List<String> getSelectedSkillIds() {
        List<String> ids = new ArrayList<>();
        int count = chipGroupSkills.getChildCount();
        for (int i = 0; i < count; i++) {
            android.view.View child = chipGroupSkills.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    Object tag = chip.getTag();
                    if (tag != null) ids.add(tag.toString());
                }
            }
        }
        return ids;
    }
}


