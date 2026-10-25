/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Reservation service: records reservations and turns them into loans. Every inventory
 * change goes through {@link BookService}.
 */
public final class ReservationService {

    /**
     * Persistence access for reservations.
     */
    private final ReservationDao reservationDao;

    /**
     * Persistence access for members.
     */
    private final MemberDao memberDao;

    /**
     * Persistence access for loans.
     */
    private final LoanDao loanDao;

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
     * @param reservationDao the reservation DAO.
     * @param memberDao      the member DAO.
     * @param loanDao        the loan DAO.
     * @param bookService    the catalog service.
     * @param clock          the clock used to read the current date.
     */
    public ReservationService(ReservationDao reservationDao, MemberDao memberDao, LoanDao loanDao,
                              BookService bookService, Clock clock) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.loanDao = loanDao;
        this.bookService = bookService;
        this.clock = clock;
    }

    /**
     * Records a reservation made today.
     *
     * @param bookId   the id of the book to reserve.
     * @param memberId the id of the member who reserves.
     * @return the new reservation.
     * @throws NotFoundException if the book or the member does not exist.
     */
    public Reservation reserve(int bookId, int memberId) {
        Book book = bookService.findById(bookId);
        if (book == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new NotFoundException("Member not found: " + memberId);
        }

        Reservation reservation = new Reservation(member, book, LocalDate.now(clock));
        reservationDao.create(reservation);
        return reservation;
    }

    /**
     * Lists every reservation.
     *
     * @return all reservations.
     */
    public List<Reservation> findAll() {
        return reservationDao.findAll();
    }

    /**
     * Turns a pending reservation into a loan that is due according to
     * {@link LoanPolicy#dueDate(LocalDate)}. The reservation update, the inventory change
     * and the loan creation run in one transaction.
     *
     * @param reservationId the id of the reservation to fulfill.
     * @return the new loan.
     * @throws NotFoundException     if the reservation does not exist.
     * @throws IllegalStateException if the reservation was already fulfilled, or the book
     *                               has no available copies.
     */
    public Loan fulfill(int reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        if (reservation == null) {
            throw new NotFoundException("Reservation not found: " + reservationId);
        }
        if (reservation.isFulfilled()) {
            throw new IllegalStateException("Reservation already fulfilled: " + reservationId);
        }

        LocalDate today = LocalDate.now(clock);
        int bookId = reservation.getBook().getId();
        return inTransaction(() -> {
            reservation.setFulfilled(true);
            reservationDao.update(reservation);
            bookService.borrow(bookId);
            Book book = bookService.findById(bookId);
            Loan loan = new Loan(reservation.getMember(), book, today, LoanPolicy.dueDate(today));
            loanDao.create(loan);
            return loan;
        });
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
            return reservationDao.transaction(work);
        } catch (SQLException e) {
            throw new RuntimeException("Transaction failed", e);
        }
    }
}
