package com.library;

import org.junit.jupiter.api.*;
import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogueTest {

    private Catalogue catalogue;

    @BeforeEach
    void setUp() {
        catalogue = new Catalogue();
        catalogue.addPublication(new Book("Кобзар", 1840, "Тарас Шевченко", "Знання", "Поезія"));
        catalogue.addPublication(new Book("Тіні забутих предків", 1911, "Михайло Коцюбинський", "Дніпро", "Проза"));
        catalogue.addPublication(new Book("1984", 1949, "Джордж Орвелл", "Secker & Warburg", "Антиутопія"));
    }

    // ✅ ВАЛІДНІ ТЕСТИ

    @Test
    void testAddPublication() {
        int before = catalogue.getAllPublications().size();
        catalogue.addPublication(new Book("Нова книга", 2024, "Автор", "Видавництво", "Жанр"));
        assertEquals(before + 1, catalogue.getAllPublications().size());
    }

    @Test
    void testFindByExactTitle() {
        Publication p = catalogue.findPublicationByTitle("1984");
        assertNotNull(p);
        assertEquals("1984", p.getTitle());
    }

    @Test
    void testFindByTitleCaseInsensitive() {
        Publication p = catalogue.findPublicationByTitle("кобзар");
        assertNotNull(p);
    }

    @Test
    void testRemoveExistingBook() {
        assertDoesNotThrow(() -> catalogue.removePublicationByTitle("1984"));
        assertNull(catalogue.findPublicationByTitle("1984"));
    }

    @Test
    void testSearchPartialTitle() {
        List<Publication> results = catalogue.searchByTitle("тін");
        assertEquals(1, results.size());
        assertEquals("Тіні забутих предків", results.get(0).getTitle());
    }

    @Test
    void testSearchReturnsMultiple() {
        catalogue.addPublication(new Book("Тіні минулого", 2000, "Автор", "Вид", "Проза"));
        List<Publication> results = catalogue.searchByTitle("тін");
        assertEquals(2, results.size());
    }

    @Test
    void testUpdateBook() throws BookNotFoundException {
        Book updated = new Book("Кобзар", 1840, "Тарас Шевченко", "Нове видавництво", "Поезія");
        catalogue.updatePublication("Кобзар", updated);
        Publication p = catalogue.findPublicationByTitle("Кобзар");
        assertEquals("Нове видавництво", ((Book) p).getPublisher());
    }

    @Test
    void testSaveAndLoadFromFile() throws Exception {
        String filename = "test_catalogue.dat";
        catalogue.saveToFile(filename);

        Catalogue loaded = new Catalogue();
        loaded.loadFromFile(filename);

        assertEquals(catalogue.getAllPublications().size(), loaded.getAllPublications().size());
        assertEquals("1984", loaded.findPublicationByTitle("1984").getTitle());

        new File(filename).delete();
    }

    @Test
    void testBookToString() {
        Book b = new Book("Тест", 2020, "Автор", "Вид", "Жанр");
        String s = b.toString();
        assertTrue(s.contains("Тест"));
        assertTrue(s.contains("Автор"));
    }

    // ❌ НЕВАЛІДНІ ТЕСТИ

    @Test
    void testRemoveNonExistentBook() {
        assertThrows(BookNotFoundException.class,
                () -> catalogue.removePublicationByTitle("Неіснуюча книга"));
    }

    @Test
    void testUpdateNonExistentBook() {
        Book updated = new Book("Неіснуюча", 2020, "Автор", "Вид", "Жанр");
        assertThrows(BookNotFoundException.class,
                () -> catalogue.updatePublication("Неіснуюча", updated));
    }

    @Test
    void testFindNonExistentBook() {
        Publication p = catalogue.findPublicationByTitle("Такої книги нема");
        assertNull(p);
    }

    @Test
    void testSearchEmptyQuery() {
        List<Publication> results = catalogue.searchByTitle("");
        assertEquals(catalogue.getAllPublications().size(), results.size());
    }

    @Test
    void testBookNotFoundExceptionMessage() {
        BookNotFoundException ex = new BookNotFoundException("Тестова книга");
        assertTrue(ex.getMessage().contains("Тестова книга"));
    }

    @Test
    void testLoadFromNonExistentFile() {
        Catalogue c = new Catalogue();
        assertThrows(IOException.class, () -> c.loadFromFile("non_existent_file.dat"));
    }
}
