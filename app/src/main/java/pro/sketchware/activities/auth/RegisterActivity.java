package pro.sketchware.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    private static final int CATEGORY_SELECTION_REQUEST = 1001;
    
    private TextInputEditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private TextInputEditText phoneInput, pinInput;
    private TextInputEditText birthdayInput, homeCepInput;
    private RadioGroup genderRadioGroup;
    private MaterialButton registerButton;
    private MaterialTextView loginText;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Auth Manager
        authManager = AuthManager.getInstance();

        // Initialize views
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        phoneInput = findViewById(R.id.phone_input);
        pinInput = findViewById(R.id.pin_input);
        birthdayInput = findViewById(R.id.birthday_input);
        homeCepInput = findViewById(R.id.home_cep_input);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        registerButton = findViewById(R.id.register_button);
        loginText = findViewById(R.id.login_text);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listeners
        registerButton.setOnClickListener(v -> performRegister());
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Setup automatic formatters
        setupPhoneFormatter();
        setupDateFormatter();
        setupZipCodeFormatter();
    }

    private void setupPhoneFormatter() {
        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString().replaceAll("[^\\d]", "");
                if (phone.length() > 11) {
                    phone = phone.substring(0, 11);
                }
                
                String formatted = "";
                if (phone.length() > 0) {
                    formatted = "(" + phone.substring(0, Math.min(2, phone.length()));
                    if (phone.length() > 2) {
                        formatted += ") " + phone.substring(2, Math.min(7, phone.length()));
                        if (phone.length() > 7) {
                            formatted += "-" + phone.substring(7);
                        }
                    }
                }
                
                if (!formatted.equals(s.toString())) {
                    phoneInput.removeTextChangedListener(this);
                    phoneInput.setText(formatted);
                    phoneInput.setSelection(formatted.length());
                    phoneInput.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupDateFormatter() {
        birthdayInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String date = s.toString().replaceAll("[^\\d]", "");
                if (date.length() > 8) {
                    date = date.substring(0, 8);
                }
                
                String formatted = "";
                if (date.length() > 0) {
                    formatted = date.substring(0, Math.min(2, date.length()));
                    if (date.length() > 2) {
                        formatted += "/" + date.substring(2, Math.min(4, date.length()));
                        if (date.length() > 4) {
                            formatted += "/" + date.substring(4);
                        }
                    }
                }
                
                if (!formatted.equals(s.toString())) {
                    birthdayInput.removeTextChangedListener(this);
                    birthdayInput.setText(formatted);
                    birthdayInput.setSelection(formatted.length());
                    birthdayInput.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupZipCodeFormatter() {
        homeCepInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String zip = s.toString().replaceAll("[^\\d]", "");
                if (zip.length() > 8) {
                    zip = zip.substring(0, 8);
                }
                
                String formatted = "";
                if (zip.length() > 0) {
                    formatted = zip.substring(0, Math.min(5, zip.length()));
                    if (zip.length() > 5) {
                        formatted += "-" + zip.substring(5);
                    }
                }
                
                if (!formatted.equals(s.toString())) {
                    homeCepInput.removeTextChangedListener(this);
                    homeCepInput.setText(formatted);
                    homeCepInput.setSelection(formatted.length());
                    homeCepInput.addTextChangedListener(this);
                }
            }
        });
    }

    private void performRegister() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String pin = pinInput.getText().toString().trim();
        String birthday = birthdayInput.getText().toString().trim();
        String homeCep = homeCepInput.getText().toString().trim();
        
        // Get selected gender from radio group
        String gender = getSelectedGender();

        // Validate all required inputs
        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.auth_name_required));
            return;
        }

        if (phone.isEmpty()) {
            phoneInput.setError(getString(R.string.auth_phone_required));
            return;
        }

        if (email.isEmpty()) {
            emailInput.setError(getString(R.string.auth_email_required));
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError(getString(R.string.auth_password_required));
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError(getString(R.string.auth_confirm_password_required));
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError(getString(R.string.auth_passwords_dont_match));
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError(getString(R.string.auth_password_too_short));
            return;
        }

        if (pin.isEmpty()) {
            pinInput.setError(getString(R.string.auth_pin_required));
            return;
        }

        if (pin.length() != 6) {
            pinInput.setError(getString(R.string.auth_pin_invalid));
            return;
        }

        if (birthday.isEmpty()) {
            birthdayInput.setError(getString(R.string.auth_birthday_required));
            return;
        }

        if (gender.isEmpty()) {
            Toast.makeText(this, getString(R.string.auth_gender_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (homeCep.isEmpty()) {
            homeCepInput.setError(getString(R.string.auth_home_cep_required));
            return;
        }

        // Store form data temporarily and go to category selection
        Intent intent = new Intent(RegisterActivity.this, CategorySelectionActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("phone", phone);
        intent.putExtra("pin", pin);
        intent.putExtra("birthday", birthday);
        intent.putExtra("gender", gender);
        intent.putExtra("home_cep", homeCep);
        
        startActivityForResult(intent, CATEGORY_SELECTION_REQUEST);
    }

    private String getSelectedGender() {
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.gender_male) {
            return getString(R.string.auth_gender_male);
        } else if (selectedId == R.id.gender_female) {
            return getString(R.string.auth_gender_female);
        } else if (selectedId == R.id.gender_non_binary) {
            return getString(R.string.auth_gender_non_binary);
        } else if (selectedId == R.id.gender_prefer_not_to_say) {
            return getString(R.string.auth_gender_prefer_not_to_say);
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CATEGORY_SELECTION_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get selected categories
            List<String> selectedCategories = data.getStringArrayListExtra("selected_categories");
            if (selectedCategories == null) {
                selectedCategories = new ArrayList<>();
            }
            final List<String> finalSelectedCategories = selectedCategories;
            
            // Get form data from intent
            String name = data.getStringExtra("name");
            String email = data.getStringExtra("email");
            String password = data.getStringExtra("password");
            String phone = data.getStringExtra("phone");
            String pin = data.getStringExtra("pin");
            String birthday = data.getStringExtra("birthday");
            String gender = data.getStringExtra("gender");
            String homeCep = data.getStringExtra("home_cep");
            
            // Show progress
            setLoading(true);
            
            // Create user with Firebase Auth
            authManager.getAuth().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success, save user data to Firebase Database
                            FirebaseUser user = authManager.getCurrentUser();
                            if (user != null) {
                                saveUserData(user.getUid(), name, email, phone, pin, 
                                           birthday, gender, homeCep, finalSelectedCategories);
                            }
                        } else {
                            // If sign up fails, display a message to the user.
                            setLoading(false);
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : getString(R.string.auth_register_failed);
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void saveUserData(String userId, String name, String email, String phone, String pin, 
                            String birthday, String gender, String homeCep, List<String> selectedCategories) {
        // Create user data map with all required fields
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone_number", phone);
        userData.put("birthday", birthday);
        userData.put("pin", pin);
        userData.put("gender", gender);
        userData.put("home_cep", homeCep);
        userData.put("sub_category_ids", selectedCategories.toArray(new String[0]));
        userData.put("token", "");
        userData.put("utm_campaign", "");
        userData.put("utm_medium", "");
        userData.put("utm_term", "");
        userData.put("utm_content", "");
        userData.put("utm_source", "");
        userData.put("utm_gclid", "");
        userData.put("created_at", System.currentTimeMillis());
        userData.put("updated_at", System.currentTimeMillis());

        // Save to Firebase Database
        authManager.getDatabase().child("users").child(userId).setValue(userData)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        // Registration and data save successful
                        Toast.makeText(RegisterActivity.this, getString(R.string.auth_register_success), Toast.LENGTH_SHORT).show();
                        
                        // Navigate to MainActivity
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Data save failed
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : getString(R.string.auth_save_data_failed);
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!isLoading);
        loginText.setEnabled(!isLoading);
    }
}
