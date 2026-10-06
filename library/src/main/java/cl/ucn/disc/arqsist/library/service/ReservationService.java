/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Reservation service: records reservations and turns them into loans.
 */
public final class ReservationService {

    /**
     * Persistence access for reservations.
     */
    private final ReservationDao reservationDao;

    /**
     * Persistence access for books.
     */
    private final BookDao bookDao;

    /**
     * Persistence access for members.
     */
    private final MemberDao memberDao;

    /**
     * Persistence access for loans.
     */
    private final LoanDao loanDao;

    /**
     * Creates the service.
     *
     * @param reservationDao the reservation DAO.
     * @param bookDao        the book DAO.
     * @param memberDao      the member DAO.
     * @param loanDao        the loan DAO.
     */
    public ReservationService(ReservationDao reservationDao, BookDao bookDao, MemberDao memberDao, LoanDao loanDao) {
        this.reservationDao = reservationDao;
        this.bookDao = bookDao;
        this.memberDao = memberDao;
        this.loanDao = loanDao;
    }

    /**
     * Records a reservation made today.
     *
     * @param bookId   the id of the book to reserve.
     * @param memberId the id of the member who reserves.
     * @return the new reservation.
     * @throws SQLException if a read or write fails.
     */
    public Reservation reserve(int bookId, int memberId) throws SQLException {
        Book book = bookDao.findById(bookId);
        Member member = memberDao.findById(memberId);
        Reservation reservation = new Reservation(member, book, LocalDate.now());
        reservationDao.create(reservation);
        return reservation;
    }

    /**
     * Lists every reservation.
     *
     * @return all reservations.
     * @throws SQLException if the query fails.
     */
    public List<Reservation> findAll() throws SQLException {
        return reservationDao.findAll();
    }

    /**
     * Turns a pending reservation into a loan that is due according to
     * {@link LoanPolicy#dueDate(LocalDate)}.
     *
     * @param reservationId the id of the reservation to fulfill.
     * @return the new loan.
     * @throws IllegalStateException if the reservation does not exist or was already fulfilled.
     * @throws SQLException          if a read or write fails.
     */
    public Loan fulfill(int reservationId) throws SQLException {
        Reservation reservation = reservationDao.findById(reservationId);
        if (reservation == null || reservation.isFulfilled()) {
            throw new IllegalStateException("Reservation not available");
        }

        reservation.setFulfilled(true);
        reservationDao.update(reservation);

        LocalDate today = LocalDate.now();
        Loan loan = new Loan(reservation.getMember(), reservation.getBook(), today, LoanPolicy.dueDate(today));
        loanDao.create(loan);
        return loan;
    }
}
