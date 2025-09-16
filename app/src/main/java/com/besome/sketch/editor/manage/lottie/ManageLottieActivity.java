package com.besome.sketch.editor.manage.lottie;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import pro.sketchware.R;

public class ManageLottieActivity extends BaseAppCompatActivity implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    private FloatingActionButton fab;
    private String sc_id;
    private AdView bannerAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_lottie_activity);
        sc_id = getIntent().getStringExtra("sc_id");

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        getSupportActionBar().setTitle(getTranslatedString(R.string.design_drawer_menu_title_lottie));

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(this);
        tabLayout.setupWithViewPager(viewPager);

        fab = findViewById(R.id.fab);
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> {
                showAddLottieDialog();
            });
        }

        // Initialize AdMob banner
        bannerAd = findViewById(R.id.banner_ad);
        if (bannerAd != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            bannerAd.loadAd(adRequest);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_lottie_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrollStateChanged(int state) { }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageSelected(int position) {
        View importLayout = findViewById(R.id.layout_btn_import);
        View actionLayout = findViewById(R.id.layout_btn_group);
        if (importLayout != null) importLayout.setVisibility(View.GONE);
        if (actionLayout != null) actionLayout.setVisibility(View.GONE);

        Fragment current = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
        if (current instanceof ThisProjectLottiesFragment) {
            ((ThisProjectLottiesFragment) current).unselectAll();
        } else if (current instanceof MyCollectionLottiesFragment) {
            ((MyCollectionLottiesFragment) current).unselectAll();
        }

        if (fab == null) return;
        if (position == 0) {
            fab.animate().translationY(0F).setDuration(200L).start();
            fab.show();
        } else {
            fab.animate().translationY(400F).setDuration(200L).start();
            fab.hide();
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        private final String[] titles;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            titles = new String[]{
                    getTranslatedString(R.string.design_manager_tab_title_this_project),
                    getTranslatedString(R.string.design_manager_tab_title_my_collection)
            };
        }

        @Override
        public int getCount() { return 2; }

        @Override
        public CharSequence getPageTitle(int position) { return titles[position]; }

        @Override
        @NonNull
        public Fragment getItem(int position) {
            return position == 0 ? new ThisProjectLottiesFragment() : new MyCollectionLottiesFragment();
        }
    }

    private void showAddLottieDialog() {
        // Use the same dialog type and style by launching a dialog-styled activity like Image Manager
        android.content.Intent intent = new android.content.Intent(this, AddLottieActivity.class);
        intent.putExtra("sc_id", sc_id);
        startActivity(intent);
    }

    public void setCurrentTab(int index) {
        if (viewPager != null) viewPager.setCurrentItem(index);
    }
}
