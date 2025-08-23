package pro.sketchware.activities.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import pro.sketchware.R;

public class UserSettingsFragment extends Fragment {

    private MaterialButton btnLogout, btnEditProfile, btnChangePassword, btnDeleteAccount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);
        
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        
        setupListeners();
        
        return view;
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logout());
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        btnDeleteAccount.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(requireContext(), "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
        requireActivity().finish();
    }
}
