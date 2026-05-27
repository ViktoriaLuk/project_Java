package com.library;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Catalogue {
    private ArrayList<Publication> publications = new ArrayList<>();

    public void addPublication(Publication p) {
        publications.add(p);
    }

    public void removePublicationByTitle(String title) throws BookNotFoundException {
        Publication found = findPublicationByTitle(title);
        if (found == null) throw new BookNotFoundException(title);
        publications.remove(found);
    }

    public Publication findPublicationByTitle(String title) {
        return publications.stream()
                .filter(p -> p.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }

    public List<Publication> searchByTitle(String query) {
        String lower = query.toLowerCase();
        return publications.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public ArrayList<Publication> getAllPublications() {
        return publications;
    }

    public void updatePublication(String title, Book updated) throws BookNotFoundException {
        Publication found = findPublicationByTitle(title);
        if (found == null) throw new BookNotFoundException(title);
        int index = publications.indexOf(found);
        publications.set(index, updated);
    }

    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(publications);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            publications = (ArrayList<Publication>) ois.readObject();
        }
    }
}
