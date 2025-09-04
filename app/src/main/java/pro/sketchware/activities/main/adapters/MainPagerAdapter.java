package pro.sketchware.activities.main.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pro.sketchware.activities.main.fragments.projects.ProjectsFragment;
import pro.sketchware.activities.main.fragments.loja.LojaFragment;
import pro.sketchware.activities.main.fragments.freelance.FreelanceFeedFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ProjectsFragment();
            case 1:
                return new FreelanceFeedFragment();
            case 2:
                return new LojaFragment();
            default:
                return new ProjectsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Projects, Freelance, Loja
    }
}
