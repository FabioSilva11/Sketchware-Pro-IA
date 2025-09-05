package pro.sketchware.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pro.sketchware.activities.profile.fragments.ProfilePostsFragment;
import pro.sketchware.activities.profile.fragments.ProfileSkillsFragment;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 2;

    public ProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ProfilePostsFragment();
            case 1:
                return new ProfileSkillsFragment();
            default:
                return new ProfilePostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public String getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Posts";
            case 1:
                return "Skills";
            default:
                return "Posts";
        }
    }
}
