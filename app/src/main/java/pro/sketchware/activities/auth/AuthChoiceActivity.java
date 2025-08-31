package pro.sketchware.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.MainActivity;

public class AuthChoiceActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister, btnSkipAuth;
    private ImageView ivLogo;
    private TextView tvTitle, tvSubtitle;
    private MaterialCardView cardBenefits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_choice);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnSkipAuth = findViewById(R.id.btn_skip_auth);
        ivLogo = findViewById(R.id.iv_logo);
        cardBenefits = findViewById(R.id.card_benefits);

        // Find title and subtitle views
        View headerLayout = findViewById(R.id.header_layout);
        if (headerLayout != null) {
            tvTitle = headerLayout.findViewById(R.id.tv_title);
            tvSubtitle = headerLayout.findViewById(R.id.tv_subtitle);
        }

        // Setup animations
        setupAnimations();

        // Setup click listeners
        setupClickListeners();
    }

    private void setupAnimations() {
        // Logo animation
        Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setInterpolator(new android.view.animation.DecelerateInterpolator());
        ivLogo.startAnimation(fadeIn);

        // Title and subtitle animations
        if (tvTitle != null) {
            Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            slideUp.setDuration(800);
            slideUp.setStartOffset(300);
            tvTitle.startAnimation(slideUp);
        }

        if (tvSubtitle != null) {
            Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            slideUp.setDuration(800);
            slideUp.setStartOffset(500);
            tvSubtitle.startAnimation(slideUp);
        }

        // Card benefits animation
        Animation scaleIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        scaleIn.setDuration(600);
        scaleIn.setStartOffset(800);
        cardBenefits.startAnimation(scaleIn);

        // Buttons animations
        Animation buttonSlideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        buttonSlideUp.setDuration(600);
        buttonSlideUp.setStartOffset(1000);
        btnRegister.startAnimation(buttonSlideUp);

        Animation buttonSlideUp2 = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        buttonSlideUp2.setDuration(600);
        buttonSlideUp2.setStartOffset(1200);
        btnLogin.startAnimation(buttonSlideUp2);

        Animation buttonSlideUp3 = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        buttonSlideUp3.setDuration(600);
        buttonSlideUp3.setStartOffset(1400);
        btnSkipAuth.startAnimation(buttonSlideUp3);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            // Add button press animation
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            
            startActivity(new Intent(AuthChoiceActivity.this, LoginActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnRegister.setOnClickListener(v -> {
            // Add button press animation
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            
            startActivity(new Intent(AuthChoiceActivity.this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnSkipAuth.setOnClickListener(v -> {
            // Add button press animation
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            
            startActivity(new Intent(AuthChoiceActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog when user tries to go back
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit Sketchware IA?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
