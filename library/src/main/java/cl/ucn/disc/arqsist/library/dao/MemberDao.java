/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.dao;

import cl.ucn.disc.arqsist.library.model.Member;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Data access object for {@link Member} entities.
 * All CRUD operations and the transaction helper are inherited from {@link BaseDao}.
 */
public final class MemberDao extends BaseDao<Member> {

    /**
     * Creates the DAO for {@link Member}.
     *
     * @param connectionSource the connection source used to create the underlying ORMLite DAO.
     */
    public MemberDao(ConnectionSource connectionSource) {
        super(connectionSource, Member.class);
    }
}
