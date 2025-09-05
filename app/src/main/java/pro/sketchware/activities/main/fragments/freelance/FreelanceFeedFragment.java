package pro.sketchware.activities.main.fragments.freelance;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.sketchware.R;
import pro.sketchware.activities.auth.AuthManager;
import pro.sketchware.activities.profile.FreelanceDetailActivity;

public class FreelanceFeedFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyView;
    private final List<Map<String, Object>> items = new ArrayList<>();
    private FeedAdapter adapter;
    private DatabaseReference ref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_freelance_feed, container, false);
        recyclerView = root.findViewById(R.id.recycler_feed);
        swipeRefresh = root.findViewById(R.id.swipe_refresh);
        emptyView = root.findViewById(R.id.empty_view);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FeedAdapter(items);
        recyclerView.setAdapter(adapter);
        
        // Configurar refresh
        swipeRefresh.setOnRefreshListener(this::refreshPosts);
        
        ref = FirebaseDatabase.getInstance().getReference().child("freelance_posts");
        loadPosts();
        return root;
    }

    private void loadPosts() {
        String myUid = AuthManager.getInstance().getCurrentUser() != null ? AuthManager.getInstance().getCurrentUser().getUid() : null;
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Map<String, Object>> allPosts = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Map<String, Object> post = (Map<String, Object>) child.getValue();
                    if (post == null) continue;
                    Map<String, Object> owner = (Map<String, Object>) post.get("owner");
                    String ownerUid = owner != null ? (String) owner.get("uid") : null;
                    if (myUid != null && myUid.equals(ownerUid)) {
                        // skip own posts in main feed
                        continue;
                    }
                    allPosts.add(post);
                }
                
                // Embaralhar e limitar a 50 itens
                Collections.shuffle(allPosts);
                items.clear();
                items.addAll(allPosts.subList(0, Math.min(50, allPosts.size())));
                
                adapter.notifyDataSetChanged();
                emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { 
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }
    
    private void refreshPosts() {
        loadPosts();
    }

    private class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.VH> {
        private final List<Map<String, Object>> data;
        FeedAdapter(List<Map<String, Object>> data) { this.data = data; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_freelance_post, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Map<String, Object> post = data.get(position);
            String title = safeString(post.get("title"));
            String shortDesc = safeString(post.get("short_description"));
            int views = 0;
            Object vObj = post.get("views");
            if (vObj instanceof Number) views = ((Number) vObj).intValue();
            else if (vObj instanceof String) try { views = Integer.parseInt((String) vObj); } catch (Exception ignored) {}
            holder.tvTitle.setText(title);
            holder.tvShort.setText(shortDesc);
            holder.tvViews.setText(String.valueOf(views));

            // Owner and date
            Object ownerObj = post.get("owner");
            if (ownerObj instanceof java.util.Map) {
                java.util.Map owner = (java.util.Map) ownerObj;
                String ownerName = safeString(owner.get("name"));
                String ownerPhoto = safeString(owner.get("photo"));
                if (holder.itemView.findViewById(R.id.tv_owner) instanceof TextView) {
                    ((TextView) holder.itemView.findViewById(R.id.tv_owner)).setText(ownerName.isEmpty()?"Unknown":ownerName);
                }
                android.view.View avatar = holder.itemView.findViewById(R.id.iv_owner);
                if (avatar instanceof de.hdodenhof.circleimageview.CircleImageView) {
                    if (ownerPhoto != null && !ownerPhoto.isEmpty()) {
                        try {
                            com.squareup.picasso.Picasso.get()
                                .load(ownerPhoto)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into((de.hdodenhof.circleimageview.CircleImageView) avatar);
                        } catch (Exception ignored) {}
                    }
                }
            }
            long createdAt = 0L;
            Object ts = post.get("created_at");
            if (ts instanceof Number) createdAt = ((Number) ts).longValue();
            else if (ts instanceof String) try { createdAt = Long.parseLong((String) ts);} catch (Exception ignored) {}
            if (holder.itemView.findViewById(R.id.tv_date) instanceof TextView) {
                ((TextView) holder.itemView.findViewById(R.id.tv_date)).setText(createdAt>0? new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(createdAt)) : "");
            }

            // Hide skills on main feed per requirement
            if (holder.chips != null) {
                holder.chips.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FreelanceDetailActivity.class);
                intent.putExtra("post_id", safeString(post.get("id")));
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvViews, tvShort;
            com.google.android.material.chip.ChipGroup chips;
            VH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_title);
                tvViews = itemView.findViewById(R.id.tv_views);
                tvShort = itemView.findViewById(R.id.tv_short_desc);
                chips = itemView.findViewById(R.id.chips_skills);
            }
        }
    }

    private static String safeString(Object o) { return o == null ? "" : String.valueOf(o); }
}


