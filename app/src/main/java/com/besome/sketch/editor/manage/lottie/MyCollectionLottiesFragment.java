package com.besome.sketch.editor.manage.lottie;

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

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pro.sketchware.R;
import pro.sketchware.utility.FileUtil;

public class MyCollectionLottiesFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView emptyView;
    private TextView guideView;
    private final List<File> lottieFiles = new ArrayList<>();
    private final List<Integer> selected = new ArrayList<>();
    private android.widget.Button btnImport;
    private com.google.android.material.card.MaterialCardView layoutBtnImport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lottie_list, container, false);
        recyclerView = root.findViewById(R.id.recycler);
        guideView = root.findViewById(R.id.tv_guide);
        emptyView = null;
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireActivity(), com.besome.sketch.editor.manage.image.ManageImageActivity.getImageGridColumnCount(requireContext())));
        recyclerView.setAdapter(new Adapter());
        btnImport = requireActivity().findViewById(R.id.btn_import);
        layoutBtnImport = requireActivity().findViewById(R.id.layout_btn_import);
        if (btnImport != null) {
            btnImport.setOnClickListener(v -> {
                layoutBtnImport.setVisibility(View.GONE);
                importSelected();
            });
        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLotties();
    }

    public void unselectAll() {
        selected.clear();
        if (recyclerView != null && recyclerView.getAdapter() != null) recyclerView.getAdapter().notifyDataSetChanged();
        android.view.View actionContainer = requireActivity().findViewById(R.id.layout_btn_group);
        if (actionContainer != null) actionContainer.setVisibility(View.GONE);
        if (layoutBtnImport != null) layoutBtnImport.setVisibility(View.GONE);
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) fab.show();
    }

    private void loadLotties() {
        lottieFiles.clear();
        File dir = new File(a.a.a.wq.getAbsolutePathOf(a.a.a.wq.x) + File.separator + "lottie");
        if (!FileUtil.isExistFile(dir.getAbsolutePath())) {
            FileUtil.makeDir(dir.getAbsolutePath());
        }
        File[] files = dir.listFiles((d, name) -> name != null && name.toLowerCase().endsWith(".json"));
        if (files != null) {
            for (File f : files) lottieFiles.add(f);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
        if (guideView != null) guideView.setVisibility(lottieFiles.isEmpty() ? View.VISIBLE : View.GONE);
        onItemSelected();
    }

    public void deleteSelected() {
        if (selected.isEmpty()) return;
        java.util.Collections.sort(selected, java.util.Collections.reverseOrder());
        for (int index : selected) {
            if (index >= 0 && index < lottieFiles.size()) {
                FileUtil.deleteFile(lottieFiles.get(index).getAbsolutePath());
            }
        }
        selected.clear();
        loadLotties();
    }

    private class Adapter extends RecyclerView.Adapter<VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lottie_preview, parent, false);
            return new VH(item);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            File file = lottieFiles.get(position);
            holder.title.setText(file.getName());
            try {
                String json = FileUtil.readFile(file.getAbsolutePath());
                holder.preview.setAnimationFromJson(json, file.getName());
                holder.preview.setRepeatCount(LottieDrawable.INFINITE);
                holder.preview.playAnimation();
            } catch (Exception ignored) { }
            boolean isSelected = selected.contains(holder.getAdapterPosition());
            holder.chk.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
            holder.chk.setChecked(isSelected);
            holder.itemView.setOnLongClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (!selected.contains(pos)) selected.add(pos); else selected.remove((Integer) pos);
                notifyItemChanged(pos);
                toggleActionButtons();
                onItemSelected();
                return true;
            });
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (!selected.isEmpty()) {
                    if (!selected.contains(pos)) selected.add(pos); else selected.remove((Integer) pos);
                    notifyItemChanged(pos);
                    toggleActionButtons();
                    onItemSelected();
                }
            });
        }

        @Override
        public int getItemCount() { return lottieFiles.size(); }
    }

    private void onItemSelected() {
        int count = selected.size();
        if (btnImport != null && layoutBtnImport != null) {
            if (count > 0) {
                String label = getString(R.string.common_word_import).toUpperCase();
                btnImport.setText(label + " (" + count + ")");
                layoutBtnImport.setVisibility(View.VISIBLE);
            } else {
                layoutBtnImport.setVisibility(View.GONE);
            }
        }
    }

    private void importSelected() {
        String assetsDir = new pro.sketchware.utility.FilePathUtil().getPathAssets(requireActivity().getIntent().getStringExtra("sc_id"));
        pro.sketchware.utility.FileUtil.makeDir(assetsDir);

        // Build a set of existing names to avoid duplicates
        java.util.HashSet<String> usedNames = new java.util.HashSet<>();
        File assetsDirectory = new File(assetsDir);
        File[] existing = assetsDirectory.listFiles((d, n) -> n != null && n.toLowerCase().endsWith(".json"));
        if (existing != null) {
            for (File f : existing) usedNames.add(f.getName().toLowerCase());
        }

        java.util.Collections.sort(selected);
        for (int index : new ArrayList<>(selected)) {
            File src = lottieFiles.get(index);

            // Generate a unique random name like lottie_xxxxxx.json
            String uniqueName;
            do {
                String rand = java.lang.Long.toString(java.lang.System.currentTimeMillis(), 36)
                        + Integer.toString(new java.util.Random().nextInt(0xFFFF), 36);
                rand = rand.substring(Math.max(0, rand.length() - 6));
                uniqueName = ("lottie_" + rand + ".json").toLowerCase();
            } while (usedNames.contains(uniqueName));
            usedNames.add(uniqueName);

            String dest = assetsDir + File.separator + uniqueName;
            try {
                pro.sketchware.utility.FileUtil.copyFile(src.getAbsolutePath(), dest);
            } catch (Exception ignored) { }
        }
        selected.clear();
        toggleActionButtons();
        if (getActivity() instanceof ManageLottieActivity activity) {
            activity.setCurrentTab(0);
            // Force-refresh the "This Project" list immediately after import
            androidx.fragment.app.Fragment current = activity.getSupportFragmentManager().findFragmentByTag("android:switcher:" + pro.sketchware.R.id.view_pager + ":" + 0);
            if (current instanceof ThisProjectLottiesFragment) {
                ((ThisProjectLottiesFragment) current).refresh();
            }
        }
    }

    private static class VH extends RecyclerView.ViewHolder {
        final LottieAnimationView preview;
        final TextView title;
        final android.widget.CheckBox chk;
        VH(@NonNull View itemView) {
            super(itemView);
            preview = itemView.findViewById(R.id.lottie);
            title = itemView.findViewById(R.id.title);
            chk = itemView.findViewById(R.id.chk_select);
        }
    }

    private void toggleActionButtons() {
        android.view.View actionContainer = requireActivity().findViewById(R.id.layout_btn_group);
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        boolean selecting = !selected.isEmpty();
        if (actionContainer != null) actionContainer.setVisibility(selecting ? View.VISIBLE : View.GONE);
        if (fab != null) {
            if (selecting) fab.hide(); else fab.show();
        }
        android.widget.Button btnDelete = requireActivity().findViewById(R.id.btn_delete);
        android.widget.Button btnCancel = requireActivity().findViewById(R.id.btn_cancel);
        if (btnDelete != null) btnDelete.setOnClickListener(v -> { deleteSelected(); toggleActionButtons(); });
        if (btnCancel != null) btnCancel.setOnClickListener(v -> { selected.clear(); recyclerView.getAdapter().notifyDataSetChanged(); toggleActionButtons(); });
    }
}


