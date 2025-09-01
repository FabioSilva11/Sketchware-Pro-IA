package pro.sketchware.activities.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import pro.sketchware.R;
import pro.sketchware.activities.auth.AuthManager;
import pro.sketchware.activities.auth.LoginActivity;

public class ProfileActivity extends AppCompatActivity {
    
    private AuthManager authManager;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar AuthManager
        authManager = AuthManager.getInstance();
        
        // Verificar se o usuário está logado
        if (!isUserLoggedIn()) {
            // Usuário não está logado - redirecionar para LoginActivity
            Toast.makeText(this, "Você precisa estar logado para acessar o perfil", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_profile);
        
        // Aqui você pode adicionar a lógica para carregar os dados do perfil do usuário
        loadUserProfile();
    }
    
    /**
     * Verifica se o usuário está logado
     * @return true se o usuário estiver logado, false caso contrário
     */
    private boolean isUserLoggedIn() {
        return authManager != null && authManager.isFirebaseAvailable() && authManager.isUserLoggedIn();
    }
    
    /**
     * Carrega os dados do perfil do usuário logado
     */
    private void loadUserProfile() {
        if (authManager.getCurrentUser() != null) {
            // Aqui você pode implementar a lógica para carregar e exibir os dados do perfil
            // Por exemplo, buscar dados do Firebase Database ou exibir informações básicas
            String userEmail = authManager.getCurrentUser().getEmail();
            String userName = authManager.getCurrentUser().getDisplayName();
            
            // TODO: Implementar a exibição dos dados do perfil na UI
        }
    }
}


