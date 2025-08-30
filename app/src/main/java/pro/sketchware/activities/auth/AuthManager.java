package pro.sketchware.activities.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    
    private static AuthManager instance;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    
    private AuthManager() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }
    
    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }
    
    public FirebaseAuth getAuth() {
        return mAuth;
    }
    
    public DatabaseReference getDatabase() {
        return mDatabase;
    }
    
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }
    
    public void signOut() {
        mAuth.signOut();
    }
    
    public void saveUserData(String userId, String name, String email, String phone, String pin, 
                           String cpf, String cnpj, String curp, String rfc, String birthday, 
                           String gender, String homeCep) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone_number", phone);
        userData.put("cpf", cpf);
        userData.put("cnpj", cnpj);
        userData.put("curp", curp);
        userData.put("rfc", rfc);
        userData.put("birthday", birthday);
        userData.put("gender", gender);
        userData.put("home_cep", homeCep);
        userData.put("sub_category_ids", new String[]{});
        userData.put("token", "");
        userData.put("pin", pin);
        userData.put("utm_campaign", "");
        userData.put("utm_medium", "");
        userData.put("utm_term", "");
        userData.put("utm_content", "");
        userData.put("utm_source", "");
        userData.put("utm_gclid", "");
        userData.put("created_at", System.currentTimeMillis());
        userData.put("updated_at", System.currentTimeMillis());
        
        mDatabase.child("users").child(userId).setValue(userData);
    }
}
