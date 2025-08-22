package pro.sketchware.activities.main.activities;

import java.util.Map;

public class RatingManager {
    
    public static class RatingStats {
        public double averageRating;
        public int totalReviews;
        public int[] starDistribution; // [0] = 1-star, [1] = 2-star, etc.
        
        public RatingStats() {
            starDistribution = new int[5];
        }
    }
    
    public static RatingStats calculateRatingStats(Map<String, DetalhesActivity.Comentario> comentarios) {
        RatingStats stats = new RatingStats();
        
        if (comentarios == null || comentarios.isEmpty()) {
            return stats;
        }
        
        double totalRating = 0;
        int totalReviews = 0;
        
        // Contar avaliações por estrela
        for (DetalhesActivity.Comentario comentario : comentarios.values()) {
            if (comentario.rating > 0) {
                totalRating += comentario.rating;
                totalReviews++;
                
                // Distribuir por estrelas (1-5)
                int starIndex = (int) comentario.rating - 1;
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
