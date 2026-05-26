import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Bir kullanıcının film-puan vektörünü temsil eder.
 *
 * CSV dosyaları çok geniş ve çoğunlukla 0 değerli olduğu için tüm matrisi
 * belleğe iki boyutlu dizi olarak almak yerine sadece puanlanmış filmleri
 * HashMap içinde tutuyoruz. Bu yaklaşım seyrek matrislerde daha verimlidir.
 */
public class UserRatings {
    private final int userId;
    private final String userName;
    private final Map<Integer, Integer> ratings;
    private double squaredNorm;

    private static final String[] NAMES_POOL = {
        "Ahmet", "Zeynep", "Mehmet", "Elif", "Can", "Merve", "Burak", "Selin", "Emre", "Aslı",
        "Kaan", "Defne", "Onur", "Ece", "Hakan", "Gamze", "Volkan", "Deniz", "Cem", "Büşra",
        "Seda", "Yiğit", "Ceren", "Oğuz", "Dilan", "Tolga", "Gizem", "Fatih", "İrem", "Serkan"
    };

    public static String resolveName(int userId) {
        switch (userId) {
            case 601: return "Ahmet";
            case 602: return "Zeynep";
            case 603: return "Mehmet";
            case 604: return "Elif";
            case 605: return "Can";
            case 606: return "Merve";
            case 607: return "Burak";
            case 608: return "Selin";
            case 609: return "Emre";
            case 610: return "Aslı";
            default:
                int index = Math.abs(userId) % NAMES_POOL.length;
                return NAMES_POOL[index] + " (" + userId + ")";
        }
    }

    public UserRatings(int userId) {
        this.userId = userId;
        this.userName = resolveName(userId);
        this.ratings = new HashMap<Integer, Integer>();
        this.squaredNorm = 0.0;
    }

    public UserRatings(int userId, Map<Integer, Integer> ratings) {
        this.userId = userId;
        this.userName = resolveName(userId);
        this.ratings = new HashMap<Integer, Integer>();
        this.squaredNorm = 0.0;

        for (Map.Entry<Integer, Integer> entry : ratings.entrySet()) {
            addRatingInternal(entry.getKey(), entry.getValue());
        }
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    /**
     * 0 değeri "puan verilmemiş" anlamına geldiği için saklanmaz.
     */
    public void addRating(int movieId, int rating) {
        addRatingInternal(movieId, rating);
    }

    private void addRatingInternal(int movieId, int rating) {
        if (rating > 0) {
            Integer oldRating = ratings.get(movieId);

            if (oldRating != null) {
                squaredNorm -= oldRating * oldRating;
            }

            ratings.put(movieId, rating);
            squaredNorm += rating * rating;
        }
    }

    public int getRatingForMovie(int movieId) {
        Integer rating = ratings.get(movieId);
        return rating == null ? 0 : rating;
    }

    public boolean hasRated(int movieId) {
        return ratings.containsKey(movieId);
    }

    public boolean hasAnyRating() {
        return !ratings.isEmpty();
    }

    /**
     * Kosinüs benzerliğinde kullanılan vektör uzunluğudur.
     * Puanlar değiştikçe kareler toplamı güncellendiği için burada tekrar
     * tüm vektörü dolaşmaya gerek kalmaz.
     */
    public double getVectorNorm() {
        return Math.sqrt(squaredNorm);
    }

    public Map<Integer, Integer> getRatings() {
        return Collections.unmodifiableMap(ratings);
    }

    @Override
    public String toString() {
        return userName;
    }
}
