package pro.sketchware.activities.main.activities;

import java.util.List;
import java.util.Map;
import pro.sketchware.activities.main.fragments.loja.AppItem;
import pro.sketchware.activities.main.fragments.loja.Comentario;

public class RatingManager {
    
    public static class RatingStats {
        public double averageRating;
        public int totalReviews;
        public int[] starDistribution; // [0] = 1-star, [1] = 2-star, etc.
        
        public RatingStats() {
            starDistribution = new int[5];
        }
    }
    
    // Método para o formato Map (compatibilidade)
    public static RatingStats calculateRatingStats(Map<String, Comentario> comentarios) {
        RatingStats stats = new RatingStats();
        
        if (comentarios == null || comentarios.isEmpty()) {
            return stats;
        }
        
        double totalRating = 0;
        int totalReviews = 0;
        
        // Contar avaliações por estrela
        for (Comentario comentario : comentarios.values()) {
            if (comentario.getEstrelas() > 0) {
                totalRating += comentario.getEstrelas();
                totalReviews++;
                
                // Distribuir por estrelas (1-5)
                int starIndex = comentario.getEstrelas() - 1;
                if (starIndex >= 0 && starIndex < 5) {
                    stats.starDistribution[starIndex]++;
                }
            }
        }
        
        stats.totalReviews = totalReviews;
        if (totalReviews > 0) {
            stats.averageRating = totalRating / totalReviews;
        }
        
        return stats;
    }

    // Método para o novo formato (top-level comentarios)
    public static RatingStats calculateRatingStatsFromList(List<Comentario> comentarios) {
        RatingStats stats = new RatingStats();
        
        if (comentarios == null || comentarios.isEmpty()) {
            return stats;
        }
        
        double totalRating = 0;
        int totalReviews = 0;
        
        // Contar avaliações por estrela
        for (Comentario comentario : comentarios) {
            if (comentario.getEstrelas() > 0) {
                totalRating += comentario.getEstrelas();
                totalReviews++;
                
                // Distribuir por estrelas (1-5)
                int starIndex = comentario.getEstrelas() - 1;
                if (starIndex >= 0 && starIndex < 5) {
                    stats.starDistribution[starIndex]++;
                }
            }
        }
        
        stats.totalReviews = totalReviews;
        if (totalReviews > 0) {
            stats.averageRating = totalRating / totalReviews;
        }
        
        return stats;
    }

    // Método para calcular a partir dos dados diretos do app
    public static RatingStats calculateRatingStatsFromAppData(double avaliacaoMedia, long numeroAvaliacoes) {
        RatingStats stats = new RatingStats();
        
        stats.averageRating = avaliacaoMedia;
        stats.totalReviews = (int) Math.min(numeroAvaliacoes, Integer.MAX_VALUE);
        
        // Se não temos distribuição detalhada, estimar baseado na média
        if (avaliacaoMedia > 0 && numeroAvaliacoes > 0) {
            estimateDistribution(stats, avaliacaoMedia, stats.totalReviews);
        }
        
        return stats;
    }

    private static void estimateDistribution(RatingStats stats, double averageRating, int totalReviews) {
        // Estimativa simples da distribuição baseada na média
        // Esta é uma aproximação - idealmente teríamos os dados reais
        
        if (averageRating >= 4.5) {
            // App muito bem avaliado
            stats.starDistribution[4] = (int) (totalReviews * 0.7); // 5 estrelas
            stats.starDistribution[3] = (int) (totalReviews * 0.2); // 4 estrelas
            stats.starDistribution[2] = (int) (totalReviews * 0.05); // 3 estrelas
            stats.starDistribution[1] = (int) (totalReviews * 0.03); // 2 estrelas
            stats.starDistribution[0] = totalReviews - stats.starDistribution[4] - stats.starDistribution[3] - stats.starDistribution[2] - stats.starDistribution[1];
        } else if (averageRating >= 4.0) {
            // App bem avaliado
            stats.starDistribution[4] = (int) (totalReviews * 0.5); // 5 estrelas
            stats.starDistribution[3] = (int) (totalReviews * 0.3); // 4 estrelas
            stats.starDistribution[2] = (int) (totalReviews * 0.1); // 3 estrelas
            stats.starDistribution[1] = (int) (totalReviews * 0.05); // 2 estrelas
            stats.starDistribution[0] = totalReviews - stats.starDistribution[4] - stats.starDistribution[3] - stats.starDistribution[2] - stats.starDistribution[1];
        } else {
            // Distribuição mais uniforme para apps com avaliação média
            int avgPerStar = totalReviews / 5;
            for (int i = 0; i < 5; i++) {
                stats.starDistribution[i] = avgPerStar;
            }
            // Ajustar o restante para a primeira categoria
            stats.starDistribution[0] += totalReviews % 5;
        }
    }
    
    public static String formatReviewCount(int count) {
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        } else {
            return String.valueOf(count);
        }
    }
    
    public static int calculateProgressPercentage(int starCount, int totalReviews) {
        if (totalReviews == 0) return 0;
        return (int) ((double) starCount / totalReviews * 100);
    }
}
