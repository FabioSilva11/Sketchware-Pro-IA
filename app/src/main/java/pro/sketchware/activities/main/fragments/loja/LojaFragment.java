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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pro.sketchware.databinding.FragmentLojaBinding;
import pro.sketchware.activities.main.adapters.LojaAdapter;
import pro.sketchware.R;

public class LojaFragment extends Fragment {
    private FragmentLojaBinding binding;
    private LojaAdapter lojaAdapter;
    private final List<AppItem> apps = new ArrayList<>();
    private DatabaseReference databaseReference;

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

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("apps");

        // Configurar RecyclerView
        lojaAdapter = new LojaAdapter(apps);
        binding.lojaProjetos.setAdapter(lojaAdapter);
        binding.lojaProjetos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.lojaProjetos.setHasFixedSize(true);

        // Configurar listener de click
        lojaAdapter.setOnAppClickListener(new LojaAdapter.OnAppClickListener() {
            @Override
            public void onAppClick(AppItem app) {
                // Mostrar detalhes do app (pode ser implementado depois)
                Toast.makeText(requireContext(), "App: " + app.getNome(), Toast.LENGTH_SHORT).show();
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
        
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                apps.clear();
                
                for (DataSnapshot appSnapshot : dataSnapshot.getChildren()) {
                    try {
                        AppItem app = appSnapshot.getValue(AppItem.class);
                        if (app != null) {
                            // Definir o appId baseado na chave do Firebase
                            app.setAppId(appSnapshot.getKey());
                            apps.add(app);
                        }
                    } catch (Exception e) {
                        // Log do erro para debug
                        System.err.println("Erro ao carregar app: " + e.getMessage());
                    }
                }
                
                // Atualizar UI na thread principal
                requireActivity().runOnUiThread(() -> {
                    updateUIState();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Erro ao carregar apps: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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


