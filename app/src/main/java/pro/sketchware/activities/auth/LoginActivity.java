package pro.sketchware.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import pro.sketchware.R;
import pro.sketchware.activities.auth.RegisterActivity;
import pro.sketchware.activities.main.activities.MainActivity;

public class LoginActivity extends AppCompatActivity {
    
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private MaterialTextView forgotPasswordText;
    private View progressBar;
    
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Inicializar views
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Verificar se já está logado
        checkCurrentUser();
    }
    
    private void initViews() {
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        forgotPasswordText = findViewById(R.id.forgot_password_text);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        forgotPasswordText.setOnClickListener(v -> {
            // TODO: Implementar recuperação de senha
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Usuário já está logado, ir para MainActivity
            goToMainActivity();
        }
    }
    
    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        // Validações
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email é obrigatório");
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
        
        // Mostrar progress bar
        showProgress(true);
        
        // Tentar fazer login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgress(false);
                        
                        if (task.isSuccessful()) {
                            // Login bem-sucedido
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, 
                                    "Bem-vindo, " + user.getEmail(), 
                                    Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        } else {
                            // Login falhou
                            String errorMessage = "Falha no login: " + task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        registerButton.setEnabled(!show);
    }
    
    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


