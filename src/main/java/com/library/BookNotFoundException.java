package com.library;

public class BookNotFoundException extends Exception {
    public BookNotFoundException(String title) {
        super("Книгу з назвою \"" + title + "\" не знайдено в каталозі.");
    }
}
