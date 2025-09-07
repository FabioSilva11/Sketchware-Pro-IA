package com.besome.sketch.design;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import pro.sketchware.R;
import pro.sketchware.activities.resourceseditor.components.utils.StringsEditorManager;
import pro.sketchware.databinding.PalletCustomviewBinding;
import pro.sketchware.databinding.ResourcesEditorFragmentBinding;
import pro.sketchware.databinding.ViewStringEditorAddBinding;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.utility.XmlUtil;

public class StringsTabFragment extends Fragment {

    private ResourcesEditorFragmentBinding binding;
    private final ArrayList<HashMap<String, Object>> stringsList = new ArrayList<>();
    private final HashMap<Integer, String> notesMap = new HashMap<>();
    private final StringsEditorManager stringsEditorManager = new StringsEditorManager();
    private boolean hasUnsavedChanges;
    private String filePath;
    private StringsListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ResourcesEditorFragmentBinding.inflate(inflater, container, false);
        stringsEditorManager.sc_id = DesignActivity.sc_id;
        updateStringsList();
        return binding.getRoot();
    }

    public void refreshList() {
        updateStringsList();
    }

    private void updateStringsList() {
        String baseDir = a.a.a.wq.b(DesignActivity.sc_id) + "/files/resource/values/";
        filePath = baseDir + "strings.xml";

        ArrayList<HashMap<String, Object>> defaultStrings = new ArrayList<>();
        stringsEditorManager.convertXmlStringsToListMap(FileUtil.readFileIfExist(filePath), defaultStrings);
        notesMap.clear();
        notesMap.putAll(stringsEditorManager.notesMap);

        stringsList.clear();
        stringsList.addAll(defaultStrings);
        adapter = new StringsListAdapter(stringsList);
        binding.recyclerView.setAdapter(adapter);
        updateNoContentLayout();
    }

    private void updateNoContentLayout() {
        if (stringsList.isEmpty()) {
            binding.noContentLayout.setVisibility(View.VISIBLE);
            binding.noContentTitle.setText(getString(R.string.resource_manager_no_list_title, "Strings"));
            binding.noContentBody.setText(getString(R.string.resource_manager_no_list_body, "string"));
        } else {
            binding.noContentLayout.setVisibility(View.GONE);
        }
    }

    private void showAddStringDialog() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireActivity());
        ViewStringEditorAddBinding dialogBinding = ViewStringEditorAddBinding.inflate(getLayoutInflater());
        dialog.setTitle("Create new string");
        dialog.setPositiveButton("Create", (d, which) -> {
            String key = Objects.requireNonNull(dialogBinding.stringKeyInput.getText()).toString();
            String value = Objects.requireNonNull(dialogBinding.stringValueInput.getText()).toString();
            String header = Objects.requireNonNull(dialogBinding.stringHeaderInput.getText()).toString().trim();

            if (key.isEmpty() || value.isEmpty()) {
                SketchwareUtil.toastError("Please fill in all fields");
                return;
            }

            if (stringsEditorManager.isXmlStringsExist(stringsList, key)) {
                SketchwareUtil.toastError("\"" + key + "\" is already exist");
                return;
            }

            addString(key, value, header);
            updateNoContentLayout();
        });
        dialog.setNegativeButton(getString(R.string.cancel), null);
        dialog.setView(dialogBinding.getRoot());
        dialog.show();
    }

    private void addString(String key, String text, String note) {
        hasUnsavedChanges = true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("text", text);
        stringsList.add(map);
        int position = stringsList.size() - 1;
        if (!note.isEmpty()) {
            notesMap.put(position, note);
        }
        adapter.notifyItemInserted(position);
    }

    private void editString(int position) {
        HashMap<String, Object> currentItem = stringsList.get(position);
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireActivity());
        ViewStringEditorAddBinding dialogBinding = ViewStringEditorAddBinding.inflate(getLayoutInflater());
        dialogBinding.stringKeyInput.setText((String) currentItem.get("key"));
        dialogBinding.stringValueInput.setText((String) currentItem.get("text"));
        dialogBinding.stringHeaderInput.setText(notesMap.getOrDefault(position, ""));
        if ("app_name".equals(currentItem.get("key"))) {
            dialogBinding.stringKeyInput.setEnabled(false);
        }
        dialog.setTitle("Edit string");
        dialog.setPositiveButton("Save", (d, which) -> {
            String keyInput = Objects.requireNonNull(dialogBinding.stringKeyInput.getText()).toString();
            String valueInput = Objects.requireNonNull(dialogBinding.stringValueInput.getText()).toString();
            if (keyInput.isEmpty() || valueInput.isEmpty()) {
                SketchwareUtil.toastError("Please fill in all fields");
                return;
            }
            currentItem.put("key", keyInput);
            currentItem.put("text", valueInput);
            String note = Objects.requireNonNull(dialogBinding.stringHeaderInput.getText()).toString().trim();
            if (note.isEmpty()) {
                notesMap.remove(position);
            } else {
                notesMap.put(position, note);
            }
            adapter.notifyItemChanged(position);
            hasUnsavedChanges = true;
        });
        if (!Objects.equals(currentItem.get("key"), "app_name")) {
            dialog.setNeutralButton(getString(R.string.common_word_delete), (d, which) -> {
                stringsList.remove(position);
                adapter.notifyItemRemoved(position);
                updateNoContentLayout();
                hasUnsavedChanges = true;
            });
        }
        dialog.setNegativeButton(getString(R.string.cancel), null);
        dialog.setView(dialogBinding.getRoot());
        dialog.show();
    }

    private void saveStringsFile() {
        if (!hasUnsavedChanges) return;
        XmlUtil.saveXml(filePath, stringsEditorManager.convertListMapToXmlStrings(stringsList, notesMap));
        hasUnsavedChanges = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveStringsFile();
    }

    private class StringsListAdapter extends RecyclerView.Adapter<StringsListAdapter.ViewHolder> {
        private ArrayList<HashMap<String, Object>> data;

        public StringsListAdapter(ArrayList<HashMap<String, Object>> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PalletCustomviewBinding itemBinding = PalletCustomviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HashMap<String, Object> item = data.get(position);
            String key = (String) item.get("key");
            String text = (String) item.get("text");
            holder.binding.title.setHint(key);
            holder.binding.sub.setText(text);
            if (notesMap.containsKey(position)) {
                holder.binding.tvTitle.setText(notesMap.get(position));
                holder.binding.tvTitle.setVisibility(View.VISIBLE);
            } else {
                holder.binding.tvTitle.setVisibility(View.GONE);
            }
            holder.binding.backgroundCard.setOnClickListener(v -> editString(holder.getAbsoluteAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            PalletCustomviewBinding binding;

            ViewHolder(PalletCustomviewBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}


