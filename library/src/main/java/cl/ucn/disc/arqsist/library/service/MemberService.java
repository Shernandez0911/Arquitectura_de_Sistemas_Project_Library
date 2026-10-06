/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Member service: registers and lists members, and also performs checkouts.
 */
public final class MemberService {

    /**
     * Persistence access for members.
     */
    private final MemberDao memberDao;

    /**
     * Persistence access for books.
     */
    private final BookDao bookDao;

    /**
     * Persistence access for loans.
     */
    private final LoanDao loanDao;

    /**
     * Creates the service.
     *
     * @param memberDao the member DAO.
     * @param bookDao   the book DAO.
     * @param loanDao   the loan DAO.
     */
    public MemberService(MemberDao memberDao, BookDao bookDao, LoanDao loanDao) {
        this.memberDao = memberDao;
        this.bookDao = bookDao;
        this.loanDao = loanDao;
    }

    /**
     * Registers a new member.
     *
     * @param member the member to register.
     * @return the registered member.
     * @throws SQLException if the insert fails.
     */
    public Member register(Member member) throws SQLException {
        memberDao.create(member);
        return member;
    }

    /**
     * Lists every member.
     *
     * @return all members.
     * @throws SQLException if the query fails.
     */
    public List<Member> findAll() throws SQLException {
        return memberDao.findAll();
    }

    /**
     * Lends a book to a member: takes one copy out of the inventory and creates a loan
     * that is due according to {@link LoanPolicy#dueDate(LocalDate)}.
     *
     * @param memberId the id of the member who borrows.
     * @param bookId   the id of the book to borrow.
     * @return the new loan.
     * @throws SQLException if a read or write fails.
     */
    public Loan checkout(int memberId, int bookId) throws SQLException {
        Member member = memberDao.findById(memberId);
        Book book = bookDao.findById(bookId);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookDao.update(book);

        LocalDate today = LocalDate.now();
        Loan loan = new Loan(member, book, today, LoanPolicy.dueDate(today));
        loanDao.create(loan);
        return loan;
    }
}
