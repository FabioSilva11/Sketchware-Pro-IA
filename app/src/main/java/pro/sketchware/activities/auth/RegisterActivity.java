package pro.sketchware.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.MainActivity;

public class RegisterActivity extends AppCompatActivity {
    
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialButton registerButton;
    private MaterialTextView loginText;
    private View progressBar;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Inicializar views
        initViews();
        
        // Configurar listeners
        setupListeners();
    }
    
    private void initViews() {
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        registerButton = findViewById(R.id.register_button);
        loginText = findViewById(R.id.login_text);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupListeners() {
        registerButton.setOnClickListener(v -> attemptRegister());
        
        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void attemptRegister() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        
        // Validações
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Nome é obrigatório");
            nameInput.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email é obrigatório");
            emailInput.requestFocus();
            return;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Email inválido");
            emailInput.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Senha é obrigatória");
            passwordInput.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            passwordInput.setError("Senha deve ter pelo menos 6 caracteres");
            passwordInput.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Senhas não coincidem");
            confirmPasswordInput.requestFocus();
            return;
        }
        
        // Mostrar progress bar
        showProgress(true);
        
        // Criar conta no Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro bem-sucedido
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Salvar dados do usuário no Realtime Database
                                saveUserData(user.getUid(), name, email);
                            }
                        } else {
                            // Registro falhou
                            showProgress(false);
                            String errorMessage = "Falha no registro: " + task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void saveUserData(String userId, String name, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nome", name);
        userData.put("email", email);
        userData.put("dataCriacao", System.currentTimeMillis());
        
        mDatabase.child("users").child(userId).setValue(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showProgress(false);
                        
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, 
                                    "Conta criada com sucesso!", 
                                    Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        } else {
                            Toast.makeText(RegisterActivity.this, 
                                    "Erro ao salvar dados do usuário", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
    }
    
    private void goToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


