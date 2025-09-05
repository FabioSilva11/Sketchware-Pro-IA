package pro.sketchware.activities.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;

import java.util.Map;

import pro.sketchware.R;
import pro.sketchware.activities.auth.AuthManager;

public class FreelanceDetailActivity extends BaseAppCompatActivity {

    private TextView tvTitle, tvLongDesc, tvEmail, tvPhone, tvViews;
    private androidx.recyclerview.widget.RecyclerView rvSkills;
    private MaterialButton btnUnlock;
    private DatabaseReference ref;
    private DatabaseReference usersRef;
    private String postId;
    private String ownerUid;
    private String ownerEmail;
    private String ownerPhone;
    private boolean isUnlocked = false;

    private static class SkillsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<SkillsAdapter.VH> {
        private final java.util.List<String> items;
        SkillsAdapter(java.util.List<String> items) { this.items = items; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            android.widget.TextView tv = new android.widget.TextView(parent.getContext());
            tv.setPadding(12, 8, 12, 8);
            tv.setTextSize(14);
            return new VH(tv);
        }
        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            ((android.widget.TextView) holder.itemView).setText(items.get(position));
        }
        @Override public int getItemCount() { return items.size(); }
        static class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder { VH(@NonNull View itemView) { super(itemView); } }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freelance_detail);
        tvTitle = findViewById(R.id.tv_title);
        tvLongDesc = findViewById(R.id.tv_long_desc);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvViews = findViewById(R.id.tv_views);
        rvSkills = findViewById(R.id.rv_skills);
        if (rvSkills != null) {
            rvSkills.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        }
        btnUnlock = findViewById(R.id.btn_unlock);

        postId = getIntent().getStringExtra("post_id");
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        ref = db.getReference().child("freelance_posts").child(postId);
        usersRef = db.getReference().child("users");
        loadPost();

        btnUnlock.setOnClickListener(v -> {
            attemptUnlock();
        });

        // Click actions only when unlocked
        tvEmail.setOnClickListener(v -> {
            if (!isUnlocked) {
                                 Toast.makeText(this, "Unlock to see email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!TextUtils.isEmpty(ownerEmail)) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + ownerEmail));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ownerEmail});
                                 intent.putExtra(Intent.EXTRA_SUBJECT, "About your freelance ad");
                try { startActivity(intent); } catch (Exception ignored) {}
            }
        });

        tvPhone.setOnClickListener(v -> {
            if (!isUnlocked) {
                                 Toast.makeText(this, "Unlock to see phone", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!TextUtils.isEmpty(ownerPhone)) {
                String digits = ownerPhone.replaceAll("[^\\d]", "");
                Uri uri = Uri.parse("https://wa.me/" + digits);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try { startActivity(intent); } catch (Exception ignored) {}
            }
        });
    }

    private void loadPost() {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) { finish(); return; }
                Map<String, Object> post = (Map<String, Object>) snapshot.getValue();
                if (post == null) { finish(); return; }
                tvTitle.setText(String.valueOf(post.get("title")));
                tvLongDesc.setText(String.valueOf(post.get("long_description")));
                Object vObj = post.get("views");
                int views = 0;
                if (vObj instanceof Number) views = ((Number) vObj).intValue();
                                 tvViews.setText("Views: " + views);
                Map<String, Object> owner = (Map<String, Object>) post.get("owner");
                if (owner != null) {
                    ownerUid = (String) owner.get("uid");
                    ownerEmail = (String) owner.get("email");
                    ownerPhone = (String) owner.get("phone");
                    // Check if already unlocked
                    String myUid = AuthManager.getInstance().getCurrentUser() != null ? AuthManager.getInstance().getCurrentUser().getUid() : null;
                    Object unlockedMap = post.get("unlocked_by");
                    boolean already = false;
                    if (unlockedMap instanceof Map && myUid != null) {
                        Object val = ((Map<?, ?>) unlockedMap).get(myUid);
                        already = val instanceof Boolean && (Boolean) val;
                    }
                    setContactsMasked(!already);
                    isUnlocked = already;
                    btnUnlock.setEnabled(!already && (myUid == null || !myUid.equals(ownerUid)));
                }

                // Bind skills grid
                Object rich = post.get("sub_categories");
                java.util.List<java.util.Map<String, Object>> richList = null;
                if (rich instanceof java.util.List) {
                    richList = (java.util.List<java.util.Map<String, Object>>) rich;
                }
                if (rvSkills != null) {
                    java.util.List<String> labels = new java.util.ArrayList<>();
                    if (richList != null && !richList.isEmpty()) {
                        for (java.util.Map<String, Object> sc : richList) {
                            String icon = String.valueOf(sc.get("icon"));
                            String scTitle = String.valueOf(sc.get("title"));
                            labels.add(((icon != null && !icon.equals("null") && !icon.isEmpty()) ? icon + " " : "") + scTitle);
                        }
                    } else {
                        Object ids = post.get("skills");
                        if (ids instanceof java.util.List) {
                            java.util.List<?> list = (java.util.List<?>) ids;
                            for (Object idObj : list) {
                                String id = String.valueOf(idObj);
                                pro.sketchware.activities.auth.CategoryManager.SubCategory sc = pro.sketchware.activities.auth.CategoryManager.getInstance().getSubCategoryById(id);
                                if (sc != null) {
                                    String icon = sc.getIcon();
                                    String scTitle = sc.getTitle();
                                    labels.add(((icon != null && !icon.isEmpty()) ? icon + " " : "") + scTitle);
                                }
                            }
                        }
                    }
                    rvSkills.setAdapter(new SkillsAdapter(labels));
                }

                // If viewing own post, disable unlock
                String myUid = AuthManager.getInstance().getCurrentUser() != null ? AuthManager.getInstance().getCurrentUser().getUid() : null;
                if (!TextUtils.isEmpty(ownerUid) && ownerUid.equals(myUid)) {
                    btnUnlock.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void attemptUnlock() {
        String myUid = AuthManager.getInstance().getCurrentUser() != null ? AuthManager.getInstance().getCurrentUser().getUid() : null;
        if (TextUtils.isEmpty(myUid)) {
                         Toast.makeText(this, "Please login to unlock", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.isEmpty(ownerUid) && ownerUid.equals(myUid)) {
            btnUnlock.setEnabled(false);
            return;
        }

        // Check if already unlocked
        ref.child("unlocked_by").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snap) {
                if (Boolean.TRUE.equals(snap.getValue(Boolean.class))) {
                    setContactsMasked(false);
                    isUnlocked = true;
                    btnUnlock.setEnabled(false);
                } else {
                    chargeAndUnlock(myUid);
                }
            }
            @Override public void onCancelled(DatabaseError error) { }
        });
    }

    private void chargeAndUnlock(String myUid) {
        // Show confirmation dialog with current balance
        usersRef.child(myUid).child("coin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object val = snapshot.getValue();
                final int currentCoins = getCurrentCoins(val);
                
                new MaterialAlertDialogBuilder(FreelanceDetailActivity.this)
                    .setTitle("Unlock contacts")
                    .setMessage("Current balance: " + currentCoins + " coins\n\nCost: 10 coins\nBalance after: " + (currentCoins - 10) + " coins\n\nContinue?")
                    .setPositiveButton("Unlock", (dialog, which) -> {
                        if (currentCoins < 10) {
                            Toast.makeText(FreelanceDetailActivity.this, "Insufficient balance (10)", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        performChargeAndUnlock(myUid);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FreelanceDetailActivity.this, "Error checking balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performChargeAndUnlock(String myUid) {
        DatabaseReference coinRef = usersRef.child(myUid).child("coin");
        coinRef.runTransaction(new Transaction.Handler() {
            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Object val = currentData.getValue();
                int coins = 0;
                if (val instanceof Number) coins = ((Number) val).intValue();
                else if (val instanceof String) try { coins = Integer.parseInt((String) val); } catch (Exception ignored) {}
                if (coins < 10) {
                    return Transaction.abort();
                }
                coins -= 10;
                // Preserve original type (store as String to match existing schema)
                currentData.setValue(String.valueOf(coins));
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (!committed) {
                    Toast.makeText(FreelanceDetailActivity.this, "Insufficient balance (10)", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Mark unlocked and reveal
                ref.child("unlocked_by").child(myUid).setValue(true);
                setContactsMasked(false);
                isUnlocked = true;
                btnUnlock.setEnabled(false);
                Toast.makeText(FreelanceDetailActivity.this, "Contacts unlocked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getCurrentCoins(Object val) {
        if (val instanceof Number) {
            return ((Number) val).intValue();
        } else if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (Exception ignored) {
                return 0;
            }
        }
        return 0;
    }

    private void setContactsMasked(boolean masked) {
        if (masked) {
            tvEmail.setText(maskEmail(ownerEmail));
            tvPhone.setText(maskPhone(ownerPhone));
        } else {
            tvEmail.setText(ownerEmail);
            tvPhone.setText(ownerPhone);
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.length() < 3) return "***";
        int at = email.indexOf('@');
        if (at <= 1) return "***@";
        String name = email.substring(0, at);
        String domain = email.substring(at);
        String masked = name.charAt(0) + "***" + name.charAt(name.length()-1);
        return masked + domain;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return phone.substring(0, Math.min(3, phone.length())) + "****";
    }
}


