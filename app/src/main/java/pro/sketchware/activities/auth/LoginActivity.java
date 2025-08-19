package pro.sketchware.activities.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.besome.sketch.lib.base.BaseAppCompatActivity;

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
        binding.btnGoToRegister.setOnClickListener(v -> finish());
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

        Toast.makeText(this, getTranslatedString(pro.sketchware.R.string.auth_login_success), Toast.LENGTH_SHORT).show();
        finish();
    }
}


