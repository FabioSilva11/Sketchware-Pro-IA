package pro.sketchware.activities.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pro.sketchware.R;
import pro.sketchware.activities.auth.AuthManager;
import pro.sketchware.activities.auth.CategoryManager;
import pro.sketchware.activities.auth.LoginActivity;
import pro.sketchware.activities.main.activities.MainActivity;
import pro.sketchware.adapters.SkillChipAdapter;

public class ProfileActivity extends BaseAppCompatActivity {
    
    private AuthManager authManager;
    
    // Views do layout
    private CircleImageView profileImage;
    private TextView fullNameText;
    private TextView userTypeText;
    private TextView phoneNumberText;
    private TextView emailUserText;
    private TextView genderUserText;
    private TextView cepUserText;
    private TextView coinText;
    private TextView birthdayText;
    private RecyclerView skillsRecyclerView;
    private View logoutLayout;
    private ProgressBar progressBar;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    
    // Toolbar
    private MaterialToolbar toolbar;
    
    // Loading dialog
    private AlertDialog loadingDialog;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar AuthManager
        authManager = AuthManager.getInstance();
        
        // Verificar se o usuário está logado
        if (!isUserLoggedIn()) {
            // Usuário não está logado - redirecionar para LoginActivity
            Toast.makeText(this, "You need to be logged in to access the profile", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_profile);
        
        // Inicializar views
        initializeViews();
        
        // Configurar listeners
        setupListeners();
        
        // Show loading dialog
        showLoadingDialog();
        
        // Carregar dados do perfil
        loadUserProfile();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Track screen view with analytics
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "ProfileActivity");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ProfileActivity");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
        startAutoScroll();
    }
    
    /**
     * Inicializa as views do layout
     */
    private void initializeViews() {
        profileImage = findViewById(R.id.circleimageview1);
        toolbar = findViewById(R.id.toolbar);
        fullNameText = findViewById(R.id.textview1);
        userTypeText = findViewById(R.id.textview2);
        phoneNumberText = findViewById(R.id.phone_nunber);
        emailUserText = findViewById(R.id.gamail);
        genderUserText = findViewById(R.id.sexo);
        cepUserText = findViewById(R.id.home_cep);
        coinText = findViewById(R.id.coin);
        birthdayText = findViewById(R.id.birthday);
        skillsRecyclerView = findViewById(R.id.recyclerview_skills);
        
        // Configurar dados iniciais
        fullNameText.setText("Loading...");
        userTypeText.setText("Loading...");
        phoneNumberText.setText("Loading...");
        emailUserText.setText("Loading...");
        genderUserText.setText("Loading...");
        cepUserText.setText("Loading...");
        coinText.setText("Loading...");
        birthdayText.setText("Loading...");
    }
    
    /**
     * Configura os listeners dos elementos interativos
     */
    private void setupListeners() {
        
        // Toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Profile");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setTitle("Profile");
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }
    
    /**
     * Shows a non-dismissible loading dialog
     */
    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_loading);
        builder.setCancelable(false);
        
        loadingDialog = builder.create();
        loadingDialog.show();
    }
    
    /**
     * Hides the loading dialog
     */
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
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
            String userId = authManager.getCurrentUser().getUid();
            
            // Buscar dados do usuário no Firebase Database
            authManager.getDatabase().child("users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                displayUserData(dataSnapshot);
                            } else {
                                // Dados não encontrados, mostrar dados básicos do Firebase Auth
                                displayBasicUserData();
                            }
                        }
                        
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            String errorMessage = "Error loading profile data";
                            if (databaseError.getCode() == DatabaseError.NETWORK_ERROR) {
                                errorMessage = "Connection error. Please check your internet.";
                            } else if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                                errorMessage = "Permission denied to access profile data.";
                            }
                            
                            Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            // Fallback para dados básicos
                            displayBasicUserData();
                        }
                    });
        } else {
            // Usuário não encontrado
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            hideLoadingDialog();
            performLogout();
        }
    }
    
    /**
     * Exibe os dados completos do usuário vindos do Firebase Database
     */
    private void displayUserData(DataSnapshot dataSnapshot) {
        // Nome completo
        String name = dataSnapshot.child("name").getValue(String.class);
        if (name != null && !name.isEmpty()) {
            fullNameText.setText(name);
        } else {
            fullNameText.setText("Name not provided");
        }
        
        // Email
        String email = dataSnapshot.child("email").getValue(String.class);
        if (email != null && !email.isEmpty()) {
            emailUserText.setText(email);
        } else {
            emailUserText.setText("Email not provided");
        }
        
        // Telefone
        String phone = dataSnapshot.child("phone_number").getValue(String.class);
        if (phone != null && !phone.isEmpty()) {
            phoneNumberText.setText(formatPhoneNumber(phone));
        } else {
            phoneNumberText.setText("Phone not provided");
        }
        
        // Gender
        String gender = dataSnapshot.child("gender").getValue(String.class);
        if (gender != null && !gender.isEmpty()) {
            genderUserText.setText(gender);
        } else {
            genderUserText.setText("Gender not provided");
        }
        
        // CEP
        String cep = dataSnapshot.child("home_cep").getValue(String.class);
        if (cep != null && !cep.isEmpty()) {
            cepUserText.setText(formatCep(cep));
        } else {
            cepUserText.setText("CEP not provided");
        }
        
        // Coin
        String coin = dataSnapshot.child("coin").getValue(String.class);
        if (coin != null && !coin.isEmpty()) {
            coinText.setText(coin);
        } else {
            coinText.setText("0");
        }
        
        // Birthday
        String birthday = dataSnapshot.child("birthday").getValue(String.class);
        if (birthday != null && !birthday.isEmpty()) {
            birthdayText.setText(birthday);
        } else {
            birthdayText.setText("Not provided");
        }
        
        // Imagem de perfil
        String profileUrl = dataSnapshot.child("profile").getValue(String.class);
        if (profileUrl != null && !profileUrl.isEmpty()) {
            Picasso.get()
                    .load(profileUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(profileImage);
        }
        
        // Definir tipo de usuário baseado nas categorias
        setUserType(dataSnapshot);
        
        // Carregar categorias selecionadas
        loadUserCategories(dataSnapshot);
        
        // Hide loading dialog
        hideLoadingDialog();
    }
    
    /**
     * Exibe dados básicos do usuário (fallback)
     */
    private void displayBasicUserData() {
        if (authManager.getCurrentUser() != null) {
            String email = authManager.getCurrentUser().getEmail();
            String displayName = authManager.getCurrentUser().getDisplayName();
            
            if (displayName != null && !displayName.isEmpty()) {
                fullNameText.setText(displayName);
            } else {
                fullNameText.setText("User");
            }
            
            if (email != null && !email.isEmpty()) {
                emailUserText.setText(email);
            } else {
                emailUserText.setText("Email not provided");
            }
            
            phoneNumberText.setText("Phone not provided");
            genderUserText.setText("Gender not provided");
            cepUserText.setText("CEP not provided");
            coinText.setText("0");
            birthdayText.setText("Not provided");
            userTypeText.setText("Developer");
        }
        
        // Hide loading dialog
        hideLoadingDialog();
    }
    
    /**
     * Define o tipo de usuário baseado nas categorias selecionadas
     */
    private void setUserType(DataSnapshot dataSnapshot) {
        DataSnapshot categoriesSnapshot = dataSnapshot.child("sub_category_ids");
        if (categoriesSnapshot.exists()) {
            List<String> categoryIds = new ArrayList<>();
            
            // Verificar se é uma lista (array) ou um objeto
            if (categoriesSnapshot.hasChildren()) {
                // É um objeto com children (estrutura do Firebase)
                for (DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
                    String categoryId = categorySnapshot.getValue(String.class);
                    if (categoryId != null) {
                        categoryIds.add(categoryId);
                    }
                }
            } else {
                // Tentar ler como string direta (pode ser um array serializado)
                String categoriesString = categoriesSnapshot.getValue(String.class);
                if (categoriesString != null && !categoriesString.isEmpty()) {
                    // Para simplificar, usar a primeira categoria se for uma string
                    categoryIds.add(categoriesString);
                }
            }
            
            if (!categoryIds.isEmpty()) {
                // Determinar tipo baseado na primeira categoria
                String primaryCategoryId = categoryIds.get(0);
                CategoryManager.SubCategory subCategory = CategoryManager.getInstance()
                        .getSubCategoryById(primaryCategoryId);
                
                if (subCategory != null) {
                    userTypeText.setText(subCategory.getTitle());
                } else {
                    userTypeText.setText("Developer");
                }
            } else {
                userTypeText.setText("Developer");
            }
        } else {
            userTypeText.setText("Developer");
        }
    }
    
    /**
     * Carrega e exibe as categorias selecionadas pelo usuário
     */
    private void loadUserCategories(DataSnapshot dataSnapshot) {
        DataSnapshot categoriesSnapshot = dataSnapshot.child("sub_category_ids");
        List<String> categoryNames = new ArrayList<>();
        
        if (categoriesSnapshot.exists()) {
            // Verificar se é uma lista (array) ou um objeto
            if (categoriesSnapshot.hasChildren()) {
                // É um objeto com children (estrutura do Firebase)
                for (DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
                    String categoryId = categorySnapshot.getValue(String.class);
                    if (categoryId != null) {
                        CategoryManager.SubCategory subCategory = CategoryManager.getInstance()
                                .getSubCategoryById(categoryId);
                        if (subCategory != null) {
                            categoryNames.add(subCategory.getTitle());
                        }
                    }
                }
            } else {
                // Tentar ler como string direta (pode ser um array serializado)
                String categoriesString = categoriesSnapshot.getValue(String.class);
                if (categoriesString != null && !categoriesString.isEmpty()) {
                    // Aqui você pode implementar parsing de array se necessário
                    categoryNames.add("Categorias: " + categoriesString);
                }
            }
        }
        
        if (skillsRecyclerView != null) {
            List<String> data = !categoryNames.isEmpty() ? categoryNames : new ArrayList<String>() {{ add("No categories selected"); }};
            skillsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            skillsRecyclerView.setAdapter(new SkillRecyclerAdapter(data));
            skillsRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }

    private void startAutoScroll() {
        if (skillsRecyclerView == null) return;
        if (autoScrollHandler == null) autoScrollHandler = new Handler(Looper.getMainLooper());
        if (autoScrollRunnable != null) return;
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (skillsRecyclerView != null) {
                    skillsRecyclerView.scrollBy(2, 0);
                    autoScrollHandler.postDelayed(this, 16);
                }
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
            autoScrollRunnable = null;
        }
    }

    private class SkillRecyclerAdapter extends RecyclerView.Adapter<SkillRecyclerAdapter.SkillViewHolder> {
        private final List<String> items;

        SkillRecyclerAdapter(List<String> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Chip chip = new Chip(parent.getContext());
            chip.setTextSize(14);
            chip.setClickable(false);
            chip.setCheckable(false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            chip.setLayoutParams(lp);
            int padH = (int) (12 * parent.getResources().getDisplayMetrics().density);
            chip.setPadding(padH, 0, padH, 0);
            return new SkillViewHolder(chip);
        }

        @Override
        public void onBindViewHolder(@NonNull SkillRecyclerAdapter.SkillViewHolder holder, int position) {
            if (items.isEmpty()) return;
            String item = items.get(position % items.size());
            holder.chip.setText(item);
        }

        @Override
        public int getItemCount() {
            return items.isEmpty() ? 0 : Integer.MAX_VALUE;
        }

        class SkillViewHolder extends RecyclerView.ViewHolder {
            Chip chip;
            SkillViewHolder(@NonNull View itemView) {
                super(itemView);
                chip = (Chip) itemView;
            }
        }
    }
    
    /**
     * Realiza o logout do usuário
     */
    private void performLogout() {
        if (authManager != null) {
            authManager.signOut();
            Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show();
            
            // Redirecionar para a tela principal
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
    
    /**
     * Formata o número de telefone para exibição
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        
        // Remove todos os caracteres não numéricos
        String numbersOnly = phone.replaceAll("[^\\d]", "");
        
        // Formata baseado no tamanho
        if (numbersOnly.length() == 11) {
            // (11) 99999-9999
            return String.format("(%s) %s-%s", 
                numbersOnly.substring(0, 2),
                numbersOnly.substring(2, 7),
                numbersOnly.substring(7));
        } else if (numbersOnly.length() == 10) {
            // (11) 9999-9999
            return String.format("(%s) %s-%s", 
                numbersOnly.substring(0, 2),
                numbersOnly.substring(2, 6),
                numbersOnly.substring(6));
        } else {
            // Retorna o número original se não conseguir formatar
            return phone;
        }
    }
    
    /**
     * Formata o CEP para exibição
     */
    private String formatCep(String cep) {
        if (cep == null || cep.isEmpty()) {
            return cep;
        }
        
        // Remove todos os caracteres não numéricos
        String numbersOnly = cep.replaceAll("[^\\d]", "");
        
        // Formata CEP brasileiro (8 dígitos)
        if (numbersOnly.length() == 8) {
            return String.format("%s-%s", 
                numbersOnly.substring(0, 5),
                numbersOnly.substring(5));
        } else {
            // Retorna o CEP original se não conseguir formatar
            return cep;
        }
    }
}


