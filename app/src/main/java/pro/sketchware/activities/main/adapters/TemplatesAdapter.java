package pro.sketchware.activities.main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.besome.sketch.adapters.ProjectsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import a.a.a.yB;
import pro.sketchware.R;
import pro.sketchware.activities.main.fragments.templates.TemplatesFragment;
import pro.sketchware.activities.main.adapters.TemplatesItemBinding;

public class TemplatesAdapter extends RecyclerView.Adapter<TemplatesAdapter.TemplateViewHolder> {
    private final TemplatesFragment templatesFragment;
    private final List<HashMap<String, Object>> shownTemplates = new ArrayList<>();
    private final List<HashMap<String, Object>> allTemplates;

    public TemplatesAdapter(TemplatesFragment templatesFragment, List<HashMap<String, Object>> allTemplates) {
        this.templatesFragment = templatesFragment;
        this.allTemplates = new ArrayList<>();
        if (allTemplates != null) {
            this.allTemplates.addAll(allTemplates);
            this.shownTemplates.addAll(allTemplates);
        }
        System.out.println("TemplatesAdapter: Constructor called with " + (allTemplates != null ? allTemplates.size() : 0) + " templates");
        System.out.println("TemplatesAdapter: Constructor - allTemplates size: " + this.allTemplates.size());
        System.out.println("TemplatesAdapter: Constructor - shownTemplates size: " + this.shownTemplates.size());
    }

    public void setAllTemplates(List<HashMap<String, Object>> templates) {
        System.out.println("TemplatesAdapter: setAllTemplates called with " + (templates != null ? templates.size() : 0) + " templates");
        
        allTemplates.clear();
        shownTemplates.clear();
        
        if (templates != null && !templates.isEmpty()) {
            allTemplates.addAll(templates);
            shownTemplates.addAll(templates);
            System.out.println("TemplatesAdapter: Added templates to lists");
        }
        
        System.out.println("TemplatesAdapter: allTemplates size: " + allTemplates.size());
        System.out.println("TemplatesAdapter: shownTemplates size: " + shownTemplates.size());
        
        notifyDataSetChanged();
        System.out.println("TemplatesAdapter: notifyDataSetChanged called");
    }

    public void filterData(String query) {
        List<HashMap<String, Object>> newTemplates = query.isEmpty() ? allTemplates : new ArrayList<>();
        if (!query.isEmpty()) {
            for (HashMap<String, Object> template : allTemplates) {
                if (matchesQuery(template, query)) {
                    newTemplates.add(template);
                }
            }
        }
        shownTemplates.clear();
        shownTemplates.addAll(newTemplates);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        System.out.println("TemplatesAdapter: getItemCount called, returning " + shownTemplates.size());
        return shownTemplates.size();
    }

    private boolean matchesQuery(HashMap<String, Object> templateMap, String searchQuery) {
        searchQuery = searchQuery.toLowerCase();
        for (String key : Arrays.asList("sc_id", "my_ws_name", "my_app_name", "my_sc_pkg_name")) {
            if (yB.c(templateMap, key).toLowerCase().contains(searchQuery)) {
                return true;
            }
        }
        return false;
    }



    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        System.out.println("TemplatesAdapter: onBindViewHolder called for position " + position);
        HashMap<String, Object> templateMap = shownTemplates.get(position);
        String scId = yB.c(templateMap, "sc_id");

        // Configurar ícone do Sketchware para templates
        holder.binding.imgIcon.setImageResource(R.drawable.sketch_app_icon);

        // Configurar apenas o nome do projeto (o resto já está no XML)
        holder.binding.projectName.setText(yB.c(templateMap, "my_app_name"));
        
        // Marcar como template
        holder.itemView.setTag("template");

        // Configurar click para usar o template
        holder.binding.getRoot().setOnClickListener(v -> {});

        // Configurar click no ícone para mostrar opções
        holder.binding.imgIcon.setOnClickListener(v -> {});



        // Configurar long click para mostrar opções
        holder.binding.getRoot().setOnLongClickListener(v -> true);
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TemplatesItemBinding binding = TemplatesItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TemplateViewHolder(binding);
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        final TemplatesItemBinding binding;

        TemplateViewHolder(TemplatesItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
