package pro.sketchware.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.MainActivity;

public class AuthChoiceActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister, btnSkipAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_choice);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnSkipAuth = findViewById(R.id.btn_skip_auth);

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(AuthChoiceActivity.this, LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(AuthChoiceActivity.this, RegisterActivity.class));
        });

        btnSkipAuth.setOnClickListener(v -> {
            startActivity(new Intent(AuthChoiceActivity.this, MainActivity.class));
            finish();
        });
    }
}
