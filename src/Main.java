import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Uygulamayı başlatan ana sınıftır.
 */
public class Main {
    private static final String MAIN_DATA_FILE = "main_data.csv";
    private static final String MOVIES_FILE = "movies.csv";
    private static final String TARGET_USER_FILE = "target_user.csv";

    public static void main(String[] args) {
        try {
            String mainDataFile = args.length >= 3
                    ? args[0]
                    : resolveDataFile(MAIN_DATA_FILE, args);
            String moviesFile = args.length >= 3
                    ? args[1]
                    : resolveDataFile(MOVIES_FILE, args);
            String targetUserFile = args.length >= 3
                    ? args[2]
                    : resolveDataFile(TARGET_USER_FILE, args);

            CsvReader csvReader = new CsvReader();
            List<UserRatings> mainUsers = csvReader.readMainData(mainDataFile);
            Map<Integer, Movie> moviesById = csvReader.readMovies(moviesFile);
            List<UserRatings> targetUsers = csvReader.readTargetUsers(targetUserFile);

            final RecommendationEngine recommendationEngine = new RecommendationEngine(
                    mainUsers,
                    targetUsers,
                    moviesById
            );

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MovieRecommendationGUI.create(recommendationEngine).setVisible(true);
                }
            });
        } catch (IOException exception) {
            showStartupError(exception);
        } catch (RuntimeException exception) {
            showStartupError(exception);
        }
    }

    /**
     * CSV dosyalarını sadece çalışma dizinine bağımlı bırakmamak için birkaç
     * olası konum denenir. Tek argüman verilirse bu argüman veri klasörü kabul
     * edilir; üç argüman verilirse dosya yolları sırasıyla doğrudan kullanılır.
     */
    private static String resolveDataFile(String fileName, String[] args) throws IOException {
        List<Path> candidatePaths = new ArrayList<Path>();

        if (args.length == 1) {
            candidatePaths.add(Paths.get(args[0]).resolve(fileName));
        }

        candidatePaths.add(Paths.get(fileName));
        candidatePaths.add(Paths.get("data").resolve(fileName));

        Path applicationDirectory = findApplicationDirectory();

        if (applicationDirectory != null) {
            candidatePaths.add(applicationDirectory.resolve(fileName));
            candidatePaths.add(applicationDirectory.resolve("data").resolve(fileName));
        }

        for (Path candidatePath : candidatePaths) {
            if (Files.isRegularFile(candidatePath)) {
                return candidatePath.toString();
            }
        }

        throw new IOException("Dosya bulunamadı: " + fileName);
    }

    private static Path findApplicationDirectory() {
        try {
            Path applicationPath = Paths.get(
                    Main.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            if (Files.isRegularFile(applicationPath)) {
                return applicationPath.getParent();
            }

            return applicationPath;
        } catch (URISyntaxException exception) {
            return null;
        }
    }

    private static void showStartupError(Exception exception) {
        JOptionPane.showMessageDialog(
                null,
                "Uygulama başlatılamadı: " + exception.getMessage(),
                "Başlatma Hatası",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
