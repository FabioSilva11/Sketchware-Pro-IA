package pro.sketchware.activities.main.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pro.sketchware.activities.main.fragments.projects.ProjectsFragment;
import pro.sketchware.activities.main.fragments.ChatBotFragment;
import pro.sketchware.activities.main.fragments.ZabbarFragment;

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
                return ZabbarFragment.newInstance();
            case 2:
                return ChatBotFragment.newInstance();  
            default:
                return new ProjectsFragment();
        }
    }

    @Override
    public int getItemCount() {
        // Projects, ChatBot, Zabbar
        return 3;
    }
}
