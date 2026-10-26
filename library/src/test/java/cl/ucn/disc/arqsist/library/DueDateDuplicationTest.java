/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;
import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import cl.ucn.disc.arqsist.library.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that a loan made by checkout and a loan made by fulfilling a reservation
 * get the same loan period.
 */
class DueDateDuplicationTest {

    /**
     * Service used to check out the book.
     */
    private LoanService loanService;

    /**
     * Service used to reserve and fulfill.
     */
    private ReservationService reservationService;

    /**
     * The book used by the test.
     */
    private Book book;

    /**
     * The member used by the test.
     */
    private Member member;

    /**
     * Builds an in-memory database with one book (two copies) and one member, and a fixed
     * clock shared by both services.
     *
     * @throws Exception if the database cannot be set up.
     */
    @BeforeEach
    void setUp() throws Exception {
        Database db = new Database("jdbc:sqlite::memory:");
        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        ReservationDao reservationDao = new ReservationDao(db.connectionSource());

        Clock clock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);
        BookService bookService = new BookService(bookDao);
        loanService = new LoanService(loanDao, memberDao, bookService, clock);
        reservationService = new ReservationService(reservationDao, memberDao, loanDao, bookService, clock);

        book = bookService.create(new Book("Design Patterns", "Gamma et al.", "9780201633610", 2));
        member = new Member("Grace Hopper", "grace@example.com");
        memberDao.create(member);
    }

    /**
     * Creates one loan by checkout and one by fulfill, and compares their due dates.
     * Each of the two calls borrows one of the two copies.
     */
    @Test
    void checkoutAndFulfillUseTheSameLoanPeriod() {
        Loan fromCheckout = loanService.checkout(member.getId(), book.getId());

        Reservation reservation = reservationService.reserve(book.getId(), member.getId());
        Loan fromFulfill = reservationService.fulfill(reservation.getId());

        assertEquals(fromCheckout.getDueDate(), fromFulfill.getDueDate(),
                "the same kind of loan should have the same due date");
    }
}
