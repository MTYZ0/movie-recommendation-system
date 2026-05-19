/**
 * Film bilgisini temsil eden basit model sınıfıdır.
 *
 * Bu sınıf sadece veriyi taşır; öneri algoritması veya arayüz mantığı içermez.
 * Böylece Tek Sorumluluk Prensibi'ne uygun kalır.
 */
public class Movie {
    private final int id;
    private final String title;
    private final String genres;

    public Movie(int id, String title, String genres) {
        this.id = id;
        this.title = title;
        this.genres = genres;
    }

    public int getId() {
        return id;
    }

    /**
     * Bazı örnek kodlarda film ID getter'ı getMovieId olarak adlandırılır.
     * Ana kod getId kullanır; bu metot isim uyumluluğu için tutulur.
     */
    public int getMovieId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenres() {
        return genres;
    }

    /**
     * Swing ComboBox film nesnesini doğrudan yazdırırken bu metni gösterir.
     */
    @Override
    public String toString() {
        return title;
    }
}
