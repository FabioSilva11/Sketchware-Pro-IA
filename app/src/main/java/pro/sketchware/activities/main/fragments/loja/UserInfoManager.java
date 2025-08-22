package pro.sketchware.activities.main.fragments.loja;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserInfoManager {
    
    private static UserInfoManager instance;
    private final DatabaseReference usersRef;
    private final Map<String, String> userCache = new HashMap<>();
    
    private UserInfoManager() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }
    
    public static UserInfoManager getInstance() {
        if (instance == null) {
            instance = new UserInfoManager();
        }
        return instance;
    }
    
    public interface UserInfoCallback {
        void onUserInfoReceived(String userId, String userName);
        void onError(String userId, String error);
    }
    
    public void getUserName(String userId, UserInfoCallback callback) {
        // Verificar cache primeiro
        if (userCache.containsKey(userId)) {
            callback.onUserInfoReceived(userId, userCache.get(userId));
            return;
        }
        
        // Buscar no Firebase
        usersRef.child(userId).child("nome").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.getValue(String.class);
                    if (userName != null && !userName.trim().isEmpty()) {
                        // Adicionar ao cache
                        userCache.put(userId, userName);
                        callback.onUserInfoReceived(userId, userName);
                    } else {
                        callback.onUserInfoReceived(userId, "Usuário " + userId);
                    }
                } else {
                    callback.onUserInfoReceived(userId, "Usuário " + userId);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(userId, databaseError.getMessage());
            }
        });
    }
    
    public void clearCache() {
        userCache.clear();
    }
    
    public void removeFromCache(String userId) {
        userCache.remove(userId);
    }
}
