package pro.sketchware.activities.about;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import mod.hey.studios.util.Helper;
import pro.sketchware.R;
import pro.sketchware.activities.about.fragments.BetaChangesFragment;
import pro.sketchware.activities.about.fragments.TeamFragment;
import pro.sketchware.activities.about.models.AboutAppViewModel;
import pro.sketchware.activities.about.models.AboutResponseModel;
import pro.sketchware.databinding.ActivityAboutAppBinding;
import pro.sketchware.utility.Network;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AboutActivity extends BaseAppCompatActivity {

    private final Network network = new Network();
    public AboutAppViewModel aboutAppData;
    private ActivityAboutAppBinding binding;
    private SharedPreferences sharedPref;
    private AdView adView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);

        binding = ActivityAboutAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        aboutAppData = new ViewModelProvider(this).get(AboutAppViewModel.class);
        sharedPref = getSharedPreferences("AppData", Activity.MODE_PRIVATE);

        initViews();
        initData();
    }

    private void initViews() {
        binding.toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));

        AboutAdapter adapter = new AboutAdapter(this);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setAdapter(adapter);

        String[] tabTitles = new String[]{
                Helper.getResString(R.string.about_team_title),
                Helper.getResString(R.string.about_beta_changes_title)
        };

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> tab.setText(tabTitles[position])).attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // No-op after removing Discord-related UI
            }
        });

        String toSelect = getIntent().getStringExtra("select");
        if (toSelect != null) {
            if ("betaChanges".equals(toSelect)) {
                binding.viewPager.setCurrentItem(1);
            }
        }

        loadBannerAd();
    }

    private void initData() {
        network.get(Helper.getResString(R.string.link_about_team), response -> {
            if (response != null) {
                sharedPref.edit().putString("aboutData", response).apply();
            } else {
                response = sharedPref.getString("aboutData", null);
            }
            if (response == null) return;

            Gson gson = new Gson();
            AboutResponseModel aboutResponseModel = gson.fromJson(response, AboutResponseModel.class);

            aboutAppData.setTeamMembers(aboutResponseModel.getTeam());
        });
    }

    private void loadBannerAd() {
        android.view.ViewGroup container = (android.view.ViewGroup) findViewById(pro.sketchware.R.id.ad_container_about);
        if (container == null) return;

        if (adView != null) {
            container.removeView(adView);
            adView.destroy();
            adView = null;
        }

        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-6598765502914364/1327212196");

        container.post(() -> {
            int containerWidthPx = container.getWidth();
            if (containerWidthPx == 0) containerWidthPx = getResources().getDisplayMetrics().widthPixels;
            float density = getResources().getDisplayMetrics().density;
            int adWidth = Math.max(1, (int) (containerWidthPx / density));
            AdSize adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
            adView.setAdSize(adSize);
            container.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        });
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        super.onDestroy();
    }

    // ----------------- classes ----------------- //

    public static class AboutAdapter extends FragmentStateAdapter {
        public AboutAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return switch (position) {
                case 1 -> new BetaChangesFragment();
                default -> new TeamFragment();
            };
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
