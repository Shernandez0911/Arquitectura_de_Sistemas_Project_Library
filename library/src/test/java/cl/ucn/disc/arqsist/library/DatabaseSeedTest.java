/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the initial data is consistent and that seeding twice does not duplicate it.
 */
class DatabaseSeedTest {

    /**
     * The database under test.
     */
    private Database db;

    /**
     * DAO used to read the books.
     */
    private BookDao bookDao;

    /**
     * DAO used to read the loans.
     */
    private LoanDao loanDao;

    /**
     * DAO used to read the reservations.
     */
    private ReservationDao reservationDao;

    /**
     * Opens an in-memory database and seeds it.
     *
     * @throws Exception if the database cannot be set up.
     */
    @BeforeEach
    void setUp() throws Exception {
        db = new Database("jdbc:sqlite::memory:");
        bookDao = new BookDao(db.connectionSource());
        loanDao = new LoanDao(db.connectionSource());
        reservationDao = new ReservationDao(db.connectionSource());
        db.seedIfEmpty();
    }

    /**
     * Checks that the seed creates loans and reservations, that exactly one loan is open
     * and overdue, and that every book with an open loan has fewer available copies than
     * total copies.
     */
    @Test
    void seedCreatesConsistentData() {
        List<Loan> loans = loanDao.findAll();
        assertFalse(loans.isEmpty(), "the seed should create loans");
        assertFalse(reservationDao.findAll().isEmpty(), "the seed should create reservations");

        LocalDate today = LocalDate.now();
        long overdue = loans.stream()
                .filter(loan -> !loan.isReturned() && loan.getDueDate().isBefore(today))
                .count();
        assertEquals(1, overdue, "exactly one loan should be open and overdue");

        for (Loan loan : loans) {
            if (!loan.isReturned()) {
                Book book = bookDao.findById(loan.getBook().getId());
                assertTrue(book.getAvailableCopies() < book.getTotalCopies(),
                        "the book of an open loan should have fewer available copies: " + book.getTitle());
            }
        }
    }

    /**
     * Seeds a second time and checks that the number of loans and reservations is the same.
     *
     * @throws Exception if the second seeding fails.
     */
    @Test
    void seedingTwiceDoesNotDuplicateData() throws Exception {
        int loans = loanDao.findAll().size();
        int reservations = reservationDao.findAll().size();

        db.seedIfEmpty();

        assertEquals(loans, loanDao.findAll().size(), "the number of loans should not change");
        assertEquals(reservations, reservationDao.findAll().size(),
                "the number of reservations should not change");
    }
}
