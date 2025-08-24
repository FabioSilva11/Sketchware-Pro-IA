package pro.sketchware.activities.main.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import pro.sketchware.activities.main.fragments.UserAppsFragment;
import pro.sketchware.activities.main.fragments.UserDraftsFragment;
import pro.sketchware.activities.main.fragments.UserSettingsFragment;
import pro.sketchware.activities.main.fragments.loja.AppItem;

public class UserProfilePagerAdapter extends FragmentStateAdapter {

    private List<AppItem> userApps;
    private List<AppItem> userDrafts;

    public UserProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void updateData(List<AppItem> userApps, List<AppItem> userDrafts) {
        this.userApps = userApps;
        this.userDrafts = userDrafts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return UserAppsFragment.newInstance(userApps);
            case 1:
                return UserDraftsFragment.newInstance(userDrafts);
            case 2:
                return new UserSettingsFragment();
            default:
                return UserAppsFragment.newInstance(userApps);
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Meus Apps, Rascunhos, Configurações
    }
}
