package pro.sketchware.activities.profile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import pro.sketchware.activities.profile.FreelanceDetailActivity;

public class ProfilePostsFragment extends Fragment {

    private RecyclerView postsRecyclerView;
    private PostsAdapter adapter;
    private List<Map<String, Object>> posts = new ArrayList<>();
    private DatabaseReference postsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile_posts, container, false);
        
        postsRecyclerView = root.findViewById(R.id.recyclerview_posts);
        
        // Configurar LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsRecyclerView.setLayoutManager(layoutManager);
        
        adapter = new PostsAdapter(posts);
        postsRecyclerView.setAdapter(adapter);
        
        loadPosts();
        
        return root;
    }

    private void loadPosts() {
        AuthManager authManager = AuthManager.getInstance();
        if (authManager != null && authManager.getCurrentUser() != null) {
            String uid = authManager.getCurrentUser().getUid();
            
            postsRef = FirebaseDatabase.getInstance().getReference().child("freelance_posts");
            postsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    posts.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Map<String, Object> post = (Map<String, Object>) child.getValue();
                        if (post == null) continue;
                        
                        Map<String, Object> owner = (Map<String, Object>) post.get("owner");
                        String ownerUid = owner != null ? (String) owner.get("uid") : null;
                        
                        // Mostrar apenas posts do usuário atual
                        if (uid.equals(ownerUid)) {
                            posts.add(post);
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

    private class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {
        private final List<Map<String, Object>> posts;

        PostsAdapter(List<Map<String, Object>> posts) {
            this.posts = posts;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_freelance_post, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            Map<String, Object> post = posts.get(position);
            holder.bind(post);
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvTitle;
            private final TextView tvShortDesc;
            private final TextView tvViews;

            PostViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_title);
                tvShortDesc = itemView.findViewById(R.id.tv_short_desc);
                tvViews = itemView.findViewById(R.id.tv_views);
            }

            void bind(Map<String, Object> post) {
                String title = safeString(post.get("title"));
                String shortDesc = safeString(post.get("short_description"));
                int views = 0;
                Object vObj = post.get("views");
                if (vObj instanceof Number) views = ((Number) vObj).intValue();
                else if (vObj instanceof String) {
                    try { views = Integer.parseInt((String) vObj); } catch (Exception ignored) {}
                }
                
                tvTitle.setText(title);
                tvShortDesc.setText(shortDesc);
                tvViews.setText(String.valueOf(views));

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), FreelanceDetailActivity.class);
                    intent.putExtra("post_id", safeString(post.get("id")));
                    v.getContext().startActivity(intent);
                });
            }
        }
    }

    private static String safeString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
