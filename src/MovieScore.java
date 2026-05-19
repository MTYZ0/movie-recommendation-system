/**
 * Benzer bir kullanıcının öneriye aday film puanını temsil eder.
 *
 * Bu sınıf da Comparable'dır; böylece her benzer kullanıcının en yüksek
 * puanladığı filmleri yine heap üzerinden seçebiliriz.
 */
public class MovieScore implements Comparable<MovieScore> {
    private final Movie movie;
    private final int rating;
    private final int sourceUserId;
    private final double sourceSimilarity;

    public MovieScore(Movie movie, int rating, int sourceUserId, double sourceSimilarity) {
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

    @Override
    public int compareTo(MovieScore other) {
        int ratingComparison = Integer.compare(this.rating, other.rating);

        if (ratingComparison != 0) {
            return ratingComparison;
        }

        int similarityComparison = Double.compare(this.sourceSimilarity, other.sourceSimilarity);

        if (similarityComparison != 0) {
            return similarityComparison;
        }

        // Max-Heap'te A-Z sıralama için alfabetik olarak küçük başlık daha büyük kabul edilir.
        return other.movie.getTitle().compareTo(this.movie.getTitle());
    }
}
