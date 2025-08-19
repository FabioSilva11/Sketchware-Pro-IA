package pro.sketchware.activities.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.besome.sketch.lib.base.BaseAppCompatActivity;

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
        binding.btnGoToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        @NonNull String email = String.valueOf(binding.email.getText()).trim();
        @NonNull String password = String.valueOf(binding.password.getText());
        @NonNull String confirmPassword = String.valueOf(binding.confirmPassword.getText());

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

        if (!TextUtils.equals(password, confirmPassword)) {
            binding.confirmPasswordLayout.setError(getTranslatedString(pro.sketchware.R.string.auth_password_mismatch));
            valid = false;
        } else {
            binding.confirmPasswordLayout.setError(null);
        }

        if (!valid) return;

        Toast.makeText(this, getTranslatedString(pro.sketchware.R.string.auth_register_success), Toast.LENGTH_SHORT).show();
        finish();
    }
}


