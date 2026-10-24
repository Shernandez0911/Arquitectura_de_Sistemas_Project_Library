/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Loan service: lends books, lists loans, closes them when the book comes back and finds
 * the overdue ones. Every inventory change goes through {@link BookService}.
 */
public final class LoanService {

    /**
     * Persistence access for loans.
     */
    private final LoanDao loanDao;

    /**
     * Persistence access for members.
     */
    private final MemberDao memberDao;

    /**
     * Catalog service, the single owner of the available copies.
     */
    private final BookService bookService;

    /**
     * Clock used to read the current date.
     */
    private final Clock clock;

    /**
     * Creates the service.
     *
     * @param loanDao     the loan DAO.
     * @param memberDao   the member DAO.
     * @param bookService the catalog service.
     * @param clock       the clock used to read the current date.
     */
    public LoanService(LoanDao loanDao, MemberDao memberDao, BookService bookService, Clock clock) {
        this.loanDao = loanDao;
        this.memberDao = memberDao;
        this.bookService = bookService;
        this.clock = clock;
    }

    /**
     * Lists every loan. Open overdue loans carry their accrued fee (not persisted).
     *
     * @return all loans.
     */
    public List<Loan> findAll() {
        return withAccruedFees(loanDao.findAll());
    }

    /**
     * Lends a book to a member: takes one copy out of the inventory and creates a loan
     * that is due according to {@link LoanPolicy#dueDate(LocalDate)}. Both writes run in
     * one transaction.
     *
     * @param memberId the id of the member who borrows.
     * @param bookId   the id of the book to borrow.
     * @return the new loan.
     * @throws NotFoundException     if the member or the book does not exist.
     * @throws IllegalStateException if the book has no available copies.
     */
    public Loan checkout(int memberId, int bookId) {
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new NotFoundException("Member not found: " + memberId);
        }
        if (bookService.findById(bookId) == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }

        LocalDate today = LocalDate.now(clock);
        return inTransaction(() -> {
            bookService.borrow(bookId);
            Book book = bookService.findById(bookId);
            Loan loan = new Loan(member, book, today, LoanPolicy.dueDate(today));
            loanDao.create(loan);
            return loan;
        });
    }

    /**
     * Closes a loan: records the return date, charges the overdue fee if the book is late
     * and puts the copy back into the inventory. The loan update and the inventory change
     * run in one transaction.
     *
     * @param loanId the id of the loan to close.
     * @return the closed loan.
     * @throws NotFoundException     if the loan does not exist.
     * @throws IllegalStateException if the loan was already returned.
     */
    public Loan returnLoan(int loanId) {
        Loan loan = loanDao.findById(loanId);
        if (loan == null) {
            throw new NotFoundException("Loan not found: " + loanId);
        }
        if (loan.isReturned()) {
            throw new IllegalStateException("Loan already returned: " + loanId);
        }

        LocalDate today = LocalDate.now(clock);
        loan.setReturned(true);
        loan.setReturnDate(today);
        if (today.isAfter(loan.getDueDate())) {
            loan.setOverdueFee(accruedFee(loan, today));
        }

        return inTransaction(() -> {
            loanDao.update(loan);
            bookService.returnCopy(loan.getBook().getId());
            return loan;
        });
    }

    /**
     * Lists the loans that are still open and past their due date, with their accrued fee
     * (not persisted).
     *
     * @return the overdue loans.
     */
    public List<Loan> overdueLoans() {
        LocalDate today = LocalDate.now(clock);
        List<Loan> overdue = loanDao.findAll().stream()
                .filter(loan -> !loan.isReturned() && loan.getDueDate().isBefore(today))
                .toList();
        return withAccruedFees(overdue);
    }

    /**
     * Sets the accrued fee on every open overdue loan of the list. The fee is not persisted.
     *
     * @param loans the loans to complete.
     * @return the same loans, with the fee set on the open overdue ones.
     */
    private List<Loan> withAccruedFees(List<Loan> loans) {
        LocalDate today = LocalDate.now(clock);
        for (Loan loan : loans) {
            if (!loan.isReturned() && loan.getDueDate().isBefore(today)) {
                loan.setOverdueFee(accruedFee(loan, today));
            }
        }
        return loans;
    }

    /**
     * Computes the fee of a loan up to a date.
     *
     * @param loan  the loan.
     * @param today the date up to which the fee accrues.
     * @return the days past the due date times {@link LoanPolicy#FEE_PER_DAY}.
     */
    private static double accruedFee(Loan loan, LocalDate today) {
        return ChronoUnit.DAYS.between(loan.getDueDate(), today) * LoanPolicy.FEE_PER_DAY;
    }

    /**
     * Runs a unit of work in a database transaction. A domain error thrown inside is
     * rethrown as is (see {@code BaseDao.transaction}).
     *
     * @param work the unit of work.
     * @param <R>  the result type.
     * @return the result of the unit of work.
     * @throws RuntimeException if the transaction fails with a database error.
     */
    private <R> R inTransaction(Callable<R> work) {
        try {
            return loanDao.transaction(work);
        } catch (SQLException e) {
            throw new RuntimeException("Transaction failed", e);
        }
    }
}
