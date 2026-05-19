import java.util.Locale;

/**
 * Arayüzde gösterilecek nihai öneri satırıdır.
 */
public class Recommendation {
    private final Movie movie;
    private final int rating;
    private final int sourceUserId;
    private final double sourceSimilarity;

    public Recommendation(Movie movie, int rating, int sourceUserId, double sourceSimilarity) {
        this.movie = movie;
        this.rating = rating;
        this.sourceUserId = sourceUserId;
        this.sourceSimilarity = sourceSimilarity;
    }

    public Movie getMovie() {
        return movie;
    }

    public int getRating() {
        return rating;
    }

    public int getSourceUserId() {
        return sourceUserId;
    }

    public double getSourceSimilarity() {
        return sourceSimilarity;
    }

    /**
     * Film ID göstermeden, sadece film adı ve önerinin kaynağını listeler.
     */
    public String toDisplayText() {
        return String.format(
                Locale.US,
                "%s | Puan: %d | Kaynak kullanıcı: %d | Benzerlik: %.4f",
                movie.getTitle(),
                rating,
                sourceUserId,
                sourceSimilarity
        );
    }

    @Override
    public String toString() {
        return toDisplayText();
    }
}
