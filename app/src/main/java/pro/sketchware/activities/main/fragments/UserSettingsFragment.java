package pro.sketchware.activities.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;

import pro.sketchware.R;
import pro.sketchware.activities.auth.LoginActivity;
import pro.sketchware.activities.main.activities.MainActivity;

public class UserSettingsFragment extends Fragment {

    private MaterialButton btnLogout;
    private MaterialButton btnEditProfile;
    private MaterialButton btnPrivacySettings;
    private MaterialButton btnNotificationSettings;
    private MaterialCardView cardAccountInfo;
    private MaterialTextView tvAccountStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);
        
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnPrivacySettings = view.findViewById(R.id.btn_privacy_settings);
        btnNotificationSettings = view.findViewById(R.id.btn_notification_settings);
        cardAccountInfo = view.findViewById(R.id.card_account_info);
        tvAccountStatus = view.findViewById(R.id.tv_account_status);
        
        setupListeners();
        updateAccountInfo();
        
        return view;
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logout());
        
        btnEditProfile.setOnClickListener(v -> {
            // Implementar edição de perfil
            Toast.makeText(requireContext(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        
        btnPrivacySettings.setOnClickListener(v -> {
            // Implementar configurações de privacidade
            Toast.makeText(requireContext(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        
        btnNotificationSettings.setOnClickListener(v -> {
            // Implementar configurações de notificação
            Toast.makeText(requireContext(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateAccountInfo() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            tvAccountStatus.setText("Conta ativa");
            cardAccountInfo.setCardBackgroundColor(requireContext().getColor(R.color.md_theme_light_primary_container));
        } else {
            tvAccountStatus.setText("Conta inativa");
            cardAccountInfo.setCardBackgroundColor(requireContext().getColor(R.color.md_theme_light_error_container));
        }
    }

    private void logout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        
        // Ir para MainActivity (que redirecionará para Login se necessário)
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        Toast.makeText(requireContext(), "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
    }
}
