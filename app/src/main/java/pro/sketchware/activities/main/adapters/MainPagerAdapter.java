package pro.sketchware.activities.main.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pro.sketchware.activities.main.fragments.projects.ProjectsFragment;
import pro.sketchware.activities.main.fragments.loja.LojaFragment;

public class MainPagerAdapter extends FragmentStateAdapter {
    private ProjectsFragment projectsFragment;
    private LojaFragment lojaFragment;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                if (projectsFragment == null) {
                    projectsFragment = new ProjectsFragment();
                }
                return projectsFragment;
            case 1:
                if (lojaFragment == null) {
                    lojaFragment = new LojaFragment();
                }
                return lojaFragment;
            default:
                if (projectsFragment == null) {
                    projectsFragment = new ProjectsFragment();
                }
                return projectsFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Projects e Templates
    }

    public Fragment getFragment(int position) {
        // Retornar o fragmento atual baseado na posição
        switch (position) {
            case 0:
                return projectsFragment;
            case 1:
                return lojaFragment;
            default:
                return projectsFragment;
        }
    }
}
