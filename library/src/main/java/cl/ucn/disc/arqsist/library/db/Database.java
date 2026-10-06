/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.db;

import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class Database {

    private static final Logger log = LoggerFactory.getLogger(Database.class);

    private final ConnectionSource connectionSource;

    public Database(String jdbcUrl) throws SQLException {
        this.connectionSource = new JdbcConnectionSource(jdbcUrl);
        TableUtils.createTableIfNotExists(connectionSource, Book.class);
        TableUtils.createTableIfNotExists(connectionSource, Member.class);
        TableUtils.createTableIfNotExists(connectionSource, Loan.class);
        TableUtils.createTableIfNotExists(connectionSource, Reservation.class);
    }

    public ConnectionSource connectionSource() {
        return connectionSource;
    }

    public void seedIfEmpty() throws SQLException {
        Dao<Book, Integer> bookDao = DaoManager.createDao(connectionSource, Book.class);
        if (bookDao.queryForAll().isEmpty()) {
            log.debug("Seeding books");
            bookDao.create(new Book("Clean Code", "Robert C. Martin", "9780132350884", 3));
            bookDao.create(new Book("The Pragmatic Programmer", "Hunt & Thomas", "9780201616224", 2));
            bookDao.create(new Book("Design Patterns", "Gamma et al.", "9780201633610", 4));
        }

        Dao<Member, Integer> memberDao = DaoManager.createDao(connectionSource, Member.class);
        if (memberDao.queryForAll().isEmpty()) {
            log.debug("Seeding members");
            memberDao.create(new Member("Ada Lovelace", "ada@example.com"));
            memberDao.create(new Member("Grace Hopper", "grace@example.com"));
            memberDao.create(new Member("Alan Turing", "alan@example.com"));
        }

        Dao<Loan, Integer> loanDao = DaoManager.createDao(connectionSource, Loan.class);
        if (loanDao.queryForAll().isEmpty()) {
            log.debug("Seeding loans");
            List<Book> books = bookDao.queryForAll();
            List<Member> members = memberDao.queryForAll();
            LocalDate today = LocalDate.now();

            // Returned loan: the copy is back, so the available count does not change.
            log.debug("Creating returned loan");
            Loan returned = new Loan(members.getFirst(), books.getFirst(), today.minusDays(30), today.minusDays(9));
            returned.setReturned(true);
            returned.setReturnDate(today.minusDays(10));
            loanDao.create(returned);

            // Active loan: one copy is out.
            log.debug("Creating active loan");
            Book activeBook = books.get(2);
            loanDao.create(new Loan(members.get(1), activeBook, today.minusDays(2), today.plusDays(12)));
            activeBook.setAvailableCopies(activeBook.getAvailableCopies() - 1);
            bookDao.update(activeBook);

            // Overdue loan: open and past its due date, one copy is out.
            log.debug("Creating overdue loan");
            Book overdueBook = books.get(1);
            loanDao.create(new Loan(members.get(2), overdueBook, today.minusDays(30), today.minusDays(9)));
            overdueBook.setAvailableCopies(overdueBook.getAvailableCopies() - 1);
            bookDao.update(overdueBook);
        }

        Dao<Reservation, Integer> reservationDao = DaoManager.createDao(connectionSource, Reservation.class);
        if (reservationDao.queryForAll().isEmpty()) {
            log.debug("Seeding reservations");
            List<Book> books = bookDao.queryForAll();
            List<Member> members = memberDao.queryForAll();
            LocalDate today = LocalDate.now();

            reservationDao.create(new Reservation(members.getFirst(), books.get(1), today.minusDays(1)));
        }
    }
}