/**
 * Hedef kullanıcıya göre hesaplanan benzer kullanıcı bilgisidir.
 *
 * Comparable implementasyonu Max-Heap tarafından kullanılır. Benzerlik skoru
 * büyük olan kullanıcı heap köküne daha yakın konumlanır.
 */
public class SimilarUser implements Comparable<SimilarUser> {
    private static final double SIMILARITY_SCALE = 100000.0;

    private final UserRatings userRatings;
    private final double similarity;

    public SimilarUser(UserRatings userRatings, double similarity) {
        this.userRatings = userRatings;
        this.similarity = roundSimilarity(similarity);
    }

    public UserRatings getUserRatings() {
        return userRatings;
    }

    public double getSimilarity() {
        return similarity;
    }

    /**
     * Benzerlik değerleri heap'e girmeden önce tek standartta tutulur.
     * Böylece hem sıralama hem de arayüzde gösterilen kaynak benzerliği aynı
     * hassasiyetten beslenir.
     */
    private double roundSimilarity(double value) {
        return Math.round(value * SIMILARITY_SCALE) / SIMILARITY_SCALE;
    }

    @Override
    public int compareTo(SimilarUser other) {
        int similarityComparison = Double.compare(this.similarity, other.similarity);

        if (similarityComparison != 0) {
            return similarityComparison;
        }

        // Eşit benzerlik durumunda deterministik sonuç üretmek için userId kullanılır.
        return Integer.compare(this.userRatings.getUserId(), other.userRatings.getUserId());
    }
}
