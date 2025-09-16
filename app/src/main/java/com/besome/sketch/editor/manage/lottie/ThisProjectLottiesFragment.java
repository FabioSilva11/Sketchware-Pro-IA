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

import a.a.a.wq;
import pro.sketchware.R;
import pro.sketchware.utility.FilePathUtil;

public class ThisProjectLottiesFragment extends Fragment {
    private String sc_id;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private TextView guideView;
    private final List<File> lottieFiles = new ArrayList<>();
    private final List<Integer> selected = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lottie_list, container, false);
        recyclerView = root.findViewById(R.id.recycler);
        guideView = root.findViewById(R.id.tv_guide);
        emptyView = null;
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireActivity(), com.besome.sketch.editor.manage.image.ManageImageActivity.getImageGridColumnCount(requireContext())));
        recyclerView.setAdapter(new Adapter());
        // Ensure import bar is hidden for project tab
        View layoutBtnImport = requireActivity().findViewById(R.id.layout_btn_import);
        if (layoutBtnImport != null) layoutBtnImport.setVisibility(View.GONE);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sc_id = requireActivity().getIntent().getStringExtra("sc_id");
        loadLotties();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLotties();
    }

    private void loadLotties() {
        lottieFiles.clear();
        String base = new FilePathUtil().getPathAssets(sc_id);
        File dir = new File(base);
        File[] files = dir.listFiles((d, name) -> name != null && name.toLowerCase().endsWith(".json"));
        if (files != null) {
            for (File f : files) lottieFiles.add(f);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
        if (guideView != null) guideView.setVisibility(lottieFiles.isEmpty() ? View.VISIBLE : View.GONE);
        View layoutBtnImport = requireActivity().findViewById(R.id.layout_btn_import);
        if (layoutBtnImport != null) layoutBtnImport.setVisibility(View.GONE);
    }

    // Expose a public refresh to allow parent Activity to trigger reload
    public void refresh() {
        loadLotties();
    }

    public void unselectAll() {
        selected.clear();
        if (recyclerView != null && recyclerView.getAdapter() != null) recyclerView.getAdapter().notifyDataSetChanged();
        View actionContainer = requireActivity().findViewById(R.id.layout_btn_group);
        if (actionContainer != null) actionContainer.setVisibility(View.GONE);
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) fab.show();
    }

    public void deleteSelected() {
        if (selected.isEmpty()) return;
        // Delete in reverse index order to avoid shifting
        java.util.Collections.sort(selected, java.util.Collections.reverseOrder());
        for (int index : selected) {
            if (index >= 0 && index < lottieFiles.size()) {
                pro.sketchware.utility.FileUtil.deleteFile(lottieFiles.get(index).getAbsolutePath());
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
                String json = pro.sketchware.utility.FileUtil.readFile(file.getAbsolutePath());
                holder.preview.setAnimationFromJson(json, file.getName());
                holder.preview.setRepeatCount(LottieDrawable.INFINITE);
                holder.preview.playAnimation();
            } catch (Exception ignored) {
            }
            boolean isSelected = selected.contains(holder.getAdapterPosition());
            holder.chk.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
            holder.chk.setChecked(isSelected);
            holder.itemView.setOnLongClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (!selected.contains(pos)) selected.add(pos); else selected.remove((Integer) pos);
                notifyItemChanged(pos);
                toggleActionButtons();
                return true;
            });
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (!selected.isEmpty()) {
                    if (!selected.contains(pos)) selected.add(pos); else selected.remove((Integer) pos);
                    notifyItemChanged(pos);
                    toggleActionButtons();
                }
            });
        }

        @Override
        public int getItemCount() {
            return lottieFiles.size();
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


