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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.PublishAppActivity;
import pro.sketchware.activities.main.activities.UserProfileActivity;
import pro.sketchware.activities.main.adapters.LojaAdapter;
import pro.sketchware.activities.main.fragments.loja.AppItem;

public class UserDraftsFragment extends Fragment {

    private RecyclerView recyclerView;
    private MaterialTextView tvEmptyState;
    private LojaAdapter adapter;
    private List<AppItem> drafts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_drafts, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        
        setupRecyclerView();
        loadUserDrafts();
        
        return view;
    }

    private void setupRecyclerView() {
        adapter = new LojaAdapter(drafts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnAppClickListener(new LojaAdapter.OnAppClickListener() {
            @Override
            public void onAppClick(AppItem app) {
                // Navegar para edição do rascunho
                Toast.makeText(requireContext(), "Funcionalidade de edição em desenvolvimento", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadClick(AppItem app) {
                // Rascunhos não podem ser baixados
                Toast.makeText(requireContext(), "Rascunhos não podem ser baixados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserDrafts() {
        if (getActivity() instanceof UserProfileActivity) {
            UserProfileActivity activity = (UserProfileActivity) getActivity();
            drafts.clear();
            drafts.addAll(activity.getUserDrafts());
            updateUI();
        }
    }

    private void updateUI() {
        if (drafts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Você não tem rascunhos salvos.\nCrie um novo aplicativo para começar!");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserDrafts();
    }
}
