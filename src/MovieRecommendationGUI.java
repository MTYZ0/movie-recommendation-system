import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

/**
 * Film öneri sisteminin Swing tabanlı kullanıcı arayüzüdür.
 *
 * Arayüz iki ana işlem ekranından oluşur:
 * 1. Hedef kullanıcıya göre öneri
 * 2. Kullanıcının seçtiği ve puanladığı filmlere göre öneri
 */
@SuppressWarnings("serial")
public class MovieRecommendationGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_SIMILAR_USER_COUNT = 3;
    private static final int DEFAULT_MOVIE_COUNT_PER_USER = 3;
    private static final int RANDOM_MOVIE_COUNT_PER_COMBO_BOX = 10;
    private static final int MANUAL_MOVIE_SELECTION_COUNT = 5;

    private final RecommendationEngine recommendationEngine;
    private final JComboBox<UserRatings> targetUserComboBox;
    private final JTextField targetXField;
    private final JTextField targetKField;
    private final DefaultListModel<String> targetResultModel;
    private final List<JComboBox<Movie>> manualMovieComboBoxes;
    private final List<JTextField> manualRatingFields;
    private final JTextField manualXField;
    private final JTextField manualKField;
    private final DefaultListModel<String> manualResultModel;

    public static MovieRecommendationGUI create(RecommendationEngine recommendationEngine) {
        MovieRecommendationGUI gui = new MovieRecommendationGUI(recommendationEngine);
        gui.configureFrame();
        gui.loadTargetUsers();
        gui.add(gui.createTabbedPane(), BorderLayout.CENTER);
        return gui;
    }

    private MovieRecommendationGUI(RecommendationEngine recommendationEngine) {
        this.recommendationEngine = recommendationEngine;
        this.targetUserComboBox = new JComboBox<UserRatings>();
        this.targetXField = createSmallNumberField(DEFAULT_SIMILAR_USER_COUNT);
        this.targetKField = createSmallNumberField(DEFAULT_MOVIE_COUNT_PER_USER);
        this.targetResultModel = new DefaultListModel<String>();
        this.manualMovieComboBoxes = new ArrayList<JComboBox<Movie>>();
        this.manualRatingFields = new ArrayList<JTextField>();
        this.manualXField = createSmallNumberField(DEFAULT_SIMILAR_USER_COUNT);
        this.manualKField = createSmallNumberField(DEFAULT_MOVIE_COUNT_PER_USER);
        this.manualResultModel = new DefaultListModel<String>();
    }

    private void configureFrame() {
        setTitle("Yığın Tabanlı Film Öneri Sistemi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Hedef Kullanıcı", createTargetUserPanel());
        tabbedPane.addTab("Film Puanlarına Göre", createManualMoviePanel());
        return tabbedPane;
    }

    private JPanel createTargetUserPanel() {
        JPanel panel = createRootPanel();
        panel.add(createTargetControlPanel(), BorderLayout.NORTH);
        panel.add(createResultScrollPane(targetResultModel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTargetControlPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Ekran A - Hedef Kullanıcıya Göre Öneri"));

        GridBagConstraints constraints = createGridBagConstraints();
        addLabel(formPanel, "Hedef kullanıcı:", constraints, 0, 0);

        targetUserComboBox.setPreferredSize(new Dimension(220, 28));
        addComponent(formPanel, targetUserComboBox, constraints, 1, 0);

        addLabel(formPanel, "X (benzer kullanıcı):", constraints, 2, 0);
        addComponent(formPanel, targetXField, constraints, 3, 0);

        addLabel(formPanel, "K (film/kullanıcı):", constraints, 4, 0);
        addComponent(formPanel, targetKField, constraints, 5, 0);

        JButton recommendationButton = new JButton("Get Recommendations");
        recommendationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                handleTargetRecommendation();
            }
        });

        addComponent(formPanel, recommendationButton, constraints, 6, 0);
        return formPanel;
    }

    private JPanel createManualMoviePanel() {
        JPanel panel = createRootPanel();
        panel.add(createManualInputPanel(), BorderLayout.NORTH);
        panel.add(createResultScrollPane(manualResultModel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createManualInputPanel() {
        JPanel wrapperPanel = new JPanel(new BorderLayout(8, 8));
        wrapperPanel.setBorder(BorderFactory.createTitledBorder("Ekran B - Filmlere Göre Öneri"));

        JPanel movieGrid = new JPanel(new GridLayout(MANUAL_MOVIE_SELECTION_COUNT, 5, 8, 8));

        for (int index = 0; index < MANUAL_MOVIE_SELECTION_COUNT; index++) {
            JLabel movieLabel = new JLabel("Film " + (index + 1) + ":", SwingConstants.RIGHT);
            JComboBox<Movie> movieComboBox = new JComboBox<Movie>();
            JLabel ratingLabel = new JLabel("Puan:", SwingConstants.RIGHT);
            JTextField ratingField = createSmallNumberField(5);
            JLabel ratingRangeLabel = new JLabel("[1-5]");

            manualMovieComboBoxes.add(movieComboBox);
            manualRatingFields.add(ratingField);

            movieGrid.add(movieLabel);
            movieGrid.add(movieComboBox);
            movieGrid.add(ratingLabel);
            movieGrid.add(ratingField);
            movieGrid.add(ratingRangeLabel);
        }

        loadRandomMoviesToManualComboBoxes();

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionPanel.add(new JLabel("X (benzer kullanıcı):"));
        actionPanel.add(manualXField);
        actionPanel.add(new JLabel("K (film/kullanıcı):"));
        actionPanel.add(manualKField);

        JButton refreshButton = new JButton("Filmleri Yenile");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                loadRandomMoviesToManualComboBoxes();
            }
        });

        JButton recommendationButton = new JButton("Get Recommendations");
        recommendationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                handleManualRecommendation();
            }
        });

        actionPanel.add(refreshButton);
        actionPanel.add(recommendationButton);

        wrapperPanel.add(movieGrid, BorderLayout.CENTER);
        wrapperPanel.add(actionPanel, BorderLayout.SOUTH);
        return wrapperPanel;
    }

    private JScrollPane createResultScrollPane(DefaultListModel<String> resultModel) {
        JList<String> resultList = new JList<String>(resultModel);
        resultList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setVisibleRowCount(18);

        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Önerilen Filmler"));
        return scrollPane;
    }

    private JPanel createRootPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private JTextField createSmallNumberField(int defaultValue) {
        JTextField textField = new JTextField(String.valueOf(defaultValue), 4);
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        return textField;
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 6, 4, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        return constraints;
    }

    private void addLabel(
            JPanel panel,
            String text,
            GridBagConstraints constraints,
            int gridX,
            int gridY
    ) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        addComponent(panel, label, constraints, gridX, gridY);
    }

    private void addComponent(
            JPanel panel,
            java.awt.Component component,
            GridBagConstraints constraints,
            int gridX,
            int gridY
    ) {
        constraints.gridx = gridX;
        constraints.gridy = gridY;
        panel.add(component, constraints);
    }

    private void loadTargetUsers() {
        DefaultComboBoxModel<UserRatings> comboBoxModel = new DefaultComboBoxModel<UserRatings>();

        for (UserRatings targetUser : recommendationEngine.getTargetUsers()) {
            comboBoxModel.addElement(targetUser);
        }

        targetUserComboBox.setModel(comboBoxModel);
    }

    private void loadRandomMoviesToManualComboBoxes() {
        int totalMovieOptionCount = RANDOM_MOVIE_COUNT_PER_COMBO_BOX * MANUAL_MOVIE_SELECTION_COUNT;
        List<Movie> randomMovies = recommendationEngine.getRandomMovies(totalMovieOptionCount);
        int movieCursor = 0;

        for (JComboBox<Movie> movieComboBox : manualMovieComboBoxes) {
            DefaultComboBoxModel<Movie> comboBoxModel = new DefaultComboBoxModel<Movie>();

            if (!randomMovies.isEmpty()) {
                for (int optionIndex = 0; optionIndex < RANDOM_MOVIE_COUNT_PER_COMBO_BOX; optionIndex++) {
                    comboBoxModel.addElement(randomMovies.get(movieCursor % randomMovies.size()));
                    movieCursor++;
                }
            }

            movieComboBox.setModel(comboBoxModel);
        }
    }

    private void handleTargetRecommendation() {
        try {
            final UserRatings selectedTargetUser = (UserRatings) targetUserComboBox.getSelectedItem();
            final int similarUserCount = readPositiveInteger(targetXField, "X");
            final int moviesPerSimilarUser = readPositiveInteger(targetKField, "K");

            runRecommendationInBackground(targetResultModel, new RecommendationJob() {
                @Override
                public List<Recommendation> run() {
                    return recommendationEngine.recommendForTargetUser(
                            selectedTargetUser,
                            similarUserCount,
                            moviesPerSimilarUser
                    );
                }
            });
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void handleManualRecommendation() {
        try {
            final Map<Integer, Integer> manualRatings = readManualRatings();
            final int similarUserCount = readPositiveInteger(manualXField, "X");
            final int moviesPerSimilarUser = readPositiveInteger(manualKField, "K");

            runRecommendationInBackground(manualResultModel, new RecommendationJob() {
                @Override
                public List<Recommendation> run() {
                    return recommendationEngine.recommendForManualRatings(
                            manualRatings,
                            similarUserCount,
                            moviesPerSimilarUser
                    );
                }
            });
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private Map<Integer, Integer> readManualRatings() {
        Map<Integer, Integer> manualRatings = new HashMap<Integer, Integer>();

        for (int index = 0; index < MANUAL_MOVIE_SELECTION_COUNT; index++) {
            Movie selectedMovie = (Movie) manualMovieComboBoxes.get(index).getSelectedItem();

            if (selectedMovie == null) {
                throw new IllegalArgumentException("Her satır için bir film seçilmelidir.");
            }

            if (manualRatings.containsKey(selectedMovie.getMovieId())) {
                throw new IllegalArgumentException("Aynı film birden fazla seçilemez.");
            }

            int rating = readRating(manualRatingFields.get(index), "Film " + (index + 1));
            manualRatings.put(selectedMovie.getMovieId(), rating);
        }

        return manualRatings;
    }

    private int readPositiveInteger(JTextField textField, String fieldName) {
        int value = readInteger(textField, fieldName);

        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " pozitif tam sayı olmalıdır.");
        }

        return value;
    }

    private int readRating(JTextField textField, String fieldName) {
        int value = readInteger(textField, fieldName + " puanı");

        if (value < 1 || value > 5) {
            throw new IllegalArgumentException(fieldName + " puanı 1 ile 5 arasında olmalıdır.");
        }

        return value;
    }

    private int readInteger(JTextField textField, String fieldName) {
        try {
            return Integer.parseInt(textField.getText().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " tam sayı olmalıdır.");
        }
    }

    private void showRecommendations(
            DefaultListModel<String> resultModel,
            List<Recommendation> recommendations
    ) {
        resultModel.clear();

        if (recommendations.isEmpty()) {
            resultModel.addElement("Öneri bulunamadı. Ortak puanlanan film veya eşik üstü benzer kullanıcı olmayabilir.");
            return;
        }

        for (int index = 0; index < recommendations.size(); index++) {
            resultModel.addElement((index + 1) + ". " + recommendations.get(index).toDisplayText());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
    }

    private void runRecommendationInBackground(
            final DefaultListModel<String> resultModel,
            final RecommendationJob recommendationJob
    ) {
        resultModel.clear();
        resultModel.addElement("Hesaplanıyor...");

        SwingWorker<List<Recommendation>, Void> worker = new SwingWorker<List<Recommendation>, Void>() {
            @Override
            protected List<Recommendation> doInBackground() {
                return recommendationJob.run();
            }

            @Override
            protected void done() {
                try {
                    showRecommendations(resultModel, get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Öneri hesaplama işlemi kesildi.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    String message = cause == null ? exception.getMessage() : cause.getMessage();
                    showError(message);
                }
            }
        };

        worker.execute();
    }

    private interface RecommendationJob {
        List<Recommendation> run();
    }
}
