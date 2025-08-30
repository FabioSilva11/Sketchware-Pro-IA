package pro.sketchware.activities.auth;

import java.util.ArrayList;
import java.util.List;

public class CategoryManager {
    
    public static class Category {
        private String id;
        private String title;
        private String description;
        private String icon;
        private List<SubCategory> subCategories;
        
        public Category(String id, String title, String description, String icon) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.subCategories = new ArrayList<>();
        }
        
        public void addSubCategory(SubCategory subCategory) {
            this.subCategories.add(subCategory);
        }
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public List<SubCategory> getSubCategories() { return subCategories; }
    }
    
    public static class SubCategory {
        private String id;
        private String title;
        private String description;
        private String icon;
        
        public SubCategory(String id, String title, String description, String icon) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.icon = icon;
        }
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
    
    private static CategoryManager instance;
    private List<Category> categories;
    
    private CategoryManager() {
        initializeCategories();
    }
    
    public static CategoryManager getInstance() {
        if (instance == null) {
            instance = new CategoryManager();
        }
        return instance;
    }
    
    private void initializeCategories() {
        categories = new ArrayList<>();
        
        // Categoria: Desenvolvimento Mobile
        Category mobileDev = new Category("mobile_dev", "Desenvolvimento Mobile", 
            "Criação de aplicativos para dispositivos móveis", "📱");
        
        mobileDev.addSubCategory(new SubCategory("android_dev", "Desenvolvimento Android", 
            "Apps nativos para Android usando Java/Kotlin", "🤖"));
        mobileDev.addSubCategory(new SubCategory("ios_dev", "Desenvolvimento iOS", 
            "Apps nativos para iOS usando Swift/Objective-C", "🍎"));
        mobileDev.addSubCategory(new SubCategory("cross_platform", "Desenvolvimento Cross-Platform", 
            "Apps que funcionam em Android e iOS", "🔄"));
        mobileDev.addSubCategory(new SubCategory("flutter_dev", "Flutter", 
            "Framework Google para apps multiplataforma", "🦋"));
        mobileDev.addSubCategory(new SubCategory("react_native", "React Native", 
            "Framework Facebook para apps multiplataforma", "⚛️"));
        
        categories.add(mobileDev);
        
        // Categoria: Desenvolvimento Web
        Category webDev = new Category("web_dev", "Desenvolvimento Web", 
            "Criação de sites e aplicações web", "🌐");
        
        webDev.addSubCategory(new SubCategory("frontend_dev", "Desenvolvimento Frontend", 
            "Interface do usuário com HTML, CSS, JavaScript", "🎨"));
        webDev.addSubCategory(new SubCategory("backend_dev", "Desenvolvimento Backend", 
            "Lógica do servidor e APIs", "⚙️"));
        webDev.addSubCategory(new SubCategory("fullstack_dev", "Desenvolvimento Full Stack", 
            "Frontend e Backend completos", "🚀"));
        webDev.addSubCategory(new SubCategory("react_dev", "React.js", 
            "Biblioteca JavaScript para interfaces", "⚛️"));
        webDev.addSubCategory(new SubCategory("vue_dev", "Vue.js", 
            "Framework JavaScript progressivo", "💚"));
        webDev.addSubCategory(new SubCategory("angular_dev", "Angular", 
            "Framework JavaScript completo", "🅰️"));
        
        categories.add(webDev);
        
        // Categoria: Design e UI/UX
        Category design = new Category("design", "Design e UI/UX", 
            "Criação de interfaces e experiências do usuário", "🎨");
        
        design.addSubCategory(new SubCategory("ui_design", "UI Design", 
            "Design de interfaces do usuário", "🎭"));
        design.addSubCategory(new SubCategory("ux_design", "UX Design", 
            "Experiência do usuário e usabilidade", "🧠"));
        design.addSubCategory(new SubCategory("graphic_design", "Design Gráfico", 
            "Criação de elementos visuais", "🖼️"));
        design.addSubCategory(new SubCategory("icon_design", "Design de Ícones", 
            "Criação de ícones e símbolos", "🔷"));
        design.addSubCategory(new SubCategory("logo_design", "Design de Logos", 
            "Criação de identidades visuais", "🏷️"));
        design.addSubCategory(new SubCategory("prototyping", "Prototipagem", 
            "Criação de protótipos interativos", "📱"));
        
        categories.add(design);
        
        // Categoria: Desenvolvimento de Jogos
        Category gameDev = new Category("game_dev", "Desenvolvimento de Jogos", 
            "Criação de jogos para diferentes plataformas", "🎮");
        
        gameDev.addSubCategory(new SubCategory("mobile_games", "Jogos Mobile", 
            "Jogos para smartphones e tablets", "📱"));
        gameDev.addSubCategory(new SubCategory("pc_games", "Jogos PC", 
            "Jogos para computadores", "💻"));
        gameDev.addSubCategory(new SubCategory("unity_dev", "Unity", 
            "Engine para desenvolvimento de jogos", "🎯"));
        gameDev.addSubCategory(new SubCategory("unreal_dev", "Unreal Engine", 
            "Engine avançado para jogos 3D", "🌟"));
        gameDev.addSubCategory(new SubCategory("game_art", "Arte para Jogos", 
            "Criação de assets visuais", "🎨"));
        
        categories.add(gameDev);
        
        // Categoria: Inteligência Artificial
        Category ai = new Category("ai", "Inteligência Artificial", 
            "Desenvolvimento de sistemas inteligentes", "🤖");
        
        ai.addSubCategory(new SubCategory("machine_learning", "Machine Learning", 
            "Algoritmos de aprendizado automático", "🧠"));
        ai.addSubCategory(new SubCategory("deep_learning", "Deep Learning", 
            "Redes neurais profundas", "🔬"));
        ai.addSubCategory(new SubCategory("nlp", "Processamento de Linguagem Natural", 
            "Análise e geração de texto", "💬"));
        ai.addSubCategory(new SubCategory("computer_vision", "Visão Computacional", 
            "Análise de imagens e vídeos", "👁️"));
        ai.addSubCategory(new SubCategory("ai_apps", "Apps com IA", 
            "Aplicações que utilizam inteligência artificial", "📱"));
        
        categories.add(ai);
        
        // Categoria: DevOps e Infraestrutura
        Category devops = new Category("devops", "DevOps e Infraestrutura", 
            "Automação e gerenciamento de infraestrutura", "⚙️");
        
        devops.addSubCategory(new SubCategory("cloud_computing", "Computação em Nuvem", 
            "AWS, Azure, Google Cloud", "☁️"));
        devops.addSubCategory(new SubCategory("containerization", "Containerização", 
            "Docker, Kubernetes", "📦"));
        devops.addSubCategory(new SubCategory("ci_cd", "CI/CD", 
            "Integração e entrega contínua", "🔄"));
        devops.addSubCategory(new SubCategory("monitoring", "Monitoramento", 
            "Observabilidade e alertas", "📊"));
        devops.addSubCategory(new SubCategory("security", "Segurança", 
            "Segurança de aplicações e infraestrutura", "🔒"));
        
        categories.add(devops);
        
        // Categoria: Blockchain e Web3
        Category blockchain = new Category("blockchain", "Blockchain e Web3", 
            "Tecnologias descentralizadas", "⛓️");
        
        blockchain.addSubCategory(new SubCategory("smart_contracts", "Smart Contracts", 
            "Contratos inteligentes na blockchain", "📜"));
        blockchain.addSubCategory(new SubCategory("defi", "DeFi", 
            "Finanças descentralizadas", "💰"));
        blockchain.addSubCategory(new SubCategory("nft", "NFTs", 
            "Tokens não fungíveis", "🖼️"));
        blockchain.addSubCategory(new SubCategory("dapps", "DApps", 
            "Aplicações descentralizadas", "🌐"));
        blockchain.addSubCategory(new SubCategory("cryptocurrency", "Criptomoedas", 
            "Desenvolvimento de criptomoedas", "🪙"));
        
        categories.add(blockchain);
    }
    
    public List<Category> getAllCategories() {
        return categories;
    }
    
    public Category getCategoryById(String categoryId) {
        for (Category category : categories) {
            if (category.getId().equals(categoryId)) {
                return category;
            }
        }
        return null;
    }
    
    public SubCategory getSubCategoryById(String subCategoryId) {
        for (Category category : categories) {
            for (SubCategory subCategory : category.getSubCategories()) {
                if (subCategory.getId().equals(subCategoryId)) {
                    return subCategory;
                }
            }
        }
        return null;
    }
    
    public List<String> getAllSubCategoryIds() {
        List<String> ids = new ArrayList<>();
        for (Category category : categories) {
            for (SubCategory subCategory : category.getSubCategories()) {
                ids.add(subCategory.getId());
            }
        }
        return ids;
    }
    
    public List<String> getSubCategoryIdsByCategory(String categoryId) {
        Category category = getCategoryById(categoryId);
        if (category != null) {
            List<String> ids = new ArrayList<>();
            for (SubCategory subCategory : category.getSubCategories()) {
                ids.add(subCategory.getId());
            }
            return ids;
        }
        return new ArrayList<>();
    }
}
