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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.PublishAppActivity;
import pro.sketchware.activities.main.adapters.LojaAdapter;
import pro.sketchware.activities.main.fragments.loja.AppItem;

public class UserDraftsFragment extends Fragment {

    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private MaterialTextView tvEmptyState;
    private MaterialButton btnCreateNew;
    private LojaAdapter adapter;
    private List<AppItem> drafts;

    public static UserDraftsFragment newInstance(List<AppItem> drafts) {
        UserDraftsFragment fragment = new UserDraftsFragment();
        fragment.drafts = drafts;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_drafts, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        btnCreateNew = view.findViewById(R.id.btn_create_new);
        
        setupRecyclerView();
        setupListeners();
        updateUI();
        
        return view;
    }

    private void setupRecyclerView() {
        adapter = new LojaAdapter(drafts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnAppClickListener(new LojaAdapter.OnAppClickListener() {
            @Override
            public void onAppClick(AppItem app) {
                // Editar rascunho
                Intent intent = new Intent(requireContext(), PublishAppActivity.class);
                intent.putExtra("edit_draft", true);
                intent.putExtra("draft_id", app.getAppId());
                startActivity(intent);
            }

            @Override
            public void onDownloadClick(AppItem app) {
                // Publicar rascunho
                Toast.makeText(requireContext(), "Publicando rascunho...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnCreateNew.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PublishAppActivity.class);
            startActivity(intent);
        });
    }

    private void updateUI() {
        if (drafts == null || drafts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            btnCreateNew.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            btnCreateNew.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    public void updateDrafts(List<AppItem> newDrafts) {
        this.drafts = newDrafts;
        if (adapter != null) {
            updateUI();
        }
    }
}
