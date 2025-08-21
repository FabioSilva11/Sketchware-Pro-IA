package pro.sketchware.activities.main.fragments.loja;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pro.sketchware.activities.main.adapters.LojaAdapter;
import pro.sketchware.databinding.FragmentLojaBinding;

public class LojaFragment extends Fragment {
    private FragmentLojaBinding binding;
    private LojaAdapter lojaAdapter;
    private final List<HashMap<String, Object>> items = new ArrayList<>();

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

        lojaAdapter = new LojaAdapter(items);
        binding.templates.setAdapter(lojaAdapter);
        binding.templates.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.templates.setHasFixedSize(true);

        binding.swipeRefresh.setOnRefreshListener(() -> binding.swipeRefresh.setRefreshing(false));

        showEmptyState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showEmptyState() {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.templates.setVisibility(View.GONE);
        binding.emptyStateContainer.setVisibility(View.VISIBLE);
    }
}


