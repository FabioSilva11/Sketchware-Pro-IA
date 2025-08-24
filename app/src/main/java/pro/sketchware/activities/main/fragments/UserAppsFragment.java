package pro.sketchware.activities.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.DetalhesActivity;
import pro.sketchware.activities.main.adapters.LojaAdapter;
import pro.sketchware.activities.main.fragments.loja.AppItem;

public class UserAppsFragment extends Fragment {

    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private MaterialTextView tvEmptyState;
    private LojaAdapter adapter;
    private List<AppItem> apps;

    public static UserAppsFragment newInstance(List<AppItem> apps) {
        UserAppsFragment fragment = new UserAppsFragment();
        fragment.apps = apps;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_apps, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        
        setupRecyclerView();
        updateUI();
        
        return view;
    }

    private void setupRecyclerView() {
        adapter = new LojaAdapter(apps);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnAppClickListener(new LojaAdapter.OnAppClickListener() {
            @Override
            public void onAppClick(AppItem app) {
                // Navegar para detalhes do app
                Intent intent = new Intent(requireContext(), DetalhesActivity.class);
                intent.putExtra("app_id", app.getAppId());
                intent.putExtra("app_title", app.getNome());
                intent.putExtra("app_description", app.getDescricaoLonga());
                intent.putExtra("app_category", app.getDescricaoCurta());
                intent.putExtra("app_icon_url", app.getIcone());
                startActivity(intent);
            }

            @Override
            public void onDownloadClick(AppItem app) {
                // Implementar download
                Toast.makeText(requireContext(), "Download iniciado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (apps == null || apps.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    public void updateApps(List<AppItem> newApps) {
        this.apps = newApps;
        if (adapter != null) {
            updateUI();
        }
    }
}
