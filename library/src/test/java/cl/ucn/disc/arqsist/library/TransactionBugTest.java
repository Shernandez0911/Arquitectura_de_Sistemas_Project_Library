/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks that a failed checkout leaves the inventory unchanged.
 */
class TransactionBugTest {

    /**
     * DAO used to create and reload the test book.
     */
    private BookDao bookDao;

    /**
     * Service under test.
     */
    private MemberService memberService;

    /**
     * Builds an in-memory database and the service under test.
     *
     * @throws Exception if the database cannot be set up.
     */
    @BeforeEach
    void setUp() throws Exception {
        Database db = new Database("jdbc:sqlite::memory:");
        bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        memberService = new MemberService(memberDao, bookDao, loanDao);
    }

    /**
     * Checks out a book for a member that does not exist and verifies that the copy
     * count of the book did not change.
     *
     * @throws Exception if a DAO call fails.
     */
    @Test
    void checkoutLeavesNoPartialStateOnFailure() throws Exception {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", 2);
        bookDao.create(book);

        assertThrows(Exception.class, () -> memberService.checkout(9999, book.getId()));

        Book reloaded = bookDao.findById(book.getId());
        assertEquals(reloaded.getTotalCopies(), reloaded.getAvailableCopies(),
                "availableCopies was decremented even though the loan was never created");
    }
}
