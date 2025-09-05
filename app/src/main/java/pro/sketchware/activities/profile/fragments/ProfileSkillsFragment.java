package pro.sketchware.activities.profile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pro.sketchware.R;
import pro.sketchware.activities.auth.AuthManager;
import pro.sketchware.activities.auth.CategoryManager;

public class ProfileSkillsFragment extends Fragment {

    private RecyclerView skillsRecyclerView;
    private SkillsAdapter adapter;
    private List<CategoryManager.SubCategory> skills = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile_skills, container, false);
        
        skillsRecyclerView = root.findViewById(R.id.recyclerview_skills);
        
        // Configurar GridLayoutManager com 2 colunas
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        skillsRecyclerView.setLayoutManager(layoutManager);
        
        adapter = new SkillsAdapter(skills);
        skillsRecyclerView.setAdapter(adapter);
        
        loadSkills();
        
        return root;
    }

    private void loadSkills() {
        AuthManager authManager = AuthManager.getInstance();
        if (authManager != null && authManager.getCurrentUser() != null) {
            String uid = authManager.getCurrentUser().getUid();
            
            // Carregar as skills específicas do usuário do Firebase
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    skills.clear();
                    
                    // Buscar as subcategorias do usuário
                    Object subCategoriesObj = snapshot.child("sub_categories").getValue();
                    if (subCategoriesObj instanceof List) {
                        List<Map<String, Object>> subCategoriesList = (List<Map<String, Object>>) subCategoriesObj;
                        CategoryManager categoryManager = CategoryManager.getInstance();
                        
                        for (Map<String, Object> subCategoryMap : subCategoriesList) {
                            String categoryId = (String) subCategoryMap.get("id");
                            if (categoryId != null && categoryManager != null) {
                                CategoryManager.SubCategory subCategory = categoryManager.getSubCategoryById(categoryId);
                                if (subCategory != null) {
                                    skills.add(subCategory);
                                }
                            }
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private static class SkillsAdapter extends RecyclerView.Adapter<SkillsAdapter.SkillViewHolder> {
        private final List<CategoryManager.SubCategory> skills;

        SkillsAdapter(List<CategoryManager.SubCategory> skills) {
            this.skills = skills;
        }

        @NonNull
        @Override
        public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_skill_grid, parent, false);
            return new SkillViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
            CategoryManager.SubCategory skill = skills.get(position);
            holder.bind(skill);
        }

        @Override
        public int getItemCount() {
            return skills.size();
        }

        static class SkillViewHolder extends RecyclerView.ViewHolder {
            private final TextView skillText;

            SkillViewHolder(@NonNull View itemView) {
                super(itemView);
                skillText = itemView.findViewById(R.id.text_skill);
            }

            void bind(CategoryManager.SubCategory skill) {
                String displayText = skill.getIcon() + " " + skill.getTitle();
                skillText.setText(displayText);
            }
        }
    }
}
