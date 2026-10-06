/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Loan service: lists loans, closes them when the book comes back and finds the overdue ones.
 */
public final class LoanService {

    /**
     * Persistence access for loans.
     */
    private final LoanDao loanDao;

    /**
     * Persistence access for books.
     */
    private final BookDao bookDao;

    /**
     * Creates the service.
     *
     * @param loanDao the loan DAO.
     * @param bookDao the book DAO.
     */
    public LoanService(LoanDao loanDao, BookDao bookDao) {
        this.loanDao = loanDao;
        this.bookDao = bookDao;
    }

    /**
     * Lists every loan.
     *
     * @return all loans.
     * @throws SQLException if the query fails.
     */
    public List<Loan> findAll() throws SQLException {
        return loanDao.findAll();
    }

    /**
     * Closes a loan: records the return date, charges the overdue fee if the book is late
     * and puts the copy back into the inventory.
     *
     * @param loanId the id of the loan to close.
     * @return the closed loan, or the loan as it is when it does not exist ({@code null})
     *         or was already returned.
     * @throws SQLException if a read or write fails.
     */
    public Loan returnLoan(int loanId) throws SQLException {
        Loan loan = loanDao.findById(loanId);
        if (loan == null || loan.isReturned()) {
            return loan;
        }

        loan.setReturned(true);
        loan.setReturnDate(LocalDate.now());

        LocalDate due = loan.getDueDate();
        LocalDate today = LocalDate.now();
        if (today.isAfter(due)) {
            long daysOverdue = ChronoUnit.DAYS.between(due, today);
            loan.setOverdueFee(daysOverdue * 1.0);
        }

        loanDao.update(loan);

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookDao.update(book);

        return loan;
    }

    /**
     * Lists the loans that are still open and past their due date.
     *
     * @return the overdue loans.
     * @throws SQLException if the query fails.
     */
    public List<Loan> overdueLoans() throws SQLException {
        LocalDate today = LocalDate.now();
        return loanDao.findAll().stream()
                .filter(loan -> !loan.isReturned() && loan.getDueDate().isBefore(today))
                .toList();
    }
}
