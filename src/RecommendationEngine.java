import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Öneri algoritmasının merkez sınıfıdır.
 *
 * Bu sınıf kosinüs benzerliğini hesaplar, benzer kullanıcıları özel heap'e
 * ekler ve heap kökünden en iyi kullanıcıları çekerek film önerilerini üretir.
 */
public class RecommendationEngine {
    private static final double MINIMUM_SIMILARITY = 0.0001;

    private final List<UserRatings> mainUsers;
    private final List<UserRatings> targetUsers;
    private final Map<Integer, Movie> moviesById;
    private final List<Movie> allMovies;
    private final Random random;

    public RecommendationEngine(
            List<UserRatings> mainUsers,
            List<UserRatings> targetUsers,
            Map<Integer, Movie> moviesById
    ) {
        this.mainUsers = new ArrayList<UserRatings>(mainUsers);
        this.targetUsers = new ArrayList<UserRatings>(targetUsers);
        this.moviesById = new HashMap<Integer, Movie>(moviesById);
        
        // Ekran B'de "Öneri bulunamadı" hatasını önlemek ve öneri kalitesini/çeşitliliğini artırmak için:
        // Yalnızca main_data.csv içinde aktif olarak puanlanmış (en az 3 kez puanlanmış) filmleri seçiyoruz.
        // Böylece kullanıcının puanladığı filmler ile veri setindeki diğer kullanıcılar arasında mutlaka ortak puan bulunur.
        Map<Integer, Integer> movieRatingCounts = new HashMap<Integer, Integer>();
        for (UserRatings user : mainUsers) {
            for (Integer movieId : user.getRatings().keySet()) {
                Integer count = movieRatingCounts.get(movieId);
                if (count == null) {
                    movieRatingCounts.put(movieId, 1);
                } else {
                    movieRatingCounts.put(movieId, count + 1);
                }
            }
        }
        
        List<Movie> activeMovies = new ArrayList<Movie>();
        for (Movie movie : moviesById.values()) {
            Integer count = movieRatingCounts.get(movie.getMovieId());
            if (count != null && count >= 3) { // En az 3 kullanıcı tarafından puanlanmış popüler/aktif filmler
                activeMovies.add(movie);
            }
        }
        
        // Eğer aktif film sayısı çok az ise eşiği düşürerek en az 1 puanlıları al
        if (activeMovies.size() < 100) {
            activeMovies.clear();
            for (Movie movie : moviesById.values()) {
                Integer count = movieRatingCounts.get(movie.getMovieId());
                if (count != null && count >= 1) {
                    activeMovies.add(movie);
                }
            }
        }
        
        // Eğer veri seti boş ise güvenli geri dönüş (fallback) yap
        if (activeMovies.isEmpty()) {
            activeMovies.addAll(moviesById.values());
        }
        
        this.allMovies = activeMovies;
        this.random = new Random();
    }

    public List<UserRatings> getTargetUsers() {
        return Collections.unmodifiableList(targetUsers);
    }

    public List<Movie> getRandomMovies(int count) {
        List<Movie> selectedMovies = new ArrayList<Movie>();

        if (count <= 0 || allMovies.isEmpty()) {
            return selectedMovies;
        }

        if (count >= allMovies.size()) {
            return new ArrayList<Movie>(allMovies);
        }

        Set<Integer> selectedIndexes = new HashSet<Integer>();

        while (selectedMovies.size() < count) {
            int randomIndex = random.nextInt(allMovies.size());

            if (selectedIndexes.add(randomIndex)) {
                selectedMovies.add(allMovies.get(randomIndex));
            }
        }

        return selectedMovies;
    }

    public List<Recommendation> recommendForTargetUser(
            UserRatings targetUser,
            int similarUserCount,
            int moviesPerSimilarUser
    ) {
        return recommend(targetUser, similarUserCount, moviesPerSimilarUser);
    }

    public List<Recommendation> recommendForManualRatings(
            Map<Integer, Integer> manualRatings,
            int similarUserCount,
            int moviesPerSimilarUser
    ) {
        UserRatings syntheticTargetUser = new UserRatings(0, manualRatings);
        return recommend(syntheticTargetUser, similarUserCount, moviesPerSimilarUser);
    }

    /**
     * Kosinüs Benzerliği:
     *
     * similarity(A,B) = dot(A,B) / (||A|| * ||B||)
     *
     * HashMap tabanlı seyrek vektör kullandığımız için dot product hesabında
     * sadece puanlanmış filmler dolaşılır.
     */
    public double calculateCosineSimilarity(UserRatings firstUser, UserRatings secondUser) {
        double dotProduct = 0.0;
        double firstNorm = firstUser.getVectorNorm();
        double secondNorm = secondUser.getVectorNorm();

        Map<Integer, Integer> smallerVector = firstUser.getRatings();
        UserRatings otherUser = secondUser;

        if (secondUser.getRatings().size() < firstUser.getRatings().size()) {
            smallerVector = secondUser.getRatings();
            otherUser = firstUser;
        }

        for (Map.Entry<Integer, Integer> entry : smallerVector.entrySet()) {
            int otherRating = otherUser.getRatingForMovie(entry.getKey());
            dotProduct += entry.getValue() * otherRating;
        }

        if (firstNorm == 0.0 || secondNorm == 0.0) {
            return 0.0;
        }

        return dotProduct / (firstNorm * secondNorm);
    }

    private List<Recommendation> recommend(
            UserRatings targetUser,
            int similarUserCount,
            int moviesPerSimilarUser
    ) {
        validateRecommendationParameters(targetUser, similarUserCount, moviesPerSimilarUser);

        NodeMaxHeap<SimilarUser> similarityHeap = buildSimilarityHeap(targetUser);
        List<Recommendation> recommendations = new ArrayList<Recommendation>();

        for (int userIndex = 0; userIndex < similarUserCount && !similarityHeap.isEmpty(); userIndex++) {
            SimilarUser similarUser = similarityHeap.extractMax();

            appendTopMoviesFromSimilarUser(
                    recommendations,
                    similarUser,
                    moviesPerSimilarUser
            );
        }

        return recommendations;
    }

    private NodeMaxHeap<SimilarUser> buildSimilarityHeap(UserRatings targetUser) {
        NodeMaxHeap<SimilarUser> similarityHeap = new NodeMaxHeap<SimilarUser>();

        for (UserRatings candidateUser : mainUsers) {
            if (candidateUser.getUserId() == targetUser.getUserId()) {
                continue;
            }

            double similarity = calculateCosineSimilarity(targetUser, candidateUser);

            // Ortak puanı olmayan kullanıcıların kosinüs benzerliği 0 olur.
            // Bu kullanıcıları heap'e eklemek öneri kalitesini düşürür.
            if (similarity > MINIMUM_SIMILARITY) {
                final int candidateUserId = candidateUser.getUserId();
                SimilarUser existingUser = similarityHeap.search(
                        new NodeMaxHeap.SearchCondition<SimilarUser>() {
                            @Override
                            public boolean matches(SimilarUser value) {
                                return value.getUserRatings().getUserId() == candidateUserId;
                            }
                        }
                );

                // Aynı user_id veri dosyasında tekrar ederse heap'e ikinci kez alınmaz.
                if (existingUser == null) {
                    similarityHeap.insert(new SimilarUser(candidateUser, similarity));
                }
            }
        }

        return similarityHeap;
    }

    private void appendTopMoviesFromSimilarUser(
            List<Recommendation> recommendations,
            SimilarUser similarUser,
            int moviesPerSimilarUser
    ) {
        NodeMaxHeap<MovieScore> movieHeap = new NodeMaxHeap<MovieScore>();

        for (Map.Entry<Integer, Integer> entry : similarUser.getUserRatings().getRatings().entrySet()) {
            int movieId = entry.getKey();

            Movie movie = moviesById.get(movieId);

            // movies.csv içinde adı olmayan kayıtlar arayüzde ID ile gösterilmez.
            if (movie == null) {
                continue;
            }

            movieHeap.insert(new MovieScore(
                    movie,
                    entry.getValue(),
                    similarUser.getUserRatings().getUserId(),
                    similarUser.getSimilarity()
            ));
        }

        for (int movieIndex = 0; movieIndex < moviesPerSimilarUser && !movieHeap.isEmpty(); movieIndex++) {
            MovieScore movieScore = movieHeap.extractMax();
            recommendations.add(new Recommendation(
                    movieScore.getMovie(),
                    movieScore.getRating(),
                    movieScore.getSourceUserId(),
                    movieScore.getSourceSimilarity()
            ));
        }
    }

    private void validateRecommendationParameters(
            UserRatings targetUser,
            int similarUserCount,
            int moviesPerSimilarUser
    ) {
        if (targetUser == null) {
            throw new IllegalArgumentException("Hedef kullanıcı seçilmelidir.");
        }

        if (!targetUser.hasAnyRating()) {
            throw new IllegalArgumentException("Hedef kullanıcı veya hedef vektör en az bir puan içermelidir.");
        }

        if (similarUserCount <= 0) {
            throw new IllegalArgumentException("X değeri pozitif tam sayı olmalıdır.");
        }

        if (moviesPerSimilarUser <= 0) {
            throw new IllegalArgumentException("K değeri pozitif tam sayı olmalıdır.");
        }
    }
}
