package pro.sketchware.activities.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pro.sketchware.databinding.ActivityLoginBinding;

public class LoginActivity extends BaseAppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.btnGoToRegister.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, RegisterActivity.class))
        );
    }

    private void attemptLogin() {
        @NonNull String email = String.valueOf(binding.email.getText()).trim();
        @NonNull String password = String.valueOf(binding.password.getText());

        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError(getTranslatedString(pro.sketchware.R.string.auth_required_field));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError(getTranslatedString(pro.sketchware.R.string.auth_invalid_email));
            valid = false;
        } else {
            binding.emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError(getTranslatedString(pro.sketchware.R.string.auth_required_field));
            valid = false;
        } else if (password.length() < 6) {
            binding.passwordLayout.setError(getTranslatedString(pro.sketchware.R.string.auth_password_min));
            valid = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        if (!valid) return;

        try {
            com.google.firebase.FirebaseApp.initializeApp(this);
        } catch (Throwable ignored) {}
        java.util.List<com.google.firebase.FirebaseApp> apps = com.google.firebase.FirebaseApp.getApps(this);
        if (apps == null || apps.isEmpty()) {
            Toast.makeText(this, "Firebase não está configurado neste app.", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        // Atualiza último login no Realtime Database
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(user.getUid());
                        java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
                        updates.put("ultimo_login", java.time.Instant.now().toString());
                        ref.updateChildren(updates);
                    }
                    Toast.makeText(this, getTranslatedString(pro.sketchware.R.string.auth_login_success), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }
}


