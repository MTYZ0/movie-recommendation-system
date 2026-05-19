import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Projedeki CSV dosyalarını okumaktan sorumludur.
 *
 * main_data.csv ve target_user.csv geniş kullanıcı-film matrisi formatındadır:
 * ilk sütun user_id, sonraki sütun başlıkları film ID, hücre değerleri puandır.
 *
 * movies.csv ise movieId,title,genres formatındadır. Film adlarında virgül
 * bulunabildiği için String.split(",") kullanılmaz; bunun yerine küçük bir
 * CSV parser yazılmıştır.
 */
public class CsvReader {

    public List<UserRatings> readMainData(String fileName) throws IOException {
        return readUserMatrix(fileName);
    }

    public List<UserRatings> readTargetUsers(String fileName) throws IOException {
        return readUserMatrix(fileName);
    }

    public Map<Integer, Movie> readMovies(String fileName) throws IOException {
        Map<Integer, Movie> movies = new LinkedHashMap<Integer, Movie>();

        BufferedReader reader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8);

        try {
            String header = reader.readLine();

            if (header == null) {
                return movies;
            }

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> cells = parseCsvLine(line);

                if (cells.size() < 2) {
                    continue;
                }

                int movieId = parseInteger(readCell(cells, 0), -1);
                String title = readCell(cells, 1);
                String genres = readCell(cells, 2);

                if (movieId > 0 && !title.isEmpty()) {
                    movies.put(movieId, new Movie(movieId, title, genres));
                }
            }
        } finally {
            reader.close();
        }

        return movies;
    }

    private List<UserRatings> readUserMatrix(String fileName) throws IOException {
        List<UserRatings> users = new ArrayList<UserRatings>();
        BufferedReader reader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8);

        try {
            String headerLine = reader.readLine();

            if (headerLine == null) {
                return users;
            }

            List<String> headers = parseCsvLine(headerLine);
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> cells = parseCsvLine(line);

                if (cells.isEmpty()) {
                    continue;
                }

                int userId = parseInteger(readCell(cells, 0), -1);

                if (userId < 0) {
                    continue;
                }

                UserRatings userRatings = new UserRatings(userId);
                int columnLimit = Math.min(headers.size(), cells.size());

                for (int column = 1; column < columnLimit; column++) {
                    int rating = parseInteger(readCell(cells, column), 0);

                    if (rating > 0) {
                        int movieId = parseInteger(readCell(headers, column), -1);

                        if (movieId > 0) {
                            userRatings.addRating(movieId, rating);
                        }
                    }
                }

                users.add(userRatings);
            }
        } finally {
            reader.close();
        }

        return users;
    }

    /**
     * Tırnak içindeki virgülleri ayırıcı kabul etmeyen basit CSV ayrıştırıcıdır.
     */
    private List<String> parseCsvLine(String line) {
        List<String> cells = new ArrayList<String>();
        StringBuilder currentCell = new StringBuilder();
        boolean insideQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char currentChar = line.charAt(index);

            if (currentChar == '"') {
                boolean escapedQuote = insideQuotes
                        && index + 1 < line.length()
                        && line.charAt(index + 1) == '"';

                if (escapedQuote) {
                    currentCell.append('"');
                    index++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (currentChar == ',' && !insideQuotes) {
                cells.add(cleanCell(currentCell.toString()));
                currentCell.setLength(0);
            } else {
                currentCell.append(currentChar);
            }
        }

        cells.add(cleanCell(currentCell.toString()));
        return cells;
    }

    private String cleanCell(String value) {
        return value.replace("\uFEFF", "").trim();
    }

    /**
     * CSV satırında beklenen sütun yoksa boş değer döner.
     * Böylece hatalı/eksik satırlar uygulamayı IndexOutOfBoundsException ile
     * düşürmez; parseInteger veya üst seviye validasyon bu değeri ele alır.
     */
    private String readCell(List<String> cells, int index) {
        if (index < 0 || index >= cells.size()) {
            return "";
        }

        return cells.get(index);
    }

    private int parseInteger(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }
}
