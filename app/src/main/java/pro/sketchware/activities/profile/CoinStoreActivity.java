package pro.sketchware.activities.profile;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import pro.sketchware.R;

public class CoinStoreActivity extends BaseAppCompatActivity {

    private MaterialToolbar toolbar;
    private GridView gridView;
    private final List<CoinPack> packs = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_store);

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.coin_store_title));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setTitle(getString(R.string.coin_store_title));
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        gridView = findViewById(R.id.grid);
        seedDummyData();
        gridView.setAdapter(new CoinPackAdapter(packs));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CoinPack pack = packs.get(position);
                Bundle params = new Bundle();
                params.putString("pack_id", pack.id);
                params.putInt("coins", pack.coins);
                params.putString("price", pack.price);
                mAnalytics.logEvent("coin_pack_click", params);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "CoinStoreActivity");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "CoinStoreActivity");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    private void seedDummyData() {
        packs.clear();
        packs.add(new CoinPack("pack_1", 100, "$0.99"));
        packs.add(new CoinPack("pack_2", 250, "$1.99"));
        packs.add(new CoinPack("pack_3", 500, "$3.49"));
        packs.add(new CoinPack("pack_4", 1200, "$6.99"));
        packs.add(new CoinPack("pack_5", 2500, "$12.99"));
        packs.add(new CoinPack("pack_6", 5500, "$24.99"));
    }

    private static class CoinPack {
        final String id;
        final int coins;
        final String price;
        CoinPack(String id, int coins, String price) {
            this.id = id;
            this.coins = coins;
            this.price = price;
        }
    }

    private class CoinPackAdapter extends BaseAdapter {
        private final List<CoinPack> data;
        CoinPackAdapter(List<CoinPack> data) { this.data = data; }

        @Override
        public int getCount() { return data.size(); }

        @Override
        public Object getItem(int position) { return data.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coin_pack, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            CoinPack pack = data.get(position);
            holder.coins.setText(String.valueOf(pack.coins));
            holder.price.setText(pack.price);
            return convertView;
        }

        class ViewHolder {
            final TextView coins;
            final TextView price;
            final ImageView icon;
            ViewHolder(@NonNull View root) {
                coins = root.findViewById(R.id.tv_coins);
                price = root.findViewById(R.id.tv_price);
                icon = root.findViewById(R.id.iv_icon);
            }
        }
    }
}


