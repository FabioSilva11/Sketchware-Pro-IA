package pro.sketchware.activities.main.fragments.loja;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pro.sketchware.activities.main.adapters.LojaAdapter;
import pro.sketchware.activities.store.StoreDetailActivity;
import pro.sketchware.R;

public class LojaFragment extends Fragment {
    private LojaAdapter lojaAdapter;
    private final List<HashMap<String, Object>> items = new ArrayList<>();

    private View rootView;

    private RecyclerView recyclerView;

    public static LojaFragment newInstance() {
        return new LojaFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_loja, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        recyclerView = rootView.findViewById(R.id.recycler);

        lojaAdapter = new LojaAdapter(items, app -> {
            android.content.Intent i = new android.content.Intent(requireContext(), StoreDetailActivity.class);
            i.putExtra("app_id", String.valueOf(app.get("app_id")));
            startActivity(i);
        });
        
        recyclerView.setAdapter(lojaAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);

        SwipeRefreshLayout swipeRefresh = rootView.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::loadApps);

        loadApps();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
    }

    private void loadApps() {
        // Show loading
        
        try {
            FirebaseApp.initializeApp(requireContext());
        } catch (Throwable ignored) {}
        
        java.util.List<FirebaseApp> apps = FirebaseApp.getApps(requireContext());
        if (apps.isEmpty()) {
            rootView.findViewById(R.id.empty_state).setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            ((SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh)).setRefreshing(false);
            return;
        }
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("apps");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    java.util.HashMap<String, Object> map = new java.util.HashMap<>();
                    map.put("app_id", s.child("app_id").getValue());
                    map.put("nome", s.child("nome").getValue());
                    map.put("icone", s.child("icone").getValue());
                    map.put("descricao_curta", s.child("descricao_curta").getValue());
                    
                    // Add category and subcategory with null check
                    Object categoria = s.child("categoria").getValue();
                    Object subcategoria = s.child("subcategoria").getValue();
                    map.put("categoria", categoria);
                    map.put("subcategoria", subcategoria);
                    
                    // Add statistics
                    Object estatisticas = s.child("estatisticas").getValue();
                    map.put("estatisticas", estatisticas);
                    
                    items.add(map);
                }
                
                lojaAdapter.notifyDataSetChanged();
                rootView.findViewById(R.id.empty_state).setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                ((SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh)).setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                rootView.findViewById(R.id.empty_state).setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                ((SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh)).setRefreshing(false);
            }
        });
    }

    private void hideLoading() {
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


}


