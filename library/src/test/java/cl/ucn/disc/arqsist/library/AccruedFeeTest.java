/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that listing loans shows the accrued fee of an open overdue loan and no fee
 * for a loan that is not overdue.
 */
class AccruedFeeTest {

    /**
     * Service under test.
     */
    private LoanService loanService;

    /**
     * An open loan that was due on 2025-12-22.
     */
    private Loan overdueLoan;

    /**
     * An open loan that is not overdue yet.
     */
    private Loan activeLoan;

    /**
     * Builds an in-memory database with one book, one member and two open loans, and a
     * fixed clock set to 2026-01-15.
     *
     * @throws Exception if the database cannot be set up.
     */
    @BeforeEach
    void setUp() throws Exception {
        Database db = new Database("jdbc:sqlite::memory:");
        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());

        Clock clock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);
        loanService = new LoanService(loanDao, memberDao, new BookService(bookDao), clock);

        Book book = new Book("Refactoring", "Martin Fowler", "9780134757599", 2);
        bookDao.create(book);
        Member member = new Member("Grace Hopper", "grace@example.com");
        memberDao.create(member);

        overdueLoan = new Loan(member, book, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 22));
        loanDao.create(overdueLoan);
        activeLoan = new Loan(member, book, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 31));
        loanDao.create(activeLoan);
    }

    /**
     * Lists the loans and checks the fee of each one: 24 days past the due date for the
     * overdue loan, and none for the active loan.
     */
    @Test
    void findAllShowsTheAccruedFeeOfOverdueLoans() {
        List<Loan> loans = loanService.findAll();

        assertEquals(24.0, feeOf(loans, overdueLoan), 0.0001,
                "the overdue loan should show 24 days of fee");
        assertEquals(0.0, feeOf(loans, activeLoan), 0.0001,
                "the active loan should have no fee");
    }

    /**
     * Finds a loan in the list by id and returns its fee.
     *
     * @param loans the listed loans.
     * @param loan  the loan to look for.
     * @return the fee of that loan.
     */
    private static double feeOf(List<Loan> loans, Loan loan) {
        return loans.stream()
                .filter(l -> l.getId() == loan.getId())
                .findFirst()
                .orElseThrow()
                .getOverdueFee();
    }
}
