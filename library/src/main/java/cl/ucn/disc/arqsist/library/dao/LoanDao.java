/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.dao;

import cl.ucn.disc.arqsist.library.model.Loan;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Data access object for {@link Loan} entities.
 * All CRUD operations and the transaction helper are inherited from {@link BaseDao}.
 */
public final class LoanDao extends BaseDao<Loan> {

    /**
     * Creates the DAO for {@link Loan}.
     *
     * @param connectionSource the connection source used to create the underlying ORMLite DAO.
     */
    public LoanDao(ConnectionSource connectionSource) {
        super(connectionSource, Loan.class);
    }
}
