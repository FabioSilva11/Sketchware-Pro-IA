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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import pro.sketchware.databinding.ActivityRegisterBinding;

public class RegisterActivity extends BaseAppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.btnGoToLogin.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, LoginActivity.class))
        );
    }

    private void attemptRegister() {
        @NonNull String nome = String.valueOf(binding.nome.getText()).trim();
        @NonNull String username = String.valueOf(binding.username.getText()).trim();
        @NonNull String email = String.valueOf(binding.email.getText()).trim();
        @NonNull String password = String.valueOf(binding.password.getText());
        @NonNull String bio = String.valueOf(binding.bio.getText()).trim();
        @NonNull String site = String.valueOf(binding.site.getText()).trim();
        @NonNull String github = String.valueOf(binding.github.getText()).trim();
        @NonNull String linkedin = String.valueOf(binding.linkedin.getText()).trim();

        boolean valid = true;
        if (TextUtils.isEmpty(nome)) {
            binding.nomeLayout.setError(getTranslatedString(pro.sketchware.R.string.auth_required_field));
            valid = false;
        } else {
            binding.nomeLayout.setError(null);
        }
        if (TextUtils.isEmpty(username)) {
            binding.usernameLayout.setError(getTranslatedString(pro.sketchware.R.string.auth_required_field));
            valid = false;
        } else {
            binding.usernameLayout.setError(null);
        }
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

        if (bio.length() > 300) {
            binding.bioLayout.setError("Máximo de 300 caracteres");
            valid = false;
        } else {
            binding.bioLayout.setError(null);
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

        // Verificar unicidade do username antes de criar a conta
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            binding.usernameLayout.setError(getTranslatedString(pro.sketchware.R.string.auth_username_taken));
                            Toast.makeText(RegisterActivity.this, getTranslatedString(pro.sketchware.R.string.auth_username_taken), Toast.LENGTH_LONG).show();
                            return;
                        }

                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(result -> {
                                    FirebaseUser user = result.getUser();
                                    if (user == null) {
                                        Toast.makeText(RegisterActivity.this, "Erro inesperado ao criar usuário", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    String uid = user.getUid();

                                    java.util.HashMap<String, Object> payload = new java.util.HashMap<>();
                                    payload.put("usuario_id", uid);
                                    payload.put("nome", nome);
                                    payload.put("username", username);
                                    payload.put("email", email);
                                    payload.put("foto_perfil", "");
                                    payload.put("bio", bio);

                                    java.util.HashMap<String, Object> links = new java.util.HashMap<>();
                                    links.put("site", site);
                                    links.put("github", github);
                                    links.put("linkedin", linkedin);
                                    payload.put("links", links);

                                    java.util.HashMap<String, Object> estatisticas = new java.util.HashMap<>();
                                    estatisticas.put("apps_publicados", 0);
                                    estatisticas.put("likes_recebidos", 0);
                                    estatisticas.put("downloads_totais", 0);
                                    payload.put("estatisticas", estatisticas);

                                    String now = java.time.Instant.now().toString();
                                    payload.put("data_criacao", now);
                                    payload.put("ultimo_login", now);

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);
                                    ref.setValue(payload)
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(RegisterActivity.this, getTranslatedString(pro.sketchware.R.string.auth_register_success), Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}


