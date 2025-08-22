package pro.sketchware.activities.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pro.sketchware.R;
import pro.sketchware.activities.auth.LoginActivity;
import pro.sketchware.activities.main.activities.MainActivity;

public class ProfileActivity extends AppCompatActivity {
    
    private MaterialToolbar toolbar;
    private MaterialTextView userNameText;
    private MaterialTextView userEmailText;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        
        // Verificar se usuário está logado
        if (currentUser == null) {
            goToLogin();
            return;
        }
        
        // Inicializar views
        initViews();
        
        // Configurar toolbar
        setupToolbar();
        
        // Carregar dados do usuário
        loadUserData();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        userNameText = findViewById(R.id.user_name_text);
        userEmailText = findViewById(R.id.user_email_text);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Perfil");
        }
    }
    
    private void loadUserData() {
        if (currentUser == null) return;
        
        // Mostrar email do Firebase Auth
        userEmailText.setText(currentUser.getEmail());
        
        // Buscar nome do usuário no Realtime Database
        mDatabase.child("users").child(currentUser.getUid()).child("nome")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String userName = dataSnapshot.getValue(String.class);
                            if (userName != null) {
                                userNameText.setText(userName);
                            } else {
                                userNameText.setText("Usuário");
                            }
                        } else {
                            userNameText.setText("Usuário");
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        userNameText.setText("Usuário");
                    }
                });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_publish_project) {
            // TODO: Implementar publicação de projeto
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void logout() {
        // Fazer logout do Firebase Auth
        mAuth.signOut();
        
        // Ir para MainActivity (que redirecionará para Login se necessário)
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
    }
    
    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

