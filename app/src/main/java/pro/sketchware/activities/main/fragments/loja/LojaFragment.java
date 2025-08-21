package pro.sketchware.activities.main.fragments.loja;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.sketchware.activities.main.adapters.LojaAdapter;
import pro.sketchware.databinding.FragmentLojaBinding;

public class LojaFragment extends Fragment {
    private FragmentLojaBinding binding;
    private LojaAdapter lojaAdapter;
    private DatabaseReference appsRef;

    public static LojaFragment newInstance() {
        return new LojaFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLojaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lojaAdapter = new LojaAdapter();
        binding.templates.setAdapter(lojaAdapter);
        binding.templates.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.templates.setHasFixedSize(true);

        appsRef = FirebaseDatabase.getInstance().getReference("apps");

        binding.swipeRefresh.setOnRefreshListener(this::loadApps);

        showEmptyState();
        loadApps();
    }

    private void loadApps() {
        appsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LojaAdapter.AppItem> items = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Map<String, Object> map = (Map<String, Object>) child.getValue();
                    if (map == null) continue;
                    LojaAdapter.AppItem item = new LojaAdapter.AppItem();
                    item.appId = (String) map.get("app_id");
                    item.nome = (String) map.get("nome");
                    // Categoria não estava explícita; usar placeholder se existir
                    item.categoria = (String) map.getOrDefault("categoria", "");
                    item.autor = null;
                    Object publisher = map.get("publisher");
                    if (publisher instanceof Map) {
                        Object uid = ((Map<?, ?>) publisher).get("usuario_id");
                        if (uid != null) item.autor = String.valueOf(uid);
                    }
                    Object estatisticas = map.get("estatisticas");
                    if (estatisticas instanceof Map) {
                        Object rating = ((Map<?, ?>) estatisticas).get("rating");
                        if (rating instanceof Number) item.rating = ((Number) rating).doubleValue();
                    }
                    item.iconeBase64 = (String) map.get("icone");
                    items.add(item);
                }
                binding.swipeRefresh.setRefreshing(false);
                if (items.isEmpty()) {
                    showEmptyState();
                } else {
                    lojaAdapter.submitList(items);
                    showList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.swipeRefresh.setRefreshing(false);
                showEmptyState();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showEmptyState() {
        binding.templates.setVisibility(View.GONE);
        binding.emptyStateContainer.setVisibility(View.VISIBLE);
    }

    private void showList() {
        binding.templates.setVisibility(View.VISIBLE);
        binding.emptyStateContainer.setVisibility(View.GONE);
    }
}


