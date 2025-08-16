package pro.sketchware.activities.main.fragments.templates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.besome.sketch.adapters.ProjectsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import a.a.a.yB;
import a.a.a.wq;
import pro.sketchware.R;
import pro.sketchware.activities.main.adapters.TemplatesAdapter;
import pro.sketchware.databinding.FragmentTemplatesBinding;
import android.content.Intent;
import android.os.Environment;

import mod.hey.studios.project.backup.BackupRestoreManager;

public class TemplatesFragment extends Fragment {
    private FragmentTemplatesBinding binding;
    private TemplatesAdapter templatesAdapter;
    private final List<HashMap<String, Object>> templatesList = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static TemplatesFragment newInstance() {
        return new TemplatesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTemplatesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Mostrar estado de carregamento inicialmente
        showLoadingState();
        
        // Configurar o RecyclerView
        templatesAdapter = new TemplatesAdapter(this, templatesList);
        binding.templates.setAdapter(templatesAdapter);
        binding.templates.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.templates.setHasFixedSize(true);
        
        // Configurar o SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener(this::refreshTemplatesList);
        
        // Carregar templates
        binding.templates.post(this::refreshTemplatesList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        executorService.shutdown();
    }

    public void refreshTemplatesList() {
        if (!isAdded()) return;

        executorService.execute(() -> {
            List<HashMap<String, Object>> loadedTemplates = loadTemplatesFromDirectory();
            
            requireActivity().runOnUiThread(() -> {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                
                System.out.println("UI Thread: Updating templates list");
                System.out.println("UI Thread: Templates loaded: " + loadedTemplates.size());
                
                templatesList.clear();
                templatesList.addAll(loadedTemplates);
                
                System.out.println("UI Thread: Templates list size after update: " + templatesList.size());
                System.out.println("UI Thread: Adapter item count: " + templatesAdapter.getItemCount());
                
                // Criar uma nova lista para o adapter
                List<HashMap<String, Object>> templatesForAdapter = new ArrayList<>(templatesList);
                templatesAdapter.setAllTemplates(templatesForAdapter);
                
                System.out.println("UI Thread: After setAllTemplates, adapter item count: " + templatesAdapter.getItemCount());
                
                updateUIState();
            });
        });
    }

    private void updateUIState() {
        if (templatesList.isEmpty()) {
            showEmptyState();
        } else {
            showTemplatesList();
        }
    }

    private void showEmptyState() {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.templates.setVisibility(View.GONE);
        binding.emptyStateContainer.setVisibility(View.VISIBLE);
    }

    private void showTemplatesList() {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.emptyStateContainer.setVisibility(View.GONE);
        binding.templates.setVisibility(View.VISIBLE);
    }

    private void showLoadingState() {
        binding.loadingContainer.setVisibility(View.VISIBLE);
        binding.emptyStateContainer.setVisibility(View.GONE);
        binding.templates.setVisibility(View.GONE);
    }

    private List<HashMap<String, Object>> loadTemplatesFromDirectory() {
        List<HashMap<String, Object>> templates = new ArrayList<>();
        
        try {
            // Caminho para a pasta de templates - usar caminho completo
            String templatesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/templates";
            File templatesDir = new File(templatesPath);
            
            // Debug: verificar o caminho
            System.out.println("Templates path: " + templatesPath);
            System.out.println("Templates dir exists: " + templatesDir.exists());
            System.out.println("Templates dir absolute path: " + templatesDir.getAbsolutePath());
            System.out.println("Storage directory: " + Environment.getExternalStorageDirectory().getAbsolutePath());
            
            // Criar a pasta se não existir
            if (!templatesDir.exists()) {
                templatesDir.mkdirs();
                System.out.println("Created templates directory");
            }
            
            // Listar todos os arquivos .swb e .sdb
            File[] files = templatesDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".swb") || lowerName.endsWith(".sdb");
            });
            
            System.out.println("Found " + (files != null ? files.length : 0) + " .swb/.sdb files");
            
            // Debug: listar todos os arquivos na pasta
            File[] allFiles = templatesDir.listFiles();
            if (allFiles != null) {
                System.out.println("All files in templates directory:");
                for (File file : allFiles) {
                    System.out.println("  - " + file.getName() + " (isFile: " + file.isFile() + ", isDirectory: " + file.isDirectory() + ")");
                }
            }
            
            if (files != null) {
                for (File sdbFile : files) {
                    System.out.println("Processing file: " + sdbFile.getName());
                    try {
                        HashMap<String, Object> templateInfo = extractTemplateInfo(sdbFile);
                        if (templateInfo != null) {
                            templates.add(templateInfo);
                            System.out.println("Added template: " + templateInfo.get("sc_id"));
                        } else {
                            System.out.println("Failed to extract template info from: " + sdbFile.getName());
                        }
                    } catch (Exception e) {
                        System.out.println("Error processing file " + sdbFile.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in loadTemplatesFromDirectory: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Total templates loaded: " + templates.size());
        return templates;
    }

    private HashMap<String, Object> extractTemplateInfo(File sdbFile) {
        try {
            String fileName = sdbFile.getName().toLowerCase();
            String scId = sdbFile.getName().replace(".swb", "").replace(".sdb", "");
            
            System.out.println("Creating template info for: " + sdbFile.getName() + " (scId: " + scId + ")");
            
            // Criar informações padrão para o template
            HashMap<String, Object> templateData = new HashMap<>();
            
            // ID do template (nome do arquivo sem extensão)
            templateData.put("sc_id", scId);
            
            // Nome do projeto (nome do arquivo sem extensão)
            templateData.put("my_app_name", scId);
            templateData.put("my_ws_name", scId);
            
            // Package fictício
            templateData.put("my_sc_pkg_name", "com.template." + scId.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""));
            
            // Versão padrão
            templateData.put("sc_ver_name", "1.0");
            templateData.put("sc_ver_code", "1");
            
            // Marcar como template
            templateData.put("is_template", true);
            
            System.out.println("Created template: " + scId);
            return templateData;
            
        } catch (Exception e) {
            System.out.println("Error creating template info for " + sdbFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void onTemplateClicked(String scId) {
        // Mostrar diálogo customizado totalmente via XML
        TemplateUseDialogFragment dialog = TemplateUseDialogFragment.newInstance(scId);
        dialog.show(getChildFragmentManager(), "template_use_dialog");
    }

    public void useTemplate(String scId) {
        // Encontrar o arquivo .swb correspondente
        String templatePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/templates/" + scId + ".swb";
        File templateFile = new File(templatePath);
        
        if (!templateFile.exists()) {
            Toast.makeText(requireContext(), "Template não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Gerar um novo ID único para o projeto
        String newProjectId = generateUniqueProjectId();
        
        // Usar a BackupRestoreManager para restaurar o template como um novo projeto
        BackupRestoreManager backupManager = new BackupRestoreManager(requireActivity());
        backupManager.doRestore(templateFile.getAbsolutePath(), false); // false = não copiar local libs
        
        Toast.makeText(requireContext(), "Template copiado para a aba de projetos com sucesso!", Toast.LENGTH_SHORT).show();
    }

    private String generateUniqueProjectId() {
        // Gerar um ID único baseado no timestamp
        long timestamp = System.currentTimeMillis();
        return "template_" + timestamp;
    }
}
