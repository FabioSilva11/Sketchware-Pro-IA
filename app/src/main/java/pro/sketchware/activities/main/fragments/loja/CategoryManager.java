package pro.sketchware.activities.main.fragments.loja;

import java.util.HashMap;
import java.util.Map;

public class CategoryManager {
    
    private static final Map<String, String> categoryMap = new HashMap<>();
    
    static {
        // Mapeamento de categorias comuns
        categoryMap.put("games", "Jogos");
        categoryMap.put("entertainment", "Entretenimento");
        categoryMap.put("productivity", "Produtividade");
        categoryMap.put("education", "Educação");
        categoryMap.put("social", "Social");
        categoryMap.put("tools", "Ferramentas");
        categoryMap.put("lifestyle", "Estilo de Vida");
        categoryMap.put("music", "Música");
        categoryMap.put("video", "Vídeo");
        categoryMap.put("photography", "Fotografia");
        categoryMap.put("news", "Notícias");
        categoryMap.put("weather", "Clima");
        categoryMap.put("finance", "Finanças");
        categoryMap.put("health", "Saúde");
        categoryMap.put("sports", "Esportes");
        categoryMap.put("travel", "Viagem");
        categoryMap.put("shopping", "Compras");
        categoryMap.put("food", "Comida");
        categoryMap.put("utilities", "Utilitários");
        categoryMap.put("communication", "Comunicação");
        categoryMap.put("business", "Negócios");
        categoryMap.put("medical", "Médico");
        categoryMap.put("books", "Livros");
        categoryMap.put("personalization", "Personalização");
        categoryMap.put("libraries", "Bibliotecas");
        categoryMap.put("demo", "Demonstração");
        categoryMap.put("art", "Arte");
        categoryMap.put("auto", "Automóveis");
        categoryMap.put("beauty", "Beleza");
        categoryMap.put("comics", "Quadrinhos");
        categoryMap.put("dating", "Encontros");
        categoryMap.put("events", "Eventos");
        categoryMap.put("family", "Família");
        categoryMap.put("house", "Casa");
        categoryMap.put("maps", "Mapas");
        categoryMap.put("parenting", "Paternidade");
        categoryMap.put("reference", "Referência");
        categoryMap.put("transportation", "Transporte");
    }
    
    public static String getCategoryDisplay(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "Categoria não definida";
        }
        
        String trimmedCategory = category.trim().toLowerCase();
        
        // Verificar se existe no mapeamento
        if (categoryMap.containsKey(trimmedCategory)) {
            return categoryMap.get(trimmedCategory);
        }
        
        // Se não existe no mapeamento, capitalizar a primeira letra
        if (trimmedCategory.length() > 0) {
            return trimmedCategory.substring(0, 1).toUpperCase() + trimmedCategory.substring(1);
        }
        
        return "Categoria não definida";
    }
    
    public static String getCategoryFromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        
        String desc = description.toLowerCase();
        
        // Verificar palavras-chave na descrição para inferir categoria
        if (desc.contains("jogo") || desc.contains("game") || desc.contains("play")) {
            return "games";
        } else if (desc.contains("produtividade") || desc.contains("productivity") || desc.contains("trabalho")) {
            return "productivity";
        } else if (desc.contains("educação") || desc.contains("education") || desc.contains("aprendizado")) {
            return "education";
        } else if (desc.contains("social") || desc.contains("comunicação") || desc.contains("chat")) {
            return "social";
        } else if (desc.contains("ferramenta") || desc.contains("tool") || desc.contains("utility")) {
            return "tools";
        } else if (desc.contains("música") || desc.contains("music") || desc.contains("audio")) {
            return "music";
        } else if (desc.contains("vídeo") || desc.contains("video") || desc.contains("filme")) {
            return "video";
        } else if (desc.contains("foto") || desc.contains("photo") || desc.contains("camera")) {
            return "photography";
        } else if (desc.contains("notícia") || desc.contains("news") || desc.contains("informação")) {
            return "news";
        } else if (desc.contains("clima") || desc.contains("weather") || desc.contains("tempo")) {
            return "weather";
        } else if (desc.contains("finança") || desc.contains("finance") || desc.contains("dinheiro")) {
            return "finance";
        } else if (desc.contains("saúde") || desc.contains("health") || desc.contains("fitness")) {
            return "health";
        } else if (desc.contains("esporte") || desc.contains("sport") || desc.contains("exercício")) {
            return "sports";
        } else if (desc.contains("viagem") || desc.contains("travel") || desc.contains("turismo")) {
            return "travel";
        } else if (desc.contains("compra") || desc.contains("shopping") || desc.contains("loja")) {
            return "shopping";
        } else if (desc.contains("comida") || desc.contains("food") || desc.contains("restaurante")) {
            return "food";
        } else if (desc.contains("entretenimento") || desc.contains("entertainment") || desc.contains("diversão")) {
            return "entertainment";
        }
        
        return null;
    }
}
