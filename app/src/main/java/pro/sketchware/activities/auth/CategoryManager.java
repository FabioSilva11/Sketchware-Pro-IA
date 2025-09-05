package pro.sketchware.activities.auth;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import pro.sketchware.SketchApplication;

public class CategoryManager {
    
    public static class Category {
        private String id;
        private String title;
        private String description;
        private String icon;
        private List<SubCategory> subCategories;
        
        public Category(String id, String title, String description, String icon) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.subCategories = new ArrayList<>();
        }
        
        public void addSubCategory(SubCategory subCategory) {
            this.subCategories.add(subCategory);
        }
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public List<SubCategory> getSubCategories() { return subCategories; }
    }
    
    public static class SubCategory {
        private String id;
        private String title;
        private String description;
        private String icon;
        
        public SubCategory(String id, String title, String description, String icon) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.icon = icon;
        }
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
    
    private static CategoryManager instance;
    private List<Category> categories;
    
    private CategoryManager() {
        // Carrega exclusivamente do JSON. Se falhar, mantém lista vazia.
        if (!initializeCategoriesFromJson()) {
            categories = new ArrayList<>();
            Log.w("CategoryManager", "No categories loaded; ensure assets/categories.json exists and is valid");
        }
    }
    
    public static CategoryManager getInstance() {
        if (instance == null) {
            instance = new CategoryManager();
        }
        return instance;
    }
    
    private void initializeCategories() {
        // Mantida para compatibilidade, mas sem dados hardcoded
        categories = new ArrayList<>();
    }

    /**
     * Carrega categorias de assets/categories.json
     * @return true se conseguiu carregar do JSON, false caso contrário
     */
    private boolean initializeCategoriesFromJson() {
        categories = new ArrayList<>();
        Context context = SketchApplication.getContext();
        if (context == null) return false;

        AssetManager am = context.getAssets();
        try (InputStream is = am.open("categories.json");
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONObject root = new JSONObject(sb.toString());
            JSONArray cats = root.optJSONArray("categories");
            if (cats == null) return false;

            for (int i = 0; i < cats.length(); i++) {
                JSONObject c = cats.getJSONObject(i);
                String id = c.optString("id");
                String title = c.optString("title");
                String description = c.optString("description");
                String icon = c.optString("icon");
                Category category = new Category(id, title, description, icon);

                JSONArray subs = c.optJSONArray("sub_categories");
                if (subs != null) {
                    for (int j = 0; j < subs.length(); j++) {
                        JSONObject s = subs.getJSONObject(j);
                        String sid = s.optString("id");
                        String stitle = s.optString("title");
                        String sdesc = s.optString("description");
                        String sicon = s.optString("icon");
                        category.addSubCategory(new SubCategory(sid, stitle, sdesc, sicon));
                    }
                }
                categories.add(category);
            }
            return !categories.isEmpty();
        } catch (IOException | JSONException e) {
            Log.w("CategoryManager", "Failed to load categories.json, using defaults", e);
            categories.clear();
            return false;
        }
    }
    
    public List<Category> getAllCategories() {
        return categories;
    }
    
    public Category getCategoryById(String categoryId) {
        for (Category category : categories) {
            if (category.getId().equals(categoryId)) {
                return category;
            }
        }
        return null;
    }
    
    public SubCategory getSubCategoryById(String subCategoryId) {
        for (Category category : categories) {
            for (SubCategory subCategory : category.getSubCategories()) {
                if (subCategory.getId().equals(subCategoryId)) {
                    return subCategory;
                }
            }
        }
        return null;
    }
    
    public List<String> getAllSubCategoryIds() {
        List<String> ids = new ArrayList<>();
        for (Category category : categories) {
            for (SubCategory subCategory : category.getSubCategories()) {
                ids.add(subCategory.getId());
            }
        }
        return ids;
    }
    
    public List<String> getSubCategoryIdsByCategory(String categoryId) {
        Category category = getCategoryById(categoryId);
        if (category != null) {
            List<String> ids = new ArrayList<>();
            for (SubCategory subCategory : category.getSubCategories()) {
                ids.add(subCategory.getId());
            }
            return ids;
        }
        return new ArrayList<>();
    }
}
