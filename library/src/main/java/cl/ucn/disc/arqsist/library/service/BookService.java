/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.model.Book;

import java.util.List;

/**
 * Catalog service. It is the single owner of {@code Book.availableCopies}: every
 * inventory change passes through {@link #borrow(int)} or {@link #returnCopy(int)}.
 */
public final class BookService {

    /**
     * Persistence access for books.
     */
    private final BookDao dao;

    /**
     * Creates the service.
     *
     * @param dao the book DAO.
     */
    public BookService(BookDao dao) {
        this.dao = dao;
    }

    /**
     * Lists every book in the catalog.
     *
     * @return all books.
     */
    public List<Book> listAll() {
        return dao.findAll();
    }

    /**
     * Finds a book by its id.
     *
     * @param id the book id.
     * @return the book, or {@code null} if it does not exist.
     */
    public Book findById(int id) {
        return dao.findById(id);
    }

    /**
     * Registers a new book. All copies start as available.
     *
     * @param book the book to register.
     * @return the registered book.
     */
    public Book create(Book book) {
        book.setAvailableCopies(book.getTotalCopies());
        dao.create(book);
        return book;
    }

    /**
     * Takes one copy out of the inventory.
     *
     * @param bookId the id of the book to borrow.
     * @throws NotFoundException     if the book does not exist.
     * @throws IllegalStateException if the book has no available copies.
     */
    public void borrow(int bookId) {
        Book book = dao.findById(bookId);
        if (book == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies of book " + bookId);
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        dao.update(book);
    }

    /**
     * Puts one copy back into the inventory.
     *
     * @param bookId the id of the book to return.
     * @throws NotFoundException if the book does not exist.
     */
    public void returnCopy(int bookId) {
        Book book = dao.findById(bookId);
        if (book == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        dao.update(book);
    }
}
