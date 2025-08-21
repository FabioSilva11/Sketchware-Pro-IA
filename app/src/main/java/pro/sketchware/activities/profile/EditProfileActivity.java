package pro.sketchware.activities.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pro.sketchware.R;
import pro.sketchware.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends BaseAppCompatActivity {

    private ActivityEditProfileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);

        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setTitle(getTranslatedString(R.string.profile_edit));

        loadCurrentValues();

        binding.btnSave.setOnClickListener(v -> save());
    }

    private void loadCurrentValues() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            finish();
            return;
        }
        // Ensure loading visible while loading
        binding.content.setVisibility(android.view.View.GONE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(current.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.content.setVisibility(android.view.View.VISIBLE);
                binding.nome.setText(snapshot.child("nome").getValue(String.class));
                binding.username.setText(snapshot.child("username").getValue(String.class));
                binding.bio.setText(snapshot.child("bio").getValue(String.class));
                binding.site.setText(snapshot.child("links").child("site").getValue(String.class));
                binding.github.setText(snapshot.child("links").child("github").getValue(String.class));
                binding.linkedin.setText(snapshot.child("links").child("linkedin").getValue(String.class));
                binding.email.setText(snapshot.child("email").getValue(String.class));
                binding.email.setEnabled(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void save() {
        String nome = String.valueOf(binding.nome.getText()).trim();
        String username = String.valueOf(binding.username.getText()).trim();
        String bio = String.valueOf(binding.bio.getText()).trim();
        String site = String.valueOf(binding.site.getText()).trim();
        String github = String.valueOf(binding.github.getText()).trim();
        String linkedin = String.valueOf(binding.linkedin.getText()).trim();

        boolean valid = true;
        if (TextUtils.isEmpty(nome)) {
            binding.nomeLayout.setError(getTranslatedString(R.string.auth_required_field));
            valid = false;
        } else {
            binding.nomeLayout.setError(null);
        }
        if (TextUtils.isEmpty(username)) {
            binding.usernameLayout.setError(getTranslatedString(R.string.auth_required_field));
            valid = false;
        } else {
            binding.usernameLayout.setError(null);
        }
        if (bio.length() > 300) {
            binding.bioLayout.setError("Máximo de 300 caracteres");
            valid = false;
        } else {
            binding.bioLayout.setError(null);
        }
        if (!valid) return;

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            finish();
            return;
        }
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("usuarios");

        // Verificar unicidade do username se alterado
        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean takenByOther = false;
                        for (DataSnapshot s : snapshot.getChildren()) {
                            if (!current.getUid().equals(s.getKey())) {
                                takenByOther = true;
                                break;
                            }
                        }
                        if (takenByOther) {
                            binding.usernameLayout.setError(getTranslatedString(R.string.auth_username_taken));
                            Toast.makeText(EditProfileActivity.this, getTranslatedString(R.string.auth_username_taken), Toast.LENGTH_LONG).show();
                            return;
                        }

                        java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
                        updates.put("nome", nome);
                        updates.put("username", username);
                        updates.put("bio", bio);
                        java.util.HashMap<String, Object> links = new java.util.HashMap<>();
                        links.put("site", site);
                        links.put("github", github);
                        links.put("linkedin", linkedin);
                        updates.put("links", links);

                        usersRef.child(current.getUid()).updateChildren(updates)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(EditProfileActivity.this, getTranslatedString(R.string.common_word_saved), Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}


