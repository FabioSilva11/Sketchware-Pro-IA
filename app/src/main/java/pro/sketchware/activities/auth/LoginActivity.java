package pro.sketchware.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton, registerButton;
    private MaterialTextView forgotPasswordText;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Auth Manager
        authManager = AuthManager.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        forgotPasswordText = findViewById(R.id.forgot_password_text);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listeners
        loginButton.setOnClickListener(v -> performLogin());
        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
        forgotPasswordText.setOnClickListener(v -> {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, getString(R.string.auth_forgot_password_coming_soon), Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            emailInput.setError(getString(R.string.auth_email_required));
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError(getString(R.string.auth_password_required));
            return;
        }

        // Check if Firebase is available
        if (!authManager.isFirebaseAvailable()) {
            Toast.makeText(this, "Firebase is not configured. Login temporarily unavailable.", Toast.LENGTH_LONG).show();
            return;
        }

        // Show progress
        setLoading(true);

        // Perform Firebase authentication
        try {
            authManager.getAuth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        setLoading(false);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = authManager.getCurrentUser();
                            if (user != null) {
                                try {
                                    FirebaseMessaging.getInstance().subscribeToTopic("all");
                                } catch (Exception ignored) {}
                                // Navigate to MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : getString(R.string.auth_login_failed);
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (IllegalStateException e) {
            setLoading(false);
            Toast.makeText(this, "Firebase is not configured. Login temporarily unavailable.", Toast.LENGTH_LONG).show();
        }
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        registerButton.setEnabled(!isLoading);
        forgotPasswordText.setEnabled(!isLoading);
    }
}
