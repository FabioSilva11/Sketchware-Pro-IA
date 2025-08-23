package pro.sketchware.activities.main.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pro.sketchware.activities.main.fragments.UserAppsFragment;
import pro.sketchware.activities.main.fragments.UserDraftsFragment;
import pro.sketchware.activities.main.fragments.UserSettingsFragment;

public class UserProfilePagerAdapter extends FragmentStateAdapter {

    public UserProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new UserAppsFragment();
            case 1:
                return new UserDraftsFragment();
            case 2:
                return new UserSettingsFragment();
            default:
                return new UserAppsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
