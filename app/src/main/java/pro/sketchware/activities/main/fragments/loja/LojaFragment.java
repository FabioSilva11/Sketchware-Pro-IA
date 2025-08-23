package pro.sketchware.activities.main.fragments.loja;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import pro.sketchware.databinding.FragmentLojaBinding;
import pro.sketchware.activities.main.adapters.LojaAdapter;
import pro.sketchware.R;

public class LojaFragment extends Fragment {
    private FragmentLojaBinding binding;
    private LojaAdapter lojaAdapter;
    private final List<AppItem> apps = new ArrayList<>();
    private DatabaseManager databaseManager;

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

        // Inicializar DatabaseManager
        databaseManager = DatabaseManager.getInstance();

        // Configurar RecyclerView
        lojaAdapter = new LojaAdapter(apps);
        binding.lojaProjetos.setAdapter(lojaAdapter);
        binding.lojaProjetos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.lojaProjetos.setHasFixedSize(true);

        // Configurar listener de click
        lojaAdapter.setOnAppClickListener(new LojaAdapter.OnAppClickListener() {
            @Override
            public void onAppClick(AppItem app) {
                // Navegar para a DetalhesActivity
                Intent intent = new Intent(requireContext(), pro.sketchware.activities.main.activities.DetalhesActivity.class);
                intent.putExtra("app_id", app.getAppId());
                intent.putExtra("app_title", app.getNome());
                intent.putExtra("app_description", app.getDescricaoLonga());
                intent.putExtra("app_category", app.getDescricaoCurta());
                intent.putExtra("app_icon_url", app.getIcone());
                
                // Adicionar downloads e avaliações usando os novos campos
                intent.putExtra("app_downloads", app.getDownloads());
                intent.putExtra("app_rating", app.getAvaliacao_media());
                intent.putExtra("app_reviews_count", app.getNumero_avaliacoes());
                intent.putExtra("app_version", app.getVersao());
                intent.putExtra("app_screenshots", app.getScreenshots() != null ? 
                    app.getScreenshots().values().toArray(new String[0]) : new String[0]);
                
                startActivity(intent);
            }

            @Override
            public void onDownloadClick(AppItem app) {
                // Abrir URL de download
                if (app.getUrlDownload() != null && !app.getUrlDownload().isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(app.getUrlDownload()));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Erro ao abrir link de download", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Link de download não disponível", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Carregar apps do Firebase
        loadAppsFromFirebase();
    }

    private void loadAppsFromFirebase() {
        showLoadingState();
        
        // Usar o DatabaseManager para carregar dados completos
        databaseManager.loadCompleteData(new DatabaseManager.AppsLoadCallback() {
            @Override
            public void onAppsLoaded(List<AppItem> loadedApps) {
                // Atualizar UI na thread principal
                requireActivity().runOnUiThread(() -> {
                    apps.clear();
                    apps.addAll(loadedApps);
                    updateUIState();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Erro ao carregar apps: " + error, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
            }
        });
    }

    private void updateUIState() {
        if (apps.isEmpty()) {
            showEmptyState();
        } else {
            showAppsList();
        }
        
        // Notificar o adapter sobre as mudanças
        lojaAdapter.notifyDataSetChanged();
    }

    private void showEmptyState() {
        binding.progressbar1.setVisibility(View.GONE);
        binding.lojaProjetos.setVisibility(View.GONE);
        // Como não há estado vazio específico no layout, vamos mostrar uma mensagem
        Toast.makeText(requireContext(), "Nenhum app encontrado", Toast.LENGTH_SHORT).show();
    }

    private void showAppsList() {
        binding.progressbar1.setVisibility(View.GONE);
        binding.lojaProjetos.setVisibility(View.VISIBLE);
    }

    private void showLoadingState() {
        binding.progressbar1.setVisibility(View.VISIBLE);
        binding.lojaProjetos.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


