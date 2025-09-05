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
import android.content.res.ColorStateList;
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
import com.google.android.material.color.MaterialColors;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
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
import pro.sketchware.activities.profile.PublishFreelanceActivity;

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
    private RecyclerView myPostsRecyclerView;
    private View logoutLayout;
    private ProgressBar progressBar;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    
    private DatabaseReference userRef;
    private ValueEventListener userListener;
    
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
    protected void onStart() {
        super.onStart();
        attachLiveUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachLiveUpdates();
    }

    private void attachLiveUpdates() {
        if (!isUserLoggedIn()) return;
        String userId = authManager.getCurrentUser().getUid();
        if (userRef == null) {
            userRef = authManager.getDatabase().child("users").child(userId);
        }
        if (userListener == null) {
            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        displayUserData(snapshot);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // no-op
                }
            };
        }
        userRef.addValueEventListener(userListener);
    }

    private void detachLiveUpdates() {
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
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
        } else if (item.getItemId() == R.id.action_buy_coins) {
            startActivity(new Intent(this, CoinStoreActivity.class));
            // Analytics
            Bundle params = new Bundle();
            params.putString("from", "profile_menu");
            mAnalytics.logEvent("open_coin_store", params);
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
        loadMyPosts();
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
        myPostsRecyclerView = findViewById(R.id.recyclerview_my_posts);
        
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

        View fab = findViewById(R.id.fab_action);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                startActivity(new Intent(this, PublishFreelanceActivity.class));
            });
        }
    }

    private void loadMyPosts() {
        if (!isUserLoggedIn()) return;
        String uid = authManager.getCurrentUser().getUid();
        androidx.recyclerview.widget.LinearLayoutManager lm = new androidx.recyclerview.widget.LinearLayoutManager(this);
        myPostsRecyclerView.setLayoutManager(lm);
        java.util.List<java.util.Map<String, Object>> data = new java.util.ArrayList<>();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                .child("freelance_posts")
                .orderByChild("owner/uid").equalTo(uid)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        data.clear();
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            java.util.Map<String, Object> post = (java.util.Map<String, Object>) child.getValue();
                            if (post != null) data.add(post);
                        }
                        myPostsRecyclerView.setAdapter(new MyPostsAdapter(data));
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) { }
                });
    }

    private class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.VH> {
        private final java.util.List<java.util.Map<String, Object>> data;
        MyPostsAdapter(java.util.List<java.util.Map<String, Object>> data) { this.data = data; }

        @androidx.annotation.NonNull
        @Override
        public VH onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_freelance_post, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull VH holder, int position) {
            java.util.Map<String, Object> post = data.get(position);
            holder.title.setText(String.valueOf(post.get("title")));
            Object shortObj = post.get("short_description");
            if (holder.shortDesc != null) holder.shortDesc.setText(shortObj == null ? "" : String.valueOf(shortObj));
            // views
            int views = 0;
            Object vObj = post.get("views");
            if (vObj instanceof Number) views = ((Number) vObj).intValue();
            else if (vObj instanceof String) try { views = Integer.parseInt((String) vObj); } catch (Exception ignored) {}
            if (holder.views != null) holder.views.setText(String.valueOf(views));
            // owner/date
            Object ownerObj = post.get("owner");
            if (ownerObj instanceof java.util.Map) {
                java.util.Map owner = (java.util.Map) ownerObj;
                String ownerName = String.valueOf(owner.get("name"));
                android.widget.TextView tvOwner = holder.ownerName;
                if (tvOwner != null) {
                    tvOwner.setText(ownerName == null || ownerName.equals("null") || ownerName.isEmpty() ? "You" : ownerName);
                }
                String photo = String.valueOf(owner.get("photo"));
                if (holder.ownerAvatar != null && photo != null && !photo.equals("null") && !photo.isEmpty()) {
                    try {
                        com.squareup.picasso.Picasso.get()
                            .load(photo)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(holder.ownerAvatar);
                    } catch (Exception ignored) {}
                }
            }
            long createdAt = 0L;
            Object ts = post.get("created_at");
            if (ts instanceof Number) createdAt = ((Number) ts).longValue();
            else if (ts instanceof String) try { createdAt = Long.parseLong((String) ts);} catch (Exception ignored) {}
            if (holder.date != null) {
                holder.date.setText(createdAt>0? new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(createdAt)) : "");
            }
            holder.delete.setOnClickListener(v -> {
                String id = String.valueOf(post.get("id"));
                com.google.android.material.dialog.MaterialAlertDialogBuilder dlg = new com.google.android.material.dialog.MaterialAlertDialogBuilder(ProfileActivity.this);
                dlg.setTitle("Delete ad");
                dlg.setMessage("Do you want to delete this ad?");
                dlg.setPositiveButton("Delete", (d, w) -> {
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference().child("freelance_posts").child(id).removeValue();
                    data.remove(position);
                    notifyItemRemoved(position);
                });
                dlg.setNegativeButton(R.string.common_word_cancel, null);
                dlg.show();
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title; TextView shortDesc; TextView views; TextView ownerName; TextView date; View delete; de.hdodenhof.circleimageview.CircleImageView ownerAvatar;
            VH(@androidx.annotation.NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tv_title);
                shortDesc = itemView.findViewById(R.id.tv_short_desc);
                views = itemView.findViewById(R.id.tv_views);
                ownerName = itemView.findViewById(R.id.tv_owner);
                date = itemView.findViewById(R.id.tv_date);
                ownerAvatar = itemView.findViewById(R.id.iv_owner);
                delete = itemView.findViewById(R.id.btn_delete);
            }
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
        // Prefer rich structure when available
        DataSnapshot richSnapshot = dataSnapshot.child("sub_categories");
        if (richSnapshot.exists() && richSnapshot.hasChildren()) {
            DataSnapshot first = null;
            for (DataSnapshot child : richSnapshot.getChildren()) { first = child; break; }
            if (first != null) {
                String title = first.child("title").getValue(String.class);
                if (title != null && !title.isEmpty()) {
                    userTypeText.setText(title);
                    return;
                }
            }
        }

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
        DataSnapshot richSnapshot = dataSnapshot.child("sub_categories");
        List<String> categoryNames = new ArrayList<>();

        if (richSnapshot.exists() && richSnapshot.hasChildren()) {
            for (DataSnapshot child : richSnapshot.getChildren()) {
                String title = child.child("title").getValue(String.class);
                String icon = child.child("icon").getValue(String.class);
                if (title != null && !title.isEmpty()) {
                    categoryNames.add(icon != null && !icon.isEmpty() ? (icon + " " + title) : title);
                }
            }
        } else {
            DataSnapshot categoriesSnapshot = dataSnapshot.child("sub_category_ids");
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
                                String icon = subCategory.getIcon();
                                String title = subCategory.getTitle();
                                categoryNames.add(icon != null && !icon.isEmpty() ? (icon + " " + title) : title);
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
        }
        
        if (skillsRecyclerView != null) {
            List<String> data = !categoryNames.isEmpty() ? categoryNames : new ArrayList<String>() {{ add("No categories selected"); }};
            skillsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            skillsRecyclerView.setAdapter(new SkillRecyclerAdapter(data));
            skillsRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            if (skillsRecyclerView.getItemDecorationCount() == 0) {
                skillsRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(dpToPx(3)));
            }
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
            android.view.LayoutInflater inflater = android.view.LayoutInflater.from(parent.getContext());
            android.view.View view = inflater.inflate(R.layout.item_skill_chip, parent, false);
            return new SkillViewHolder(view);
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
                chip = itemView.findViewById(R.id.skill_chip);
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

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private static class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacePx;

        HorizontalSpaceItemDecoration(int spacePx) {
            this.spacePx = spacePx;
        }

        @Override
        public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            outRect.left = spacePx;
            outRect.right = spacePx;
            // Optional: no vertical spacing since item height is fixed
        }
    }
}


