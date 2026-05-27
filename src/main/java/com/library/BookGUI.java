package com.library;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class BookGUI extends Application {

    private Catalogue catalogue = new Catalogue();
    private ObservableList<String> listItems = FXCollections.observableArrayList();
    private ListView<String> listView = new ListView<>(listItems);

    private TextField titleField = new TextField();
    private TextField authorField = new TextField();
    private TextField publisherField = new TextField();
    private TextField yearField = new TextField();
    private TextField genreField = new TextField();
    private TextField searchField = new TextField();

    private Label statusLabel = new Label("Готово");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Каталог книг");

        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        form.add(new Label("Назва:"), 0, 0);
        form.add(titleField, 1, 0);
        form.add(new Label("Автор:"), 0, 1);
        form.add(authorField, 1, 1);
        form.add(new Label("Видавництво:"), 0, 2);
        form.add(publisherField, 1, 2);
        form.add(new Label("Рік:"), 0, 3);
        form.add(yearField, 1, 3);
        form.add(new Label("Жанр:"), 0, 4);
        form.add(genreField, 1, 4);

        titleField.setPromptText("Введіть назву");
        authorField.setPromptText("Введіть автора");
        publisherField.setPromptText("Введіть видавництво");
        yearField.setPromptText("Рік (наприклад, 2024)");
        genreField.setPromptText("Введіть жанр");

        // Buttons
        Button addBtn = new Button("➕ Додати книгу");
        Button removeBtn = new Button("🗑 Видалити книгу");
        Button updateBtn = new Button("↺ Оновити книгу");
        Button saveBtn = new Button("💾 Зберегти у файл");
        Button loadBtn = new Button("📂 Завантажити з файлу");
        Button searchBtn = new Button("🔍 Пошук");
        Button resetBtn = new Button("↺ Скинути пошук");

        addBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        loadBtn.setMaxWidth(Double.MAX_VALUE);
        searchBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setMaxWidth(Double.MAX_VALUE);

        VBox buttons = new VBox(8, addBtn, removeBtn, updateBtn, new Separator(), saveBtn, loadBtn, new Separator());

        // Search
        HBox searchBox = new HBox(8, searchField, searchBtn, resetBtn);
        searchBtn.setMinWidth(80);
        resetBtn.setMinWidth(120);
        searchField.setPromptText("Пошук за назвою...");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Status
        statusLabel.setStyle("-fx-text-fill: #555; -fx-font-style: italic;");

        // Layout
        VBox left = new VBox(10, form, buttons, searchBox);
        left.setPadding(new Insets(10));
        left.setPrefWidth(320);

        VBox right = new VBox(10, new Label("📚 Список книг:"), listView, statusLabel);
        right.setPadding(new Insets(10));
        VBox.setVgrow(listView, Priority.ALWAYS);

        listView.setOnMouseClicked(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null)
                return;
            for (Publication p : catalogue.getAllPublications()) {
                if (selected.equals(p.toString())) {
                    titleField.setText(p.getTitle());
                    yearField.setText(String.valueOf(p.getYear()));
                    if (p instanceof Book b) {
                        authorField.setText(b.getAuthor());
                        publisherField.setText(b.getPublisher());
                        genreField.setText(b.getGenre());
                    }
                    break;
                }
            }
        });

        HBox root = new HBox(10, left, right);
        HBox.setHgrow(right, Priority.ALWAYS);

        // Button actions
        addBtn.setOnAction(e -> addBook());
        removeBtn.setOnAction(e -> removeBook(primaryStage));
        updateBtn.setOnAction(e -> updateBook());
        saveBtn.setOnAction(e -> saveToFile(primaryStage));
        loadBtn.setOnAction(e -> loadFromFile(primaryStage));
        searchBtn.setOnAction(e -> searchBooks());
        resetBtn.setOnAction(e -> resetSearch());

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Integer parseYear() {
        try {
            int y = Integer.parseInt(yearField.getText().trim());
            if (y < 1000 || y > 2100) {
                showError("Рік має бути між 1000 та 2100.");
                return null;
            }
            return y;
        } catch (NumberFormatException e) {
            showError("Рік введено некоректно. Введіть число.");
            return null;
        }
    }

    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String publisher = publisherField.getText().trim();
        String genre = genreField.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            showError("Назва та автор є обов'язковими полями.");
            return;
        }
        Integer year = parseYear();
        if (year == null)
            return;

        Book book = new Book(title, year, author, publisher, genre);
        catalogue.addPublication(book);
        refreshList();
        setStatus("Книгу \"" + title + "\" додано.");
        clearFields();
    }

    private void removeBook(Stage stage) {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showError("Введіть назву книги для видалення.");
            return;
        }
        try {
            catalogue.removePublicationByTitle(title);
            refreshList();
            setStatus("Книгу \"" + title + "\" видалено.");
            clearFields();
        } catch (BookNotFoundException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String publisher = publisherField.getText().trim();
        String genre = genreField.getText().trim();

        if (title.isEmpty()) {
            showError("Введіть назву книги для оновлення.");
            return;
        }
        Integer year = parseYear();
        if (year == null)
            return;

        Book updated = new Book(title, year, author, publisher, genre);
        try {
            catalogue.updatePublication(title, updated);
            refreshList();
            setStatus("Книгу \"" + title + "\" оновлено.");
            clearFields();
        } catch (BookNotFoundException ex) {
            showError(ex.getMessage());
        }
    }

    private void saveToFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Зберегти каталог");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Каталог (*.dat)", "*.dat"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                catalogue.saveToFile(file.getAbsolutePath());
                setStatus("Збережено у " + file.getName());
            } catch (Exception ex) {
                showError("Помилка збереження: " + ex.getMessage());
            }
        }
    }

    private void loadFromFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Завантажити каталог");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Каталог (*.dat)", "*.dat"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            try {
                catalogue.loadFromFile(file.getAbsolutePath());
                refreshList();
                setStatus("Завантажено з " + file.getName());
            } catch (Exception ex) {
                showError("Помилка завантаження: " + ex.getMessage());
            }
        }
    }

    private void searchBooks() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshList();
            return;
        }
        List<Publication> results = catalogue.searchByTitle(query);
        listItems.clear();
        for (Publication p : results) {
            listItems.add(p.toString());
        }
        setStatus("Знайдено: " + results.size() + " кн.");
    }

    private void resetSearch() {
        searchField.clear();
        refreshList();
        setStatus("Фільтр скинуто.");
    }

    private void refreshList() {
        listItems.clear();
        for (Publication p : catalogue.getAllPublications()) {
            listItems.add(p.toString());
        }
    }

    private void clearFields() {
        titleField.clear();
        authorField.clear();
        publisherField.clear();
        yearField.clear();
        genreField.clear();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
        setStatus("⚠ " + msg);
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
