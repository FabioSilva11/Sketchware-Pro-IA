package pro.sketchware.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.MainActivity;

public class CategorySelectionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView categoriesRecycler;
    private TextView selectedCountText;
    private MaterialButton continueButton, skipButton;
    private CategoryAdapter categoryAdapter;
    private Set<String> selectedSubCategoryIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        updateSelectedCount();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        categoriesRecycler = findViewById(R.id.categories_recycler);
        selectedCountText = findViewById(R.id.selected_count);
        continueButton = findViewById(R.id.btn_continue);
        skipButton = findViewById(R.id.btn_skip_categories);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter();
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(this));
        categoriesRecycler.setAdapter(categoryAdapter);
    }

    private void setupButtons() {
        continueButton.setOnClickListener(v -> {
            if (!selectedSubCategoryIds.isEmpty()) {
                // Pass selected categories back to registration
                Intent resultIntent = new Intent();
                resultIntent.putStringArrayListExtra("selected_categories", 
                    new ArrayList<>(selectedSubCategoryIds));
                
                // Pass back all the original form data
                resultIntent.putExtra("name", getIntent().getStringExtra("name"));
                resultIntent.putExtra("email", getIntent().getStringExtra("email"));
                resultIntent.putExtra("password", getIntent().getStringExtra("password"));
                resultIntent.putExtra("phone", getIntent().getStringExtra("phone"));
                resultIntent.putExtra("pin", getIntent().getStringExtra("pin"));
                resultIntent.putExtra("birthday", getIntent().getStringExtra("birthday"));
                resultIntent.putExtra("gender", getIntent().getStringExtra("gender"));
                resultIntent.putExtra("home_cep", getIntent().getStringExtra("home_cep"));
                
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show();
            }
        });

        skipButton.setOnClickListener(v -> {
            // Skip categories and go to main
            Intent intent = new Intent(CategorySelectionActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateSelectedCount() {
        selectedCountText.setText(String.valueOf(selectedSubCategoryIds.size()));
        continueButton.setEnabled(!selectedSubCategoryIds.isEmpty());
    }

    private void onSubCategorySelectionChanged(String subCategoryId, boolean isSelected) {
        if (isSelected) {
            selectedSubCategoryIds.add(subCategoryId);
        } else {
            selectedSubCategoryIds.remove(subCategoryId);
        }
        updateSelectedCount();
    }

    // Category Adapter
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        private List<CategoryManager.Category> categories = CategoryManager.getInstance().getAllCategories();

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            holder.bind(categories.get(position));
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            private TextView categoryIcon, categoryTitle, categoryDescription;
            private CheckBox categoryCheckbox;
            private RecyclerView subcategoriesRecycler;
            private SubCategoryAdapter subCategoryAdapter;

            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                categoryIcon = itemView.findViewById(R.id.category_icon);
                categoryTitle = itemView.findViewById(R.id.category_title);
                categoryDescription = itemView.findViewById(R.id.category_description);
                categoryCheckbox = itemView.findViewById(R.id.category_checkbox);
                subcategoriesRecycler = itemView.findViewById(R.id.subcategories_recycler);
            }

            public void bind(CategoryManager.Category category) {
                categoryIcon.setText(category.getIcon());
                categoryTitle.setText(category.getTitle());
                categoryDescription.setText(category.getDescription());

                // Setup subcategories
                subCategoryAdapter = new SubCategoryAdapter(category.getSubCategories());
                subcategoriesRecycler.setLayoutManager(new GridLayoutManager(itemView.getContext(), 2));
                subcategoriesRecycler.setAdapter(subCategoryAdapter);

                // Handle category checkbox
                categoryCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    // Select/deselect all subcategories
                    subCategoryAdapter.setAllSelected(isChecked);
                });

                // Update category checkbox based on subcategory selection
                subCategoryAdapter.setOnSelectionChangeListener(selectedCount -> {
                    boolean allSelected = selectedCount == category.getSubCategories().size();
                    boolean someSelected = selectedCount > 0;
                    categoryCheckbox.setChecked(allSelected);
                    categoryCheckbox.setButtonTintList(null); // Reset tint
                });
            }
        }
    }

    // SubCategory Adapter
    private class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder> {

        private List<CategoryManager.SubCategory> subCategories;
        private Set<String> selectedIds = new HashSet<>();
        private OnSelectionChangeListener listener;

        public SubCategoryAdapter(List<CategoryManager.SubCategory> subCategories) {
            this.subCategories = subCategories;
        }

        @NonNull
        @Override
        public SubCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subcategory, parent, false);
            return new SubCategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SubCategoryViewHolder holder, int position) {
            holder.bind(subCategories.get(position));
        }

        @Override
        public int getItemCount() {
            return subCategories.size();
        }

        public void setAllSelected(boolean selected) {
            selectedIds.clear();
            if (selected) {
                for (CategoryManager.SubCategory subCategory : subCategories) {
                    selectedIds.add(subCategory.getId());
                }
            }
            notifyDataSetChanged();
            updateGlobalSelection();
        }

        public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
            this.listener = listener;
        }

        private void updateGlobalSelection() {
            // Update global selection
            for (String id : selectedIds) {
                selectedSubCategoryIds.add(id);
            }
            for (CategoryManager.SubCategory subCategory : subCategories) {
                if (!selectedIds.contains(subCategory.getId())) {
                    selectedSubCategoryIds.remove(subCategory.getId());
                }
            }
            updateSelectedCount();
            
            if (listener != null) {
                listener.onSelectionChanged(selectedIds.size());
            }
        }

        class SubCategoryViewHolder extends RecyclerView.ViewHolder {
            private Chip subcategoryChip;

            public SubCategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                subcategoryChip = (Chip) itemView;
            }

            public void bind(CategoryManager.SubCategory subCategory) {
                subcategoryChip.setText(subCategory.getTitle());
                subcategoryChip.setChecked(selectedIds.contains(subCategory.getId()));
                
                subcategoryChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedIds.add(subCategory.getId());
                    } else {
                        selectedIds.remove(subCategory.getId());
                    }
                    updateGlobalSelection();
                });
            }
        }

        interface OnSelectionChangeListener {
            void onSelectionChanged(int selectedCount);
        }
    }
}
