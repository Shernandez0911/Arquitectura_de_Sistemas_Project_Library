/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import java.time.LocalDate;

/**
 * Single source of truth for the loan period and the overdue fee rate.
 */
public final class LoanPolicy {

    /**
     * Loan period in days.
     */
    public static final int DUE_DAYS = 21;

    /**
     * Fee charged for each day a loan is past its due date.
     */
    public static final double FEE_PER_DAY = 1.0;

    /**
     * Utility class: not instantiable.
     */
    private LoanPolicy() {
    }

    /**
     * Computes the due date of a loan.
     *
     * @param loanDate the date the loan starts.
     * @return the loan date plus {@link #DUE_DAYS} days.
     */
    public static LocalDate dueDate(LocalDate loanDate) {
        return loanDate.plusDays(DUE_DAYS);
    }
}