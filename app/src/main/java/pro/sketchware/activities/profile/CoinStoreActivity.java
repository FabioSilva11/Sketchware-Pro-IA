package pro.sketchware.activities.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import pro.sketchware.R;

public class CoinStoreActivity extends BaseAppCompatActivity {

    private MaterialToolbar toolbar;
    private GridView gridView;
    private LinearLayout livePurchasesContainer;
    private HorizontalScrollView livePurchasesScroll;

    private final List<CoinPack> packs = new ArrayList<>();
    private final List<String> fakeNames = new ArrayList<>();
    private final List<String> coinPackNames = new ArrayList<>();
    private Handler livePurchasesHandler;
    private Runnable livePurchasesRunnable;
    private Handler scrollHandler;
    private Runnable scrollRunnable;
    private Random random = new Random();

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
        livePurchasesContainer = findViewById(R.id.live_purchases_container);
        livePurchasesScroll = findViewById(R.id.live_purchases_scroll);
        
        seedDummyData();
        initializeFakeNames();
        initializeCoinPackNames();
        gridView.setAdapter(new CoinPackAdapter(packs));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CoinPack pack = packs.get(position);
                Bundle params = new Bundle();
                params.putString("pack_id", pack.id);
                params.putInt("coins", pack.coins);
                params.putString("price", pack.price);
                params.putString("pack_title", pack.title);
                params.putBoolean("is_bestseller", pack.isBestseller);
                mAnalytics.logEvent("coin_pack_click", params);
                
                // Analytics adicional para bestseller
                if (pack.isBestseller) {
                    Bundle bestsellerParams = new Bundle();
                    bestsellerParams.putString("pack_id", pack.id);
                    bestsellerParams.putString("pack_title", pack.title);
                    mAnalytics.logEvent("bestseller_pack_clicked", bestsellerParams);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "CoinStoreActivity");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "CoinStoreActivity");
        bundle.putString("store_type", "coin_store");
        bundle.putInt("total_packs", packs.size());
        mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
        
        // Analytics para abertura da loja
        Bundle openParams = new Bundle();
        openParams.putString("store_opened", "true");
        openParams.putLong("timestamp", System.currentTimeMillis());
        mAnalytics.logEvent("coin_store_opened", openParams);
        
        startLivePurchases();
        startAutoScroll();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        // Analytics para fechamento da loja
        Bundle closeParams = new Bundle();
        closeParams.putString("store_closed", "true");
        closeParams.putLong("timestamp", System.currentTimeMillis());
        mAnalytics.logEvent("coin_store_closed", closeParams);
        
        stopLivePurchases();
        stopAutoScroll();
    }

    private void seedDummyData() {
        packs.clear();
        
        // Pacotes básicos
        packs.add(new CoinPack("pack_1", 50, "$1.99", false, "Starter Pack"));
        packs.add(new CoinPack("pack_2", 100, "$3.99", false, "Basic Pack"));
        packs.add(new CoinPack("pack_3", 200, "$7.99", false, "Standard Pack"));
        packs.add(new CoinPack("pack_4", 400, "$9.99", false, "Premium Pack"));
        
        // Pacote mais vendido (engenharia social + escassez) - MÁXIMO $15.00
        packs.add(new CoinPack("pack_bestseller", 1500, "$15.00", true, "🔥 BESTSELLER", "⚡ Only 2 left in stock!", "⭐ 10,000+ sold this week"));
        
        // Pacotes intermediários
        packs.add(new CoinPack("pack_5", 600, "$11.99", false, "Pro Pack"));
        packs.add(new CoinPack("pack_6", 800, "$12.99", false, "Elite Pack"));
        packs.add(new CoinPack("pack_7", 1200, "$14.99", false, "Ultimate Pack"));
    }
    
    private void initializeFakeNames() {
        fakeNames.clear();
        fakeNames.addAll(Arrays.asList(
            // Português
            "Ana Silva", "Carlos Santos", "Maria Oliveira", "João Costa", "Fernanda Lima",
            "Pedro Alves", "Juliana Rocha", "Rafael Pereira", "Camila Souza", "Lucas Ferreira",
            "Beatriz Nunes", "Gabriel Martins", "Larissa Dias", "Diego Rodrigues", "Natália Barbosa",
            "Thiago Cardoso", "Isabela Gomes", "Bruno Moreira", "Amanda Vieira", "Felipe Ribeiro",
            
            // Inglês
            "John Smith", "Sarah Johnson", "Michael Brown", "Emily Davis", "David Wilson",
            "Jessica Miller", "Christopher Garcia", "Ashley Martinez", "Matthew Anderson", "Amanda Taylor",
            "Joshua Thomas", "Stephanie Jackson", "Andrew White", "Nicole Harris", "Daniel Martin",
            "Brittany Thompson", "Ryan Garcia", "Samantha Martinez", "Justin Robinson", "Megan Clark",
            
            // Espanhol
            "Carlos Rodriguez", "Maria Garcia", "Jose Martinez", "Ana Lopez", "Luis Gonzalez",
            "Carmen Perez", "Antonio Sanchez", "Isabel Ramirez", "Francisco Torres", "Elena Flores",
            "Miguel Herrera", "Rosa Jimenez", "Jorge Ruiz", "Patricia Morales", "Roberto Gutierrez",
            "Monica Castillo", "Fernando Vargas", "Adriana Romero", "Sergio Mendoza", "Valeria Aguilar",
            
            // Francês
            "Pierre Dubois", "Marie Martin", "Jean Bernard", "Sophie Leroy", "Philippe Moreau",
            "Catherine Petit", "Michel Simon", "Isabelle Laurent", "Alain Roux", "Nathalie David",
            "François Thomas", "Christine Robert", "Patrick Richard", "Sylvie Durand", "Claude Blanc",
            "Françoise Lemaire", "Gérard Rousseau", "Monique Vincent", "André Henry", "Jacqueline Fabre",
            
            // Alemão
            "Hans Mueller", "Anna Schmidt", "Peter Weber", "Greta Fischer", "Klaus Wagner",
            "Ingrid Becker", "Wolfgang Schulz", "Ursula Hoffmann", "Dieter Schaefer", "Helga Koch",
            "Gunther Bauer", "Erika Richter", "Horst Klein", "Gisela Wolf", "Manfred Neumann",
            "Hildegard Schwarz", "Rudolf Zimmermann", "Elisabeth Braun", "Werner Krueger", "Margot Hofmann",
            
            // Italiano
            "Giuseppe Rossi", "Maria Bianchi", "Antonio Ferrari", "Giulia Romano", "Francesco Ricci",
            "Anna Conti", "Marco Gallo", "Elena Bruno", "Alessandro Greco", "Sofia Marino",
            "Lorenzo Giordano", "Valentina Rizzo", "Matteo Mancini", "Chiara Villa", "Andrea Costa",
            "Federica Leone", "Davide Martinelli", "Sara Lombardi", "Simone Barbieri", "Alessia Moretti",
            
            // Japonês
            "Yamada Taro", "Sato Hanako", "Suzuki Ichiro", "Takahashi Yuki", "Watanabe Kenji",
            "Ito Akiko", "Tanaka Masahiro", "Kobayashi Emiko", "Kato Hiroshi", "Yoshida Michiko",
            "Nakamura Takeshi", "Kimura Sachiko", "Kawasaki Daisuke", "Ogawa Yoko", "Ishida Koji",
            "Matsumoto Noriko", "Hayashi Shigeru", "Fujita Mariko", "Abe Tetsuo", "Murakami Kumiko",
            
            // Chinês
            "Wang Wei", "Li Ming", "Zhang Lei", "Chen Jing", "Liu Gang",
            "Yang Mei", "Huang Qiang", "Zhao Li", "Wu Bin", "Zhou Fang",
            "Xu Jian", "Sun Ping", "Ma Hui", "Zhu Lin", "Guo Tao",
            "He Ying", "Luo Xin", "Cao Min", "Deng Lei", "Feng Yan",
            
            // Coreano
            "Kim Min-jun", "Lee So-young", "Park Ji-hoon", "Choi Hye-jin", "Jung Seung-ho",
            "Kang Min-ji", "Yoon Dong-hyun", "Im Soo-jin", "Shin Kyung-ho", "Han Ji-eun",
            "Oh Min-seok", "Jang Hye-rim", "Song Jae-ho", "Kwon So-mi", "Bae Sung-min",
            "Ryu Ji-hye", "Hwang Min-kyu", "Seo Yeon-joo", "Cho Hyun-woo", "Moon Ji-ae",
            
            // Russo
            "Ivan Petrov", "Elena Smirnova", "Sergey Volkov", "Olga Kozlova", "Dmitry Novikov",
            "Tatiana Morozova", "Alexey Sokolov", "Natalia Lebedeva", "Andrey Popov", "Svetlana Fomina",
            "Vladimir Orlov", "Irina Kuznetsova", "Mikhail Stepanov", "Larisa Medvedeva", "Nikolai Zaitsev",
            "Galina Sokolova", "Pavel Volkov", "Valentina Petrova", "Yuri Smirnov", "Nina Kozlova",
            
            // Árabe
            "Ahmed Hassan", "Fatima Al-Zahra", "Mohammed Ali", "Aisha Rahman", "Omar Ibrahim",
            "Khadija Mahmoud", "Hassan Abdullah", "Zainab Hussein", "Yusuf Ahmad", "Mariam Khalil",
            "Ibrahim Saleh", "Amina Farid", "Khalid Nasser", "Layla Mansour", "Tariq Saad",
            "Nour El-Din", "Rashid Bakr", "Hala Mostafa", "Said Kamal", "Dina Ashraf",
            
            // Hindi
            "Raj Kumar", "Priya Sharma", "Amit Singh", "Sunita Patel", "Vikram Gupta",
            "Kavita Joshi", "Ravi Verma", "Meera Agarwal", "Suresh Reddy", "Anita Malhotra",
            "Deepak Jain", "Pooja Saxena", "Manoj Tiwari", "Rekha Iyer", "Naveen Nair",
            "Shilpa Rao", "Arjun Menon", "Swati Krishnan", "Rohit Pillai", "Divya Nair"
        ));
    }
    
    private void initializeCoinPackNames() {
        coinPackNames.clear();
        coinPackNames.addAll(Arrays.asList(
            "Starter Pack", "Basic Pack", "Standard Pack", "Premium Pack", 
            "🔥 BESTSELLER", "Pro Pack", "Elite Pack", "Ultimate Pack"
        ));
    }
    
    private void startLivePurchases() {
        if (livePurchasesHandler == null) {
            livePurchasesHandler = new Handler(Looper.getMainLooper());
        }
        
        livePurchasesRunnable = new Runnable() {
            @Override
            public void run() {
                showRandomPurchase();
                
                // Analytics para compra fictícia
                Bundle params = new Bundle();
                params.putString("fake_purchase", "true");
                params.putString("pack_name", coinPackNames.get(random.nextInt(coinPackNames.size())));
                mAnalytics.logEvent("fake_purchase_displayed", params);
                
                // Próxima compra em 3-8 segundos
                int delay = 3000 + random.nextInt(5000);
                livePurchasesHandler.postDelayed(this, delay);
            }
        };
        
        // Primeira compra em 2-5 segundos
        int initialDelay = 2000 + random.nextInt(3000);
        livePurchasesHandler.postDelayed(livePurchasesRunnable, initialDelay);
    }
    
    private void stopLivePurchases() {
        if (livePurchasesHandler != null && livePurchasesRunnable != null) {
            livePurchasesHandler.removeCallbacks(livePurchasesRunnable);
        }
    }
    
    private void showRandomPurchase() {
        if (livePurchasesContainer == null) return;
        
        String name = fakeNames.get(random.nextInt(fakeNames.size()));
        String packName = coinPackNames.get(random.nextInt(coinPackNames.size()));
        
        // Criar view da compra
        View purchaseView = LayoutInflater.from(this).inflate(R.layout.item_live_purchase, livePurchasesContainer, false);
        
        TextView nameText = purchaseView.findViewById(R.id.tv_purchase_name);
        TextView packText = purchaseView.findViewById(R.id.tv_purchase_pack);
        
        nameText.setText(name);
        packText.setText(packName);
        
        // Adicionar à container (sempre no final para o letreiro)
        livePurchasesContainer.addView(purchaseView);
        
        // Limitar a 10 compras para não sobrecarregar
        if (livePurchasesContainer.getChildCount() > 10) {
            livePurchasesContainer.removeViewAt(0);
        }
        
        // Animar entrada
        purchaseView.setAlpha(0f);
        purchaseView.animate()
            .alpha(1f)
            .setDuration(500)
            .start();
        
        // Remover após 8 segundos
        purchaseView.postDelayed(() -> {
            if (livePurchasesContainer != null && purchaseView.getParent() != null) {
                purchaseView.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        if (livePurchasesContainer != null) {
                            livePurchasesContainer.removeView(purchaseView);
                        }
                    })
                    .start();
            }
        }, 8000);
    }
    
    private void startAutoScroll() {
        if (scrollHandler == null) {
            scrollHandler = new Handler(Looper.getMainLooper());
        }
        
        scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (livePurchasesScroll != null && livePurchasesContainer.getChildCount() > 0) {
                    // Scroll suave para a direita
                    livePurchasesScroll.smoothScrollBy(2, 0);
                }
                scrollHandler.postDelayed(this, 50); // 50ms = 20 FPS
            }
        };
        
        scrollHandler.post(scrollRunnable);
    }
    
    private void stopAutoScroll() {
        if (scrollHandler != null && scrollRunnable != null) {
            scrollHandler.removeCallbacks(scrollRunnable);
        }
    }

    private static class CoinPack {
        final String id;
        final int coins;
        final String price;
        final boolean isBestseller;
        final String title;
        final String urgencyText;
        final String socialProof;
        
        CoinPack(String id, int coins, String price, boolean isBestseller, String title) {
            this(id, coins, price, isBestseller, title, null, null);
        }
        
        CoinPack(String id, int coins, String price, boolean isBestseller, String title, String urgencyText, String socialProof) {
            this.id = id;
            this.coins = coins;
            this.price = price;
            this.isBestseller = isBestseller;
            this.title = title;
            this.urgencyText = urgencyText;
            this.socialProof = socialProof;
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
            holder.title.setText(pack.title);
            
            // Configurar estilo para bestseller
            if (pack.isBestseller) {
                // Título em destaque
                holder.title.setTextColor(0xFFFF6B35); // Cor laranja para destaque
                holder.title.setTextSize(14);
                holder.title.setTypeface(null, android.graphics.Typeface.BOLD);
                
                // Preço em destaque
                holder.price.setTextColor(0xFFFF6B35);
                holder.price.setTextSize(18);
                
                // Coins em destaque
                holder.coins.setTextColor(0xFF2E7D32);
                holder.coins.setTextSize(20);
                
                // Mostrar texto de urgência se disponível
                if (pack.urgencyText != null) {
                    holder.urgencyText.setVisibility(View.VISIBLE);
                    holder.urgencyText.setText(pack.urgencyText);
                    holder.urgencyText.setTextColor(0xFFFF4444); // Vermelho para urgência
                    holder.urgencyText.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    holder.urgencyText.setVisibility(View.GONE);
                }
                
                // Mostrar prova social se disponível
                if (pack.socialProof != null) {
                    holder.socialProof.setVisibility(View.VISIBLE);
                    holder.socialProof.setText(pack.socialProof);
                    holder.socialProof.setTextColor(0xFF4CAF50); // Verde para prova social
                    holder.socialProof.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    holder.socialProof.setVisibility(View.GONE);
                }
                
                // Destacar o card
                convertView.setBackgroundColor(0xFFFFF3E0); // Fundo laranja claro
                convertView.setElevation(4); // Mesma elevação dos outros cards
                
            } else {
                // Reset para pacotes normais
                holder.title.setTextColor(0xFF666666);
                holder.title.setTextSize(12);
                holder.title.setTypeface(null, android.graphics.Typeface.NORMAL);
                holder.price.setTextColor(0xFF2196F3);
                holder.price.setTextSize(16);
                holder.coins.setTextColor(0xFF333333);
                holder.coins.setTextSize(18);
                holder.urgencyText.setVisibility(View.GONE);
                holder.socialProof.setVisibility(View.GONE);
                convertView.setBackgroundColor(0xFFFFFFFF); // Fundo branco
                convertView.setElevation(4); // Elevação normal
            }
            
            return convertView;
        }

        class ViewHolder {
            final TextView coins;
            final TextView price;
            final TextView title;
            final TextView urgencyText;
            final TextView socialProof;
            final ImageView icon;
            ViewHolder(@NonNull View root) {
                coins = root.findViewById(R.id.tv_coins);
                price = root.findViewById(R.id.tv_price);
                title = root.findViewById(R.id.tv_title);
                urgencyText = root.findViewById(R.id.tv_urgency);
                socialProof = root.findViewById(R.id.tv_social_proof);
                icon = root.findViewById(R.id.iv_icon);
            }
        }
    }
}


