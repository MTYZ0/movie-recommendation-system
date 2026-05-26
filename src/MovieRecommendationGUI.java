import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.UIManager;

/**
 * Film öneri sisteminin Swing tabanlı kullanıcı arayüzüdür.
 * Modern koyu tema (slate/indigo) ve kart tabanlı liste tasarımı ile özelleştirilmiştir.
 */
@SuppressWarnings("serial")
public class MovieRecommendationGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_SIMILAR_USER_COUNT = 3;
    private static final int DEFAULT_MOVIE_COUNT_PER_USER = 3;
    private static final int RANDOM_MOVIE_COUNT_PER_COMBO_BOX = 10;
    private static final int MANUAL_MOVIE_SELECTION_COUNT = 5;

    // Arayüz Renk Paleti (Modern Slate & Royal Indigo)
    private static final Color BG_DARK = new Color(15, 23, 42);       // Slate 900
    private static final Color BG_CARD = new Color(30, 41, 59);       // Slate 800
    private static final Color ACCENT = new Color(79, 70, 229);        // Indigo 600
    private static final Color ACCENT_HOVER = new Color(67, 56, 202);  // Indigo 700
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252); // Slate 50
    private static final Color TEXT_MUTED = new Color(148, 163, 184);   // Slate 400
    private static final Color INPUT_BG = new Color(51, 65, 85);        // Slate 700
    private static final Color BORDER_COLOR = new Color(51, 65, 85);    // Slate 700
    private static final Color NEON_CYAN = new Color(6, 182, 212);      // Cyan 500

    private final RecommendationEngine recommendationEngine;
    private final JComboBox<UserRatings> targetUserComboBox;
    private final JTextField targetXField;
    private final JTextField targetKField;
    private final DefaultListModel<Object> targetResultModel;
    private final List<JComboBox<Movie>> manualMovieComboBoxes;
    private final List<JTextField> manualRatingFields;
    private final JTextField manualXField;
    private final JTextField manualKField;
    private final DefaultListModel<Object> manualResultModel;

    public static MovieRecommendationGUI create(RecommendationEngine recommendationEngine) {
        // Sistem Look & Feel'ini ayarla
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

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
        this.targetResultModel = new DefaultListModel<Object>();
        this.manualMovieComboBoxes = new ArrayList<JComboBox<Movie>>();
        this.manualRatingFields = new ArrayList<JTextField>();
        this.manualXField = createSmallNumberField(DEFAULT_SIMILAR_USER_COUNT);
        this.manualKField = createSmallNumberField(DEFAULT_MOVIE_COUNT_PER_USER);
        this.manualResultModel = new DefaultListModel<Object>();
    }

    private void configureFrame() {
        setTitle("🎬 Heap Tabanlı İşbirlikçi Filtreleme Öneri Sistemi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 720);
        setMinimumSize(new Dimension(880, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_CARD);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabbedPane.addTab("Hedef Kullanıcı (Ekran A)", createTargetUserPanel());
        tabbedPane.addTab("Film Puanlarına Göre (Ekran B)", createManualMoviePanel());
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
        formPanel.setBackground(BG_CARD);
        formPanel.setBorder(createStyledTitledBorder("Ekran A - Hedef Kullanıcı Analizi ve Öneri"));

        GridBagConstraints constraints = createGridBagConstraints();

        JLabel userLabel = new JLabel("Hedef kullanıcı:");
        styleLabel(userLabel, false);
        addComponent(formPanel, userLabel, constraints, 0, 0);

        targetUserComboBox.setPreferredSize(new Dimension(240, 30));
        styleComboBox(targetUserComboBox);
        addComponent(formPanel, targetUserComboBox, constraints, 1, 0);

        JLabel xLabel = new JLabel("X (benzer kullanıcı):");
        styleLabel(xLabel, false);
        addComponent(formPanel, xLabel, constraints, 2, 0);

        styleTextField(targetXField);
        addComponent(formPanel, targetXField, constraints, 3, 0);

        JLabel kLabel = new JLabel("K (film/kullanıcı):");
        styleLabel(kLabel, false);
        addComponent(formPanel, kLabel, constraints, 4, 0);

        styleTextField(targetKField);
        addComponent(formPanel, targetKField, constraints, 5, 0);

        JButton recommendationButton = new JButton("Öneri Al");
        styleButton(recommendationButton, true);
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
        JPanel wrapperPanel = new JPanel(new BorderLayout(10, 10));
        wrapperPanel.setBackground(BG_CARD);
        wrapperPanel.setBorder(createStyledTitledBorder("Ekran B - Özel Film Puanlama Analizi"));

        JPanel movieGrid = new JPanel(new GridLayout(MANUAL_MOVIE_SELECTION_COUNT, 5, 10, 8));
        movieGrid.setBackground(BG_CARD);

        for (int index = 0; index < MANUAL_MOVIE_SELECTION_COUNT; index++) {
            JLabel movieLabel = new JLabel("Film " + (index + 1) + ":", SwingConstants.RIGHT);
            styleLabel(movieLabel, false);

            JComboBox<Movie> movieComboBox = new JComboBox<Movie>();
            styleComboBox(movieComboBox);
            movieComboBox.setPreferredSize(new Dimension(280, 28));

            JLabel ratingLabel = new JLabel("Puan:", SwingConstants.RIGHT);
            styleLabel(ratingLabel, false);

            JTextField ratingField = createSmallNumberField(5);
            styleTextField(ratingField);

            JLabel ratingRangeLabel = new JLabel("[1-5]");
            styleLabel(ratingRangeLabel, false);
            ratingRangeLabel.setForeground(NEON_CYAN); // Siber açık mavi ipucu rengi

            manualMovieComboBoxes.add(movieComboBox);
            manualRatingFields.add(ratingField);

            movieGrid.add(movieLabel);
            movieGrid.add(movieComboBox);
            movieGrid.add(ratingLabel);
            movieGrid.add(ratingField);
            movieGrid.add(ratingRangeLabel);
        }

        loadRandomMoviesToManualComboBoxes();

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        actionPanel.setBackground(BG_CARD);

        JLabel xLabel = new JLabel("X (benzer kullanıcı):");
        styleLabel(xLabel, false);
        actionPanel.add(xLabel);

        styleTextField(manualXField);
        actionPanel.add(manualXField);

        JLabel kLabel = new JLabel("K (film/kullanıcı):");
        styleLabel(kLabel, false);
        actionPanel.add(kLabel);

        styleTextField(manualKField);
        actionPanel.add(manualKField);

        JButton refreshButton = new JButton("Filmleri Yenile");
        styleButton(refreshButton, false);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                loadRandomMoviesToManualComboBoxes();
            }
        });

        JButton recommendationButton = new JButton("Öneri Al");
        styleButton(recommendationButton, true);
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

    private JScrollPane createResultScrollPane(DefaultListModel<Object> resultModel) {
        JList<Object> resultList = new JList<Object>(resultModel);
        resultList.setCellRenderer(new RecommendationCellRenderer());
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setVisibleRowCount(18);
        resultList.setBackground(BG_DARK);

        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(createStyledTitledBorder("Önerilen Filmler (Max-Heap ile Sıralı)"));
        scrollPane.setBackground(BG_DARK);
        return scrollPane;
    }

    private JPanel createRootPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(BG_DARK);
        return panel;
    }

    private JTextField createSmallNumberField(int defaultValue) {
        JTextField textField = new JTextField(String.valueOf(defaultValue), 4);
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        return textField;
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
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
        styleLabel(label, false);
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

    // Arayüz Elemanı Özelleştirme Metotları
    private void styleLabel(JLabel label, boolean isHeader) {
        if (isHeader) {
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            label.setForeground(TEXT_PRIMARY);
        } else {
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));
            label.setForeground(TEXT_MUTED);
        }
    }

    private void styleTextField(JTextField textField) {
        textField.setBackground(INPUT_BG);
        textField.setForeground(TEXT_PRIMARY);
        textField.setCaretColor(TEXT_PRIMARY);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(INPUT_BG);
        comboBox.setForeground(BG_DARK);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    }

    private void styleButton(final JButton button, boolean isPrimary) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));

        if (isPrimary) {
            button.setBackground(ACCENT);
            button.setForeground(Color.WHITE);
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(ACCENT_HOVER);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(ACCENT);
                }
            });
        } else {
            button.setBackground(new Color(51, 65, 85)); // Slate 700
            button.setForeground(TEXT_PRIMARY);
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(71, 85, 105)); // Slate 600
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(51, 65, 85));
                }
            });
        }
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
    }

    private javax.swing.border.Border createStyledTitledBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                TEXT_MUTED
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
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
            DefaultListModel<Object> resultModel,
            List<Recommendation> recommendations
    ) {
        resultModel.clear();

        if (recommendations.isEmpty()) {
            resultModel.addElement("Öneri bulunamadı. Ortak puanlanan film veya eşik üstü benzer kullanıcı olmayabilir.");
            return;
        }

        for (Recommendation recommendation : recommendations) {
            resultModel.addElement(recommendation);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
    }

    private void runRecommendationInBackground(
            final DefaultListModel<Object> resultModel,
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

    /**
     * Önerileri visual bir liste kartı olarak çizen özel Cell Renderer.
     */
    private static class RecommendationCellRenderer extends JPanel implements javax.swing.ListCellRenderer<Object> {
        private final JLabel rankLabel;
        private final JLabel titleLabel;
        private final JLabel infoLabel;
        private final JLabel ratingLabel;

        public RecommendationCellRenderer() {
            setLayout(new BorderLayout(15, 6));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));
            setBackground(BG_CARD);

            // Sıralama Numarası Badge'i
            rankLabel = new JLabel();
            rankLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            rankLabel.setForeground(NEON_CYAN);
            rankLabel.setPreferredSize(new Dimension(40, 40));
            rankLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // Yazı Paneli (Başlık ve Alt Bilgi)
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
            textPanel.setOpaque(false);

            titleLabel = new JLabel();
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            titleLabel.setForeground(TEXT_PRIMARY);

            infoLabel = new JLabel();
            infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            infoLabel.setForeground(TEXT_MUTED);

            textPanel.add(titleLabel);
            textPanel.add(infoLabel);

            // Puan Göstergesi (Yıldızlar)
            ratingLabel = new JLabel();
            ratingLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            ratingLabel.setForeground(new Color(245, 158, 11)); // Altın sarısı (Amber 500)
            ratingLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            add(rankLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
            add(ratingLabel, BorderLayout.EAST);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            if (value instanceof Recommendation) {
                Recommendation rec = (Recommendation) value;

                rankLabel.setText("#" + (index + 1));
                rankLabel.setVisible(true);

                titleLabel.setText(rec.getMovie().getTitle());

                String similarityPct = String.format(java.util.Locale.US, "%.2f%%", rec.getSourceSimilarity() * 100.0);
                infoLabel.setText("👤 Benzer Kullanıcı: " + UserRatings.resolveName(rec.getSourceUserId()) + "   •   🔗 Benzerlik: " + similarityPct);

                // Yıldız şeklinde puan çizimi
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < rec.getRating(); i++) {
                    stars.append("★");
                }
                for (int i = rec.getRating(); i < 5; i++) {
                    stars.append("☆");
                }
                ratingLabel.setText(stars.toString() + " (" + rec.getRating() + "/5)");
                ratingLabel.setVisible(true);

                // Seçim rengi
                if (isSelected) {
                    setBackground(new Color(79, 70, 229, 65)); // Hafif saydam Indigo
                } else {
                    setBackground(BG_CARD);
                }
            } else {
                // "Hesaplanıyor..." veya "Öneri bulunamadı..." gibi düz metinler için
                rankLabel.setVisible(false);
                ratingLabel.setVisible(false);
                titleLabel.setText(value != null ? value.toString() : "");
                titleLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
                infoLabel.setText("");

                if (isSelected) {
                    setBackground(new Color(79, 70, 229, 45));
                } else {
                    setBackground(BG_CARD);
                }
            }

            return this;
        }
    }
}
