/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.model;
import java.time.LocalDate;
import cl.ucn.disc.arqsist.library.db.LocalDatePersister;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A member's request to borrow a book. It stays pending until it is fulfilled.
 */
@DatabaseTable(tableName = "reservations")
public final class Reservation {

    /**
     * Generated primary key.
     */
    @DatabaseField(generatedId = true)
    private int id;

    /**
     * The member who made the reservation.
     */
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Member member;

    /**
     * The reserved book.
     */
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Book book;

    /**
     * The date the reservation was made.
     */
    @DatabaseField(canBeNull = false, persisterClass = LocalDatePersister.class)
    private LocalDate reservedAt;

    /**
     * Whether the reservation has been turned into a loan.
     */
    @DatabaseField
    private boolean fulfilled;

    /**
     * No-argument constructor required by ORMLite and by JSON deserialization.
     */
    public Reservation() {
    }

    /**
     * Creates a pending reservation.
     *
     * @param member     the member who reserves.
     * @param book       the reserved book.
     * @param reservedAt the date of the reservation.
     */
    public Reservation(Member member, Book book, LocalDate reservedAt) {
        this.member = member;
        this.book = book;
        this.reservedAt = reservedAt;
        this.fulfilled = false;
    }

    /**
     * Returns the reservation id.
     *
     * @return the id.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the reservation id.
     *
     * @param id the new id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the member who made the reservation.
     *
     * @return the member.
     */
    public Member getMember() {
        return member;
    }

    /**
     * Sets the member who made the reservation.
     *
     * @param member the new member.
     */
    public void setMember(Member member) {
        this.member = member;
    }

    /**
     * Returns the reserved book.
     *
     * @return the book.
     */
    public Book getBook() {
        return book;
    }

    /**
     * Sets the reserved book.
     *
     * @param book the new book.
     */
    public void setBook(Book book) {
        this.book = book;
    }

    /**
     * Returns the date of the reservation.
     *
     * @return the reservation date.
     */
    public LocalDate getReservedAt() {
        return reservedAt;
    }

    /**
     * Sets the date of the reservation.
     *
     * @param reservedAt the new reservation date.
     */
    public void setReservedAt(LocalDate reservedAt) {
        this.reservedAt = reservedAt;
    }

    /**
     * Tells whether the reservation has been fulfilled.
     *
     * @return {@code true} if it was turned into a loan.
     */
    public boolean isFulfilled() {
        return fulfilled;
    }

    /**
     * Marks the reservation as fulfilled or pending.
     *
     * @param fulfilled {@code true} if the reservation was turned into a loan.
     */
    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }
}
