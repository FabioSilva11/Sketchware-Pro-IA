package pro.sketchware.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
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
    private TextInputEditText phoneInput, pinInput, cpfInput, cnpjInput, curpInput, rfcInput;
    private TextInputEditText birthdayInput, cepInput, homeCepInput, razaoSocialInput, foundedAtInput;
    private AutoCompleteTextView genderInput, companySizeInput;
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
        cpfInput = findViewById(R.id.cpf_input);
        cnpjInput = findViewById(R.id.cnpj_input);
        curpInput = findViewById(R.id.curp_input);
        rfcInput = findViewById(R.id.rfc_input);
        birthdayInput = findViewById(R.id.birthday_input);
        cepInput = findViewById(R.id.cep_input);
        homeCepInput = findViewById(R.id.home_cep_input);
        razaoSocialInput = findViewById(R.id.razao_social_input);
        foundedAtInput = findViewById(R.id.founded_at_input);
        genderInput = findViewById(R.id.gender_input);
        companySizeInput = findViewById(R.id.company_size_input);
        registerButton = findViewById(R.id.register_button);
        loginText = findViewById(R.id.login_text);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listeners
        registerButton.setOnClickListener(v -> performRegister());
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Setup spinners
        setupGenderSpinner();
        setupCompanySizeSpinner();
    }

    private void performRegister() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String pin = pinInput.getText().toString().trim();
        String cpf = cpfInput.getText().toString().trim();
        String cnpj = cnpjInput.getText().toString().trim();
        String curp = curpInput.getText().toString().trim();
        String rfc = rfcInput.getText().toString().trim();
        String birthday = birthdayInput.getText().toString().trim();
        String gender = genderInput.getText().toString().trim();
        String cep = cepInput.getText().toString().trim();
        String homeCep = homeCepInput.getText().toString().trim();
        String companySize = companySizeInput.getText().toString().trim();
        String razaoSocial = razaoSocialInput.getText().toString().trim();
        String foundedAt = foundedAtInput.getText().toString().trim();

        // Validate required inputs
        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.auth_name_required));
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

        // Store form data temporarily and go to category selection
        Intent intent = new Intent(RegisterActivity.this, CategorySelectionActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("phone", phone);
        intent.putExtra("pin", pin);
        intent.putExtra("cpf", cpf);
        intent.putExtra("cnpj", cnpj);
        intent.putExtra("curp", curp);
        intent.putExtra("rfc", rfc);
        intent.putExtra("birthday", birthday);
        intent.putExtra("gender", gender);
        intent.putExtra("cep", cep);
        intent.putExtra("home_cep", homeCep);
        intent.putExtra("company_size", companySize);
        intent.putExtra("razao_social", razaoSocial);
        intent.putExtra("founded_at", foundedAt);
        
        startActivityForResult(intent, CATEGORY_SELECTION_REQUEST);
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
            String cpf = data.getStringExtra("cpf");
            String cnpj = data.getStringExtra("cnpj");
            String curp = data.getStringExtra("curp");
            String rfc = data.getStringExtra("rfc");
            String birthday = data.getStringExtra("birthday");
            String gender = data.getStringExtra("gender");
            String cep = data.getStringExtra("cep");
            String homeCep = data.getStringExtra("home_cep");
            String companySize = data.getStringExtra("company_size");
            String razaoSocial = data.getStringExtra("razao_social");
            String foundedAt = data.getStringExtra("founded_at");
            
            // Show progress
            setLoading(true);
            
            // Create user with Firebase Auth
            authManager.getAuth().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success, save user data to Firebase Database
                            FirebaseUser user = authManager.getCurrentUser();
                            if (user != null) {
                                saveUserData(user.getUid(), name, email, phone, pin, cpf, cnpj, curp, rfc, 
                                           birthday, gender, cep, homeCep, companySize, razaoSocial, foundedAt, finalSelectedCategories);
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
                            String cpf, String cnpj, String curp, String rfc, String birthday, 
                            String gender, String cep, String homeCep, String companySize, 
                            String razaoSocial, String foundedAt, List<String> selectedCategories) {
        // Create user data map with all required fields
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone_number", phone);
        userData.put("cpf", cpf);
        userData.put("cnpj", cnpj);
        userData.put("curp", curp);
        userData.put("rfc", rfc);
        userData.put("birthday", birthday);
        userData.put("pin", pin);
        userData.put("gender", gender);
        userData.put("cep", cep);
        userData.put("home_cep", homeCep);
        userData.put("compane_size", companySize);
        userData.put("razao_social", razaoSocial);
        userData.put("founded_at", foundedAt);
        userData.put("sub_category_ids", selectedCategories.toArray(new String[0]));
        userData.put("token", "");
        userData.put("pin", pin);
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

    private void setupGenderSpinner() {
        String[] genderOptions = {"Masculino", "Feminino", "Não-binário", "Prefiro não informar"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, genderOptions);
        genderInput.setAdapter(adapter);
    }

    private void setupCompanySizeSpinner() {
        String[] companySizeOptions = {"1-10 funcionários", "11-50 funcionários", "51-200 funcionários", 
                                     "201-1000 funcionários", "1000+ funcionários", "Não aplicável"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, companySizeOptions);
        companySizeInput.setAdapter(adapter);
    }
}
