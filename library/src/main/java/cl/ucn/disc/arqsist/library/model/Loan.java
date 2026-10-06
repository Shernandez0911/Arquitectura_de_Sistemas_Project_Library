/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.LocalDate;
import cl.ucn.disc.arqsist.library.db.LocalDatePersister;

/**
 * A book lent to a member, from the loan date until it is returned.
 */
@DatabaseTable(tableName = "loans")
public final class Loan {

    /**
     * Generated primary key.
     */
    @DatabaseField(generatedId = true)
    private int id;

    /**
     * The member who borrowed the book.
     */
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Member member;

    /**
     * The borrowed book.
     */
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Book book;

    /**
     * The date the loan started.
     */
    @DatabaseField(canBeNull = false, persisterClass = LocalDatePersister.class)
    private LocalDate loanDate;

    /**
     * The date the book must be back.
     */
    @DatabaseField(canBeNull = false, persisterClass = LocalDatePersister.class)
    private LocalDate dueDate;

    /**
     * The date the book was returned, or {@code null} while the loan is open.
     */
    @DatabaseField (persisterClass = LocalDatePersister.class)
    private LocalDate returnDate;

    /**
     * Whether the book has been returned.
     */
    @DatabaseField
    private boolean returned;

    /**
     * Fee charged for the days the book was kept past its due date.
     */
    @DatabaseField
    private double overdueFee;

    /**
     * No-argument constructor required by ORMLite and by JSON deserialization.
     */
    public Loan() {
    }

    /**
     * Creates an open loan with no fee.
     *
     * @param member   the member who borrows.
     * @param book     the borrowed book.
     * @param loanDate the date the loan starts.
     * @param dueDate  the date the book must be back.
     */
    public Loan(Member member, Book book, LocalDate loanDate, LocalDate dueDate) {
        this.member = member;
        this.book = book;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returned = false;
        this.overdueFee = 0.0;
    }

    /**
     * Returns the loan id.
     *
     * @return the id.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the loan id.
     *
     * @param id the new id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the member who borrowed the book.
     *
     * @return the member.
     */
    public Member getMember() {
        return member;
    }

    /**
     * Sets the member who borrowed the book.
     *
     * @param member the new member.
     */
    public void setMember(Member member) {
        this.member = member;
    }

    /**
     * Returns the borrowed book.
     *
     * @return the book.
     */
    public Book getBook() {
        return book;
    }

    /**
     * Sets the borrowed book.
     *
     * @param book the new book.
     */
    public void setBook(Book book) {
        this.book = book;
    }

    /**
     * Returns the date the loan started.
     *
     * @return the loan date.
     */
    public LocalDate getLoanDate() {
        return loanDate;
    }

    /**
     * Sets the date the loan started.
     *
     * @param loanDate the new loan date.
     */
    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    /**
     * Returns the date the book must be back.
     *
     * @return the due date.
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Sets the date the book must be back.
     *
     * @param dueDate the new due date.
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Returns the date the book was returned.
     *
     * @return the return date, or {@code null} while the loan is open.
     */
    public LocalDate getReturnDate() {
        return returnDate;
    }

    /**
     * Sets the date the book was returned.
     *
     * @param returnDate the new return date.
     */
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    /**
     * Tells whether the book has been returned.
     *
     * @return {@code true} if the loan is closed.
     */
    public boolean isReturned() {
        return returned;
    }

    /**
     * Marks the loan as returned or open.
     *
     * @param returned {@code true} if the book was returned.
     */
    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    /**
     * Returns the overdue fee.
     *
     * @return the fee, {@code 0.0} if there is none.
     */
    public double getOverdueFee() {
        return overdueFee;
    }

    /**
     * Sets the overdue fee.
     *
     * @param overdueFee the new fee.
     */
    public void setOverdueFee(double overdueFee) {
        this.overdueFee = overdueFee;
    }
}
