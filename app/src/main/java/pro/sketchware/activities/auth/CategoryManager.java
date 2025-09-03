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
        
        // Category: Mobile Development
        Category mobileDev = new Category("mobile_dev", "Mobile Development", 
            "Creating applications for mobile devices", "📱");
        
        mobileDev.addSubCategory(new SubCategory("android_dev", "Android Development", 
            "Native Android apps using Java/Kotlin", "🤖"));
        mobileDev.addSubCategory(new SubCategory("ios_dev", "iOS Development", 
            "Native iOS apps using Swift/Objective-C", "🍎"));
        mobileDev.addSubCategory(new SubCategory("cross_platform", "Cross-Platform Development", 
            "Apps that work on Android and iOS", "🔄"));
        mobileDev.addSubCategory(new SubCategory("flutter_dev", "Flutter", 
            "Google framework for multiplatform apps", "🦋"));
        mobileDev.addSubCategory(new SubCategory("react_native", "React Native", 
            "Facebook framework for multiplatform apps", "⚛️"));
        
        categories.add(mobileDev);
        
        // Category: Web Development
        Category webDev = new Category("web_dev", "Web Development", 
            "Creating websites and web applications", "🌐");
        
        webDev.addSubCategory(new SubCategory("frontend_dev", "Frontend Development", 
            "User interface with HTML, CSS, JavaScript", "🎨"));
        webDev.addSubCategory(new SubCategory("backend_dev", "Backend Development", 
            "Server logic and APIs", "⚙️"));
        webDev.addSubCategory(new SubCategory("fullstack_dev", "Full Stack Development", 
            "Complete Frontend and Backend", "🚀"));
        webDev.addSubCategory(new SubCategory("react_dev", "React.js", 
            "JavaScript library for interfaces", "⚛️"));
        webDev.addSubCategory(new SubCategory("vue_dev", "Vue.js", 
            "Progressive JavaScript framework", "💚"));
        webDev.addSubCategory(new SubCategory("angular_dev", "Angular", 
            "Complete JavaScript framework", "🅰️"));
        
        categories.add(webDev);
        
        // Category: Design and UI/UX
        Category design = new Category("design", "Design and UI/UX", 
            "Creating interfaces and user experiences", "🎨");
        
        design.addSubCategory(new SubCategory("ui_design", "UI Design", 
            "User interface design", "🎭"));
        design.addSubCategory(new SubCategory("ux_design", "UX Design", 
            "User experience and usability", "🧠"));
        design.addSubCategory(new SubCategory("graphic_design", "Graphic Design", 
            "Creating visual elements", "🖼️"));
        design.addSubCategory(new SubCategory("icon_design", "Icon Design", 
            "Creating icons and symbols", "🔷"));
        design.addSubCategory(new SubCategory("logo_design", "Logo Design", 
            "Creating visual identities", "🏷️"));
        design.addSubCategory(new SubCategory("prototyping", "Prototyping", 
            "Creating interactive prototypes", "📱"));
        
        categories.add(design);
        
        // Category: Game Development
        Category gameDev = new Category("game_dev", "Game Development", 
            "Creating games for different platforms", "🎮");
        
        gameDev.addSubCategory(new SubCategory("mobile_games", "Mobile Games", 
            "Games for smartphones and tablets", "📱"));
        gameDev.addSubCategory(new SubCategory("pc_games", "PC Games", 
            "Games for computers", "💻"));
        gameDev.addSubCategory(new SubCategory("unity_dev", "Unity", 
            "Engine for game development", "🎯"));
        gameDev.addSubCategory(new SubCategory("unreal_dev", "Unreal Engine", 
            "Advanced engine for 3D games", "🌟"));
        gameDev.addSubCategory(new SubCategory("game_art", "Game Art", 
            "Creating visual assets", "🎨"));
        
        categories.add(gameDev);
        
        // Category: Artificial Intelligence
        Category ai = new Category("ai", "Artificial Intelligence", 
            "Developing intelligent systems", "🤖");
        
        ai.addSubCategory(new SubCategory("machine_learning", "Machine Learning", 
            "Automatic learning algorithms", "🧠"));
        ai.addSubCategory(new SubCategory("deep_learning", "Deep Learning", 
            "Deep neural networks", "🔬"));
        ai.addSubCategory(new SubCategory("nlp", "Natural Language Processing", 
            "Text analysis and generation", "💬"));
        ai.addSubCategory(new SubCategory("computer_vision", "Computer Vision", 
            "Image and video analysis", "👁️"));
        ai.addSubCategory(new SubCategory("ai_apps", "AI Apps", 
            "Applications that use artificial intelligence", "📱"));
        
        categories.add(ai);
        
        // Category: DevOps and Infrastructure
        Category devops = new Category("devops", "DevOps and Infrastructure", 
            "Automation and infrastructure management", "⚙️");
        
        devops.addSubCategory(new SubCategory("cloud_computing", "Cloud Computing", 
            "AWS, Azure, Google Cloud", "☁️"));
        devops.addSubCategory(new SubCategory("containerization", "Containerization", 
            "Docker, Kubernetes", "📦"));
        devops.addSubCategory(new SubCategory("ci_cd", "CI/CD", 
            "Continuous integration and delivery", "🔄"));
        devops.addSubCategory(new SubCategory("monitoring", "Monitoring", 
            "Observability and alerts", "📊"));
        devops.addSubCategory(new SubCategory("security", "Security", 
            "Application and infrastructure security", "🔒"));
        
        categories.add(devops);
        
        // Category: Blockchain and Web3
        Category blockchain = new Category("blockchain", "Blockchain and Web3", 
            "Decentralized technologies", "⛓️");
        
        blockchain.addSubCategory(new SubCategory("smart_contracts", "Smart Contracts", 
            "Intelligent contracts on blockchain", "📜"));
        blockchain.addSubCategory(new SubCategory("defi", "DeFi", 
            "Decentralized finance", "💰"));
        blockchain.addSubCategory(new SubCategory("nft", "NFTs", 
            "Non-fungible tokens", "🖼️"));
        blockchain.addSubCategory(new SubCategory("dapps", "DApps", 
            "Decentralized applications", "🌐"));
        blockchain.addSubCategory(new SubCategory("cryptocurrency", "Cryptocurrency", 
            "Cryptocurrency development", "🪙"));
        
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
